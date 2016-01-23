package tysovsky.skyplayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 * Retrieves and organizes media to play. Before being used, you must call {@link #prepare()},
 * which will retrieve all of the music on the user's device (by performing a query on a content
 * resolver). After that, it's ready to retrieve a random song, with its title and URI, upon
 * request.
 */
public class MusicRetriever {
    final String TAG = "MusicRetriever";
    ContentResolver mContentResolver;

    // the items (songs) we have queried
    public ArrayList<Song> mSongs = new ArrayList<Song>();

    private int index = 0;

    Random mRandom = new Random();
    public MusicRetriever(ContentResolver cr) {
        mContentResolver = cr;
    }
    /**
     * Loads music data. This method may take long, so be sure to call it asynchronously without
     * blocking the main thread.
     */
    public void prepare() {
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Log.i(TAG, "Querying media...");
        Log.i(TAG, "URI: " + uri.toString());
        // Perform a query on the content resolver. The URI we're passing specifies that we
        // want to query for all audio media on external storage (e.g. SD card)
        Cursor cur = mContentResolver.query(uri, null,
                MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null);
        Log.i(TAG, "Query finished. " + (cur == null ? "Returned NULL." : "Returned a cursor."));
        if (cur == null) {
            // Query failed...
            Log.e(TAG, "Failed to retrieve music: cursor is null :-(");
            return;
        }
        if (!cur.moveToFirst()) {
            // Nothing to query. There is no music on the device. How boring.
            Log.e(TAG, "Failed to move cursor to first row (no query results).");
            return;
        }
        Log.i(TAG, "Listing...");
        // retrieve the indices of the columns where the ID, title, etc. of the song are
        int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int artistIdColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID);
        int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int albumIdColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
        int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
        int idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID);
        int trackColumn = cur.getColumnIndex(MediaStore.Audio.Media.TRACK);
        int yearColumn = cur.getColumnIndex(MediaStore.Audio.Media.YEAR);

        Log.i(TAG, "Title column index: " + String.valueOf(titleColumn));
        Log.i(TAG, "ID column index: " + String.valueOf(titleColumn));
        // add each song to mItems
        do {
            Log.i(TAG, "ID: " + cur.getString(idColumn) + " Title: " + cur.getString(titleColumn));
            mSongs.add(new Song(
                    cur.getLong(idColumn),
                    cur.getString(artistColumn),
                    cur.getLong(artistIdColumn),
                    cur.getString(titleColumn),
                    cur.getString(albumColumn),
                    cur.getLong(albumIdColumn),
                    cur.getLong(durationColumn),
                    cur.getInt(trackColumn),
                    cur.getInt(yearColumn)));
        } while (cur.moveToNext());
        Log.i(TAG, "Done querying media. MusicRetriever is ready.");
    }
    public ContentResolver getContentResolver() {
        return mContentResolver;
    }

    public static Bitmap getAlbumArt(Long album_id, Context context)
    {
        Bitmap bm = null;
        try
        {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null)
            {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception e) {
        }
        return bm;
    }

    /** Returns a random Item. If there are no items available, returns null. */
    public Song getRandomSong() {
        if (mSongs.size() <= 0){
            return null;
        }
        return mSongs.get(mRandom.nextInt(mSongs.size()));
    }

    public Song getFirstSong(){
        index = 0;
        return mSongs.get(index);
    }

    public Song getPrevious(){
        if(index == 0){
            index = mSongs.size() -1;
        }
        else{
            index = index-1;
        }
        return mSongs.get(index);
    }

    public Song getNext(){
        if(index == mSongs.size()-1){
            index = 0;
        }
        else{
            index++;
        }

        return mSongs.get(index);
    }
}