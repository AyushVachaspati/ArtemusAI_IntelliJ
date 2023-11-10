package com.artemus.inlineCompletionApi.listeners

import com.artemus.inlineCompletionApi.CompletionPreview
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.FocusChangeListener
import com.intellij.util.ObjectUtils

class InlineFocusListener(private val completionPreview: CompletionPreview) : FocusChangeListener {
    init {
        ObjectUtils.consumeIfCast(
            completionPreview.editor, EditorEx::class.java
        ) { e: EditorEx -> e.addFocusListener(this, completionPreview) }
    }

    override fun focusGained(editor: Editor) {
        CompletionPreview.clear(editor)
    }
    override fun focusLost(editor: Editor) {
        CompletionPreview.clear(editor)
    }

}
