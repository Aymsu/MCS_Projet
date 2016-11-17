package com.paulsab.aymer.mcs;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.paulsab.aymer.mcs.AnalyzeActivity.AudioRecorderToWav;
import com.paulsab.aymer.mcs.AnalyzeActivity.Constante;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecoVocale extends Activity {

    // Used to load the 'native-lib' library on application startup.
    static {
//            System.loadLibrary("dtw-lib");
    }

    private Looper samplingThread;
    private MediaRecorderToWav mediaRecorderToWav;
    private int bufferSize;
    private short[] audioBuffer;
    private WaveformView mWaveformView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reco_vocale);

        final Button talkButton = (Button) findViewById(R.id.talkButton);
        mediaRecorderToWav = new MediaRecorderToWav();

        mWaveformView = (WaveformView) findViewById(R.id.chart);




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

                    Log.i("l","ALLLEZZZZZZZZZ");
                    // mediaRecorderToWav.startRecording();
                    talkButton.setText("pressssed");
                    samplingThread = new Looper();
                    samplingThread.start();
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    samplingThread.finish();

                    talkButton.setText("Released");
                    // mediaRecorderToWav.stopRecording();

                    MediaPlayer mp = MediaPlayer.create(getBaseContext(),
                            R.raw.loading_cube);

                    mp.start();
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp = MediaPlayer.create(getBaseContext(),R.raw.cube_loaded);
                            mp.start();
                        }
                    });
                }
                return false;
            }
        });

    }

    public class Looper extends Thread{

        AudioRecord record;
        boolean isRunning = true;
        AudioRecorderToWav audioRecord;


        @Override
        public void run() {

            bufferSize = AudioRecord.getMinBufferSize(Constante.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            try {
            record = new AudioRecord(MediaRecorder.AudioSource.MIC, Constante.SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            audioRecord = new AudioRecorderToWav("test.wav");
            audioRecord.writeWavHeader2(AudioFormat.CHANNEL_IN_MONO,Constante.SAMPLE_RATE,AudioFormat.ENCODING_PCM_16BIT);

            byte[] buffer = new byte[bufferSize];

            int read;
            if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(Constante.TAG, "Audio Record can't initialize!");
                return;
            }
            record.startRecording();
            //audioRecord = new AudioRecorderToWav("toto.wav");


            while(isRunning) {
                //TODO: Enregistrer chaque audioBuffer dans un tableau pour creer
                //TODO: le fichier à la fin de la boucle
                read  = record.read(buffer,0, bufferSize);

                //Log.i(Constante.TAG,"data = "+read);

                audioRecord.write(buffer,read);

                audioBuffer = byteToShort(buffer);
                mWaveformView.updateAudioData(audioBuffer);
                updateDecibelLevel();
            }
            audioRecord.stop();

            record.stop();
            record.release();
            record = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                audioRecord.updateWavHeader();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private short[] byteToShort (byte[] buff ) {
            short[] arrayShort = new short[buff.length/2];
            ByteBuffer.wrap(buff).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(arrayShort);
            return arrayShort;
        }

        public void finish() {
            isRunning = false;
            interrupt();
        }



        private void updateDecibelLevel() {
            // Compute the root-mean-squared of the sound buffer and then apply the formula for
            // computing the decibel level, 20 * log_10(rms). This is an uncalibrated calculation
            // that assumes no noise in the samples; with 16-bit recording, it can range from
            // -90 dB to 0 dB.
            double sum = 0;

            for (short rawSample : audioBuffer) {
                double sample = rawSample / 32768.0;
                sum += sample * sample;
            }

            double rms = Math.sqrt(sum / audioBuffer.length);
            final double db = 20 * Math.log10(rms);
        }

    }


    }