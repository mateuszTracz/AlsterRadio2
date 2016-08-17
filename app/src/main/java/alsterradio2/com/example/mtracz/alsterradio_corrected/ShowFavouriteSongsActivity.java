package alsterradio2.com.example.mtracz.alsterradio_corrected;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import alsterradio2.com.example.mtracz.alsterradio_corrected.database.DatabaseDAO;
import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.Song;


public class ShowFavouriteSongsActivity extends Activity {

    private DatabaseDAO databaseDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_favourite_songs);

        databaseDAO = new DatabaseDAO(this);
        ArrayList<Song> allFavouriteSongs = databaseDAO.getAllFavouriteSongs();
        int size = allFavouriteSongs.size();
        String[] songsToDisplay = new String[size];
        for (int i = 0; i < size; i++) {
            songsToDisplay[i] = allFavouriteSongs.get(i).getTitle();
        }

        ListView listView = (ListView) findViewById(R.id.listViewShowFavouriteSongs);
        listView.setAdapter(new SongListAdapter(this, R.layout.show_song_list_item, allFavouriteSongs));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_favourite_songs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class SongListAdapter extends ArrayAdapter<Song> {
        public SongListAdapter(Context context, int resource, List<Song> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = getLayoutInflater().inflate(R.layout.show_song_list_item, parent, false);
            }

            Song currentSong = getItem(position);

            ImageView icon = (ImageView) convertView.findViewById(R.id.songListViewIcon);
            TextView title = (TextView) convertView.findViewById(R.id.songListViewTitle);
            TextView timestamp = (TextView) convertView.findViewById(R.id.songListViewTimestamp);

            title.setText(currentSong.getTitle());
            Date date = new Date(Long.parseLong(currentSong.getTimestamp()));
            timestamp.setText(currentSong.getDateAndTimePlayed());

            return convertView;
        }
    }
}
