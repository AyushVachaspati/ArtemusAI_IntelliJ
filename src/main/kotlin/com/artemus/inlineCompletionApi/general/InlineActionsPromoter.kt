package com.artemus.inlineCompletionApi.general

import com.artemus.inlineCompletionApi.CompletionPreview
import com.artemus.inlineCompletionApi.actions.InlineCompletionAction
import com.intellij.codeInsight.lookup.impl.actions.ChooseItemAction
import com.intellij.openapi.actionSystem.ActionPromoter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import java.util.stream.Collectors

class InlineActionsPromoter: ActionPromoter {
    override fun promote(actions: List<AnAction>, context: DataContext): List<AnAction>? {
        val editor = CommonDataKeys.EDITOR.getData(context)
        if (editor != null) {
            val preview = CompletionPreview.getInstance(editor)
            if (preview != null) {
                return actions.filter { action: AnAction? ->
                        action is ChooseItemAction || action is InlineCompletionAction }
            }
        }
        return emptyList()
    }
}