package tysovsky.skyplayer;

import android.app.Notification;
import android.content.Context;

/**
 * Created by tysovsky on 1/23/16.
 */
public class NotificationManager {
    private Context context;
    private Notification.Builder builder;

    public NotificationManager(Context context){
        this.context = context;
        this.builder = new Notification.Builder(context);
    }
}
