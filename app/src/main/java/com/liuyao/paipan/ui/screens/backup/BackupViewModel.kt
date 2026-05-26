package com.liuyao.paipan.ui.screens.backup

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liuyao.paipan.data.backup.ExportManager
import com.liuyao.paipan.data.backup.ExportPreview
import com.liuyao.paipan.data.backup.ImportManager
import com.liuyao.paipan.data.backup.ImportMode
import com.liuyao.paipan.data.backup.ImportPreview
import com.liuyao.paipan.data.db.AppDatabase
import com.liuyao.paipan.data.db.LiuYaoRepository
import com.liuyao.paipan.domain.imports.ImportedTextReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BackupUiState(
    val preview: ExportPreview? = null,
    val importPreview: ImportPreview? = null,
    val importJsonText: String? = null,
    val message: String? = null,
    val busy: Boolean = false,
)

class BackupViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = LiuYaoRepository(AppDatabase.get(app))
    private val exporter = ExportManager(repo)
    private val importer = ImportManager(repo)

    private val _ui = MutableStateFlow(BackupUiState())
    val ui: StateFlow<BackupUiState> = _ui.asStateFlow()

    fun previewRules() = viewModelScope.launch {
        _ui.update { it.copy(busy = true, message = null) }
        val p = exporter.exportRulesJson()
        _ui.update { it.copy(preview = p, busy = false) }
    }

    fun previewCases() = viewModelScope.launch {
        _ui.update { it.copy(busy = true, message = null) }
        val p = exporter.exportCasesJson()
        _ui.update { it.copy(preview = p, busy = false) }
    }

    fun previewCaseMarkdown(caseId: String) = viewModelScope.launch {
        _ui.update { it.copy(busy = true, message = null) }
        val p = exporter.exportCaseMarkdown(caseId)
        _ui.update { it.copy(preview = p, busy = false, message = if (p == null) "案例不存在" else null) }
    }

    fun previewChartMarkdown(chartId: String) = viewModelScope.launch {
        _ui.update { it.copy(busy = true) }
        val p = exporter.exportChartMarkdown(chartId)
        _ui.update { it.copy(preview = p, busy = false, message = if (p == null) "排盘不存在" else null) }
    }

    fun clearPreview() = _ui.update { it.copy(preview = null) }

    fun writeToUri(uri: Uri, content: String) = viewModelScope.launch {
        try {
            withContext(Dispatchers.IO) {
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use {
                    it.write(content.toByteArray(Charsets.UTF_8))
                } ?: throw IllegalStateException("无法写入文件")
            }
            _ui.update { it.copy(message = "导出成功") }
        } catch (e: Exception) {
            _ui.update { it.copy(message = "导出失败：${e.message}") }
        }
    }

    fun loadImportFile(uri: Uri) = viewModelScope.launch {
        _ui.update { it.copy(busy = true, message = null) }
        try {
            val text = withContext(Dispatchers.IO) {
                val bytes = getApplication<Application>().contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalStateException("无法打开文件")
                when (val outcome = ImportedTextReader.read(bytes)) {
                    is ImportedTextReader.ReadOutcome.Success -> outcome.result.text
                    is ImportedTextReader.ReadOutcome.Failure -> throw IllegalStateException(outcome.message)
                }
            }
            val p = importer.preview(text)
            _ui.update { it.copy(importJsonText = text, importPreview = p, busy = false) }
        } catch (e: Exception) {
            _ui.update { it.copy(busy = false, message = "读取失败：${e.message}") }
        }
    }

    fun confirmRestore(mode: ImportMode, onDone: () -> Unit = {}) = viewModelScope.launch {
        val text = _ui.value.importJsonText ?: return@launch
        val type = _ui.value.importPreview?.type ?: return@launch
        _ui.update { it.copy(busy = true) }
        try {
            val n = when (type) {
                com.liuyao.paipan.data.backup.JsonSchema.TYPE_RULES -> importer.restoreRules(text, mode)
                com.liuyao.paipan.data.backup.JsonSchema.TYPE_CASES -> importer.restoreCases(text, mode)
                else -> 0
            }
            _ui.update { it.copy(busy = false, message = "已恢复 $n 条") }
            onDone()
        } catch (e: Exception) {
            _ui.update { it.copy(busy = false, message = "恢复失败：${e.message}") }
        }
    }

    fun clearImport() = _ui.update { it.copy(importPreview = null, importJsonText = null) }
}
