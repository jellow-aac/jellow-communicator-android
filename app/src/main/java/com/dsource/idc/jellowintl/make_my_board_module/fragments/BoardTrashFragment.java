package com.dsource.idc.jellowintl.make_my_board_module.fragments;

import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.startMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.activities.SpeechEngineBaseActivity;
import com.dsource.idc.jellowintl.make_my_board_module.adapters.BoardAdapter;
import com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs.DialogCustom;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.data_models.BoardModel;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.BoardClickListener;
import com.dsource.idc.jellowintl.make_my_board_module.managers.SelectionManager;
import com.dsource.idc.jellowintl.make_my_board_module.models.BoardListModel;
import com.dsource.idc.jellowintl.make_my_board_module.presenter_interfaces.IBoardListPresenter;
import com.dsource.idc.jellowintl.make_my_board_module.view_interfaces.IBoardListView;

import java.util.ArrayList;

public class BoardTrashFragment extends BaseBoardFragment<IBoardListView, IBoardListPresenter, BoardAdapter> implements IBoardListView, BoardClickListener {

    private final boolean DELETE_DISABLED = false;
    private final boolean DELETE_ENABLED = true;
    private boolean deleteMode = DELETE_DISABLED;

    @Override
    public int getLayoutId() {
        return R.layout.activity_board_list;
    }

    @Override
    public BoardAdapter getAdapter() {
        return new BoardAdapter(requireContext(), R.layout.my_board_card, new ArrayList<BoardModel>(), true);
    }

    @Override
    public void initViewsAndEvents() {
        if (getView() == null) return;
        mPresenter.loadBoards("Deleted");
        mAdapter.setOnItemClickListener(this);
        setupActionBarTitle(getView(), View.VISIBLE, getString(R.string.home) + "/ " + getString(R.string.menuBoardsTrash));
        applyMonochromeColor(getView());

        View ivBack = getView().findViewById(R.id.iv_action_bar_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> NavHostFragment.findNavController(BoardTrashFragment.this).popBackStack());
        }
    }

    @Override
    public IBoardListPresenter createPresenter() {
        return new BoardListModel(getAppDatabase());
    }

    @Override
    public void setLayoutManager(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
    }

    @Override
    public void boardLoaded(ArrayList<BoardModel> boardList) {
        if (getView() == null) return;
        View placeholder = getView().findViewById(R.id.place_holder_text);
        if (placeholder != null) {
            if (boardList.size() == 0) {
                placeholder.setVisibility(View.VISIBLE);
                if (placeholder instanceof TextView) {
                    ((TextView) placeholder).setText(R.string.trash_board_placeholder);
                }
            } else {
                placeholder.setVisibility(View.GONE);
            }
        }
        mAdapter.update(boardList);
    }

    @Override
    public void onItemClick(int position) {}

    @Override
    public void onItemDelete(final int position) {
        final DialogCustom dialog = new DialogCustom(requireContext());
        dialog.setText(getString(R.string.delete_board).replace("-", mAdapter.getItem(position).getBoardName()));
        dialog.setOnNegativeClickListener(new DialogCustom.OnNegativeClickListener() {
            @Override
            public void onNegativeClickListener() {
                dialog.dismiss();
            }
        });
        dialog.setOnPositiveClickListener(new DialogCustom.OnPositiveClickListener() {
            @Override
            public void onPositiveClickListener() {
                mPresenter.deleteBoard(requireContext(), mAdapter.getItem(position));
                Toast.makeText(requireContext(),
                        getString(R.string.board_deleted).replace("_", mAdapter.getItem(position).getBoardName()),
                        Toast.LENGTH_SHORT).show();
                mAdapter.remove(position);
                if (getMenu() != null && getMenu().findItem(R.id.enable_delete) != null) {
                    getMenu().findItem(R.id.enable_delete).setTitle("Disabled board delete");
                    getMenu().findItem(R.id.enable_delete).setIcon(R.drawable.ic_board_delete_disabled);
                }
                mAdapter.setDeleteMode(DELETE_DISABLED);
                deleteMode = DELETE_DISABLED;
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onItemRestore(int position) {
        mPresenter.restoreTheBoardFromTrash(mAdapter.getItem(position));
        Toast.makeText(requireContext(),
                getString(R.string.board_restored_from_trash).replace("_", mAdapter.getItem(position).getBoardName()),
                Toast.LENGTH_SHORT).show();
        mAdapter.remove(position);
        if (mAdapter.getItemCount() == 0 && getView() != null) {
            View placeholder = getView().findViewById(R.id.place_holder_text);
            if (placeholder != null) {
                placeholder.setVisibility(View.VISIBLE);
                if (placeholder instanceof TextView) {
                    ((TextView) placeholder).setText(R.string.trash_board_placeholder);
                }
            }
        }
    }

    @Override
    public void onBoardEdit(int position) {}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.enable_delete) {
            deleteMode = !deleteMode;
            if (deleteMode) {
                item.setIcon(R.drawable.ic_board_delete_enabled);
            } else {
                item.setIcon(R.drawable.ic_board_delete_disabled);
            }
            mAdapter.setDeleteMode(deleteMode);
            mAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLevelActivity().setVisibleAct(BoardTrashFragment.class.getSimpleName());
        getLevelActivity().setupToolbarMenu(getView());

        if (!isAnalyticsActive()) {
            resetAnalytics(requireContext(), getSession().getUserId());
        }
        startMeasuring();
        SelectionManager.getInstance().delete();
        getSession().setCurrentBoardLanguage("");
        getSession().setBoardVoice("");
        if (mPresenter != null) {
            mPresenter.loadBoards("Deleted");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        long sessionTime = validatePushId(getSession().getSessionCreatedAt());
        getSession().setSessionCreatedAt(sessionTime);
        stopMeasuring(BoardTrashFragment.class.getSimpleName());
    }

    public void setDeleteMode(boolean delete) {
        this.deleteMode = delete;
        if (mAdapter != null) {
            mAdapter.setDeleteMode(delete);
        }
    }

    public boolean isDeleteMode() {
        return deleteMode;
    }

    public void highlightSearchedBoard(String boardName) {
        if (mAdapter == null) return;
        for (int i = 0; i < mAdapter.getList().size(); i++) {
            if (mAdapter.getList().get(i).getBoardName().equals(boardName)) {
                mAdapter.highlightSearchedBoard(i);
                final int finalI = i;
                if (getView() != null) {
                    getView().postDelayed(() -> {
                        if (getActivity() instanceof SpeechEngineBaseActivity) {
                            ((SpeechEngineBaseActivity) getActivity()).speakFromMMB(boardName);
                        }
                        if (mRecyclerView != null) {
                            mRecyclerView.smoothScrollToPosition(finalI);
                        }
                    }, 100);
                }
                break;
            }
        }
    }
}
