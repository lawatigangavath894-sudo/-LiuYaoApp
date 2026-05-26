package com.liuyao.paipan.ui.screens.imports

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liuyao.paipan.data.db.AppDatabase
import com.liuyao.paipan.data.db.LiuYaoRepository
import com.liuyao.paipan.data.db.entity.RuleSourceEntity
import com.liuyao.paipan.domain.imports.DraftRule
import com.liuyao.paipan.domain.imports.ImportedTextReader
import com.liuyao.paipan.domain.imports.RuleImportParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ImportResult(val success: Int, val skipped: Int, val failed: Int)

data class ImportUiState(
    val fileName: String? = null,
    val rawBytes: ByteArray? = null,
    val encoding: String? = null,
    val autoEncoding: String? = null,
    val encodingOptions: List<String> = emptyList(),
    val candidateResults: List<ImportedTextReader.DecodedCandidate> = emptyList(),
    val rawPreview: String = "",
    val drafts: List<DraftRule> = emptyList(),
    val isParsing: Boolean = false,
    val importResult: ImportResult? = null,
    val error: String? = null,
    val encodingWarning: String? = null,
) {
    val reviewCount: Int get() = drafts.count { it.needsReview }
    val selectedCount: Int get() = drafts.count { it.selectedForImport }
    val isPossiblyGarbled: Boolean get() = drafts.any { it.selectedForImport && it.maybeGarbled }
}

class RuleImportViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = LiuYaoRepository(AppDatabase.get(app))

    private val _ui = MutableStateFlow(ImportUiState())
    val ui: StateFlow<ImportUiState> = _ui.asStateFlow()

    companion object {
        private const val PREVIEW_CHARS = 3000
    }

    fun loadFile(uri: Uri, displayName: String?) {
        _ui.update { ImportUiState(isParsing = true) }
        viewModelScope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }
                when (val outcome = ImportedTextReader.read(bytes)) {
                    is ImportedTextReader.ReadOutcome.Failure -> {
                        _ui.update { it.copy(isParsing = false, error = outcome.message) }
                    }
                    is ImportedTextReader.ReadOutcome.Success -> {
                        val result = outcome.result
                        applyDecodedText(
                            fileName = displayName ?: "未命名文件",
                            rawBytes = bytes,
                            selectedEncoding = result.encoding,
                            autoEncoding = result.encoding,
                            decodedText = result.text,
                            warning = result.warning,
                            candidates = result.candidates,
                        )
                    }
                }
            } catch (e: Exception) {
                _ui.update { it.copy(isParsing = false, error = "读取失败：${e.message ?: "未知错误"}") }
            }
        }
    }

    fun selectEncoding(encoding: String) {
        val state = _ui.value
        val bytes = state.rawBytes ?: return
        _ui.update { it.copy(isParsing = true, error = null) }
        viewModelScope.launch {
            val selected = if (encoding == ImportedTextReader.AUTO) {
                state.candidateResults.maxByOrNull { it.score }
            } else {
                withContext(Dispatchers.Default) { ImportedTextReader.decode(bytes, encoding) }
            } ?: return@launch
            applyDecodedText(
                fileName = state.fileName ?: "未命名文件",
                rawBytes = bytes,
                selectedEncoding = selected.encoding,
                autoEncoding = state.autoEncoding ?: selected.encoding,
                decodedText = selected.text,
                warning = selected.warning,
                candidates = state.candidateResults.ifEmpty { listOf(selected) },
            )
        }
    }

    private fun applyDecodedText(
        fileName: String,
        rawBytes: ByteArray?,
        selectedEncoding: String,
        autoEncoding: String,
        decodedText: String,
        warning: String?,
        candidates: List<ImportedTextReader.DecodedCandidate>,
    ) {
        val parsed = RuleImportParser.parse(decodedText)
        val drafts = parsed.map { draft ->
            val garbled = looksGarbled(draft.originalText)
            draft.copy(
                sourceFileName = fileName,
                detectedEncoding = selectedEncoding,
                maybeGarbled = garbled,
                selectedForImport = !garbled,
            )
        }
        val parseWarning = when {
            parsed.isEmpty() -> "未能从文件中解析出断语条目，请检查文件内容或切换编码。"
            warning != null -> warning
            looksGarbled(decodedText.take(PREVIEW_CHARS)) -> "当前预览可能仍存在乱码，请尝试切换 GB18030 / GBK / UTF-8 编码。"
            else -> null
        }
        _ui.update {
            it.copy(
                fileName = fileName,
                rawBytes = rawBytes,
                encoding = selectedEncoding,
                autoEncoding = autoEncoding,
                encodingOptions = listOf(ImportedTextReader.AUTO) + ImportedTextReader.supportedEncodingNames(),
                candidateResults = candidates,
                rawPreview = decodedText.take(PREVIEW_CHARS),
                drafts = drafts,
                isParsing = false,
                importResult = null,
                error = null,
                encodingWarning = parseWarning,
            )
        }
    }

    private fun looksGarbled(text: String): Boolean =
        text.contains('\uFFFD') ||
            listOf("锟斤拷", "���", "Ã", "Â", "闁", "鐨", "鍙", "绛", "涓").any { text.contains(it) }

    fun updateDraft(updated: DraftRule) {
        _ui.update { state -> state.copy(drafts = state.drafts.map { if (it.id == updated.id) updated else it }) }
    }

    fun toggleSelected(id: String) {
        _ui.update { state ->
            state.copy(drafts = state.drafts.map { if (it.id == id) it.copy(selectedForImport = !it.selectedForImport) else it })
        }
    }

    fun selectAll(selected: Boolean) {
        _ui.update { state -> state.copy(drafts = state.drafts.map { it.copy(selectedForImport = selected) }) }
    }

    fun removeDraft(id: String) {
        _ui.update { state -> state.copy(drafts = state.drafts.filterNot { it.id == id }) }
    }

    fun confirmImport(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val all = _ui.value.drafts
            val selected = all.filter { it.selectedForImport }
            val skipped = all.size - selected.size
            var success = 0
            var failed = 0
            val sourceName = _ui.value.fileName ?: "导入文件"
            val source = RuleSourceEntity("src-import", "导入", sourceName, null)
            selected.forEachIndexed { index, draft ->
                try {
                    repo.saveRule(draft.toRule(), source = if (index == 0) source else null)
                    success++
                } catch (e: Exception) {
                    failed++
                }
            }
            _ui.update { it.copy(importResult = ImportResult(success, skipped, failed)) }
            onDone()
        }
    }

    fun reset() {
        _ui.update { ImportUiState() }
    }
}
