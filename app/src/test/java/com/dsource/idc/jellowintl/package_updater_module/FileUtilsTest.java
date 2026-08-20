package com.dsource.idc.jellowintl.package_updater_module;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class FileUtilsTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testGetDirectories() {
        File baseDir = FileUtils.getBaseDir(context);
        assertTrue(baseDir.exists());
        assertTrue(baseDir.isDirectory());

        File updateDir = FileUtils.getUpdateDir(context);
        assertTrue(updateDir.exists());
        assertTrue(updateDir.isDirectory());

        File updateIconsDir = FileUtils.getUpdateIconsDir(context);
        assertTrue(updateIconsDir.exists());
        assertTrue(updateIconsDir.isDirectory());

        File updateVerbiageDir = FileUtils.getUpdateVerbiageDir(context);
        assertTrue(updateVerbiageDir.exists());
        assertTrue(updateVerbiageDir.isDirectory());
    }

    @Test
    public void testWriteAndDoesExist() {
        File file = FileUtils.getUpdateFile(context, "test_file.txt");
        assertFalse(FileUtils.doesExist(file));

        boolean written = FileUtils.writeToFile(file, "Hello, World!");
        assertTrue(written);
        assertTrue(FileUtils.doesExist(file));
        
        FileUtils.deleteFile(file);
        assertFalse(FileUtils.doesExist(file));
    }

    @Test
    public void testRenameAndDeleteDir() {
        File oldFile = FileUtils.getUpdateFile(context, "old_file.txt");
        FileUtils.writeToFile(oldFile, "Data");
        
        File newFile = FileUtils.getUpdateFile(context, "new_file.txt");
        boolean renamed = FileUtils.renameFile(oldFile, newFile);
        
        assertTrue(renamed);
        assertTrue(FileUtils.doesExist(newFile));
        assertFalse(FileUtils.doesExist(oldFile));
        
        File updateDir = FileUtils.getUpdateDir(context);
        FileUtils.deleteDir(updateDir);
        assertFalse(FileUtils.doesExist(newFile));
        assertFalse(updateDir.exists());
    }
}
