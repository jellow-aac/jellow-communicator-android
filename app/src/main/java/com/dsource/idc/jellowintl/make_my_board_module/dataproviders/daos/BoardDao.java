package com.dsource.idc.jellowintl.make_my_board_module.dataproviders.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.data_models.BoardModel;

import java.util.List;

@Dao
public interface BoardDao {

    @Query("SELECT * FROM BoardModel WHERE is_deleted= 0")
    List<BoardModel> getAllBoards();

    @Query("SELECT * FROM BoardModel WHERE board_name LIKE:query AND is_deleted= 0")
    List<BoardModel> getAllBoardsStartWithName(String query);

    @Query("SELECT * FROM BoardModel WHERE language_code =(:languageCode) AND is_deleted= 0")
    List<BoardModel> getAllBoards(String languageCode);

    @Query("SELECT * FROM BoardModel WHERE is_deleted= 1")
    List<BoardModel> getAllDeletedBoards();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBoard(BoardModel boardModel);

    @Query("SELECT * FROM BoardModel WHERE board_id = (:boardID)")
    BoardModel getBoardById(String boardID);

    @Update
    void updateBoard(BoardModel boardModel);

    @Delete
    void deleteBoard(BoardModel boardModel);

    @Query("SELECT board_name FROM BoardModel WHERE board_id = (:boardId)")
    String getBoardNameFromId(String boardId);

    @Query("SELECT board_voice FROM BoardModel WHERE board_id = (:boardId)")
    String getBoardVoiceFromId(String boardId);
}
