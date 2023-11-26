package com.artemus.completionProvider

import com.google.protobuf.kotlin.toByteString
import com.google.protobuf.kotlin.toByteStringUtf8
import com.jetbrains.rd.util.printlnError
import inference.GRPCInferenceServiceGrpcKt
import inference.GrpcService
import inference.GrpcService.ModelInferResponse
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.*
import kotlin.coroutines.cancellation.CancellationException


data class ModelPrediction(val result: String)

object PredictionUtils {
    val debounceMs = 300L
    val grpcUrl = "127.0.0.1:81"
    val inlineModelName = "santacoder_huggingface"
    val streamModelName ="santacoder_huggingface_stream"

    private fun decodeRawOutput(rawOutput: ModelInferResponse):String{
        return rawOutput.getRawOutputContents(0)
            .asReadOnlyByteBuffer()
            .toByteString()
            .toStringUtf8()
            .substring(4)
    }

    suspend fun debouncedInlineCompletion(prefix:String): ModelPrediction? {

        // Acts as debounce, since CompletionManager can call cancel.
        try {
            delay(debounceMs)
        }
        catch(e: CancellationException){
//            println("Cancelled by Debounce")
            return null
        }


        //TODO: Set Status Bar Fetching here

        val managedChannel = ManagedChannelBuilder.forTarget(grpcUrl)
            .usePlaintext()
            .build()

        try {
            val inferenceStub = GRPCInferenceServiceGrpcKt.GRPCInferenceServiceCoroutineStub(managedChannel)
            val modelRequest = GrpcService.ModelInferRequest.newBuilder()
                .setModelName(inlineModelName)
                .addInputs(
                    GrpcService.ModelInferRequest.InferInputTensor.newBuilder()
                        .setName("input")
                        .setDatatype("BYTES")
                        .addShape(1).addShape(1)
                        .setContents(
                            GrpcService.InferTensorContents.newBuilder()
                                .addBytesContents(prefix.toByteStringUtf8())
                        )
                ).build()
            // We don't want this request to be cancelled, so that we can cache it and not waste an API call.
            // Rest of the code is non-cancellable by default since it doesn't cooperate with cancel signal.
            val modelOutput = withContext(NonCancellable){ inferenceStub.modelInfer(modelRequest) }
            return ModelPrediction(decodeRawOutput(modelOutput))

        }
        catch(e: CancellationException){
            printlnError("Request Cancelled")
        }
        catch(e:Exception){
            printlnError(e.stackTraceToString())
        }
        finally {
            managedChannel.shutdown()
            // TODO: Set status bar active here
        }
        return null
    }
}