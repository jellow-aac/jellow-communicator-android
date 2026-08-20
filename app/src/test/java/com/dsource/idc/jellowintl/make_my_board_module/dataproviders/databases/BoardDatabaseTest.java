package com.dsource.idc.jellowintl.make_my_board_module.dataproviders.databases;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.data_models.BoardModel;
import com.dsource.idc.jellowintl.models.AppDatabase;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.IDataCallback;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class BoardDatabaseTest {

    private AppDatabase db;
    private BoardDatabase boardDatabase;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        boardDatabase = new BoardDatabase(db);
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void testInsertAndGetBoard() {
        BoardModel model = new BoardModel();
        model.setBoardId("test_board_1");
        model.setBoardName("My Test Board");
        model.setLanguage("en-rIN");

        boardDatabase.addBoardToDatabase(model);

        BoardModel retrieved = boardDatabase.getBoardById("test_board_1");
        assertNotNull(retrieved);
        assertEquals("My Test Board", retrieved.getBoardName());
        assertEquals("en-rIN", retrieved.getLanguage());
    }

    @Test
    public void testGetAllBoards() {
        BoardModel model1 = new BoardModel();
        model1.setBoardId("board_1");
        model1.setBoardName("B1");
        boardDatabase.addBoardToDatabase(model1);

        BoardModel model2 = new BoardModel();
        model2.setBoardId("board_2");
        model2.setBoardName("B2");
        boardDatabase.addBoardToDatabase(model2);

        boardDatabase.getAllBoards(new IDataCallback<ArrayList<BoardModel>>() {
            @Override
            public void onSuccess(ArrayList<BoardModel> result) {
                assertEquals(2, result.size());
            }

            @Override
            public void onFailure(String msg) { }
        });
    }

    @Test
    public void testUpdateAndTrashBoard() {
        BoardModel model = new BoardModel();
        model.setBoardId("board_trash");
        model.setBoardName("To Trash");
        boardDatabase.addBoardToDatabase(model);

        BoardModel retrieved = boardDatabase.getBoardById("board_trash");
        assertEquals(BoardModel.BOARD_STATE_NO_STATE, retrieved.getIsDeleted());

        boardDatabase.moveBoardToTrash(retrieved);

        BoardModel trashed = boardDatabase.getBoardById("board_trash");
        assertEquals(BoardModel.BOARD_STATE_TRASH, trashed.getIsDeleted());
        
        boardDatabase.getAllDeletedBoards(new IDataCallback<ArrayList<BoardModel>>() {
            @Override
            public void onSuccess(ArrayList<BoardModel> result) {
                assertEquals(1, result.size());
                assertEquals("board_trash", result.get(0).getBoardId());
            }
            @Override
            public void onFailure(String msg) { }
        });
    }
}
