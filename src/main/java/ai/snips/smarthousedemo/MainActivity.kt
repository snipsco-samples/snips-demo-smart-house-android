package ai.snips.smarthousedemo

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.transition.Fade
import android.transition.Slide
import android.transition.TransitionManager
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.LazyThreadSafetyMode.NONE


class MainActivity : AppCompatActivity() {

    companion object {
        private const val AUDIO_ECHO_REQUEST = 0
    }

    private val model by lazy(NONE) { ViewModelProviders.of(this)[RoomsViewModel::class.java] }

    private val adapter by lazy(NONE) { RoomsAdapter(Rooms.asList()) }

    private val roomDataObserver = Observer<List<RoomModel>> { adapter.update(it!!) }
    private val listeningObserver = Observer<Boolean> {
        if (it!!) {
            findViewById<FloatingActionButton>(R.id.fab).setImageResource(R.drawable.mic_listening)
            findViewById<AppCompatTextView>(R.id.listening_text).visibility = VISIBLE
        } else {
            findViewById<FloatingActionButton>(R.id.fab).setImageResource(R.drawable.mic)
            findViewById<AppCompatTextView>(R.id.listening_text).visibility = GONE
        }
    }
    private val sessionListeningObserver = Observer<Boolean> {
        TransitionManager.beginDelayedTransition(animationContainer, Fade())
        if (it!!) {
            findViewById<View>(R.id.modal_background).visibility = VISIBLE
        } else {
            findViewById<View>(R.id.modal_background).visibility = GONE
            findViewById<View>(R.id.modal).visibility = GONE
        }
    }
    private val readyObserver = Observer<Boolean> {
        if (it!!) {
            findViewById<RecyclerView>(R.id.recycler_view).visibility = VISIBLE
            findViewById<ProgressBar>(R.id.loading_bar).visibility = GONE
        } else {
            findViewById<RecyclerView>(R.id.recycler_view).visibility = GONE
            findViewById<ProgressBar>(R.id.loading_bar).visibility = VISIBLE
        }
    }
    private val roomUpdateObserver = Observer<RoomUpdateModel> {
        val modal = findViewById<LinearLayout>(R.id.modal)
        TransitionManager.beginDelayedTransition(animationContainer, Slide())

        it?.let {
            val beforeRoomItem = it.beforeRoomItem
            val afterRoomItem = it.afterRoomItem

            modal.apply {
                visibility = VISIBLE

                findViewById<TextView>(R.id.target_room_name).text = it.updatedRooms.map { getText(it.nameRes) }.joinToString()
                findViewById<ImageView>(R.id.target_room_item_1_img).setImageResource(beforeRoomItem.imageResId)

                beforeRoomItem.value.res?.let { this.findViewById<TextView>(R.id.target_room_item_1_name).setText(it) }
                beforeRoomItem.value.value?.let { this.findViewById<TextView>(R.id.target_room_item_1_name).text = it }
            }

            Handler().postDelayed({
                modal.apply {
                    findViewById<ImageView>(R.id.target_room_item_1_img).setImageResource(afterRoomItem.imageResId)
                    afterRoomItem.value.res?.let { this.findViewById<TextView>(R.id.target_room_item_1_name).setText(it) }
                    afterRoomItem.value.value?.let { this.findViewById<TextView>(R.id.target_room_item_1_name).text = it }
                }
            }, 500)
        }

    }

    private val animationContainer: ConstraintLayout by lazy(NONE) { findViewById<ConstraintLayout>(R.id.animation_container) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<RecyclerView>(R.id.recycler_view).apply {
            adapter = this@MainActivity.adapter
            layoutManager = GridLayoutManager(this@MainActivity, getSpanCount())
            setHasFixedSize(true)
        }

        findViewById<FloatingActionButton>(R.id.fab).apply {
            setOnClickListener {
                if (canUseModel()) {
                    model.startSession()
                }
            }
        }
    }


    fun getSpanCount() = resources.getInteger(R.integer.column_number)


    private fun canUseModel() = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED


    private fun ensurePermissions() = canUseModel().also {
        if (!it) ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), AUDIO_ECHO_REQUEST)
    }

    private fun register() = ensurePermissions().also {
        if (it) {
            model.platformReady.observe(this, readyObserver)
            model.roomData.observe(this, roomDataObserver)
            model.roomUpdateListener.observe(this, roomUpdateObserver)
            model.listening.observe(this, listeningObserver)
            model.sessionListener.observe(this, sessionListeningObserver)
        }
    }

    override fun onResume() {
        super.onResume()
        if (register()) model.resume()
    }

    override fun onPause() {
        if (canUseModel()) model.pause()
        super.onPause()
    }
}
