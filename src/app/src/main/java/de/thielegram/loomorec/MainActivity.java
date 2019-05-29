package de.thielegram.loomorec;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

import AlizeSpkRec.AlizeException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_list_speakers = findViewById(R.id.btn_list_speakers);
        btn_list_speakers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToDirectoryActivity();
            }
        });

        Button btn_add_speaker = findViewById(R.id.btn_nav_add_speaker);
        btn_add_speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToRegistrationActivity();
            }
        });

        Button btn_identify_speaker = findViewById(R.id.btn_nav_identify_speaker);
        btn_identify_speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToIdentificationActivity();
            }
        });

        try {
            Identifier.getInstance().initializeSystem(MainActivity.this);
        } catch (IOException | AlizeException e) {
            e.printStackTrace();
        }
    }

    private void switchToDirectoryActivity() {
        Intent intent = new Intent(this, DirectoryActivity.class);
        startActivity(intent);
    }

    private void switchToRegistrationActivity() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    private void switchToIdentificationActivity() {
        Intent intent = new Intent(this, IdentificationActivity.class);
        startActivity(intent);
    }
}
