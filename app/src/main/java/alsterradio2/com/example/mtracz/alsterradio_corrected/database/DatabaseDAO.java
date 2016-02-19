package alsterradio2.com.example.mtracz.alsterradio_corrected.database;

import android.content.Context;

import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.Bytes;

/**
 * Created by mtracz on 09.Feb.2016.
 */
public class DatabaseDAO {

    private DataDbAdapter dbAdapter;
    private Context context;

    public DatabaseDAO(Context context) {
        this.context = context;
        dbAdapter = new DataDbAdapter(this.context);
    }

    public int getAllBytesCount()
    {
        dbAdapter.open();
        int result = dbAdapter.getBytesCount();
        dbAdapter.close();
        return result;
    }

    public long insertBytes(Bytes bytes)
    {
        dbAdapter.open();
        long id =  dbAdapter.insertBytes(String.valueOf(bytes.getLatestBytesCount()), String.valueOf(bytes.getTime()), bytes.getAction());
        dbAdapter.close();
        return id;
    }

    public void insertEmptyEntry() {
        dbAdapter.open();
        Bytes example = new Bytes(1, 0, 0, "");
        dbAdapter.insertBytes(String.valueOf(example.getLatestBytesCount()), String.valueOf(example.getTime()), String.valueOf(example.getAction()));
        dbAdapter.close();
    }

    public Bytes getLastBytesEntry() {
        dbAdapter.open();
        long id = dbAdapter.getBytesCount();
        Bytes result = dbAdapter.getBytesByID(id);
        dbAdapter.close();
        return result;
    }

    public Bytes getBytesById(long id)
    {
        dbAdapter.open();
        Bytes result = dbAdapter.getBytesByID(id);
        dbAdapter.close();
        return result;
    }

    public String getValuesFrom(String time)
    {
        dbAdapter.open();
        String result = dbAdapter.getValueFrom(time);
        dbAdapter.close();
        return result;
    }
}
