package com.al70b.core.misc;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Naseem on 5/12/2015.
 */
public class StorageOperations extends ContextWrapper {

    public StorageOperations(Context context) {
        super(context);
    }

    public String saveImageToInternalStorage(String name, Bitmap bitmap) {
        String thumbnailsDirPath = getFilesDir().getAbsolutePath().concat(AppConstants.THUMBNAILS_FOLDER_PATH);

        // Create imageDir
        File bitmapFile = new File(thumbnailsDirPath, name);

        Log.d("thumbnailsDirPath-write", thumbnailsDirPath);

        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(bitmapFile);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap.toString();
    }

    public Bitmap loadImageFromInternalStorage(String name) {
        Bitmap bitmap = null;
        String thumbnailsDirPath = getFilesDir().getAbsolutePath().concat(AppConstants.THUMBNAILS_FOLDER_PATH);

        // point the bitmap file
        File bitmapFile = new File(thumbnailsDirPath, name);
        Log.d("thumbnailsDirPath-read", thumbnailsDirPath);
        if (bitmapFile.exists()) {
            try {
                bitmap = BitmapFactory.decodeStream(new FileInputStream(bitmapFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return bitmap;
    }

    public Bitmap loadImageFromExternalStorage(String path) {
        Bitmap bitmap = null;

        // point the bitmap file
        File bitmapFile = new File(path);
        Log.d("fromExternal-read", path);
        if (bitmapFile.exists()) {
            try {
                bitmap = ImageHandler.decodeFile(bitmapFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public void writeDictionaryToStorage(Translator.TranslatorDictionary dictionary) {
        String thumbnailsDirPath = getFilesDir().getAbsolutePath().concat(AppConstants.ROOT_FOLDER_PATH);

        // Create file for translation
        File outputTranslation = new File(thumbnailsDirPath, AppConstants.TRANSLATION_FILE_NAME);

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {

            fos = new FileOutputStream(outputTranslation);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(dictionary);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                oos.close();
                fos.close();
            } catch (IOException ex) {
            }
        }
    }

    public Translator.TranslatorDictionary loadDictionaryFromStorage() {
        String thumbnailsDirPath = getFilesDir().getAbsolutePath().concat(AppConstants.ROOT_FOLDER_PATH);

        // Create file for translation
        File inputTranslation = new File(thumbnailsDirPath, AppConstants.TRANSLATION_FILE_NAME);

        Translator.TranslatorDictionary dictionary;

        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(inputTranslation);
            ois = new ObjectInputStream(fis);
            dictionary = (Translator.TranslatorDictionary) ois.readObject();
        } catch (Exception e) {
            dictionary = null;
        } finally {
            try {
                if (ois != null)
                    ois.close();

                if (fis != null)
                    fis.close();
            } catch (IOException ex) {

            }
        }

        return dictionary;
    }
}
