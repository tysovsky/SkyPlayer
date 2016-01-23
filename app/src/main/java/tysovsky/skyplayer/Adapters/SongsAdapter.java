package tysovsky.skyplayer.Adapters;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tysovsky.skyplayer.Activities.MainActivity;
import tysovsky.skyplayer.MusicRetriever;
import tysovsky.skyplayer.R;
import tysovsky.skyplayer.Song;

/**
 * Created by tysovsky on 1/19/16.
 */
public class SongsAdapter extends ArrayAdapter<Song> {

    Context context;
    public SongsAdapter(Context context, List<Song> songs){
        super(context, 0, songs);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Song currentSong = getItem(position);

        TextView songTitle = null;
        TextView songSubTitle = null;
        ImageView songArt = null;
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_song,parent,false);

        }

        songTitle =  (TextView)convertView.findViewById(R.id.item_song_title);
        songSubTitle = (TextView)convertView.findViewById(R.id.item_song_subtitle);
        songArt = (ImageView)convertView.findViewById(R.id.item_song_artwork);

        final Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");

        Uri uri = ContentUris.withAppendedId(sArtworkUri, currentSong.getAlbum_id());


        Picasso.with(context)
                .load(uri)
                .resize(100, 100).centerCrop()
                .into(songArt);

        //songArt.setImageBitmap(MusicRetriever.getAlbumArt(currentSong.getAlbum_id(), getContext()));
        songTitle.setText(currentSong.getTitle());
        songSubTitle.setText(currentSong.getArtist() + " - " + currentSong.getAlbum());

        return convertView;
    }
}
