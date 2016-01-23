package tysovsky.skyplayer.Activities;

import android.app.ActivityManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.truizlop.fabreveallayout.FABRevealLayout;
import com.truizlop.fabreveallayout.OnRevealChangeListener;
import com.xgc1986.parallaxPagerTransformer.ParallaxPagerTransformer;

import tysovsky.skyplayer.Adapters.AlbumArtParallaxAdapter;
import tysovsky.skyplayer.Fragments.AlbumArtFragment;
import tysovsky.skyplayer.Fragments.MainFragment;
import tysovsky.skyplayer.Fragments.ExpandedPanelFragment;
import tysovsky.skyplayer.Fragments.MiniPlayerFragment;
import tysovsky.skyplayer.Fragments.TabAlbumsFragment;
import tysovsky.skyplayer.Fragments.TabArtistsFragment;
import tysovsky.skyplayer.Fragments.TabSongsFragment;
import tysovsky.skyplayer.Helpers.Enums;
import tysovsky.skyplayer.Interfaces.OnAlbumClickedListener;
import tysovsky.skyplayer.Interfaces.OnMusicQueryCompletedListener;
import tysovsky.skyplayer.Interfaces.OnSongSelectedListener;
import tysovsky.skyplayer.MusicManager;
import tysovsky.skyplayer.PrepareMusicRetrieverTask;
import tysovsky.skyplayer.Services.MusicService;
import tysovsky.skyplayer.R;
import tysovsky.skyplayer.Song;
import tysovsky.skyplayer.Helpers.Utilities;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMusicQueryCompletedListener, OnSongSelectedListener, OnAlbumClickedListener
{


    String TAG = "MainActivity";

    //Fragments
    private MiniPlayerFragment miniPlayerFragment;
    private ExpandedPanelFragment expandedPanelFragment;
    private MainFragment mainFragment;
    private FragmentManager fragmentManager;

    private SlidingUpPanelLayout playerLayout;



    ViewPager albumArtPager;
    AlbumArtParallaxAdapter albumArtParallaxAdapter;


    boolean optionsRevealed = false;
    FABRevealLayout optionLayout = null;
    FABRevealLayout fabRevealLayout = null;

    //Buttons
    ImageButton btnPlayerPlay;
    ImageButton btnPlayerSkipPrevious;
    ImageButton btnPlayerSkipNext;

    Button btnGoToAlbum;
    Button btnGoToArtist;
    Button btnQueue;
    Button btnLyrics;
    Button btnEqualizer;
    Button btnEditTags;

    //TextViews
    TextView tvPlayerSongTitle;
    TextView tvPlayerSongDuration;
    TextView tvPlayerSongCurrentDuration;

    //Progress slider
    SeekBar progressSlider;

    MusicManager mMusicManager;

    public static Song currentSong = null;
    public static long currentDuration;
    boolean sliderHeld = false;
    public boolean songPlaying = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "MainActivity onCreateView called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set status bar to primary color
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));

        //Initialize fabRevealLayout
        fabRevealLayout = (FABRevealLayout) findViewById(R.id.fab_reveal_layout);

        //Initialize fragments
        fragmentManager = getFragmentManager();

        miniPlayerFragment = MiniPlayerFragment.newInstance(MainActivity.this);
        expandedPanelFragment = new ExpandedPanelFragment();
        mainFragment = new MainFragment();

        //Add the main fragment
        if(fragmentManager.findFragmentByTag(MainFragment.TAG) == null){
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.tab_view, mainFragment, mainFragment.TAG);
            transaction.commit();
        }


        //Configure everything
        configureViews();

        configureFABReveal(fabRevealLayout);

        configurePlayerPanel();

        configureAlbumScroll();


        mMusicManager = new MusicManager(getContentResolver());
        (new PrepareMusicRetrieverTask(mMusicManager, this, Enums.MusicQueryType.SONGS)).execute();
        (new PrepareMusicRetrieverTask(mMusicManager, this, Enums.MusicQueryType.ALBUMS)).execute();
        (new PrepareMusicRetrieverTask(mMusicManager, this, Enums.MusicQueryType.ARTISTS)).execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setMusicServiceLock();
        //Request song update, in case a new song started playing while the activity was paused
        Intent serviceIntent = Utilities.convertImplicitIntentToExplicitIntent(new Intent(MusicService.ACTION_REQUEST_SONG_UPDATE), getApplicationContext());
        startService(serviceIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setMusicServiceUnLock();
    }

    //Initialize all the views
    private void configureViews() {
        btnPlayerPlay = (ImageButton) findViewById(R.id.player_button_play);
        btnPlayerPlay.setOnClickListener(this);
        btnPlayerSkipPrevious = (ImageButton) findViewById(R.id.player_button_skip_previous);
        btnPlayerSkipPrevious.setOnClickListener(this);
        btnPlayerSkipNext = (ImageButton) findViewById(R.id.player_button_skip_next);
        btnPlayerSkipNext.setOnClickListener(this);

        btnGoToAlbum = (Button) findViewById(R.id.button_album);
        btnGoToAlbum.setOnClickListener(this);
        btnGoToArtist = (Button) findViewById(R.id.button_artist);
        btnGoToArtist.setOnClickListener(this);
        btnQueue = (Button) findViewById(R.id.button_queue);
        btnQueue.setOnClickListener(this);
        btnEqualizer = (Button) findViewById(R.id.button_equalizer);
        btnEqualizer.setOnClickListener(this);
        btnLyrics = (Button) findViewById(R.id.button_lyrics);
        btnLyrics.setOnClickListener(this);
        btnEditTags = (Button) findViewById(R.id.button_edit_tags);
        btnEditTags.setOnClickListener(this);

        tvPlayerSongDuration = (TextView) findViewById(R.id.player_song_duration);
        tvPlayerSongTitle = (TextView) findViewById(R.id.player_title_text);
        tvPlayerSongCurrentDuration = (TextView) findViewById(R.id.playere_current_duration);

        progressSlider = (SeekBar) findViewById(R.id.player_song_slider);

        progressSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(sliderHeld){
                    tvPlayerSongCurrentDuration.setText(Utilities.milliSecondsToTimer(Utilities.progressToTimer(progress, (int) currentSong.getDuration())));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //When user touches the progress bar, tell the service to stop sending progress updates
                Intent serviceIntent = Utilities.convertImplicitIntentToExplicitIntent(new Intent(MusicService.ACTION_STOP_UPDATING_DURATION), getApplicationContext());
                startService(serviceIntent);
                sliderHeld = true;

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //When user lets go of the progress bar, tell the service to rewind
                Intent serviceIntent = Utilities.convertImplicitIntentToExplicitIntent(new Intent(MusicService.ACTION_REWIND_TO), getApplicationContext());
                serviceIntent.putExtra("POSITION", Utilities.progressToTimer(progressSlider.getProgress(), (int) currentSong.getDuration()));
                startService(serviceIntent);
                sliderHeld = false;

            }
        });
    }

    //Configure FABReveal, also event listeners
    private void configureFABReveal(FABRevealLayout fabRevealLayout) {
        fabRevealLayout.setOnRevealChangeListener(new OnRevealChangeListener() {
            @Override
            public void onMainViewAppeared(FABRevealLayout fabRevealLayout, View mainView) {
                optionsRevealed = false;
            }

            @Override
            public void onSecondaryViewAppeared(final FABRevealLayout fabRevealLayout, View secondaryView) {
                optionsRevealed = true;
                optionLayout = fabRevealLayout;
            }
        });
    }

    //Configure  the sliding panel
    private void configurePlayerPanel(){
        playerLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        playerLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset < 0.005) {
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.pull_up_fragment, miniPlayerFragment, MiniPlayerFragment.TAG);
                    transaction.commit();
                } else {
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.pull_up_fragment, expandedPanelFragment, MiniPlayerFragment.TAG);
                    transaction.commit();
                }
            }

            @Override
            public void onPanelExpanded(View panel) {

            }

            @Override
            public void onPanelCollapsed(View panel) {
                if (optionsRevealed && optionLayout != null) {
                    optionLayout.revealMainView();
                    optionsRevealed = false;
                }
            }

            @Override
            public void onPanelAnchored(View panel) {
            }

            @Override
            public void onPanelHidden(View panel) {

            }
        });

        //Add the miniplayer fragment
        if(fragmentManager.findFragmentByTag(MiniPlayerFragment.TAG) == null){
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.pull_up_fragment, miniPlayerFragment, MiniPlayerFragment.TAG);
            transaction.commit();
        }

    }

    //Configure Album Scroll in the player view
    private void configureAlbumScroll(){

        albumArtPager = (ViewPager)findViewById(R.id.album_art_pager);
        albumArtPager.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        ParallaxPagerTransformer pt = new ParallaxPagerTransformer(R.id.album_art);
        pt.setBorder(20);
        albumArtPager.setPageTransformer(false, pt);

        albumArtParallaxAdapter = new AlbumArtParallaxAdapter(getSupportFragmentManager());
        albumArtParallaxAdapter.setPager(albumArtPager);

        Bundle bAlbum1 = new Bundle();
        bAlbum1.putInt("album_art", R.drawable.artwork);
        AlbumArtFragment fAlbum1 = new AlbumArtFragment();
        fAlbum1.setArguments(bAlbum1);

        Bundle bAlbum2 = new Bundle();
        bAlbum2.putInt("album_art", R.drawable.artwork);
        AlbumArtFragment fAlbum2 = new AlbumArtFragment();
        fAlbum2.setArguments(bAlbum2);

        Bundle bAlbum3 = new Bundle();
        bAlbum3.putInt("album_art", R.drawable.artwork);
        AlbumArtFragment fAlbum3 = new AlbumArtFragment();
        fAlbum3.setArguments(bAlbum3);

        albumArtParallaxAdapter.add(fAlbum1);
        albumArtParallaxAdapter.add(fAlbum2);
        albumArtParallaxAdapter.add(fAlbum3);
        albumArtPager.setAdapter(albumArtParallaxAdapter);
        albumArtPager.setCurrentItem(1);

        albumArtPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {


                Log.i(TAG, "Page selected: "+position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



    }

    public void playPressed(){
        if(!songPlaying){
            Intent serviceIntent = Utilities.convertImplicitIntentToExplicitIntent(new Intent(MusicService.ACTION_RESUME), getApplicationContext());
            startService(serviceIntent);
            btnPlayerPlay.setImageResource(R.drawable.btn_pause);
            miniPlayerFragment.setPauseIcon();
            songPlaying = true;
        }
        else {
            Intent serviceIntent = Utilities.convertImplicitIntentToExplicitIntent(new Intent(MusicService.ACTION_PAUSE), getApplicationContext());
            startService(serviceIntent);
            btnPlayerPlay.setImageResource(R.drawable.btn_play);
            miniPlayerFragment.setPlayIcon();
            songPlaying = false;
        }

    }

    public void skipPreviousPressed(){
        startService(Utilities.convertImplicitIntentToExplicitIntent(new Intent(MusicService.ACTION_SKIP_PREVIOUS), getApplicationContext()));
    }

    public void skipNextPressed(){
        startService(Utilities.convertImplicitIntentToExplicitIntent(new Intent(MusicService.ACTION_SKIP_NEXT), getApplicationContext()));
    }

    public void playNewSong(int position){
        Intent serviceIntent = Utilities.convertImplicitIntentToExplicitIntent(new Intent(MusicService.ACTION_PLAY), getApplicationContext());
        serviceIntent.putParcelableArrayListExtra("QUEUE", mMusicManager.mSongs);
        serviceIntent.putExtra("POSITION", position);
        startService(serviceIntent);
        setMusicServiceLock();
        btnPlayerPlay.setImageResource(R.drawable.btn_pause);
        miniPlayerFragment.setPauseIcon();
        songPlaying = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.demo, menu);
        MenuItem item = menu.findItem(R.id.action_toggle);
        if (playerLayout != null) {
            if (playerLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                item.setTitle("Test");
            } else {
                item.setTitle("Test2");
            }
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_toggle: {
                if (playerLayout != null) {
                    if (playerLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN) {
                        playerLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                        item.setTitle("Show");
                    } else {
                        playerLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        item.setTitle("Hide");
                    }
                }
                return true;
            }
            case R.id.action_anchor: {
                if (playerLayout != null) {
                    if (playerLayout.getAnchorPoint() == 1.0f) {
                        playerLayout.setAnchorPoint(0.7f);
                        playerLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                        item.setTitle("Test3");
                    } else {
                        playerLayout.setAnchorPoint(1.0f);
                        playerLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        item.setTitle("Test3");
                    }
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(optionsRevealed && optionLayout != null){
            optionLayout.revealMainView();
        }
        else if (playerLayout != null &&
                (playerLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || playerLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            playerLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.player_button_play:
                playPressed();
                break;
            case R.id.player_button_skip_previous:
                skipPreviousPressed();
                break;
            case R.id.player_button_skip_next:
                skipNextPressed();
                break;

            case R.id.mplayer_btn_play:
                playPressed();
                break;
            case R.id.mplayer_btn_skip_previous:
                skipPreviousPressed();
                break;
            case R.id.mplayer_btn_skip_next:
                skipNextPressed();
                break;

            case R.id.button_album:
                Toast.makeText(MainActivity.this, "Album Pressed", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_artist:
                Toast.makeText(MainActivity.this, "Artist Pressed", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_queue:
                Toast.makeText(MainActivity.this, "Queue Pressed", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_lyrics:
                Toast.makeText(MainActivity.this, "Lyrics Pressed", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_equalizer:
                Toast.makeText(MainActivity.this, "Equalizer Pressed", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_edit_tags:
                Toast.makeText(MainActivity.this, "Edit Tags Pressed", Toast.LENGTH_SHORT).show();
                break;

        }
    }

    @Override
    public void onSongSelected(int position) {
        playNewSong(position);
    }

    public void setSong(int position) {

        currentSong = mMusicManager.mSongs.get(position);
        tvPlayerSongTitle.setText(currentSong.getTitle());
        tvPlayerSongDuration.setText("/" + Utilities.milliSecondsToTimer(currentSong.getDuration()));
        miniPlayerFragment.setSong(currentSong);
        expandedPanelFragment.setSong(currentSong);

        Bitmap albumArt = mMusicManager.getAlbumArt(currentSong.getAlbum_id(), MainActivity.this);

        AlbumArtFragment fAlbum;

        if(albumArt != null){
            fAlbum = new AlbumArtFragment();
            fAlbum.setAlbumArt(albumArt);
        }
        else{
            Bundle bAlbum1 = new Bundle();
            bAlbum1.putInt("album_art", R.drawable.artwork);
            fAlbum = new AlbumArtFragment();
            fAlbum.setArguments(bAlbum1);
        }


        albumArtParallaxAdapter.removeAll();

        albumArtParallaxAdapter.add(fAlbum);
        //albumArtPager.setCurrentItem(0);


    }

    public void setSongProgress(final long progress){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentDuration = progress;
                //Update the TextView
                tvPlayerSongCurrentDuration.setText(Utilities.milliSecondsToTimer(currentDuration));
                // Updating progress bar
                int position = Utilities.getProgressPercentage(currentDuration, currentSong.getDuration());
                progressSlider.setProgress(position);
                miniPlayerFragment.setSongProgress(position);
            }
        });

    }

    @Override
    public void onSongsQueryCompleted() {
        Log.i(TAG, "Music Retreaved. Size: " + mMusicManager.mSongs.size());
        if(fragmentManager.findFragmentByTag(MainFragment.TAG) != null && mainFragment.mPagerAdapter != null){
            TabSongsFragment songsFragment = (TabSongsFragment) mainFragment.mPagerAdapter.getItem(3);
            songsFragment.updateSongList(mMusicManager.mSongs);
        }

    }

    @Override
    public void onAlbumsQueryCompleted() {
        if(fragmentManager.findFragmentByTag(MainFragment.TAG) != null && mainFragment.mPagerAdapter != null){
            TabAlbumsFragment albumsFragment = (TabAlbumsFragment) mainFragment.mPagerAdapter.getItem(2);
            albumsFragment.setAlbums(mMusicManager.getAlbumsQuery());
        }

    }

    @Override
    public void onArtistsQueryCompleted() {
        Log.i(TAG, "Artist query completed. Size: " + mMusicManager.getArtistsQuery().size());
        //TabArtistsFragment artistsFragment = (TabArtistsFragment) mainFragment.mPagerAdapter.getItem(1);
        //artistsFragment.setArtists(mMusicManager.getArtistsQuery());
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void setMusicServiceLock(){
        if(isServiceRunning(MusicService.class)){
            synchronized (MusicService.CURRENTACTIVITYLOCK){
                MusicService.currentActivity = this;
            }
        }
    }

    public void setMusicServiceUnLock(){
        if(isServiceRunning(MusicService.class)){
            synchronized (MusicService.CURRENTACTIVITYLOCK){
                MusicService.currentActivity = null;
            }
        }
    }

    @Override
    public void albumClicked(int position) {

    }
}

