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

import alsterradio2.com.example.mtracz.alsterradio_corrected.database.DatabaseDAO;
import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.Bytes;
import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.BytesMapper;
import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.Constans;
import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.TimeMapper;

import static alsterradio2.com.example.mtracz.alsterradio_corrected.ManageNetworkUse.getSummaryBytesCount;


public class MainActivity extends Activity {

    private Button buttonPlay;
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

    private Handler refreshingDatabaseHandler;
    private Runnable refreshingDatabaseRunnable;

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

            if (isPlaying()) {
                buttonPlay.setText("STOP");
            } else {
                buttonPlay.setText("PLAY");
            }
        }

        databaseDao = new DatabaseDAO(this);

        pManager = this.getPackageManager();

        if(dataHandler == null)
            initializeDataHandler();

        initializeDatabaseRefreshingHandler();

        if(timeHandler == null)
            initializeTimeHandler();


        if(getNumberOfClicks() == 0)
        {
            startService(createApproriateIntent(""));
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

        getFrequencyOfRefreshingDatabase();


    }

    private void getFrequencyOfRefreshingDatabase() {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        frequencyOfRefreshingDatabase = Integer.parseInt(sh.getString("refreshingDatabaseFrequency", "1"));
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

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(Constans.keyToRecognizeAction).equals(Constans.handleButtonPlaying)) {
                handleButtonPlayingState(intent);
                Log.d("BroadcastReceiver", "handleButtonPlayingState" + " /n" + intent.getStringExtra(Constans.broadCastKey));
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

    private void enableOrDisableButtonPlay(Intent intent) {
        if(intent.getStringExtra(Constans.CHANGE_BUTTON_PLAY_STATE).equals(Constans.DISABLE)){
            buttonPlay.setEnabled(false);
        }
        else {
            buttonPlay.setEnabled(true);
        }
    }

    private void handleButtonPlayingState(Intent intent) {
        if (intent.getStringExtra(Constans.broadCastKey).contains(Constans.ACTION_START)) {
            if (buttonPlay.getText().toString().equals("STOP")) {
                changeButtonPlayState();
                timeHandler.removeCallbacks(timeRunnable);
                dataHandler.removeCallbacks(dataRunnable);
                refreshingDatabaseHandler.removeCallbacks(refreshingDatabaseRunnable);
            }
        } else {
            if (intent.getStringExtra(Constans.broadCastKey).contains(Constans.ACTION_STOP)) {
                if (buttonPlay.getText().toString().equals("PLAY")) {
                    changeButtonPlayState();
                    timeHandler.postDelayed(timeRunnable, 1000);
                    dataHandler.postDelayed(dataRunnable, 500);
                    refreshingDatabaseHandler.postDelayed(refreshingDatabaseRunnable, frequencyOfRefreshingDatabase*60*1000);
                }
            }
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
    public boolean onOptionsItemSelected(MenuItem item) {
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

        }

        return super.onOptionsItemSelected(item);
    }



    public void play(View view)
    {
        Log.d("numberOfClicks", String.valueOf(getNumberOfClicks()));

        if (getNumberOfClicks() % 2 == 0)
            play();
        else
            stop();
    }

    public void play()
    {
        if(isThereApproriateInternetConnection(getApproriateFlagToRecognizeInternetConnection()))
        {
            buttonPlay.setText("STOP");
            startService(createApproriateIntent(Constans.ACTION_START));
            increaseNumberOfClicks();
            setIsPlaying(true);

            dataHandler.postDelayed(dataRunnable, 0);
            timeHandler.postDelayed(timeRunnable, 1000);
            refreshingDatabaseHandler.postDelayed(refreshingDatabaseRunnable, frequencyOfRefreshingDatabase*60*1000);

        }
        else
        {
            Toast.makeText(getApplicationContext(), "There is no active internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void stop()
    {
        startService(createApproriateIntent(Constans.ACTION_STOP));
        buttonPlay.setText("PLAY");
        increaseNumberOfClicks();
        setIsPlaying(false);

        dataHandler.removeCallbacks(dataRunnable);
        timeHandler.removeCallbacks(timeRunnable);
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

    private Intent createApproriateIntent(String action)
    {
        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.setAction(action);
        return intent;
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
        increaseNumberOfClicks();
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
        Log.d("lifecycle", "onResume");
        dataHandler.postDelayed(dataRunnable, 0);
        synchronizeButtonPlay();
        getFrequencyOfRefreshingDatabase();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("mediaPlayerService"));

        super.onResume();
    }

    private void synchronizeButtonPlay() {
        if(isPlaying())
            buttonPlay.setText("STOP");
        else if(!isPlaying())
            buttonPlay.setText("PLAY");
    }

    public void onStop()
    {
        databaseDao.insertBytes(new Bytes(1, summaryBytesCount + bytesUsedAtTheMoment, Calendar.getInstance().getTimeInMillis(), Constans.ACTION_STOP));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
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
        return PlayerProperties.getInstance().isPlaying();
    }

    private void setIsPlaying(boolean isPlaying)
    {
        PlayerProperties.getInstance().setIsPlaying(isPlaying);
    }

    private int getNumberOfClicks()
    {
        return PlayerProperties.getInstance().getNumberOfClicks();
    }

    private void increaseNumberOfClicks()
    {
        PlayerProperties.getInstance().setNumberOfClicks(getNumberOfClicks()+1);
    }
}
