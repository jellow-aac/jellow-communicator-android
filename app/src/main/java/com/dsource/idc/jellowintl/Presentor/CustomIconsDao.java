package com.dsource.idc.jellowintl.Presentor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dsource.idc.jellowintl.models.CustomIconsModel;

import java.util.List;

@Dao
public interface CustomIconsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIcon(CustomIconsModel icon);

    @Query("SELECT * FROM CustomIconsModel WHERE iconLanguage= :language AND iconLocation= :iconLocation")
    List<CustomIconsModel> getAllCustomIcons(String language, String iconLocation);

    @Query("SELECT iconId FROM CustomIconsModel WHERE iconLanguage= :language AND iconLocation LIKE :startWith")
    List<String> getAllCustomIconsUsingLocation(String language, String startWith);

    @Query("SELECT * FROM CustomIconsModel WHERE iconId= :iconId")
    CustomIconsModel getCustomIcon(String iconId);

    @Query("SELECT isCategory FROM CustomIconsModel WHERE iconId= :iconId")
    boolean getCustomIconType(String iconId);

    @Query("DELETE FROM CustomIconsModel WHERE iconId=:iconId")
    void delete(String iconId);
}