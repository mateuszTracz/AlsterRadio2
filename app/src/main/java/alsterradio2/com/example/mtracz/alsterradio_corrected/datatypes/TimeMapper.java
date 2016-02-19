package alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes;

/**
 * Created by mtracz on 09.Feb.2016.
 */
public class TimeMapper {

    public static String getTimeFormatted(long time) {
        long seconds, minutes, hours;
        seconds = time % 60;
        minutes = time / 60 % 60;
        hours = time / 3600 % 60;

        StringBuilder result = new StringBuilder();
        if (hours < 10) result.append("0").append(hours).append(":");
        else result.append(hours).append(":");

        if (minutes < 10) result.append("0").append(minutes).append(":");
        else result.append(minutes).append(":");

        if (seconds < 10) result.append("0").append(seconds);
        else result.append(seconds);

        return result.toString();
    }
}
