package com.artemus.inlineCompletionApi

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

class EnterHandler(private val myOriginalHandler: EditorActionHandler) : EditorActionHandler() {
    public override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        println("Enter Called")

        // This does not count as caret offset change. So we need to cancel Preview here

        CompletionPreview.clear(editor)

        if (myOriginalHandler.isEnabled(editor, caret, dataContext)) {
            myOriginalHandler.execute(editor, caret, dataContext)
        }
    }

    public override fun isEnabledForCaret(
        editor: Editor,
        caret: Caret,
        dataContext: DataContext
    ): Boolean {
        return true
    }
}
