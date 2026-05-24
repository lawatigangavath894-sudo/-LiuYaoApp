package com.liuyao.paipan.ui.screens.imports

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liuyao.paipan.data.db.AppDatabase
import com.liuyao.paipan.data.db.LiuYaoRepository
import com.liuyao.paipan.data.db.entity.RuleSourceEntity
import com.liuyao.paipan.domain.imports.DraftRule
import com.liuyao.paipan.domain.imports.RuleImportParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 导入流程状态。
 */
data class ImportUiState(
    val fileName: String? = null,
    val drafts: List<DraftRule> = emptyList(),
    val isParsing: Boolean = false,
    val importedCount: Int? = null, // 非空 = 已完成导入
    val error: String? = null,
) {
    val reviewCount: Int get() = drafts.count { it.needsReview }
}

class RuleImportViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = LiuYaoRepository(AppDatabase.get(app))

    private val _ui = MutableStateFlow(ImportUiState())
    val ui: StateFlow<ImportUiState> = _ui.asStateFlow()

    /** 读取并解析选中的文件 */
    fun loadFile(uri: Uri, displayName: String?) {
        _ui.update { it.copy(isParsing = true, error = null, importedCount = null) }
        viewModelScope.launch {
            try {
                val text = withContext(Dispatchers.IO) { readText(uri) }
                val drafts = RuleImportParser.parse(text)
                _ui.update {
                    it.copy(fileName = displayName, drafts = drafts, isParsing = false)
                }
            } catch (e: Exception) {
                _ui.update { it.copy(isParsing = false, error = "读取失败:${e.message}") }
            }
        }
    }

    private fun readText(uri: Uri): String {
        val resolver = getApplication<Application>().contentResolver
        return resolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: throw IllegalStateException("无法打开文件")
    }

    /** 更新单条草稿 */
    fun updateDraft(updated: DraftRule) {
        _ui.update { st ->
            st.copy(drafts = st.drafts.map { if (it.id == updated.id) updated else it })
        }
    }

    /** 删除单条草稿 */
    fun removeDraft(id: String) {
        _ui.update { st -> st.copy(drafts = st.drafts.filterNot { it.id == id }) }
    }

    /** 确认导入:把所有草稿写入 Room */
    fun confirmImport(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val source = RuleSourceEntity("src-import", "导入", "刘昌明《象断六爻》", null)
            val drafts = _ui.value.drafts
            drafts.forEachIndexed { i, d ->
                repo.saveRule(d.toRule(), source = if (i == 0) source else null)
            }
            _ui.update { it.copy(importedCount = drafts.size) }
            onDone()
        }
    }

    fun reset() {
        _ui.update { ImportUiState() }
    }
}
