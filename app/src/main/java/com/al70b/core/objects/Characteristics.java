package com.al70b.core.objects;

import android.content.res.Resources;

import com.al70b.R;
import com.al70b.core.misc.AppConstants;

import java.io.Serializable;

/**
 * Created by Naseem on 5/9/2015.
 */
public class Characteristics implements Serializable {

    private String height;
    private String body, eyes, alcohol, smoking;
    private String work, education, religion, description;
    private String notSpecified;

    public Characteristics(Resources res, String height, String body, String eyes, String alcohol, String smoking, String work, String education, String religion, String description) {
        notSpecified = res.getString(R.string.not_specified);

        // if attribute is null then it's not specified
        this.height = attributeNotSet(height) ? notSpecified : height; // height not specified
        this.body = attributeNotSet(body) ? notSpecified : body;
        this.eyes = attributeNotSet(eyes) ? notSpecified : eyes;
        this.alcohol = attributeNotSet(alcohol) ? notSpecified : alcohol;
        this.smoking = attributeNotSet(smoking) ? notSpecified : smoking;
        this.work = attributeNotSet(work) ? notSpecified : work;
        this.education = attributeNotSet(education) ? notSpecified : education;
        this.religion = attributeNotSet(religion) ? notSpecified : religion;
        this.description = attributeNotSet(description) ? res.getString(R.string.none) : description;
    }

    public boolean attributeNotSet(String str) {
        return (str == null || str.isEmpty()
                || str.compareTo("null") == 0)
                || str.compareTo(notSpecified) == 0;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height != null ? height : notSpecified;
    }

    public String displayHeight(Resources res) {
        return (height.compareTo(notSpecified) == 0 ? height : // not specified
                Integer.parseInt(height) <= AppConstants.MIN_HEIGHT ? res.getString(R.string.height_lower_than, AppConstants.MIN_HEIGHT) : // lower than MIN_HEIGHT
                        (Integer.parseInt(height) >= AppConstants.MAX_HEIGHT ? res.getString(R.string.height_higher_than, AppConstants.MAX_HEIGHT) : // higher than MAX_HEIGHT
                                height)); // height as it is, and it is an integer
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body != null ? body : notSpecified;
    }

    public String getEyes() {
        return eyes;
    }

    public void setEyes(String eyes) {
        this.eyes = eyes != null ? eyes : notSpecified;
    }

    public String getAlcohol() {
        return alcohol;
    }

    public void setAlcohol(String alcohol) {
        this.alcohol = alcohol != null ? alcohol : notSpecified;
    }

    public String getSmoking() {
        return smoking;
    }

    public void setSmoking(String smoking) {
        this.smoking = smoking != null ? smoking : notSpecified;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work != null ? work : notSpecified;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education != null ? education : notSpecified;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion != null ? religion : notSpecified;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    @Override

    public String toString() {
        return "\tHeight: " + height
                + "\tBody: " + body
                + "\n\tEyes: " + eyes
                + "\tAlcohol: " + alcohol
                + "\n\tSmoking: " + smoking
                + "\tWork: " + work
                + "\n\tEducation: " + education
                + "\tReligion: " + religion
                + "\n\tDescription: " + description;
    }

}
