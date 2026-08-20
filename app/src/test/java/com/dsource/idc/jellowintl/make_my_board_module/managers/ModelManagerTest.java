package com.dsource.idc.jellowintl.make_my_board_module.managers;

import com.dsource.idc.jellowintl.make_my_board_module.datamodels.BoardIconModel;
import com.dsource.idc.jellowintl.models.JellowIcon;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ModelManagerTest {

    private ModelManager modelManager;
    private BoardIconModel rootModel;

    @Before
    public void setUp() {
        JellowIcon rootIcon = new JellowIcon("Root", "Root", 0, 0, 0);
        rootModel = new BoardIconModel(rootIcon);
        
        // Level 1
        JellowIcon l1Icon = new JellowIcon("Level1", "Level1", 1, 1, 1);
        BoardIconModel l1Model = new BoardIconModel(l1Icon);
        
        // Level 2
        JellowIcon l2Icon = new JellowIcon("Level2", "Level2", 2, 2, 2);
        BoardIconModel l2Model = new BoardIconModel(l2Icon);
        
        // Level 3
        JellowIcon l3Icon = new JellowIcon("Level3", "Level3", 3, 3, 3);
        BoardIconModel l3Model = new BoardIconModel(l3Icon);
        
        l2Model.getChildren().add(l3Model);
        l1Model.getChildren().add(l2Model);
        rootModel.getChildren().add(l1Model);

        modelManager = new ModelManager(rootModel);
        modelManager.setModel(rootModel); // Triggers refreshModel()
    }

    @Test
    public void testGettersAndSetters() {
        assertEquals(rootModel, modelManager.getModel());
        
        BoardIconModel newRoot = new BoardIconModel(new JellowIcon("NewRoot", "NewRoot", 0, 0, 0));
        modelManager.setModel(newRoot);
        assertEquals(newRoot, modelManager.getModel());
    }

    @Test
    public void testGetLevels() {
        ArrayList<JellowIcon> levelOne = modelManager.getLevelOneFromModel();
        assertEquals(1, levelOne.size());
        assertEquals("Level1…", levelOne.get(0).getIconTitle());

        ArrayList<JellowIcon> levelTwo = modelManager.getLevelTwoFromModel(0);
        assertEquals(1, levelTwo.size());
        assertEquals("Level2…", levelTwo.get(0).getIconTitle());

        ArrayList<JellowIcon> levelThree = modelManager.getLevelThreeFromModel(0, 0);
        assertEquals(1, levelThree.size());
        assertEquals("Level3", levelThree.get(0).getIconTitle());
    }

    @Test
    public void testSearchIconsForText() {
        ArrayList<JellowIcon> searchRes = modelManager.searchIconsForText("Level2");
        assertEquals(1, searchRes.size());
        
        searchRes = modelManager.searchIconsForText("level");
        assertEquals(3, searchRes.size());
    }

    @Test
    public void testGetIconPositionInModel() {
        JellowIcon searchIcon = rootModel.getChildren().get(0).getChildren().get(0).getChildren().get(0).getIcon();
        ArrayList<Integer> position = modelManager.getIconPositionInModel(searchIcon);
        
        assertEquals(3, position.size());
        assertEquals(Integer.valueOf(0), position.get(0));
        assertEquals(Integer.valueOf(0), position.get(1));
        assertEquals(Integer.valueOf(0), position.get(2));
    }
}
