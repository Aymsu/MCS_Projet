package com.paulsab.aymer.mcs;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.support.design.widget.FloatingActionButton;

import android.os.Bundle;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.paulsab.aymer.mcs.AnalyzeActivity.AudioRecorderToWav;
import com.paulsab.aymer.mcs.AnalyzeActivity.Constante;

import org.w3c.dom.Text;

import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RecoVocale extends Activity {

    // Used to load the 'native-lib' library on application startup.
    static {
            System.loadLibrary("dtw-lib");
    }

    private Looper samplingThread;
    private int bufferSize;
    private short[] audioBuffer;
    private WaveformView mWaveformView;
    private TextView intro;
    private RelativeLayout circularLayout;
    private TextView mot;
    private RelativeLayout motLayout;
    private String motReconnu;

    private ImageView etoile;
    private ImageView etoile2;

    public native String recoVocale(String filename, AssetManager manager);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reco_vocale);

        intro = (TextView) findViewById(R.id.intro);

        etoile = (ImageView) findViewById(R.id.star1);
        etoile.setImageResource(R.drawable.mariostar);

        etoile2 = (ImageView) findViewById(R.id.star2);
        etoile2.setImageResource(R.drawable.mariostar);


        mot = (TextView) findViewById(R.id.motReconnu);
        motLayout = (RelativeLayout) findViewById(R.id.frameMot);
        motLayout.setVisibility(View.INVISIBLE);

        intro.setText("Maintenez le bouton pour commencer l'enregistrement.");

        circularLayout = (RelativeLayout) findViewById(R.id.activity_reco_vocale);

        /*AssetManager assetManager = getApplicationContext().getAssets();
        intro.setText(recoVocale("file", assetManager));*/

        if(savedInstanceState == null)
            circularLayout.setVisibility(View.INVISIBLE);


        ViewTreeObserver viewTreeObserver = circularLayout.getViewTreeObserver();

        if(viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    circularRevealActivity();
                    circularLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.backButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(StartActivity.mute == 1)
                    StartActivity.mpMario.start();
                finish();
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });

        FloatingActionButton micButton = (FloatingActionButton) findViewById(R.id.microButton);

        mWaveformView = (WaveformView) findViewById(R.id.chart);
        mWaveformView.setVisibility(View.INVISIBLE);

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

        micButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                motLayout.setVisibility(View.INVISIBLE);

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mWaveformView.setVisibility(View.VISIBLE);
                    samplingThread = new Looper();
                    samplingThread.start();
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    samplingThread.finish();

                    try {
                        samplingThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    mot.setText(motReconnu);
                    motLayout.setVisibility(View.VISIBLE);


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
                   // mWaveformView.setVisibility(View.INVISIBLE);
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
                    Constante.RECORDER_CHANNELS,Constante.RECORDER_AUDIO_ENCODING, bufferSize);
            audioRecord = new AudioRecorderToWav("enreg.wav",getBaseContext());
            audioRecord.writeWavHeader2(Constante.RECORDER_CHANNELS,Constante.SAMPLE_RATE,Constante.RECORDER_AUDIO_ENCODING);

            byte[] buffer = new byte[bufferSize];

            int read;
            if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(Constante.TAG, "Audio Record can't initialize!");
                return;
            }
            record.startRecording();

            while(isRunning) {
                read  = record.read(buffer,0, bufferSize);

                audioRecord.write(buffer,read);

                audioBuffer = byteToShort(buffer);
                mWaveformView.updateAudioData(audioBuffer);

            }
            for ( int i = 1000 ; i > 0 ; i=i-20) {
                for ( int j = 0 ; j < audioBuffer.length ; j++) {
                    audioBuffer[j] = (short) i;
                }
                mWaveformView.updateAudioData(audioBuffer);
            }

            record.stop();
            record.release();
            record = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                audioRecord.updateWavHeader();
                audioRecord.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }

            AssetManager assetManager = getApplicationContext().getAssets();
            System.out.println("toto= "+audioRecord.getFilenameOut());
            String ordre = recoVocale(audioRecord.getFilenameOut(), assetManager);
            motReconnu = ordre;

            Log.println(Log.INFO , Constante.TAG, ordre);

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


    }

    private void circularRevealActivity() {

        int cx = circularLayout.getWidth() / 2;
        int cy = circularLayout.getHeight() / 2;

        float finalRadius = Math.max(circularLayout.getWidth(), circularLayout.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(circularLayout, cx, cy, 0, finalRadius);
        circularReveal.setDuration(1000);

        // make the view visible and start the animation
        circularLayout.setVisibility(View.VISIBLE);
        circularReveal.start();
    }


}