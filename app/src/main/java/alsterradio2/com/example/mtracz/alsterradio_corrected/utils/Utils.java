package alsterradio2.com.example.mtracz.alsterradio_corrected.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import alsterradio2.com.example.mtracz.alsterradio_corrected.MediaPlayerProperties;
import alsterradio2.com.example.mtracz.alsterradio_corrected.MediaPlayerService;
import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.Constans;

/**
 * Created by MTRACZ on 01.Apr.2016.
 */
public class Utils {

    private static Intent createApproriateIntent(Context context, String action)
    {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(action);
        return intent;
    }

    public static void restartPlaying(Context context)
    {
        stopPlaying(context);
        startPlaying(context);
    }

    public static void increaseNumberOfClicks()
    {
        MediaPlayerProperties.getInstance().setNumberOfClicks(getNumberOfClicks()+1);
    }

    public static int getNumberOfClicks()
    {
        return MediaPlayerProperties.getInstance().getNumberOfClicks();
    }

    public static void startPlaying(Context context)
    {
        context.startService(createApproriateIntent(context, Constans.ACTION_START));
    }

    public static void stopPlaying(Context context)
    {
        context.startService(createApproriateIntent(context, Constans.ACTION_STOP));
    }

    public static void updateNotification(Context context)
    {
        context.startService(createApproriateIntent(context, Constans.ACTION_UPDATE_NOTIFICATION));
    }

    private static String getStringFromPersistence(String stringKey, String whatIfNotFound, Context context){
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getString(stringKey, whatIfNotFound);
    }

    public static String getActuallySelectedStreamURL(Context context){
        return getStringFromPersistence(Constans.SELECTED_STREAM, "error", context);
    }

    public static String getActuallyPlayingSong(Context context) {
        return getStringFromPersistence(Constans.actuallyPlayingSongKey, "", context);
    }

    public static String getActuallySelectedStreamName(Context context) {
        return getStringFromPersistence(Constans.SELECTED_STREAM_NAME, "", context);
    }
}
