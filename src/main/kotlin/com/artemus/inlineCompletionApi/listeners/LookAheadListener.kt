package com.artemus.inlineCompletionApi.listeners

import com.artemus.ShowTestPreview
import com.artemus.inlineCompletionApi.CompletionPreview
import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.LookupListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import java.util.Random

object LookAheadListener : LookupListener {
    var editor: Editor? = null

    override fun currentItemChanged(event: LookupEvent) {
        // This is lookaheadsuggestion with item.
        // if the item is null.. then cancel preview and return.
        // else if current completions have current item as prefix then keep them. otherwise trigger new completion
        // with completion type as lookaheadsuggestion
        // make sure current completion shown remains shown, as long as it keeps matching (wrt ctrl + ] ,  ctrl + ])
        println("Look Ahead Item Changed")
//            ShowTestPreview().createPreview(event.lookup.editor, "This is completion from lookup item: $event.item")
        println(event.item)
    }

    override fun lookupCanceled(event: LookupEvent) {
        if(event.isCanceledExplicitly)
            return

        CompletionPreview.clear(editor!!)
        // TODO: Need to call new preview with completionType = Inline from here
        println("Look Ahead Item Cancelled")
        val r = Runnable {
            ShowTestPreview().createPreview(editor!!, "Look ahead cancelled ${Random().ints(1).average()}")
        }
        ApplicationManager.getApplication().invokeLater(r)
    }

    override fun itemSelected(event: LookupEvent) {
        // TODO: Need to call new preview with completionType = Inline from here
        println("Look Ahead Item Selected")
        val r = Runnable {
            ShowTestPreview().createPreview(editor!!, "Look ahead accepted ${Random().ints(1).average()}")
        }
        ApplicationManager.getApplication().invokeLater(r)
    }
}
