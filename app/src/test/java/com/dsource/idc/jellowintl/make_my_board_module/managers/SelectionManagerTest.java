package com.dsource.idc.jellowintl.make_my_board_module.managers;

import com.dsource.idc.jellowintl.models.JellowIcon;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class SelectionManagerTest {

    private SelectionManager manager;

    @Before
    public void setUp() {
        manager = SelectionManager.getInstance();
        manager.clearList(); // Ensure clean state before each test
    }

    @After
    public void tearDown() {
        manager.delete();
    }

    @Test
    public void testSingletonInstance() {
        SelectionManager anotherInstance = SelectionManager.getInstance();
        assertSame(manager, anotherInstance);
    }

    @Test
    public void testAddAndRemoveIcon() {
        JellowIcon icon1 = new JellowIcon("Title1", "Icon1", 1, 1, 1);
        
        manager.addIconToList(icon1);
        assertEquals(1, manager.getList().size());
        assertTrue(manager.isPresent(icon1));

        // Test duplicate insertion
        manager.addIconToList(icon1);
        assertEquals(1, manager.getList().size());

        // Remove icon
        manager.removeIconFromList(icon1);
        assertEquals(0, manager.getList().size());
        assertFalse(manager.isPresent(icon1));
    }

    @Test
    public void testSelectAllAndDeselectAll() {
        JellowIcon icon1 = new JellowIcon("Title1", "Icon1", 1, 1, 1);
        JellowIcon icon2 = new JellowIcon("Title2", "Icon2", 2, 2, 2);
        ArrayList<JellowIcon> list = new ArrayList<>();
        list.add(icon1);
        list.add(icon2);

        manager.selectAll(true, list);
        assertEquals(2, manager.getList().size());
        assertTrue(manager.isPresent(icon1));
        assertTrue(manager.isPresent(icon2));

        manager.selectAll(false, list);
        assertEquals(0, manager.getList().size());
    }

    @Test
    public void testIsSublistAndContainsAny() {
        JellowIcon icon1 = new JellowIcon("Title1", "Icon1", 1, 1, 1);
        JellowIcon icon2 = new JellowIcon("Title2", "Icon2", 2, 2, 2);
        JellowIcon icon3 = new JellowIcon("Title3", "Icon3", 3, 3, 3);

        manager.addIconToList(icon1);
        manager.addIconToList(icon2);

        ArrayList<JellowIcon> checkList1 = new ArrayList<>();
        checkList1.add(icon1);
        checkList1.add(icon2);

        ArrayList<JellowIcon> checkList2 = new ArrayList<>();
        checkList2.add(icon1);
        checkList2.add(icon3);

        assertTrue(manager.isSublist(checkList1));
        assertFalse(manager.isSublist(checkList2));

        assertTrue(manager.containsAny(checkList1));
        assertTrue(manager.containsAny(checkList2));

        ArrayList<JellowIcon> checkList3 = new ArrayList<>();
        checkList3.add(icon3);
        assertFalse(manager.containsAny(checkList3));
    }
}
