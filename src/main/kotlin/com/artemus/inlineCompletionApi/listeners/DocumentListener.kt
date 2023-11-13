package com.artemus.inlineCompletionApi.listeners

import com.artemus.ShowTestPreview
import com.artemus.inlineCompletionApi.CompletionPreview
import com.artemus.inlineCompletionApi.inlineCompletionGlobalState.GlobalState
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.BulkAwareDocumentListener
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.wm.IdeFocusManager
import java.util.Random

class DocumentListener: BulkAwareDocumentListener {


    override fun documentChangedNonBulk(event: DocumentEvent) {
        val editor = getActiveEditor(event.document) ?: return

        if (CompletionPreview.getInstance(editor) != null) {
//            println("Already has a preview")
            return
        }

        val project = editor.project
        val undoManager = UndoManager.getInstance(project!!)
        if(undoManager.isUndoInProgress){
            if(!GlobalState.isArtemusUndoInProgress) {
                val r = Runnable { CompletionPreview.clear(editor) }
                ApplicationManager.getApplication().invokeLater(r)
            }
        }

        if(!GlobalState.clearedByKeyPress) {
            val r = Runnable {
                ShowTestPreview().createPreview(editor, "Document changed\nsdkjfh${Random().ints(1).average()}")
            }
            ApplicationManager.getApplication().invokeLater(r)
        }

        return
    }


    private fun getActiveEditor(document: Document): Editor? {
        if (!ApplicationManager.getApplication().isDispatchThread) {
            return null
        }
        val focusOwner = IdeFocusManager.getGlobalInstance().focusOwner
        val dataContext = DataManager.getInstance().getDataContext(focusOwner)
        // ignore caret placing when exiting
        var activeEditor =
            if (ApplicationManager.getApplication().isDisposed) null else CommonDataKeys.EDITOR.getData(dataContext)
        if (activeEditor != null && activeEditor.document !== document) {
            activeEditor = null
        }
        return activeEditor
    }
}