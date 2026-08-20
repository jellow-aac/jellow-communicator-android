package com.dsource.idc.jellowintl.Presentor;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.dsource.idc.jellowintl.models.AppDatabase;
import com.dsource.idc.jellowintl.models.CustomIconsModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class CustomIconsDaoTest {

    private AppDatabase db;
    private CustomIconsDao customIconsDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        customIconsDao = db.customIconsDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void testInsertAndGetCustomIcon() {
        CustomIconsModel model = new CustomIconsModel("icon_1", "home", "en-rIN", "my verbiage", true);
        customIconsDao.insertIcon(model);

        CustomIconsModel retrieved = customIconsDao.getCustomIcon("icon_1");
        assertNotNull(retrieved);
        assertEquals("home", retrieved.getIconLocation());
        assertEquals("my verbiage", retrieved.getIconVerbiage());
        assertTrue(retrieved.isCategoryIcon());
        assertTrue(customIconsDao.getCustomIconType("icon_1"));
    }

    @Test
    public void testGetAllCustomIcons() {
        CustomIconsModel model1 = new CustomIconsModel("icon_1", "home", "en-rIN", "verbiage 1", false);
        CustomIconsModel model2 = new CustomIconsModel("icon_2", "home", "en-rIN", "verbiage 2", false);
        CustomIconsModel model3 = new CustomIconsModel("icon_3", "school", "en-rIN", "verbiage 3", false);

        customIconsDao.insertIcon(model1);
        customIconsDao.insertIcon(model2);
        customIconsDao.insertIcon(model3);

        List<CustomIconsModel> homeIcons = customIconsDao.getAllCustomIcons("en-rIN", "home");
        assertEquals(2, homeIcons.size());

        List<String> ids = customIconsDao.getAllCustomIconsUsingLocation("en-rIN", "school%");
        assertEquals(1, ids.size());
        assertEquals("icon_3", ids.get(0));
    }

    @Test
    public void testDelete() {
        CustomIconsModel model = new CustomIconsModel("icon_delete", "home", "en-rIN", "verbiage", false);
        customIconsDao.insertIcon(model);

        assertNotNull(customIconsDao.getCustomIcon("icon_delete"));

        customIconsDao.delete("icon_delete");
        assertNull(customIconsDao.getCustomIcon("icon_delete"));
    }
}
