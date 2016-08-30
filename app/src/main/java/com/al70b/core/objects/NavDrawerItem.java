package com.al70b.core.objects;

/**
 * Created by Naseem on 5/13/2015.
 */
public class NavDrawerItem {

    public NavDrawerItem(String title) {
        this.title = title;
        isSection = true;
        isHighlighted = false;
    }

    public NavDrawerItem(String title, int icon, int iconFocused) {
        this.title = title;
        this.icon = icon;
        this.iconFocused = iconFocused;
        isSection = false;
        isHighlighted = false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getIconFocused() {
        return iconFocused;
    }

    public void setIconFocused(int iconFocused) {
        this.iconFocused = iconFocused;
    }

    public boolean isSection() {
        return isSection;
    }

    public void setSection(boolean section) {
        isSection = section;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        if(isSection)
            return;
        isHighlighted = highlighted;
    }

    public void setSubtext(String subtext) {
        if(subtext == null || subtext.trim().isEmpty()) {
            hasSubtext = false;
        } else {
            hasSubtext = true;
            this.subtext = subtext;
        }
    }

    public String getSubtext() {
        return  subtext;
    }

    public boolean hasSubtext() {
        return hasSubtext;
    }

    private String title;
    private int icon, iconFocused;
    private boolean isSection;
    private boolean isHighlighted;
    private String subtext;
    private boolean hasSubtext;
}
