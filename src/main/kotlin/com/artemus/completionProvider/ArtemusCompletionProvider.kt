package com.artemus.completionProvider

import com.artemus.inlineCompletionApi.InlineCompletionItem
import com.artemus.inlineCompletionApi.InlineCompletionProvider
import com.artemus.inlineCompletionApi.general.Utils
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.jetbrains.rd.util.printlnError
import java.util.regex.Pattern

class ArtemusCompletionProvider: InlineCompletionProvider {

    override suspend fun getInlineCompletion(editor: Editor, triggerOffset: Int):List<InlineCompletionItem> {
        val currentOffset = editor.caretModel.offset

        if(currentOffset!=triggerOffset) return emptyList()

        // isValidMidlinePostion is not needed for FIM task for the LLM
//        if(!isValidMidlinePosition(editor.document, currentOffset)) return emptyList()

        println("Inline Completion Triggered")
        val document = editor.document
        val prefix = document.getText(TextRange(0,currentOffset))
        val postfix = document.getText(TextRange(currentOffset, document.getLineEndOffset(document.lineCount-1)))
        val startToken = "<fim-prefix>"
        val endToken = "<fim-suffix>"
        val middleToken = "<fim-middle>"
        val prompt:String
        val fillInMiddle = postfix.trim().isNotEmpty()
        val lineEndOffset = document.getLineEndOffset(document.getLineNumber(triggerOffset))  //Lookahead replace until end of line

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

        // get all editor values before calling the API, the editor can change while we wait.
        val prediction = PredictionUtils.debouncedInlineCompletion(prompt)
        var inlineCompletion = prediction?.result
//        inlineCompletion? globalCache.set(sha1(JSON.stringify(cacheItem)), inlineCompletion) : undefined;

        if(inlineCompletion!=null){
            inlineCompletion = inlineCompletion.substring(prompt.length)
            if(isFirstLineSubstring(Utils.asLines(inlineCompletion)[0], document.getText(TextRange(triggerOffset, lineEndOffset))))
                return listOf(InlineCompletionItem(inlineCompletion, triggerOffset, lineEndOffset))
            return listOf(InlineCompletionItem(inlineCompletion, triggerOffset, triggerOffset))
        }

        return emptyList()
    }


    override suspend fun getLookAheadCompletion(
        editor: Editor,
        userPrefix: String,
        lookAheadItem: String,
        triggerOffset: Int):List<InlineCompletionItem> {

        val currentOffset = editor.caretModel.offset

        if(currentOffset!=triggerOffset) return emptyList()

        // isValidMidlinePostion is not needed for FIM task for the LLM
//        if(!isValidMidlinePosition(editor.document, currentOffset)) return emptyList()

        println("Inline Completion Triggered")
        val document = editor.document
        var prefix = document.getText(TextRange(0,currentOffset))
        val postfix = document.getText(TextRange(currentOffset, document.getLineEndOffset(document.lineCount-1)))
        val startToken = "<fim-prefix>"
        val endToken = "<fim-suffix>"
        val middleToken = "<fim-middle>"
        val prompt:String
        val fillInMiddle = postfix.trim().isNotEmpty()
        val lineEndOffset = document.getLineEndOffset(document.getLineNumber(triggerOffset))  //Lookahead replace until end of line

        prefix = prefix.removeSuffix(userPrefix)
        prefix = prefix + lookAheadItem //get prefix as if popup suggestion was accepted

        if(fillInMiddle){
            prompt = "${startToken}${prefix}${endToken}${postfix}${middleToken}"
        }
        else{
            prompt = prefix;
        }

//        let cacheItem:CachePrompt = {
//                prefix: prompt,
//                completionType: CompletionType.lookAheadSuggestion
//        };
//
//        let inlineCompletion:string|undefined = globalCache.get(sha1(JSON.stringify(prompt)));

        // get all editor values before calling the API, the editor can change while we wait.
        var inlineCompletion = PredictionUtils.debouncedInlineCompletion(prompt)?.result

        if(inlineCompletion!=null){
            inlineCompletion = lookAheadItem.removePrefix(userPrefix) + inlineCompletion.substring(prompt.length)

//            inlineCompletion? globalCache.set(sha1(JSON.stringify(prompt)), inlineCompletion) : undefined;

//            // Also cache inlineSuggestion, this will be shown when user accepts LookAheadSuggestion in order to maintain a seamless experience.
//            let ifAcceptedLookAheadSuggestion = prediction? prediction.result.slice(prompt.length) : undefined;
//            cacheItem.completionType = CompletionType.inlineSuggestion;
//            ifAcceptedLookAheadSuggestion ? globalCache.set(sha1(JSON.stringify(prompt)),ifAcceptedLookAheadSuggestion) : undefined;

            println(inlineCompletion)
            if(isFirstLineSubstring(Utils.asLines(inlineCompletion)[0], document.getText(TextRange(triggerOffset, lineEndOffset))))
                return listOf(InlineCompletionItem(inlineCompletion, triggerOffset, lineEndOffset))
            return listOf(InlineCompletionItem(inlineCompletion, triggerOffset, triggerOffset))

        }
        return emptyList()
    }



    private fun isValidMidlinePosition(document: Document, offset: Int): Boolean {
        val END_OF_LINE_VALID_PATTERN = Pattern.compile("^\\s*[)}\\]\"'`]*\\s*[:{;,]?\\s*$")
        val lineIndex = document.getLineNumber(offset)
        val suffix = document.getText(TextRange(offset, document.getLineEndOffset(lineIndex)))
        return END_OF_LINE_VALID_PATTERN.matcher(suffix).matches()
    }

    private fun isFirstLineSubstring(firstLine: String, lineSuffix: String): Boolean{
        return firstLine.indexOf(lineSuffix.trimEnd()) != -1
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