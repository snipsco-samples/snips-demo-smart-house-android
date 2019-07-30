package ai.snips.smarthousedemo

import ai.snips.platform.SnipsPlatformClient
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder.AudioSource
import android.os.Process
import java.util.concurrent.locks.ReentrantLock

class AudioThread(private val client: SnipsPlatformClient) : Thread() {
    companion object {
        // If we try to create a new AudioRecord while another is running, we get an error,
        // so lets put that behind a lock
        private val lock = ReentrantLock()

        private const val FREQUENCY = 16000
        private const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }

    @Volatile
    private var continueStreaming = true

    override fun run() {
        lock.lock()
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        runStreaming()
    }

    private fun runStreaming() {
        var recorder: AudioRecord? = null
        try {
            println("starting audio streaming")
            val minBufferSizeInBytes = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL, ENCODING)
            println("minBufferSizeInBytes: $minBufferSizeInBytes")

            recorder = AudioRecord(AudioSource.MIC, FREQUENCY, CHANNEL, ENCODING, /*minBufferSizeInBytes */ 2048)
            if (recorder.state == AudioRecord.STATE_INITIALIZED) {
                recorder.startRecording()
                while (continueStreaming) {
                    val buffer = ShortArray(minBufferSizeInBytes / 2)
                    recorder.read(buffer, 0, buffer.size)
                    client.sendAudioBuffer(buffer)

                }
            } else {
                throw RuntimeException("could not grab sound")
            }

        } finally {
            try {
                recorder?.stop()
            } catch (e: Exception) {
                println("could not stop recorder: $e")
            }
            try {
                recorder?.release()
            } catch (e: Exception) {
                println("could not release recorder: $e")
            }

            lock.unlock()
            println("audio streaming stopped")
        }
    }

    fun stopStreaming() {
        continueStreaming = false
    }
}
