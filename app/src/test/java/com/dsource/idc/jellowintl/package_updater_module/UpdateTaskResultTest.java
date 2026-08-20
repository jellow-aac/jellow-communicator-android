package com.dsource.idc.jellowintl.package_updater_module;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UpdateTaskResultTest {
    @Test
    public void testEnumValues() {
        assertNotNull(UpdateTaskResult.valueOf("FAILED"));
        assertNotNull(UpdateTaskResult.valueOf("PACKAGE_SUCCESSFULLY_UPDATED"));
        assertNotNull(UpdateTaskResult.valueOf("NO_UPDATES_FOUND"));
        assertEquals(3, UpdateTaskResult.values().length);
    }
}
