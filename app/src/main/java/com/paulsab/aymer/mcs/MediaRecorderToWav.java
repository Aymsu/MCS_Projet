package com.paulsab.aymer.mcs;


import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;


import java.io.IOException;

/**
 * Created by benjidu11 on 08/11/2016.
 */

public class MediaRecorderToWav {

    private MediaRecorder recorder = null;

    private String filenameDirectory = new String();
    /**
     * Méthode exécutée quand l'utilisateur appuie sur le push-to-tal
     */
    public void startRecording () {
        recorder = new MediaRecorder();

        filenameDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        filenameDirectory += "/temp.wav";

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(filenameDirectory);

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start();
    }

    public void stopRecording () {
        if ( recorder != null ) {
            recorder.stop();
        }
        recorder.release();
        recorder = null;

    }

    public String getFilenameDirectory(){
        return filenameDirectory;
    }

}
