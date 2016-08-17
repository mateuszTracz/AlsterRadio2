package alsterradio2.com.example.mtracz.alsterradio_corrected;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import alsterradio2.com.example.mtracz.alsterradio_corrected.database.DatabaseDAO;
import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.Bytes;
import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.BytesMapper;
import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.Constans;
import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.Song;
import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.TimeMapper;
import alsterradio2.com.example.mtracz.alsterradio_corrected.utils.Utils;

import static alsterradio2.com.example.mtracz.alsterradio_corrected.ManageNetworkUse.getSummaryBytesCount;


public class MainActivity extends Activity {

    private static Button buttonPlay;
    private int requestCodeForSettingsActivity = 0;
    private int frequencyOfRefreshingDatabase;
    private static TextView textViewUsedNetworkData;
    private static TextView textViewSummaryReveivedData;
    private static TextView textViewTimeFromStart;

    private PackageManager pManager;

    private long bytesOnStartApplication, bytesUsedAtTheMoment;
    
    private static Handler dataHandler;
    private static Runnable dataRunnable;
    private long summaryBytesCount;
    private long summaryTime;

    private static Handler timeHandler;
    private static Runnable timeRunnable;

    private static Handler refreshingDatabaseHandler;
    private static Runnable refreshingDatabaseRunnable;

    private static Handler updatingMainNotificationHandler;
    private static Runnable updatingMainNotificationRunnable;
    private int frequencyOfUpdatingMainNotification; //seconds

    private DatabaseDAO databaseDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("lifecycle", "onCreate");

        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, true);

        buttonPlay = (Button) findViewById(R.id.buttonPlay);
        textViewUsedNetworkData = (TextView) findViewById(R.id.textViewUsedNetworkData);
        textViewSummaryReveivedData = (TextView) findViewById(R.id.textViewSummaryReceivedData);
        textViewTimeFromStart = (TextView) findViewById(R.id.textViewSummaryTime);



        if(savedInstanceState != null) {
            bytesOnStartApplication = savedInstanceState.getLong("bytesOnStartApplication");
            textViewUsedNetworkData.setText(savedInstanceState.getString("alreadyUsedData"));
            summaryTime = savedInstanceState.getLong("summaryTime");
        }

        databaseDao = new DatabaseDAO(this);

        pManager = this.getPackageManager();

        if(dataHandler == null)
            initializeDataHandler();

        if (refreshingDatabaseHandler == null)
            initializeDatabaseRefreshingHandler();

        if(timeHandler == null) {
            initializeTimeHandler();
        }

        if(updatingMainNotificationHandler == null)
            initializeUpdatingMainNotificationHandler();

        if(Utils.getNumberOfClicks() == 0)
        {
            Intent intentForInitializePlayingService = new Intent(this, MediaPlayerService.class);
            intentForInitializePlayingService.setAction("");
            startService(intentForInitializePlayingService);


            Log.d("onCreate", "startService");
            if(!isPlaying()) {
               Log.d("onCreate", "buttonPlay.click");
               buttonPlay.performClick();
            }
        }

        bytesOnStartApplication = getSummaryBytesCount(pManager);


        if (databaseDao.getAllBytesCount() != 0) {
            summaryBytesCount = databaseDao.getLastBytesEntry().getLatestBytesCount();
        } else {
            summaryBytesCount = 0;
        }

        textViewTimeFromStart.setText(TimeMapper.getTimeFormatted(summaryTime));

        synchronizeValuesFromSettings();

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sh.edit();
        Set<String> set = sh.getStringSet(Constans.ALL_STREAM_SET, new HashSet<String>());
        if(set.size() == 0)
        {
            set.add("(" + Constans.alsterRadioStream128+")AlsterRadio");
            editor.putStringSet(Constans.ALL_STREAM_SET, set);
            editor.putString(Constans.SELECTED_STREAM, Constans.alsterRadioStream128);
            editor.putString(Constans.SELECTED_STREAM_NAME, "AlsterRadio");
            editor.commit();
        }
    }

    private void synchronizeValuesFromSettings() {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        frequencyOfRefreshingDatabase = Integer.parseInt(sh.getString("refreshingDatabaseFrequency", "1"));
        frequencyOfUpdatingMainNotification = Integer.parseInt(sh.getString("updatingMainNotificationFrequency", "30"));
    }

    private void initializeDatabaseRefreshingHandler() {
        refreshingDatabaseHandler = new Handler();
        refreshingDatabaseRunnable = new Runnable() {
            @Override
            public void run() {
                databaseDao.insertBytes(new Bytes(0,summaryBytesCount + bytesUsedAtTheMoment, Calendar.getInstance().getTimeInMillis(), "handler"));
                Log.d("refreshingDatabaseFrequency", "inserted");
                refreshingDatabaseHandler.postDelayed(this,frequencyOfRefreshingDatabase*60*1000);
            }
        };
    }


    private void initializeTimeHandler() {
        timeHandler = new Handler();
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                textViewTimeFromStart.setText(TimeMapper.getTimeFormatted(summaryTime));
                timeHandler.postDelayed(this, 1000);
                summaryTime++;
            }
        };
    }

    private void initializeUpdatingMainNotificationHandler(){
        updatingMainNotificationHandler = new Handler();
        updatingMainNotificationRunnable = new Runnable() {
            @Override
            public void run() {
                new GetMetadataTask(getApplicationContext(), Constans.MetadataCalledBy.handler).execute(Utils.getActuallySelectedStreamURL(getApplicationContext()));
                updatingMainNotificationHandler.postDelayed(this, frequencyOfUpdatingMainNotification*1000);
            }
        };
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(Constans.keyToRecognizeAction).equals(Constans.handleButtonPlaying)) {
                Log.d("hamdleHamdlersState", "callback from notification");
                synchronizeButtonPlay();
                handleHandlersState(intent);
            }
            if (intent.getStringExtra(Constans.keyToRecognizeAction).equals(Constans.STORE_IN_DATABASE))
            {
                insertApproriateBytesToDatabase(intent);
            }
            if(intent.getStringExtra(Constans.keyToRecognizeAction).equals(Constans.CHANGE_BUTTON_PLAY_STATE))
            {
                enableOrDisableButtonPlay(intent);
            }
        }

    };

    private BroadcastReceiver receiverFromMetadataTask = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getBooleanExtra(Constans.addSongToFavourite, false) == true)
            {
                String timestamp = String.valueOf(Calendar.getInstance().getTimeInMillis());
                String song = Utils.getActuallyPlayingSong(getApplicationContext());
                String streamName = Utils.getActuallySelectedStreamName(getApplicationContext());
                Song x = new Song(0, timestamp, streamName, song);
                databaseDao.insertSong(x);
                Toast.makeText(getApplicationContext(), "Added " + song + " to favourites", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void handleHandlersState(Intent intent) {
            if(intent.getStringExtra(Constans.broadCastKey).equals(Constans.ACTION_START)) {
                if(MediaPlayerProperties.getInstance().isTimerStarted()) {
                    timeHandler.removeCallbacks(timeRunnable);
                    MediaPlayerProperties.getInstance().setTimerStarted(false);
                    Log.d("handleHandlersState", Constans.ACTION_START);
                }

            }
            else {
                if(!MediaPlayerProperties.getInstance().isTimerStarted()) {
                    timeHandler.postDelayed(timeRunnable, 1000);
                    MediaPlayerProperties.getInstance().setTimerStarted(true);
                    Log.d("handleHandlersState", Constans.ACTION_STOP);
                }

            }
    }

    private void enableOrDisableButtonPlay(Intent intent) {
        if(intent.getStringExtra(Constans.CHANGE_BUTTON_PLAY_STATE).equals(Constans.DISABLE)){
            buttonPlay.setEnabled(false);
        }
        else {
            buttonPlay.setEnabled(true);
        }
    }


    private void insertApproriateBytesToDatabase(Intent intent)
    {
        if(intent.getStringExtra(Constans.STORE_IN_DATABASE).equals(Constans.ACTION_START)){
            Bytes bytes = new Bytes(0, summaryBytesCount + bytesUsedAtTheMoment, Calendar.getInstance().getTimeInMillis(), Constans.ACTION_START);
            databaseDao.insertBytes(bytes);
        }
        else
            if(intent.getStringExtra(Constans.STORE_IN_DATABASE).equals(Constans.ACTION_STOP)){
                Bytes bytes = new Bytes(0, summaryBytesCount + bytesUsedAtTheMoment, Calendar.getInstance().getTimeInMillis(), Constans.ACTION_STOP);
                databaseDao.insertBytes(bytes);
            }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean
    onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.preferences.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, Settings.class);
            startActivityForResult(i, requestCodeForSettingsActivity);
            return true;
        }
        if(id == R.id.statistics)
        {
            //TODO new dialog class
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.statistics_dialog);
            dialog.setTitle("Statistics (may be inacurate)");
            dialog.show();
            TextView textView1Hour = (TextView) dialog.findViewById(R.id.textViewOneHour);
            TextView textView24Hour = (TextView) dialog.findViewById(R.id.textView24Hours);
            TextView textView7Days = (TextView) dialog.findViewById(R.id.textView7Days);

            Button buttokOK = (Button) dialog.findViewById(R.id.statisticsButtonOK);

            textView1Hour.setText(BytesMapper.getUnderstableForm(databaseDao.getLastBytesEntry().getLatestBytesCount() - Long.parseLong(databaseDao.getValuesFrom(String.valueOf(60*60*1000)))));
            textView24Hour.setText(BytesMapper.getUnderstableForm(databaseDao.getLastBytesEntry().getLatestBytesCount() - Long.parseLong(databaseDao.getValuesFrom(String.valueOf(24*60*60*1000)))));
            textView7Days.setText(BytesMapper.getUnderstableForm(databaseDao.getLastBytesEntry().getLatestBytesCount() - Long.parseLong(databaseDao.getValuesFrom(String.valueOf(7*24*60*60*1000)))));

            buttokOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

        }
        if(id == R.id.addStream)
        {
            AddStreamDialog dialog = new AddStreamDialog();
            dialog.show(getFragmentManager(), "add stream");
        }
        if(id == R.id.changeStream)
        {
            ChangeStreamDialog dialog = new ChangeStreamDialog();
            dialog.setContext(getApplicationContext());
            dialog.show(getFragmentManager(), " change stream");
        }
        if(id == R.id.streamData)
        {
            new GetMetadataTask(getApplicationContext(), Constans.MetadataCalledBy.user).execute(Utils.getActuallySelectedStreamURL(getApplicationContext()));
        }
        if(id == R.id.closeApp)
        {
            Utils.stopPlaying(getApplicationContext());
            this.finish();
            System.exit(0);
        }
        if(id == R.id.showFavouriteSongs)
        {
            Intent startShowFavouriteSongsActivity = new Intent(this, ShowFavouriteSongsActivity.class);
            startActivity(startShowFavouriteSongsActivity);
        }
        return super.onOptionsItemSelected(item);
    }

    public void play(View view)
    {

        if (Utils.getNumberOfClicks() % 2 == 0)
            play();
        else
            stop();
    }

    public void addSongToFavourite(View view){
        new GetMetadataTask(getApplicationContext(), Constans.MetadataCalledBy.addToFavourites).execute(Utils.getActuallySelectedStreamURL(getApplicationContext()));

    }

    public void play()
    {
        if(isThereApproriateInternetConnection(getApproriateFlagToRecognizeInternetConnection()))
        {
            buttonPlay.setText("STOP");
            Utils.startPlaying(getApplicationContext());
            setIsPlaying(true);

            dataHandler.postDelayed(dataRunnable, 0);
            updatingMainNotificationHandler.postDelayed(updatingMainNotificationRunnable, frequencyOfUpdatingMainNotification*1000);

            if(!MediaPlayerProperties.getInstance().isTimerStarted()) {
                timeHandler.postDelayed(timeRunnable, 1000);
                MediaPlayerProperties.getInstance().setTimerStarted(true);
            }

          refreshingDatabaseHandler.postDelayed(refreshingDatabaseRunnable, frequencyOfRefreshingDatabase*60*1000);
        }
        else
        {
            Toast.makeText(getApplicationContext(), "There is no active internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void stop()
    {
        Utils.stopPlaying(getApplicationContext());
        buttonPlay.setText("PLAY");
        setIsPlaying(false);

        dataHandler.removeCallbacks(dataRunnable);
        updatingMainNotificationHandler.removeCallbacks(updatingMainNotificationRunnable);

        if(MediaPlayerProperties.getInstance().isTimerStarted()) {
            timeHandler.removeCallbacks(timeRunnable);
            MediaPlayerProperties.getInstance().setTimerStarted(false);
        }


        refreshingDatabaseHandler.removeCallbacks(refreshingDatabaseRunnable);
        //blinkingTimeHandler.postDelayed(blinkingTimeRunnable, 500);
    }

    private boolean isThereApproriateInternetConnection(ArrayList<Integer> flags) {
        boolean value = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        for (int i = 0; i < flags.size(); i++) {
            NetworkInfo info = connectivityManager.getNetworkInfo(flags.get(i));
            if (info != null && info.isConnected())
                value = true;
        }
        return value;
    }

    private ArrayList<Integer> getApproriateFlagToRecognizeInternetConnection() {
        ArrayList<Integer> flags = new ArrayList<>(1);
        String connectionType = getRequestedConnectionType();
        if (connectionType.contains("3G"))
            flags.add(ConnectivityManager.TYPE_MOBILE);
        else {
            if (connectionType.contains("WIFI")) flags.add(ConnectivityManager.TYPE_WIFI);
            else {
                flags.add(ConnectivityManager.TYPE_WIFI);
                flags.add(ConnectivityManager.TYPE_MOBILE);
            }
        }
        return flags;
    }

    private String getRequestedConnectionType() {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(this);

        return sh.getString("connectionType", "error");
    }

    private void changeButtonPlayState()
    {
        if(isPlaying())
        {
            buttonPlay.setText("PLAY");
            setIsPlaying(false);
        }
        else
        {
            buttonPlay.setText("STOP");
            setIsPlaying(true);
        }
        Utils.increaseNumberOfClicks();
    }

    public void onSaveInstanceState(Bundle bundle)
    {
        Log.d("onSaveInstanceState", "called");

        bundle.putString("alreadyUsedData", textViewUsedNetworkData.getText().toString());
        bundle.putLong("bytesOnStartApplication", bytesOnStartApplication);
        bundle.putLong("summaryTime", summaryTime);
    }

    private void initializeDataHandler() {
        dataHandler = new Handler();
        dataRunnable = new Runnable() {
            @Override
            public void run() {
                bytesUsedAtTheMoment = getSummaryBytesCount(pManager) - bytesOnStartApplication;
                textViewUsedNetworkData.setText(BytesMapper.getUnderstableForm(bytesUsedAtTheMoment));

                textViewSummaryReveivedData.setText(BytesMapper.getUnderstableForm(summaryBytesCount + bytesUsedAtTheMoment));
                dataHandler.postDelayed(this, 500);
            }
        };
    }

    public void onPause()
    {
        dataHandler.removeCallbacks(dataRunnable);
        Log.d("lifecycle", "onPause");
        super.onPause();
    }

    public void onResume()
    {
        Log.d("lifecycle", "onResume ");
        dataHandler.postDelayed(dataRunnable, 0);
        synchronizeButtonPlay();
        synchronizeValuesFromSettings();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("mediaPlayerService"));
        LocalBroadcastManager.getInstance(this).registerReceiver(receiverFromMetadataTask, new IntentFilter(Constans.intentFilterMetadata));

        super.onResume();
    }

    private void synchronizeButtonPlay() {
        if(Utils.getNumberOfClicks() % 2 != 0){
            buttonPlay.setText("STOP");
        }
        else{
            buttonPlay.setText("PLAY");
        }


        boolean shouldButtonPlayBeEnabled = !MediaPlayerProperties.getInstance().shouldButtonPlayBeDisabled();
        buttonPlay.setEnabled(shouldButtonPlayBeEnabled);
    }

    public void onStop()
    {
        databaseDao.insertBytes(new Bytes(1, summaryBytesCount + bytesUsedAtTheMoment, Calendar.getInstance().getTimeInMillis(), Constans.ACTION_STOP));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverFromMetadataTask);
        Log.d("lifecycle", "onStop");
        super.onStop();
    }

    public void onDestroy()
    {
        Log.d("lifecycle", "onDestroy");
        super.onDestroy();
    }

    private boolean isPlaying()
    {
        return MediaPlayerProperties.getInstance().isPlaying();
    }

    private void setIsPlaying(boolean isPlaying)
    {
        MediaPlayerProperties.getInstance().setIsPlaying(isPlaying);
    }


}
