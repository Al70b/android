package com.al70b.core.objects;

import android.content.Context;
import android.util.Log;

import com.al70b.R;
import com.al70b.core.misc.JSONHelper;
import com.al70b.core.misc.Translator;
import com.inscripts.keys.StatusOption;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class User implements Serializable {


    // each user has a unique ID
    protected int userID;

    // name of the user
    protected String name;

    // email of the user
    protected String email;

    // address containing basically the country and city of the user
    protected Address address;

    // user's date of birth
    protected Calendar dateOfBirth;

    // object representing the characteristics of the user
    protected Characteristics userChar;

    // picture object representing user's profile picture
    protected Picture profilePicture;

    // list of user's pictures object - not the pictures themselves
    protected List<Picture> pictures;

    // object representing user's gender, this field can have only male or female values, constricted via ui
    protected Gender gender;

    // object representing user's interests
    protected UserInterest userInterest;

    // user online status
    protected OnlineStatus onlineStatus;

    // user status message
    protected String statusMessage;

    protected transient Context context;

    // for register new user only
    public User() {

    }

    public User(Context context) {
        this.context = context;

        // initialize pictures list and profile picture object
        this.pictures = new ArrayList<>();
        this.onlineStatus = new OnlineStatus(StatusOption.AVAILABLE);
    }

    public User(String email, String password, String name, String country, Gender gender,
                Calendar dateOfBirth, Gender lookingFor) {
        this(name, email, new Address("", country), gender, dateOfBirth, new UserInterest().setGenderInterest(lookingFor));
    }

    public User(String name, String email, String password,
                String city, String country, Gender gender, Calendar dateOfBirth, UserInterest userInterest) {
        this(name, email, new Address(city, country), gender, dateOfBirth, userInterest);
    }

    public User(String name, String email, Address address, Gender gender,
                Calendar dateOfBirth, UserInterest userInterest) {
        this.name = name;
        this.email = email;
        this.address = address;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.userInterest = userInterest;

        // initialize pictures list and profile picture object
        this.pictures = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public int getUserID() {
        return userID;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setAddress(String city, String country) {
        this.address = new Address(city, country);
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        if (gender.getValue() != Gender.BOTH)
            this.gender = gender;
    }

    public Calendar getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Calendar dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public UserInterest getUserInterest() {
        return userInterest;
    }

    public void setUserInterest(UserInterest userInterest) {
        this.userInterest = userInterest;
    }

    public List<Picture> getPicturesList() {
        return pictures;
    }

    public void addPhoto(Picture picture) {
        pictures.add(picture);
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Picture getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(int newProfilePictureId) {
        if (profilePicture != null)
            profilePicture.isProfilePicture = false;

        for (Picture pic : pictures) {
            if (pic.getId() == newProfilePictureId) {
                this.profilePicture = pic;
                profilePicture.isProfilePicture = true;
                break;
            }
        }
    }

    public boolean isProfilePictureSet() {
        return profilePicture != null;
    }

    public Characteristics getUserChar() {
        return userChar;
    }

    public void setUserChar(Characteristics userChar) {
        this.userChar = userChar;
    }

    public OnlineStatus getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(StatusOption status) {
        onlineStatus.setStatus(status);
    }

    public void setOnlineStatus(String status) {
        onlineStatus.setStatus(status);
    }

    public void setContext(Context context) {
        this.context = context.getApplicationContext();
    }


    @Override
    public String toString() {
        return "User ID: " + userID + "\tName: " + name +
                "\nEmail: " + email +
                "\n" + address +
                "\nGender: " + gender +
                "\nDate of Birth: " + dateOfBirth +
                "\nProfile picture: " + profilePicture +
                "\nPhotos: " + (pictures == null ? "none" : pictures.toString()) +
                "\nInterests\n" + userInterest +
                "\nSefat: " + (userChar == null ? "none" : userChar.toString());
    }

    public JSONObject parseUserToJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            // put user basic data
            jsonObject.put(JSONHelper.SERVER_NAME, name);
            jsonObject.put(JSONHelper.SERVER_GENDER, gender.getValue());
            jsonObject.put(JSONHelper.SERVER_BIRTH_DATE, dateOfBirth);
            jsonObject.put(JSONHelper.SERVER_CITY, address.getCity());
            jsonObject.put(JSONHelper.SERVER_COUNTRY, address.getCountry());

            // put user interest
            jsonObject.put(JSONHelper.SERVER_MATCH_GENDER, userInterest.getGenderInterest().getValue());

            // put user advanced data
            jsonObject.put(JSONHelper.SERVER_HEIGHT, userChar.getHeight());
            jsonObject.put(JSONHelper.SERVER_BODY, userChar.getBody());
            jsonObject.put(JSONHelper.SERVER_EYES, userChar.getEyes());
            jsonObject.put(JSONHelper.SERVER_WORK, userChar.getWork());
            jsonObject.put(JSONHelper.SERVER_EDUCATION, userChar.getEducation());
            jsonObject.put(JSONHelper.SERVER_RELIGION, userChar.getReligion());
            jsonObject.put(JSONHelper.SERVER_ALCOHOL, userChar.getAlcohol());
            jsonObject.put(JSONHelper.SERVER_SMOKING, userChar.getSmoking());
            jsonObject.put(JSONHelper.SERVER_DESCRIPTION, userChar.getDescription());

        } catch (JSONException ex) {
            Log.d("JSONException - User", ex.toString());
        }

        return jsonObject;
    }

    public User parseJSONToUser(JSONObject jsonObject) {
        try {
            Translator translator = Translator.getInstance(context);

            // parse basic user data
            name = jsonObject.getString(JSONHelper.SERVER_NAME);
            gender = new Gender(Integer.parseInt(jsonObject.getString(JSONHelper.SERVER_GENDER)));
            dateOfBirth = parseDate(jsonObject.getString(JSONHelper.SERVER_BIRTH_DATE));

            if (jsonObject.has(JSONHelper.SERVER_COUNTRY))
                address = new Address(jsonObject.getString(JSONHelper.SERVER_CITY),
                        translator.translate(jsonObject.getString(JSONHelper.SERVER_COUNTRY), translator.getDictionary().COUNTRIES));

            // parse user's interest
            userInterest = new UserInterest();
            if (jsonObject.has(JSONHelper.SERVER_MATCH_GENDER))
                userInterest.setGenderInterest(new Gender(jsonObject.getInt(JSONHelper.SERVER_MATCH_GENDER)));

            // parse user's list of pictures
            if (jsonObject.has(JSONHelper.SERVER_PHOTOS)) {
                // get user's list of photos and profile picture
                JSONArray jsonArrayPictures = jsonObject.getJSONArray(JSONHelper.SERVER_PHOTOS);
                pictures = parseJSONArrayToPictures(jsonArrayPictures);
            }

            // parse advanced user data
            String height, body, eyes, alcohol, smoking, work, education, religion, description;
            height = body = eyes = alcohol = smoking = work = education = religion = description = null;

            if (jsonObject.has(JSONHelper.SERVER_HEIGHT))
                height = jsonObject.getString(JSONHelper.SERVER_HEIGHT);
            if (jsonObject.has(JSONHelper.SERVER_BODY))
                body = translator.translate(jsonObject.getString(JSONHelper.SERVER_BODY),
                        translator.getDictionary().CHARACTERS.get(JSONHelper.SERVER_BODY));
            if (jsonObject.has(JSONHelper.SERVER_EYES))
                eyes = translator.translate(jsonObject.getString(JSONHelper.SERVER_EYES),
                        translator.getDictionary().CHARACTERS.get(JSONHelper.SERVER_EYES));
            if (jsonObject.has(JSONHelper.SERVER_WORK))
                work = jsonObject.getString(JSONHelper.SERVER_WORK);
            if (jsonObject.has(JSONHelper.SERVER_EDUCATION))
                education = translator.translate(jsonObject.getString(JSONHelper.SERVER_EDUCATION),
                        translator.getDictionary().CHARACTERS.get(JSONHelper.SERVER_EDUCATION));
            if (jsonObject.has(JSONHelper.SERVER_RELIGION))
                religion = translator.translate(jsonObject.getString(JSONHelper.SERVER_RELIGION),
                        translator.getDictionary().CHARACTERS.get(JSONHelper.SERVER_RELIGION));
            if (jsonObject.has(JSONHelper.SERVER_ALCOHOL))
                alcohol = translator.translate(jsonObject.getString(JSONHelper.SERVER_ALCOHOL),
                        translator.getDictionary().CHARACTERS.get(JSONHelper.SERVER_ALCOHOL));
            if (jsonObject.has(JSONHelper.SERVER_SMOKING))
                smoking = translator.translate(jsonObject.getString(JSONHelper.SERVER_SMOKING),
                        translator.getDictionary().CHARACTERS.get(JSONHelper.SERVER_SMOKING));

            try {
                if (jsonObject.has(JSONHelper.SERVER_DESCRIPTION))
                    description = URLDecoder.decode(jsonObject.getString(JSONHelper.SERVER_DESCRIPTION), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                description = "";
            }

            userChar = new Characteristics(context.getResources(), height, body, eyes,
                    alcohol, smoking, work, education, religion, description);

            if (onlineStatus == null) {
                onlineStatus = new OnlineStatus(StatusOption.AVAILABLE);
            }

        } catch (JSONException ex) {
            Log.d("JSONExc-UserParse", ex.toString());
        }

        return this;
    }


    protected Calendar parseDate(String date) {
        int year, month, day;
        String c = "/";
        try {
            if (!date.contains(c))
                c = "-";
            String[] dateArr = date.split(c);
            year = Integer.parseInt(dateArr[0]);
            month = Integer.parseInt(dateArr[1]) - 1;
            day = Integer.parseInt(dateArr[2]);
        } catch (Exception ex) {
            year = month = day = 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return cal;
    }

    private List<Picture> parseJSONArrayToPictures(JSONArray jsonArray) throws JSONException {
        Picture pic;

        // initialize empty list of pictures
        pictures = new ArrayList<>();

        // parse pictures json from user to list of json objects
        List<JSONObject> listOfJSONS = JSONHelper.parseJSONArray(jsonArray);

        for (JSONObject j : listOfJSONS) {
            // parse each json object to a picture
            pic = Picture.parseJSONToPicture(j);

            // in case this picture is a profile picture
            if (pic.isProfilePicture()) {
                profilePicture = pic;
            }

            // add picture to the list of pictures of the user
            pictures.add(pic);
        }

        return pictures;
    }

    // class that represents gender
    public static class Gender implements Serializable {

        public static final int NOT_SET = 0;
        public static final int MALE = 1;
        public static final int FEMALE = 2;
        public static final int BOTH = 3;

        private int value;

        public Gender(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }


        public String toString(Context context) {
            Translator translator = Translator.getInstance(context);
            return translator.translate(value,
                    translator.getDictionary().GENDER);
        }
    }

    // class that represents status
    public static class OnlineStatus implements Serializable {

        private StatusOption status;
        private int resID;

        public OnlineStatus(StatusOption status) {
            this.status = status;
            setResourceID();
        }

        public OnlineStatus(String status) {
            this.status = stringToStatusOption(status);
            setResourceID();
        }

        public StatusOption getStatus() {
            return status;
        }

        public int getResourceID() {
            return resID;
        }

        public static StatusOption stringToStatusOption(String valueStr) {
            StatusOption status;

            if (valueStr.compareTo(JSONHelper.SERVER_STATUS_OFFLINE) == 0) {
                status = StatusOption.OFFLINE;
            } else if (valueStr.compareTo(JSONHelper.SERVER_STATUS_AVAILABLE) == 0) {
                status = StatusOption.AVAILABLE;
            } else if (valueStr.compareTo(JSONHelper.SERVER_STATUS_AWAY) == 0) {
                status = StatusOption.INVISIBLE;
            } else if (valueStr.compareTo(JSONHelper.SERVER_STATUS_BUSY) == 0) {
                status = StatusOption.BUSY;
            } else {
                status = null;
            }

            return status;
        }
/*
        public OnlineStatus duplicate() {
            return new OnlineStatus(status);
        }*/


        private void setStatus(StatusOption status) {
            this.status = status;
            setResourceID();
        }

        private void setStatus(String statusStr) {
            setStatus(stringToStatusOption(statusStr));
        }

        private void setResourceID() {
            switch (status) {
                case AVAILABLE:
                    resID = R.drawable.status_online;
                    break;
                case OFFLINE:
                    resID = R.drawable.status_offline;
                    break;
                case INVISIBLE:
                    resID = R.drawable.status_away;
                    break;
                case BUSY:
                    resID = R.drawable.status_busy;
                    break;
            }
        }

        @Override
        public String toString() {
            String str = "Offline";

            switch (status) {
                case AVAILABLE:
                    str = "Available";
                    break;
                case OFFLINE:
                    str = "Offline";
                    break;
                case INVISIBLE:
                    str = "Invisible";
                    break;
                case BUSY:
                    str = "Busy";
            }
            return str;
        }
    }
}
