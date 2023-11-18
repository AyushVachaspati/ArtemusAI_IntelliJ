package com.artemus.inlineCompletionApi.listeners

import com.artemus.inlineCompletionApi.CompletionPreview
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.FocusChangeListener

class InlineFocusListener(completionPreview: CompletionPreview) : FocusChangeListener {
    init {
        if(completionPreview.editor is EditorEx){
            val e = completionPreview.editor
            e.addFocusListener(this, completionPreview)
        }}

    override fun focusGained(editor: Editor) {
        CompletionPreview.clear(editor)
    }
    override fun focusLost(editor: Editor) {
        CompletionPreview.clear(editor)
    }

}
