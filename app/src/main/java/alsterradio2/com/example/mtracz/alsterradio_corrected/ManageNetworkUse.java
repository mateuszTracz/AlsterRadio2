package alsterradio2.com.example.mtracz.alsterradio_corrected;

import android.content.pm.PackageManager;
import android.net.TrafficStats;

/**
 * Created by mtracz on 09.Feb.2016.
 */
public class ManageNetworkUse {

    public static long getSummaryBytesCount(PackageManager p)
    {
        int uid = -1;
        try {
            uid = p.getApplicationInfo("alsterradio2.com.example.mtracz.alsterradio_corrected", 0).uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return TrafficStats.getUidRxBytes(uid);
    }
}
