package tysovsky.skyplayer.Services;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RemoteControlClient;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import tysovsky.skyplayer.Activities.MainActivity;
import tysovsky.skyplayer.Helpers.AudioFocusHelper;
import tysovsky.skyplayer.Interfaces.MusicFocusable;
import tysovsky.skyplayer.Helpers.MediaButtonHelper;
import tysovsky.skyplayer.MusicIntentReceiver;
import tysovsky.skyplayer.MusicManager;
import tysovsky.skyplayer.MusicRetriever;
import tysovsky.skyplayer.R;
import tysovsky.skyplayer.RemoteControlClientCompat;
import tysovsky.skyplayer.Helpers.RemoteControlHelper;
import tysovsky.skyplayer.Song;

/**
 * Service that handles media playback. This is the Service through which we perform all the media
 * handling in our application. Upon initialization, it starts a {@link MusicRetriever} to scan
 * the user's media. Then, it waits for Intents (which come from our main activity,
 * {@link MainActivity}, which signal the service to perform specific operations: Play, Pause,
 * Rewind, Skip, etc.
 */
public class MusicService extends Service implements OnCompletionListener, OnPreparedListener,
        OnErrorListener, MusicFocusable
{
    // The tag we put on debug messages
    final static String TAG = "MusicService";


    // These are the Intent actions that we are prepared to handle. Notice that the fact these
    // constants exist in our class is a mere convenience: what really defines the actions our
    // service can handle are the <action> tags in the <intent-filters> tag for our service in
    // AndroidManifest.xml.
    public static final String ACTION_PLAY = "tysovsky.skyplayer.action.PLAY";
    public static final String ACTION_RESUME = "tysovsky.skyplayer.action.RESUME";
    public static final String ACTION_PAUSE = "tysovsky.skyplayer.action.PAUSE";
    public static final String ACTION_STOP = "tysovsky.skyplayer.action.STOP";
    public static final String ACTION_SKIP_PREVIOUS = "tysovsky.skyplayer.action.SKIP_PREVIOUS";
    public static final String ACTION_SKIP_NEXT = "tysovsky.skyplayer.action.SKIP_NEXT";
    public static final String ACTION_REWIND_TO = "tysovsky.skyplayer.action.REWIND_TO";
    public static final String ACTION_STOP_UPDATING_DURATION = "tysovsky.skyplayer.action.STOP_UPDATING_DURATION";
    public static final String ACTION_REQUEST_SONG_UPDATE = "tysovsky.skyplayer.action.REQUEST_SONG_UPDATE";



    public List<Song> songQueue = new ArrayList<>();
    public int currentSongIndex = 0;

    // The volume we set the media player to when we lose audio focus, but are allowed to reduce
    // the volume instead of stopping playback.
    public static final float DUCK_VOLUME = 0.1f;
    // our media player
    MediaPlayer mPlayer = null;
    // our AudioFocusHelper object, if it's available (it's available on SDK level >= 8)
    // If not available, this will be null. Always check for null before using!
    AudioFocusHelper mAudioFocusHelper = null;

    // indicates the state our service:
    enum State {
        Stopped,    // media player is stopped and not prepared to play
        Preparing,  // media player is preparing...
        Playing,    // playback active (media player ready!). (but the media player may actually be
        // paused in this state if we don't have audio focus. But we stay in this state
        // so that we know we have to resume playback once we get focus back)
        Paused      // playback paused (media player ready!)
    }

    ;
    State mState = State.Stopped;

    enum PauseReason {
        UserRequest,  // paused by user request
        FocusLoss,    // paused because of audio focus loss
    }

    ;
    // why did we pause? (only relevant if mState == State.Paused)
    PauseReason mPauseReason = PauseReason.UserRequest;

    // do we have audio focus?
    enum AudioFocus {
        NoFocusNoDuck,    // we don't have audio focus, and can't duck
        NoFocusCanDuck,   // we don't have focus, but can play at a low volume ("ducking")
        Focused           // we have full audio focus
    }

    AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;
    // title of the song we are currently playing
    String mSongTitle = "";
    // whether the song we are playing is streaming from the network
    boolean mIsStreaming = false;


    // The ID we use for the notification (the onscreen alert that appears at the notification
    // area at the top of the screen as an icon -- and as text as well if the user expands the
    // notification area).
    final int NOTIFICATION_ID = 1;

    // our RemoteControlClient object, which will use remote control APIs available in
    // SDK level >= 14, if they're available.
    RemoteControlClientCompat mRemoteControlClientCompat;
    // Dummy album art we will pass to the remote control (if the APIs are available).
    Bitmap mAlbumArt;
    // The component name of MusicIntentReceiver, for use with media button and remote control
    // APIs
    ComponentName mMediaButtonReceiverComponent;
    AudioManager mAudioManager;
    NotificationManager mNotificationManager;
    Notification.Builder mNotificationBuilder = null;


    private ProgressReporter progressReporter = null;

    public static Activity currentActivity;
    public static final Object CURRENTACTIVITYLOCK = new Object();

    /**
     * Makes sure the media player exists and has been reset. This will create the media player
     * if needed, or reset the existing media player if one already exists.
     */
    void createMediaPlayerIfNeeded() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            // Make sure the media player will acquire a wake-lock while playing. If we don't do
            // that, the CPU might go to sleep while the song is playing, causing playback to stop.
            //
            // Remember that to use this, we have to declare the android.permission.WAKE_LOCK
            // permission in AndroidManifest.xml.
            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            // we want the media player to notify us when it's ready preparing, and when it's done
            // playing:
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
        } else
            mPlayer.reset();
    }


    @Override
    public void onCreate() {
        Log.i(TAG, "debug: Creating service");
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // create the Audio Focus Helper, if the Audio Focus feature is available (SDK 8 or above)
        if (android.os.Build.VERSION.SDK_INT >= 8)
            mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
        else
            mAudioFocus = AudioFocus.Focused; // no focus feature, so we always "have" audio focus

        mMediaButtonReceiverComponent = new ComponentName(this, MusicIntentReceiver.class);

        progressReporter = new ProgressReporter();
        new Thread(progressReporter).start();

    }


    /**
     * Called when we receive an Intent. When we receive an intent sent to us via startService(),
     * this is the method that gets called. So here we react appropriately depending on the
     * Intent's action, which specifies what is being requested of us.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle extras = intent.getExtras();
        String action = intent.getAction();

        switch (action){
            case ACTION_PLAY:
                //Retrieve the current songs queue
                songQueue = extras.getParcelableArrayList("QUEUE");
                //Retrieve the position of the current song in the queue
                currentSongIndex = extras.getInt("POSITION");
                processPlayRequest();
                break;

            case ACTION_RESUME:
                processResumeRequest();
                break;
            case ACTION_PAUSE:
                processPauseRequest();
                break;
            case ACTION_STOP:
                processStopRequest();
                break;
            case ACTION_SKIP_PREVIOUS:
                processSkipPrevious();
                break;
            case ACTION_SKIP_NEXT:
                processSkipNext();
                break;
            case ACTION_REWIND_TO:
                int position = extras.getInt("POSITION",0);
                processRewindRequest(position);
                break;
            case ACTION_STOP_UPDATING_DURATION:
                progressReporter.pause();
                break;
            case ACTION_REQUEST_SONG_UPDATE:
                if(songQueue.size() > 0){
                    sendSong(songQueue.get(currentSongIndex));
                }
                break;

        }


        return START_NOT_STICKY; // Means we started the service, but don't want it to
        // restart in case it's killed.
    }

    //Play a new song
    void processPlayRequest() {

        tryToGetAudioFocus();

        // actually play the song
        if (!(mState == State.Preparing)) {
            // If we're stopped, just go ahead and play the first song
            playSong(currentSongIndex);

        }

        // Tell any remote controls that our playback state is 'playing'.
        if (mRemoteControlClientCompat != null) {
            mRemoteControlClientCompat
                    .setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        }
    }

    //Resume a paused song
    void processResumeRequest(){
        Log.i(TAG, "Resume Pressed Service");
        if(mState == State.Paused){
            mState = State.Playing;
            tryToGetAudioFocus();
            setUpAsForeground(songQueue.get(currentSongIndex));
            configAndStartMediaPlayer();
        }
    }

    //Pause a playing song
    void processPauseRequest() {
        if (mState == State.Playing) {
            // Pause media player and cancel the 'foreground service' state.
            mState = State.Paused;
            mPlayer.pause();
            relaxResources(false); // while paused, we always retain the MediaPlayer
            // do not give up audio focus
        }
        // Tell any remote controls that our playback state is 'paused'.
        if (mRemoteControlClientCompat != null) {
            mRemoteControlClientCompat
                    .setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
        }
    }

    void processRewindRequest(long position) {
        if (mState == State.Playing || mState == State.Paused) {
            mPlayer.seekTo((int)position);
            progressReporter.resume();
        }
    }

    void processSkipNext(){
        if (mState == State.Playing || mState == State.Paused) {
            tryToGetAudioFocus();
            if(currentSongIndex < songQueue.size()-1){
                currentSongIndex++;
            }
            else {
                currentSongIndex = 0;
            }
            playSong(currentSongIndex);
        }
    }

    void processSkipPrevious(){
        if (mState == State.Playing || mState == State.Paused) {
            tryToGetAudioFocus();
            if(currentSongIndex > 0){
                currentSongIndex = currentSongIndex-1;
            }
            else{
                currentSongIndex = songQueue.size()-1;
            }
            playSong(currentSongIndex);

        }
    }

    void processStopRequest() {
        processStopRequest(false);
    }

    void processStopRequest(boolean force) {
        if (mState == State.Playing || mState == State.Paused || force) {
            mState = State.Stopped;
            // let go of all resources...
            relaxResources(true);
            giveUpAudioFocus();
            // Tell any remote controls that our playback state is 'paused'.
            if (mRemoteControlClientCompat != null) {
                mRemoteControlClientCompat
                        .setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
            }
            // service is no longer necessary. Will be started again if needed.
            stopSelf();
        }
    }

    /**
     * Releases resources used by the service for playback. This includes the "foreground service"
     * status and notification, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer Indicates whether the Media Player should also be released or not
     */
    void relaxResources(boolean releaseMediaPlayer) {
        // stop being a foreground service
        stopForeground(true);
        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
    }

    void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.abandonFocus())
            mAudioFocus = AudioFocus.NoFocusNoDuck;
    }

    void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.requestFocus())
            mAudioFocus = AudioFocus.Focused;
    }

    /**
     * Play a song in the queue
     * @param position
     */
    void playSong(int position){
        mState = State.Stopped;
        relaxResources(false); // release everything except MediaPlayer
        try {
            Song currentSong = songQueue.get(position);
            //Set the album art
            mAlbumArt = MusicManager.getAlbumArt(currentSong.getAlbum_id(), this);
            mIsStreaming = false; // playing a locally available song

            // set the source of the media player a a content URI
            createMediaPlayerIfNeeded();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(getApplicationContext(), currentSong.getURI());

            //Send song update to the activity
            sendSong(currentSong);

            mSongTitle = currentSong.getTitle();
            mState = State.Preparing;

            setUpAsForeground(currentSong);


            // Use the media button APIs (if available) to register ourselves for media button
            // events
            MediaButtonHelper.registerMediaButtonEventReceiverCompat(
                    mAudioManager, mMediaButtonReceiverComponent);
            // Use the remote control APIs (if available) to set the playback state
            if (mRemoteControlClientCompat == null) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                intent.setComponent(mMediaButtonReceiverComponent);
                mRemoteControlClientCompat = new RemoteControlClientCompat(
                        PendingIntent.getBroadcast(this /*context*/,
                                0 /*requestCode, ignored*/, intent /*intent*/, 0 /*flags*/));
                RemoteControlHelper.registerRemoteControlClient(mAudioManager,
                        mRemoteControlClientCompat);
            }
            mRemoteControlClientCompat.setPlaybackState(
                    RemoteControlClient.PLAYSTATE_PLAYING);
            mRemoteControlClientCompat.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                            RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                            RemoteControlClient.FLAG_KEY_MEDIA_STOP);
            // Update the remote controls
            mRemoteControlClientCompat.editMetadata(true)
                    .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, currentSong.getArtist())
                    .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, currentSong.getAlbum())
                    .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, currentSong.getTitle())
                    .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION,
                            currentSong.getDuration())
                            // TODO: fetch real item artwork
                    .putBitmap(
                            RemoteControlClientCompat.MetadataEditorCompat.METADATA_KEY_ARTWORK,
                            mAlbumArt)
                    .apply();

            // starts preparing the media player in the background. When it's done, it will call
            // our OnPreparedListener (that is, the onPrepared() method on this class, since we set
            // the listener to 'this').
            //
            // Until the media player is prepared, we *cannot* call start() on it!
            mPlayer.prepareAsync();
        } catch (IOException ex) {
            Log.e("MusicService", "IOException playing next song: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Called when media player is done playing current song.
     */
    public void onCompletion(MediaPlayer player) {
        // The media player finished playing the current song, so we go ahead and start the next.
        processSkipNext();
    }

    /**
     * Called when media player is done preparing.
     */
    public void onPrepared(MediaPlayer player) {
        // The media player is done preparing. That means we can start playing!
        mState = State.Playing;
        updateNotification(songQueue.get(currentSongIndex));
        configAndStartMediaPlayer();
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and starts/restarts it. This
     * method starts/restarts the MediaPlayer respecting the current audio focus state. So if
     * we have focus, it will play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is allowed by the
     * current focus settings. This method assumes mPlayer != null, so if you are calling it,
     * you have to do so from a context where you are sure this is the case.
     */
    void configAndStartMediaPlayer() {
        if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
            // If we don't have audio focus and can't duck, we have to pause, even if mState
            // is State.Playing. But we stay in the Playing state so that we know we have to resume
            // playback once we get the focus back.
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
            }

            return;
        }
        else if (mAudioFocus == AudioFocus.NoFocusCanDuck) {
            mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);  // we'll be relatively quiet
        }
        else {
            mPlayer.setVolume(1.0f, 1.0f); // we can be loud
        }
        if (!mPlayer.isPlaying()){

            mPlayer.start();

            //Start sending messages
            //new Thread(observer).start();

        }
    }

    /**
     * Updates the notification.
     */
    void updateNotification(Song song) {
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setContentText(song.getAlbum())
                .setContentTitle(song.getTitle())
                .setContentIntent(pi);
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    /**
     * Configures service as a foreground service. A foreground service is a service that's doing
     * something the user is actively aware of (such as playing music), and must appear to the
     * user as a notification. That's why we create the notification here.
     */
    void setUpAsForeground(Song song) {
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        // Build the notification object.
        mNotificationBuilder = new Notification.Builder(getApplicationContext())
                .setStyle(new Notification.MediaStyle())
                .setSmallIcon(R.drawable.ic_play_arrow_white)
                .setTicker("1")
                .setUsesChronometer(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(song.getTitle())
                .setContentText(song.getAlbum())
                .setContentIntent(pi)
                .setLargeIcon(mAlbumArt)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setOngoing(true);
        addPlayPauseAction(mNotificationBuilder);
        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
    }


    private void addPlayPauseAction(Notification.Builder builder) {
        String label = "";
        int icon;
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(ACTION_PAUSE);
        PendingIntent pintent;
        if (mState == State.Playing) {
            icon = R.drawable.ic_pause_white;
            pintent = PendingIntent.getService(this, 579, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            icon = R.drawable.ic_play_arrow_white;
            pintent = PendingIntent.getService(this, 579, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        builder.addAction(new Notification.Action(icon, label, pintent));
    }

    /**
     * Called when there's an error playing media. When this happens, the media player goes to
     * the Error state. We warn the user about the error and reset the media player.
     */
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getApplicationContext(), "Media player error! Resetting.",
                Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra) + " state: " + mState);
        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
        return true; // true indicates we handled the error
    }

    public void onGainedAudioFocus() {
        Toast.makeText(getApplicationContext(), "gained audio focus.", Toast.LENGTH_SHORT).show();
        mAudioFocus = AudioFocus.Focused;
        // restart media player with new focus settings
        if (mState == State.Playing)
            configAndStartMediaPlayer();
    }

    public void onLostAudioFocus(boolean canDuck) {
        Toast.makeText(getApplicationContext(), "lost audio focus." + (canDuck ? "can duck" :
                "no duck"), Toast.LENGTH_SHORT).show();
        mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;
        // start/restart/pause media player with new focus settings
        if (mPlayer != null && mPlayer.isPlaying())
            configAndStartMediaPlayer();
    }

    @Override
    public void onDestroy() {
        // Service is being killed, so make sure we release our resources
        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    void sendSong(Song song){
        if(currentActivity != null){
            ((MainActivity)currentActivity).setSong(currentSongIndex);
        }
    }


    private class ProgressReporter implements Runnable {
        private AtomicBoolean stop = new AtomicBoolean(false);
        private AtomicBoolean pause = new AtomicBoolean(false);

        public ProgressReporter(){
        }

        public void stop() {
            stop.set(true);
        }

        public void pause(){
            pause.set(false);
        }

        public void resume(){
            pause.set(false);
        }

        @Override
        public void run() {
            while (!stop.get()) {
                if(!pause.get()){
                    try {
                        if(mPlayer != null && mPlayer.isPlaying() && currentActivity != null){

                            ((MainActivity)currentActivity).setSongProgress(mPlayer.getCurrentPosition());

                        }
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }



}