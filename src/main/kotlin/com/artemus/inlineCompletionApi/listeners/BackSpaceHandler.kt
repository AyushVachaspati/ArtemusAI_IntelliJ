package com.artemus.inlineCompletionApi.listeners

import com.artemus.inlineCompletionApi.CompletionPreview
import com.artemus.inlineCompletionApi.InlineCompletionsManager
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

class BackSpaceHandler(private val myOriginalHandler: EditorActionHandler) : EditorActionHandler() {
    public override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        println("BackSpace Called")

        // This does not count as caret offset change. So we need to cancel Preview here
        CompletionPreview.clear(editor)

        val lookupEx = LookupManager.getActiveLookup(editor)
        if(lookupEx != null){
            val r = Runnable {
                val item = lookupEx.currentItem
                if(item != null) {
                    val userPrefix = lookupEx.itemPattern(item)
                    InlineCompletionsManager.createPreviewLookAhead(
                        editor,userPrefix, item.lookupString,
                        "${item.lookupString.substring(userPrefix.length)} something something"
                    )
                }
            }
            ApplicationManager.getApplication().invokeLater(r)
        }


        if (myOriginalHandler.isEnabled(editor, caret, dataContext)) {
            myOriginalHandler.execute(editor, caret, dataContext)
        }
    }

    public override fun isEnabledForCaret(
        editor: Editor,
        caret: Caret,
        dataContext: DataContext
    ): Boolean {
        return true
    }
}
