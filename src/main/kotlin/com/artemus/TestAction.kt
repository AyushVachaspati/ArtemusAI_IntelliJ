package com.artemus

import com.artemus.inlineCompletionApi.CompletionPreview
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys

class TestAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        println("Test Action Performed")
        val editor = e.getData(PlatformDataKeys.EDITOR)
        if(editor!=null){
            CompletionPreview.createInstance(editor,
                listOf("\t\tThis is a Test1\n\tThis is a Test2\n  This is a Test3\n    This is a Test4\n        This is a Test5\n",
                "\t\twell This is a Test\n\tThis is a Test2",
                    "\t\twell This is a Test"))
        }
    }
}