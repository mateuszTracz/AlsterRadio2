package alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes;

/**
 * Created by MTRACZ on 17.Jul.2016.
 */
public class Song {

    private long id;
    private String timestamp;
    private String songPlayedBy;
    private String title;

    public Song(long id, String timestamp, String songPlayedBy, String title) {
        this.id = id;
        this.timestamp = timestamp;
        this.songPlayedBy = songPlayedBy;
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSongPlayedBy() {
        return songPlayedBy;
    }

    public void setSongPlayedBy(String songPlayedBy) {
        this.songPlayedBy = songPlayedBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
