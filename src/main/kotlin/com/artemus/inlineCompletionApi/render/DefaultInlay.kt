package com.artemus.inlineCompletionApi.render

import com.artemus.inlineCompletionApi.CompletionPreviewInsertionHint
import com.artemus.inlineCompletionApi.general.Utils
import com.intellij.openapi.Disposable
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.command.undo.UndoableAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ModalTaskOwner.project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.utils.editor.getVirtualFile
import com.jetbrains.rd.generator.nova.PredefinedType.string
import java.awt.Rectangle
import java.util.stream.Collectors


class DefaultInlay(parent: Disposable) : ArtemusInlay {
    private var beforeSuffixInlay: Inlay<*>? = null
    private var afterSuffixInlay: Inlay<*>? = null
    private var blockInlay: Inlay<*>? = null
    private var insertionHint: CompletionPreviewInsertionHint? = null
    private var bringToEnd: String? = null
    private var editor: Editor? = null;
    init {
        Disposer.register(parent, this)
    }

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
        bringToEnd?.let {
            // TODO: Does Undo work in all cases. We need good testing for this.
            //  test case when user cancel preview by doing undo
            val project = editor?.project
            if(project!=null)  {
                UndoManager.getInstance(project).undo(
                    FileEditorManager.getInstance(project).getSelectedEditor(editor!!.virtualFile)
                )
            }
        }

    }

    //TODO: Only 1 Substring in the first line is supported. More general solution with subsequence
    // and multi-line subsequence of the completion might be possible. But not necessary for current MVP
    override fun render(editor: Editor, completion: String, start_offset: Int) {
        // TODO: implement completion interface with insertText and Range parameters
        var lines = Utils.asLines(completion)   // completion.insertText is the API I want
        if (lines.isEmpty()){
            return
        }

        val tabSize = getTabSize(editor)
        lines = lines.map { it.replace("\t"," ".repeat(if (tabSize != null) tabSize else 4 ))}
        val firstLine = lines[0]

        val start_offset = editor.caretModel.offset
        var end_offset = editor.caretModel.visualLineEnd
        end_offset = if (end_offset==start_offset) end_offset else end_offset-1;

        // old suffix is just until eol for us
        val old_suffix = editor.document.getText(TextRange(start_offset, end_offset))

        val endIndex = firstLine.indexOf(old_suffix)

        // TODO: BringToEnd text needs to go to the end of the render based on what the completion is. If last completion has next line
        val instructions = determineRendering(lines, old_suffix)

        when (instructions.firstLine) {
            FirstLineRendering.NoSuffix -> {
                renderNoSuffix(editor, firstLine, completion, start_offset)
            }
            FirstLineRendering.SuffixOnly -> {
                renderAfterSuffix(endIndex, completion, old_suffix, firstLine, editor, start_offset)
            }
            FirstLineRendering.BeforeAndAfterSuffix -> {
                renderBeforeSuffix(firstLine, endIndex, editor, completion, start_offset)
                renderAfterSuffix(endIndex, completion,old_suffix, firstLine, editor, start_offset)
            }
            FirstLineRendering.None -> {}
        }

        if (instructions.shouldRenderBlock) {
            val otherLines = lines.stream().skip(1).collect(Collectors.toList())
            renderBlock(otherLines, editor, completion, start_offset)
        }


//        if (instructions.firstLine != FirstLineRendering.None) {
//            insertionHint = CompletionPreviewInsertionHint(editor, this, "")
//        }

        // remove the extra text from editor and set editor so that it can be disposed by reinserting the correct text.
        if(endIndex==-1)
        {
            // TODO: Need to test what happens when user does undo and the preview is shown. This could cause issues.
            this.bringToEnd = old_suffix
            val r = Runnable { editor.document.deleteString(start_offset, start_offset + bringToEnd!!.length) }
            WriteCommandAction.runWriteCommandAction(editor.project, r)
            this.editor = editor
        }

    }

    private fun renderBlock(
        lines: List<String>,
        editor: Editor,
        completion: String,
        offset: Int
    ) {
        val blockElementRenderer = BlockElementRenderer(editor, lines, false)
        val element = editor
            .inlayModel
            .addBlockElement(
                offset,
                true,
                false,
                1,
                blockElementRenderer
            )

        element?.let { Disposer.register(this, it) }

        blockInlay = element
    }

    private fun renderInline(
        editor: Editor,
        before: String,
        completion: String,
        offset: Int
    ): Inlay<InlineElementRenderer>? {
        val element = editor
            .inlayModel
            .addInlineElement(offset, true, InlineElementRenderer(editor, before, false))

        element?.let { Disposer.register(this, it) }

        return element
    }

    private fun renderNoSuffix(
        editor: Editor,
        firstLine: String,
        completion: String,
        offset: Int
    ) {
        beforeSuffixInlay = renderInline(editor, firstLine, completion, offset)
    }

    private fun renderBeforeSuffix(
        firstLine: String,
        endIndex: Int,
        editor: Editor,
        completion: String,
        offset: Int
    ) {
        val beforeSuffix = firstLine.substring(0, endIndex)
        beforeSuffixInlay = renderInline(editor, beforeSuffix, completion, offset)
    }

    private fun renderAfterSuffix(
        endIndex: Int,
        completion: String,
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
                completion,
                offset + oldSuffix.length
            )
        }
    }

}
