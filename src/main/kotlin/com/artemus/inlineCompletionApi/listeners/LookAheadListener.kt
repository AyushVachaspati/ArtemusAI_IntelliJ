package com.artemus.inlineCompletionApi.listeners

import com.artemus.ShowTestPreview
import com.artemus.inlineCompletionApi.CompletionPreview
import com.artemus.inlineCompletionApi.inlineCompletionGlobalState.GlobalState
import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.LookupListener
import com.intellij.openapi.application.ApplicationManager
import java.util.Random

object LookAheadListener : LookupListener {

    override fun currentItemChanged(event: LookupEvent) {

        val editor = event.lookup.editor
        println("Look Ahead Item Changed")
        CompletionPreview.clear(editor)
        GlobalState.clearedByLookupItemChange = true
        val r = Runnable {
            GlobalState.clearedByLookupItemChange = false
            if(event.item != null) {
                val userPrefix = event.lookup.itemPattern(event.item!!)
                // TODO: This adjustment needs to be done to account for delay in user typing and completion delay
                ShowTestPreview().createPreviewLookAhead(
                    event.lookup.editor,
                    "${event.item?.lookupString?.substring(userPrefix.length)} something something"
                )
            }
        }
        ApplicationManager.getApplication().invokeLater(r)
        println(event.item)
    }

    override fun lookupCanceled(event: LookupEvent) {
        val editor = event.lookup.editor
        if (event.isCanceledExplicitly) {
            CompletionPreview.clear(editor)
            return
        }
        println("Look Ahead Item Cancelled")
        val r = Runnable {
            ShowTestPreview().createPreviewInline(editor!!, "cancelled lookahead ${Random().ints(1).average()}")
        }
        ApplicationManager.getApplication().invokeLater(r)
    }

    override fun itemSelected(event: LookupEvent) {

        val editor=event.lookup.editor
        println("Look Ahead Item Selected")
        val r = Runnable {
            ShowTestPreview().createPreviewInline(editor!!, "accepted look ahead ${Random().ints(1).average()}")
        }
        ApplicationManager.getApplication().invokeLater(r)
    }
}
