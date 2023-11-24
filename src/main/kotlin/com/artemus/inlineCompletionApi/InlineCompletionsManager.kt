package com.artemus.inlineCompletionApi

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.Editor
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing


object InlineCompletionsManager: AnAction() {
    private val dispatcher = Dispatchers.Swing
    private var scope = CoroutineScope(Job() + dispatcher)
    private val completionProviders = ArrayList<InlineCompletionProvider>()

    fun addCompletionProvider(provider: InlineCompletionProvider){
        completionProviders.add(provider)
    }

    fun removeCompletionProvider(provider: InlineCompletionProvider){
        completionProviders.remove(provider)
    }

    private fun createNewCoroutine(){
        scope = CoroutineScope(Job() + dispatcher)
    }

    fun cancelCompletions(){
        scope.cancel()
        createNewCoroutine()
    }

    fun createPreviewInline(editor: Editor, completion: String){
        cancelCompletions()
        scope.launch {
            val jobs = completionProviders.map { async {it.getInlineCompletion(editor, editor.caretModel.offset)}}
            val results = jobs.awaitAll()
            val completions = results.flatMap{it.asIterable()}
            if(isActive) {
                CompletionPreview.createInstance(editor,completions,CompletionType.INLINE_COMPLETION)
            }
        }
    }

    fun createPreviewLookAhead(editor: Editor, completion: String){
        cancelCompletions()
        // TODO: call a function that calls all the providers  in an async -> then get all the completions from them using await().
        //  Then adjust all the completions for lookahead userPrefix input.
        //  Then adjust for other letters typed by the user after the userPrefix. Use prefix-suffix match used in DocumentListener
        //  then it concatenates all the completions and creates a completion preview based on this.
        //  Check for CANCEL before creating the preview.
        scope.launch {
            async{
                getLookaheadCompletion(editor, completion)
            }.await()
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(PlatformDataKeys.EDITOR)
        createPreviewInline(editor!!, "")
//        cancelCompletions()
//        scope.launch {
//            val editor = e.getData(PlatformDataKeys.EDITOR)
//            if (editor != null) {
//                async{
//                    getSampleCompletions(editor)
//                }.await()
//            }
//        }
    }

    private suspend fun getLookaheadCompletion(editor: Editor, completion: String) {
        delay(5000)
        CompletionPreview.createInstance(
            editor,
            listOf(
                // These tests expect "This is" as the text on the current line. and cursor at the start of line
                // Also test with caret in the middle of line
                // also test with caret at the end of line
//                InlineCompletionItem(
//                    completion,
//                    editor.caretModel.offset,
//                    editor.caretModel.visualLineEnd - 1
//                ),
                InlineCompletionItem(
                    "$completion 1234\nTesting\nTesting\nTesting",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 1
                )
            ),
            CompletionType.LOOK_AHEAD_COMPLETION
        )
    }


    private suspend fun getSampleCompletions(editor: Editor) {
        delay(5000)
        CompletionPreview.createInstance(
            editor,
            listOf(
                // These tests expect "This is" as the text on the current line. and cursor at the start of line
                // Also test with caret in the middle of line
                // also test with caret at the end of line
                InlineCompletionItem(
                    "Before Completion This is",
                    editor.caretModel.offset,
                    editor.caretModel.offset
                ),
                InlineCompletionItem(
                    "This is After Completion",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 1
                ),
                InlineCompletionItem(
                    "Before Completion This is After Completion",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 1
                ),

                InlineCompletionItem(
                    "Before Completion This is",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 4
                ),
                InlineCompletionItem(
                    "This is After Completion",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 4
                ),
                InlineCompletionItem(
                    "Before Completion This is After Completion",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 4
                ),


                // These tests are same as above but with empty next line
                InlineCompletionItem(
                    "Before Completion This is\n",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 1
                ),
                InlineCompletionItem(
                    "This is After Completion\n",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 1
                ),
                InlineCompletionItem(
                    "Before Completion This is After Completion\n",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 1
                ),

                InlineCompletionItem(
                    "Before Completion This is\n",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 4
                ),
                InlineCompletionItem(
                    "This is After Completion\n",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 4
                ),
                InlineCompletionItem(
                    "Before Completion This is After Completion\n",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 4
                ),


                // this is stuff with extra line in the middle
                InlineCompletionItem(
                    "Before Completion This is\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 1
                ),
                InlineCompletionItem(
                    "This is After Completion\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 1
                ),
                InlineCompletionItem(
                    "Before Completion This is After Completion\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 1
                ),

                InlineCompletionItem(
                    "Before Completion This is\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 4
                ),
                InlineCompletionItem(
                    "This is After Completion\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 4
                ),
                InlineCompletionItem(
                    "Before Completion This is After Completion\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 4
                ),


                // These tests are same as above but with empty next line
                InlineCompletionItem(
                    "Before Completion This is\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 1
                ),
                InlineCompletionItem(
                    "This is After Completion\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 1
                ),
                InlineCompletionItem(
                    "Before Completion This is After Completion\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 1
                ),

                InlineCompletionItem(
                    "Before Completion This is\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 4
                ),
                InlineCompletionItem(
                    "This is After Completion\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 4
                ),
                InlineCompletionItem(
                    "Before Completion This is After Completion\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.visualLineEnd - 4
                ),


                InlineCompletionItem(
                    "Before Completion This is",
                    editor.caretModel.offset,
                    editor.caretModel.offset
                ),
                InlineCompletionItem(
                    "This is After Completion",
                    editor.caretModel.offset,
                    editor.caretModel.offset
                ),
                InlineCompletionItem(
                    "Before Completion This is After Completion",
                    editor.caretModel.offset,
                    editor.caretModel.offset
                ),


                // These tests are same as above but with empty next line
                InlineCompletionItem(
                    "Before Completion This is\n",
                    editor.caretModel.offset,
                    editor.caretModel.offset
                ),
                InlineCompletionItem(
                    "This is After Completion\n",
                    editor.caretModel.offset,
                    editor.caretModel.offset
                ),
                InlineCompletionItem(
                    "Before Completion This is After Completion\n",
                    editor.caretModel.offset,
                    editor.caretModel.offset
                ),


                // this is stuff with extra line in the middle
                InlineCompletionItem(
                    "Before Completion This is\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.offset
                ),
                InlineCompletionItem(
                    "This is After Completion\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.offset
                ),
                InlineCompletionItem(
                    "Before Completion This is After Completion\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.offset
                ),


                // These tests are same as above but with empty next line
                InlineCompletionItem(
                    "Before Completion This is\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.offset
                ),
                InlineCompletionItem(
                    "This is After Completion\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.offset
                ),
                InlineCompletionItem(
                    "Before Completion This is After Completion\n" +
                            "MIDDLE LINE\n" +
                            "MIDDLE LINE",
                    editor.caretModel.offset,
                    editor.caretModel.offset
                ),

                ),
            CompletionType.INLINE_COMPLETION
        )
    }
}
