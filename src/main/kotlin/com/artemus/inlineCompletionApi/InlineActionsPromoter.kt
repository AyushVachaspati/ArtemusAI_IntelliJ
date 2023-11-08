package com.artemus.inlineCompletionApi

import com.intellij.openapi.actionSystem.ActionPromoter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import java.util.stream.Collectors

class InlineActionsPromoter: ActionPromoter {
    override fun promote(actions: List<out AnAction>, context: DataContext): List<AnAction>? {
        val editor = CommonDataKeys.EDITOR.getData(context)
        if (editor != null) {
            val preview = CompletionPreview.getInstance(editor)
            if (preview != null) {
                return actions.stream()
                    .filter { action: AnAction? -> action is InlineCompletionAction }
                    .collect(Collectors.toList())
            }
        }
        return emptyList()
    }
}