package com.paulsab.aymer.mcs;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.IOException;
import java.util.jar.Manifest;

    public class RecoVocale extends AppCompatActivity {

        // Used to load the 'native-lib' library on application startup.
        static {
            System.loadLibrary("dtw-lib");
        }
        private MediaRecorderToWav recorder = new MediaRecorderToWav();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_reco_vocale);

            final Button talkButton = (Button) findViewById(R.id.talkButton);

            // Demander à l'utilisateur d'utiliser le microphone
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.RECORD_AUDIO},0);
            }

            // Demander à l'utilisateur de créer des fichiers
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                String[] rules = new String[]{
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                };
                ActivityCompat.requestPermissions(this,rules,1);
            }

            talkButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        recorder.startRecording();
                        talkButton.setText("pressssed");
                    }
                    else if (event.getAction() == MotionEvent.ACTION_UP){
                        recorder.stopRecording();
                        talkButton.setText("Released");
                        MediaPlayer mp = MediaPlayer.create(getBaseContext(),
                                R.raw.bastionSound);
                        mp.start();
                    }
                    return false;
                }
            });

        }

    }