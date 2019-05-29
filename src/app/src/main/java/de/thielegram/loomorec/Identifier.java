package de.thielegram.loomorec;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import AlizeSpkRec.AlizeException;
import AlizeSpkRec.IdAlreadyExistsException;
import AlizeSpkRec.SimpleSpkDetSystem;
import AlizeSpkRec.SimpleSpkDetSystem.SpkRecResult;

class Identifier {
    private static final Identifier sInstance = new Identifier();

    static Identifier getInstance() {
        return sInstance;
    }

    private Identifier() {
    }

    /**
     * Initializes the alize system if it hasn't already been initialized.
     * Loads teh background model, as well as the registered speakers.
     *
     * @param ctx Context taken from the MainActivity. Necessary to access files stored in the assets folder.
     * @throws IOException Exception thrown if one of the assets could not be loaded
     * @throws AlizeException Exception thrown when a problem inside of the alize system occurs
     */
    void initializeSystem(Context ctx) throws IOException, AlizeException {

        if(alizeSystem != null) {
            return;
        }

        InputStream configAsset = ctx.getAssets().open("Configuration.cfg");
        alizeSystem = new SimpleSpkDetSystem(configAsset, ctx.getFilesDir().getPath());
        configAsset.close();

        InputStream backgroundModelAsset = ctx.getAssets().open("gmm/world.gmm");
        alizeSystem.loadBackgroundModel(backgroundModelAsset);
        backgroundModelAsset.close();

        mDatabaseHelper = new DatabaseHelper(ctx);
        loadSpeakers();
    }

    private SimpleSpkDetSystem alizeSystem = null;
    private DatabaseHelper mDatabaseHelper = null;

    String[] getSpeakerList() throws AlizeException {
        return alizeSystem.speakerIDs();
    }

    /**
     * Registers a new speaker.
     * Features are extracted from the given voice sample and stored under the given name.
     * The speaker's name as displayed in-app, along with the file's name his features are stored in is saved to the database.
     *
     * @param voiceSample Audio data containing the recorded voice sample
     * @param displayName Speakers name as it is displayed in the application
     * @throws AlizeException Exception thrown when a problem inside of the alize system occurs
     * @throws IdAlreadyExistsException Exception thrown if a speaker is already registered under the given name
     */
    void addSpeaker(short[] voiceSample, String displayName) throws AlizeException, IdAlreadyExistsException {
        alizeSystem.addAudio(voiceSample);
        alizeSystem.createSpeakerModel(displayName);

        String fileName = displayName.replace(" ", "");

        alizeSystem.saveSpeakerModel(displayName, fileName);
        mDatabaseHelper.addData(displayName, fileName);

        alizeSystem.resetAudio();
        alizeSystem.resetFeatures();
    }

    /**
     * Loads list of registered speakers.
     *
     * @throws IOException Exception thrown when the system has trouble loading a speaker's feature file
     * @throws AlizeException Exception thrown when a problem inside of the alize system occurs
     */
    private void loadSpeakers() throws IOException, AlizeException {
        HashMap<String, String> speakerList = mDatabaseHelper.getSpeakers();
        for(String key : speakerList.keySet()) {
            alizeSystem.loadSpeakerModel(speakerList.get(key), key);
        }
    }

    /**
     * Removes a chosen speaker from the system.
     * Removes the entry from both the alizeSystem and the database.
     *
     * @param displayName Name of the speaker as it is displayed in the application
     * @throws AlizeException Exception thrown when a problem inside of the alize system occurs
     */
    void removeSpeaker(String displayName) throws AlizeException {
        alizeSystem.removeSpeaker(displayName);
        mDatabaseHelper.removeData(displayName);
    }

    /**
     * Identifies a speaker, based on the given voice sample.
     *
     * @param voiceSample Audio data containing the recorded voice sample
     * @return Result of the identification, containing the identified speaker, a confidence value and a choice,
     * whether the confidence value has passed a given threshold.
     * @throws AlizeException Exception thrown when a problem inside of the alize system occurs
     */
    SpkRecResult identifySpeaker(short[] voiceSample) throws AlizeException {

        alizeSystem.addAudio(voiceSample);
        SpkRecResult output = alizeSystem.identifySpeaker();

        alizeSystem.resetAudio();
        alizeSystem.resetFeatures();

        return output;
    }
}
