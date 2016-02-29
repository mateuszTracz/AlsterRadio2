package alsterradio2.com.example.mtracz.alsterradio_corrected;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.Constans;

/**
 * Created by MTRACZ on 22.Feb.2016.
 */
public class MusicIntentReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            Log.d("BroadcastReceiver", "Audio Becoming Noisy");
            Intent i = new Intent(context, MediaPlayerService.class);
            i.setAction(Constans.ACTION_STOP);
            context.startService(i);
        }

    }
}
