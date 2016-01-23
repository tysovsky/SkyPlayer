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

import tysovsky.skyplayer.Artist;
import tysovsky.skyplayer.R;

/**
 * Created by tysovsky on 1/21/16.
 */
public class ArtistsAdapter extends BaseAdapter {

    ArrayList<Artist> artists = new ArrayList<>();
    Context context = null;

    public ArtistsAdapter(Context context, ArrayList<Artist> artists){
        this.context = context;
        this.artists = artists;
    }

    @Override
    public int getCount() {
        return artists.size();
    }

    @Override
    public Artist getItem(int position) {
        return artists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Artist  currentArtist = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_artist, parent, false);
        }

        ImageView artistPicture = (ImageView)convertView.findViewById(R.id.item_artist_picture);
        TextView artistName = (TextView)convertView.findViewById(R.id.item_artist_name);

        Picasso.with(context)
                .load(R.drawable.artwork)
                .resize(300, 300).centerCrop()
                .into(artistPicture);

        artistName.setText(currentArtist.getName());

        return convertView;
    }
}
