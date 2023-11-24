package com.artemus.completionProvider

import com.artemus.inlineCompletionApi.InlineCompletionItem
import com.artemus.inlineCompletionApi.InlineCompletionProvider
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import java.util.regex.Pattern

class ArtemusCompletionProvider: InlineCompletionProvider {
    private val END_OF_LINE_VALID_PATTERN = Pattern.compile("^\\s*[)}\\]\"'`]*\\s*[:{;,]?\\s*$")

    override suspend fun getInlineCompletion(editor: Editor, triggerOffset: Int):List<InlineCompletionItem> {
        val currentOffset = editor.caretModel.offset

        if(currentOffset!=triggerOffset) return emptyList()
        if(!isValidMidlinePosition(editor.document, currentOffset)) return emptyList()

        println("Inline Completion Triggered")
        val document = editor.document
        val prefix = document.getText(TextRange(0,currentOffset))
        val postfix = document.getText(TextRange(currentOffset, document.getLineEndOffset(document.lineCount-1)))
        val startToken = "<fim-prefix>"
        val endToken = "<fim-suffix>"
        val middleToken = "<fim-middle>"
        val prompt:String
        val fillInMiddle = postfix.trim().isNotEmpty()

        if(fillInMiddle){
            prompt = "${startToken}${prefix}${endToken}${postfix}${middleToken}"
        }
        else{
            prompt = prefix
        }

//        let cacheItem:CachePrompt = {
//                prefix: prompt,
//                completionType: CompletionType.inlineSuggestion
//        };
//
//        console.log(prompt)
//        let inlineCompletion:string|undefined = globalCache.get(sha1(JSON.stringify(cacheItem)).toString());

//        if(!inlineCompletion){
//            let prediction = await debounceCompletions(prompt);
//            inlineCompletion = prediction? prediction.result.slice(prompt.length) : undefined;
//            inlineCompletion? globalCache.set(sha1(JSON.stringify(cacheItem)), inlineCompletion) : undefined;
//        }

        val prediction = PredictionUtils.debouncedInlineCompletion(prompt)
        var inlineCompletion = prediction?.result
//        inlineCompletion? globalCache.set(sha1(JSON.stringify(cacheItem)), inlineCompletion) : undefined;

        if(inlineCompletion!=null){
            inlineCompletion = inlineCompletion.substring(prompt.length)
            return listOf(InlineCompletionItem(inlineCompletion, currentOffset, currentOffset),
                InlineCompletionItem(inlineCompletion+"Testing", currentOffset, currentOffset),)
        }

        return emptyList()
    }


    override suspend fun getLookAheadCompletion(
        editor: Editor,
        lookAheadItem: String,
        userPrefix: String,
        triggerOffset: Int):List<InlineCompletionItem> {
        val result = PredictionUtils.debouncedInlineCompletion("Complete this string")?.result
        print(result)
        return emptyList()
    }



    private fun isValidMidlinePosition(document: Document, offset: Int): Boolean {
        val lineIndex: Int = document.getLineNumber(offset)
        val lineRange = TextRange.create(document.getLineStartOffset(lineIndex), document.getLineEndOffset(lineIndex))
        val line = document.getText(lineRange)
        val lineSuffix = line.substring(offset - lineRange.startOffset)
        return END_OF_LINE_VALID_PATTERN.matcher(lineSuffix).matches()
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