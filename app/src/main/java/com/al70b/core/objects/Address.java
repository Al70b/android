package com.al70b.core.objects;

import java.io.Serializable;

public class Address implements Serializable {

    private String city;
    private String country;

    public Address(String city, String country) {
        this.city = city;
        this.country = country;
    }

    public Address(String country) {
        this("", country);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isCityEmpty() {
        return city == null || city.isEmpty()
                || city.compareTo("null") == 0;
    }

    @Override
    public String toString() {
        return (city == null ? country : country + ", " + city);
    }
}
