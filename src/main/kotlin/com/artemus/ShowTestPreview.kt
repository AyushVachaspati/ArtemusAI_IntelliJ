package com.artemus

import com.artemus.inlineCompletionApi.CompletionPreview
import com.artemus.inlineCompletionApi.CompletionType
import com.artemus.inlineCompletionApi.InlineCompletionItem
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.Editor

// these are when just inserting code and not replacing any substring

//InlineCompletionItem("Some Completion ",
//editor.caretModel.offset,
//editor.caretModel.offset),

class ShowTestPreview: AnAction() {
    public fun createPreviewInline(editor: Editor, completion: String){
        println("Test Action Performed")
        if(editor!=null) {
            CompletionPreview.createInstance(
                editor,
                listOf(
                    // These tests expect "This is" as the text on the current line. and cursor at the start of line
                    // Also test with caret in the middle of line
                    // also test with caret at the end of line
                    InlineCompletionItem(
                        completion,
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd - 1
                    )
                ),
                CompletionType.INLINE_COMPLETION
            )
        }
    }
    public fun createPreviewLookAhead(editor: Editor, completion: String){
        println("Test Action Performed")
        if(editor!=null) {
            CompletionPreview.createInstance(
                editor,
                listOf(
                    // These tests expect "This is" as the text on the current line. and cursor at the start of line
                    // Also test with caret in the middle of line
                    // also test with caret at the end of line
                    InlineCompletionItem(
                        completion,
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd - 1
                    ),InlineCompletionItem(
                        completion +"new",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd - 1
                    )
                ),
                CompletionType.LOOK_AHEAD_COMPLETION
            )
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        println("Test Action Performed")
        val editor = e.getData(PlatformDataKeys.EDITOR)
        if(editor!=null){
            CompletionPreview.createInstance(editor,
                listOf(
                    // These tests expect "This is" as the text on the current line. and cursor at the start of line
                    // Also test with caret in the middle of line
                    // also test with caret at the end of line
                    InlineCompletionItem("Before Completion This is",
                        editor.caretModel.offset,
                        editor.caretModel.offset),
                    InlineCompletionItem("This is After Completion",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-1),
                    InlineCompletionItem("Before Completion This is After Completion",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-1),

                    InlineCompletionItem("Before Completion This is",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-4),
                    InlineCompletionItem("This is After Completion",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-4),
                    InlineCompletionItem("Before Completion This is After Completion",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-4),


                    // These tests are same as above but with empty next line
                    InlineCompletionItem("Before Completion This is\n",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-1),
                    InlineCompletionItem("This is After Completion\n",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-1),
                    InlineCompletionItem("Before Completion This is After Completion\n",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-1),

                    InlineCompletionItem("Before Completion This is\n",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-4),
                    InlineCompletionItem("This is After Completion\n",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-4),
                    InlineCompletionItem("Before Completion This is After Completion\n",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-4),



                    // this is stuff with extra line in the middle
                    InlineCompletionItem("Before Completion This is\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-1),
                    InlineCompletionItem("This is After Completion\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-1),
                    InlineCompletionItem("Before Completion This is After Completion\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-1),

                    InlineCompletionItem("Before Completion This is\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-4),
                    InlineCompletionItem("This is After Completion\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-4),
                    InlineCompletionItem("Before Completion This is After Completion\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-4),


                    // These tests are same as above but with empty next line
                    InlineCompletionItem("Before Completion This is\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-1),
                    InlineCompletionItem("This is After Completion\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-1),
                    InlineCompletionItem("Before Completion This is After Completion\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-1),

                    InlineCompletionItem("Before Completion This is\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-4),
                    InlineCompletionItem("This is After Completion\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-4),
                    InlineCompletionItem("Before Completion This is After Completion\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.visualLineEnd-4),





                    InlineCompletionItem("Before Completion This is",
                        editor.caretModel.offset,
                        editor.caretModel.offset),
                    InlineCompletionItem("This is After Completion",
                        editor.caretModel.offset,
                        editor.caretModel.offset),
                    InlineCompletionItem("Before Completion This is After Completion",
                        editor.caretModel.offset,
                        editor.caretModel.offset),


                    // These tests are same as above but with empty next line
                    InlineCompletionItem("Before Completion This is\n",
                        editor.caretModel.offset,
                        editor.caretModel.offset),
                    InlineCompletionItem("This is After Completion\n",
                        editor.caretModel.offset,
                        editor.caretModel.offset),
                    InlineCompletionItem("Before Completion This is After Completion\n",
                        editor.caretModel.offset,
                        editor.caretModel.offset),


                    // this is stuff with extra line in the middle
                    InlineCompletionItem("Before Completion This is\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.offset),
                    InlineCompletionItem("This is After Completion\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.offset),
                    InlineCompletionItem("Before Completion This is After Completion\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.offset),


                    // These tests are same as above but with empty next line
                    InlineCompletionItem("Before Completion This is\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.offset),
                    InlineCompletionItem("This is After Completion\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.offset),
                    InlineCompletionItem("Before Completion This is After Completion\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                        editor.caretModel.offset,
                        editor.caretModel.offset),

                ),
                CompletionType.INLINE_COMPLETION
            )
        }
    }
}
