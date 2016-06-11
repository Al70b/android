package com.al70b.core.misc;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Naseem on 6/21/2015.
 */
public class ImageHandler {


    public static final int MAX_FILE_SIZE = 350;


    public static void scaleImage(Resources res, ImageView view, float boundBox, boolean scaled) {
        int boundBoxInDp = (int) (boundBox / Resources.getSystem().getDisplayMetrics().density);

        // Get the ImageView and its bitmap
        Drawable drawing = view.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) drawing).getBitmap();

        // Get current dimensions
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // turn height and width to dp units
        width = (int) (width / Resources.getSystem().getDisplayMetrics().density);
        height = (int) (height / Resources.getSystem().getDisplayMetrics().density);

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = boundBoxInDp / width;
        float yScale = boundBoxInDp / height;

        float scale = (xScale <= yScale) ? xScale : yScale;

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        if (scaled)
            matrix.postScale(scale, scale);
        else
            matrix.postScale(xScale, yScale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        BitmapDrawable result = new BitmapDrawable(res, scaledBitmap);
        width = scaledBitmap.getWidth();
        height = scaledBitmap.getHeight();

        // Apply the scaled bitmap
        view.setImageDrawable(result);

        ViewParent parent = view.getParent();

        if (parent instanceof RelativeLayout) {
            // Now change ImageView's dimensions to match the scaled image
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
            params.width = width;
            params.height = height;
            view.setLayoutParams(params);
        } else if (parent instanceof LinearLayout) {
            // Now change ImageView's dimensions to match the scaled image
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
            params.width = width;
            params.height = height;
            view.setLayoutParams(params);
        }
    }

    public static void scaleImage(Resources res, ImageView view, float boundXInDp, float boundYInDp) {

    }


    public static Bitmap decodeFile(InputStream is, InputStream secondIS) throws FileNotFoundException {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, o);


            // The new size we want to scale to
            final int REQUIRED_SIZE = MAX_FILE_SIZE;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap bitmap = BitmapFactory.decodeStream(secondIS, null, o2);

            return bitmap;
        } finally {
            try {
                is.close();
                secondIS.close();
            } catch (IOException ex) {
            }
        }
    }

    public static Bitmap decodeFile(File f) throws FileNotFoundException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(f);
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = MAX_FILE_SIZE;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);

            return bitmap;
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException ex) {
            }
        }
    }

    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String imageFileName = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());

        File storageDir;

        if (Environment.isExternalStorageEmulated()) {
            storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            storageDir = new File(storageDir.getAbsolutePath().concat(AppConstants.ROOT_FOLDER_PATH));
        } else
            storageDir = new File(context.getFilesDir().getAbsolutePath().concat(AppConstants.IMAGES_FOLDER_PATH));


        if (!storageDir.exists())
            storageDir.mkdirs();

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir     /* directory */
        );

        return image;
    }


}
