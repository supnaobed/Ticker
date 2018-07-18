package io.dinis.ticker

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.launch




/**
 * Created by Dinis Ishmukhametov on 19.07.2018.
 * dinis.ish@gmail.com
 */
class MainViewModel : ViewModel() {

    var client: TickerWebSocketClient = TickerWebSocketClient("BTCUSD")

    private var liveData: MediatorLiveData<ChartViewState> = MediatorLiveData()
    private var points : MutableList<Float> = ArrayList()

    fun observe(owner: LifecycleOwner, observer: Observer<ChartViewState>) {
        liveData.observe(owner, observer)
    }


    init {
        liveData.postValue(ChartViewState(true, emptyList()))
        launch(CommonPool) {
            client.subscription()
                    .parsePoint()
                    .consumeEach {
                        points.add(it)
                        liveData.postValue(ChartViewState(points.size < 2, points))
                    }
        }
    }

    private fun ReceiveChannel<String>.parsePoint() = produce<Float> {
        consumeEach {
            if (it.startsWith("[").and(it.endsWith("]"))) {
                val element = it.removePrefix("[").removeSuffix("]").split(",")
                if (element.size >= 9){
                    val element1 = element[8].toFloatOrNull()
                    if (element1!=null) send(element1)
                }
            }
        }
    }

    override fun onCleared() {
        client.close()
    }


}