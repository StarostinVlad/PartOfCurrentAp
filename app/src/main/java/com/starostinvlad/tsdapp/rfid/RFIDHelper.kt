package com.starostinvlad.tsdapp.rfid

import android.util.Log
import cn.pda.serialport.Tools
import com.android.hdhe.uhf.reader.UhfReader
import com.android.hdhe.uhf.readerInterface.TagModel
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class RFIDHelper @Inject constructor() : CoroutineScope {
    private var listeners: MutableList<RfidListener> = mutableListOf()
    private val manager: UhfReader? = UhfReader.getInstance()
    private var readFlag = false
    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    fun addListener(listener: RfidListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: RfidListener) {
        listeners.remove(listener)
    }

    fun readRfidTag() {
        readFlag = true
    }

    init {
        launch {
            while (manager != null) {
                if (readFlag) {
                    val tagList = manager.inventoryRealTime() //实时盘存
//                    Log.d("RfidHelper", "tag: ${tagList}")
                    if (tagList!!.isNotEmpty()) {
                        Log.d("RfidHelper", "tag: ${tagList.first().getmEpcBytes()}")
                        Log.d("RfidHelper", "listeners:${listeners.size}")
                        listeners.forEach { listener ->
                            tagList.forEach { tag ->
                                withContext(Dispatchers.Main) {
                                    listener.onTagRead(
                                        Tools.Bytes2HexString(
                                            tag.getmEpcBytes(),
                                            tag.getmEpcBytes().size
                                        )
                                    )
                                }
                            }
                        }
                    }
                    readFlag = false
                }
                delay(50)
            }
        }
    }

    fun destroy() {
        job.cancelChildren()
    }
}

interface RfidListener {
    fun onTagRead(rfidTagEpc: String)
}
