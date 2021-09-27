package com.dsource.idc.jellowintl.make_my_board_module.dataproviders.databases;

import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.data_models.BoardModel;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.IDataCallback;
import com.dsource.idc.jellowintl.models.AppDatabase;

import java.util.ArrayList;

/**
 * Created by Ayaz Alam on 31/05/2018.
 */
public class BoardDatabase{


    private final AppDatabase database;

    public BoardDatabase(AppDatabase context) {
        this.database = context;
    }

    public void addBoardToDatabase(final BoardModel board) {
        database.boardDao().insertBoard(board);
    }

    public BoardModel getBoardById(String boardID) {
        return database.boardDao().getBoardById(boardID);
    }

    public void getAllBoards(final IDataCallback<ArrayList<BoardModel>> presenter) {
        presenter.onSuccess(new ArrayList<>(database.boardDao().getAllBoards()));
    }
    public void getAllBoards(final String langCode, final IDataCallback<ArrayList<BoardModel>> presenter) {
        presenter.onSuccess(new ArrayList<>(database.boardDao().getAllBoards(langCode)));
    }
    public ArrayList<BoardModel> getAllBoardsStartWithName(String query) {
        return new ArrayList<>(database.boardDao().getAllBoardsStartWithName(query));
    }

    public void getAllDeletedBoards(final IDataCallback<ArrayList<BoardModel>> presenter) {
        presenter.onSuccess(new ArrayList<>(database.boardDao().getAllDeletedBoards()));
    }

    public void updateBoardIntoDatabase(BoardModel board) {
        database.boardDao().updateBoard(board);
    }

    public void moveBoardToTrash(BoardModel board) {
        board.setIsDeleted(BoardModel.BOARD_STATE_TRASH);
        database.boardDao().updateBoard(board);
    }

    public void deleteBoard(BoardModel boardID) {
        database.boardDao().deleteBoard(boardID);
    }

    public String getBoardName(String boardId) {
        return database.boardDao().getBoardNameFromId(boardId);
    }

    public String getBoardVoice(String boardId) {
        return database.boardDao().getBoardVoiceFromId(boardId);
    }

    public void restoreTheBoardFromTrash(BoardModel board) {
        board.setIsDeleted(BoardModel.BOARD_STATE_NO_STATE);
        database.boardDao().updateBoard(board);
    }
}