package com.artemus.inlineCompletionApi.render

import com.artemus.inlineCompletionApi.CompletionType
import com.artemus.inlineCompletionApi.InlineCompletionItem
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import java.awt.Rectangle

interface ArtemusInlay : Disposable {
    // TODO: Check if we can remove isEmpty and Offset
    val offset: Int?
    val isEmpty: Boolean

    fun getBounds(): Rectangle?
    fun render(editor: Editor, completion: InlineCompletionItem, completionType: CompletionType)

    companion object {
        @JvmStatic
        fun create(parent: Disposable): ArtemusInlay {
            // Define which Inlay should be used globally. Can also send different Inlay based on some conditions.
            return DefaultInlay(parent)
        }
    }
}