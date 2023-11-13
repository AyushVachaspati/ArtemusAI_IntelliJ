package com.artemus.inlineCompletionApi.actions

import com.artemus.inlineCompletionApi.CompletionPreview
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class ShowNextInlineCompletion :
    BaseCodeInsightAction(false),
    DumbAware,
    InlineCompletionAction {
    companion object {
        const val ACTION_ID = "ShowNextInlineCompletion"
    }

    override fun getHandler(): CodeInsightActionHandler {
        return CodeInsightActionHandler { _: Project?, editor: Editor, _: PsiFile? ->
            CompletionPreview.getInstance(editor)?.togglePreview(+1)
        }
    }

    override fun isValidForLookup(): Boolean = true
}
