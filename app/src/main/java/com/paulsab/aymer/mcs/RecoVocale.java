package com.paulsab.aymer.mcs;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.paulsab.aymer.mcs.AnalyzeActivity.Constante;

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

        bufferSize = AudioRecord.getMinBufferSize(Constante.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioBuffer = new short[bufferSize / 2];


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
                    samplingThread.finish();                    talkButton.setText("Released");
                    // mediaRecorderToWav.stopRecording();
                    MediaPlayer mp = MediaPlayer.create(getBaseContext(),
                            R.raw.bastion_sound);
                    mp.start();
                }
                return false;
            }
        });

    }

    public class Looper extends Thread{

        AudioRecord record;
        int minBytes;
        long baseTimeMs;
        boolean isRunning = true;
        boolean isPaused1 = false;


        @Override
        public void run() {

            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
            record = new AudioRecord(MediaRecorder.AudioSource.MIC, Constante.SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            record.startRecording();

            while(isRunning) {

                record.read(audioBuffer, 0, bufferSize/2);
                mWaveformView.updateAudioData(audioBuffer);
                updateDecibelLevel();

            }


            record.stop();
            record.release();
            record = null;
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

        private void update(final double[] data) {
            RecoVocale.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }
    }


    }