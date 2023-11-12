package com.artemus.inlineCompletionApi

import com.artemus.ShowTestPreview
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.BulkAwareDocumentListener
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.wm.IdeFocusManager

class DocumentListener: BulkAwareDocumentListener {
    override fun documentChangedNonBulk(event: DocumentEvent) {
        val editor = getActiveEditor(event.document) ?: return

        //if preview not shown.. then trigger a new preview.. otherwise ignore
        if (CompletionPreview.getInstance(editor) != null) {
            println("Already has a preview")
            return
        }
        else {
            // Trigger inside InvokeLater to avoid write conflicts
            val r = Runnable {
//                ShowTestPreview().createPreview(editor, "Document changed\nsdkjfh")
            }
            ApplicationManager.getApplication().invokeLater(r)
        }
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