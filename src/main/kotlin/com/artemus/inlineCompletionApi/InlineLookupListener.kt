package com.artemus.inlineCompletionApi

import com.artemus.ShowTestPreview
import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.LookupListener

object InlineLookupListener : LookupListener {
        override fun currentItemChanged(event: LookupEvent) {
            // This is lookaheadsuggestion with item.
            // if the item is null.. then cancel preview and return.
            // else if current completions have current item as prefix then keep them. otherwise trigger new completion
            // with completion type as lookaheadsuggestion
            // make sure current completion shown remains shown, as long as it keeps matching (wrt ctrl + ] ,  ctrl + ])
            println("current item called")
//            ShowTestPreview().createPreview(event.lookup.editor, "This is completion from lookup item: $event.item")
            println(event.item)
        }

        override fun lookupCanceled(event: LookupEvent) {
            println("Lookup Cancelled")
            // if look up is cancelled for any reason, cancel current previews and call for new preview with completion type inline
        }

        override fun itemSelected(event: LookupEvent) {
            // frop current preview and call for new preview with completion type inline
            println("Selected suggestion from popup")
        }
}
