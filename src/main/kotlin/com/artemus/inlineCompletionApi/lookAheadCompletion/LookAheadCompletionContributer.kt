package com.artemus.inlineCompletionApi.lookAheadCompletion

import com.artemus.inlineCompletionApi.listeners.InlineLookupListener
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupManager

class LookAheadCompletionContributor:CompletionContributor() {

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        // if lookaheadcompletion is called, we register a lookup listener to handle the suggestions.

        val lookupEx = LookupManager.getActiveLookup(parameters.editor)
        lookupEx?.removeLookupListener(InlineLookupListener)
        lookupEx?.addLookupListener(InlineLookupListener)
    }

}