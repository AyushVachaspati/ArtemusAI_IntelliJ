package com.artemus.completionProvider

import com.artemus.inlineCompletionApi.InlineCompletionItem
import com.artemus.inlineCompletionApi.InlineCompletionProvider
import com.intellij.openapi.editor.Editor

class ArtemusCompletionProvider: InlineCompletionProvider {
    override suspend fun getInlineCompletion(editor: Editor, triggerOffset: Int):List<InlineCompletionItem> {
        return emptyList()
    }

    override suspend fun getLookAheadCompletion(
        editor: Editor,
        lookAheadItem: String,
        userPrefix: String,
        triggerOffset: Int):List<InlineCompletionItem> {
        return emptyList()
    }
}