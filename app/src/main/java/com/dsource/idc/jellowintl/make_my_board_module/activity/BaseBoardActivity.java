package com.dsource.idc.jellowintl.make_my_board_module.activity;

import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.BOARD_ID;
import static com.dsource.idc.jellowintl.models.GlobalConstants.SCREEN_SIZE_PHONE;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.activities.SpeechEngineBaseActivity;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.data_models.BoardModel;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.databases.BoardDatabase;
import com.dsource.idc.jellowintl.make_my_board_module.presenter_interfaces.IBasePresenter;
import com.dsource.idc.jellowintl.make_my_board_module.view_interfaces.IBaseView;
import com.dsource.idc.jellowintl.models.GlobalConstants;
import com.google.android.material.appbar.MaterialToolbar;

public abstract class BaseBoardActivity<V extends IBaseView, P extends IBasePresenter<V>, A extends RecyclerView.Adapter> extends SpeechEngineBaseActivity {

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutId());
        setupActionBarTitle(View.VISIBLE, "");
        applyMonochromeColor();

        mViewList = new SparseArray<>();

        mContext = this;

        getCurrentBoard();

        mPresenter = createPresenter();

        mPresenter.attachView((V) this);

        mAdapter = getAdapter();

        mRecyclerView = findViewById(R.id.recycler_view);

        setLayoutManager(mRecyclerView);

        mRecyclerView.setAdapter(mAdapter);

        initViewsAndEvents();
    }

    private void getCurrentBoard() {
        try {
            String boardId = "";

            if (getIntent().getExtras() != null)
                boardId = getIntent().getExtras().getString(BOARD_ID);
            BoardDatabase database = new BoardDatabase(getAppDatabase());
             currentBoard = database.getBoardById(boardId);
        } catch (NullPointerException e) {
            Toast.makeText(this, "Some error occurred", Toast.LENGTH_LONG).show();
        }
    }

    public void refreshBoard(){
        BoardDatabase database = new BoardDatabase(getAppDatabase());
        String boardId = getIntent().getExtras().getString(BOARD_ID);
        currentBoard.setBoardName(database.getBoardName(boardId));
        currentBoard.setBoardVoice(database.getBoardVoice(boardId));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public View getView(int resId) {
        if (mViewList.get(resId) == null)
            mViewList.append(resId, findViewById(resId));
        return mViewList.get(resId);
    }

    public void setVisibility(int resId, boolean isVisible) {
        if (getView(resId) != null)
            getView(resId).setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void disableView(int resId, boolean isDisabled) {
        if (getView(resId) != null) {
            if (isDisabled) {
                getView(resId).setEnabled(false);
                getView(resId).setAlpha(GlobalConstants.DISABLE_ALPHA);
            } else {
                getView(resId).setEnabled(true);
                getView(resId).setAlpha(GlobalConstants.ENABLE_ALPHA);
            }
        }
    }

    public void setupToolBar(int stringResId){
        if(getSupportActionBar()!=null) {
//            enableNavigationBack();
            setupActionBarTitle(View.VISIBLE, getString(R.string.home) + "/" +
                    getString(R.string.my_boards) + "/" +
                    currentBoard.getBoardName()+" "+getString(R.string.board) + "/" +
                    getString(stringResId));
            setNavigationUiConditionally();
        }
        findViewById(R.id.iv_action_bar_back).setOnClickListener(v -> onBackPressed());
    }

    public void setupSaveButton(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE && getScreenSize() == SCREEN_SIZE_PHONE) {
            ImageView logo = findViewById(R.id.jellow_logo);
            if (logo != null) {
                ViewCompat.setOnApplyWindowInsetsListener(logo, (v, windowInsets) -> {
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                    mlp.leftMargin = 0;
                    mlp.bottomMargin = 0;
                    mlp.rightMargin = 0;
                    mlp.topMargin = 0;
                    v.setLayoutParams(mlp);
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) v.getLayoutParams();
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    int widthDp = 120;
                    params.width = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            widthDp,
                            displayMetrics
                    );

                    int heightDp = 60;
                    params.height = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            heightDp,
                            displayMetrics
                    );

                    int marginDp = -8;
                    params.bottomMargin = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            marginDp,
                            displayMetrics
                    );
                    return WindowInsetsCompat.CONSUMED;
                });

                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                int paddingDp = 32;
                int paddingInPx = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        paddingDp,
                        displayMetrics
                );
                logo.setPadding(paddingInPx,0,0,0);
            }
        }
    }

    public int getNumberOfIconPerScreen() {
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
        }
        return 9;
    }

    public void adjustIconListParentView(){
        RelativeLayout parentView = findViewById(R.id.rlIconListParent);
        if (parentView != null){
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ViewCompat.setOnApplyWindowInsetsListener(parentView, (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                    // Apply the insets as a margin to the view. This solution sets only the
                    // bottom, left, and right dimensions, but you can apply whichever insets are
                    // appropriate to your layout. You can also update the view padding if that's
                    // more appropriate.
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                    mlp.leftMargin = insets.left;
                    mlp.bottomMargin = insets.bottom;
                    mlp.rightMargin = insets.right;
                    mlp.topMargin = insets.top;
                    parentView.setPadding(0,insets.top,0,0);
                    v.setLayoutParams(mlp);
                    // Return CONSUMED if you don't want the window insets to keep passing
                    // down to descendant views.
                    return WindowInsetsCompat.CONSUMED;
                });
            } else {
                MaterialToolbar toolbar = findViewById(R.id.toolbar);
                if (toolbar == null) {
                    return;
                }
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) parentView.getLayoutParams();
                params.topMargin = 0;
                parentView.setPadding(
                        parentView.getPaddingLeft(),
                        toolbar.getHeight() / 2,
                        parentView.getPaddingRight(),
                        parentView.getPaddingBottom()
                );
                parentView.setLayoutParams(params);
            }
        }

        RelativeLayout sidePane = findViewById(R.id.left_level_select_pane);
        if (sidePane != null) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ViewCompat.setOnApplyWindowInsetsListener(sidePane, (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                    sidePane.setPadding(insets.right, 0, 0, 0);
                    return WindowInsetsCompat.CONSUMED;
                });
            }
        }
    }
}