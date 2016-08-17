package alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes;

/**
 * Created by mtracz on 02.Feb.2016.
 */
public class Constans {

    public static final String CHANGE_BUTTON_PLAY_STATE = "changeButtonPlayState";
    public static final String DISABLE = "disable";
    public static final String ALL_STREAM_SET = "streamsSet";
    public static final String SELECTED_STREAM = "streamSelected";
    public static final String SELECTED_STREAM_NAME = "selectedStreamName";
    public static String ENABLE = "enalbe";

    public static long BYTES_KB = 1024;
    public static long BYTES_MB = 1024 * 1014;
    public static long BYTES_GB = 1024 * 124 * 1024;
    public static long BYTES_TB = (long) 1024 * 124 * 1024 * 1024;

    public static String ACTION_START = "start";
    public static String ACTION_STOP = "stop";
    public static String ACTION_UPDATE_NOTIFICATION = "update_notification";

    public static String alsterRadioStream64 = "http://live64.alsterradio.de";
    public static String muzoStream = "http://stream4.nadaje.com/muzo";
    public static String alsterRadioStream128_copy = "http://62.75.154.4:80";
    public static String alsterRadioStream128 = "http://live96.alsterradio.de";

    public static String broadCastKey = "broadcastKey";
    public static String handleButtonPlaying = "handleButtonPlaying";
    public static String keyToRecognizeAction = "keyToRecognizeAction";
    public static int notificationId = 999;
    public static String STORE_IN_DATABASE = "storeInDatabase";

    public static String actuallyPlayingSongKey = "actuallyPlayingSongKey";
    public static String addSongToFavourite = "addSongToFavourite";
    public static String intentFilterMetadata = "metadataTask";

    public enum MetadataCalledBy {user, handler, addToFavourites}
}
