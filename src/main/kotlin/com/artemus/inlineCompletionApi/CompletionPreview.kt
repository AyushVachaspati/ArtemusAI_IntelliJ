package com.artemus.inlineCompletionApi

import com.artemus.inlineCompletionApi.render.ArtemusInlay
import com.artemus.inlineCompletionApi.render.ArtemusInlay.Companion.create
import com.intellij.openapi.Disposable
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.refactoring.rename.inplace.InplaceRefactoring
import com.jetbrains.rd.util.printlnError

class CompletionPreview private constructor(
    val editor: Editor, private val completions: List<String>, private val offset: Int
) : Disposable {
    private var artemusInlay: ArtemusInlay
    private var currentIndex = 0

    init {
        EditorUtil.disposeWithEditor(editor, this)
        artemusInlay = create(this)
    }

    val currentCompletion: String
        get() = completions[currentIndex]

    fun togglePreview(diff: Int) {
        val nextIndex = currentIndex + diff
        currentIndex = (completions.size + nextIndex) % completions.size

        Disposer.dispose(artemusInlay)
        artemusInlay = ArtemusInlay.create(this);

        createPreview();
    }


    private fun createPreview(): String? {
        val completion = completions[currentIndex]

        // conditions to check when showing preview
        return if (editor !is EditorImpl
            || editor.selectionModel.hasSelection() || InplaceRefactoring.getActiveInplaceRenamer(editor) != null
        ) {
            null
        } else try {
            editor.document.startGuardedBlockChecking()
            artemusInlay.render(editor, completion, offset)
            completion
        } finally {
            editor.document.stopGuardedBlockChecking()
        }
    }

    override fun dispose() {
        editor.putUserData(INLINE_COMPLETION_PREVIEW, null)
    }

    fun applyPreview(editor: Editor?) {
        if(editor==null) return
        try {
            applyPreviewInternal(editor)
        } catch (e: Error) {
            printlnError("Failed in the processes of accepting completion$e")
        } finally {
            Disposer.dispose(this)
        }
    }

        private fun applyPreviewInternal(editor: Editor) {

            clear(editor) // Remove the Inlay Currently shown

            val completion = completions[currentIndex]
            val startOffset = editor.caretModel.offset
            var endOffset = editor.caretModel.visualLineEnd
            endOffset = if (endOffset==startOffset) endOffset else endOffset-1

            val oldSuffix = editor.document.getText(TextRange(startOffset, endOffset)) // old suffix is just until eol for us
            val endIndex = completion.indexOf(oldSuffix)
            if(endIndex==-1){
                // just insert as is
                val r = Runnable {
                    editor.document.insertString(startOffset, completion)
                }
                WriteCommandAction.runWriteCommandAction(editor.project, r)
            }
            else{
                //remove the suffix and then insert
                val r = Runnable {
                    editor.document.deleteString(startOffset, startOffset + oldSuffix.length)
                    editor.document.insertString(startOffset, completion)
                }
                WriteCommandAction.runWriteCommandAction(editor.project, r)
            }
            editor.caretModel.moveToOffset(startOffset+completion.length)
      }
    companion object {
        private val INLINE_COMPLETION_PREVIEW = Key.create<CompletionPreview>("INLINE_COMPLETION_PREVIEW")
        fun createInstance(
            editor: Editor, completions: List<String>, offset: Int
        ): String? {

            //Clear any currently showing Preview. Specially when called manually.
            clear(editor)
            val preview = CompletionPreview(editor, completions, offset)
            editor.putUserData(INLINE_COMPLETION_PREVIEW, preview)
            return preview.createPreview()
        }

        fun getCurrentCompletion(editor: Editor): String? {
            val preview = getInstance(editor) ?: return null
            return preview.currentCompletion
        }

        fun getInstance(editor: Editor?): CompletionPreview? {
            if(editor==null) return null
            return editor.getUserData(INLINE_COMPLETION_PREVIEW)
        }

        fun clear(editor: Editor) {
            val completionPreview = getInstance(editor)
            if (completionPreview != null) {
                Disposer.dispose(completionPreview)
            }
        }
    }
}
