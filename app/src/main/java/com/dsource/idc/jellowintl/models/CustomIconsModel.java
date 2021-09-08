package com.dsource.idc.jellowintl.models;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class CustomIconsModel implements Serializable {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "iconId")
    private String iconId;

    @NonNull
    @ColumnInfo(name = "iconLocation")
    private String iconLocation;

    @NonNull
    @ColumnInfo(name = "iconLanguage")
    private String iconLanguage;

    @NonNull
    @ColumnInfo(name = "iconVerbiage")
    private String iconVerbiage;

    @NonNull
    @ColumnInfo(name = "isCategory")
    private boolean isCategoryIcon;

    public CustomIconsModel(@NonNull String iconId, @NonNull String iconLocation, @NonNull String iconLanguage, @NonNull String iconVerbiage, @NonNull boolean isCategoryIcon) {
        this.iconId = iconId;
        this.iconLocation=iconLocation;
        this.iconLanguage=iconLanguage;
        this.iconVerbiage=iconVerbiage;
        this.isCategoryIcon =isCategoryIcon;
    }

    @NonNull
    public String getIconId() {
        return iconId;
    }

    public void setIconId(@NonNull String iconId) {
        this.iconId = iconId;
    }

    @NonNull
    public String getIconLocation() {
        return iconLocation;
    }

    public void setIconLocation(@NonNull String iconLocation) {
        this.iconLocation = iconLocation;
    }

    @NonNull
    public String getIconLanguage() {
        return iconLanguage;
    }

    public void setIconLanguage(@NonNull String iconLanguage) {
        this.iconLanguage = iconLanguage;
    }

    @NonNull
    public String getIconVerbiage() {
        return iconVerbiage;
    }

    public void setIconVerbiage(@NonNull String iconVerbiage) {
        this.iconVerbiage = iconVerbiage;
    }

    public boolean isCategoryIcon() {
        return isCategoryIcon;
    }

    public void setCategoryIcon(boolean categoryIcon) {
        isCategoryIcon = categoryIcon;
    }

    public String toString(){
        return "iconsId:"+getIconId() +
                ", iconLanguage:" + getIconLanguage() +
                ", iconLocation:"+ getIconLocation() +
                ", iconVerbiage:"+ getIconVerbiage()+
                ", isCategoryIcon:"+isCategoryIcon();
    }
}
