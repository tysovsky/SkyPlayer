package tysovsky.skyplayer.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import tysovsky.skyplayer.Activities.MainActivity;
import tysovsky.skyplayer.Adapters.SongsAdapter;
import tysovsky.skyplayer.Interfaces.OnMusicQueryCompletedListener;
import tysovsky.skyplayer.Interfaces.OnSongSelectedListener;
import tysovsky.skyplayer.R;
import tysovsky.skyplayer.Song;


public class TabSongsFragment extends Fragment {

    public static String TAG = "TabSongsFragment";
    ListView songsListView;
    SongsAdapter adapter;
    List<Song> songsList = new ArrayList<>();
    OnSongSelectedListener onSongSelectedListener = null;
    MainActivity mainActivity = null;
    OnMusicQueryCompletedListener onMusicQueryCompletedListener = null;


    public static TabSongsFragment newInstance(MainActivity activity){
        TabSongsFragment fragment = new TabSongsFragment();
        fragment.setActivity(activity);

        return fragment;
    }

    public TabSongsFragment(){

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onSongSelectedListener = (OnSongSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_songs, container, false);

        songsListView = (ListView)v.findViewById(R.id.tab_songs_list);
        adapter = new SongsAdapter(getContext(), songsList);
        songsListView.setAdapter(adapter);

        songsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onSongSelectedListener.onSongSelected(position);
            }
        });

        if(onMusicQueryCompletedListener != null){
            onMusicQueryCompletedListener.onSongsQueryCompleted();
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

    public void updateSongList(List<Song> songs){
        this.songsList = songs;
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }

    }

    public void setActivity(MainActivity activity){
        this.mainActivity = activity;
        this.onMusicQueryCompletedListener = (OnMusicQueryCompletedListener)mainActivity;
        //onMusicQueryCompletedListener.onSongsQueryCompleted();
    }


}
