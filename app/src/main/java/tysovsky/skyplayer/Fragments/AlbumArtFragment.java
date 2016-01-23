package tysovsky.skyplayer.Fragments;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import tysovsky.skyplayer.Adapters.AlbumArtParallaxAdapter;
import tysovsky.skyplayer.R;


public class AlbumArtFragment extends Fragment {

    Bitmap albumBitmap = null;

    private AlbumArtParallaxAdapter mAdapter;
    ImageView image;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_art, container, false);

        final ImageView image = (ImageView)view.findViewById(R.id.album_art);
        if(albumBitmap == null ){
            if(getArguments() != null){
                image.setImageResource(getArguments().getInt("album_art"));
            }

        } else {
            image.setImageBitmap(albumBitmap);
        }


        image.post(new Runnable() {
            @Override
            public void run() {
                Matrix matrix = new Matrix();
                matrix.reset();

                float wv = image.getWidth();
                float hv = image.getHeight();

                float wi = image.getDrawable().getIntrinsicWidth();
                float hi = image.getDrawable().getIntrinsicHeight();

                float width = wv;
                float height = hv;

                if (wi / wv > hi / hv) {
                    matrix.setScale(hv / hi, hv / hi);
                    width = wi * hv / hi;
                } else {
                    matrix.setScale(wv / wi, wv / wi);
                    height = hi * wv / wi;
                }

                matrix.preTranslate((wv - width) / 2, (hv - height) / 2);
                image.setScaleType(ImageView.ScaleType.MATRIX);
                image.setImageMatrix(matrix);
            }
        });

        return view;
    }

    public void setAdapter(AlbumArtParallaxAdapter adapter){
        mAdapter = adapter;
    }

    public void setAlbumArt(Bitmap albumArt){
        this.albumBitmap = albumArt;
    }

}
