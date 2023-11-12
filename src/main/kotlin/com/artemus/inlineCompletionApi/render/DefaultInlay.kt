package com.artemus.inlineCompletionApi.render

import com.artemus.inlineCompletionApi.CompletionPreview
import com.artemus.inlineCompletionApi.CompletionPreviewInsertionHint
import com.artemus.inlineCompletionApi.InlineCompletionItem
import com.artemus.inlineCompletionApi.general.Utils
import com.artemus.inlineCompletionApi.render.GraphicsUtils.getTabSize
import com.artemus.inlineCompletionApi.render.inlineStringProcessor.determineRendering
import com.intellij.openapi.Disposable
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.TextRange
import com.jetbrains.rd.util.printlnError
import java.awt.Rectangle


class DefaultInlay(parent: Disposable) : ArtemusInlay {
    private var beforeSuffixInlay: Inlay<*>? = null
    private var afterSuffixInlay: Inlay<*>? = null
    private var blockInlay: Inlay<*>? = null
    private var insertionHint: CompletionPreviewInsertionHint? = null
    private var editor: Editor? = null
    init {
        Disposer.register(parent, this)
    }

    // TODO: Should we remove offset
    override val offset: Int?
        get() = beforeSuffixInlay?.offset ?: afterSuffixInlay?.offset ?: blockInlay?.offset

    override fun getBounds(): Rectangle? {
        val result = beforeSuffixInlay?.bounds?.let { Rectangle(it) }

        result?.bounds?.let {
            afterSuffixInlay?.bounds?.let { after -> result.add(after) }
            blockInlay?.bounds?.let { blockBounds -> result.add(blockBounds) }
        }

        return result
    }

    override val isEmpty: Boolean
        get() = beforeSuffixInlay == null && afterSuffixInlay == null && blockInlay == null

    override fun dispose() {
        beforeSuffixInlay?.let {
            Disposer.dispose(it)
            beforeSuffixInlay = null
        }
        afterSuffixInlay?.let {
            Disposer.dispose(it)
            afterSuffixInlay = null
        }
        blockInlay?.let {
            Disposer.dispose(it)
            blockInlay = null
        }
        editor?.let {
            // TODO: Removes the \n we add for block rendering. We need good testing for this.
            //  test case when user cancel preview by doing undo
            //  test case when user closes the project/ file while preview is showing
            val project = editor!!.project
            UndoManager.getInstance(project!!)
                .undo(FileEditorManager.getInstance(project)
                    .getSelectedEditor(FileDocumentManager.getInstance().getFile(editor!!.document)!!)
                )
        }
    }

    //TODO: Only 1 Substring in the first line is supported. More general solution with subsequence
    // and multi-line subsequence of the completion might be possible. But not necessary for current MVP
    override fun render(editor: Editor, completion: InlineCompletionItem) {
        // TODO: implement completion interface with insertText and Range parameters
        var lines = Utils.asLines(completion.insertText)   // completion.insertText is the API I want
        if (lines.isEmpty()) return

        val tabSize = getTabSize(editor)
        lines = lines.map { it.replace("\t"," ".repeat(if (tabSize != null) tabSize else 4 ))}
        val firstLine = lines[0]
        val lastLine = lines[lines.size-1]

        val startOffset = editor.caretModel.offset
        var endOffset = editor.caretModel.visualLineEnd
        endOffset = if (endOffset==startOffset) endOffset else endOffset-1


        if(startOffset != completion.startOffset){
            printlnError("Current Offset $startOffset, Replace Start Offset ${completion.startOffset} are not compatible")
            return
        }

        val oldSuffixSameLine = editor.document.getText(TextRange(startOffset, endOffset)) // getting text till EOL
        var replaceSuffix = editor.document.getText(TextRange(completion.startOffset, completion.endOffset)) // old suffix is what the user gives offset for
        val oldEndIndex = oldSuffixSameLine.indexOf(replaceSuffix)

        //Not allowed to replace more than up to current line-end right now
        if(oldEndIndex == -1){
            CompletionPreview.clear(editor)
            throw InvalidDataException("Not allowed to replace more than current line. Check if completion.endOffset is correct")
        }

        replaceSuffix = replaceSuffix.trimEnd()
        val endIndex = if(replaceSuffix.isEmpty()) firstLine.length-1 else firstLine.indexOf(replaceSuffix)

        val instructions = determineRendering(lines, replaceSuffix)

        var newOffset: Int? = null
        if(instructions.shouldRenderBlock) {
            val currentPosition = editor.caretModel.logicalPosition
            val r = Runnable {
                editor.document.insertString(startOffset + replaceSuffix.length, "\n")
                this.editor = editor
                editor.caretModel.moveToLogicalPosition(currentPosition)
            }
            WriteCommandAction.runWriteCommandAction(
                editor.project,
                "AddNextLineForPreview",
                "InlinePreviewCommands", r
            )

            val currOffset = editor.caretModel.offset
            val newLine = editor.document.getLineNumber(currOffset) + 1
            newOffset = editor.document.getLineStartOffset(newLine)
        }

        when (instructions.firstLine) {
            FirstLineRendering.BeforeSubstring -> {
                if(instructions.shouldRenderBlock){
                    renderBeforeSubstring(firstLine, endIndex+1, editor, startOffset)
                    if(lines.size>2) {
                        val otherLines = lines.subList(1, lines.size - 1)
                        renderBlock(otherLines, editor, newOffset!!)
                    }
                    renderNoSubstring(editor, lastLine, newOffset!!)
                }
                else{
                    renderBeforeSubstring(firstLine, endIndex+1, editor, startOffset)
                }
            }

            FirstLineRendering.AfterSubstring -> {
                if(instructions.shouldRenderBlock){
                    renderAfterSubstring(endIndex, replaceSuffix, firstLine, editor, startOffset)
                    if(lines.size>2) {
                        val otherLines = lines.subList(1, lines.size - 1)
                        renderBlock(otherLines, editor, newOffset!!)
                    }
                    renderNoSubstring(editor, lastLine, newOffset!!)
                }
                else{
                    renderAfterSubstring(endIndex, replaceSuffix, firstLine, editor, startOffset)
                }
            }

            FirstLineRendering.BeforeAndAfterSubstring -> {
                if(instructions.shouldRenderBlock){
                    renderBeforeSubstring(firstLine, endIndex, editor, startOffset)
                    renderAfterSubstring(endIndex, replaceSuffix, firstLine, editor, startOffset)
                    if(lines.size>2) {
                        val otherLines = lines.subList(1, lines.size - 1)
                        renderBlock(otherLines, editor, newOffset!!)
                    }
                    renderNoSubstring(editor, lastLine, newOffset!!)
                }
                else{
                    renderBeforeSubstring(firstLine, endIndex, editor, startOffset)
                    renderAfterSubstring(endIndex, replaceSuffix, firstLine, editor, startOffset)
                }
            }

            FirstLineRendering.None -> {}
        }


//        if (instructions.firstLine != FirstLineRendering.None) {
//            insertionHint = CompletionPreviewInsertionHint(editor, this, "")
//        }

    }

    private fun renderBlock(
        lines: List<String>,
        editor: Editor,
        offset: Int,
    ) {
        val blockElementRenderer = BlockElementRenderer(editor, lines, false)
        val element = editor
            .inlayModel
            .addBlockElement(
                offset,
                true,
                true,
                1,
                blockElementRenderer
            )

        element?.let { Disposer.register(this, it) }

        blockInlay = element
    }

    private fun renderInline(
        editor: Editor,
        before: String,
        offset: Int
    ): Inlay<InlineElementRenderer>? {
        val element = editor
            .inlayModel
            .addInlineElement(offset, true, InlineElementRenderer(editor, before, false))

        element?.let { Disposer.register(this, it) }

        return element
    }

    private fun renderNoSubstring(
        editor: Editor,
        firstLine: String,
        offset: Int
    ) {
        beforeSuffixInlay = renderInline(editor, firstLine, offset)
    }

    private fun renderBeforeSubstring(
        firstLine: String,
        endIndex: Int,
        editor: Editor,
        offset: Int
    ) {
        val beforeSuffix = firstLine.substring(0, endIndex)
        beforeSuffixInlay = renderInline(editor, beforeSuffix, offset)
    }

    private fun renderAfterSubstring(
        endIndex: Int,
        oldSuffix: String,
        firstLine: String,
        editor: Editor,
        offset: Int
    ) {
        val afterSuffixIndex = endIndex + oldSuffix.length
        if (afterSuffixIndex < firstLine.length) {
            afterSuffixInlay = renderInline(
                editor,
                firstLine.substring(afterSuffixIndex),
                offset + oldSuffix.length
            )
        }
    }

}
