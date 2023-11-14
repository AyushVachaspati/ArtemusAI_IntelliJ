package com.artemus.inlineCompletionApi.actions

import com.artemus.inlineCompletionApi.CompletionPreview
import com.intellij.codeInsight.hint.HintManagerImpl
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler

object AcceptInlineCompletion :
    EditorAction(AcceptInlineCompletionHandler()),
    HintManagerImpl.ActionToIgnore,
    InlineCompletionAction {
    const val ACTION_ID = "AcceptInlineCompletion"

    class AcceptInlineCompletionHandler : EditorWriteActionHandler() {
        override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext) {
            CompletionPreview.getInstance(editor)?.applyPreview(editor)
        }

        override fun isEnabledForCaret(
            editor: Editor,
            caret: Caret,
            dataContext: DataContext
        ): Boolean {
            return CompletionPreview.getInstance(editor) != null
        }
    }
}
