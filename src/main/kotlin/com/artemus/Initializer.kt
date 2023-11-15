package com.artemus

import com.artemus.completionProvider.ArtemusCompletionProvider
import com.artemus.inlineCompletionApi.InlineCompletionsManager
import com.intellij.openapi.application.PreloadingActivity
import com.intellij.openapi.progress.ProgressIndicator

class Initializer: PreloadingActivity() {
    override fun preload(indicator: ProgressIndicator) {
        InlineCompletionsManager.addCompletionProvider(ArtemusCompletionProvider())
        InlineCompletionsManager.addCompletionProvider(ArtemusCompletionProvider())
        InlineCompletionsManager.addCompletionProvider(ArtemusCompletionProvider())
        InlineCompletionsManager.addCompletionProvider(ArtemusCompletionProvider())
    }

}