package com.artemus.inlineCompletionApi.render


import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes
import org.jetbrains.annotations.TestOnly
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle

class InlineElementRenderer(private val editor: Editor, private var line: String, private val deprecated: Boolean) :
    EditorCustomElementRenderer {
    private var color: Color? = null
    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        return editor.contentComponent
            .getFontMetrics(GraphicsUtils.getFont(editor, deprecated)).stringWidth(line)
    }

    @TestOnly
    fun getContent(): String {
        return line
    }

    override fun paint(
        inlay: Inlay<*>,
        g: Graphics,
        targetRegion: Rectangle,
        textAttributes: TextAttributes
    ) {
        println("InlineElementRenderer Called")
        color = color ?: GraphicsUtils.color
        g.color = color
        g.font = GraphicsUtils.getFont(editor, deprecated)

        val tabSize = getTabSize(editor)
        var stringToRender = line;
        if(tabSize!=null){
            stringToRender = line.replace("\t", " ".repeat(tabSize))
        }

        g.drawString(stringToRender, targetRegion.x, targetRegion.y + editor.ascent)
    }
}
