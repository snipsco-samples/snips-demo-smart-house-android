package ai.snips.smarthousedemo

import ai.snips.platform.SnipsPlatformClient
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.Handler


class RoomsViewModel : ViewModel() {

    val roomData = MutableLiveData<List<RoomModel>>().apply { postValue(Rooms.asList()) }

    val platformReady = MutableLiveData<Boolean>().apply { postValue(false) }

    val listening = MutableLiveData<Boolean>().apply { postValue(false) }

    val sessionListener = MutableLiveData<Boolean>().apply { postValue(false) }

    val roomUpdateListener = MutableLiveData<RoomUpdateModel>()

    val pendingHandler = Handler()

    private val snipsPlatformClient = SnipsPlatformClient.Builder(SmartHouseApplication.assistantDir)
            .enableLogs(true)
            .enableStreaming(true)
            .build()
            .apply {
                onIntentDetectedListener = {
                    println("received an intent $it")

                    val updateDiffsData = Rooms.getUpdateDiff(it)
                    val roomData = Rooms.applyIntent(it)

                    roomUpdateListener.postValue(updateDiffsData) // Send the update to the UI first ...
                    pendingHandler.postDelayed({ // ... And update the data later
                        this@RoomsViewModel.roomData.postValue(roomData)
                    }, 500)
                    endSession(sessionId = it.sessionId, text = null)
                }
                onPlatformReady = {
                    platformReady.postValue(true)
                }
                onListeningStateChangedListener = {
                    listening.postValue(it)
                }
                onSessionStartedListener = {
                    pendingHandler.removeCallbacksAndMessages(null)
                    sessionListener.postValue(true)
                }
                onSessionEndedListener = {
                    pendingHandler.postDelayed({
                        sessionListener.postValue(false)
                    }, 2000)
                }

                startStreaming(this)
                this.connect(SmartHouseApplication.appContext)
            }


    private var firstStart = true

    fun resume() {
        if (firstStart) {
            firstStart = false
        } else {
            startStreaming(snipsPlatformClient)
            snipsPlatformClient.resume()
        }
    }

    private var audioThread: AudioThread? = null

    private fun startStreaming(client: SnipsPlatformClient) {
        audioThread = AudioThread(client).apply { start() }
    }


    fun pause() {
        audioThread?.stopStreaming()
        audioThread = null
        snipsPlatformClient.pause()
    }

    override fun onCleared() {
        println("disconnecting snips platform")
        snipsPlatformClient.disconnect()
        super.onCleared()
    }

    fun startSession() {
        snipsPlatformClient.startSession(null, listOf(), false, null)
    }
}
