package com.artemus.inlineCompletionApi.listeners

import com.artemus.inlineCompletionApi.InlineCompletionsManager
import com.artemus.inlineCompletionApi.CompletionPreview
import com.artemus.inlineCompletionApi.CompletionType
import com.artemus.inlineCompletionApi.InlineCompletionItem
import com.artemus.inlineCompletionApi.inlineCompletionGlobalState.GlobalState
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.BulkAwareDocumentListener
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.wm.IdeFocusManager
import com.jetbrains.rd.util.printlnError
import java.util.Random

class DocumentListener: BulkAwareDocumentListener {
    override fun documentChangedNonBulk(event: DocumentEvent) {
        val editor = getActiveEditor(event.document) ?: return
        val project = editor.project
        val currentPreview = CompletionPreview.getInstance(editor)
        val lookupEx = LookupManager.getActiveLookup(editor)

        if (currentPreview != null) {
            if(currentPreview.getCompletionType() == CompletionType.INLINE_COMPLETION){

                val undoManager = UndoManager.getInstance(project!!)
                if (undoManager.isUndoInProgress || undoManager.isRedoInProgress) {
                    println("Inside Undo")
                    if (GlobalState.isArtemusUndoInProgress) return
                    val r = Runnable {
                        CompletionPreview.clear(editor)
                    }
                    ApplicationManager.getApplication().invokeLater(r)
                    return
                }

                if(!GlobalState.isKeyPressHandlerTriggered && !GlobalState.isCreatingPreview){
                    val r = Runnable {
                        CompletionPreview.clear(editor)
                        InlineCompletionsManager.createPreviewInline(
                            editor,
                            "Document changed\nTesting${Random().ints(1).average()}"
                        )
                    }
                    ApplicationManager.getApplication().invokeLater(r)
                    return
                }
                return
            }

            //LookAheadCompletion case
            if( lookupEx == null ) return
            adjustAndShowLookAheadCompletions(editor)
            return
        }
        else {
            // if not cleared by user typing something different from suggestion. then we trigger a new suggestion here
            // This prevents Document Listener from interfering with keyPress adjustment.
            // Don't trigger inside suggestion popup
            if (GlobalState.clearedByKeyPress || GlobalState.clearedByLookupItemChange || lookupEx != null) return
            CompletionPreview.clear(editor)
            val r = Runnable {
                InlineCompletionsManager.createPreviewInline(
                    editor,
                    "Document changed\nTesting${Random().ints(1).average()}"
                )
            }
            ApplicationManager.getApplication().invokeLater(r)
            return
        }
    }

    private fun adjustAndShowLookAheadCompletions(editor: Editor) {
        val currentPreview = CompletionPreview.getInstance(editor)!!
        val r = Runnable {
            var completions = currentPreview.getCompletions()
            val currentCompletionItem = currentPreview.currentCompletion
            val currentIndex = currentPreview.getCurrentIndex()


            // filter and adjust the completions which match the typed character
            completions = adjustLookAheadCompletions(editor, completions, currentCompletionItem, currentIndex)

            if (completions.isNotEmpty()) {
                // Create new preview with the Adjusted completions
                CompletionPreview.clear(editor)
                CompletionPreview.createInstance(
                    editor,
                    completions,
                    CompletionType.LOOK_AHEAD_COMPLETION
                )
            }
        }
        ApplicationManager.getApplication().invokeLater(r)
    }

    private fun adjustLookAheadCompletions(editor: Editor, completions: List<InlineCompletionItem>, currentCompletionItem: InlineCompletionItem, currentIndex: Int): List<InlineCompletionItem> {
        // match end on current prefix to the start of completion.insertText
        val result = ArrayList<InlineCompletionItem>()
        for ((i,it) in completions.withIndex()) {
            var overlap:String? = null
            val prefixText = editor.document.getText(TextRange(editor.caretModel.visualLineStart, editor.caretModel.offset))
            val insertText = it.insertText

            for(j in 1..prefixText.length) {
                if (prefixText.substring(prefixText.length - j) == insertText.substring(0, j)) {
                    overlap = insertText.substring(0, j)
                    break
                }
            }
            if(overlap!=null) {
                if (it.insertText == currentCompletionItem.insertText && i == currentIndex)
                // if the currently shown item matches the prefix. It should continue to be shown
                    result.add(0, InlineCompletionItem(insertText.substring(overlap.length), it.startOffset + overlap.length , it.endOffset + overlap.length ))
                else {
                    result.add( InlineCompletionItem(insertText.substring(overlap.length), it.startOffset + overlap.length , it.endOffset + overlap.length ))                }
            }
        }
        if(result.isEmpty()) return emptyList()
        return result.toList()
    }


    private fun getActiveEditor(document: Document): Editor? {
        if (!ApplicationManager.getApplication().isDispatchThread) {
            return null
        }
        val focusOwner = IdeFocusManager.getGlobalInstance().focusOwner
        val dataContext = DataManager.getInstance().getDataContext(focusOwner)
        // ignore caret placing when exiting
        var activeEditor = if (ApplicationManager.getApplication().isDisposed) null else CommonDataKeys.EDITOR.getData(dataContext)
        if (activeEditor != null && activeEditor.document !== document) {
            activeEditor = null
        }
        return activeEditor
    }
}