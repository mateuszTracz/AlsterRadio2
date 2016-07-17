package alsterradio2.com.example.mtracz.alsterradio_corrected;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.Constans;

/**
 * Created by MTRACZ on 31.Mrz.2016.
 */
public class GetMetadataTask extends AsyncTask<String, Integer, String> {
    private String metadata;
    private Context context;
    private boolean wasCalledByHandler = false;

    public GetMetadataTask(Context context, boolean wasCalledByHanler)
    {
        super();
        this.context = context;
        this.wasCalledByHandler = wasCalledByHanler;
    }

    @Override
    protected String doInBackground(String... params){
        String toDisplay = null;
        try {
            URL updateURL = null;
            try {
                updateURL = new URL(params[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            URLConnection conn = null;
            try {
                conn = updateURL.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            conn.setRequestProperty("Icy-MetaData", "1");
            int interval = 0;
            try {
                interval = Integer.valueOf(conn.getHeaderField("icy-metaint")); // You can get more headers if you wish. There is other useful data.
            }
            catch (Exception e)
            {
                Log.d("TAG", e.toString());
            }

            InputStream is = conn.getInputStream();

            int skipped = 0;
            while (skipped < interval) {
                skipped += is.skip(interval - skipped);
            }

            int metadataLength = is.read() * 16;

            int bytesRead = 0;
            int offset = 0;
            byte[] bytes = new byte[metadataLength];

            while (bytesRead < metadataLength && bytesRead != -1) {
                bytesRead = is.read(bytes, offset, metadataLength);
                offset = bytesRead;
            }

            String metaData = new String(bytes);
            ArrayList<Character> meta = new ArrayList<Character>();


            Log.d("Probalby metadata: ", metaData);
            int index = metaData.indexOf('\'')+1;
            try {
                toDisplay = metaData.substring(index, metaData.indexOf('\'', index + 1));
            }
            catch(Exception e)
            {
               toDisplay = "Stream doesn't return any data!";
            }
            metadata = toDisplay;
            is.close();
        } catch (MalformedURLException e) { e.printStackTrace();
        } catch (IOException e) { e.printStackTrace(); }



        return toDisplay;
    }

    protected void onPostExecute(String result)
    {
        if(metadata != null) {
            if (!wasCalledByHandler) {
                Toast.makeText(context, metadata, Toast.LENGTH_SHORT).show();
            }
            persistentActualPlayingSongData();
        }
    }

    private void persistentActualPlayingSongData() {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sh.edit();
        editor.putString(Constans.actuallyPlayingSongKey, metadata);
        editor.commit();
    }
}
