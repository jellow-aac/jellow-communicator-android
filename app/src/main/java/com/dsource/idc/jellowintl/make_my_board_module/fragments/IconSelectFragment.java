package com.dsource.idc.jellowintl.make_my_board_module.fragments;

import static android.app.Activity.RESULT_OK;
import static com.dsource.idc.jellowintl.factories.IconFactory.EXTENSION;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.BOARD_ID;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.SEARCH_CODE;
import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.startMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.make_my_board_module.adapters.SelectIconAdapter;
import com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs.DialogAddBoard;
import com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs.DialogCustom;
import com.dsource.idc.jellowintl.make_my_board_module.expandable_recycler_view.LevelAdapter;
import com.dsource.idc.jellowintl.make_my_board_module.expandable_recycler_view.datamodels.LevelParent;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.OnItemClickListener;
import com.dsource.idc.jellowintl.make_my_board_module.managers.LevelManager;
import com.dsource.idc.jellowintl.make_my_board_module.managers.SearchManager;
import com.dsource.idc.jellowintl.make_my_board_module.managers.SelectionManager;
import com.dsource.idc.jellowintl.make_my_board_module.models.SelectIconModel;
import com.dsource.idc.jellowintl.make_my_board_module.presenter_interfaces.ISelectPresenter;
import com.dsource.idc.jellowintl.make_my_board_module.view_interfaces.ISelectIconView;
import com.dsource.idc.jellowintl.models.JellowIcon;
import com.dsource.idc.jellowintl.utility.CustomGridLayoutManager;
import com.dsource.idc.jellowintl.utility.SessionManager;

import java.io.File;
import java.util.ArrayList;

public class IconSelectFragment extends BaseBoardFragment<ISelectIconView, ISelectPresenter, SelectIconAdapter> implements ISelectIconView {

    private static final String LIST_OF_ICON = "list_of_icons";
    private static final String CURRENT_POSITION = "current_position";
    private LevelManager levelManager;
    private CheckBox cbSelectAll;
    private SearchManager searchScrollManager;

    @Override
    public int getLayoutId() {
        return R.layout.activity_icon_select;
    }

    @Override
    public SelectIconAdapter getAdapter() {
        return new SelectIconAdapter(requireActivity(), new ArrayList<JellowIcon>(), false);
    }

    @Override
    public ISelectPresenter createPresenter() {
        String lang = currentBoard != null ? currentBoard.getLanguage() : getSession().getLanguage();
        return new SelectIconModel(requireActivity(), getAppDatabase(), lang);
    }

    @Override
    public void setLayoutManager(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new CustomGridLayoutManager(requireContext(), gridSize(), gridSize()));
    }

    @Override
    public void initViewsAndEvents() {
        if (getView() == null || currentBoard == null) return;
        applyMonochromeColor(getView());
        setupParent();
        adjustIconListParentView();

        searchScrollManager = new SearchManager(mRecyclerView);
        RecyclerView levelSelectRecycler = getView().findViewById(R.id.rv_level_select);

        levelManager = new LevelManager(levelSelectRecycler, requireContext(), new LevelAdapter.onLevelClickListener() {
            @Override
            public void onClick(int parent, int child) {
                if (parent == 0) {
                    mAdapter.setCheckBoxMode(false);
                    mPresenter.loadLevels(currentBoard);
                    Toast.makeText(requireContext(), getString(R.string.press_next_to_select_icons),
                            Toast.LENGTH_SHORT).show();
                } else {
                    mAdapter.setCheckBoxMode(true);
                    mPresenter.loadLevels(parent, child);
                }
            }
        });

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (mPresenter.getLevel() == 0) return;
                if (SelectionManager.getInstance().isPresent(mAdapter.getItem(position))) {
                    SelectionManager.getInstance().removeIconFromList(mAdapter.getItem(position));
                } else {
                    SelectionManager.getInstance().addIconToList(mAdapter.getItem(position));
                }

                manageSelection();
                mAdapter.setCheckedPosition(position);
                mAdapter.notifyItemChanged(position);
            }
        });

        cbSelectAll = getView().findViewById(R.id.cb_selectAll);
        if (cbSelectAll != null) {
            cbSelectAll.setOnClickListener(v -> {
                SelectionManager.getInstance().selectAll(cbSelectAll.isChecked(), mAdapter.getList());
                manageSelection();
                mAdapter.notifyDataSetChanged();
            });
        }

        View btnReset = getView() != null ? getView().findViewById(R.id.btn_reset_selection) : null;
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                SelectionManager.getInstance().selectAll(false, mAdapter.getList());
                mAdapter.notifyDataSetChanged();
                manageSelection();
            });
        }

        View btnNext = getView() != null ? getView().findViewById(R.id.btn_next_step) : null;
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> mPresenter.addListToBoard(currentBoard, SelectionManager.getInstance().getList()));
        }

        View llHeader = getView() != null ? getView().findViewById(R.id.ll_header) : null;
        if (llHeader != null) {
            llHeader.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString(BOARD_ID, currentBoard.getBoardId());
                DialogAddBoard in = DialogAddBoard.newInstance(args, board -> {
                    if (board != null) {
                        currentBoard = board;
                        if (getView() != null) {
                            TextView tvBoardTitle = getView().findViewById(R.id.board_name);
                            if (tvBoardTitle != null) {
                                tvBoardTitle.setText(currentBoard.getBoardName());
                            }
                        }
                    }
                });
                in.show(getParentFragmentManager(), DialogAddBoard.class.getSimpleName());
            });
        }

        mPresenter.loadLevels(currentBoard);
        mPresenter.loadSubLevels();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog();
            }
        });
    }

    private void setupHeader() {
        if (getView() == null || currentBoard == null) return;
        String boardName = currentBoard.getBoardName().length() <= 24 ?
                currentBoard.getBoardName() + " " + getString(R.string.board) :
                currentBoard.getBoardName().substring(0, 24) +
                        getString(R.string.limiter) + "\n" +
                        getString(R.string.board);

        TextView tvBoardName = getView().findViewById(R.id.board_name);
        if (tvBoardName != null) {
            tvBoardName.setText(boardName);
        }

        File en_dir = requireContext().getDir(SessionManager.BOARD_ICON_LOCATION, Context.MODE_PRIVATE);
        String path = en_dir.getAbsolutePath();
        ImageView ivBoardIcon = getView().findViewById(R.id.board_icon);
        if (ivBoardIcon != null) {
            Glide.with(requireContext())
                    .load(path + "/" + currentBoard.getBoardId() + EXTENSION)
                    .placeholder(R.drawable.ic_board_person)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .transform(new CircleCrop())
                    .dontAnimate()
                    .into(ivBoardIcon);
        }
    }

    private void manageSelection() {
        if (getView() == null) return;
        StringBuilder builder = new StringBuilder("(");
        builder.append(SelectionManager.getInstance().getList().size());
        builder.append(")");

        TextView tvIconCount = (TextView) getView(R.id.icon_count);
        if (tvIconCount != null) {
            tvIconCount.setText(builder.toString());
            tvIconCount.setContentDescription(
                    SelectionManager.getInstance().getList().size() + getString(R.string.icons_selected));
        }

        if (cbSelectAll != null) {
            cbSelectAll.setChecked(false);
        }

        if (mPresenter.getLevel() != 0) {
            setVisibility(R.id.btn_reset_selection, true);
            disableView(R.id.btn_reset_selection, true);

            boolean enableReset = SelectionManager.getInstance().containsAny(mAdapter.getList());
            if (enableReset) {
                disableView(R.id.btn_reset_selection, false);
                boolean selectAll = SelectionManager.getInstance().isSublist(mAdapter.getList());
                if (cbSelectAll != null) {
                    cbSelectAll.setChecked(selectAll);
                }
            } else {
                disableView(R.id.btn_reset_selection, true);
                if (cbSelectAll != null) {
                    cbSelectAll.setChecked(false);
                }
            }
        } else {
            setVisibility(R.id.btn_reset_selection, false);
        }
    }

    @Override
    public void onLevelLoaded(ArrayList<JellowIcon> list) {
        if (mAdapter != null) {
            mAdapter.update(list);
        }
        setVisibility(R.id.place_holder_text, list.isEmpty());

        if (mPresenter.getLevel() == 0) {
            setVisibility(R.id.btn_reset_selection, false);
            setVisibility(R.id.cb_selectAll, false);
            setVisibility(R.id.icon_count, false);
        } else {
            setVisibility(R.id.btn_reset_selection, true);
            setVisibility(R.id.cb_selectAll, true);
            setVisibility(R.id.icon_count, true);
        }

        manageSelection();
    }

    @Override
    public void onSublevelLoaded(ArrayList<LevelParent> list) {
        if (levelManager != null) {
            levelManager.setList(list);
        }
    }

    @Override
    public void onBoardSaved() {
        Bundle bundle = new Bundle();
        bundle.putString(BOARD_ID, currentBoard.getBoardId());
        NavHostFragment.findNavController(this).navigate(R.id.addEditBoardFragment, bundle);
    }

    @Override
    public void onFailure(String msg) {
        Log.d("IconSelectFragment", msg);
    }

    private int gridSize() {
        int gridSize = 6;
        if (getContext() != null) {
            if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
                gridSize = 10;
            } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
                gridSize = 9;
            } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
                gridSize = 4;
            }
        }
        return gridSize;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(LIST_OF_ICON, SelectionManager.getInstance().getList());
        if (levelManager != null) {
            outState.putInt(CURRENT_POSITION, levelManager.getSelectedPosition());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList<JellowIcon> selectedIconList = (ArrayList<JellowIcon>) savedInstanceState.getSerializable(LIST_OF_ICON);
            SelectionManager.getInstance().setList(selectedIconList);
            if (levelManager != null) {
                levelManager.updateSelection(savedInstanceState.getInt(CURRENT_POSITION), -1);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLevelActivity().setVisibleAct(IconSelectFragment.class.getSimpleName());
        getLevelActivity().setupToolbarMenu(getView());

        if (!isAnalyticsActive()) {
            resetAnalytics(requireContext(), getSession().getUserId());
        }
        startMeasuring();
        refreshBoard();
        setupHeader();
        setupToolBar(R.string.select_icon_title);
    }

    @Override
    public void onPause() {
        super.onPause();
        long sessionTime = validatePushId(getSession().getSessionCreatedAt());
        getSession().setSessionCreatedAt(sessionTime);
        stopMeasuring(IconSelectFragment.class.getSimpleName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            Bundle args = new Bundle();
            if (currentBoard != null) {
                args.putString(BOARD_ID, currentBoard.getBoardId());
            }
            args.putString(BoardSearchActivity.SEARCH_MODE, BoardSearchActivity.NORMAL_SEARCH);
            BoardSearchActivity searchDialog = BoardSearchActivity.newInstance(args, (icon, resultString) -> {
                if (icon != null && !SelectionManager.getInstance().isPresent(icon)) {
                    SelectionManager.getInstance().addIconToList(icon);
                    if (searchScrollManager != null) {
                        searchScrollManager.setSearchedIcon(icon);
                    }
                    if (levelManager != null) {
                        if (icon.getVerbiageId().startsWith("0000", 6)) {
                            levelManager.updateSelection((icon.getParent0() + 1), icon.getParent2());
                            levelManager.highlightSelection((icon.getParent0() + 1), icon.getParent2());
                        } else {
                            levelManager.updateSelection((icon.getParent0() + 1), icon.getParent1());
                            levelManager.highlightSelection((icon.getParent0() + 1), icon.getParent1());
                        }
                    }
                } else if (icon != null) {
                    Toast.makeText(requireContext(), getResources().getString(R.string.icon_already_present), Toast.LENGTH_SHORT).show();
                }
            });
            searchDialog.show(getParentFragmentManager(), BoardSearchActivity.class.getSimpleName());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEARCH_CODE && resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            JellowIcon icon = (JellowIcon) data.getExtras().getSerializable(getString(R.string.search_result));
            if (icon != null && !SelectionManager.getInstance().isPresent(icon)) {
                SelectionManager.getInstance().addIconToList(icon);
                if (searchScrollManager != null) {
                    searchScrollManager.setSearchedIcon(icon);
                }
                if (levelManager != null) {
                    if (icon.getVerbiageId().startsWith("0000", 6)) {
                        levelManager.updateSelection((icon.getParent0() + 1), icon.getParent2());
                        levelManager.highlightSelection((icon.getParent0() + 1), icon.getParent2());
                    } else {
                        levelManager.updateSelection((icon.getParent0() + 1), icon.getParent1());
                        levelManager.highlightSelection((icon.getParent0() + 1), icon.getParent1());
                    }
                }
            } else {
                Toast.makeText(requireContext(), getResources().getString(R.string.icon_already_present), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showExitConfirmationDialog() {
        final DialogCustom dialog = new DialogCustom(requireContext());
        dialog.setText(getString(R.string.icon_select_exit_warning));
        dialog.setOnPositiveClickListener(new DialogCustom.OnPositiveClickListener() {
            @Override
            public void onPositiveClickListener() {
                SelectionManager.getInstance().delete();
                if (getSession() != null) {
                    getSession().setCurrentBoardLanguage("");
                    getSession().setBoardVoice("");
                }
                boolean popped = NavHostFragment.findNavController(IconSelectFragment.this).popBackStack(R.id.boardListFragment, false);
                if (!popped) {
                    NavHostFragment.findNavController(IconSelectFragment.this).navigate(R.id.boardListFragment);
                }
                dialog.dismiss();
            }
        });
        dialog.setOnNegativeClickListener(new DialogCustom.OnNegativeClickListener() {
            @Override
            public void onNegativeClickListener() {
                dialog.cancel();
            }
        });
        dialog.show();
    }
}
