package com.al70b.core.misc;

import java.security.MessageDigest;

/**
 * Taken by Naseem on 5/4/2015.
 * http://runnable.com/U8lo-rXJWGlhL-OG/sha512-for-java
 */
public class SHA512 {

    public static String convertByteToHex(byte data[]) {
        StringBuffer hexData = new StringBuffer();
        for (int byteIndex = 0; byteIndex < data.length; byteIndex++)
            hexData.append(Integer.toString((data[byteIndex] & 0xff) + 0x100, 16).substring(1));

        return hexData.toString();
    }

    public static String hashText(String textToHash) throws Exception {
        final MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
        sha512.update(textToHash.getBytes());

        return convertByteToHex(sha512.digest());
    }
}