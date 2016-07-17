package alsterradio2.com.example.mtracz.alsterradio_corrected;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.Constans;
import alsterradio2.com.example.mtracz.alsterradio_corrected.utils.Utils;

/**
 * Created by MTRACZ on 29.Mrz.2016.
 */
public class ChangeStreamDialog extends DialogFragment {
    private String[] listOfStreams;
    private SharedPreferences sharedPreference;
    private Context context;


    @Override
    public Dialog onCreateDialog(final Bundle savedInstance)
    {
        sharedPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Change Stream");
        listOfStreams = getAvailableStreams();
        String [] listOfStreamNames = prepareStreamNames(listOfStreams);
        builder.setItems(listOfStreamNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = sharedPreference.edit();
                editor.putString(Constans.SELECTED_STREAM, getStreamUrlById(which));
                editor.putString(Constans.SELECTED_STREAM_NAME, getStreamName(listOfStreams[which]));
                editor.commit();

                boolean isRestartPossible = !MediaPlayerProperties.getInstance().shouldButtonPlayBeDisabled();
                if(isRestartPossible)
                    Utils.restartPlaying(context);
                else
                    Toast.makeText(getActivity(), "Restarting not possible", Toast.LENGTH_SHORT).show();
            }
        });
        return builder.create();
    }

    public void setContext(Context context)
    {
       this.context = context;
    }

    private String getStreamUrlById(int which) {
        return listOfStreams[which].substring(listOfStreams[which].indexOf('(')+1, listOfStreams[which].indexOf(')'));
    }

    private String[] getAvailableStreams() {
        Set<String> listOfStreamsSet = sharedPreference.getStringSet(Constans.ALL_STREAM_SET, new HashSet<String>());
        String[] availableStreams = new String[listOfStreamsSet.size()];
        Iterator it = listOfStreamsSet.iterator();
        int i = 0;
        while (it.hasNext())
        {
            availableStreams[i] = (((String) it.next()));
            i++;
        }
        return availableStreams;
    }

    private String getStreamName(String line)
    {
        return line.substring(line.indexOf(")")+1, line.length());
    }

    private String[] prepareStreamNames(String[] lines)
    {
        String[] array = new String[lines.length];
        for(int i = 0; i < lines.length; i++) {
            array[i] = lines[i].substring(lines[i].indexOf(')') + 1, lines[i].length());
        }
        return array;
    }
}
