package com.al70b.core.objects;

/**
 * Created by Naseem on 6/26/2015.
 */
public class Pair<X, Y> {

    public X first;
    public Y second;

    public Pair(X first, Y second) {
        this.first = first;
        this.second = second;
    }

    public Pair() {

    }

    public void set(X x, Y y) {
        this.first = x;
        this.second = y;
    }
}
