package com.artemus.completionProvider

import com.artemus.inlineCompletionApi.InlineCompletionItem
import com.artemus.inlineCompletionApi.InlineCompletionProvider
import com.google.gson.JsonParser
import com.google.protobuf.kotlin.get
import com.google.protobuf.kotlin.toByteString
import com.google.protobuf.kotlin.toByteStringUtf8
import com.intellij.openapi.editor.Editor
import com.jetbrains.rd.util.printlnError
import inference.GRPCInferenceServiceGrpcKt
import inference.GrpcService
import inference.GrpcService.ModelInferRequest.InferInputTensor
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.concurrency.await
import org.jetbrains.concurrency.runAsync
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException
import java.time.Duration

class ArtemusCompletionProvider: InlineCompletionProvider {

    override suspend fun getInlineCompletion(editor: Editor, triggerOffset: Int):List<InlineCompletionItem> {
        val result = PredictionUtils.getInlineCompletion("Complete this string").result
        print(result)
        return emptyList()
    }


    override suspend fun getLookAheadCompletion(
        editor: Editor,
        lookAheadItem: String,
        userPrefix: String,
        triggerOffset: Int):List<InlineCompletionItem> {
        val result = PredictionUtils.getInlineCompletion("Complete this string").result
        print(result)
        return emptyList()
    }
}


/** Code to make grpc stream client */
//    val managedChannel = ManagedChannelBuilder.forTarget("127.0.0.1:81")
//        .usePlaintext()
//        .build()
//    try {
//        val inferenceStub = GRPCInferenceServiceGrpcKt.GRPCInferenceServiceCoroutineStub(managedChannel)
//        val modelRequest = flowOf(
//            GrpcService.ModelInferRequest.newBuilder()
//                .setModelName("santacoder_huggingface_stream")
//                .addInputs(
//                    InferInputTensor.newBuilder()
//                        .setName("input")
//                        .setDatatype("BYTES")
//                        .addShape(1).addShape(1)
//                        .setContents(
//                            GrpcService.InferTensorContents.newBuilder()
//                                .addBytesContents("Complete this string".toByteStringUtf8())
//                        )
//                ).build()
//        )
//        val modelOutput = inferenceStub.modelStreamInfer(modelRequest)
//        modelOutput.collectIndexed { index, value ->
//            print(
//                value.inferResponse
//                .getRawOutputContents(0)
//                .asReadOnlyByteBuffer()
//                .toByteString()
//                .toStringUtf8()
//                .substring(4)
//            )
//            if (value.inferResponse.parametersMap["triton_final_response"]!!.boolParam) {
//                println()
//                printlnError("Final Response Received")
//            }
//        }
//    }
//    catch(e: CancellationException){
//        printlnError("Request Cancelled")
//    }
//    catch(e:Exception){
//        printlnError(e.stackTraceToString())
//    }
//    finally {
//        managedChannel.shutdown()
//    }
/**********************************************/