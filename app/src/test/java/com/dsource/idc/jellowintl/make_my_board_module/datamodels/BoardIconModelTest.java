package com.dsource.idc.jellowintl.make_my_board_module.datamodels;

import com.dsource.idc.jellowintl.make_my_board_module.utility.CustomPair;
import com.dsource.idc.jellowintl.models.JellowIcon;
import org.junit.Test;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BoardIconModelTest {

    @Test
    public void testAddAndGetChildren() {
        JellowIcon rootIcon = new JellowIcon("Root", "Root", 0, 0, 0);
        BoardIconModel rootModel = new BoardIconModel(rootIcon);
        
        JellowIcon childIcon = new JellowIcon("Child", "Child", 1, 1, 1);
        rootModel.addChild(childIcon);

        assertTrue(rootModel.hasChild());
        assertEquals(1, rootModel.getChildren().size());
        assertEquals(childIcon, rootModel.getChildren().get(0).getIcon());
    }

    @Test
    public void testAddAllChildAndGetSubList() {
        JellowIcon rootIcon = new JellowIcon("Root", "Root", 0, 0, 0);
        BoardIconModel rootModel = new BoardIconModel(rootIcon);

        ArrayList<JellowIcon> children = new ArrayList<>();
        JellowIcon child1 = new JellowIcon("Child1", "Child1", 1, 1, 1);
        JellowIcon child2 = new JellowIcon("Child2", "Child2", 2, 2, 2);
        children.add(child1);
        children.add(child2);

        rootModel.addAllChild(children);

        ArrayList<JellowIcon> subList = rootModel.getSubList();
        assertEquals(2, subList.size());
        assertEquals(child1, subList.get(0));
        assertEquals(child2, subList.get(1));
    }

    @Test
    public void testGetAllIcons() {
        JellowIcon rootIcon = new JellowIcon("Root", "Root", 0, 0, 0);
        BoardIconModel rootModel = new BoardIconModel(rootIcon);

        JellowIcon l1 = new JellowIcon("L1", "L1", 1, 1, 1);
        BoardIconModel l1Model = new BoardIconModel(l1);
        
        JellowIcon l2 = new JellowIcon("L2", "L2", 2, 2, 2);
        BoardIconModel l2Model = new BoardIconModel(l2);

        l1Model.getChildren().add(l2Model);
        rootModel.getChildren().add(l1Model);

        ArrayList<JellowIcon> allIcons = rootModel.getAllIcons();
        assertEquals(2, allIcons.size());
        assertEquals(l1, allIcons.get(0));
        assertEquals(l2, allIcons.get(1));
    }

    @Test
    public void testAppendNewModelToPrevious() {
        JellowIcon rootIcon1 = new JellowIcon("Root1", "Root1", 0, 0, 0);
        BoardIconModel rootModel1 = new BoardIconModel(rootIcon1);
        rootModel1.addChild(new JellowIcon("Child1", "Child1", 1, 1, 1));

        JellowIcon rootIcon2 = new JellowIcon("Root2", "Root2", 0, 0, 0);
        BoardIconModel rootModel2 = new BoardIconModel(rootIcon2);
        rootModel2.addChild(new JellowIcon("Child2", "Child2", 2, 2, 2));

        rootModel1.appendNewModelToPrevious(rootModel2);
        assertEquals(2, rootModel1.getChildren().size());
        assertEquals("Child2", rootModel1.getChildren().get(1).getIcon().getIconTitle());
    }

    @Test
    public void testRemoveIcon() {
        JellowIcon rootIcon = new JellowIcon("Root", "Root", 0, 0, 0);
        BoardIconModel rootModel = new BoardIconModel(rootIcon);
        
        rootModel.addChild(new JellowIcon("Child1", "Child1", 1, 1, 1));
        
        // Add a level 2 child
        JellowIcon l2Icon = new JellowIcon("Child2", "Child2", 2, 2, 2);
        rootModel.getChildren().get(0).getChildren().add(new BoardIconModel(l2Icon));

        // Remove from level 1 parent
        rootModel.removeIcon(0, 0);
        assertEquals(0, rootModel.getChildren().get(0).getChildren().size());

        // Remove from level 0 (root)
        rootModel.removeIcon(-1, 0);
        assertEquals(0, rootModel.getChildren().size());
    }

    @Test
    public void testGetIconPosition() {
        JellowIcon rootIcon = new JellowIcon("Root", "Root", 0, 0, 0);
        BoardIconModel rootModel = new BoardIconModel(rootIcon);
        
        JellowIcon l1 = new JellowIcon("L1", "L1", 1, 1, 1);
        BoardIconModel l1Model = new BoardIconModel(l1);
        
        JellowIcon l2 = new JellowIcon("L2", "L2", 2, 2, 2);
        BoardIconModel l2Model = new BoardIconModel(l2);

        l1Model.getChildren().add(l2Model);
        rootModel.getChildren().add(l1Model);

        CustomPair<Integer, Integer> pos1 = rootModel.getIconPosition(l1);
        assertEquals(Integer.valueOf(0), pos1.getFirst());
        assertEquals(Integer.valueOf(-1), pos1.getSecond());

        CustomPair<Integer, Integer> pos2 = rootModel.getIconPosition(l2);
        assertEquals(Integer.valueOf(0), pos2.getFirst());
        assertEquals(Integer.valueOf(0), pos2.getSecond());

        CustomPair<Integer, Integer> notFound = rootModel.getIconPosition(new JellowIcon("Missing", "Missing", 9, 9, 9));
        assertEquals(Integer.valueOf(-1), notFound.getFirst());
        assertEquals(Integer.valueOf(-1), notFound.getSecond());
    }

    @Test
    public void testMove() {
        JellowIcon rootIcon = new JellowIcon("Root", "Root", 0, 0, 0);
        BoardIconModel rootModel = new BoardIconModel(rootIcon);
        
        rootModel.addChild(new JellowIcon("Child1", "Child1", 1, 1, 1));
        rootModel.addChild(new JellowIcon("Child2", "Child2", 2, 2, 2));

        rootModel.move(0, 1);
        assertEquals("Child2", rootModel.getChildren().get(0).getIcon().getIconTitle());
        assertEquals("Child1", rootModel.getChildren().get(1).getIcon().getIconTitle());
    }
}
