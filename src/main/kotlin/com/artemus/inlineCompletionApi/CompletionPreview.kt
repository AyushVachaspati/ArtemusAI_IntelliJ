package com.artemus.inlineCompletionApi

import com.artemus.inlineCompletionApi.render.ArtemusInlay
import com.artemus.inlineCompletionApi.render.ArtemusInlay.Companion.create
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.refactoring.rename.inplace.InplaceRefactoring

class CompletionPreview private constructor(
    val editor: Editor, private val completions: List<String>, private val offset: Int
) : Disposable {
    private val artemusInlay: ArtemusInlay
    private val currentIndex = 0

    init {
        EditorUtil.disposeWithEditor(editor, this)
        artemusInlay = create(this)
    }

    val currentCompletion: String
        get() = completions[currentIndex]

    //  public void togglePreview(CompletionOrder order) {
    //    int nextIndex = currentIndex + order.diff();
    //    currentIndex = (completions.size() + nextIndex) % completions.size();
    //
    //    Disposer.dispose(tabnineInlay);
    //    tabnineInlay = TabnineInlay.create(this);
    //
    //    createPreview();
    //    completionsEventSender.sendToggleInlineSuggestionEvent(order, currentIndex);
    //  }
    private fun createPreview(): String? {
        val completion = completions[currentIndex]

        // conditions to check when showing preview
        return if (editor !is EditorImpl
            || editor.selectionModel.hasSelection() || InplaceRefactoring.getActiveInplaceRenamer(editor) != null
        ) {
            null
        } else try {
            editor.document.startGuardedBlockChecking()
            artemusInlay.render(editor, completion, offset)
            completion
        } finally {
            editor.document.stopGuardedBlockChecking()
        }
    }

    override fun dispose() {
        editor.putUserData(INLINE_COMPLETION_PREVIEW, null)
    }

    //  public void applyPreview(@Nullable Caret caret) {

    //    if (caret == null) {
    //      return;
    //    }
    //
    //    Project project = editor.getProject();
    //
    //    if (project == null) {
    //      return;
    //    }
    //
    //    PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    //
    //    if (file == null) {
    //      return;
    //    }
    //
    //    try {
    //      applyPreviewInternal(caret.getOffset(), project, file);
    //    } catch (Throwable e) {
    //      Logger.getInstance(getClass()).warn("Failed in the processes of accepting completion", e);
    //    } finally {
    //      Disposer.dispose(this);
    //    }
    //  }
    //  private void applyPreviewInternal(@NotNull Integer cursorOffset, Project project, PsiFile file) {
    //    CompletionPreview.clear(editor);
    //    String completion = completions.get(currentIndex);
    //    String suffix = completion;
    //    int startOffset = cursorOffset;
    //    int endOffset = cursorOffset + suffix.length();
    //    if (shouldRemoveSuffix(completion)) {
    //      editor.getDocument().deleteString(cursorOffset, cursorOffset + completion.oldSuffix.length());
    //    }
    //    editor.getDocument().insertString(cursorOffset, suffix);
    //    editor.getCaretModel().moveToOffset(startOffset + completion.length());
    //    if (AppSettingsState.getInstance().getAutoImportEnabled()) {
    //      Logger.getInstance(getClass()).info("Registering auto importer");
    //      AutoImporter.registerTabNineAutoImporter(editor, project, startOffset, endOffset);
    //    }
    //    previewListener.executeSelection(
    //        this.editor,
    //        completion,
    //        file.getName(),
    //        RenderingMode.INLINE,
    //        (selection -> {
    //          selection.index = currentIndex;
    //          SelectionUtil.addSuggestionsCount(selection, completions);
    //        }));
    //  }
    companion object {
        private val INLINE_COMPLETION_PREVIEW = Key.create<CompletionPreview>("INLINE_COMPLETION_PREVIEW")
        fun createInstance(
            editor: Editor, completions: List<String>, offset: Int
        ): String? {

            //Clear any currently showing Preview. Specially when called manually.
            clear(editor)
            val preview = CompletionPreview(editor, completions, offset)
            editor.putUserData(INLINE_COMPLETION_PREVIEW, preview)
            return preview.createPreview()
        }

        fun getCurrentCompletion(editor: Editor): String? {
            val preview = getInstance(editor) ?: return null
            return preview.currentCompletion
        }

        fun getInstance(editor: Editor): CompletionPreview? {
            return editor.getUserData(INLINE_COMPLETION_PREVIEW)
        }

        fun clear(editor: Editor) {
            val completionPreview = getInstance(editor)
            if (completionPreview != null) {
                Disposer.dispose(completionPreview)
            }
        }
    }
}
