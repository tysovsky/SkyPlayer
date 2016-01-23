package tysovsky.skyplayer;

import android.os.AsyncTask;

import tysovsky.skyplayer.Helpers.Enums;
import tysovsky.skyplayer.Interfaces.OnMusicQueryCompletedListener;

import static tysovsky.skyplayer.Helpers.Enums.*;
import static tysovsky.skyplayer.Helpers.Enums.MusicQueryType.SONGS;

/**
 * Asynchronous task that prepares a MusicRetriever. This asynchronous task essentially calls
 * {@link MusicRetriever#prepare()} on a {@link MusicRetriever}, which may take some time to
 * run. Upon finishing, it notifies the indicated {@MusicRetrieverPreparedListener}.
 */
public class PrepareMusicRetrieverTask extends AsyncTask<Void, Void, Void> {
    MusicManager musicManager;
    OnMusicQueryCompletedListener mListener;
    MusicQueryType queryType;
    public PrepareMusicRetrieverTask(MusicManager retriever,
                                     OnMusicQueryCompletedListener listener, MusicQueryType queryType) {
        musicManager = retriever;
        mListener = listener;
        this.queryType = queryType;
    }
    @Override
    protected Void doInBackground(Void... arg0) {
        switch (queryType){
            case SONGS:
                musicManager.prepare();
                break;
            case ALBUMS:
                musicManager.queryAlbums();
                break;
            case ARTISTS:
                musicManager.queryArtists();
                break;
        }

        return null;
    }
    @Override
    protected void onPostExecute(Void result) {
        switch (queryType){
            case SONGS:
                mListener.onSongsQueryCompleted();
                break;
            case ALBUMS:
                mListener.onAlbumsQueryCompleted();
                break;
            case ARTISTS:
                mListener.onArtistsQueryCompleted();
                break;
        }
    }
}