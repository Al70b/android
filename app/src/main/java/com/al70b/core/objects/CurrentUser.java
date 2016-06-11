package com.al70b.core.objects;

import android.content.Context;

import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.server_methods.RequestsInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;

public class CurrentUser extends User implements Serializable {

    // current user has an access token to call server methods
    private String accessToken;

    // current user has a password
    private String password;

    // current user matching profile
    private String matchingProfile;

    // accept advertisements to email
    private boolean acceptAdvertisement;

    public CurrentUser(Context context) {
        this.context = context;
    }

    // this is the src constructor that starts a user object
    public CurrentUser(Context context, int userID, String accessToken, String email) {
        this.context = context.getApplicationContext();
        this.userID = userID;
        this.accessToken = accessToken;
        this.email = email;
    }

    public CurrentUser(String email, String password, String name, String country, Gender gender, Calendar dateOfBirth, Gender lookingFor) {
        this(name, email, password, new Address("", country), gender, "", dateOfBirth, new UserInterest().setGenderInterest(lookingFor));
    }

    public CurrentUser(String name, String email, String password,
                       String city, String country, Gender gender, String socialStatus, Calendar dateOfBirth, UserInterest userInterest) {
        this(name, email, password, new Address(city, country), gender, socialStatus, dateOfBirth, userInterest);
    }

    public CurrentUser(String name, String email, String password,
                       Address address, Gender gender, String socialStatus, Calendar dateOfBirth, UserInterest userInterest) {
        super(name, email, address, gender, socialStatus, dateOfBirth, userInterest);
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getAccessToken() {
        return accessToken;
    }

    public CurrentUser parseJSONToUser(JSONObject jsonObject) {
        CurrentUser currentUser = (CurrentUser) super.parseJSONToUser(jsonObject);

        downloadMatchingProfile();

        return currentUser;
    }


    public boolean isMatchProfileSet() {
        return matchingProfile != null && !matchingProfile.isEmpty();
    }

    public JSONObject getMatchingProfile() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(matchingProfile);
        } catch (JSONException ex) {
        }

        return jsonObject;
    }

    public void setMatchingProfile(JSONObject matchingProfile) {
        if (matchingProfile != null)
            this.matchingProfile = matchingProfile.toString();
        else
            this.matchingProfile = null; // no matching profile is set for this user
    }


    @Override
    public String toString() {
        return super.toString() +
                "\nAccess Token: " + accessToken +
                "\nPassword: " + password;
    }


    public void downloadMatchingProfile() {
        RequestsInterface requestsInterface = new RequestsInterface(context);

        try {
            ServerResponse<JSONObject> sr = requestsInterface.getMatchingProfile(this);

            if (sr.isSuccess()) {
                setMatchingProfile(sr.getResult());
            }
        } catch (ServerResponseFailedException ex) {
        }
    }

    public void removeProfilePicture() {
        this.profilePicture = null;
    }

    public boolean isAcceptAdvertisement() {
        return acceptAdvertisement;
    }

    public void setAcceptAdvertisement(boolean flag) {
        this.acceptAdvertisement = flag;
    }
}
