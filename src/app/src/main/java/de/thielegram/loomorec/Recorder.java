package de.thielegram.loomorec;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.Arrays;

class Recorder {

    private final static int[] mSampleRates = new int[] { 44100, 22050, 11025, 8000 };

    private AudioTrack track = null;
    private Thread playbackThread = null;

    private boolean isRecording = false;
    private AudioRecord recorder;
    private Thread recordingThread = null;
    private short[] recordingData = null;

    Recorder() {
    }

    /**
     * Iterates over all possible setups for the AudioRecord class and uses the first applicable combination.
     *
     * @return Initialized instance of the AudioRecord class.
     */
    private AudioRecord initializeRecorder() {

        for (int rate : mSampleRates) {
            try {

                int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

                if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                    AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

                    if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                        return recorder;
                }
            } catch (Exception e) {
                Log.e("NO MATCH", rate + "Exception, keep trying.",e);
            }

            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT }) {
                for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                        Log.e("NO MATCH", rate + "Exception, keep trying.",e);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Starts the recording of a new voice sample.
     * A new instance of the AndroidRecorder class is initiated and the recorded data is reset.
     * In a new thread, the recorded data is written into an array.
     */
    void startRecording() {
        recorder = initializeRecorder();
        recordingData = new short[0];

        assert recorder != null;
        recorder.startRecording();

        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeRecordingDataToArray();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    /**
     * Reads the data recorded by the recorder and adds it to the array recordingData.
     */
    private void writeRecordingDataToArray() {

        int BUFFER_ELEMENTS_TO_REC = 1024;
        short[] sData = new short[BUFFER_ELEMENTS_TO_REC];

        while (isRecording) {
            recorder.read(sData, 0, BUFFER_ELEMENTS_TO_REC);
            System.out.println("Short writing to file" + Arrays.toString(sData));
            recordingData = combineShortArray(recordingData, sData);
        }
    }

    /**
     * Takes two arrays of shorts and returns a combined array
     *
     * @param firstArray First array to be added to the output array
     * @param secondArray Second array to be added to the output array
     * @return Combined array that contains the contents of firstArray, followed by the contents of secondArray
     */
    private short[] combineShortArray(short[] firstArray, short[] secondArray) {
        int l1 = firstArray.length;
        int l2 = secondArray.length;
        short[] output = new short[l1 + secondArray.length];
        System.arraycopy(firstArray, 0, output, 0, l1);
        System.arraycopy(secondArray, 0, output, l1, l2);
        return output;
    }

    /**
     * Stops the current recording process.
     * The current instance of AudioRecorder and the additional thread are deleted.
     */
    void stopRecording() {
        isRecording = false;

        setUpPlayback();
        recorder.stop();
        recorder.release();

        recorder = null;
        recordingThread = null;

    }

    /**
     * Returns the latest recorded audio data.
     *
     * @return Array of shorts, that contains the audio data of the latest recording process.
     */
    short[] getRecording() {
        return recordingData;
    }

    private void setUpPlayback() {
        int streamType = AudioManager.USE_DEFAULT_STREAM_TYPE;
        int sampleRate = recorder.getSampleRate();
        int channel = AudioFormat.CHANNEL_OUT_MONO;
        int encoding = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioTrack.getMinBufferSize(sampleRate, channel, encoding);
        int mode = AudioTrack.MODE_STREAM;

        track = new AudioTrack(streamType, sampleRate, channel, encoding, bufferSize, mode);
    }

    void startPlayback() {
        if(track!=null) {
            playbackThread = new Thread(new Runnable() {
                public void run() {
                    track.write(recordingData, 0, recordingData.length);
                }
            }, "AudioPlayback Thread");

            playbackThread.start();
            track.play();
        }
    }

    void stopPlayback() {
        if(track!=null) {
            track.pause();
            track.flush();
        }
    }
}
