package tysovsky.skyplayer.Fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;

import tysovsky.skyplayer.Activities.MainActivity;
import tysovsky.skyplayer.Adapters.AlbumsAdapter;
import tysovsky.skyplayer.Album;
import tysovsky.skyplayer.Interfaces.OnMusicQueryCompletedListener;
import tysovsky.skyplayer.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TabAlbumsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabAlbumsFragment extends Fragment {

    GridView albumsGrid;
    AlbumsAdapter albumsAdapter;
    ArrayList<Album> albums = null;
    MainActivity mainActivity = null;
    OnMusicQueryCompletedListener onMusicQueryCompletedListener = null;

    public static TabAlbumsFragment newInstance(MainActivity activity) {
        TabAlbumsFragment fragment = new TabAlbumsFragment();
        fragment.setActivity(activity);
        return fragment;
    }

    public TabAlbumsFragment() {
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
        View v = inflater.inflate(R.layout.fragment_tab_albums, container, false);
        albumsGrid = (GridView)v.findViewById(R.id.tab_albums_grid);
        albumsAdapter = new AlbumsAdapter(getContext(), albums);
        if(albums != null){
            albumsGrid.setAdapter(albumsAdapter);
        }

        if(onMusicQueryCompletedListener != null){
            onMusicQueryCompletedListener.onAlbumsQueryCompleted();
        }

        return v;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void setAlbums(ArrayList<Album> albums) {
        this.albums = albums;
        if(albumsAdapter != null){
            albumsAdapter.notifyDataSetChanged();
        }

    }

    public void setActivity(MainActivity activity){
        this.mainActivity = activity;
        this.onMusicQueryCompletedListener = (OnMusicQueryCompletedListener)mainActivity;
        //onMusicQueryCompletedListener.onSongsQueryCompleted();
    }
}
