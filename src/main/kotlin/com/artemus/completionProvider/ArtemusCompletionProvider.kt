package com.artemus.completionProvider

import com.artemus.inlineCompletionApi.InlineCompletionItem
import com.artemus.inlineCompletionApi.InlineCompletionProvider
import com.intellij.openapi.editor.Editor
import inference.GRPCInferenceServiceGrpcKt
import inference.GrpcService
import io.grpc.ManagedChannelBuilder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.TimeUnit

class ArtemusCompletionProvider: InlineCompletionProvider {
    override suspend fun getInlineCompletion(editor: Editor, triggerOffset: Int):List<InlineCompletionItem> {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:80/v2/models/santacoder_huggingface")).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        println(response.statusCode())
        println(response.body())
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

//val channel = ManagedChannelBuilder.forTarget("127.0.0.1:8001")
//    .usePlaintext()
//    .build();
//
//val inferenceStub = GRPCInferenceServiceGrpcKt.GRPCInferenceServiceCoroutineStub(channel)
//val modelLive = inferenceStub.modelReady(
//    GrpcService.ModelReadyRequest.newBuilder()
//        .setName("santacoder_huggingface")
//        .build()
//)
//println("#".repeat(100))
//println(modelLive)
//println("#".repeat(100))
//channel.shutdown()
