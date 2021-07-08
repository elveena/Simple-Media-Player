package com.raywenderlich.mediaplayer

import android.app.Activity
import android.content.Intent
import android.media.MediaDrm
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_video.*

@RequiresApi(Build.VERSION_CODES.O)
class VideoActivity : AppCompatActivity(), SurfaceHolder.Callback, SeekBar.OnSeekBarChangeListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnDrmInfoListener {

  private val mediaPlayer = MediaPlayer()
  private lateinit var runnable: Runnable
  private var handler = Handler(Looper.getMainLooper())
  private lateinit var selectedVideoUri: Uri

  companion object {
    const val GET_VIDEO = 123
    const val SECOND = 1000
    const val URL =
        "https://res.cloudinary.com/dit0lwal4/video/upload/v1597756157/samples/elephants.mp4"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    // Switch to AppTheme for displaying the activity
    setTheme(R.style.AppTheme)

    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_video)
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    mediaPlayer.setOnPreparedListener(this)
    // The line below tells the MediaPlayer that it could be accessing a DRM
    //protected file. If the media file is DRM protected, the app invokes onDrmInfo()
    mediaPlayer.setOnDrmInfoListener(this)
    video_view.holder.addCallback(this)
    seek_bar.setOnSeekBarChangeListener(this)
    play_button.isEnabled = false

    //TODO(7): Add the code for play button onClick listener
    play_button.setOnClickListener {
      // check if the mediaplayer is playing any video
      if (mediaPlayer.isPlaying) {
        // if it is, you pause the video and change the button icon to play
        mediaPlayer.pause()
        play_button.setImageResource(android.R.drawable.ic_media_play)
      } else {
        // if not, play the video and change the button icon to pause
        mediaPlayer.start()
        play_button.setImageResource(android.R.drawable.ic_media_pause)
      }
    }

  }

  // TODO(4): Converting seconds to mm:ss format to display on screen
  //In this function you convert seconds to MM:SS format. If the video is more than 60 seconds long, it’s better to show 2:32 minutes rather than 152 seconds.
  private fun timeInString(seconds: Int): String {
    return String.format(
      "%02d:%02d",
      (seconds / 3600 * 60 + ((seconds % 3600) / 60)),
      (seconds % 60)
    )
  }


  // TODO(5): Initialize seekBar
  private fun initializeSeekBar() {
    // sets the max value of seekbar
    seek_bar.max = mediaPlayer.seconds
    // sets the default values for textViews which shows the progress and the total duration of the video
    text_progress.text = getString(R.string.default_value)
    text_total_time.text = timeInString(mediaPlayer.seconds)
    // hides the progressbar
    progress_bar.visibility = View.GONE
    // enables the play button
    play_button.isEnabled = true
  }


  // TODO(6): Update seek bar after every 1 second
  //In this function, you use Runnable to execute the code periodically
  // after every one second. Runnable is a Java interface and executes on a
  // thread. Since it executes on a separate thread, it won't block your UI
  // and the SeekBar and TextViews will update periodically.
  private fun updateSeekBar() {
    runnable = Runnable {
      text_progress.text = timeInString(mediaPlayer.currentSeconds)
      seek_bar.progress = mediaPlayer.currentSeconds
      handler.postDelayed(runnable, SECOND.toLong())
    }
    handler.postDelayed(runnable, SECOND.toLong())
  }


  // SurfaceHolder is ready
  override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
    // TODO(1): Add the code that will get executed when surfaceView creates a holder
    /* The code below is for playing a video from the source mentioned
      mediaPlayer.apply {
      setDataSource(
        applicationContext,
        // pass the location, URI of the video
        Uri.parse("android.resource://$packageName/raw/test_video")
      )
      // set the MediaPlayer's display to the VideoView's surface by calling setDisplay(surfaceHolder)
      setDisplay(surfaceHolder)
      // call prepare() that prepares Mediaplayer ot playback the video synchronously on the main thread
      prepareAsync()
    }*/
    // The code below is for playing a video from the gallery
    mediaPlayer.apply {
      setDataSource(applicationContext, selectedVideoUri)
      setDisplay(surfaceHolder)
      prepareAsync()
    }

    //The code below is for streaming a video from a URL
      /*mediaPlayer.apply {
        setDataSource(URL)
        setDisplay(surfaceHolder)
        prepareAsync()
      }*/
    }



    // Ignore
  override fun surfaceChanged(surfaceHolder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

  }

  // Ignore
  override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {

  }

  // For DRM files
  override fun onDrmInfo(mediaPlayer: MediaPlayer?, drmInfo: MediaPlayer.DrmInfo?) {
    //TODO(12): Add the code that will get executed when the DRM info is ready
    mediaPlayer?.apply {
      // Fetch the UUID or key of the scheme in use
      val key = drmInfo?.supportedSchemes?.get(0)
      key?.let {
        // pass this retrieved key to prepare the DRM
        prepareDrm(key)
        // Get the decrypt key from the licensed sever to the DRM engine plugin using provideKeyResponse()
        val keyRequest = getKeyRequest(
          null, null, null,
          MediaDrm.KEY_TYPE_STREAMING, null
        )
        provideKeyResponse(null, keyRequest.data)
      }
    }

  }

  // This function gets called when the media player gets ready
  override fun onPrepared(mediaPlayer: MediaPlayer?) {
    //TODO(2): Add the code that will get executed when the media player is ready
    //Make the progressbar invisible
    progress_bar.visibility = View.GONE
    //Tell the MediaPlayer to start the video
    mediaPlayer?.start()

    initializeSeekBar()
    updateSeekBar()
  }

  // Update media player when user changes seekBar
  override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    //TODO(8): Add the code that will get executed when the user interacts and changes value of seekBar
    if (fromUser){
      mediaPlayer.seekTo(progress * SECOND)
    }

  }

  // Ignore
  override fun onStartTrackingTouch(seekBar: SeekBar?) {
  }

  // Ignore
  override fun onStopTrackingTouch(seekBar: SeekBar?) {
  }

  // Create option menu in toolbar
  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val inflater: MenuInflater = menuInflater
    inflater.inflate(R.menu.app_menu, menu)
    return true
  }

  // Invoked when an option is selected in menu
  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle item selection
    return when (item.itemId) {
      R.id.select_file -> {
        //TODO(9): Add the code that will get executed when the user select the option in menu
        // You get an intent which is a messaging object in android used to request different action types
        val intent = Intent()
        // Ensuring that the intent is of video format
        intent.type = "video/*"
        // Specifying this is an intet with an actionn of GET content type
        intent.action = Intent.ACTION_GET_CONTENT
        // Triggering an intent and waiting for a result
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), GET_VIDEO)

        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  // Invoked when a video is selected from the gallery
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    //TODO(10): Add the code that will get executed when the video is selected from gallery
    // check the resultCode. If the operation was executed successfully, it returns Activity.RESULT_OK
    if (resultCode == Activity.RESULT_OK) {
      // check the request code to identify the caller and define if the requestCode was actually a video
      if (requestCode == GET_VIDEO) {
        // Assign a URI to the selectedVideoUri variable declared earlier
        selectedVideoUri = data?.data!!
        // Invoke surfaceCreated() by calling video_view.holder.addCallback(this)
        video_view.holder.addCallback(this)
      }
    }

  }

  // Release the media player resources when activity gets destroyed
  override fun onDestroy() {
    super.onDestroy()
    //TODO(11): Add the code that will get executed when the activity gets dhandler.removeCallbacks(runnable)
    //Removing the runnable from the thread by calling removeCallbacks()
    handler.removeCallbacks(runnable)
    mediaPlayer.release()


  }

  //TODO(3): Create extension properties to get the media player total duration and current duration in seconds
  // seconds returns the total duration of the video in seconds
  private val MediaPlayer.seconds: Int
    get() {
      return this.duration / SECOND
    }

  // currentSeconds returns the current playback position in seconds of the video
  private val MediaPlayer.currentSeconds: Int
    get() {
      return this.currentPosition / SECOND
    }

}