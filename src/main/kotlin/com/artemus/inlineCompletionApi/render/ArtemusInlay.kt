package com.artemus.inlineCompletionApi.render

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import java.awt.Rectangle

interface ArtemusInlay : Disposable {
    // TODO: Check if we can remove isEmpty and Offset
    val offset: Int?
    val isEmpty: Boolean

    fun getBounds(): Rectangle?
    fun render(editor: Editor, completion: String)
}
