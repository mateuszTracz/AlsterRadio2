package alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes;

/**
 * Created by mtracz on 09.Feb.2016.
 */
public class Bytes {
    private long id;
    private long latestBytesCount;
    private long time;
    private String action;

    public Bytes(long id, long latestBytesCount, long time, String action) {
        this.id = id;
        this.latestBytesCount = latestBytesCount;
        this.time = time;
        this.action = action;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLatestBytesCount() {
        return latestBytesCount;
    }

    public void setLatestBytesCount(long latestBytesCount) {
        this.latestBytesCount = latestBytesCount;
    }

    @Override
    public String toString() {
        return "Bytes{" +
                "id=" + id +
                ", latestBytesCount=" + latestBytesCount +
                ", time=" + time +
                ", action='" + action + '\'' +
                '}';
    }

    public long getTime() {
        return time;
    }

    public String getAction() {
        return action;
    }
}
