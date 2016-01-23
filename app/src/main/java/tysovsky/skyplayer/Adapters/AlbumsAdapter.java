package tysovsky.skyplayer.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tysovsky.skyplayer.Activities.MainActivity;
import tysovsky.skyplayer.Album;
import tysovsky.skyplayer.Interfaces.OnAlbumClickedListener;
import tysovsky.skyplayer.Interfaces.OnSongSelectedListener;
import tysovsky.skyplayer.MusicManager;
import tysovsky.skyplayer.R;

/**
 * Created by tysovsky on 1/21/16.
 */
public class AlbumsAdapter extends BaseAdapter {

    ArrayList<Album> albums = new ArrayList<>();
    Context context = null;
    OnAlbumClickedListener onAlbumClickedListener = null;


    public AlbumsAdapter(Context context, ArrayList<Album> albums){
        this.context = context;
        onAlbumClickedListener = (OnAlbumClickedListener)context;
        this.albums = albums;
    }

    @Override
    public int getCount() {
        return albums.size();
    }

    @Override
    public Album getItem(int position) {
        return albums.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Album currentAlbum = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
        }

        ImageView albumArt = (ImageView)convertView.findViewById(R.id.item_album_art);
        TextView albumTitle = (TextView)convertView.findViewById(R.id.item_album_title);
        TextView albumArtist = (TextView)convertView.findViewById(R.id.item_album_artist);

        Picasso.with(context)
                .load(MusicManager.getAlbumArtUri(currentAlbum.getId()))
                .resize(300, 300).centerCrop()
                .into(albumArt);
        albumTitle.setText(currentAlbum.getAlbum());
        albumArtist.setText(currentAlbum.getArtist());


        return convertView;
    }
}
