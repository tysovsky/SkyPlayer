package tysovsky.skyplayer.Interfaces;

import tysovsky.skyplayer.Song;

/**
 * Created by tysovsky on 1/19/16.
 */
public interface OnServiceActionListener {
    public void onSongChanged(int position);
    public void onSongProgressReported(long progress);
}
