package com.dsource.idc.jellowintl.fragments;

import static com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs.DialogAddVerbiage.JELLOW_ID;
import static com.dsource.idc.jellowintl.models.GlobalConstants.ADD_BASIC_CUSTOM_ICON;
import static com.dsource.idc.jellowintl.models.GlobalConstants.BASIC_ICON_ID;
import static com.dsource.idc.jellowintl.models.GlobalConstants.IS_HOME_CATEGORY;
import static com.dsource.idc.jellowintl.models.GlobalConstants.IS_HOME_CUSTOM_ICON;
import static com.dsource.idc.jellowintl.utility.Analytics.bundleEvent;
import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.singleEvent;
import static com.dsource.idc.jellowintl.utility.Analytics.startMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dsource.idc.jellowintl.BuildConfig;
import com.dsource.idc.jellowintl.Presentor.CustomBasicIconHelper;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.TalkBack.TalkbackHints_SingleClick;
import com.dsource.idc.jellowintl.fragments.adapters.LevelOneFragmentAdapter;
import com.dsource.idc.jellowintl.factories.IconFactory;
import com.dsource.idc.jellowintl.factories.LanguageFactory;
import com.dsource.idc.jellowintl.factories.PathFactory;
import com.dsource.idc.jellowintl.factories.TextFactory;
import com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs.DialogAddEditIcon;
import com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs.DialogCustom;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.AddIconCallback;
import com.dsource.idc.jellowintl.models.ExpressiveIcon;
import com.dsource.idc.jellowintl.models.GlobalConstants;
import com.dsource.idc.jellowintl.models.Icon;
import com.dsource.idc.jellowintl.models.JellowIcon;
import com.dsource.idc.jellowintl.models.MiscellaneousIcon;
import com.dsource.idc.jellowintl.utility.DialogKeyboardUtterance;
import com.dsource.idc.jellowintl.utility.LevelUiUtils;
import com.dsource.idc.jellowintl.utility.UserEventCollector;
import com.dsource.idc.jellowintl.utility.interfaces.BasicCustomIconsChangedListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;

public class LevelOneFragment extends BaseFragment implements BasicCustomIconsChangedListener {

    private int mFlgLike = GlobalConstants.SHORT_SPEECH, mFlgYes = GlobalConstants.SHORT_SPEECH,
            mFlgMore = GlobalConstants.SHORT_SPEECH, mFlgDntLike = GlobalConstants.SHORT_SPEECH,
            mFlgNo = GlobalConstants.SHORT_SPEECH, mFlgLess = GlobalConstants.SHORT_SPEECH;
    private int mFlgImage = GlobalConstants.NO_EXPR;
    private ImageView mIvLike, mIvDontLike, mIvYes, mIvNo, mIvMore, mIvLess,
            mIvHome, mIvKeyboard, mIvBack;

    public RecyclerView mRecyclerView;
    private int mLevelOneItemPos = GlobalConstants.NOT_SELECTED;
    private int mSelectedItemAdapterPos = GlobalConstants.NOT_SELECTED;
    private int mActionBtnClickCount = -1;
    private boolean mShouldReadFullSpeech = false;
    private boolean mSearched = false;
    private ArrayList<View> mRecyclerItemsViewList;
    private String[] mExprBtnTxt, mNavigationBtnTxt, mIconCode;
    private String txtHome;

    private UserEventCollector mUec;
    private Icon[] level1IconObjects;
    private ImageView[] expressiveBtn;
    private AddIconCallback callback;
    private RecyclerView.OnScrollListener scrollListener;
    private ViewTreeObserver.OnGlobalLayoutListener populationDoneListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_levelx_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLevelActivity().setVisibleAct(LevelOneFragment.class.getSimpleName());
        getLevelActivity().setupActionBarTitle(view, View.GONE, getString(R.string.action_bar_title));
        getLevelActivity().setupToolbarMenu(view);
        getLevelActivity().applyMonochromeColor(view);
        mUec = new UserEventCollector();
        txtHome = getString(R.string.home);

        loadArraysFromResources(view);
        initializeArrayListOfRecycler();
        initializeLayoutViews(view);
        initializeViewListeners();

        // Check search bundle extras / arguments
        Bundle args = getArguments();
        if (args == null && getActivity() != null && getActivity().getIntent() != null) {
            args = getActivity().getIntent().getExtras();
        }
        if (args != null && args.getString(getString(R.string.from_search)) != null) {
            if (args.getString(getString(R.string.from_search)).equals(getString(R.string.search_tag))) {
                mSearched = true;
                highlightSearchedItem(args);
            }
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavController navController = NavHostFragment.findNavController(LevelOneFragment.this);
                if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.levelOneFragment) {
                    requireActivity().finish();
                } else {
                    navController.popBackStack();
                }
            }
        });
    }

    private void initializeArrayListOfRecycler() {
        mRecyclerItemsViewList = new ArrayList<>();
        if (level1IconObjects != null) {
            while (mRecyclerItemsViewList.size() < level1IconObjects.length) {
                mRecyclerItemsViewList.add(null);
            }
        }
    }

    private void highlightSearchedItem(Bundle args) {
        int iconIndex = args.getInt(getString(R.string.search_parent_0));
        mRecyclerView.scrollToPosition(iconIndex);
        if (mRecyclerItemsViewList.size() <= iconIndex || mRecyclerItemsViewList.get(iconIndex) == null) {
            /*mRecyclerView.addOnScrollListener(getListener(iconIndex));
        } else {*/
            setSearchHighlight(iconIndex);
        }
    }

    private RecyclerView.OnScrollListener getListener(final int index) {
        scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    setSearchHighlight(index);
                }
            }
        };
        return scrollListener;
    }

    public void setSearchHighlight(final int pos) {
        if (mRecyclerView.getAdapter() != null) {
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
        populationDoneListener = () -> {
            if (pos < mRecyclerItemsViewList.size()) {
                View searchedView = mRecyclerItemsViewList.get(pos);
                if (searchedView == null) {
                    if (mRecyclerView.getLayoutManager() != null) {
                        mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, pos);
                    }
                    mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(populationDoneListener);
                    return;
                }
                tappedCategoryItemEvent(searchedView, pos);
                if (scrollListener != null) {
                    mRecyclerView.removeOnScrollListener(scrollListener);
                }
                mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(populationDoneListener);
                if (isAdded() && getLevelActivity().isAccessibilityTalkBackOn((AccessibilityManager) requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE))) {
                    searchedView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                }
            }
        };
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(populationDoneListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        authenticateUserIfNot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLevelActivity().setVisibleAct(LevelOneFragment.class.getSimpleName());
        getLevelActivity().setupActionBarTitle(getView(), View.GONE, getString(R.string.action_bar_title));
        getLevelActivity().setupToolbarMenu(getView());
        if (!isAnalyticsActive()) {
            resetAnalytics(requireContext(), getSession().getUserId());
        }
        startMeasuring();

        if (!getSession().getToastMessage().isEmpty()) {
            Toast.makeText(requireContext(), getSession().getToastMessage(), Toast.LENGTH_SHORT).show();
            getSession().setToastMessage("");
        }

        Bundle args = getArguments();
        if (args != null && args.getBoolean(getString(R.string.goto_home), false)) {
            gotoHome(true);
        }
        DialogAddEditIcon.subscribe(callback);
    }

    @Override
    public void onPause() {
        super.onPause();
        long sessionTime = validatePushId(getSession().getSessionCreatedAt());
        getSession().setSessionCreatedAt(sessionTime);
        stopMeasuring("LevelOneActivity");
    }

    private void initializeLayoutViews(View view) {
        mIvLike = view.findViewById(R.id.ivlike);
        mIvDontLike = view.findViewById(R.id.ivdislike);
        mIvMore = view.findViewById(R.id.ivadd);
        mIvLess = view.findViewById(R.id.ivminus);
        mIvYes = view.findViewById(R.id.ivyes);
        mIvNo = view.findViewById(R.id.ivno);
        mIvHome = view.findViewById(R.id.ivhome);
        mIvBack = view.findViewById(R.id.ivback);
        mIvBack.setAlpha(GlobalConstants.DISABLE_ALPHA);
        mIvBack.setEnabled(false);
        mIvKeyboard = view.findViewById(R.id.keyboard);

        ViewCompat.setAccessibilityDelegate(mIvLike, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(mIvYes, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(mIvMore, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(mIvDontLike, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(mIvNo, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(mIvLess, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(mIvKeyboard, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(mIvHome, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(mIvBack, new TalkbackHints_SingleClick());

        expressiveBtn = new ImageView[]{mIvLike, mIvYes, mIvMore, mIvDontLike, mIvNo, mIvLess};
    }

    private void loadRecyclerView(View view) {
        mRecyclerView = view.findViewById(R.id.recycler_view);
        switch (getSession().getGridSize()) {
            case GlobalConstants.ONE_ICON_PER_SCREEN:
                mRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 1));
                break;
            case GlobalConstants.TWO_ICONS_PER_SCREEN:
            case GlobalConstants.FOUR_ICONS_PER_SCREEN:
                mRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
                break;
            case GlobalConstants.THREE_ICONS_PER_SCREEN:
            case GlobalConstants.NINE_ICONS_PER_SCREEN:
                mRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
                break;
        }
        mRecyclerView.setAdapter(new LevelOneFragmentAdapter(LevelOneFragment.this, level1IconObjects));
        mRecyclerView.setVerticalScrollBarEnabled(true);
        mRecyclerView.setScrollbarFadingEnabled(false);
        mRecyclerView.requestFocus();
    }

    private void initializeViewListeners() {
        initRecyclerViewListeners();
        initHomeBtnListener();
        initKeyboardBtnListener();
        initLikeBtnListener();
        initDontLikeBtnListener();
        initYesBtnListener();
        initNoBtnListener();
        initMoreBtnListener();
        initLessBtnListener();
        initCustomBasicListeners();
    }

    private void initCustomBasicListeners() {
        callback = new AddIconCallback() {
            @Override
            public void onAddedSuccessfully(JellowIcon icon) {
                if (!isAdded()) return;
                Icon customIcon = CustomBasicIconHelper.getCustomBasicIcon(getAppDatabase(), icon.getVerbiageId());
                ArrayList<Icon> iconsList = new ArrayList<>(Arrays.asList(level1IconObjects));
                iconsList.add(customIcon);
                level1IconObjects = TextFactory.getAllIconsArray(
                        requireContext(),
                        mIconCode,
                        CustomBasicIconHelper.getCustomBasicIcons(getAppDatabase(), getSession().getLanguage(), "00"),
                        getSession().getBasicCustomIconAddState());

                mRecyclerView.setAdapter(new LevelOneFragmentAdapter(LevelOneFragment.this, level1IconObjects));
                if (mRecyclerView.getAdapter() != null) {
                    mRecyclerView.getAdapter().notifyItemRangeChanged(0, level1IconObjects.length);
                }
                mRecyclerItemsViewList.clear();
                while (mRecyclerItemsViewList.size() < level1IconObjects.length) {
                    mRecyclerItemsViewList.add(null);
                }
            }

            @Override
            public void onFailure(String msg) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    private void initRecyclerViewListeners() {
        mRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                int pos = mRecyclerView.getChildLayoutPosition(view);
                if (pos >= 0 && pos < mRecyclerItemsViewList.size()) {
                    mRecyclerItemsViewList.set(pos, view);
                }
                if (mRecyclerItemsViewList.contains(view) && mSelectedItemAdapterPos > GlobalConstants.NOT_SELECTED &&
                        pos == mSelectedItemAdapterPos) {
                    LevelUiUtils.setBorderToCategoryIcon(requireActivity(), view, true,
                            mActionBtnClickCount, mFlgImage);
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                LevelUiUtils.setBorderToCategoryIcon(requireActivity(), view, false,
                        mActionBtnClickCount, mFlgImage);
                int pos = mRecyclerView.getChildLayoutPosition(view);
                if (pos >= 0 && pos < mRecyclerItemsViewList.size()) {
                    mRecyclerItemsViewList.set(pos, null);
                }
            }
        });
    }

    private void initHomeBtnListener() {
        mIvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUec.createSendFbEventFromTappedView(26, "", "");
                gotoHome(false);
            }
        });
    }

    private void initKeyboardBtnListener() {
        mIvKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singleEvent("Navigation", "Keyboard");
                new DialogKeyboardUtterance().show(requireActivity());
                speakAndShowTextBar_(mNavigationBtnTxt[2]);
                mIvKeyboard.setImageResource(R.drawable.keyboard_pressed);
                mIvBack.setImageResource(R.drawable.back);
                mIvHome.setImageResource(R.drawable.home);
            }
        });
    }

    private void initLikeBtnListener() {
        mIvLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlgYes = mFlgMore = mFlgDntLike = mFlgNo = mFlgLess = GlobalConstants.SHORT_SPEECH;
                mFlgImage = GlobalConstants.LIKE;
                LevelUiUtils.setExpressiveIconPressedState(expressiveBtn, mFlgImage);
                if (!mShouldReadFullSpeech) {
                    if (mFlgLike == GlobalConstants.LONG_SPEECH) {
                        speakAndShowTextBar_(mExprBtnTxt[1]);
                        mFlgLike = GlobalConstants.SHORT_SPEECH;
                        mUec.createSendFbEventFromTappedView(1, "", "");
                    } else {
                        speakAndShowTextBar_(mExprBtnTxt[0]);
                        mFlgLike = GlobalConstants.LONG_SPEECH;
                        mUec.createSendFbEventFromTappedView(0, "", "");
                    }
                } else {
                    ++mActionBtnClickCount;
                    if (mSelectedItemAdapterPos >= 0 && mSelectedItemAdapterPos < mRecyclerItemsViewList.size() && mRecyclerItemsViewList.get(mSelectedItemAdapterPos) != null) {
                        LevelUiUtils.setBorderToCategoryIcon(requireActivity(),
                                mRecyclerItemsViewList.get(mSelectedItemAdapterPos), true,
                                mActionBtnClickCount, mFlgImage);
                    }
                    if (mFlgLike == GlobalConstants.LONG_SPEECH) {
                        speakAndShowTextBar_(level1IconObjects[mLevelOneItemPos].getLL());
                        mFlgLike = GlobalConstants.SHORT_SPEECH;
                        if (mLevelOneItemPos < mIconCode.length)
                            mUec.createSendFbEventFromTappedView(14,
                                    level1IconObjects[mLevelOneItemPos].getEvent_Tag() + "_" +
                                            mIconCode[mLevelOneItemPos] + "LL", "");
                    } else {
                        speakAndShowTextBar_(level1IconObjects[mLevelOneItemPos].getL());
                        mFlgLike = GlobalConstants.LONG_SPEECH;
                        if (mLevelOneItemPos < mIconCode.length)
                            mUec.createSendFbEventFromTappedView(13,
                                    level1IconObjects[mLevelOneItemPos].getEvent_Tag() + "_" +
                                            mIconCode[mLevelOneItemPos] + "L0", "");
                    }
                }
            }
        });
    }

    private void initDontLikeBtnListener() {
        mIvDontLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlgLike = mFlgYes = mFlgMore = mFlgNo = mFlgLess = GlobalConstants.SHORT_SPEECH;
                mFlgImage = GlobalConstants.DONT_LIKE;
                LevelUiUtils.setExpressiveIconPressedState(expressiveBtn, mFlgImage);
                if (!mShouldReadFullSpeech) {
                    if (mFlgDntLike == GlobalConstants.LONG_SPEECH) {
                        speakAndShowTextBar_(mExprBtnTxt[7]);
                        mFlgDntLike = GlobalConstants.SHORT_SPEECH;
                        mUec.createSendFbEventFromTappedView(7, "", "");
                    } else {
                        speakAndShowTextBar_(mExprBtnTxt[6]);
                        mFlgDntLike = GlobalConstants.LONG_SPEECH;
                        mUec.createSendFbEventFromTappedView(6, "", "");
                    }
                } else {
                    ++mActionBtnClickCount;
                    if (mSelectedItemAdapterPos >= 0 && mSelectedItemAdapterPos < mRecyclerItemsViewList.size() && mRecyclerItemsViewList.get(mSelectedItemAdapterPos) != null)
                        LevelUiUtils.setBorderToCategoryIcon(requireActivity(),
                                mRecyclerItemsViewList.get(mSelectedItemAdapterPos), true,
                                mActionBtnClickCount, mFlgImage);
                    if (mFlgDntLike == GlobalConstants.LONG_SPEECH) {
                        speakAndShowTextBar_(level1IconObjects[mLevelOneItemPos].getDD());
                        mFlgDntLike = GlobalConstants.SHORT_SPEECH;
                        if (mLevelOneItemPos < mIconCode.length)
                            mUec.createSendFbEventFromTappedView(20,
                                    level1IconObjects[mLevelOneItemPos].getEvent_Tag() + "_" +
                                            mIconCode[mLevelOneItemPos] + "DD", "");
                    } else {
                        speakAndShowTextBar_(level1IconObjects[mLevelOneItemPos].getD());
                        mFlgDntLike = GlobalConstants.LONG_SPEECH;
                        if (mLevelOneItemPos < mIconCode.length)
                            mUec.createSendFbEventFromTappedView(19,
                                    level1IconObjects[mLevelOneItemPos].getEvent_Tag() + "_" +
                                            mIconCode[mLevelOneItemPos] + "D0", "");
                    }
                }
            }
        });
    }

    private void initYesBtnListener() {
        mIvYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlgLike = mFlgMore = mFlgDntLike = mFlgNo = mFlgLess = GlobalConstants.SHORT_SPEECH;
                mFlgImage = GlobalConstants.YES;
                LevelUiUtils.setExpressiveIconPressedState(expressiveBtn, mFlgImage);
                if (!mShouldReadFullSpeech) {
                    if (mFlgYes == GlobalConstants.LONG_SPEECH) {
                        speakAndShowTextBar_(mExprBtnTxt[3]);
                        mFlgYes = GlobalConstants.SHORT_SPEECH;
                        mUec.createSendFbEventFromTappedView(3, "", "");
                    } else {
                        speakAndShowTextBar_(mExprBtnTxt[2]);
                        mFlgYes = GlobalConstants.LONG_SPEECH;
                        mUec.createSendFbEventFromTappedView(2, "", "");
                    }
                } else {
                    ++mActionBtnClickCount;
                    if (mSelectedItemAdapterPos >= 0 && mSelectedItemAdapterPos < mRecyclerItemsViewList.size() && mRecyclerItemsViewList.get(mSelectedItemAdapterPos) != null)
                        LevelUiUtils.setBorderToCategoryIcon(requireActivity(),
                                mRecyclerItemsViewList.get(mSelectedItemAdapterPos), true,
                                mActionBtnClickCount, mFlgImage);
                    if (mFlgYes == GlobalConstants.LONG_SPEECH) {
                        speakAndShowTextBar_(level1IconObjects[mLevelOneItemPos].getYY());
                        mFlgYes = GlobalConstants.SHORT_SPEECH;
                        if (mLevelOneItemPos < mIconCode.length)
                            mUec.createSendFbEventFromTappedView(16,
                                    level1IconObjects[mLevelOneItemPos].getEvent_Tag() + "_" +
                                            mIconCode[mLevelOneItemPos] + "YY", "");
                    } else {
                        speakAndShowTextBar_(level1IconObjects[mLevelOneItemPos].getY());
                        mFlgYes = GlobalConstants.LONG_SPEECH;
                        if (mLevelOneItemPos < mIconCode.length)
                            mUec.createSendFbEventFromTappedView(15,
                                    level1IconObjects[mLevelOneItemPos].getEvent_Tag() + "_" +
                                            mIconCode[mLevelOneItemPos] + "Y0", "");
                    }
                }
            }
        });
    }

    private void initNoBtnListener() {
        mIvNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlgLike = mFlgYes = mFlgMore = mFlgDntLike = mFlgLess = GlobalConstants.SHORT_SPEECH;
                mFlgImage = GlobalConstants.NO;
                LevelUiUtils.setExpressiveIconPressedState(expressiveBtn, mFlgImage);
                if (!mShouldReadFullSpeech) {
                    if (mFlgNo == GlobalConstants.LONG_SPEECH) {
                        speakAndShowTextBar_(mExprBtnTxt[9]);
                        mFlgNo = GlobalConstants.SHORT_SPEECH;
                        mUec.createSendFbEventFromTappedView(9, "", "");
                    } else {
                        speakAndShowTextBar_(mExprBtnTxt[8]);
                        mFlgNo = GlobalConstants.LONG_SPEECH;
                        mUec.createSendFbEventFromTappedView(8, "", "");
                    }
                } else {
                    ++mActionBtnClickCount;
                    if (mSelectedItemAdapterPos >= 0 && mSelectedItemAdapterPos < mRecyclerItemsViewList.size() && mRecyclerItemsViewList.get(mSelectedItemAdapterPos) != null)
                        LevelUiUtils.setBorderToCategoryIcon(requireActivity(),
                                mRecyclerItemsViewList.get(mSelectedItemAdapterPos), true,
                                mActionBtnClickCount, mFlgImage);
                    if (mFlgNo == GlobalConstants.LONG_SPEECH) {
                        speakAndShowTextBar_(level1IconObjects[mLevelOneItemPos].getNN());
                        mFlgNo = GlobalConstants.SHORT_SPEECH;
                        if (mLevelOneItemPos < mIconCode.length)
                            mUec.createSendFbEventFromTappedView(22,
                                    level1IconObjects[mLevelOneItemPos].getEvent_Tag() + "_" +
                                            mIconCode[mLevelOneItemPos] + "NN", "");
                    } else {
                        speakAndShowTextBar_(level1IconObjects[mLevelOneItemPos].getN());
                        mFlgNo = GlobalConstants.LONG_SPEECH;
                        if (mLevelOneItemPos < mIconCode.length)
                            mUec.createSendFbEventFromTappedView(21,
                                    level1IconObjects[mLevelOneItemPos].getEvent_Tag() + "_" +
                                            mIconCode[mLevelOneItemPos] + "N0", "");
                    }
                }
            }
        });
    }

    private void initMoreBtnListener() {
        mIvMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlgLike = mFlgYes = mFlgDntLike = mFlgNo = mFlgLess = GlobalConstants.SHORT_SPEECH;
                mFlgImage = GlobalConstants.MORE;
                LevelUiUtils.setExpressiveIconPressedState(expressiveBtn, mFlgImage);
                if (!mShouldReadFullSpeech) {
                    if (mFlgMore == GlobalConstants.LONG_SPEECH) {
                        speakAndShowTextBar_(mExprBtnTxt[5]);
                        mFlgMore = GlobalConstants.SHORT_SPEECH;
                        mUec.createSendFbEventFromTappedView(5, "", "");
                    } else {
                        speakAndShowTextBar_(mExprBtnTxt[4]);
                        mFlgMore = GlobalConstants.LONG_SPEECH;
                        mUec.createSendFbEventFromTappedView(4, "", "");
                    }
                } else {
                    ++mActionBtnClickCount;
                    if (mSelectedItemAdapterPos >= 0 && mSelectedItemAdapterPos < mRecyclerItemsViewList.size() && mRecyclerItemsViewList.get(mSelectedItemAdapterPos) != null)
                        LevelUiUtils.setBorderToCategoryIcon(requireActivity(),
                                mRecyclerItemsViewList.get(mSelectedItemAdapterPos), true,
                                mActionBtnClickCount, mFlgImage);
                    if (mFlgMore == GlobalConstants.LONG_SPEECH) {
                        speakAndShowTextBar_(level1IconObjects[mLevelOneItemPos].getMM());
                        mFlgMore = GlobalConstants.SHORT_SPEECH;
                        if (mLevelOneItemPos < mIconCode.length)
                            mUec.createSendFbEventFromTappedView(18,
                                    level1IconObjects[mLevelOneItemPos].getEvent_Tag() + "_" +
                                            mIconCode[mLevelOneItemPos] + "MM", "");
                    } else {
                        speakAndShowTextBar_(level1IconObjects[mLevelOneItemPos].getM());
                        mFlgMore = GlobalConstants.LONG_SPEECH;
                        if (mLevelOneItemPos < mIconCode.length)
                            mUec.createSendFbEventFromTappedView(17,
                                    level1IconObjects[mLevelOneItemPos].getEvent_Tag() + "_" +
                                            mIconCode[mLevelOneItemPos] + "M0", "");
                    }
                }
            }
        });
    }

    private void initLessBtnListener() {
        mIvLess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlgLike = mFlgYes = mFlgMore = mFlgDntLike = mFlgNo = GlobalConstants.SHORT_SPEECH;
                mFlgImage = GlobalConstants.LESS;
                LevelUiUtils.setExpressiveIconPressedState(expressiveBtn, mFlgImage);
                if (!mShouldReadFullSpeech) {
                    if (mFlgLess == GlobalConstants.LONG_SPEECH) {
                        speakAndShowTextBar_(mExprBtnTxt[11]);
                        mFlgLess = GlobalConstants.SHORT_SPEECH;
                        mUec.createSendFbEventFromTappedView(11, "", "");
                    } else {
                        speakAndShowTextBar_(mExprBtnTxt[10]);
                        mFlgLess = GlobalConstants.LONG_SPEECH;
                        mUec.createSendFbEventFromTappedView(10, "", "");
                    }
                } else {
                    ++mActionBtnClickCount;
                    if (mSelectedItemAdapterPos >= 0 && mSelectedItemAdapterPos < mRecyclerItemsViewList.size() && mRecyclerItemsViewList.get(mSelectedItemAdapterPos) != null)
                        LevelUiUtils.setBorderToCategoryIcon(requireActivity(),
                                mRecyclerItemsViewList.get(mSelectedItemAdapterPos), true,
                                mActionBtnClickCount, mFlgImage);
                    if (mFlgLess == GlobalConstants.LONG_SPEECH) {
                        speakAndShowTextBar_(level1IconObjects[mLevelOneItemPos].getSS());
                        mFlgLess = GlobalConstants.SHORT_SPEECH;
                        if (mLevelOneItemPos < mIconCode.length)
                            mUec.createSendFbEventFromTappedView(24,
                                    level1IconObjects[mLevelOneItemPos].getEvent_Tag() + "_" +
                                            mIconCode[mLevelOneItemPos] + "SS", "");
                    } else {
                        speakAndShowTextBar_(level1IconObjects[mLevelOneItemPos].getS());
                        mFlgLess = GlobalConstants.LONG_SPEECH;
                        if (mLevelOneItemPos < mIconCode.length)
                            mUec.createSendFbEventFromTappedView(23,
                                    level1IconObjects[mLevelOneItemPos].getEvent_Tag() + "_" +
                                            mIconCode[mLevelOneItemPos] + "S0", "");
                    }
                }
            }
        });
    }

    public void tappedCategoryItemEvent(final View view, int position) {
        if (level1IconObjects[position].getEvent_Tag().equals(ADD_BASIC_CUSTOM_ICON)) {
            Bundle args = new Bundle();
            args.putBoolean(IS_HOME_CUSTOM_ICON, true);
            args.putBoolean(IS_HOME_CATEGORY, true);
            DialogAddEditIcon dialog = DialogAddEditIcon.newInstance(args, null);
            dialog.show(getParentFragmentManager(), DialogAddEditIcon.class.getSimpleName());
            return;
        }
        mFlgLike = mFlgYes = mFlgMore = mFlgDntLike = mFlgNo = mFlgLess = GlobalConstants.SHORT_SPEECH;
        mIvHome.setImageResource(R.drawable.home);

        if (!getSession().getBasicCustomIconAddState())
            LevelUiUtils.setExpressiveIconPressedState(expressiveBtn, GlobalConstants.NO_EXPR);
        LevelUiUtils.resetRecyclerAllItems(requireActivity(), mRecyclerView,
                mActionBtnClickCount, mFlgImage);
        mActionBtnClickCount = 0;
        LevelUiUtils.setBorderToCategoryIcon(requireActivity(), view, true,
                mActionBtnClickCount, mFlgImage);
        mShouldReadFullSpeech = true;
        String title = level1IconObjects[position].getDisplay_Label().replace("…", "");
        if (!getLevelActivity().isAccessibilityTalkBackOn((AccessibilityManager) requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE))) {
            if (mLevelOneItemPos == position) {
                Bundle bundle = new Bundle();
                bundle.putString("Icon", "Opened " + level1IconObjects[position].
                        getDisplay_Label().replace("…", ""));
                bundleEvent("Grid", bundle);

                title = getString(R.string.home) + "/ " + title;
                Bundle args = new Bundle();
                args.putInt(getString(R.string.level_one_intent_pos_tag), position);
                args.putString(getString(R.string.intent_menu_path_tag), title + "/");
                NavHostFragment.findNavController(LevelOneFragment.this).navigate(R.id.action_levelOneFragment_to_levelTwoFragment, args);
            } else {
                getLevelActivity().animateIfEnabled();
                if (!mSearched)
                    speakAndShowTextBar_(level1IconObjects[position].getSpeech_Label());
                else
                    getLevelActivity().speakWithDelay(level1IconObjects[position].getSpeech_Label());
                mSearched = false;
                mUec.createSendFbEventFromTappedView(12,
                        level1IconObjects[position].getDisplay_Label().replace("…", ""), "");
            }
        } else {
            showAccessibleDialog(position, title, view);
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            mUec.createSendFbEventFromTappedView(12, level1IconObjects[position].
                    getDisplay_Label().replace("…", ""), "");
        }
        mLevelOneItemPos = mRecyclerView.getChildLayoutPosition(view);
        if (!getSession().getBasicCustomIconAddState())
            LevelUiUtils.setExpressiveIconConditionally(expressiveBtn, level1IconObjects[mLevelOneItemPos]);

        mSelectedItemAdapterPos = mRecyclerView.getChildAdapterPosition(view);
    }

    private void showAccessibleDialog(final int position, final String title, final View disabledView) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(requireActivity());
        final View mView = getLayoutInflater().inflate(R.layout.dialog_layout, null);

        final Button enterCategory = mView.findViewById(R.id.enterCategory);
        final Button closeDialog = mView.findViewById(R.id.btnClose);
        ImageView ivLike = mView.findViewById(R.id.ivlike);
        ImageView ivYes = mView.findViewById(R.id.ivyes);
        ImageView ivAdd = mView.findViewById(R.id.ivadd);
        ImageView ivDisLike = mView.findViewById(R.id.ivdislike);
        ImageView ivNo = mView.findViewById(R.id.ivno);
        ImageView ivMinus = mView.findViewById(R.id.ivminus);
        ImageView ivBack = mView.findViewById(R.id.back);
        ImageView ivHome = mView.findViewById(R.id.home);
        ImageView ivKeyboard = mView.findViewById(R.id.keyboard);
        ViewCompat.setAccessibilityDelegate(ivLike, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(ivYes, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(ivAdd, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(ivDisLike, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(ivNo, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(ivMinus, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(ivBack, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(ivHome, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(ivKeyboard, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(enterCategory, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(closeDialog, new TalkbackHints_SingleClick());
        mBuilder.setView(mView);
        getLevelActivity().applyMonochromeColor(mView);

        final AlertDialog dialog = mBuilder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        final ImageView[] expressiveBtns = {ivLike, ivYes, ivAdd, ivDisLike, ivNo, ivMinus};

        ivLike.setOnClickListener(v -> {
            mIvLike.performClick();
            LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.LIKE);
        });
        ivYes.setOnClickListener(v -> {
            mIvYes.performClick();
            LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.YES);
        });
        ivAdd.setOnClickListener(v -> {
            mIvMore.performClick();
            LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.MORE);
        });
        ivDisLike.setOnClickListener(v -> {
            mIvDontLike.performClick();
            LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.DONT_LIKE);
        });
        ivNo.setOnClickListener(v -> {
            mIvNo.performClick();
            LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.NO);
        });
        ivMinus.setOnClickListener(v -> {
            mIvLess.performClick();
            LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.LESS);
        });

        ivBack.setEnabled(false);
        ivBack.setAlpha(GlobalConstants.DISABLE_ALPHA);
        ivBack.setOnClickListener(null);
        ivHome.setOnClickListener(v -> {
            mIvHome.performClick();
            dialog.dismiss();
        });
        ivKeyboard.setOnClickListener(v -> {
            mUec.sendEventIfAny("");
            mIvKeyboard.performClick();
            dialog.dismiss();
        });

        enterCategory.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("Icon", "Opened " + level1IconObjects[position].getDisplay_Label()
                    .replace("…", ""));
            bundleEvent("Grid", bundle);

            Bundle args = new Bundle();
            args.putInt(getString(R.string.level_one_intent_pos_tag), position);
            args.putString(getString(R.string.intent_menu_path_tag), getString(R.string.home) + "/ " + title + "/");
            NavHostFragment.findNavController(LevelOneFragment.this).navigate(R.id.action_levelOneFragment_to_levelTwoFragment, args);
            dialog.dismiss();
        });

        enterCategory.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
                super.onPopulateAccessibilityEvent(host, event);
                if (event.getEventType() != AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
                    mView.findViewById(R.id.txTitleHidden).
                            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }
            }
        });

        closeDialog.setOnClickListener(v -> {
            clearSelectionAfterAccessibilityDialogClose();
            dialog.dismiss();
        });

        dialog.setOnDismissListener(dialog1 -> disabledView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES));

        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        }
        dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        if (dialog.getWindow() != null) {
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes(lp);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && getLevelActivity().isNotchDevice()) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }

    private void clearSelectionAfterAccessibilityDialogClose() {
        resetRecyclerMenuItemsAndFlags();
        mUec.sendEventIfAny("");
        LevelUiUtils.setExpressiveIconPressedState(expressiveBtn, GlobalConstants.NO_EXPR);
        mShouldReadFullSpeech = false;
        mFlgImage = GlobalConstants.NO_EXPR;
    }

    public void gotoHome(boolean isUserRedirected) {
        getLevelActivity().setupActionBarTitle(getView(), View.GONE, txtHome);
        mFlgLike = mFlgYes = mFlgMore = mFlgDntLike = mFlgNo = mFlgLess = GlobalConstants.SHORT_SPEECH;
        mIvLike.setImageResource(R.drawable.like);
        mIvDontLike.setImageResource(R.drawable.dontlike);
        mIvYes.setImageResource(R.drawable.yes);
        mIvNo.setImageResource(R.drawable.no);
        mIvMore.setImageResource(R.drawable.more);
        mIvLess.setImageResource(R.drawable.less);
        resetRecyclerMenuItemsAndFlags();
        mShouldReadFullSpeech = false;
        mFlgImage = GlobalConstants.NO_EXPR;

        mIvHome.setImageResource(R.drawable.home_pressed);
        singleEvent("Navigation", "Home");
        if (!isUserRedirected) {
            speakAndShowTextBar_(mNavigationBtnTxt[0]);
        }
    }

    @Override
    public void onEditIconClicked(int position) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_HOME_CUSTOM_ICON, true);
        bundle.putBoolean(IS_HOME_CATEGORY, true);
        bundle.putString(BASIC_ICON_ID, level1IconObjects[position].getEvent_Tag());
        JellowIcon icon = new JellowIcon(
                level1IconObjects[position].getDisplay_Label(),
                level1IconObjects[position].getEvent_Tag(),
                -1, -1, -1
        );
        bundle.putSerializable(JELLOW_ID, icon);
        DialogAddEditIcon dialog = DialogAddEditIcon.newInstance(bundle, null);
        dialog.show(getParentFragmentManager(), DialogAddEditIcon.class.getSimpleName());
    }

    @Override
    public void onDeleteIconClicked(int position) {
        final DialogCustom dialog = new DialogCustom(requireContext());
        dialog.setText(getString(R.string.icon_delete_warning).replace("-",
                level1IconObjects[position].getDisplay_Label()));
        dialog.setOnPositiveClickListener(() -> {
            CustomBasicIconHelper.deleteCustomBasicIcon(
                    requireActivity(),
                    getAppDatabase(),
                    level1IconObjects[position].getEvent_Tag(),
                    position
            );
            ArrayList<Icon> temp = new ArrayList<>(Arrays.asList(level1IconObjects));
            temp.remove(position);
            level1IconObjects = new Icon[temp.size()];
            temp.toArray(level1IconObjects);
            LevelUiUtils.setBorderToCategoryIcon(requireActivity(), mRecyclerView.getChildAt(position),
                    false, -1, -1);
            mRecyclerView.setAdapter(new LevelOneFragmentAdapter(LevelOneFragment.this, level1IconObjects));
            if (mRecyclerView.getAdapter() != null) {
                mRecyclerView.getAdapter().notifyItemRemoved(position);
            }
            mRecyclerItemsViewList.remove(position);
            LevelUiUtils.enableAllExpressiveIcon(expressiveBtn);
            mRecyclerView.smoothScrollToPosition(position);
        });
        dialog.setOnNegativeClickListener(dialog::cancel);
        dialog.show();
    }

    private void loadArraysFromResources(View view) {
        mIconCode = IconFactory.getL1IconCodes(
                PathFactory.getJSONFile(requireContext()),
                LanguageFactory.getCurrentLanguageCode(requireContext())
        );

        level1IconObjects = TextFactory.getAllIconsArray(
                requireContext(), mIconCode,
                CustomBasicIconHelper.getCustomBasicIcons(getAppDatabase(), getSession().getLanguage(), "00"),
                getSession().getBasicCustomIconAddState()
        );

        loadRecyclerView(view);

        String[] expressiveIcons = IconFactory.getExpressiveIconCodes(
                PathFactory.getJSONFile(requireContext()),
                LanguageFactory.getCurrentLanguageCode(requireContext())
        );
        ExpressiveIcon[] expressiveIconObjects = TextFactory.getExpressiveIconObjects(expressiveIcons);
        mExprBtnTxt = TextFactory.getExpressiveSpeechText(expressiveIconObjects);

        String[] miscellaneousIcons = IconFactory.getMiscellaneousIconCodes(
                PathFactory.getJSONFile(requireContext()),
                LanguageFactory.getCurrentLanguageCode(requireContext())
        );
        MiscellaneousIcon[] miscellaneousIconObjects = TextFactory.getMiscellaneousIconObjects(miscellaneousIcons);
        mNavigationBtnTxt = TextFactory.getTitle(miscellaneousIconObjects);
    }

    private void resetRecyclerMenuItemsAndFlags() {
        mIvHome.setImageResource(R.drawable.home_pressed);
        mLevelOneItemPos = GlobalConstants.NOT_SELECTED;
        LevelUiUtils.resetRecyclerAllItems(requireActivity(), mRecyclerView,
                mActionBtnClickCount, mFlgImage);
        mActionBtnClickCount = 0;
    }

    private void authenticateUserIfNot() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            if (getLevelActivity().isConnectedToNetwork((ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE))) {
                performManualLogin(mAuth);
            } else {
                showSignInSnackBar();
            }
        }
    }

    private void performManualLogin(FirebaseAuth mAuth) {
        mAuth.signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && isAdded()) {
                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                        DatabaseReference ref = db.getReference(BuildConfig.DB_TYPE + "/users/" +
                                getSession().getUserId());
                        ref.child("versionCode").setValue(BuildConfig.VERSION_CODE);
                    }
                });
    }

    private void showSignInSnackBar() {
        if (getView() != null) {
            Snackbar.make(getView(), R.string.checkConnectivity, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry, v -> authenticateUserIfNot()).show();
        }
    }

    public void hideCustomKeyboardDialog() {
        mIvKeyboard.setImageResource(R.drawable.keyboard);
        mIvBack.setImageResource(R.drawable.back);
    }
}
