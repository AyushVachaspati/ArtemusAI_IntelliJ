package com.artemus.inlineCompletionApi

import com.intellij.codeInsight.codeVision.editorLensContextKey
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
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

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(PlatformDataKeys.EDITOR)
        createPreviewInline(editor!!, "")
    }

    fun createPreviewInline(editor: Editor, completion: String){
        cancelCompletions()
        scope.launch {
            val jobs = completionProviders.map {
                async {
                    it.getInlineCompletion(editor, editor.caretModel.offset)
                }
            }
            val results = jobs.awaitAll()
            val completions = results.flatMap{it.asIterable()}
            if(isActive) {
                CompletionPreview.createInstance(editor,completions,CompletionType.INLINE_COMPLETION)
            }
        }
    }

    fun createPreviewLookAhead(editor: Editor,userPrefix: String,lookAheadItem: String, completion: String){
        cancelCompletions()
        val triggerOffset = editor.caretModel.offset
        scope.launch {
            val jobs = completionProviders.map {
                async {
                    it.getLookAheadCompletion(editor,userPrefix, lookAheadItem, triggerOffset)
                }
            }
            val results = jobs.awaitAll()
            var completions = results.flatMap{it.asIterable()}
            // TODO: Need to adjust the completions because there is a delay in which the user might have typed stuff
            if(isActive) {
                completions = adjustNewLookAheadCompletions(editor, completions, triggerOffset)
                if(completions.isNotEmpty())
                    CompletionPreview.createInstance(editor,completions,CompletionType.LOOK_AHEAD_COMPLETION)
            }
        }
    }

    private fun adjustNewLookAheadCompletions(
        editor: Editor,
        completions: List<InlineCompletionItem>,
        triggerOffset: Int): List<InlineCompletionItem>{

        val currentOffset = editor.caretModel.offset
        if(currentOffset < triggerOffset) return emptyList()  // User Removing Completion Is not yet supported

        val extraTyped = editor.document.getText(TextRange(triggerOffset, currentOffset))
        val result = ArrayList<InlineCompletionItem>()
        for(it in completions){
            if(it.insertText.indexOf(extraTyped)!=0) continue
            val newInsertText = it.insertText.removePrefix(extraTyped)
            result.add(InlineCompletionItem(newInsertText,
                it.startOffset+extraTyped.length,
                it.endOffset+extraTyped.length))

        }

        return result.toList()
    }
}
