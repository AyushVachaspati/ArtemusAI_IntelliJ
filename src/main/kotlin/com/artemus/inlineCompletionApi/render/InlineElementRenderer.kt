package com.artemus.inlineCompletionApi.render


import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes
import org.jetbrains.annotations.TestOnly
import java.awt.Graphics
import java.awt.Rectangle

class InlineElementRenderer(private val editor: Editor, private var line: String, private val deprecated: Boolean) :
    EditorCustomElementRenderer {
    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        val width = editor.contentComponent
            .getFontMetrics(GraphicsUtils.getFont(editor, deprecated)).stringWidth(line)
        return if(width>0) width else 1
    }

    override fun paint(
        inlay: Inlay<*>,
        g: Graphics,
        targetRegion: Rectangle,
        textAttributes: TextAttributes
    ) {
//        color = color ?: GraphicsUtils.color
        g.color = GraphicsUtils.color
        g.font = GraphicsUtils.getFont(editor, deprecated)


        g.drawString(line, targetRegion.x, targetRegion.y + editor.ascent)
    }
}
