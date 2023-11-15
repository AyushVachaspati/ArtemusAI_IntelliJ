package com.artemus.inlineCompletionApi

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor

interface InlineCompletionProvider {
    suspend fun getInlineCompletion(editor: Editor, triggerOffset: Int)

    // return a completion that must start with lookAheadItem and the replace offsets must also be based on replacing the lookahead item
    // if completion is MULTILINE it should replace the entire suffix on the same line after the caret.
    // if there's suffix the completion doesn't replace then the lookahead should be at max 1 line, else it will be truncated to 1 line.
    // userPrefix gives the prefix of caret at triggerOffset (when this function was triggered)
    suspend fun getLookAheadCompletion(editor: Editor, lookAheadItem: String, userPrefix: String, triggerOffset: Int)
}