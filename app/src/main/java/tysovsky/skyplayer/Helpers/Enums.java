package tysovsky.skyplayer.Helpers;

/**
 * Created by tysovsky on 1/19/16.
 */
public class Enums {

    public enum Tab {
        WELCOME("Welcome"),
        SONGS ("Songs"),
        ARTISTS ("Artists"),
        ALBUMS ("Albums");
        private final String name;

        private Tab(String s) {
            name = s;
        }

        public boolean equalsName(String otherName){
            return (otherName != null) && name.equals(otherName);
        }

        public String toString(){
            return name;
        }

    }

    public enum MusicQueryType{
        SONGS,
        ALBUMS,
        ARTISTS
    }
}


