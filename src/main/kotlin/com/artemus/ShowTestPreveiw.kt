package com.artemus

import com.artemus.inlineCompletionApi.CompletionPreview
import com.artemus.inlineCompletionApi.InlineCompletionItem
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys

class ShowTestPreveiw: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        println("Test Action Performed")
        val editor = e.getData(PlatformDataKeys.EDITOR)
        if(editor!=null){
            CompletionPreview.createInstance(editor,
                listOf(
                    InlineCompletionItem("This is a Test1\n\tThis is a Test2\n  This is a Test3\n    This is a Test4\n        This is a Test5",
                        editor.caretModel.offset,
                        editor.caretModel.offset),
                    InlineCompletionItem("This is a Test1\n\tThis is a Test2\n  This is a Test3\n    This is a Test4\n        This is a Test5",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-5),
                    InlineCompletionItem("This is a Test1\n\tThis is a Test2\n  This is a Test3\n    This is a Test4\n        This is a Test5",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-1),

                    InlineCompletionItem("\t\twell This is a Test\n\tThis is a Test2",
                        editor.caretModel.offset,
                        editor.caretModel.offset),
                    InlineCompletionItem("\t\twell This is a Test\n\tThis is a Test2",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-5),
                    InlineCompletionItem("\t\twell This is a Test\n\tThis is a Test2",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-1),

                    InlineCompletionItem("\t\twell This is a Test",
                        editor.caretModel.offset,
                        editor.caretModel.offset),
                    InlineCompletionItem("\t\twell This is a Test",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-5),
                    InlineCompletionItem("\t\twell This is a Test",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-1)
                )
            )
        }
    }
}
