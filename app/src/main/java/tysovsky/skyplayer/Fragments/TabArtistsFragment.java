package tysovsky.skyplayer.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;

import tysovsky.skyplayer.Adapters.ArtistsAdapter;
import tysovsky.skyplayer.Artist;
import tysovsky.skyplayer.R;


public class TabArtistsFragment extends Fragment {

    String TAG = "TabArtistFragment";
    GridView artistsGrid;
    ArrayList<Artist> artists = new ArrayList<>();
    ArtistsAdapter artistsAdapter = null;


    public static TabArtistsFragment newInstance(){
        TabArtistsFragment fragment = new TabArtistsFragment();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "On create view");
        View v = inflater.inflate(R.layout.fragment_tab_artists, container, false);

        artistsGrid = (GridView)v.findViewById(R.id.tab_artists_grid);
        if(artistsAdapter != null){
            artistsGrid.setAdapter(artistsAdapter);
        }

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setArtists(ArrayList<Artist> artists) {
        this.artists = artists;
        if(artistsAdapter != null){
            artistsAdapter.notifyDataSetChanged();
            Log.i(TAG, "Updating adapter");
        }
        else if (artistsGrid != null){
            artistsAdapter = new ArtistsAdapter(getContext(), this.artists);
            artistsGrid.setAdapter(artistsAdapter);
            Log.i(TAG, "Creating adapter");
        }
    }
}
