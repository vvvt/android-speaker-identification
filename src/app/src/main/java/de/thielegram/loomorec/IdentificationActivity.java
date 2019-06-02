package de.thielegram.loomorec;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import AlizeSpkRec.AlizeException;
import AlizeSpkRec.SimpleSpkDetSystem.SpkRecResult;

public class IdentificationActivity extends AppCompatActivity {
    public IdentificationActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify_speaker);

        recorder = new Recorder();
        identifier = Identifier.getInstance();

        final ImageButton btnRecording = findViewById(R.id.btn_identify_record);
        final ImageButton btnPlay = findViewById(R.id.btn_identify_play);

        btnRecording.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                TextView t = findViewById(R.id.txt_identify_recording_present);

                if(!isRecording){
                    recorder.startRecording();
                    isRecording = true;
                    btnRecording.setImageResource(R.drawable.ic_recording_stop);
                    t.setText(R.string.txt_recording);
                }else{
                    try {
                        recorder.stopRecording();
                        isRecording = false;
                        btnRecording.setImageResource(R.drawable.ic_record);
                        t.setText(R.string.txt_recording_present);

                        recordingData = recorder.getRecording();
                        identifySpeaker();
                    } catch (AlizeException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

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

        Toolbar tb = findViewById(R.id.tb_identify);
        tb.setNavigationIcon(R.drawable.ic_back_arrow);
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recorder != null) {
                    recorder.stopPlayback();
                }
                Intent intent = new Intent(IdentificationActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private Recorder recorder;
    private Identifier identifier;

    private short[] recordingData = new short[0];
    private boolean isRecording = false;
    private boolean isPlaying = false;

    private void identifySpeaker() throws AlizeException {
        SpkRecResult result = identifier.identifySpeaker(recordingData);

        TextView txtMatch = findViewById(R.id.txt_result_match);
        TextView txtID = findViewById(R.id.txt_result_id);
        TextView txtScore = findViewById(R.id.txt_result_score);

        txtMatch.setText(String.format("%s%s", getString(R.string.txt_identify_result_match), result.match));
        txtID.setText(String.format("%s%s", getString(R.string.txt_identify_result_id), result.speakerId));
        txtScore.setText(String.format("%s%s", getString(R.string.txt_identify_result_score), result.score));
    }
}
