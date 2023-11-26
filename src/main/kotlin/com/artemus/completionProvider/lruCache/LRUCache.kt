package com.artemus.completionProvider.lruCache

import java.security.InvalidParameterException

data class LRUCacheNode(var key: String, var value: String, var prev: Int?, var next: Int? )

// TODO: Need to make this thread safe since multiple calls to InlineCompletions might access simultaneously
class LRUCache(private val size: Int) {
    private val map: HashMap<String, Int>
    private var linkedList: Array<LRUCacheNode?>
    private var head:Int?  // LRU element is at head
    private var tail: Int? //MRU element is at tail
    private var numElements: Int

    init {
        if(size <= 0){
            throw InvalidParameterException("Size should be Greater than zero")
        }
        this.map = hashMapOf()
        this.linkedList = arrayOfNulls(size)
        this.head = null
        this.tail = null
        this.numElements = 0
    }

    /************************************************************************************************************************************************************************************************/

    fun get(key: String): String?{
        val index = this.map[key]
        val node =  if(index !== null) this.linkedList[index] else null
        if(node!==null){
            this.set(node.key,node.value)
            return node.value
        }
        else{
            return null
        }
    }

    /************************************************************************************************************************************************************************************************/

    fun set(key: String, value: String){
        if(this.map[key] !==null){
            this.moveElementToMRU(key,value)
        }
        else{
            if(this.isFull()){
                this.replaceLRUElement(key,value)
            }
            else{
                this.addElementToEnd(key,value)
            }
        }
    }

    /************************************************************************************************************************************************************************************************/

    fun clearCache(){
        this.map.clear()
        this.head = null
        this.tail = null
        this.numElements = 0
        this.linkedList = arrayOfNulls(size)
    }

    /************************************************************************************************************************************************************************************************/

    fun printCache(){
        println("MAP")
        println("-".repeat(100))
        this.map.forEach{
            println("Key: ${it.key}, index: ${it.value}")
        }
        println()

        println("Linked List")
        println("-".repeat(100))
        var temp = this.head
        while(temp!==null){
            val elem = this.linkedList[temp]!!
            println("Key: ${elem.key} Result: ${elem.value}, Next: ${elem.next}, Prev: ${elem.prev}")
            temp = elem.next
        }
        println()

        println("Array State")
        println("-".repeat(100))
        println(this.linkedList)
        println()

        println("LRU: ${this.getLRUElement()}")
        println("MRU: ${this.getMRUElement()}")
    }


    /************************************************************************************************************************************************************************************************/

    private fun moveElementToMRU(key: String, value: String){
        val index = this.map[key]!!
        val currentNode = this.linkedList[index]!!

        if(index == this.tail){
            currentNode.key = key
            currentNode.value = value
            return
        } //already MRU, i.e. the tail.

        if(index == this.head){
            //key at head
            this.head = currentNode.next
            val nextLRUNode = if(this.head!==null) this.linkedList[this.head!!] else null
            if(nextLRUNode !== null)  nextLRUNode.prev = null
        }
        else{
            //key in the middle of list
            val nextNode = if(currentNode.next!==null) this.linkedList[currentNode.next!!] else null
            val prevNode = if(currentNode.prev!==null) this.linkedList[currentNode.prev!!] else null
            prevNode!!.next = currentNode.next
            nextNode!!.prev = currentNode.prev
        }

        val currentMRUNode = if(this.tail!== null) this.linkedList[this.tail!!] else null
        if(currentMRUNode!==null)  currentMRUNode.next=index
        currentNode.prev = this.tail
        currentNode.next = null
        currentNode.key = key
        currentNode.value = value
        this.tail = index
    }

    /************************************************************************************************************************************************************************************************/

    private fun addElementToEnd(key:String, value: String){
        val newIndex = this.numElements
        val prevNode = if(this.tail!==null) this.linkedList[this.tail!!] else null
        if(prevNode!==null){
            prevNode.next = newIndex
        }
        this.linkedList[newIndex] = LRUCacheNode(key= key,value= value, prev= this.tail, next= null)
        if(this.isEmpty()){this.head = newIndex}
        this.map[key] = newIndex
        this.tail = newIndex // new element is MRU
        this.numElements += 1
    }

    /************************************************************************************************************************************************************************************************/

    private fun replaceLRUElement(key:String, value: String){
        val currentLRUIndex = this.head!!
        val currentMRUIndex = this.tail!!
        val currentLRUNode = this.linkedList[currentLRUIndex]!!
        val currentMRUNode = this.linkedList[currentMRUIndex]!!

        //case when cache size is 1
        if(currentLRUIndex==currentMRUIndex){
            this.map.remove(currentLRUNode.key)
            this.map[key] = currentLRUIndex
            currentLRUNode.value = value
            currentLRUNode.key = key
            return
        }

        this.head = if(currentLRUNode.next!==null) currentLRUNode.next else null
        val nextLRUNode = this.linkedList[this.head!!]
        if(nextLRUNode !== null) nextLRUNode.prev = null

        val newMRUIndex = currentLRUIndex
        val newMRUNode = currentLRUNode
        this.map.remove(currentLRUNode.key)
        this.map[key] = newMRUIndex
        currentMRUNode.next  = newMRUIndex
        newMRUNode.next = null
        this.tail = newMRUIndex
        newMRUNode.prev = currentMRUIndex
        newMRUNode.value = value
        newMRUNode.key = key
    }


    /************************************************************************************************************************************************************************************************/

    private fun getLRUElement(): String?{
        return  if(this.head!==null) this.linkedList[this.head!!]?.value else null
    }

    /************************************************************************************************************************************************************************************************/

    private fun getMRUElement(): String?{
        return  if(this.tail!==null) this.linkedList[this.tail!!]?.value else null
    }

    /************************************************************************************************************************************************************************************************/

    private fun isEmpty(): Boolean{
        return this.numElements == 0
    }

    /************************************************************************************************************************************************************************************************/

    private fun isFull(): Boolean{
        return this.numElements == this.size
    }

    /************************************************************************************************************************************************************************************************/

}