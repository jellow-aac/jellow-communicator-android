package com.dsource.idc.jellowintl.make_my_board_module.managers;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.activities.AppActivity;
import com.dsource.idc.jellowintl.make_my_board_module.fragments.SetupMMB;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.data_models.BoardModel;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.databases.TextDatabase;
import com.dsource.idc.jellowintl.models.AppDatabase;
import com.dsource.idc.jellowintl.utility.SessionManager;

// Imports removed
import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.BOARD_ID;

public class BoardLanguageManager {
    private static final String LCODE = "LCODE";
    private static final String VCODE = "VCODE";

    private final BoardModel currentBoard;
    private final Context context;
    private final AppDatabase appDatabase;

    public BoardLanguageManager(BoardModel board, Context context, AppDatabase appDatabase){
        this.currentBoard = board;
        this.context =context;
        this.appDatabase = appDatabase;
    }



    public void checkLanguageAvailabilityInBoard() {

            if(new TextDatabase(context,currentBoard.getLanguage(), appDatabase).checkForTableExists()) {
                //if database for the language is ready
                if (currentBoard != null) {
                    SessionManager sManager = new SessionManager(context);
                    sManager.setCurrentBoardLanguage(currentBoard.getLanguage());
                    sManager.setBoardVoice(currentBoard.getBoardVoice());

                    boolean navigated = false;
                    if (context instanceof android.app.Activity) {
                        try {
                            androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController((android.app.Activity) context, R.id.nav_host_fragment);
                            android.os.Bundle bundle = new android.os.Bundle();
                            bundle.putString(BOARD_ID, currentBoard.getBoardId());
                            switch (currentBoard.getSetupStatus()) {
                                case BoardModel.STATUS_L2:
                                case BoardModel.STATUS_L1:
                                    navController.navigate(R.id.addEditBoardFragment, bundle);
                                    break;
                                case BoardModel.STATUS_L3:
                                    navController.navigate(R.id.boardHomeFragment, bundle);
                                    break;
                                default:
                                    navController.navigate(R.id.iconSelectFragment, bundle);
                                    break;
                            }
                            navigated = true;
                            if (sManager.getLanguage() != null && !sManager.getLanguage().equals(currentBoard.getLanguage())) {
                                ((android.app.Activity) context).recreate();
                            }
                        } catch (Exception e) {
                            navigated = false;
                        }
                    }

                    if (!navigated) {
                        Intent intent = new Intent(context, AppActivity.class);
                        switch (currentBoard.getSetupStatus()) {
                            case BoardModel.STATUS_L2:
                            case BoardModel.STATUS_L1:
                                intent.putExtra("destination", com.dsource.idc.jellowintl.make_my_board_module.fragments.AddEditBoardFragment.class.getSimpleName());
                                break;
                            case BoardModel.STATUS_L3:
                                intent.putExtra("destination", com.dsource.idc.jellowintl.make_my_board_module.fragments.BoardHomeFragment.class.getSimpleName());
                                break;
                            default:
                                intent.putExtra("destination", com.dsource.idc.jellowintl.make_my_board_module.fragments.IconSelectFragment.class.getSimpleName());
                                break;
                        }
                        intent.putExtra(BOARD_ID, currentBoard.getBoardId());
                        context.startActivity(intent);
                    }
                }
            }
            else {
                //If database is not created, create the database
                Toast.makeText(context, context.getResources().getString(R.string.database_not_created_info), Toast.LENGTH_LONG).show();
                if (context instanceof androidx.fragment.app.FragmentActivity) {
                    SetupMMB setupDialog = SetupMMB.newInstance(currentBoard.getBoardId(), currentBoard.getLanguage(), currentBoard.getBoardVoice());
                    setupDialog.show(((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager(), "SetupMMB");
                }
            }
    }


}
