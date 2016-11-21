package com.paulsab.aymer.mcs.AnalyzeActivity;

import android.media.AudioFormat;
import android.media.MediaRecorder;

/**
 * Created by benjidu11 on 13/11/2016.
 */

public class Constante {
    public static final String TAG = "audio";

    public final static int SAMPLE_RATE = 44100;
    public static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    public static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
}
