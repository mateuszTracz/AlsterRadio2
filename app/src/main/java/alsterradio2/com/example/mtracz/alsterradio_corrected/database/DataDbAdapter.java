package alsterradio2.com.example.mtracz.alsterradio_corrected.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Calendar;

import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.Bytes;

/**
 * Created by mtracz on 09.Feb.2016.
 */
public class DataDbAdapter {

    private static String DEBUG_TAG = "SqLiteDebugger";

    private static int DB_VERSION = 1;
    private static String DB_NAME = "AlsterRadioDatabase";

    private static String DB_BYTES_TABLE = "bytesTable";

    private static String KEY_ID = "id";
    private static String ID_OPTIONS = "INTEGER PRIMARY KEY AUTOINCREMENT";
    private static int KEY_ID_COLUMN_NUMBER = 0;

    private static String KEY_BYTES = "bytes";
    private static String BYTES_OPTIONS = "TEXT NOT NULL";
    private static int KEY_BYTES_COLUMN_NUMBER = 1;

    private static String KEY_TIME = "time";
    private static String TIME_OPTIONS = "TEXT";
    private static int KEY_TIME_COLUMN_NUMBER = 2;

    private static String KEY_ACTION = "action";
    private static String ACTION_OPTIONS = "TEXT";
    private static int KEY_ACTION_COLUMN_NUMBER = 3;

    //_____________________
    private static String DB_CREATE_BYTES_TABLE = "CREATE TABLE " + DB_BYTES_TABLE + " (" +
            KEY_ID + " " + ID_OPTIONS + ", " +
            KEY_BYTES + " " + BYTES_OPTIONS + ", " +
            KEY_TIME + " " + TIME_OPTIONS + ", " +
            KEY_ACTION + " " + ACTION_OPTIONS + ");";


    private static String DB_DROP_BYTES_TABLE = "DROP IF EXISTS " + DB_BYTES_TABLE;

    private SQLiteDatabase db;
    private Context context;
    private DatabaseHelper dbHelper;

    public DataDbAdapter(Context context)
    {
        this.context = context;
    }

    public DataDbAdapter open()
    {
        dbHelper = new DatabaseHelper(context, DB_NAME, null, DB_VERSION);
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        dbHelper.close();
    }

    public long insertBytes(String bytes, String time, String action)
    {
        ContentValues newTodoValues = new ContentValues();
        newTodoValues.put(KEY_BYTES, bytes);
        newTodoValues.put(KEY_TIME, time);
        newTodoValues.put(KEY_ACTION, action);
        return db.insert(DB_BYTES_TABLE, null, newTodoValues);
    }

    public boolean updateBytes(Bytes bytes) {
        long id = bytes.getId();
        String bytesCount = String.valueOf(bytes.getLatestBytesCount());
        return updateBytes(id, bytesCount);
    }

    public boolean updateBytes(long id, String bytes) {
        String where = KEY_ID + "=" + id;
        ContentValues updateTodoValues = new ContentValues();
        updateTodoValues.put(KEY_BYTES, bytes);
        return db.update(DB_BYTES_TABLE, updateTodoValues, where, null) > 0;
    }

    public boolean deleteBytes(long id)
    {
        String where = KEY_ID + "=" + id;
        return db.delete(DB_BYTES_TABLE, where, null) > 0;
    }

    public Cursor getAllBytes()
    {
        String[] columns = {KEY_ID, KEY_BYTES};
        return db.query(DB_BYTES_TABLE, columns, null, null, null, null, null);
    }

    public Bytes getBytesByID(long id) throws NumberFormatException
    {
        String[] columns = {KEY_ID, KEY_BYTES, KEY_TIME, KEY_ACTION};
        String where = KEY_ID + "=" + id;
        Cursor cursor = db.query(DB_BYTES_TABLE, columns, where, null, null, null, null);
        Bytes bytes = null;
        if(cursor != null && cursor.moveToFirst())
        {
            long actualBytes = Long.parseLong(cursor.getString(KEY_BYTES_COLUMN_NUMBER));
            long time = Long.parseLong(cursor.getString(KEY_TIME_COLUMN_NUMBER));
            String action = cursor.getString(KEY_ACTION_COLUMN_NUMBER);
            bytes = new Bytes(id, actualBytes, time, action);
        }
        cursor.close();
        return bytes;
    }

    public String getValueFrom(String time)
    {
        String[] columns = {KEY_ID, KEY_BYTES, KEY_TIME, KEY_ACTION};
        Cursor c = db.query(DB_BYTES_TABLE, columns, null, null, null, null, null);

        long timeToCompare = Long.parseLong(time);
        String result = "0";
        c.moveToFirst();
        do {
            long id = Long.parseLong(c.getString(KEY_ID_COLUMN_NUMBER));
            long timeDB = Long.parseLong(c.getString(KEY_TIME_COLUMN_NUMBER));
            long bytes = Long.parseLong(c.getString(KEY_BYTES_COLUMN_NUMBER));
            String action = c.getString(KEY_ACTION_COLUMN_NUMBER);

            Bytes toPressent = new Bytes(id, bytes, timeDB, action);
            Log.d("toPresent", toPressent.toString());

            if(Calendar.getInstance().getTimeInMillis() - timeToCompare > timeDB)
            {
                result = c.getString(KEY_BYTES_COLUMN_NUMBER);
                //return result;
            }

        }while(c.moveToNext());
        c.close();
        return result;
    }

    public int getBytesCount()
    {
        Cursor c = db.rawQuery("SELECT * FROM " + DB_BYTES_TABLE, null);
        return c.getCount();

    }

    public long getLastTime(long id) {
        String columns[] = {KEY_ID, KEY_TIME};
        String where = KEY_ID + "=" + id;
        Cursor cursor = db.query(DB_BYTES_TABLE, columns, where, null, null, null, null);
        long time = 0;
        if (cursor != null && cursor.moveToFirst()) {
            time = cursor.getLong(KEY_BYTES_COLUMN_NUMBER);
        }
        cursor.close();
        return time;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static String DEBUG_TAG = "SqLite";

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_BYTES_TABLE);
            Log.d(DEBUG_TAG, "Creating database");
            Log.d(DEBUG_TAG, "Table " + DB_BYTES_TABLE + " ver. " + DB_VERSION + " created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DB_DROP_BYTES_TABLE);

            Log.d(DEBUG_TAG, "Database updating...");
            Log.d("SqLite", "Table " + DB_BYTES_TABLE + " updated from ver. " + oldVersion + " to ver. " + newVersion);

            onCreate(db);
        }
    }
}
