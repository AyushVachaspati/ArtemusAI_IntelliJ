package com.artemus.inlineCompletionApi.lookAheadCompletion

import com.artemus.inlineCompletionApi.listeners.LookAheadListener
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupManager

class LookAheadCompletionContributor:CompletionContributor() {

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val lookupEx = LookupManager.getActiveLookup(parameters.editor)
        lookupEx?.removeLookupListener(LookAheadListener)
        lookupEx?.addLookupListener(LookAheadListener)
    }

}