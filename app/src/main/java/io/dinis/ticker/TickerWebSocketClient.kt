package io.dinis.ticker

import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import okhttp3.*
import okio.ByteString


/**
 * Created by Dinis Ishmukhametov on 18.07.2018.
 * dinis.ish@gmail.com
 */
class TickerWebSocketClient(val pairName: String): WebSocketListener() {

    private val broadcast = ConflatedBroadcastChannel<String>()
    private val client: WebSocket

    init {
        val client = OkHttpClient()
        val request = Request.Builder()
                .url("wss://api.bitfinex.com/ws")
                .build()
        this.client = client.newWebSocket(request, this)
    }


    override fun onMessage(webSocket: WebSocket?, text: String?) {
        if (text!=null) {
            broadcast.offer(text)
        }
    }

    override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {

    }


    override fun onOpen(webSocket: WebSocket?, response: Response?) {
        webSocket?.send("{\n" +
                "   \"event\":\"subscribe\",\n" +
                "   \"channel\":\"ticker\",\n" +
                "   \"pair\":\"$pairName\"\n" +
                "}")
    }

    fun subscription(): ReceiveChannel<String>{
        return broadcast.openSubscription()
    }

    fun close() {
        broadcast.close()
        client.close(1000, "clear")
    }

}