package com.artemus.inlineCompletionApi.render

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import java.awt.Rectangle

interface ArtemusInlay : Disposable {
    val offset: Int?
    val isEmpty: Boolean

    fun getBounds(): Rectangle?
    fun render(editor: Editor, completion: String, offset: Int)

    companion object {
        @JvmStatic
        fun create(parent: Disposable): ArtemusInlay {
            return DefaultInlay(parent)
        }
    }
}
