package de.thielegram.loomorec;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import AlizeSpkRec.AlizeException;
import AlizeSpkRec.IdAlreadyExistsException;

public class RegistrationActivity extends AppCompatActivity {

    public RegistrationActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_speaker);

        recorder = new Recorder();

        final EditText textFieldName = findViewById(R.id.txt_name_input);
        final ImageButton btnRecording = findViewById(R.id.btn_add_record);
        final ImageButton btnPlay = findViewById(R.id.btn_add_play);
        final Button btnAddSpeaker = findViewById(R.id.btn_add_speaker);

        btnRecording.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                TextView t = findViewById(R.id.txt_recordingpresent);

                if(!isRecording){
                    recorder.startRecording();
                    isRecording = true;
                    t.setText(R.string.txt_recording);
                    btnRecording.setImageResource(R.drawable.ic_recording_stop);
                }else{
                    recorder.stopRecording();
                    isRecording = false;
                    t.setText(R.string.txt_recordingpresent);
                    btnRecording.setImageResource(R.drawable.ic_record);

                    recordingData = recorder.getRecording();
                }
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Wait until the recording process is over", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else if (recorder.getRecording() == null) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Can't start playback until a voice sample is recorded", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else if (isPlaying){
                    recorder.stopPlayback();
                    isPlaying = false;
                    btnPlay.setImageResource(R.drawable.ic_play);
                } else {
                    recorder.startPlayback();
                    isPlaying = true;
                    btnPlay.setImageResource(R.drawable.ic_stop);
                }
            }
        });

        btnAddSpeaker.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                if (isRecording) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Wait until the recording process is over", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else if (recorder.getRecording() == null) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Record an audio sample before registering a new speaker", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else if (textFieldName.length() == 0) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Add a name before registering a new speaker", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    try {
                        addSpeaker();
                    } catch (AlizeException | IdAlreadyExistsException e) {
                        e.printStackTrace();
                    }

                    switchToMainActivity();
                }
            }
        });

        identifier = Identifier.getInstance();

        Toolbar tb = findViewById(R.id.tb_registration);
        tb.setNavigationIcon(R.drawable.ic_back_arrow);
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToMainActivity();
            }
        });
    }

    private Recorder recorder;
    private Identifier identifier;

    private short[] recordingData = new short[0];
    private boolean isRecording = false;
    private boolean isPlaying = false;


    private void addSpeaker() throws AlizeException, IdAlreadyExistsException {
        EditText nameInput = findViewById(R.id.txt_name_input);
        String name = nameInput.getText().toString();

        identifier.addSpeaker(recordingData, name);
    }

    private void switchToMainActivity() {
        if(recorder != null) {
            recorder.stopPlayback();
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
