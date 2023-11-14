package com.artemus.inlineCompletionApi

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor

interface InlineCompletionProvider {
    fun getInlineCompletion(editor: Editor, triggerOffset: Int)

    // return a completion that must start with lookAheadItem and the replace offsets must also be based on replacing
    // the lookAheadItem + things ahead of the item.
    // userPrefix should be used to adjust the currentOffset/triggerOffset to the correct position
    fun getLookAheadCompletion(editor: Editor, lookAheadItem: String, userPrefix: String, triggerOffset: Int)
}