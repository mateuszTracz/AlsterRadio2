package alsterradio2.com.example.mtracz.alsterradio_corrected;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.Constans;
import alsterradio2.com.example.mtracz.alsterradio_corrected.utils.Utils;

/**
 * Created by mtracz on 02.Feb.2016.
 */
public class MediaPlayerService extends Service{

    private MediaPlayer mMediaPlayer;
    private MediaSessionManager mManager;
    private MediaSession mSession;
    private MediaController mController;
    private AudioManager am;
    private WifiManager wifiManager;
    private WifiManager.WifiLock wifiLock;
    private String InfoErrorDebugString = "InfoErrorPersisteDebug";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(mMediaPlayer == null) {
            initMediaSessions();
        }
        else {
            if(intent.getAction() != null)
                handleIntent(intent);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void play() {
        sendBroadcastForDisablindButtonPlay();

        MediaPlayerProperties.getInstance().setIsPlaying(true);
        Utils.increaseNumberOfClicks();
        MediaPlayerProperties.getInstance().shouldButtonPlayBeDisabled(true);


        mMediaPlayer = setApproriateStreamSource(mMediaPlayer);
        mMediaPlayer.prepareAsync();

        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        int result = am.requestAudioFocus(amAudioFocus, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            wifiLock = wifiManager.createWifiLock("AlsterRadio");
            wifiLock.acquire();


            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();

                    Intent intentForEnablingButtonPlay = new Intent("mediaPlayerService");
                    intentForEnablingButtonPlay.putExtra(Constans.keyToRecognizeAction, Constans.CHANGE_BUTTON_PLAY_STATE);
                    intentForEnablingButtonPlay.putExtra(Constans.CHANGE_BUTTON_PLAY_STATE, Constans.ENABLE);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentForEnablingButtonPlay); // enable buuton Play
                    MediaPlayerProperties.getInstance().shouldButtonPlayBeDisabled(false);
                }
            });
            mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    String line = "Info: (" + String.valueOf(what) + ", " + String.valueOf(extra) + ") " + Calendar.getInstance().getTime().toString();
                    persistenceLine("infoSet", line);
                    ArrayList<String> arrayListLines = getPersistedLines("infoSet");
                    cancelNotification(997);
                    addNewErrorNotification(997, arrayListLines, "Infos:");

                    return false;
                }
            });
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    String line = "Error: (" + String.valueOf(what) + ", " + String.valueOf(extra) + ") " + Calendar.getInstance().getTime().toString();
                    persistenceLine("errorSet", line);
                    ArrayList<String> arrayListLines = getPersistedLines("errorSet");
                    addNewErrorNotification(998, arrayListLines, "Errors:");
                    Utils.stopPlaying(getApplicationContext());
                    return false;
                }
            });
        }
    }

    private void sendBroadcastForDisablindButtonPlay() {
        Intent intentForDisableButtonPlay = new Intent("mediaPlayerService");
        intentForDisableButtonPlay.putExtra(Constans.keyToRecognizeAction, Constans.CHANGE_BUTTON_PLAY_STATE); // change editability of button Play
        intentForDisableButtonPlay.putExtra(Constans.CHANGE_BUTTON_PLAY_STATE, Constans.DISABLE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentForDisableButtonPlay);
    }

    private void cancelNotification(int i) {
        NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificationManager.cancel(i);
    }

    private ArrayList<String> getPersistedLines(String whichSet) {
        ArrayList<String> result = new ArrayList<>(0);
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Set<String> stringSet = sh.getStringSet(whichSet, new HashSet<String>());
        Iterator it = stringSet.iterator();
        if(stringSet.size() <= 5)
        {
            while(it.hasNext())
            {
                result.add((String)it.next());
            }
        }
        else
        {
            int i = stringSet.size() - 5;
            while(i == 0)
            {
                it.next();
                i--;
            }
            while(it.hasNext())
            {
                result.add((String)it.next());
            }
        }
        return result;
    }

    private void persistenceLine(String whichSet, String line) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sh.edit();
        Set<String> stringSet = sh.getStringSet(whichSet, new HashSet<String>());
        editor.putStringSet(whichSet, stringSet);
        editor.commit();
    }

    private void addNewErrorNotification(int notificationId, ArrayList<String> lineList, String action) {
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

        style.setBigContentTitle(action);
        for(int i = 0; i < lineList.size(); i++)
        {
            style.addLine(lineList.get(i));
        }
        style.setSummaryText("Amount of elements: " + String.valueOf(lineList.size()));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(action + " from AlsterRadio");
        builder.setContentText("Expand to see details");
        builder.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.alster_radio_128));
        builder.setSmallIcon(R.drawable.ic_info_black_24dp);
        builder.setStyle(style);




        NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificationManager.notify(notificationId, builder.build() );
    }

    public void stop(){
        MediaPlayerProperties.getInstance().setIsPlaying(false);
        Utils.increaseNumberOfClicks();
        mMediaPlayer.stop();
        mMediaPlayer.reset();

        wifiLock.release();
     }

    private void initMediaSessions()
    {
        mMediaPlayer = new MediaPlayer();

        mSession = new MediaSession(getApplicationContext(), "simple player session");
        mController =new MediaController(getApplicationContext(), mSession.getSessionToken());

        mSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                if(!mMediaPlayer.isPlaying()) {
                    play();
                    buildNotification(generateAction(android.R.drawable.ic_media_pause, Constans.ACTION_STOP, Constans.ACTION_STOP));
                }
            }

            @Override
            public void onStop() {
                super.onStop();
                if(mMediaPlayer.isPlaying()){
                    stop();
                    buildNotification( generateAction( android.R.drawable.ic_media_play, Constans.ACTION_START, Constans.ACTION_START ));
                }
            }
        });
    }

    private void handleIntent(Intent intent)
    {
        if(intent.getAction().contains(Constans.ACTION_START))
        {
            mController.getTransportControls().play();

            Intent intentForBroadcast = new Intent("mediaPlayerService");
            intentForBroadcast.putExtra(Constans.keyToRecognizeAction, Constans.STORE_IN_DATABASE);
            intentForBroadcast.putExtra(Constans.STORE_IN_DATABASE, Constans.ACTION_START);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intentForBroadcast);

        }
        if(intent.getAction().contains(Constans.ACTION_STOP))
        {
            mController.getTransportControls().stop();

            Intent intentForBroadcast = new Intent("mediaPlayerService");
            intentForBroadcast.putExtra(Constans.keyToRecognizeAction, Constans.STORE_IN_DATABASE);
            intentForBroadcast.putExtra(Constans.STORE_IN_DATABASE, Constans.ACTION_STOP);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intentForBroadcast);
        }
        if(intent.getAction().contains(Constans.ACTION_UPDATE_NOTIFICATION))
        {
            Notification.Action action;
            if(MediaPlayerProperties.getInstance().isPlaying())
                action = generateAction(R.drawable.ic_action_pause, Constans.ACTION_STOP, Constans.ACTION_STOP);
            else
                action= generateAction(R.drawable.ic_action_play, Constans.ACTION_START, Constans.ACTION_START);
            buildNotification(action);
        }
    }


    private String getRequestedStreamKind() {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sh.getString(Constans.SELECTED_STREAM, "error");
    }

    private MediaPlayer setApproriateStreamSource(MediaPlayer mediaPlayer)
    {
        String streamSelected = getRequestedStreamKind();
        Log.d("streamSelected", streamSelected);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(streamSelected);
        } catch (IOException e) {
            Log.d("Fatal error occured", e.toString());
        }
        return mediaPlayer;
    }

    private void buildNotification( Notification.Action action) {
        Notification.MediaStyle style = new Notification.MediaStyle();

        Intent intentForOpeningMainActivity = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntentForOpeningMainActivity = PendingIntent.getActivity(getApplicationContext(), 0, intentForOpeningMainActivity, 0);

        Notification.Builder builder = new Notification.Builder( this )
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.alster_radio_128))
                .setContentTitle(Utils.getActuallySelectedStreamName(getApplicationContext()))
                .setContentText( Utils.getActuallyPlayingSong(getApplicationContext()))
                .setStyle(style);



        builder.addAction(action);
        builder.setContentIntent(pendingIntentForOpeningMainActivity);

        Intent intentForBroadcast = new Intent("mediaPlayerService");
        intentForBroadcast.putExtra(Constans.keyToRecognizeAction, Constans.handleButtonPlaying);

        if(action.title.toString().contains(Constans.ACTION_START)) {
            builder.setSmallIcon(R.drawable.ic_play_circle_outline_white_24dp);
            intentForBroadcast.putExtra(Constans.broadCastKey, Constans.ACTION_START);
        }
        else {
            builder.setSmallIcon(R.drawable.ic_pause_circle_outline_white_24dp);
            intentForBroadcast.putExtra(Constans.broadCastKey, Constans.ACTION_STOP);
        }

        style.setShowActionsInCompactView(0);

        NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificationManager.notify( Constans.notificationId, builder.build() );

        LocalBroadcastManager.getInstance(this).sendBroadcast(intentForBroadcast); // For sending broadcast to MainActivity - to change button state


    }

    private Notification.Action generateAction( int icon, String title, String intentAction ) {
        Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
        intent.setAction( intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder( icon, title, pendingIntent ).build();
    }

    public void onDestroy()
    {
        NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificationManager.cancel(Constans.notificationId);
        Log.d("service", "onDestroy()");
        super.onDestroy();
    }


    AudioManager.OnAudioFocusChangeListener amAudioFocus = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
            {
                Utils.stopPlaying(getApplicationContext());
            }
            else if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
            {
                mMediaPlayer.setVolume(1f, 1f);
                Utils.startPlaying(getApplicationContext());

            }
            else if (focusChange == AudioManager.AUDIOFOCUS_LOSS)
            {
                am.abandonAudioFocus(amAudioFocus);
                Utils.stopPlaying(getApplicationContext());
            }
            else if(focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
            {
                mMediaPlayer.setVolume(0.1f, 0.1f);
            }

        }
    };

}
