package tysovsky.skyplayer;

/**
 * Created by tysovsky on 1/21/16.
 */
public class Album {
    long id;
    String artist;
    long artist_id;
    String album;
    long album_id;
    int year;

    public Album(long id, String album, String artist, int year){
        this.id = id;
        this.album = album;
        this.artist = artist;
        this.year = year;
    }

    public long getAlbum_id() {
        return album_id;
    }

    public String getAlbum() {
        return album;
    }

    public long getArtist_id() {
        return artist_id;
    }

    public String getArtist() {
        return artist;
    }

    public int getYear() {
        return year;
    }

    public long getId() {
        return id;
    }

    public void setAlbum_id(long album_id) {
        this.album_id = album_id;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setArtist_id(long artist_id) {
        this.artist_id = artist_id;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
