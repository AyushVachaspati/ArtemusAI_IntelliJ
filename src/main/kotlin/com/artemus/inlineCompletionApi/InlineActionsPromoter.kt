package com.artemus.inlineCompletionApi

import com.intellij.openapi.actionSystem.ActionPromoter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import java.util.*

class InlineActionsPromoter: ActionPromoter {
    override fun promote(actions: MutableList<out AnAction>, context: DataContext): MutableList<AnAction>? {
        val editor = CommonDataKeys.EDITOR.getData(context)
        return Collections.emptyList()
    }
}