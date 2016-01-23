package tysovsky.skyplayer.Helpers;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import tysovsky.skyplayer.Activities.MainActivity;
import tysovsky.skyplayer.Interfaces.OnServiceActionListener;
import tysovsky.skyplayer.Song;

/**
 * Created by tysovsky on 1/19/16.
 */
public class MessageHandler extends Handler {
    OnServiceActionListener onServiceActionListener;

    public MessageHandler(MainActivity activity){
        this.onServiceActionListener = (OnServiceActionListener)activity;
    }
    @Override
    public void handleMessage(Message message) {

        switch (message.arg1){
            //Song changed
            case 0:
                Song song = (Song)message.obj;
                onServiceActionListener.onSongChanged(message.arg2);
                break;
            //Progress reported
            case 1:
                Log.i("Duration", Utilities.milliSecondsToTimer((long) message.arg2));
                long currentDuration = (long)message.arg2;
                onServiceActionListener.onSongProgressReported(currentDuration);
                break;
        }


        //Toast.makeText(context, song.getTitle(), Toast.LENGTH_SHORT).show();

    }
}