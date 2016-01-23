package tysovsky.skyplayer.Fragments;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.rey.material.widget.Slider;

import tysovsky.skyplayer.MusicRetriever;
import tysovsky.skyplayer.R;
import tysovsky.skyplayer.Song;

/**
 *
 */
public class MiniPlayerFragment extends Fragment{

    public static String TAG = "MiniPlayerFragment";

    ImageButton btnMPlayerPlay;
    ImageButton btnMplayerSkipPrevious;
    ImageButton btnMPlayerSkipNext;

    TextView tvMPlayerSongTitle;

    ImageView imMPlayerAlbumArtwork;

    Slider progressSlider = null;

    Song currentSong = null;
    boolean playing = false;

    static Context context = null;

    float sliderPosition = -1;




    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment MiniPlayerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MiniPlayerFragment newInstance(final Context context_) {
        MiniPlayerFragment fragment = new MiniPlayerFragment();
        context = context_;
        return fragment;
    }

    public MiniPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.i(TAG, "OnAttach called");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "OnCreate called");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mini_player, container, false);

        configureViews(view);

        if(currentSong != null){
            tvMPlayerSongTitle.setText(currentSong.getTitle());
            imMPlayerAlbumArtwork.setImageBitmap(MusicRetriever.getAlbumArt(currentSong.getAlbum_id(), context));
        }

        Log.i(TAG, "OnViewCreate called");

        return view;
    }

    private void configureViews(View view){
        btnMPlayerPlay = (ImageButton)view.findViewById(R.id.mplayer_btn_play);
        btnMPlayerPlay.setOnClickListener((View.OnClickListener) getActivity());
        btnMplayerSkipPrevious = (ImageButton)view.findViewById(R.id.mplayer_btn_skip_previous);
        btnMplayerSkipPrevious.setOnClickListener((View.OnClickListener) getActivity());
        btnMPlayerSkipNext = (ImageButton)view.findViewById(R.id.mplayer_btn_skip_next);
        btnMPlayerSkipNext.setOnClickListener((View.OnClickListener) getActivity());

        progressSlider = (Slider)view.findViewById(R.id.mplayer_song_slider);
        if(sliderPosition > -1){
            progressSlider.setPosition(sliderPosition, true);
        }

        tvMPlayerSongTitle = (TextView)view.findViewById(R.id.mplayer_title_text);
        imMPlayerAlbumArtwork = (ImageView)view.findViewById(R.id.mplayer_album_artwok);

        if(playing){
            btnMPlayerPlay.setImageResource(R.drawable.btn_pause);
        }
    }



    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "OnPause called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "OnStop called");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "OnDestroyView called");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "OnDetach called");
    }

    public void setSong(Song song){
        if(tvMPlayerSongTitle != null){
            tvMPlayerSongTitle.setText(song.getTitle());
        }
        if(imMPlayerAlbumArtwork != null){
            imMPlayerAlbumArtwork.setImageBitmap(MusicRetriever.getAlbumArt(song.getAlbum_id(), context));
        }

        currentSong = song;

    }

    public void setPlayIcon(){
        btnMPlayerPlay.setImageResource(R.drawable.btn_play);
        playing = false;
    }

    public void setPauseIcon(){
        btnMPlayerPlay.setImageResource(R.drawable.btn_pause);
        playing = true;
    }

    public void setSongProgress(int position){
        Log.i(TAG, "" + position);
        if(progressSlider != null){
            progressSlider.setPosition(((float)position/1000), false);
        }
        else{
            sliderPosition = ((float)position/1000);
        }
    }

}
