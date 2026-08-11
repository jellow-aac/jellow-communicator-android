package com.dsource.idc.jellowintl.make_my_board_module.fragments;

import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.BOARD_ID;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.fragments.BaseFragment;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.data_models.BoardModel;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.databases.BoardDatabase;
import com.dsource.idc.jellowintl.make_my_board_module.presenter_interfaces.IBasePresenter;
import com.dsource.idc.jellowintl.make_my_board_module.view_interfaces.IBaseView;
import com.dsource.idc.jellowintl.models.GlobalConstants;

public abstract class BaseBoardFragment<V extends IBaseView, P extends IBasePresenter<V>, A extends RecyclerView.Adapter> extends BaseFragment {

    public P mPresenter;
    public A mAdapter;
    public RecyclerView mRecyclerView;
    public BoardModel currentBoard;
    public Context mContext;
    private SparseArray<View> mViewList;

    public abstract int getLayoutId();

    public abstract A getAdapter();

    public abstract void initViewsAndEvents();

    public abstract P createPresenter();

    public abstract void setLayoutManager(RecyclerView recyclerView);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = requireContext();
        mViewList = new SparseArray<>();

        setupActionBarTitle(view, View.VISIBLE, "");
        applyMonochromeColor(view);

        getCurrentBoard();

        mPresenter = createPresenter();
        if (mPresenter != null) {
            mPresenter.attachView((V) this);
        }

        mAdapter = getAdapter();
        mRecyclerView = view.findViewById(R.id.recycler_view);

        if (mRecyclerView != null) {
            setLayoutManager(mRecyclerView);
            mRecyclerView.setAdapter(mAdapter);
        }

        initViewsAndEvents();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!NavHostFragment.findNavController(BaseBoardFragment.this).popBackStack()) {
                    NavHostFragment.findNavController(BaseBoardFragment.this).navigate(R.id.mainFragment);
                }
            }
        });
    }

    public void getCurrentBoard() {
        try {
            String boardId = "";
            if (getArguments() != null) {
                boardId = getArguments().getString(BOARD_ID, "");
            }
            if (boardId == null || boardId.isEmpty()) {
                if (requireActivity().getIntent() != null && requireActivity().getIntent().getExtras() != null) {
                    boardId = requireActivity().getIntent().getExtras().getString(BOARD_ID, "");
                }
            }
            BoardDatabase database = new BoardDatabase(getAppDatabase());
            currentBoard = database.getBoardById(boardId);
        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Some error occurred", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void refreshBoard() {
        if (currentBoard == null || getArguments() == null) return;
        BoardDatabase database = new BoardDatabase(getAppDatabase());
        String boardId = getArguments().getString(BOARD_ID, "");
        currentBoard.setBoardName(database.getBoardName(boardId));
        currentBoard.setBoardVoice(database.getBoardVoice(boardId));
    }

    public View getView(int resId) {
        if (getView() == null) return null;
        if (mViewList.get(resId) == null) {
            View found = getView().findViewById(resId);
            if (found != null) {
                mViewList.append(resId, found);
            }
            return found;
        }
        return mViewList.get(resId);
    }

    public void setVisibility(int resId, boolean isVisible) {
        View target = getView(resId);
        if (target != null) {
            target.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    public void disableView(int resId, boolean isDisabled) {
        View target = getView(resId);
        if (target != null) {
            if (isDisabled) {
                target.setEnabled(false);
                target.setAlpha(GlobalConstants.DISABLE_ALPHA);
            } else {
                target.setEnabled(true);
                target.setAlpha(GlobalConstants.ENABLE_ALPHA);
            }
        }
    }

    public void setupToolBar(int stringResId) {
        if (getView() == null || currentBoard == null) return;
        setupActionBarTitle(getView(), View.VISIBLE, getString(R.string.home) + "/" +
                getString(R.string.my_boards) + "/" +
                currentBoard.getBoardName() + " " + getString(R.string.board) + "/" +
                getString(stringResId));

        View ivBack = getView().findViewById(R.id.iv_action_bar_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                if (!NavHostFragment.findNavController(BaseBoardFragment.this).popBackStack()) {
                    NavHostFragment.findNavController(BaseBoardFragment.this).navigate(R.id.mainFragment);
                }
            });
        }
    }

    public int getNumberOfIconPerScreen() {
        if (currentBoard == null) return 9;
        switch (currentBoard.getGridSize()) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 8;
            default:
                return 9;
        }
    }

    public void adjustIconListParentView() {
        if (getView() == null) return;
        RelativeLayout parentView = getView().findViewById(R.id.rlIconListParent);
        if (parentView != null) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ViewCompat.setOnApplyWindowInsetsListener(parentView, (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                    mlp.leftMargin = insets.left;
                    mlp.bottomMargin = insets.bottom;
                    mlp.rightMargin = insets.right;
                    mlp.topMargin = insets.top;
                    parentView.setPadding(0, insets.top, 0, 0);
                    v.setLayoutParams(mlp);
                    return WindowInsetsCompat.CONSUMED;
                });

                RelativeLayout sidePane = getView().findViewById(R.id.left_level_select_pane);
                if (sidePane != null) {
                    ViewCompat.setOnApplyWindowInsetsListener(sidePane, (v, windowInsets) -> {
                        Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                        sidePane.setPadding(insets.right, 0, 0, 0);
                        return WindowInsetsCompat.CONSUMED;
                    });
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (mViewList != null) {
            mViewList.clear();
        }
        mRecyclerView = null;
        mAdapter = null;
        super.onDestroyView();
    }
}
