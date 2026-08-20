package com.dsource.idc.jellowintl.make_my_board_module.dataproviders.databases;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.dsource.idc.jellowintl.models.AppDatabase;
import com.dsource.idc.jellowintl.models.ExpressiveIcon;
import com.dsource.idc.jellowintl.models.Icon;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class TextDatabaseTest {

    private AppDatabase db;
    private TextDatabase textDatabase;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        textDatabase = new TextDatabase(context, "en-rIN", db);
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void testCheckForTableExists() {
        // Initially it should be false (empty db)
        assertTrue(!textDatabase.checkForTableExists());
    }

    @Test
    public void testAddAndGetVerbiage() {
        Icon icon = new Icon();
        icon.setDisplay_Label("Apple");
        icon.setEvent_Tag("food_apple");
        icon.setSearchTag("fruit, red");

        textDatabase.addNewVerbiage("001", icon);

        Icon retrieved = textDatabase.getVerbiageById("001");
        assertNotNull(retrieved);
        assertEquals("Apple", retrieved.getDisplay_Label());
        assertEquals("food_apple", retrieved.getEvent_Tag());
    }

    @Test
    public void testUpdateVerbiage() {
        Icon icon = new Icon();
        icon.setDisplay_Label("Banana");
        textDatabase.addNewVerbiage("002", icon);

        Icon updatedIcon = new Icon();
        updatedIcon.setDisplay_Label("Yellow Banana");
        textDatabase.updateVerbiage("002", updatedIcon);

        Icon retrieved = textDatabase.getVerbiageById("002");
        assertNotNull(retrieved);
        assertEquals("Yellow Banana", retrieved.getDisplay_Label());
    }
}
