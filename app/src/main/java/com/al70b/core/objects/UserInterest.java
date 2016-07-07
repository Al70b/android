package com.al70b.core.objects;


import java.io.Serializable;

import com.al70b.core.objects.User.Gender;


public class UserInterest implements Serializable {

    private Gender genderInterest;

    // empty interest object
    public UserInterest() {
        genderInterest = new Gender(Gender.NOT_SET);
    }

    public UserInterest(Gender genderInterest) {
        this.genderInterest = genderInterest;
    }

    public Gender getGenderInterest() {
        return genderInterest;
    }

    public UserInterest setGenderInterest(Gender i) {
        this.genderInterest = i;
        return this;
    }


    @Override
    public String toString() {
        return "\t\tInterested in: " + genderInterest;

    }
}
