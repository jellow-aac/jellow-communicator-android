package com.dsource.idc.jellowintl.make_my_board_module.fragments;

import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.BOARD_ID;
import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.startMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.activities.SpeechEngineBaseActivity;
import com.dsource.idc.jellowintl.make_my_board_module.adapters.BoardAdapter;
import com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs.DialogAddBoard;
import com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs.DialogCustom;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.data_models.BoardModel;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.BoardClickListener;
import com.dsource.idc.jellowintl.make_my_board_module.managers.SelectionManager;
import com.dsource.idc.jellowintl.make_my_board_module.models.BoardListModel;
import com.dsource.idc.jellowintl.make_my_board_module.presenter_interfaces.IBoardListPresenter;
import com.dsource.idc.jellowintl.make_my_board_module.view_interfaces.IBoardListView;

import java.util.ArrayList;

public class BoardListFragment extends BaseBoardFragment<IBoardListView, IBoardListPresenter, BoardAdapter> implements IBoardListView, BoardClickListener {

    private final boolean EDIT_ENABLED = true;
    private final boolean DELETE_ENABLED = true;
    public static final boolean EDIT_DISABLED = false;
    public static final boolean DELETE_DISABLED = false;
    private boolean deleteMode = DELETE_DISABLED;
    private boolean editMode = EDIT_DISABLED;

    @Override
    public int getLayoutId() {
        return R.layout.activity_board_list;
    }

    @Override
    public BoardAdapter getAdapter() {
        return new BoardAdapter(requireContext(), R.layout.my_board_card, new ArrayList<BoardModel>(), false);
    }

    @Override
    public void initViewsAndEvents() {
        if (getView() == null) return;
        mPresenter.loadBoards("All");
        mAdapter.setOnItemClickListener(this);
        setupActionBarTitle(getView(), View.VISIBLE, getString(R.string.home) + "/ " + getString(R.string.menuMyBoards));
        applyMonochromeColor(getView());

        View ivBack = getView().findViewById(R.id.iv_action_bar_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> NavHostFragment.findNavController(BoardListFragment.this).popBackStack());
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
            placeholder.setVisibility(boardList.size() > 1 ? View.GONE : View.VISIBLE);
        }
        mAdapter.update(boardList);
    }

    @Override
    public void onItemClick(int position) {
        if (editMode == EDIT_ENABLED || deleteMode == DELETE_ENABLED) {
            return;
        }
        if (position == 0) {
            DialogAddBoard dialog = DialogAddBoard.newInstance(null, board -> {
                if (board != null && getActivity() != null) {
                    mPresenter.openBoard(requireActivity(), board);
                }
            });
            dialog.show(getParentFragmentManager(), DialogAddBoard.class.getSimpleName());
            return;
        }
        BoardModel board = mAdapter.getItem(position);
        mPresenter.openBoard(requireActivity(), board);
        getSession().setCurrentBoardLanguage(board.getLanguage());
        getSession().setBoardVoice(board.getBoardVoice());
    }

    @Override
    public void onItemDelete(final int position) {
        final DialogCustom dialog = new DialogCustom(requireContext());
        dialog.setText(getString(R.string.trash_the_board).replace("-", mAdapter.getItem(position).getBoardName()));
        dialog.setOnNegativeClickListener(new DialogCustom.OnNegativeClickListener() {
            @Override
            public void onNegativeClickListener() {
                dialog.dismiss();
            }
        });
        dialog.setOnPositiveClickListener(new DialogCustom.OnPositiveClickListener() {
            @Override
            public void onPositiveClickListener() {
                mPresenter.moveTheBoardToTrash(mAdapter.getItem(position));
                Toast.makeText(requireContext(),
                        getString(R.string.board_moved_to_trash).replace("_", mAdapter.getItem(position).getBoardName()),
                        Toast.LENGTH_SHORT).show();
                mAdapter.remove(position);
                if (mAdapter.getItemCount() == 1 && getView() != null) {
                    View placeholder = getView().findViewById(R.id.place_holder_text);
                    if (placeholder != null) {
                        placeholder.setVisibility(View.VISIBLE);
                    }
                    mAdapter.setEditMode(EDIT_DISABLED);
                    editMode = EDIT_DISABLED;
                    if (getMenu() != null && getMenu().findItem(R.id.enable_edit) != null) {
                        getMenu().findItem(R.id.enable_edit).setIcon(R.drawable.ic_edit_icon_disabled);
                    }
                    deleteMode = DELETE_DISABLED;
                    mAdapter.setDeleteMode(DELETE_DISABLED);
                    if (getMenu() != null && getMenu().findItem(R.id.enable_delete) != null) {
                        getMenu().findItem(R.id.enable_delete).setVisible(false);
                    }
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onItemRestore(int position) {}

    @Override
    public void onBoardEdit(int position) {
        getSession().setCurrentBoardLanguage(mAdapter.getList().get(position).getLanguage());
        getSession().setBoardVoice(mAdapter.getList().get(position).getBoardVoice());

        Bundle bundle = new Bundle();
        bundle.putString(BOARD_ID, mAdapter.getList().get(position).getBoardId());
        NavHostFragment.findNavController(this).navigate(R.id.iconSelectFragment, bundle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.enable_edit) {
            editMode = !editMode;
            if (editMode) {
                deleteMode = DELETE_DISABLED;
                mAdapter.setDeleteMode(DELETE_DISABLED);
                if (getMenu() != null && getMenu().findItem(R.id.enable_delete) != null) {
                    getMenu().findItem(R.id.enable_delete).setIcon(R.drawable.ic_board_delete_disabled);
                }
                item.setIcon(R.drawable.ic_edit_icon_enabled);
            } else {
                item.setIcon(R.drawable.ic_edit_icon_disabled);
            }
            mAdapter.setEditMode(editMode);
            mAdapter.notifyDataSetChanged();
            return true;
        } else if (itemId == R.id.enable_delete) {
            deleteMode = !deleteMode;
            if (deleteMode) {
                editMode = EDIT_DISABLED;
                mAdapter.setEditMode(EDIT_DISABLED);
                if (getMenu() != null && getMenu().findItem(R.id.enable_edit) != null) {
                    getMenu().findItem(R.id.enable_edit).setIcon(R.drawable.ic_edit_icon_disabled);
                }
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
        getLevelActivity().setVisibleAct(BoardListFragment.class.getSimpleName());
        getLevelActivity().setupToolbarMenu(getView());

        if (!isAnalyticsActive()) {
            resetAnalytics(requireContext(), getSession().getUserId());
        }
        startMeasuring();
        SelectionManager.getInstance().delete();
        getSession().setCurrentBoardLanguage("");
        getSession().setBoardVoice("");
        if (mPresenter != null) {
            mPresenter.loadBoards("All");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        long sessionTime = validatePushId(getSession().getSessionCreatedAt());
        getSession().setSessionCreatedAt(sessionTime);
        stopMeasuring(BoardListFragment.class.getSimpleName());
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

    public void setEditMode(boolean edit) {
        this.editMode = edit;
        if (mAdapter != null) {
            mAdapter.setEditMode(edit);
        }
    }

    public boolean isEditMode() {
        return editMode;
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
}
