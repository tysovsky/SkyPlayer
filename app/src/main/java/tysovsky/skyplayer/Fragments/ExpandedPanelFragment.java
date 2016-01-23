package tysovsky.skyplayer.Fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tysovsky.skyplayer.R;
import tysovsky.skyplayer.Song;


/**
 * A simple {@link Fragment} subclass.
 * 
 */
public class ExpandedPanelFragment extends Fragment {

    public static String TAG = "ExpandedPanelFragment";
    TextView tvPanelAlbum;
    TextView tvPanelArtist;

    Song currentSong = null;


    public static ExpandedPanelFragment newInstance(String param1, String param2) {
        ExpandedPanelFragment fragment = new ExpandedPanelFragment();
        return fragment;
    }

    public ExpandedPanelFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_expanded_panel, container, false);

        configureViews(v);

        if(currentSong != null){
            tvPanelAlbum.setText(currentSong.getAlbum());
            tvPanelArtist.setText(currentSong.getArtist());
        }

        return v;
    }

    private void configureViews(View view){
        tvPanelAlbum = (TextView)view.findViewById(R.id.panel_album_text);
        tvPanelArtist = (TextView)view.findViewById(R.id.panel_author_text);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setSong(Song song){
        if(tvPanelArtist != null){
            tvPanelAlbum.setText(song.getAlbum());
            tvPanelArtist.setText(song.getArtist());
        }
        currentSong = song;

    }

}
