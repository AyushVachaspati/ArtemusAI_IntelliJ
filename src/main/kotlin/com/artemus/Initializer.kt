package com.artemus

import com.artemus.completionProvider.ArtemusCompletionProvider
import com.artemus.inlineCompletionApi.InlineCompletionsManager
import com.artemus.inlineCompletionApi.InlineCompletionsManager.addCompletionProvider
import com.intellij.ide.ApplicationInitializedListener
import com.intellij.openapi.application.PreloadingActivity
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class Initializer: StartupActivity {

    override fun runActivity(project: Project) {
            addCompletionProvider(ArtemusCompletionProvider())
    }

//    override fun preload(indicator: ProgressIndicator) {
//        InlineCompletionsManager.addCompletionProvider(ArtemusCompletionProvider())
//        InlineCompletionsManager.addCompletionProvider(ArtemusCompletionProvider())
//        InlineCompletionsManager.addCompletionProvider(ArtemusCompletionProvider())
//        InlineCompletionsManager.addCompletionProvider(ArtemusCompletionProvider())
//    }

}