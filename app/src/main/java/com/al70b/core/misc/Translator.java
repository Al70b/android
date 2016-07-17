package com.al70b.core.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.al70b.R;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Naseem on 5/23/2015.
 * singleton class representing translations object
 */
public class Translator implements Serializable {

    public static Translator translator;
    private transient Context context;
    private TranslatorDictionary dictionary;
    private Word NOT_SPECIFIED;

    // private to make singleton
    private Translator(Context context) {
        this.context = context.getApplicationContext();
    }

    public static Translator getInstance(Context context) {

        if (translator == null)      // if hasn't been created yet
            translator = new Translator(context);

        if (translator.getDictionary() == null) {    // if dictionary hasn't been initialized yet
            initializeDictionary(context);
        }

        // return singleton
        return translator;
    }

    private static void initializeDictionary(Context context) {

        RequestsInterface requests = new RequestsInterface(context.getApplicationContext());

        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        String localDateStr = sharedPreferences.getString(AppConstants.TRANSLATION_SHARED_PREF_KEY, null);

        // get translation from server
        ServerResponse<JSONObject> sr = requests.getTranslations();
        if (sr != null && sr.isSuccess()) {
            // translation was received successfully from server
            String serverDateStr;
            Date localDate, serverDate;
            serverDate = null;

            try {
                // get server's translation last update
                serverDateStr = sr.getResult().getString(KEYS.SERVER.TRANSLATION_DATE);

                if (serverDateStr != null) {
                    // parse last update to a date object
                    serverDate = new SimpleDateFormat(KEYS.SERVER.TRANSLATION_DATE_FORMAT).parse(serverDateStr);
                }

                if (localDateStr != null) {
                    // compare translations date and use the most recent one
                    // parse local date to a date object
                    localDate = new SimpleDateFormat(KEYS.SERVER.TRANSLATION_DATE_FORMAT).parse(localDateStr);

                    // compare
                    if (localDate.compareTo(serverDate) == 0) {
                        // translation up to date, use last saved and return
                        translator.useLastSavedTranslation();
                        //return;
                    }
                }

                /* either local translation is not up to date or no local translation is set
                  use server's translation */

                // write down date of this update
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(AppConstants.TRANSLATION_SHARED_PREF_KEY, serverDateStr);
                editor.commit();

                // parse the translation
                translator.useServerTranslations(sr.getResult());

            } catch (JSONException ex) {
            } catch (ParseException ex) {
            }
        } else if (localDateStr != null) {      // could not connect to server
            // a previous translation was saved, use it
            translator.useLastSavedTranslation();
        } else {
            // use the default translation
            translator.useDefaultTranslation();
        }
    }

    public TranslatorDictionary getDictionary() {
        return dictionary;
    }

    public List<String> getValues(List<Word> listOfWords, boolean sorted) {
        List<String> values = new ArrayList<>();

        for (Word word : listOfWords) {
            values.add(word.getArabic());
        }

        if (sorted)
            // sort the values
            Collections.sort(values);

        return values;
    }


    public String translate(int i, List<Word> dic) {
        return translate(String.valueOf(i), dic);
    }

    public String translate(String s, List<Word> dic) {
        if (s == null || s.compareTo("null") == 0)
            return null;

        StringManp manp = new StringManp();

        if (manp.isInEnglish(s)) {
            for (Word w : dic) {
                if (w.getEnglish().compareTo(s) == 0)
                    return w.getArabic();
            }
        } else {
            for (Word w : dic) {
                if (w.getArabic().compareTo(s) == 0)
                    return w.getEnglish();
            }
        }

        return "";
    }

    public ArrayList<String> translate(List<String> list, List<Word> dic) {
        ArrayList<String> translated = new ArrayList<String>();
        for (String s : list) {
            translated.add(translate(s, dic));
        }

        return translated;
    }

    public ArrayList<String> translate(JSONArray arr, List<Word> dic) {
        List<String> list = new ArrayList<>();

        for (int i = 0; i < arr.length(); i++) {
            try {
                list.add(arr.getString(i));
            } catch (JSONException ex) {
            }
        }
        return translate(list, dic);
    }

    public void useServerTranslations(JSONObject jsonObject) throws JSONException {
        // initialize dictionary
        this.dictionary = new TranslatorDictionary();

        NOT_SPECIFIED = new Word("Not Specified", context.getString(R.string.not_specified));

        JSONObject temp;

        // fill dictionary with translations
        // gender
        temp = jsonObject.getJSONObject(KEYS.SERVER.MATCH_GENDER);
        dictionary.GENDER.add(new Word(KEYS.SERVER.GENDER_MALE, temp.getString(KEYS.SERVER.GENDER_MALE)));
        dictionary.GENDER.add(new Word(KEYS.SERVER.GENDER_FEMALE, temp.getString(KEYS.SERVER.GENDER_FEMALE)));
        dictionary.GENDER.add(new Word(KEYS.SERVER.GENDER_BOTH, temp.getString(KEYS.SERVER.GENDER_BOTH)));

        // relationships
        temp = jsonObject.getJSONObject(KEYS.SERVER.INTERESTED_PURPOSE);
        fillListWithValues(temp, dictionary.RELATIONSHIP);

        // social status
        temp = jsonObject.getJSONObject(KEYS.SERVER.SOCIAL_STATUS);
        fillListWithValues(temp, dictionary.SOCIAL_STATUS);

        // countries
        temp = jsonObject.getJSONObject(KEYS.SERVER.COUNTRY);
        fillListWithValues(temp, dictionary.COUNTRIES);


        // characters
        // body
        temp = jsonObject.getJSONObject(KEYS.SERVER.BODY);
        dictionary.CHARACTERS.put(KEYS.SERVER.BODY, fillListWithValues(temp, new ArrayList<Word>()));
        dictionary.CHARACTERS.get(KEYS.SERVER.BODY).add(0, NOT_SPECIFIED);

        // eyes
        temp = jsonObject.getJSONObject(KEYS.SERVER.EYES);
        dictionary.CHARACTERS.put(KEYS.SERVER.EYES, fillListWithValues(temp, new ArrayList<Word>()));
        dictionary.CHARACTERS.get(KEYS.SERVER.EYES).add(0, NOT_SPECIFIED);

        // education
        temp = jsonObject.getJSONObject(KEYS.SERVER.EDUCATION);
        dictionary.CHARACTERS.put(KEYS.SERVER.EDUCATION, fillListWithValues(temp, new ArrayList<Word>()));
        dictionary.CHARACTERS.get(KEYS.SERVER.EDUCATION).add(0, NOT_SPECIFIED);

        // religion
        temp = jsonObject.getJSONObject(KEYS.SERVER.RELIGION);
        dictionary.CHARACTERS.put(KEYS.SERVER.RELIGION, fillListWithValues(temp, new ArrayList<Word>()));
        dictionary.CHARACTERS.get(KEYS.SERVER.RELIGION).add(0, NOT_SPECIFIED);

        // alcohol
        temp = jsonObject.getJSONObject(KEYS.SERVER.ALCOHOL);
        dictionary.CHARACTERS.put(KEYS.SERVER.ALCOHOL, fillListWithValues(temp, new ArrayList<Word>()));
        dictionary.CHARACTERS.get(KEYS.SERVER.ALCOHOL).add(0, NOT_SPECIFIED);

        // smoking
        temp = jsonObject.getJSONObject(KEYS.SERVER.SMOKING);
        dictionary.CHARACTERS.put(KEYS.SERVER.SMOKING, fillListWithValues(temp, new ArrayList<Word>()));
        dictionary.CHARACTERS.get(KEYS.SERVER.SMOKING).add(0, NOT_SPECIFIED);

        // write dictionary to a file for future use
        new StorageOperations(context).writeDictionaryToStorage(dictionary);

        Log.v("Translation", "Using server's translation.");
    }

    public void useLastSavedTranslation() {
        // load translation from file in storage
        dictionary = new StorageOperations(context).loadDictionaryFromStorage();

        Log.v("Translation", "Using last saved translation.");
    }

    public void useDefaultTranslation() {
        String[] temp = context.getResources().getStringArray(R.array.translation_relationship);

        // initialize dictionary
        this.dictionary = new TranslatorDictionary();

        // fill dictionary with default values
        for (String s : temp)
            dictionary.RELATIONSHIP.add(new Word(s.hashCode() + "", s));

        temp = context.getResources().getStringArray(R.array.translation_social_status);

        for (String s : temp)
            dictionary.SOCIAL_STATUS.add(new Word(s.hashCode() + "", s));

        Log.v("Translation", "Using default translation.");
    }

    private List<Word> fillListWithValues(JSONObject jsonObject, List<Word> listOfWords) throws JSONException {
        Iterator<String> jsonKeys = jsonObject.keys();

        String key;
        while (jsonKeys.hasNext()) {
            key = jsonKeys.next();
            listOfWords.add(new Word(key, jsonObject.getString(key)));
        }

        return listOfWords;
    }

    /**
     * Each translator has its own Dictionary object
     * Dictionary holds words and their translation
     */
    public class TranslatorDictionary implements Serializable {
        public final List<Word> GENDER = new ArrayList<Word>();

        // possible relationships translations
        public final List<Word> RELATIONSHIP = new ArrayList<Word>();

        // user character's translations
        public final Map<String, List<Word>> CHARACTERS = new HashMap<String, List<Word>>();

        // user social status translations
        public final List<Word> SOCIAL_STATUS = new ArrayList<Word>();

        // countries translations
        public final List<Word> COUNTRIES = new ArrayList<Word>();
    }

    public class Word implements Serializable {

        public String english;
        public String arabic;

        public Word(String english, String arabic) {
            this.english = english;
            this.arabic = arabic;
        }

        public String getArabic() {
            return arabic;
        }

        public String getEnglish() {
            return english;
        }
    }

}
