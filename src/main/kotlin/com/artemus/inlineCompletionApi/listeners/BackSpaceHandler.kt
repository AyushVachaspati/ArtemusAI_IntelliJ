package com.artemus.inlineCompletionApi.listeners

import com.artemus.inlineCompletionApi.CompletionPreview
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

class BackSpaceHandler(private val myOriginalHandler: EditorActionHandler) : EditorActionHandler() {
    public override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        println("BackSpace Called")

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
