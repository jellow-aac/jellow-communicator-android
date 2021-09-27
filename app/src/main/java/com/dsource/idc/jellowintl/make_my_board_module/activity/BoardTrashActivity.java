package com.dsource.idc.jellowintl.make_my_board_module.activity;

import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.startMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.make_my_board_module.adapters.BoardAdapter;
import com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs.DialogCustom;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.data_models.BoardModel;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.BoardClickListener;
import com.dsource.idc.jellowintl.make_my_board_module.managers.SelectionManager;
import com.dsource.idc.jellowintl.make_my_board_module.models.BoardListModel;
import com.dsource.idc.jellowintl.make_my_board_module.presenter_interfaces.IBoardListPresenter;
import com.dsource.idc.jellowintl.make_my_board_module.view_interfaces.IBoardListView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class BoardTrashActivity extends BaseBoardActivity<IBoardListView, IBoardListPresenter, BoardAdapter> implements IBoardListView, BoardClickListener {
    private final boolean DELETE_DISABLED = false;
    private final boolean DELETE_ENABLED = true;
    private boolean deleteMode = DELETE_DISABLED;

    @Override
    public int getLayoutId() {
        return R.layout.activity_board_list;
    }

    @Override
    public BoardAdapter getAdapter() {
        return new BoardAdapter(mContext, R.layout.my_board_card, new ArrayList<BoardModel>(), true);
    }

    @Override
    public void initViewsAndEvents() {
        mPresenter.loadBoards("Deleted");
        mAdapter.setOnItemClickListener(this);
        enableNavigationBack();
        setupActionBarTitle(View.VISIBLE, getString(R.string.home) + "/ "+ getString(R.string.menuBoardsTrash));
        applyMonochromeColor();
        setNavigationUiConditionally();
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.yellow_bg));
        findViewById(R.id.iv_action_bar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public IBoardListPresenter createPresenter() {
        return new BoardListModel(getAppDatabase());
    }

    @Override
    public void setLayoutManager(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
    }

    @Override
    public void boardLoaded(ArrayList<BoardModel> boardList) {
        if(boardList.size() == 0){
            findViewById(R.id.place_holder_text).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.place_holder_text)).setText(R.string.trash_board_placeholder);
        }else{
            findViewById(R.id.place_holder_text).setVisibility(View.GONE);
        }
        mAdapter.update(boardList);
    }

    @Override
    public void onItemClick(int position) {}

    @Override
    public void onItemDelete(final int position) {
        final DialogCustom dialog = new DialogCustom(mContext);
        dialog.setText(getString(R.string.delete_board).replace("-",
                mAdapter.getItem(position).getBoardName()));
        dialog.setOnNegativeClickListener(new DialogCustom.OnNegativeClickListener() {
            @Override
            public void onNegativeClickListener() {
                dialog.dismiss();
            }
        });
        dialog.setOnPositiveClickListener(new DialogCustom.OnPositiveClickListener() {
            @Override
            public void onPositiveClickListener() {
                mPresenter.deleteBoard(mContext,mAdapter.getItem(position));
                Toast.makeText(mContext,
                        getString(R.string.board_deleted).
                                replace("_", mAdapter.getItem(position).getBoardName()),
                        Toast.LENGTH_SHORT).show();
                mAdapter.remove(position);
                getMenu().findItem(R.id.enable_delete).setTitle("Disabled board delete");
                mAdapter.setDeleteMode(DELETE_DISABLED);
                deleteMode = DELETE_DISABLED;
                getMenu().findItem(R.id.enable_delete).setIcon(R.drawable.ic_board_delete_disabled);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onItemRestore(int position) {
        mPresenter.restoreTheBoardFromTrash(mAdapter.getItem(position));
        Toast.makeText(mContext,
                getString(R.string.board_restored_from_trash).
                        replace("_", mAdapter.getItem(position).getBoardName()),
                Toast.LENGTH_SHORT).show();
        mAdapter.remove(position);
    }

    @Override
    public void onBoardEdit(int position) {}

    @Override
    protected void onResume() {
        super.onResume();
        setVisibleAct(BoardTrashActivity.class.getSimpleName());
        if(!isAnalyticsActive()){
            resetAnalytics(this, getSession().getUserId());
        }
        // Start measuring user app screen timer.
        startMeasuring();
        SelectionManager.getInstance().delete();
        getSession().setCurrentBoardLanguage("");
        getSession().setBoardVoice("");
        initViewsAndEvents();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Check if pushId is older than 24 hours (86400000 millisecond).
        // If yes then create new pushId (user session)
        // If no then do not create new pushId instead user existing and
        // current session time is saved.
        long sessionTime = validatePushId(getSession().getSessionCreatedAt());
        getSession().setSessionCreatedAt(sessionTime);

        // Stop measuring user app screen timer.
        stopMeasuring(BoardTrashActivity.class.getSimpleName());
    }

    @Override
    public void onBackPressed() {
        if(getSession()!=null) {
            getSession().setCurrentBoardLanguage("");
            getSession().setBoardVoice("");
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Integer.parseInt(getString(R.string.search_board)) && resultCode == RESULT_OK) {
            final String boardName = data.getStringExtra(getString(R.string.search_result));
            for (int i = 0; i < mAdapter.getList().size(); i++) {
                if (mAdapter.getList().get(i).getBoardName().equals(boardName)){
                    mAdapter.highlightSearchedBoard(i);
                    Timer timer = new Timer();
                    final int finalI = i;
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            speakFromMMB(boardName);
                            mRecyclerView.smoothScrollToPosition(finalI);
                        }
                    },1000);
                    return;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.enable_delete){
            if(deleteMode==DELETE_DISABLED){
                item.setTitle("Enabled board delete");
                mAdapter.setDeleteMode(DELETE_ENABLED);
                deleteMode = DELETE_ENABLED;
                item.setIcon(R.drawable.ic_board_delete_enabled);
            }else{
                item.setTitle("Disabled board delete");
                mAdapter.setDeleteMode(DELETE_DISABLED);
                deleteMode = DELETE_DISABLED;
                item.setIcon(R.drawable.ic_board_delete_disabled);
            }
            mAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }
}
