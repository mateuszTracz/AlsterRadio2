package alsterradio2.com.example.mtracz.alsterradio_corrected;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;

/**
 * Created by MTRACZ on 05.Feb.2016.
 */
public class Settings extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setApproriateTheme();
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
