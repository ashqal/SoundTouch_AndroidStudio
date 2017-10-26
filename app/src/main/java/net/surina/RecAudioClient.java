package net.surina;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by lake on 16-5-24.
 *
 */
public class RecAudioClient {
    private static final String TAG = "SOUNDTOUCH";
    private final Object syncOp = new Object();
    private AudioRecordThread audioRecordThread;
    private AudioRecord audioRecord;
    private ByteBuffer audioBuffer;
    private int audioRecordBufferSize;
    private int audioRecordSliceSize;
    private AudioCallback callback;
    private static final int sSampleRate = 48000;

    public RecAudioClient() {
    }

    public boolean prepare(AudioCallback callback) {
        synchronized (syncOp) {
            this.callback = callback;
            audioRecordSliceSize = sSampleRate / 50;
            audioRecordBufferSize = audioRecordSliceSize;
            prepareAudio();
            return true;
        }
    }

    private boolean start() {
        synchronized (syncOp) {
            if (audioRecordThread != null && audioRecordThread.isRunning) {
                return true;
            }

            audioRecord.startRecording();
            audioRecordThread = new AudioRecordThread();
            audioRecordThread.start();
            return true;
        }
    }

    private boolean stop() {
        synchronized (syncOp) {
            audioRecordThread.quit();
            try {
                audioRecordThread.join();
            } catch (InterruptedException ignored) {
            }
            audioRecordThread = null;
            audioRecord.stop();
            return true;
        }
    }

    public boolean destroy() {
        synchronized (syncOp) {
            audioRecord.release();
            return true;
        }
    }

    private boolean prepareAudio() {
        int minBufferSize = AudioRecord.getMinBufferSize(sSampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                sSampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize * 5);
        // audioRecordBufferSize
        audioBuffer = ByteBuffer.allocateDirect(audioRecordBufferSize * 2);
        if (AudioRecord.STATE_INITIALIZED != audioRecord.getState()) {
            Log.e("aa", "audioRecord.getState()!=AudioRecord.STATE_INITIALIZED!");
            return false;
        }
        if (AudioRecord.SUCCESS != audioRecord.setPositionNotificationPeriod(audioRecordSliceSize)) {
            Log.e("aa", "AudioRecord.SUCCESS != audioRecord.setPositionNotificationPeriod(" + audioRecordSliceSize + ")");
            return false;
        }
        return true;
    }

    public void resume() {
        start();
    }

    public void pause() {
        stop();
    }

    private class AudioRecordThread extends Thread {
        private boolean isRunning = true;

        AudioRecordThread() {
            isRunning = true;
        }

        public void quit() {
            isRunning = false;
        }

        @Override
        public void run() {
            while (isRunning) {
                int size = audioRecord.read(audioBuffer.array(), audioBuffer.arrayOffset(), audioRecordBufferSize);

                if (isRunning && size > 0) {
                    audioBuffer.clear();
                    audioBuffer.put(audioBuffer.array(), audioBuffer.arrayOffset(), audioRecordBufferSize);
                    audioBuffer.flip();
                    audioBuffer.mark();
                    callback.onRecvAudioData(audioBuffer);
                }
            }
        }
    }

    public interface AudioCallback {
        void onRecvAudioData(ByteBuffer audioBuffer);
    }
}
