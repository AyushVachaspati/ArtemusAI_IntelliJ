package com.artemus.completionProvider

import com.artemus.inlineCompletionApi.InlineCompletionItem
import com.artemus.inlineCompletionApi.InlineCompletionProvider
import com.intellij.openapi.editor.Editor
import inference.GRPCInferenceServiceGrpcKt
import inference.GrpcService
import io.grpc.ManagedChannelBuilder
import java.util.concurrent.TimeUnit

class ArtemusCompletionProvider: InlineCompletionProvider {
    override suspend fun getInlineCompletion(editor: Editor, triggerOffset: Int):List<InlineCompletionItem> {

        val channel = ManagedChannelBuilder.forTarget("127.0.0.1:8001")
            .idleTimeout(5, TimeUnit.SECONDS)
            .build();

        val inferenceStub = GRPCInferenceServiceGrpcKt.GRPCInferenceServiceCoroutineStub(channel)
        val modelLive = inferenceStub.modelReady(
            GrpcService.ModelReadyRequest.newBuilder()
                .setName("santacoder_huggingface")
                .build()
        )
        println("#".repeat(100))
        println(modelLive)
        println("#".repeat(100))
        channel.shutdown()

        return emptyList()
    }


    override suspend fun getLookAheadCompletion(
        editor: Editor,
        lookAheadItem: String,
        userPrefix: String,
        triggerOffset: Int):List<InlineCompletionItem> {
        return emptyList()
    }
}