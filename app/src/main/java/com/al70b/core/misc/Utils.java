package com.al70b.core.misc;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

/**
 * Created by nasee on 6/22/2016.
 */
public class Utils {

    public static Uri resourceToUri(Context context, int resID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(resID) + '/' +
                context.getResources().getResourceTypeName(resID) + '/' +
                context.getResources().getResourceEntryName(resID) );
    }

}
