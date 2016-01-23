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
import android.provider.MediaStore.Audio.Albums;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by tysovsky on 1/21/16.
 */
public class MusicManager {
    final String TAG = "MusicManager";
    ContentResolver mContentResolver;

    // the items (songs) we have queried
    public ArrayList<Song> mSongs = new ArrayList<Song>();

    public ArrayList<Song> currentQuery = new ArrayList<>();
    public ArrayList<Song> songsQuery = new ArrayList<>();
    public ArrayList<Artist> artistsQuery = new ArrayList<>();
    public ArrayList<Album> albumsQuery = new ArrayList<>();

    private int index = 0;

    Random mRandom = new Random();
    public MusicManager(ContentResolver cr) {
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
        String sortOrder = MediaStore.Video.Media.TITLE + " ASC";
        Cursor cur = mContentResolver.query(uri, null,
                MediaStore.Audio.Media.IS_MUSIC + " = 1", null, sortOrder);
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
            //Log.i(TAG, "ID: " + cur.getString(idColumn) + " Title: " + cur.getString(titleColumn));
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

    public void querySongs(){
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Log.i(TAG, "Querying songs...");
        Log.i(TAG, "URI: " + uri.toString());
        // Perform a query on the content resolver. The URI we're passing specifies that we
        // want to query for all audio media on external storage (e.g. SD card)
        //We want to sort by title in ascending order
        String sortOrder = MediaStore.Video.Media.TITLE + " ASC";
        Cursor cur = mContentResolver.query(uri, null,
                MediaStore.Audio.Media.IS_MUSIC + " = 1", null, sortOrder);
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
            //Log.i(TAG, "ID: " + cur.getString(idColumn) + " Title: " + cur.getString(titleColumn));
            songsQuery.add(new Song(
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
        Log.i(TAG, "Done querying songs. MusicRetriever is ready.");
    }

    public void queryAlbums(){
        albumsQuery = new ArrayList<>();
        String[] projection = new String[] {Albums._ID, Albums.ALBUM, Albums.ARTIST,  Albums.ALBUM_ART, Albums.NUMBER_OF_SONGS};
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = MediaStore.Video.Media.ALBUM + " ASC";
        Cursor cur = mContentResolver.query(Albums.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
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
        int artistColumn = cur.getColumnIndex(Albums.ARTIST);
        int albumColumn = cur.getColumnIndex(Albums.ALBUM);
        int idColumn = cur.getColumnIndex(Albums._ID);
        do {
            //Log.i(TAG, "ID: " + cur.getString(idColumn) + " Title: " + cur.getString(titleColumn));
            albumsQuery.add(new Album(
                    cur.getLong(idColumn),
                    cur.getString(albumColumn),
                    cur.getString(artistColumn),
                    0));
        } while (cur.moveToNext());
    }

    public void queryArtists(){
        artistsQuery = new ArrayList<>();
        String[] projection = {MediaStore.Audio.Artists._ID, MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.Artists.NUMBER_OF_ALBUMS, MediaStore.Audio.Artists.NUMBER_OF_TRACKS };
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = MediaStore.Video.Media.ARTIST + " ASC";
        Cursor cur = mContentResolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
        Log.i(TAG, "Artist Query finished. " + (cur == null ? "Returned NULL." : "Returned a cursor."));
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
        int artistColumn = cur.getColumnIndex(MediaStore.Audio.Artists.ARTIST);
        int numAlbumsColumn = cur.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
        int numTracksColumn = cur.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
        int idColumn = cur.getColumnIndex(MediaStore.Audio.Artists._ID);
        do {
            //Log.i(TAG, "ID: " + cur.getString(idColumn) + " Title: " + cur.getString(titleColumn));
            artistsQuery.add(new Artist(
                    cur.getLong(idColumn),
                    cur.getString(artistColumn),
                    cur.getInt(numAlbumsColumn),
                    cur.getInt(numTracksColumn)));
        } while (cur.moveToNext());

    }

    public void querySongByAlbum(){

    }

    public void querySongsByArtist(){

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

    public static Uri getAlbumArtUri(long album_id){
        final Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");

        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

        return uri;
    }

    public ArrayList<Song> getCurrentQuery() {
        return currentQuery;
    }

    public ArrayList<Album> getAlbumsQuery() {
        return albumsQuery;
    }

    public ArrayList<Artist> getArtistsQuery() {
        return artistsQuery;
    }

    public ArrayList<Song> getSongsQuery() {
        return songsQuery;
    }

    public ArrayList<Song> getmSongs() {
        return mSongs;
    }
}
