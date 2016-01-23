package tysovsky.skyplayer;

/**
 * Created by tysovsky on 1/21/16.
 */
public class Artist {
    long id;
    String name;
    int numberOfAlbums;
    int numberOfTracks;

    public Artist(long id, String name, int numberOfAlbums, int numberOfTracks){
        this.id = id;
        this.name = name;
        this.numberOfAlbums = numberOfAlbums;
        this.numberOfTracks = numberOfTracks;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getNumberOfAlbums() {
        return numberOfAlbums;
    }

    public int getNumberOfTracks() {
        return numberOfTracks;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumberOfAlbums(int numberOfAlbums) {
        this.numberOfAlbums = numberOfAlbums;
    }

    public void setNumberOfTracks(int numberOfTracks) {
        this.numberOfTracks = numberOfTracks;
    }
}
