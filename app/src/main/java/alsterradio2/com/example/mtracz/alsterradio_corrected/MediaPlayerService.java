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
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.Constans;

/**
 * Created by mtracz on 02.Feb.2016.
 */
public class MediaPlayerService extends Service{

    private MediaPlayer mMediaPlayer;
    private MediaSessionManager mManager;
    private MediaSession mSession;
    private MediaController mController;
    private AudioManager am;

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

    public void play()
    {
        mMediaPlayer = setApproriateStreamSource(mMediaPlayer);
        mMediaPlayer.prepareAsync();

        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        int result = am.requestAudioFocus(amAudioFocus, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        }
    }

    public void stop(){
        mMediaPlayer.stop();
        mMediaPlayer.reset();
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
        else if(intent.getAction().contains(Constans.ACTION_STOP))
        {
            mController.getTransportControls().stop();

            Intent intentForBroadcast = new Intent("mediaPlayerService");
            intentForBroadcast.putExtra(Constans.keyToRecognizeAction, Constans.STORE_IN_DATABASE);
            intentForBroadcast.putExtra(Constans.STORE_IN_DATABASE, Constans.ACTION_STOP);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intentForBroadcast);
        }
    }


    private String getRequestedStreamKind() {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sh.getString("streamQuality", "error");
    }

    private MediaPlayer setApproriateStreamSource(MediaPlayer mediaPlayer)
    {
        String streamQuality = getRequestedStreamKind();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if(streamQuality.equals("64kbps"))
        {
            try {
                mediaPlayer.setDataSource(Constans.alsterRadioStream64);
            } catch (IOException e) {
                Log.d("A fatal error occured.", e.toString());
            }
        }
        else if(streamQuality.equals("128kbps") || streamQuality.equals("error"))
        {
            try {
                mediaPlayer.setDataSource(Constans.alsterRadioStream128);
            } catch (IOException e) {
                Log.d("A fatal error occured.", e.toString());
            }
        }
        return mediaPlayer;
    }

    private void buildNotification( Notification.Action action) {
        Notification.MediaStyle style = new Notification.MediaStyle();

        Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );

        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        Notification.Builder builder = new Notification.Builder( this )
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.alster_radio_128))
                .setContentTitle("AlsterRadio")
                .setContentText( "Hamburg" )
                .setDeleteIntent( pendingIntent )
                .setStyle(style);

        builder.addAction( action );

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
                startService(createAproriateIntent(Constans.ACTION_STOP));
            }
            else if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
            {
                mMediaPlayer.setVolume(1f, 1f);
                startService(createAproriateIntent(Constans.ACTION_START));

            }
            else if (focusChange == AudioManager.AUDIOFOCUS_LOSS)
            {
                am.abandonAudioFocus(amAudioFocus);
                startService(createAproriateIntent(Constans.ACTION_STOP));
            }
            else if(focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
            {
                mMediaPlayer.setVolume(0.1f, 0.1f);
            }

        }
    };

    private Intent createAproriateIntent(String action)
    {
        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.setAction(action);
        return intent;
    }

}
