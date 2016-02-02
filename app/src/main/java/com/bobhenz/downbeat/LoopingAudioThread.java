package com.bobhenz.downbeat;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Created by bhenz on 1/31/2016.
 */
public class LoopingAudioThread {

    private boolean isRunning;
    private short[] audioData;
    private int dataSizeSamples;
    private int samplesPerSecond;
    private int sampleSizeBytes;
    private int audioFormat;
    private int audioChannels;

    LoopingAudioThread(short[] audioData, int dataSizeSamples, int samplesPerSecond) {
        isRunning = false;
        this.audioData = audioData;
        this.dataSizeSamples = dataSizeSamples;
        this.samplesPerSecond = samplesPerSecond;

        // These are all linked and assumed true for the given data.
        this.audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        this.audioChannels = AudioFormat.CHANNEL_OUT_MONO;
        this.sampleSizeBytes = 2;
    }

    public void Start() {
        Thread runner = new Thread() {
            public void run() {
                // set process priority
                setPriority(Thread.MAX_PRIORITY);
                // set the buffer size
                int requestedSizeBytes = AudioTrack.getMinBufferSize(samplesPerSecond,
                        audioChannels, audioFormat);

                int bufferSizeBytes;
                int dataSizeBytes = dataSizeSamples * sampleSizeBytes;
                if (requestedSizeBytes <= dataSizeBytes) {
                    bufferSizeBytes = dataSizeBytes;
                } else {
                    bufferSizeBytes = (requestedSizeBytes / dataSizeBytes + 1) * dataSizeBytes;
                    // Make sure buffer is a multiple of the sample size.
                    bufferSizeBytes = (bufferSizeBytes / sampleSizeBytes + 1) * sampleSizeBytes;
                }

                // create an audiotrack object
                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        samplesPerSecond, audioChannels,
                        audioFormat, bufferSizeBytes,
                        AudioTrack.MODE_STREAM);

                // start audio
                audioTrack.play();

                // synthesis loop
                while (isRunning) {
                    audioTrack.write(audioData, 0, dataSizeSamples);
                }
                audioTrack.stop();
                audioTrack.release();
            }
        };

        isRunning = true;
        runner.start();
    }

    public void Stop() {
        isRunning = false;
    }
}
