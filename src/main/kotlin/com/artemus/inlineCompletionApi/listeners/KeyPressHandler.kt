package com.artemus.inlineCompletionApi.listeners

import com.artemus.ShowTestPreview
import com.artemus.inlineCompletionApi.CompletionPreview
import com.artemus.inlineCompletionApi.CompletionType
import com.artemus.inlineCompletionApi.InlineCompletionItem
import com.artemus.inlineCompletionApi.inlineCompletionGlobalState.GlobalState
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.InvalidDataException
import com.intellij.psi.PsiFile


class KeyPressHandler: TypedHandlerDelegate() {
    override fun beforeCharTyped(typedChar: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {
        println("Char typed: $typedChar")
        // TODO: Check for matching completions. (only if current preview exists) otherwise trigger new completion
        var currentPreview = CompletionPreview.getInstance(editor)
        if(currentPreview!=null) {
            var completions = currentPreview.getCompletions()
            var currentCompletionItem = currentPreview.currentCompletion
            var currentIndex = currentPreview.getCurrentIndex()


            // filter and adjust the completions which match the typed character
            completions = filterAndAdjustCompletions(completions, typedChar, currentCompletionItem, currentIndex)

            if(completions.isEmpty()){
                CompletionPreview.clear(editor)
                val r = Runnable {
                    ShowTestPreview().createPreview(editor, "On typing new completion")
                }
                ApplicationManager.getApplication().invokeLater(r)
                return Result.CONTINUE
            }


            // Create new preview with the Adjusted completions
            CompletionPreview.clear(editor)
            GlobalState.clearedByKeyPress = true
            val r = Runnable {
                try {
                    CompletionPreview.createInstance(editor, completions, CompletionType.INLINE_COMPLETION)
                }
                catch(e:InvalidDataException){
                    // TODO: Trigger a new completion here
                    //  Since none of the filtered completions were valid
                    ShowTestPreview().createPreview(editor, "Full text typed trigger")
                }
                finally {
                    GlobalState.clearedByKeyPress = false
                }
            }
            ApplicationManager.getApplication().invokeLater(r)
        }
        else{
            // TODO: Trigger completion as the user has started typing without any prior completions shown
            // Don't think we need this trigger here, since Document Listener does that already
        }

        return Result.CONTINUE
    }


    private fun filterAndAdjustCompletions(completions: List<InlineCompletionItem>, typedChar: Char,
                                           currentCompletionItem: InlineCompletionItem, currentIndex: Int
    ): List<InlineCompletionItem> {
        //TODO: Correct offset changes so typing to the end of completions and completions with new line triggers new completion
        //TODO: i.e it does not trigger the InvalidDataException in CompltionPreview constructor.
        val result = ArrayList<InlineCompletionItem>()
        for ((i,it) in completions.withIndex()) {
            if(it.insertText.startsWith(typedChar)) {
                val newInsertText = it.insertText.substring(typedChar.toString().length)
                if (newInsertText.isNotEmpty()) {
                    if (it.insertText == currentCompletionItem.insertText && i == currentIndex)
                    // if the currently shown item matches the prefix. It should continue to be shown
                        result.add(0, InlineCompletionItem(newInsertText, it.startOffset + 1, it.endOffset + 1))
                    else {
                        result.add(InlineCompletionItem(newInsertText, it.startOffset + 1, it.endOffset + 1))
                    }
                }
            }
        }
        if(result.isEmpty()) return emptyList()
        return result.toList()
    }
}