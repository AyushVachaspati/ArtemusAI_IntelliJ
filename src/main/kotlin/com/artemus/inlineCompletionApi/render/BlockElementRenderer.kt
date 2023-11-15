package com.artemus.inlineCompletionApi.render

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Graphics
import java.awt.Rectangle

class BlockElementRenderer(
    private val editor: Editor,
    private val blockText: List<String>,
    private val deprecated: Boolean
) : EditorCustomElementRenderer {

    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        val firstLine = blockText[0]
        val width = editor.contentComponent
            .getFontMetrics(GraphicsUtils.getFont(editor, deprecated)).stringWidth(firstLine)
        return if(width>0) width else 1
    }

    override fun calcHeightInPixels(inlay: Inlay<*>): Int {
        return editor.lineHeight * blockText.size
    }

    override fun paint(
        inlay: Inlay<*>,
        g: Graphics,
        targetRegion: Rectangle,
        textAttributes: TextAttributes
    ) {
        blockText.withIndex().forEach { (i, line) ->
            g.color = GraphicsUtils.color
            g.font = GraphicsUtils.getFont(editor, deprecated)

            g.drawString(
                line,
                0,
                targetRegion.y + i * editor.lineHeight + editor.ascent
            )
        }
    }

}
