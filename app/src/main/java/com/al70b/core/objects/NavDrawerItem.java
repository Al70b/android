package com.al70b.core.objects;

/**
 * Created by Naseem on 5/13/2015.
 */
public class NavDrawerItem {

    public String title;
    public int icon, icon_focused;
    public boolean isSection;

    public NavDrawerItem(String title) {
        this.title = title;
        isSection = true;
    }

    public NavDrawerItem(String title, int icon, int icon_focused) {
        this.title = title;
        this.icon = icon;
        this.icon_focused = icon_focused;
        isSection = false;
    }


}
