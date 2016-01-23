package tysovsky.skyplayer;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tysovsky on 1/18/16.
 */
public class Song implements Parcelable {
    long id;
    String artist;
    long artist_id;
    String title;
    String album;
    long album_id;
    long duration;
    int track;
    int year;
    public Song(long id, String artist, long artist_id, String title, String album, long album_id, long duration, int track, int year) {
        this.id = id;
        this.artist = artist;
        this.artist_id = artist_id;
        this.title = title;
        this.album = album;
        this.album_id = album_id;
        this.duration = duration;
        this.track = track;
        this.year = year;
    }

    protected Song(Parcel in) {
        id = in.readLong();
        artist = in.readString();
        artist_id = in.readLong();
        title = in.readString();
        album = in.readString();
        album_id = in.readLong();
        duration = in.readLong();
        track = in.readInt();
        year = in.readInt();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public long getId() {
        return id;
    }
    public String getArtist() {
        return artist;
    }
    public String getTitle() {
        return title;
    }
    public String getAlbum() {
        return album;
    }
    public long getDuration() {
        return duration;
    }
    public long getAlbum_id(){return album_id;}
    public Uri getURI() {
        return ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(artist);
        dest.writeLong(artist_id);
        dest.writeString(title);
        dest.writeString(album);
        dest.writeLong(album_id);
        dest.writeLong(duration);
        dest.writeInt(track);
        dest.writeInt(year);
    }
}
