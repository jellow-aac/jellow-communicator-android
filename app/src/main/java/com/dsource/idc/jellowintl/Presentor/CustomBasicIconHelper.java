package com.dsource.idc.jellowintl.Presentor;

import android.content.Context;

import com.dsource.idc.jellowintl.factories.PathFactory;
import com.dsource.idc.jellowintl.models.AppDatabase;
import com.dsource.idc.jellowintl.models.CustomIconsModel;
import com.dsource.idc.jellowintl.models.Icon;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomBasicIconHelper {

    public static void insertCustomBasicIcon(AppDatabase appDatabase, CustomIconsModel icon){
        appDatabase.customIconsDao().insertIcon(icon);
    }

    public static List<Icon> getCustomBasicIcons(AppDatabase appDatabase, String iconLanguage, String iconLocation){
         return mapCustomModelToIcons(appDatabase.customIconsDao().getAllCustomIcons(iconLanguage, iconLocation));
    }

    public static Icon getCustomBasicIcon(AppDatabase appDatabase, String iconId){
        CustomIconsModel iconVerbiage = appDatabase.customIconsDao().getCustomIcon(iconId);
        if (iconVerbiage == null)
            return null;
        return new Gson().fromJson(appDatabase.customIconsDao().getCustomIcon(iconId).getIconVerbiage(), Icon.class);
    }

    public static boolean givenCustomIconIsCategory(AppDatabase appDatabase, String iconId){
        return appDatabase.customIconsDao().getCustomIconType(iconId);
    }

    public static void deleteCustomBasicIcon(Context context, AppDatabase appDatabase, String iconId, int position){
        CustomIconsModel categoryIcon = appDatabase.customIconsDao().getCustomIcon(iconId);
        if(categoryIcon.isCategoryIcon()) {
            String location = categoryIcon.getIconLocation()+","+ (position<10 ? "0"+position: position)+"%";
            List<String> iconIdsInsideCategories =
                    appDatabase
                            .customIconsDao()
                            .getAllCustomIconsUsingLocation(
                                    categoryIcon.getIconLanguage(),
                                    location
                            );

            for (String icon : iconIdsInsideCategories) {
                appDatabase.customIconsDao().delete(icon);
                File f = new File(PathFactory.getBasicCustomIconsPath(context, icon) + ".png");
                if (f.exists())
                    f.delete();
            }
        }

        appDatabase.customIconsDao().delete(iconId);
        File f = new File(PathFactory.getBasicCustomIconsPath(context, iconId) + ".png");
        if (f.exists())
            f.delete();
    }

    private static List<Icon> mapCustomModelToIcons(List<CustomIconsModel> allCustomIcons) {
        ArrayList<Icon> iconObjects = new ArrayList();
        for (CustomIconsModel c :
                allCustomIcons) {
            iconObjects.add(new Gson().fromJson(c.getIconVerbiage(),Icon.class));
        }
        return iconObjects;
    }
}
