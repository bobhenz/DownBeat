package com.bobhenz.downbeat;

import android.util.Log;

/**
 * Created by bhenz on 1/31/2016.
 */
public class Metronome {
    public static final int MAX_BEATS_PER_MINUTE = 200;

    private int beatsPerMeasure;
    private int tempoBeatsPerMinute;
    private int samplesPerSecond;

    private BeatSoundInformation[] beatSoundArray;
    private short[] fullMeasureSound;
    private int fullMeasureSoundSizeSamples;
    private int sampleSizeBytes;
    private LoopingAudioThread looper;
    private int samplesPerBeat;

    private final class BeatSoundInformation {
        float[] data;
        float volume;
    }

    public Metronome(int beatsPerMeasure, int tempoBeatsPerMinute) {
        this.samplesPerSecond = 44100;
        if (beatsPerMeasure < 2)
            beatsPerMeasure = 2;
        if (beatsPerMeasure > 6)
            beatsPerMeasure = 6;
        this.beatsPerMeasure = beatsPerMeasure;
        this.sampleSizeBytes = 1;
        this.beatSoundArray = new BeatSoundInformation[beatsPerMeasure];

        SetTempo(tempoBeatsPerMinute);
        SetBeatSound(0, PrefabSounds.ID.CLICK, 1f);
        for (int beat = 1; beat < beatsPerMeasure; beat++) {
            SetBeatSound(beat, PrefabSounds.ID.CLICK, 0.5f);
        }
    }

    public void SetTempo(int beatsPerMinute) {
        if (tempoBeatsPerMinute > MAX_BEATS_PER_MINUTE)
            tempoBeatsPerMinute = MAX_BEATS_PER_MINUTE;

        this.tempoBeatsPerMinute = beatsPerMinute;

        // For convenience, calculate a few values.
        double beatsPerSecond = (double)tempoBeatsPerMinute / 60.0;
        this.samplesPerBeat = (int)Math.round((double) samplesPerSecond / beatsPerSecond);
    }

    public void SetBeatSound(int beatIndex, PrefabSounds.ID prefabId, float volume) {
        BeatSoundInformation beat = new BeatSoundInformation();
        beat.data = PrefabSounds.GeneratePrefabSound(prefabId, samplesPerSecond);
        beat.volume = volume;
        beatSoundArray[beatIndex] = beat;
    }

    private void BuildFullMeasureSound() {
        fullMeasureSoundSizeSamples = samplesPerBeat * beatsPerMeasure;

        fullMeasureSound = new short[fullMeasureSoundSizeSamples];

        Log.d("msg", String.format("Created buffer of size %d", fullMeasureSoundSizeSamples));
        for (int index = 0; index < fullMeasureSound.length; index++) {
            fullMeasureSound[index] = 0;
        }

        int dstBeatOffset = 0;
        for (int beat = 0; beat < beatsPerMeasure; beat++) {
            float[] soundData = beatSoundArray[beat].data;
            float volume = beatSoundArray[beat].volume;
            int dstIndex = 0;
            for (int sample = 0; sample < soundData.length; sample++) {
                // Scale by volume and then into a short value.
                short val = (short)(32767.0 * soundData[sample] * volume);

                // In 16-bit wav PCM, first byte is the low order byte
                fullMeasureSound[dstBeatOffset + dstIndex] = val;
                dstIndex++;
            }
            dstBeatOffset += samplesPerBeat;
        }
    }

    public void Start() {
        Stop();
        Log.d("msg", "Starting");
        if (fullMeasureSound == null) {
            Log.d("msg", "Calling Build");
            BuildFullMeasureSound();
        }

        looper = new LoopingAudioThread(fullMeasureSound, fullMeasureSoundSizeSamples, samplesPerSecond);
        looper.Start();

    }

    public void Stop() {
        if (looper != null) {
            looper.Stop();
            looper = null;
        }
    }
}
