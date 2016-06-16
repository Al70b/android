package com.al70b.core.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Naseem on 4/28/2015.
 */
public class StringManp {

    // english regex to use for checking if word is in english
    private static final String ENGLISH_REGEX = "^[a-zA-Z0-9]*$";
    public static String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    public static String PASSWORD_REGEX = "^([a-zA-Z0-9@*#]{5,15})$";
    // pattern object to make regex
    private Pattern pattern = Pattern.compile(ENGLISH_REGEX);

    /**
     * method is used for checking valid email userID format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isValidEmail(final String email) {
        boolean isValid = false;

        Pattern pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean isPasswordValid(final String password) {
        boolean isValid = false;

        Pattern pattern = Pattern.compile(PASSWORD_REGEX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(password);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * @param str The string to be checked
     * @return true if string is in english
     */
    public boolean isInEnglish(String str) {
        Matcher m = pattern.matcher(str);
        return m.find();
    }
}
