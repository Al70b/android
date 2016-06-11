package com.al70b.core.objects;

import java.io.Serializable;

public class UserBackup implements Serializable {
/*
    // name of the user
    private String name;

    // email and password, basically password is saved for future change password requests
    private String email, password;

    // each user has a user ID and an access token for requests, provided by the server
    private String userID, accessToken;

    // address containing basically the country and city of the user
    private Address address;

    // user's date of birth
    private Calendar dateOfBirth;

    // user's social status
    private String socialStatus;

    // object of characteristics of the user
    private Characteristics userChar;

    // profile picture of the user
    private ProfilePicture profilePicture;

    // list of paths to photos on the server
    private List<Picture> pictures;

    // enum type describing the user's gender, can have only male of female values
    private Gender gender;

    // object representing user's interests
    private UserInterest userInterest;

    // a pointer to this user
    private UserBackup thisUser = this;

    private transient Context context;

    private transient String status;

    private String friendStatus;

    // for register new user only
    public UserBackup() {

    }

    public UserBackup(Context context) {
        this.context = context;
    }

    // this is the src constructor that starts a user object
    public UserBackup(Context context, String userID, String accessToken, String email, ProfilePicture profilePicture) {
        this.context = context.getApplicationContext();
        this.userID = userID;
        this.accessToken = accessToken;
        this.email = email;
        this.profilePicture = profilePicture;
    }

    public UserBackup(String email, String password, String name, String country, Gender gender, Calendar dateOfBirth, Gender lookingFor) {
        this(name, email, password, new Address("", country), gender, "", dateOfBirth, new UserInterest().setGenderInterest(lookingFor));
    }

    public UserBackup(String name, String email, String password,
                      String city, String country, Gender gender, String socialStatus, Calendar dateOfBirth, UserInterest userInterest) {
        this(name, email, password, new Address(city, country), gender, socialStatus, dateOfBirth, userInterest);
    }

    public UserBackup(String nickname, String email, String password,
                      Address address, Gender gender, String socialStatus, Calendar dateOfBirth, UserInterest userInterest) {
        this.name = nickname;
        this.email = email;
        this.password = password;
        this.address = address;
        this.gender = gender;
        this.socialStatus = socialStatus;
        this.dateOfBirth = dateOfBirth;
        this.userInterest = userInterest;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
        if (gender != Gender.BOTH)
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

    public String getUserID() {
        return userID;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getSocialStatus() {
        return socialStatus;
    }

    public void setSocialStatus(String socialStatus) {
        this.socialStatus = socialStatus;
    }

    public ProfilePicture getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(ProfilePicture profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Characteristics getUserChar() {
        return userChar;
    }

    public void setUserChar(Characteristics userChar) {
        this.userChar = userChar;
    }

    public String getFriendStatus() {
        return friendStatus;
    }

    public void setFriendStatus(String friendStatus) {
        this.friendStatus = friendStatus;
    }

    @Override
    public String toString() {
        return "User ID: " + userID + "\tName: " + name +
                "\nAccess Token: " + accessToken +
                "\nEmail: " + email +
                "\nPassword: " + password +
                "\n" + address +
                "\nGender: " + gender +
                "\nDate of Birth: " + dateOfBirth +
                "\nSocial Status: " + socialStatus +
                "\nProfile picture: " + profilePicture +
                "\nPhotos: " + (pictures == null?  "none" : pictures.toString()) +
                "\nInterests\n" + userInterest +
                "\nSefat: " + (userChar == null? "none" : userChar.toString());
    }

    public JSONObject parseUserToJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            // put user basic data
            jsonObject.put(JSONHelper.SERVER_NAME, name);
            jsonObject.put(JSONHelper.SERVER_GENDER, gender.getGenderNum());
            jsonObject.put(JSONHelper.SERVER_BIRTH_DATE, dateOfBirth);
            jsonObject.put(JSONHelper.SERVER_SOCIAL_STATUS, socialStatus);
            jsonObject.put(JSONHelper.SERVER_CITY, address.getCity());
            jsonObject.put(JSONHelper.SERVER_COUNTRY, address.getCountry());

            // put user interest
            jsonObject.put(JSONHelper.SERVER_MATCH_GENDER, userInterest.getGenderInterest().getGenderNum());
            jsonObject.put(JSONHelper.SERVER_INTERESTED_PURPOSE, userInterest.getPurposesOfInterest().toString());

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

    public UserBackup parseJSONToUser(JSONObject jsonObject) {
        try {
            Translator translator = Translator.getInstance();

            // parse basic user data
            name = jsonObject.getString(JSONHelper.SERVER_NAME);
            gender = Gender.values()[Integer.parseInt(jsonObject.getString(JSONHelper.SERVER_GENDER))];
            dateOfBirth = parseDate(jsonObject.getString(JSONHelper.SERVER_BIRTH_DATE));
            socialStatus = translator.translate(jsonObject.getString(JSONHelper.SERVER_SOCIAL_STATUS), translator.getDictionary().SOCIAL_STATUS);
            address = new Address(jsonObject.getString(JSONHelper.SERVER_CITY),
                    translator.translate(jsonObject.getString(JSONHelper.SERVER_COUNTRY), translator.getDictionary().COUNTRIES));

            // parse user interest
            userInterest = new UserInterest();
            userInterest.setGenderInterest(Gender.getGender(jsonObject.getInt(JSONHelper.SERVER_MATCH_GENDER)));
            JSONArray jsonTemp = jsonObject.getJSONArray(JSONHelper.SERVER_INTERESTED_PURPOSE);
            List<String> temp = JSONHelper.parseJSONArray(jsonTemp);
            List<String> interestPurpose = translator.translate(temp, translator.getDictionary().RELATIONSHIP);

            userInterest.setPurposesOfInterest(interestPurpose);

            // get user's list of photos and profile picture
            JSONArray jsonArrayPictures = jsonObject.getJSONArray(JSONHelper.SERVER_PHOTOS);
            pictures = parseJSONArrayToPictures(jsonArrayPictures);


            // parse advanced user data
            String height, body, eyes, alcohol, smoking, work, education, religion, description;

            height = jsonObject.getString(JSONHelper.SERVER_HEIGHT);
            body = jsonObject.getString(JSONHelper.SERVER_BODY);
            eyes = jsonObject.getString(JSONHelper.SERVER_EYES);
            work = jsonObject.getString(JSONHelper.SERVER_WORK);
            education = jsonObject.getString(JSONHelper.SERVER_EDUCATION);
            religion = jsonObject.getString(JSONHelper.SERVER_RELIGION);
            alcohol = jsonObject.getString(JSONHelper.SERVER_ALCOHOL);
            smoking = jsonObject.getString(JSONHelper.SERVER_SMOKING);
            description = jsonObject.getString(JSONHelper.SERVER_DESCRIPTION);

            userChar = new Characteristics(height, body, eyes, alcohol, smoking, work, education, religion, description);
        } catch (JSONException ex) {
            Log.d("JSONException - User", ex.toString());
        }

        return this;
    }

    public UserBackup parseJSONToMember(JSONObject jsonObject) {
        try {
            Translator translator = Translator.getInstance();

            // parse basic user data
            userID = jsonObject.getString("id");
            email = jsonObject.getString(JSONHelper.SERVER_EMAIL);
            name = jsonObject.getString(JSONHelper.SERVER_NAME);
            gender = Gender.values()[Integer.parseInt(jsonObject.getString(JSONHelper.SERVER_GENDER))];
            dateOfBirth = parseDate(jsonObject.getString(JSONHelper.SERVER_BIRTH_DATE));
            address = new Address(jsonObject.getString(JSONHelper.SERVER_CITY),
                    translator.translate(jsonObject.getString(JSONHelper.SERVER_COUNTRY), translator.getDictionary().COUNTRIES));

            friendStatus = jsonObject.getString(JSONHelper.SERVER_FRIEND_STATUS);

            // parse user interest
            userInterest = new UserInterest();
            userInterest.setGenderInterest(Gender.getGender(jsonObject.getInt(JSONHelper.SERVER_MATCH_GENDER)));
            status = jsonObject.getString(JSONHelper.SERVER_ONLINE_STATUS);
            String profilePictureString = jsonObject.getString(JSONHelper.SERVER_MAIN_PHOTO);

            if(profilePictureString != null)
                profilePicture = new ProfilePicture(profilePictureString);

        } catch (JSONException ex) {
            Log.d("JSONException - User", ex.toString());
        }

        return this;
    }

    private Calendar parseDate(String date) {
        int year, month, day;
        String c = "/";
        try {
            if(date.indexOf(c) == -1)
                c = "-";
            String[] dateArr = date.split(c);
            year = Integer.parseInt(dateArr[0]);
            month = Integer.parseInt(dateArr[1]);
            day = Integer.parseInt(dateArr[2]);
        }catch(Exception ex) {
            year = month = day = 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return cal;
    }

    private List<Picture> parseJSONArrayToPictures(JSONArray jsonArray) throws JSONException{
        Picture pic;

        // initialize empty list of pictures
        pictures = new ArrayList<Picture>();

        // parse pictures json from user to list of json objects
        List<JSONObject> listOfJSONS = JSONHelper.parseJSONArray(jsonArray);

        for(JSONObject j: listOfJSONS) {
            // parse each json object to a picture
            pic = Picture.parseJSONToPicture(j);

            // in case this picture is a profile picture, download it and save it to device
            if(pic instanceof ProfilePicture) {
                // found the profile picture, set it as profile picture of this user
                profilePicture = (ProfilePicture) pic;

                // start new thread for downloading and saving the profile picture
                new Thread(new Runnable() {
                    public void run() {
                        ServerResponse<Bitmap> sr;
                        try {
                            // download user's profile picture to this device
                            sr = new RequestsInterface(context)
                                    .getThumbnail(profilePicture.getProfilePictureName());

                            if (sr.isSuccess()) {
                                Bitmap pp = sr.getResult();

                                profilePicture.setBitmap(pp);

                                // save the downloaded profile picture to this device's internal storage
                                new StorageOperations(context).saveImageToInternalStorage(
                                        profilePicture.getProfilePictureName(),
                                        profilePicture.getBitmap());
                            } else {
                                Log.d("Error:ProfilePic", "Could not download profile picture - " + sr.getErrorMsg());
                            }
                        }catch(ServerResponseFailedException ex) {
                            sr = null;
                        }
                    }
                }).start();
            }

            // add picture to the list of pictures of the user
            pictures.add(pic);
        }
        return pictures;
    }

    // enum that represents gender
    public enum Gender implements Serializable {
        MALE(1),
        FEMALE(2),
        BOTH(3);

        private final int genderNum;

        Gender(int genderNum) {
            this.genderNum = genderNum;
        }

        public int getGenderNum() {
            return genderNum;
        }

        public static Gender getGender(int num) {
            switch(num) {
                case 1:
                    return MALE;
                case 2:
                    return FEMALE;
                case 3:
                    return BOTH;
                default:
                    return null;
            }
        }

    }*/
}
