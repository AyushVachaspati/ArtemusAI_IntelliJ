package com.artemus

import com.artemus.inlineCompletionApi.CompletionPreview
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys

class RemoveTestPreview: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        println("Test Action Performed")
        val editor = e.getData(PlatformDataKeys.EDITOR);
        if(editor!=null){
            CompletionPreview.clear(editor)
        }
    }
}