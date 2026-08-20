package com.dsource.idc.jellowintl.Presentor;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.dsource.idc.jellowintl.models.AppDatabase;
import com.dsource.idc.jellowintl.models.CategoryPreference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class CategoryPreferencesDaoTest {

    private AppDatabase db;
    private CategoryPreferencesDao dao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.categoryPreferenceDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void testInsertAndGetPreferences() {
        CategoryPreference pref = new CategoryPreference();
        pref.setCategoryPosition("cat_1");
        pref.setPrefString("pref_value_1");
        dao.insertPreferences(pref);

        CategoryPreference retrieved = dao.getCategoryPreference("cat_1");
        assertNotNull(retrieved);
        assertEquals("pref_value_1", retrieved.getPrefString());
    }

    @Test
    public void testClearPreferences() {
        CategoryPreference pref = new CategoryPreference();
        pref.setCategoryPosition("cat_2");
        pref.setPrefString("pref_value_2");
        dao.insertPreferences(pref);
        
        assertNotNull(dao.getCategoryPreference("cat_2"));

        dao.clearPreferences();

        assertNull(dao.getCategoryPreference("cat_2"));
    }
}
