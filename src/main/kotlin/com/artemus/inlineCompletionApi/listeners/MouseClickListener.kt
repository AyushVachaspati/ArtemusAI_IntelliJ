package com.artemus.inlineCompletionApi.listeners

import com.artemus.inlineCompletionApi.CompletionPreview
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.util.ObjectUtils

class MouseClickListener(private val completionPreview: CompletionPreview) : EditorMouseListener {

    init {
        ObjectUtils.consumeIfCast(
            completionPreview.editor, EditorEx::class.java
        ) { e: EditorEx -> e.addEditorMouseListener(this, completionPreview) }
    }

    override fun mousePressed(event: EditorMouseEvent) {
        CompletionPreview.clear(event.editor)
    }

}
