package com.paulsab.aymer.mcs;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.paulsab.aymer.mcs.AnalyzeActivity.Constante;
import com.paulsab.aymer.mcs.AnalyzeActivity.RealDoubleFFT;
import com.paulsab.aymer.mcs.Charts.DrawLineChart;

public class RecoVocale extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
//            System.loadLibrary("dtw-lib");
    }

    private Looper samplingThread;
    private LineChart graphView;
    private DrawLineChart graphChart;
    private MediaRecorderToWav mediaRecorderToWav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reco_vocale);

        final Button talkButton = (Button) findViewById(R.id.talkButton);
        mediaRecorderToWav = new MediaRecorderToWav();
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
                    graphView =  (LineChart) findViewById(R.id.chart);
                    graphChart = new DrawLineChart(graphView);
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
                            R.raw.bastionsound);
                    mp.start();
                }
                return false;
            }
        });

    }

    /**
     * Convert our samples to double for fft.
     */
    private static double[] shortToDouble(short[] s, double[] d) {
        for (int i = 0; i < d.length; i++) {
            d[i] = s[i];
        }
        return d;
    }

    /**
     * Compute db of bin, where "max" is the reference db
     * @param r Real part
     * @param i complex part
     */
    private static double db2(double r, double i, double maxSquared) {
        return 5.0 * Math.log10((r * r + i * i) / maxSquared);
    }

    /**
     * Convert the fft output to DB
     */

    static double[] convertToDb(double[] data, double maxSquared) {
        data[0] = db2(data[0], 0.0, maxSquared);
        int j = 1;
        for (int i=1; i < data.length - 1; i+=2, j++) {
            data[j] = db2(data[i], data[i+1], maxSquared);
        }
        data[j] = data[0];
        return data;
    }



    public double[] hanningFunction ( int sizeVector) {
        double[] m_win = new double[sizeVector];
        for ( int i = 0 ; i < sizeVector ; i++ ){
            m_win[i] = (4.0/sizeVector) * 0.5*(1-Math.cos(2*Math.PI*i/sizeVector));
        }
        return m_win;
    }

    public void recompute(double[] data) {
        graphChart.recompute(data,data.length);
        graphView.invalidate();
    }
    public class Looper extends Thread{

        AudioRecord record;
        int minBytes;
        long baseTimeMs;
        boolean isRunning = true;
        boolean isPaused1 = false;

        public Looper() {
            minBytes = AudioRecord.getMinBufferSize(Constante.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            minBytes = Math.max(minBytes, Constante.FFT_BINS);
            // VOICE_RECOGNITION: use the mic with AGC turned off!
            record =  new AudioRecord(Constante.AGC_OFF, Constante.SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,  minBytes);
        }

        @Override
        public void run() {
            double[] fftData = new double[Constante.FFT_BINS];
            RealDoubleFFT fft = new RealDoubleFFT(Constante.FFT_BINS);
            double scale = Constante.MEAN_MAX * Constante.MEAN_MAX * Constante.FFT_BINS * Constante.FFT_BINS / 2d;

            short[] audioSamples = new short[minBytes];
            record.startRecording();
            baseTimeMs = SystemClock.uptimeMillis();
            int bufferReadResult;

            double[] win = hanningFunction(Constante.FFT_BINS);

            while(isRunning) {
                baseTimeMs += Constante.UPDATE_MS;
                /*int delay = (int) (baseTimeMs - SystemClock.uptimeMillis());
                if (delay < 20) {
                    Log.i(Constante.TAG, "wait: " + delay);
                }
                try {
                    Thread.sleep(delay < 10 ? 10 : delay);
                } catch (InterruptedException e) {
                    // Log.i(TAG, "Delay interrupted");
                    continue;
                }*/
                int valplus = 0;
                int valmoins = 0;
                bufferReadResult = record.read(audioSamples, 0, minBytes);
                for ( int i = 0 ; i < minBytes && i < bufferReadResult ; i++) {
                        fftData[i] = win[i] * audioSamples[i];
                    /*if ( audioSamples[i] > 500) {
                        valplus++;
                    }else{
                        valmoins++;*/
                    //}
                   // Log.i(Constante.TAG,"fftdata = "+audioSamples[i]);
                }
                /*Log.i(Constante.TAG,"plus = "+valplus+" moins = "+valmoins);
                valmoins = 0;
                valplus = 0;*/
                fft.ft(fftData);
                convertToDb(fftData, scale);

                update(fftData);

                /**
                 * TODO: transformer FFT dans le chart
                 * TODO: Arrêter le thread
                 */

            }
            Log.i(Constante.TAG, "Releasing Audio");
            record.stop();
            record.release();
            record = null;
        }

        public void finish() {
            isRunning = false;
            interrupt();
        }

        public void showData (double[] data){
            Log.i(Constante.TAG,"affichage des Datas : ");
            /*for (int i = 0 ; i < data.length ; i++){*/
                Log.i(Constante.TAG,Double.toString(data[0]));
            //}
        }

        private void update(final double[] data) {
            RecoVocale.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    RecoVocale.this.recompute(data);
                }
            });
        }
    }


    }