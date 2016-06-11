package com.al70b.core.objects;

import com.al70b.core.objects.User.Gender;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class UserInterest implements Serializable {

    private Gender genderInterest;
    private List<String> purposesOfInterest;

    // empty interest object
    public UserInterest() {
        genderInterest = new Gender(Gender.NOT_SET);
        purposesOfInterest = new ArrayList<>();
    }

    public UserInterest(Gender genderInterest, List<String> purposesOfInterest) {
        this.genderInterest = genderInterest;
        this.purposesOfInterest = purposesOfInterest;
    }


    public Gender getGenderInterest() {
        return genderInterest;
    }

    public UserInterest setGenderInterest(Gender i) {
        this.genderInterest = i;
        return this;
    }

    public List<String> getPurposesOfInterest() {
        return purposesOfInterest;
    }

    public void setPurposesOfInterest(List<String> req) {
        this.purposesOfInterest = req;
    }

    public String myListToString() {
        StringBuilder str = new StringBuilder();
        int n = purposesOfInterest.size();

        for (int i = 0; i < n; i++) {
            str.append(purposesOfInterest.get(i));

            if (i < n - 1)
                str.append("، ");
        }

        return str.toString();
    }

    @Override
    public String toString() {
        return "\t\tInterested in: " + genderInterest
                + "\t\tRequested relationship: " + myListToString();

    }
}
