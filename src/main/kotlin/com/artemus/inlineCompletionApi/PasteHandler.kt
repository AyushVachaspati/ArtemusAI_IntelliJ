package com.artemus.inlineCompletionApi

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

class PasteHandler(private val myOriginalHandler: EditorActionHandler) : EditorActionHandler() {
    public override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        // TODO: Need to trigger completion on Paste.
        // this is not working right now. The editor is not calling this function for some reason
            // need to investigate and ask for help from JetBrains.
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
