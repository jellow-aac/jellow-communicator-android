package com.dsource.idc.jellowintl.make_my_board_module.fragments;

import static com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs.DialogAddVerbiage.JELLOW_ID;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.BOARD_ID;
import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.startMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.core.view.ViewCompat;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.TalkBack.TalkbackHints_SingleClick;
import com.dsource.idc.jellowintl.make_my_board_module.adapters.AddEditAdapter;
import com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs.DialogAddEditIcon;
import com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs.DialogCustom;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.data_models.BoardModel;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.AddIconCallback;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.EditAdapterCallback;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.GridSelectListener;
import com.dsource.idc.jellowintl.make_my_board_module.managers.SearchScrollManager;
import com.dsource.idc.jellowintl.make_my_board_module.models.AddEditModel;
import com.dsource.idc.jellowintl.make_my_board_module.presenter_interfaces.IAddEditPresenter;
import com.dsource.idc.jellowintl.make_my_board_module.utility.CustomPair;
import com.dsource.idc.jellowintl.make_my_board_module.view_interfaces.IAddEditView;
import com.dsource.idc.jellowintl.models.GlobalConstants;
import com.dsource.idc.jellowintl.models.JellowIcon;

import java.util.ArrayList;

public class AddEditBoardFragment extends BaseBoardFragment<IAddEditView, IAddEditPresenter, AddEditAdapter> implements IAddEditView {

    private static final int SEARCH = 1234;
    private SearchScrollManager manager;

    @Override
    public int getLayoutId() {
        return R.layout.activity_levelx_layout;
    }

    @Override
    public AddEditAdapter getAdapter() {
        ArrayList<JellowIcon> icons = new ArrayList<>();
        if (currentBoard != null && currentBoard.getIconModel() != null) {
            icons = currentBoard.getIconModel().getAllIcons();
        }
        int gridSize = currentBoard != null ? currentBoard.getGridSize() : 4;
        final AddEditAdapter adapter = new AddEditAdapter(requireActivity(), getLevelXAdapterLayout(gridSize), icons);
        adapter.setListener(new EditAdapterCallback() {
            @Override
            public void onIconClicked(int adapterPosition) {
                if (manager != null) manager.clearListener();
                if (adapterPosition == 0) {
                    showAddIconDialog();
                }
            }

            @Override
            public void onIconEdit(int adapterPosition) {
                showEditIconDialog(adapter.getItem(adapterPosition), adapterPosition);
            }

            @Override
            public void onIconRemove(int adapterPosition) {
                askBeforeDeleteIcon(adapterPosition, adapter);
            }
        });
        return adapter;
    }

    private void askBeforeDeleteIcon(final int adapterPosition, final AddEditAdapter adapter) {
        final DialogCustom dialog = new DialogCustom(requireContext());
        dialog.setText(getString(R.string.icon_delete_warning).replace("-",
                adapter.getItem(adapterPosition).getIconTitle()));
        dialog.setOnPositiveClickListener(new DialogCustom.OnPositiveClickListener() {
            @Override
            public void onPositiveClickListener() {
                adapter.remove(adapterPosition);
                mPresenter.removeIcon(adapterPosition - 1);
                if (mAdapter.getList().size() == 0) {
                    setVisibility(R.id.place_holder_text, true);
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

    @Override
    public void initViewsAndEvents() {
        if (getView() == null || currentBoard == null) return;
        applyMonochromeColor(getView());
        setupParent();
        setupToolBar(R.string.addicon_title);

        setVisibility(R.id.save_button, true);
        if (mAdapter.getList().size() == 0) {
            setVisibility(R.id.place_holder_text, true);
        }
        if (getView(R.id.keyboard) != null) {
            getView(R.id.keyboard).setAlpha(GlobalConstants.DISABLE_ALPHA);
        }

        int[] icons = {R.id.ivlike, R.id.ivyes, R.id.ivadd, R.id.ivdislike, R.id.ivno,
                R.id.ivminus, R.id.ivback, R.id.keyboard};
        for (int icon : icons) {
            View target = getView(icon);
            if (target != null) {
                target.setAlpha(GlobalConstants.DISABLE_ALPHA);
                target.setEnabled(false);
                target.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
                ViewCompat.setAccessibilityDelegate(target, new TalkbackHints_SingleClick());
            }
        }

        mPresenter.loadIcons();
        manager = new SearchScrollManager(requireContext(), mRecyclerView);

        View saveBtn = getView(R.id.save_button);
        if (saveBtn != null) {
            saveBtn.setOnClickListener(v -> {
                if (currentBoard.getIconModel() == null || currentBoard.getIconModel().getAllIcons().size() == 0) {
                    Toast.makeText(requireContext(), getString(R.string.no_icon_warning), Toast.LENGTH_LONG).show();
                } else {
                    getSession().setBoardVoice(currentBoard.getBoardVoice());
                    currentBoard.setSetupStatus(BoardModel.STATUS_L3);
                    mPresenter.updateBoard(currentBoard);

                    Bundle bundle = new Bundle();
                    bundle.putString(BOARD_ID, currentBoard.getBoardId());
                    androidx.navigation.NavOptions navOptions = new androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.boardListFragment, false)
                            .build();
                    NavHostFragment.findNavController(AddEditBoardFragment.this).navigate(R.id.boardHomeFragment, bundle, navOptions);
                }
            });
        }

        if (getView(R.id.ivback) != null) {
            getView(R.id.ivback).setEnabled(false);
            getView(R.id.ivback).setAlpha(GlobalConstants.DISABLE_ALPHA);
        }

        if (getView(R.id.ivhome) != null) {
            getView(R.id.ivhome).setOnClickListener(v -> {
                if (mRecyclerView != null && mRecyclerView.getLayoutManager() != null) {
                    mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, 0);
                    if (mAdapter != null) {
                        mAdapter.setHighlightedPosition(-1);
                    }
                }
            });
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Bundle bundle = new Bundle();
                        if (currentBoard != null) {
                            bundle.putString(BOARD_ID, currentBoard.getBoardId());
                        }
                        boolean popped = NavHostFragment.findNavController(AddEditBoardFragment.this).popBackStack(R.id.iconSelectFragment, false);
                        if (!popped) {
                            NavHostFragment.findNavController(AddEditBoardFragment.this).navigate(R.id.iconSelectFragment, bundle);
                        }
                    }
                });
    }

    @Override
    public IAddEditPresenter createPresenter() {
        return new AddEditModel(requireContext(), currentBoard, getAppDatabase());
    }

    @Override
    public void setLayoutManager(RecyclerView mRecyclerView) {
        if (currentBoard == null) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
            return;
        }
        switch (currentBoard.getGridSize()) {
            case 0:
                mRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 1));
                break;
            case 1:
            case 3:
                mRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
                break;
            default:
                mRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
                break;
        }
    }

    @Override
    public void onIconLoaded(ArrayList<JellowIcon> icons) {
        if (mAdapter != null) {
            mAdapter.update(icons);
        }
    }

    private void showAddIconDialog() {
        Bundle bundle = new Bundle();
        bundle.putString(BOARD_ID, currentBoard.getBoardId());
        DialogAddEditIcon dialog = DialogAddEditIcon.newInstance(bundle, new AddIconCallback() {
            @Override
            public void onAddedSuccessfully(JellowIcon icon) {
                if (currentBoard != null && currentBoard.getIconModel() != null) {
                    currentBoard.getIconModel().addChild(icon);
                    currentBoard.addCustomIconID(icon.getVerbiageId());
                    if (mAdapter != null) {
                        mAdapter.add(icon);
                        if (mRecyclerView != null) {
                            mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                        }
                    }
                    mPresenter.updateBoard(currentBoard);
                    if (mAdapter != null && mAdapter.getList().size() != 0) {
                        setVisibility(R.id.place_holder_text, false);
                    }
                }
            }

            @Override
            public void onFailure(String msg) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        });
        dialog.show(getParentFragmentManager(), DialogAddEditIcon.class.getSimpleName());
    }

    private void showEditIconDialog(final JellowIcon jellowIcon, final int positionInTheList) {
        Bundle bundle = new Bundle();
        bundle.putString(BOARD_ID, currentBoard.getBoardId());
        bundle.putSerializable(JELLOW_ID, jellowIcon);

        DialogAddEditIcon dialog = DialogAddEditIcon.newInstance(bundle, new AddIconCallback() {
            @Override
            public void onAddedSuccessfully(JellowIcon icon) {
                if (currentBoard != null && currentBoard.getIconModel() != null) {
                    currentBoard.getIconModel().getChildren().get(positionInTheList - 1).setIcon(icon);
                    if (mAdapter != null) {
                        mAdapter.replaceItem(positionInTheList, icon);
                        if (mRecyclerView != null) {
                            mRecyclerView.smoothScrollToPosition(positionInTheList);
                        }
                    }
                    mPresenter.updateBoard(currentBoard);
                }
            }

            @Override
            public void onFailure(String msg) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        });
        dialog.show(getParentFragmentManager(), DialogAddEditIcon.class.getSimpleName());
    }

    private int getLevelXAdapterLayout(int gridSize) {
        switch (gridSize) {
            case 0:
                return R.layout.layout_level_xadapter_1_icon;
            case 1:
                return R.layout.layout_level_xadapter_2_icons;
            case 2:
                return R.layout.layout_level_xadapter_3_icons;
            case 3:
                return R.layout.layout_level_xadapter_4_icons;
            default:
                return R.layout.layout_level_xadapter_9_icons;
        }
    }

    private void showGridDialog() {
        showGridDialog(new GridSelectListener() {
            @Override
            public void onGridSelectListener(int size) {
                if (currentBoard != null) {
                    currentBoard.setGridSize(size);
                    changeGridSize();
                }
            }
        }, currentBoard != null ? currentBoard.getGridSize() : 4);
    }

    private void changeGridSize() {
        if (mRecyclerView != null && currentBoard != null) {
            switch (currentBoard.getGridSize()) {
                case 0:
                    mRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 1));
                    break;
                case 1:
                case 3:
                    mRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
                    break;
                default:
                    mRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
                    break;
            }
        }
        resetLayout();
    }

    private void resetLayout() {
        mAdapter = getAdapter();
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(mAdapter);
        }
        initViewsAndEvents();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.grid_size) {
            showGridDialog();
            return true;
        } else if (itemId == R.id.action_search) {
            searchInBoard();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void searchInBoard() {
        Bundle args = new Bundle();
        args.putString(BoardSearchActivity.SEARCH_MODE, BoardSearchActivity.SEARCH_IN_BOARD);
        if (currentBoard != null) {
            args.putString(BOARD_ID, currentBoard.getBoardId());
        }
        BoardSearchActivity searchDialog = BoardSearchActivity.newInstance(args, (icon, resultString) -> {
            if (currentBoard != null && currentBoard.getIconModel() != null && icon != null) {
                CustomPair<Integer, Integer> iconPos = currentBoard.getIconModel().getIconPosition(icon);
                if (iconPos.getFirst() != -1) {
                    scroll(iconPos);
                }
            }
        });
        searchDialog.show(getParentFragmentManager(), BoardSearchActivity.class.getSimpleName());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEARCH && resultCode == Activity.RESULT_OK && data != null) {
            JellowIcon icon = (JellowIcon) data.getSerializableExtra(getString(R.string.search_result));
            if (currentBoard != null && currentBoard.getIconModel() != null && icon != null) {
                CustomPair<Integer, Integer> iconPos = currentBoard.getIconModel().getIconPosition(icon);
                if (iconPos.getFirst() != -1) {
                    scroll(iconPos);
                }
            }
        }
    }

    private void scroll(CustomPair<Integer, Integer> iconPos) {
        if (mAdapter == null) return;
        if (iconPos.getSecond() != -1) {
            if (iconPos.getSecond() > getNumberOfIconPerScreen() || iconPos.getSecond() < getFirstVisibleItem()) {
                if (manager != null) manager.scrollToPosition(iconPos.getSecond() + 1);
            }
            mAdapter.setHighlightedPosition(iconPos.getSecond() + 1);
            mAdapter.notifyItemChanged(iconPos.getSecond() + 1);
        } else if (iconPos.getFirst() != -1) {
            if ((iconPos.getFirst() + 1) >= getNumberOfIconPerScreen() || (iconPos.getFirst() + 1) < getFirstVisibleItem()) {
                if (manager != null) manager.scrollToPosition(iconPos.getFirst() + 1);
            }
            mAdapter.setHighlightedPosition(iconPos.getFirst() + 1);
            mAdapter.notifyItemChanged(iconPos.getFirst() + 1);
        }
    }

    private Integer getFirstVisibleItem() {
        if (mRecyclerView != null && mRecyclerView.getLayoutManager() != null) {
            return ((GridLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        }
        return -1;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLevelActivity().setVisibleAct(AddEditBoardFragment.class.getSimpleName());
        getLevelActivity().setupToolbarMenu(getView());

        if (!isAnalyticsActive()) {
            resetAnalytics(requireContext(), getSession().getUserId());
        }
        startMeasuring();
    }

    @Override
    public void onPause() {
        super.onPause();
        long sessionTime = validatePushId(getSession().getSessionCreatedAt());
        getSession().setSessionCreatedAt(sessionTime);
        stopMeasuring(AddEditBoardFragment.class.getSimpleName());
    }
}
