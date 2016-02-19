package alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes;

/**
 * Created by mtracz on 09.Feb.2016.
 */
public class BytesMapper {

    public static String getUnderstableForm(long value)
    {
        if (value < Constans.BYTES_KB) {
            return value + " B";
        }
        else //kB
        {
            if (value < Constans.BYTES_MB) {
                return cutToPoints(value / 1024.0) + " kB";
            }
            else //MB
            {
                if (value < Constans.BYTES_GB) {
                    return cutToPoints(value / (1024 * 1024.0)) + " MB";
                }
                else //GB
                {
                    if (value < Constans.BYTES_TB) {
                        return cutToPoints(value / (1024 * 1024 * 1024.0)) + " GB";
                    }
                }
            }
        }
        return "error";
    }

    private static String cutToPoints(Double value) {
        String x = String.valueOf(value);
        //Log.d("Value to cut", x);
        if(x.indexOf('.') != -1 && x.length() - x.indexOf('.') > 3)
            return x.substring(0, x.indexOf('.')+3);
        else {
            //TODO
            //if you see repaired on screen change this IMMEDIATELY
            return "repaired";
        }
    }
}
