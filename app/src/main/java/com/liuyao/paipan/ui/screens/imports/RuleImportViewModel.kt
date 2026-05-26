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

/** 导入完成统计 */
data class ImportResult(val success: Int, val skipped: Int, val failed: Int)

/** 导入流程状态 */
data class ImportUiState(
    val fileName: String? = null,
    val encoding: String? = null,
    val rawPreview: String = "",        // 原文预览(前若干字)
    val drafts: List<DraftRule> = emptyList(),
    val isParsing: Boolean = false,
    val importResult: ImportResult? = null, // 非空 = 已完成导入
    val error: String? = null,
    val encodingWarning: String? = null,
) {
    val reviewCount: Int get() = drafts.count { it.needsReview }
    val selectedCount: Int get() = drafts.count { it.selectedForImport }
}

class RuleImportViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = LiuYaoRepository(AppDatabase.get(app))

    private val _ui = MutableStateFlow(ImportUiState())
    val ui: StateFlow<ImportUiState> = _ui.asStateFlow()

    companion object {
        private const val PREVIEW_CHARS = 3000
    }

    /** 读取并解析选中的文件(编码自动识别) */
    fun loadFile(uri: Uri, displayName: String?) {
        _ui.update { ImportUiState(isParsing = true) }
        viewModelScope.launch {
            try {
                val outcome = withContext(Dispatchers.IO) {
                    val bytes = getApplication<Application>().contentResolver
                        .openInputStream(uri)?.use { it.readBytes() }
                    ImportedTextReader.read(bytes)
                }
                when (outcome) {
                    is ImportedTextReader.ReadOutcome.Failure -> {
                        _ui.update { it.copy(isParsing = false, error = outcome.message) }
                    }
                    is ImportedTextReader.ReadOutcome.Success -> {
                        val res = outcome.result
                        val parsed = RuleImportParser.parse(res.text)
                        if (parsed.isEmpty()) {
                            _ui.update {
                                it.copy(
                                    isParsing = false, fileName = displayName, encoding = res.encoding,
                                    rawPreview = res.text.take(PREVIEW_CHARS),
                                    error = "未能从文件中解析出断语条目,请检查文件内容或编码。",
                                    encodingWarning = res.warning,
                                )
                            }
                            return@launch
                        }
                        // 标注疑似乱码(含替换符的条目),并补充文件名/编码
                        val drafts = parsed.map { d ->
                            val garbled = d.originalText.contains('\uFFFD')
                            d.copy(
                                sourceFileName = displayName ?: "",
                                detectedEncoding = res.encoding,
                                maybeGarbled = garbled,
                                selectedForImport = !garbled, // 乱码条目默认不选
                            )
                        }
                        _ui.update {
                            it.copy(
                                isParsing = false, fileName = displayName, encoding = res.encoding,
                                rawPreview = res.text.take(PREVIEW_CHARS), drafts = drafts,
                                encodingWarning = res.warning,
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _ui.update { it.copy(isParsing = false, error = "读取失败:${e.message ?: "未知错误"}") }
            }
        }
    }

    fun updateDraft(updated: DraftRule) {
        _ui.update { st -> st.copy(drafts = st.drafts.map { if (it.id == updated.id) updated else it }) }
    }

    fun toggleSelected(id: String) {
        _ui.update { st -> st.copy(drafts = st.drafts.map { if (it.id == id) it.copy(selectedForImport = !it.selectedForImport) else it }) }
    }

    fun selectAll(selected: Boolean) {
        _ui.update { st -> st.copy(drafts = st.drafts.map { it.copy(selectedForImport = selected) }) }
    }

    fun removeDraft(id: String) {
        _ui.update { st -> st.copy(drafts = st.drafts.filterNot { it.id == id }) }
    }

    /** 确认导入:只写入选中的条目,统计成功/跳过/失败 */
    fun confirmImport(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val all = _ui.value.drafts
            val selected = all.filter { it.selectedForImport }
            val skipped = all.size - selected.size
            var success = 0
            var failed = 0
            val source = RuleSourceEntity("src-import", "导入", "刘昌明《象断六爻》", null)
            selected.forEachIndexed { i, d ->
                try {
                    repo.saveRule(d.toRule(), source = if (i == 0) source else null)
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
