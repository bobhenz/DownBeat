package com.bobhenz.downbeat;

/**
 * Created by bhenz on 2/1/2016.
 */

/**
 * Static class for prefabricated sounds.
 * Sounds returned as arrays of floats that range from -1..+1 in magnitude.
 * This allows the result to be independent of whether you are using
 * 8-bit or 16-bit PCM, and allows for easy scaling (i.e. volume).
 */
public class PrefabSounds {
    public enum ID {
        DEFAULT,
        CLICK,
    };

    public static float[] GeneratePrefabSound(ID prefabId, int samplesPerSecond) {
        switch (prefabId) {
            case DEFAULT:
            case CLICK:
            default:
                return PrefabSounds.GenerateClickSound(samplesPerSecond);
        } // switch
    }

    private static float[] GenerateClickSound(int samplesPerSecond) {
        int clickLength = (int)(0.01 * (double)samplesPerSecond);
        float[] soundBuffer = new float[clickLength];

        /* Create the click sound with three regions to avoid
         * sharp (static-inducing) transitions. (1/8) (6/8) (1/8)*/

        /* 1: smooth transition from 0..-1
         * To accomplish this we use an offset-n-scaled cosine from 0 to PI. */
        int remainingClickLength = clickLength;
        int regionLength;
        int bufferOffset = 0;

        regionLength = clickLength / 8;
        for (int index = 0; index < regionLength; index++) {
            double angleRadians = (double)index/(double)regionLength * Math.PI;
            double dValue = 0.5 * Math.cos(angleRadians) - 0.5;
            soundBuffer[index + bufferOffset] = (float)dValue;
        }
        remainingClickLength -= regionLength;
        bufferOffset += regionLength;

        /* 2: smooth transition from -1..+1
         * To accomplish this, we use and unscaled cosine from PI to 8*PI. */
        regionLength = (clickLength * 6) / 8;
        for (int index = 0; index < regionLength; index++) {
            double angleRadians = Math.PI + (double)index/(double)regionLength * 11.0 * Math.PI;
            double dValue = Math.cos(angleRadians);
            soundBuffer[index + bufferOffset] = (float)dValue;
        }
        remainingClickLength -= regionLength;
        bufferOffset += regionLength;

        /* 3: smooth transition from +1..0
         * To accomplish this we use an offset-n-scaled cosine from 0 to PI. */
        regionLength = remainingClickLength;
        for (int index = 0; index < regionLength; index++) {
            double angleRadians = (double)index/(double)regionLength * Math.PI;
            double dValue = 0.5 * Math.cos(angleRadians) + 0.5;
            soundBuffer[index + bufferOffset] = (float)dValue;
        }

        return soundBuffer;
    }
}
