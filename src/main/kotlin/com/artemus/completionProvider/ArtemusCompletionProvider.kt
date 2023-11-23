package com.artemus.completionProvider

import com.artemus.inlineCompletionApi.InlineCompletionItem
import com.artemus.inlineCompletionApi.InlineCompletionProvider
import com.google.gson.JsonParser
import com.intellij.openapi.editor.Editor
import com.jetbrains.rd.util.printlnError
import inference.GRPCInferenceServiceGrpcKt
import inference.GrpcService
import io.grpc.ManagedChannelBuilder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException
import java.time.Duration

class ArtemusCompletionProvider: InlineCompletionProvider {

    private fun getCompletion(): String?{
        val client = HttpClient.newHttpClient()
        try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:80/v2/models/santacoder_huggingface/infer"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(
                    HttpRequest.BodyPublishers.ofString("""
                        {"id":"test123",
                        "inputs":[{
                            "name":"input",
                            "shape":[1,1],
                            "datatype": "BYTES",
                            "data":[["Complete this string"]]
                           }]
                       }
                    """.trimIndent()
                    )
                )
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if(response.statusCode() == 200) {
                val jsonObj = JsonParser.parseString(response.body()).asJsonObject
                val completion = jsonObj.get("outputs")
                    .asJsonArray[0]
                    .asJsonObject
                    .get("data")
                    .asJsonArray[0]
                    .asString
                return completion
            }
            else{
                printlnError("Invalid Response Received from API")
                return null
            }
        }
        catch(e: HttpTimeoutException){
            printlnError("Connection Timed out")
            return null
        }
    }

    override suspend fun getInlineCompletion(editor: Editor, triggerOffset: Int):List<InlineCompletionItem> {

        val channel = ManagedChannelBuilder.forTarget("127.0.0.1:81")
            .usePlaintext()
            .build()
        println(channel)
        val inferenceStub = GRPCInferenceServiceGrpcKt.GRPCInferenceServiceCoroutineStub(channel)
        println(inferenceStub)
        val modelRequest = GrpcService.ModelReadyRequest.newBuilder()
            .setName("santacoder_huggingface")
            .build()
        println(modelRequest)
        val modelLive = inferenceStub.modelReady(modelRequest)


        println("#".repeat(100))
        println(modelLive)
        println("#".repeat(100))
        channel.shutdown()
        // try to use current class loader to load the class instead of Intellij.. use Class<?> to get the current class and
        // then cast it to the right interface, then use it to make the channel
        val completion = getCompletion()
        println(completion)
        return emptyList()
    }


    override suspend fun getLookAheadCompletion(
        editor: Editor,
        lookAheadItem: String,
        userPrefix: String,
        triggerOffset: Int):List<InlineCompletionItem> {

        val completion  = getCompletion()
        println(completion)
        return emptyList()
    }
}


/** Code to make grpc client. Not working */
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
/**********************************************/