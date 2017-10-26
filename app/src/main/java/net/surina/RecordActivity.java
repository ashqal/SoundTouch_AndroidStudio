package net.surina;

import android.app.Activity;
import android.os.Bundle;

import net.surina.soundtouch.SoundTouch;
import net.surina.soundtouchexample.R;

import java.nio.ByteBuffer;


/**
 * Created by hzqiujiadi on 2017/10/25.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class RecordActivity extends Activity {

    private static final String TAG = "SOUNDTOUCH";

    private PcmPlayer pcmPlayer;

    private RecAudioClient client;

    private SoundTouch soundTouch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        soundTouch = new SoundTouch();
        soundTouch.setPitchSemiTones(5);

        client = new RecAudioClient();
        client.prepare(new RecAudioClient.AudioCallback() {
            @Override
            public void onRecvAudioData(ByteBuffer buffer) {
                buffer.reset();
                soundTouch.processBuffer(buffer, buffer.remaining(), 2, 48000, 1);

                // Log.d(TAG, String.format("onRecvAudioData buffer.arrayOffset():%d", buffer.arrayOffset()));

                buffer.reset();
                pcmPlayer.write(buffer);
            }

        });

        pcmPlayer = new PcmPlayer();
        pcmPlayer.prepare();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.destroy();
        pcmPlayer.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        client.pause();
        pcmPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        client.resume();
        pcmPlayer.resume();
    }
}
