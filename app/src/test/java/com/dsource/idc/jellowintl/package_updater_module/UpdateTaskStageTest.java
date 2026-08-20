package com.dsource.idc.jellowintl.package_updater_module;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UpdateTaskStageTest {
    @Test
    public void testEnumValues() {
        assertNotNull(UpdateTaskStage.valueOf("STAGE_1"));
        assertNotNull(UpdateTaskStage.valueOf("STAGE_8"));
        assertEquals(8, UpdateTaskStage.values().length);
    }
}
