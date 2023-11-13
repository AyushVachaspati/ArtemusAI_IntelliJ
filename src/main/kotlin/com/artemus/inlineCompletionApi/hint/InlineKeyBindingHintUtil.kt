package com.artemus.inlineCompletionApi.hint

import com.artemus.inlineCompletionApi.actions.AcceptInlineCompletion
import com.artemus.inlineCompletionApi.actions.ShowNextInlineCompletion
import com.artemus.inlineCompletionApi.actions.ShowPreviousInlineCompletion
import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.hint.HintManagerImpl
import com.intellij.codeInsight.hint.HintUtil
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.LightweightHint
import com.intellij.ui.SimpleColoredText
import com.intellij.ui.SimpleTextAttributes
import java.awt.Point
import java.awt.event.KeyEvent
import javax.swing.JComponent

object InlineKeybindingHintUtil {
    fun createAndShowHint(editor: Editor, pos: Point) {
        try {
            HintManagerImpl.getInstanceImpl()
                .showEditorHint(
                    LightweightHint(createInlineHintComponent()),
                    editor,
                    pos,
                    HintManager.HIDE_BY_ANY_KEY or HintManager.UPDATE_BY_SCROLLING,
                    0,
                    false
                )
        } catch (e: Throwable) {
            Logger.getInstance(InlineKeybindingHintUtil::class.java)
                .warn("Failed to show inline key bindings hints", e)
        }
    }

    private fun createInlineHintComponent(): JComponent {
        val component = HintUtil.createInformationComponent()
        val coloredText = SimpleColoredText(hintText(), SimpleTextAttributes.REGULAR_ATTRIBUTES)
        coloredText.appendToComponent(component)
        return InlineKeybindingHintComponent(component)
    }

    private fun hintText(): String {
        val nextShortcut = getShortcutText(ShowNextInlineCompletion.ACTION_ID)
        val prevShortcut = getShortcutText(ShowPreviousInlineCompletion.ACTION_ID)
        val acceptShortcut = getShortcutText(AcceptInlineCompletion.ACTION_ID)
        val cancelShortcut = KeymapUtil.getKeyText(KeyEvent.VK_ESCAPE)
        return String.format(
            "Next (%s) Prev (%s) Accept (%s) Cancel (%s)",
            nextShortcut, prevShortcut, acceptShortcut, cancelShortcut
        )
    }

    private fun getShortcutText(actionId: String): String {
        return StringUtil.defaultIfEmpty(
            KeymapUtil.getFirstKeyboardShortcutText(ActionManager.getInstance().getAction(actionId)),
            "Missing shortcut key"
        )
    }
}
