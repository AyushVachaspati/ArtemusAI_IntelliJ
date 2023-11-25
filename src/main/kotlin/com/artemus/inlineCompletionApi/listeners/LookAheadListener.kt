package com.artemus.inlineCompletionApi.listeners

import com.artemus.inlineCompletionApi.InlineCompletionsManager
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
            val item = event.item
            if(item != null) {
                val userPrefix = event.lookup.itemPattern(item)
                InlineCompletionsManager.createPreviewLookAhead(
                    event.lookup.editor,userPrefix, item.lookupString,
                    "${item.lookupString.substring(userPrefix.length)} something something"
                )
                GlobalState.clearedByLookupItemChange = false
            }
        }
        ApplicationManager.getApplication().invokeLater(r)
    }

    override fun lookupCanceled(event: LookupEvent) {
        val editor = event.lookup.editor
        println("Look Ahead Item Cancelled")
        if(event.isCanceledExplicitly){
            CompletionPreview.clear(editor)
            val r = Runnable {
                InlineCompletionsManager.createPreviewInline(editor, "Explicit cancelled lookahead ${Random().ints(1).average()}")
            }
            ApplicationManager.getApplication().invokeLater(r)
        }
        else(
            if(CompletionPreview.getInstance(editor) == null){
                CompletionPreview.clear(editor)
                val r = Runnable {
                    InlineCompletionsManager.createPreviewInline(editor, "Implicit cancelled lookahead ${Random().ints(1).average()}")
                }
                ApplicationManager.getApplication().invokeLater(r)
            }
        )
    }

    override fun itemSelected(event: LookupEvent) {
        println("Accepted lookup")
        val editor=event.lookup.editor
        val r = Runnable {
            CompletionPreview.clear(editor)
            InlineCompletionsManager.createPreviewInline(editor, "accepted look ahead ${Random().ints(1).average()}")
        }
        ApplicationManager.getApplication().invokeLater(r)
    }
}
