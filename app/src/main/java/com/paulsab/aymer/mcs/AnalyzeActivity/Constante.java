package com.paulsab.aymer.mcs.AnalyzeActivity;

import android.media.MediaRecorder;

/**
 * Created by benjidu11 on 13/11/2016.
 */

public class Constante {
    public static final String TAG = "audio";
    public final static float MEAN_MAX = 16384f;   // Maximum signal value
    public final static int AGC_OFF = MediaRecorder.AudioSource.VOICE_RECOGNITION;
    public final static int FFT_BINS = 1024;
    public final static int SAMPLE_RATE = 22050;
    public final static int UPDATE_MS = 150;
    public final static int MAX_X = 20000;
    public final static int MAX_Y = -90;
}
