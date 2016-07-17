package alsterradio2.com.example.mtracz.alsterradio_corrected;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.HashSet;
import java.util.Set;

import alsterradio2.com.example.mtracz.alsterradio_corrected.datatypes.Constans;

/**
 * Created by MTRACZ on 14.Mrz.2016.
 */
public class AddStreamDialog extends DialogFragment{
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstance)
    {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Add Stream");

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View view = inflater.inflate(R.layout.add_stream_layout, null);
        alertDialog.setView(view);

        final EditText stream = (EditText) view.findViewById(R.id.editTextStreamName);
        final EditText streamURL = (EditText) view.findViewById(R.id.editTextStreamURL);


        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String line = "(" + streamURL.getText().toString() + ")" + stream.getText().toString();

                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                editor = sharedPreferences.edit();
                Set<String> streamSet = sharedPreferences.getStringSet(Constans.ALL_STREAM_SET, new HashSet<String>());
                streamSet.add(line);
                editor.putStringSet(Constans.ALL_STREAM_SET, streamSet);
                editor.commit();
            }
            });




        return alertDialog.create();
    }
}
