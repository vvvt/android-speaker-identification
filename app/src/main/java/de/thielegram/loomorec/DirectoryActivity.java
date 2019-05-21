package de.thielegram.loomorec;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import AlizeSpkRec.AlizeException;

public class DirectoryActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_directory);

        identifier = Identifier.getInstance();
        String[] speakerList = new String[0];

        try {
            speakerList = identifier.getSpeakerList();
        } catch (AlizeException e) {
            e.printStackTrace();
        }

        List<String> listData = new ArrayList<>(Arrays.asList(speakerList));
        ArrayAdapter<String> listDataAdapter = new ArrayAdapter<>(this, R.layout.activity_directory_row, R.id.listRowTextView, listData);

        this.setListAdapter(listDataAdapter);

        Toolbar tb = findViewById(R.id.tb_directory);
        tb.setNavigationIcon(R.drawable.ic_back_arrow);
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DirectoryActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private Identifier identifier;

    @Override
    @SuppressWarnings("unchecked")
    protected void onListItemClick(final ListView listView, View v, final int position, long id) {

        final ArrayAdapter arrayAdapter = (ArrayAdapter) listView.getAdapter();
        final Object selectItemObj = arrayAdapter.getItem(position);
        final String itemText = (String)selectItemObj;

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        try {
                            identifier.removeSpeaker(itemText);
                        } catch (AlizeException e) {
                            e.printStackTrace();
                        }

                        arrayAdapter.remove(selectItemObj);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setMessage(R.string.dia_question)
                .setTitle(R.string.dia_title)
                .setPositiveButton(R.string.dia_positive, dialogClickListener)
                .setNegativeButton(R.string.dia_negative, dialogClickListener)
                .show();
    }
}
