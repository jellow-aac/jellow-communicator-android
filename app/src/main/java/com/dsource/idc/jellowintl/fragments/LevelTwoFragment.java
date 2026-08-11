package com.dsource.idc.jellowintl.fragments;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static com.dsource.idc.jellowintl.factories.IconFactory.getIconCode;
import static com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs.DialogAddVerbiage.JELLOW_ID;
import static com.dsource.idc.jellowintl.models.GlobalConstants.ADD_BASIC_CUSTOM_ICON;
import static com.dsource.idc.jellowintl.models.GlobalConstants.BASIC_ICON_ID;
import static com.dsource.idc.jellowintl.models.GlobalConstants.IS_HOME_CUSTOM_ICON;
import static com.dsource.idc.jellowintl.utility.Analytics.bundleEvent;
import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.singleEvent;
import static com.dsource.idc.jellowintl.utility.Analytics.startMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dsource.idc.jellowintl.Presentor.CustomBasicIconHelper;
import com.dsource.idc.jellowintl.Presentor.PreferencesHelper;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.TalkBack.TalkbackHints_SingleClick;
import com.dsource.idc.jellowintl.fragments.adapters.LevelTwoAdapter;
import com.dsource.idc.jellowintl.fragments.adapters.PeopleAdapter;
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
import com.dsource.idc.jellowintl.utility.CustomGridLayoutManager;
import com.dsource.idc.jellowintl.utility.DialogKeyboardUtterance;
import com.dsource.idc.jellowintl.utility.IndexSorter;
import com.dsource.idc.jellowintl.utility.LevelUiUtils;
import com.dsource.idc.jellowintl.utility.SessionManager;
import com.dsource.idc.jellowintl.utility.UserEventCollector;
import com.dsource.idc.jellowintl.utility.interfaces.BasicCustomIconsChangedListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

public class LevelTwoFragment extends BaseFragment implements BasicCustomIconsChangedListener {
    private final int CATEGORY_ICON_PEOPLE = 5;
    private final int CATEGORY_ICON_HELP = 8;

    /* This flags are used to identify respective expressive button is pressed either
      once or twice. eg. mFlgLike used to identify Like expressive button pressed once or twice.*/
    private int mFlgLike = GlobalConstants.SHORT_SPEECH, mFlgYes = GlobalConstants.SHORT_SPEECH,
            mFlgMore = GlobalConstants.SHORT_SPEECH, mFlgDntLike = GlobalConstants.SHORT_SPEECH,
            mFlgNo = GlobalConstants.SHORT_SPEECH, mFlgLess = GlobalConstants.SHORT_SPEECH;
    /* This flag identifies which expressive button is pressed.*/
    private int mFlgImage = -1;
    /* This flag identifies that user is pressed a category icon and which border should appear
      on pressed category icon. If flag value = 0, then brown (initial border) will appear.*/
    private int mActionBtnClickCount;
    /*Image views which are visible on the layout such as six expressive buttons, below navigation
      buttons and speak button when keyboard is open.*/
    private ImageView mIvLike, mIvDontLike, mIvYes, mIvNo, mIvMore, mIvLess,
            mIvHome, mIvKeyboard, mIvBack;
    /*Recycler view which will populate category icons.*/
    private RecyclerView mRecyclerView;

    /*This variable indicates index of category icon selected in level one and two respectively.*/
    private int mLevelOneItemPos, mLevelTwoItemPos = GlobalConstants.NOT_SELECTED;
    /*This variable indicates index of category icon in adapter in level 2. This variable is
     different than mLevelTwoItemPos. */
    private int mSelectedItemAdapterPos = GlobalConstants.NOT_SELECTED;
    /*This flag is sets to true, when category icon is pressed followed by an expressive button;
     in this case the full sentence associated with selected expressive button is spoken.
     In case flag is false only expressive button is spoken out.*/
    private boolean mShouldReadFullSpeech = false, mSearched = false;
    /*This variable hold the views populated in recycler view (category icon) list.*/
    private ArrayList<View> mRecyclerItemsViewList;
    /*This variable store current action bar title.*/
    private String mActionBarTitle;

    /*Below array stores the speech text, below text, expressive button speech text,
     navigation button speech text respectively.*/
    private String[] mSpeechText, mDisplayText, mExprBtnTxt,
            mNavigationBtnTxt, mIconCode;
    /*Below array stores tap count and index sort array respectively. This variables are used
     only, when in level one people or places category is
      selected.*/
    private Integer[] mArrPeopleTapCount, mArrSort;

    private String txtActionBarTitle, mSpeak, mEnterCat;

    /*Firebase event Collector class instance.*/
    private UserEventCollector mUec;

    private Icon[] level2IconObjects;

    private ImageView[] expressiveBtn;

    private AddIconCallback callback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_levelx_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLevelActivity().setVisibleAct(LevelTwoFragment.class.getSimpleName());

        if (getArguments() != null) {
            mLevelOneItemPos = getArguments().getInt(getString(R.string.level_one_intent_pos_tag), 0);
            txtActionBarTitle = getArguments().getString(getString(R.string.intent_menu_path_tag), "");
        } else {
            mLevelOneItemPos = 0;
            txtActionBarTitle = "";
        }

        setupActionBarTitle(view, View.GONE, txtActionBarTitle);
        setupToolbarMenu(view);
        setupParent();
        applyMonochromeColor(view);
        mUec = new UserEventCollector();

        loadArraysFromResources();
        initializeArrayListOfRecycler();
        initializeLayoutViews(view);
        initializeRecyclerViewAdapter();
        initializeViewListeners();
        mSpeak = getString(R.string.speak);
        mEnterCat = getString(R.string.enter_category);

        if (getArguments() != null && getArguments().getString(getString(R.string.from_search)) != null) {
            if (getArguments().getString(getString(R.string.from_search)).equals(getString(R.string.search_tag))) {
                mSearched = true;
                highlightSearchedItem();
            }
        }

        /*session parameter denotes custom icon add/edit/delete is enabled*/
        if (getSession().getBasicCustomIconAddState()) {
            for (ImageView btn : expressiveBtn) {
                btn.setAlpha(GlobalConstants.DISABLE_ALPHA);
                btn.setEnabled(false);
            }
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mIvBack.performClick();
            }
        });
    }

    //A scrollListener field to listen when recycler view have done populating the data
    private RecyclerView.OnScrollListener scrollListener;
    private void highlightSearchedItem() {
        if (getArguments() == null) return;
        final int p1 = getArguments().getInt(getString(R.string.level_one_intent_pos_tag));
        final int s = getArguments().getInt(getString(R.string.search_parent_1));
        final int gridCode = getSession().getGridSize();
        int gridSize;
        if (gridCode == GlobalConstants.NINE_ICONS_PER_SCREEN)
            gridSize = 8;
        else gridSize = 2;

        if (p1 == CATEGORY_ICON_PEOPLE) {
            final int sortedIndex = getSortedIndex(s);
            if (sortedIndex > gridSize) {
                scrollListener = null;
                scrollListener = getListener(sortedIndex);
                mRecyclerView.addOnScrollListener(scrollListener);
                mRecyclerView.smoothScrollToPosition(sortedIndex);
            } else {
                setSearchHighlight(sortedIndex);
            }
        } else {
            if (s > gridSize) {
                scrollListener = null;
                scrollListener = getListener(s);
                mRecyclerView.addOnScrollListener(scrollListener);
                if (mRecyclerView.getLayoutManager() != null) {
                    mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, s);
                }
            } else {
                setSearchHighlight(s);
            }
        }
    }

    private RecyclerView.OnScrollListener getListener(final int index) {
        scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    setSearchHighlight(index);
                }
            }
        };
        return scrollListener;
    }

    private ViewTreeObserver.OnGlobalLayoutListener populationDoneListener;
    public void setSearchHighlight(final int pos) {
        if (mRecyclerView.getAdapter() != null) {
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
        populationDoneListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mRecyclerItemsViewList != null && pos < mRecyclerItemsViewList.size()) {
                    View searchedView = mRecyclerItemsViewList.get(pos);
                    if (searchedView == null) {
                        mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(populationDoneListener);
                        if (mRecyclerView.getLayoutManager() != null) {
                            mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, pos);
                        }
                        return;
                    }
                    tappedCategoryItemEvent(searchedView, pos);
                    mRecyclerView.removeOnScrollListener(scrollListener);
                    mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(populationDoneListener);
                    if (isAccessibilityTalkBackOn((AccessibilityManager) requireActivity().getSystemService(ACCESSIBILITY_SERVICE))) {
                        searchedView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                    }
                }
            }
        };
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(populationDoneListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLevelActivity().setVisibleAct(LevelTwoFragment.class.getSimpleName());
        getLevelActivity().setupActionBarTitle(getView(), View.GONE, txtActionBarTitle);
        getLevelActivity().setupToolbarMenu(getView());
        if (!isAnalyticsActive()) {
            resetAnalytics(requireContext(), getSession().getUserId());
        }
        startMeasuring();
        if (!getSession().getToastMessage().isEmpty()) {
            Toast.makeText(requireContext(), getSession().getToastMessage(), Toast.LENGTH_SHORT).show();
            getSession().setToastMessage("");
        }
        DialogAddEditIcon.subscribe(callback);
    }

    @Override
    public void onPause() {
        super.onPause();
        long sessionTime = validatePushId(getSession().getSessionCreatedAt());
        getSession().setSessionCreatedAt(sessionTime);
        stopMeasuring(LevelTwoFragment.class.getSimpleName());
    }

    @Override
    public void onDestroyView() {
        mRecyclerView = null;
        mRecyclerItemsViewList = null;
        mExprBtnTxt = null;
        mArrSort = null;
        super.onDestroyView();
    }

    private void clearSelectionAfterAccessibilityDialogClose() {
        LevelUiUtils.enableAllExpressiveIcon(expressiveBtn);
        LevelUiUtils.setExpressiveIconPressedState(expressiveBtn, GlobalConstants.NO_EXPR);
        LevelUiUtils.resetRecyclerAllItems(requireActivity(), mRecyclerView,
                mActionBtnClickCount, mFlgImage);
        mActionBtnClickCount = -1;
        mShouldReadFullSpeech = false;
        mFlgImage = -1;
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
        mIvBack.setAlpha(GlobalConstants.ENABLE_ALPHA);
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

        mRecyclerView = view.findViewById(R.id.recycler_view);

        switch (getSession().getGridSize()) {
            case GlobalConstants.ONE_ICON_PER_SCREEN:
                mRecyclerView.setLayoutManager(new CustomGridLayoutManager(requireContext(), 1, 3));
                break;
            case GlobalConstants.TWO_ICONS_PER_SCREEN:
            case GlobalConstants.FOUR_ICONS_PER_SCREEN:
                mRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
                break;
            case GlobalConstants.THREE_ICONS_PER_SCREEN:
                mRecyclerView.setLayoutManager(new CustomGridLayoutManager(requireContext(), 3, 3));
                break;
            case GlobalConstants.NINE_ICONS_PER_SCREEN:
                mRecyclerView.setLayoutManager(new CustomGridLayoutManager(requireContext(), 3, getSession().getGridSize()));
                break;
        }

        mRecyclerView.setVerticalScrollBarEnabled(true);
        mRecyclerView.setScrollbarFadingEnabled(false);
        expressiveBtn = new ImageView[]{mIvLike, mIvYes, mIvMore, mIvDontLike, mIvNo, mIvLess};

        if (level2IconObjects != null && ((getSession().getBasicCustomIconAddState() && level2IconObjects.length == 1) ||
                (!getSession().getBasicCustomIconAddState() && level2IconObjects.length == 0))) {
            ((TextView) view.findViewById(R.id.place_holder_text)).setText(R.string.home_custom_icon_place_holder_text);
            view.findViewById(R.id.place_holder_text).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.place_holder_text).setVisibility(View.GONE);
        }
    }

    private void initializeRecyclerViewAdapter() {
        if (level2IconObjects == null) {
            initializeArrayListOfRecycler();
        }
        if (mLevelOneItemPos != CATEGORY_ICON_PEOPLE) {
            mRecyclerView.setAdapter(new LevelTwoAdapter(this, level2IconObjects, mIconCode.length));
        } else {
            mRecyclerView.setAdapter(new PeopleAdapter(this, level2IconObjects, mArrSort, mIconCode.length));
        }
    }

    private void initializeViewListeners() {
        initRecyclerViewListeners();
        initBackBtnListener();
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
                ArrayList<Icon> iconsList = new ArrayList<>(Arrays.asList(level2IconObjects));
                iconsList.add(customIcon);
                level2IconObjects = TextFactory.getAllIconsArray(
                        requireActivity(), mIconCode,
                        CustomBasicIconHelper.getCustomBasicIcons(
                                getAppDatabase(),
                                getSession().getLanguage(),
                                "00," + (mLevelOneItemPos < 10 ? "0" + mLevelOneItemPos : mLevelOneItemPos)),
                        getSession().getBasicCustomIconAddState());

                mDisplayText = TextFactory.getDisplayText(level2IconObjects);
                mSpeechText = TextFactory.getSpeechText(level2IconObjects);

                initializeRecyclerViewAdapter();

                if (mRecyclerView.getAdapter() != null) {
                    mRecyclerView.getAdapter().notifyItemRangeChanged(0, level2IconObjects.length);
                }
                mRecyclerItemsViewList.clear();
                while (mRecyclerItemsViewList.size() < level2IconObjects.length)
                    mRecyclerItemsViewList.add(null);

                View rootView = getView();
                if (rootView != null) {
                    if ((getSession().getBasicCustomIconAddState() && level2IconObjects.length == 1) ||
                            (!getSession().getBasicCustomIconAddState() && level2IconObjects.length == 0)) {
                        ((TextView) rootView.findViewById(R.id.place_holder_text)).setText(R.string.home_custom_icon_place_holder_text);
                        rootView.findViewById(R.id.place_holder_text).setVisibility(View.VISIBLE);
                    } else {
                        rootView.findViewById(R.id.place_holder_text).setVisibility(View.GONE);
                    }
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
            public void onChildViewAttachedToWindow(View view) {
                if (mRecyclerItemsViewList != null && mRecyclerView != null) {
                    int pos = mRecyclerView.getChildLayoutPosition(view);
                    if (pos >= 0 && pos < mRecyclerItemsViewList.size()) {
                        mRecyclerItemsViewList.set(pos, view);
                        if (mSelectedItemAdapterPos > GlobalConstants.NOT_SELECTED &&
                                pos == mSelectedItemAdapterPos)
                            LevelUiUtils.setBorderToCategoryIcon(requireActivity(), view, true,
                                    mActionBtnClickCount, mFlgImage);
                    }
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                if (mRecyclerItemsViewList != null && mRecyclerView != null) {
                    LevelUiUtils.setBorderToCategoryIcon(requireActivity(), view, false,
                            mActionBtnClickCount, mFlgImage);
                    int pos = mRecyclerView.getChildLayoutPosition(view);
                    if (pos >= 0 && pos < mRecyclerItemsViewList.size()) {
                        mRecyclerItemsViewList.set(pos, null);
                    }
                }
            }
        });
    }

    private void initBackBtnListener() {
        mIvBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                singleEvent("Navigation", "Back");
                speakAndShowTextBar_(mNavigationBtnTxt[1]);
                mUec.createSendFbEventFromTappedView(27, "", "");
                mIvBack.setImageResource(R.drawable.back_pressed);
                NavHostFragment.findNavController(LevelTwoFragment.this).popBackStack();
            }
        });
    }

    private void initHomeBtnListener() {
        mIvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUec.createSendFbEventFromTappedView(26, "", "");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        speakAndShowTextBar_(mNavigationBtnTxt[0]);
                    }
                }).start();
                mIvHome.setImageResource(R.drawable.home_pressed);
                mIvKeyboard.setImageResource(R.drawable.keyboard);
                NavHostFragment.findNavController(LevelTwoFragment.this).popBackStack(R.id.mainFragment, false);
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
                if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1)
                    setExpressiveButtonToAboutMe(mFlgImage);
                else
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
                    if (mRecyclerItemsViewList.get(mSelectedItemAdapterPos) != null)
                        LevelUiUtils.setBorderToCategoryIcon(requireActivity(),
                                mRecyclerItemsViewList.get(mSelectedItemAdapterPos), true,
                                mActionBtnClickCount, mFlgImage);
                    if (mFlgLike == GlobalConstants.LONG_SPEECH) {
                        if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE) {
                            speakAndShowTextBar_(level2IconObjects[mArrSort[mLevelTwoItemPos]].getLL());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(14,
                                        level2IconObjects[mArrSort[mLevelTwoItemPos]].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "LL", "");
                        } else if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1) {
                            if (isNoTTSLanguage()) {
                                speakInQueue(level2IconObjects[mLevelTwoItemPos].getLL());
                            } else {
                                String speechTxt = level2IconObjects[mLevelTwoItemPos].getL()
                                        .replace("_", getSession().getName());
                                speakAndShowTextBar_(speechTxt);
                            }
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(14,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "LL", "");
                        } else {
                            speakAndShowTextBar_(level2IconObjects[mLevelTwoItemPos].getLL());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(14,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "LL", "");
                        }
                        mFlgLike = GlobalConstants.SHORT_SPEECH;
                    } else {
                        if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE) {
                            speakAndShowTextBar_(level2IconObjects[mArrSort[mLevelTwoItemPos]].getL());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(13,
                                        level2IconObjects[mArrSort[mLevelTwoItemPos]].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "L0", "");
                        } else if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1) {
                            if (isNoTTSLanguage()) {
                                speakInQueue(level2IconObjects[mLevelTwoItemPos].getL());
                            } else {
                                String speechTxt = level2IconObjects[mLevelTwoItemPos].getL()
                                        .replace("_", getSession().getName());
                                speakAndShowTextBar_(speechTxt);
                            }
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(13,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "L0", "");
                        } else {
                            speakAndShowTextBar_(level2IconObjects[mLevelTwoItemPos].getL());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(13,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "L0", "");
                        }
                        mFlgLike = GlobalConstants.LONG_SPEECH;
                    }
                }
                mIvBack.setImageResource(R.drawable.back);
            }
        });
    }

    private void initDontLikeBtnListener() {
        mIvDontLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlgLike = mFlgYes = mFlgMore = mFlgNo = mFlgLess = GlobalConstants.SHORT_SPEECH;
                mFlgImage = GlobalConstants.DONT_LIKE;
                if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1)
                    setExpressiveButtonToAboutMe(mFlgImage);
                else
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
                    if (mRecyclerItemsViewList.get(mSelectedItemAdapterPos) != null)
                        LevelUiUtils.setBorderToCategoryIcon(requireActivity(),
                                mRecyclerItemsViewList.get(mSelectedItemAdapterPos), true,
                                mActionBtnClickCount, mFlgImage);

                    if (mFlgDntLike == GlobalConstants.LONG_SPEECH) {
                        if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE) {
                            speakAndShowTextBar_(level2IconObjects[mArrSort[mLevelTwoItemPos]].getDD());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(20,
                                        level2IconObjects[mArrSort[mLevelTwoItemPos]].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "DD", "");
                        } else if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1) {
                            if (isNoTTSLanguage()) {
                                speakInQueue(level2IconObjects[mLevelTwoItemPos].getDD());
                            } else {
                                String speechTxt = level2IconObjects[mLevelTwoItemPos].getDD()
                                        .replace("_", getSession().getCaregiverName());
                                speakAndShowTextBar_(speechTxt);
                            }
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(20,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "DD", "");
                        } else {
                            speakAndShowTextBar_(level2IconObjects[mLevelTwoItemPos].getDD());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(20,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "DD", "");
                        }
                        mFlgDntLike = GlobalConstants.SHORT_SPEECH;
                    } else {
                        if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE) {
                            speakAndShowTextBar_(level2IconObjects[mArrSort[mLevelTwoItemPos]].getD());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(19,
                                        level2IconObjects[mArrSort[mLevelTwoItemPos]].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "D0", "");
                        } else if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1) {
                            if (isNoTTSLanguage()) {
                                speakInQueue(level2IconObjects[mLevelTwoItemPos].getDD());
                            } else {
                                String speechTxt = level2IconObjects[mLevelTwoItemPos].getDD()
                                        .replace("_", getSession().getCaregiverName());
                                speakAndShowTextBar_(speechTxt);
                            }
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(19,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "D0", "");
                        } else {
                            speakAndShowTextBar_(level2IconObjects[mLevelTwoItemPos].getD());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(19,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "D0", "");
                        }
                        mFlgDntLike = GlobalConstants.LONG_SPEECH;
                    }
                }
                mIvBack.setImageResource(R.drawable.back);
            }
        });
    }

    private void initYesBtnListener() {
        mIvYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlgLike = mFlgMore = mFlgDntLike = mFlgNo = mFlgLess = GlobalConstants.SHORT_SPEECH;
                mFlgImage = GlobalConstants.YES;
                if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1)
                    setExpressiveButtonToAboutMe(mFlgImage);
                else
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
                    if (mRecyclerItemsViewList.get(mSelectedItemAdapterPos) != null)
                        LevelUiUtils.setBorderToCategoryIcon(requireActivity(),
                                mRecyclerItemsViewList.get(mSelectedItemAdapterPos), true,
                                mActionBtnClickCount, mFlgImage);
                    if (mFlgYes == GlobalConstants.LONG_SPEECH) {
                        if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE) {
                            speakAndShowTextBar_(level2IconObjects[mArrSort[mLevelTwoItemPos]].getYY());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(16,
                                        level2IconObjects[mArrSort[mLevelTwoItemPos]].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "YY", "");
                        } else if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1) {
                            if (isNoTTSLanguage()) {
                                speakInQueue(level2IconObjects[mLevelTwoItemPos].getYY());
                            } else {
                                String speechTxt = level2IconObjects[mLevelTwoItemPos].getYY();
                                speechTxt = speechTxt.replace("_", getSession().getEmailId()
                                        .replaceAll(".", ",$0 ").replace(".", "dot"));
                                speakAndShowTextBar_(speechTxt);
                            }
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(16,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "YY", "");
                        } else {
                            speakAndShowTextBar_(level2IconObjects[mLevelTwoItemPos].getYY());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(16,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "YY", "");
                        }
                        mFlgYes = GlobalConstants.SHORT_SPEECH;
                    } else {
                        if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE) {
                            speakAndShowTextBar_(level2IconObjects[mArrSort[mLevelTwoItemPos]].getY());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(15,
                                        level2IconObjects[mArrSort[mLevelTwoItemPos]].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "Y0", "");
                        } else if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1) {
                            if (isNoTTSLanguage()) {
                                speakInQueue(level2IconObjects[mLevelTwoItemPos].getY());
                            } else {
                                String speechTxt = level2IconObjects[mLevelTwoItemPos].getY();
                                speechTxt = speechTxt.replace("_", getSession().getEmailId()
                                        .replaceAll(".", ",$0 ").replace(".", "dot"));
                                speakAndShowTextBar_(speechTxt);
                            }
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(15,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "Y0", "");
                        } else {
                            speakAndShowTextBar_(level2IconObjects[mLevelTwoItemPos].getY());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(15,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "Y0", "");
                        }
                        mFlgYes = GlobalConstants.LONG_SPEECH;
                    }
                }
                mIvBack.setImageResource(R.drawable.back);
            }
        });
    }

    private void initNoBtnListener() {
        mIvNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlgLike = mFlgYes = mFlgMore = mFlgDntLike = mFlgLess = GlobalConstants.SHORT_SPEECH;
                mFlgImage = GlobalConstants.NO;
                if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1)
                    setExpressiveButtonToAboutMe(mFlgImage);
                else
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
                    if (mRecyclerItemsViewList.get(mSelectedItemAdapterPos) != null)
                        LevelUiUtils.setBorderToCategoryIcon(requireActivity(),
                                mRecyclerItemsViewList.get(mSelectedItemAdapterPos), true,
                                mActionBtnClickCount, mFlgImage);
                    if (mFlgNo == GlobalConstants.LONG_SPEECH) {
                        if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE) {
                            speakAndShowTextBar_(level2IconObjects[mArrSort[mLevelTwoItemPos]].getNN());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(22,
                                        level2IconObjects[mArrSort[mLevelTwoItemPos]].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "NN", "");
                        } else if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1) {
                            if (isNoTTSLanguage()) {
                                speakInQueue(level2IconObjects[mLevelTwoItemPos].getNN());
                            } else {
                                String speechTxt = level2IconObjects[mLevelTwoItemPos].getNN()
                                        .replace("_", getSession().getAddress());
                                speakAndShowTextBar_(speechTxt);
                            }
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(22,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "NN", "");
                        } else {
                            speakAndShowTextBar_(level2IconObjects[mLevelTwoItemPos].getNN());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(22,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "NN", "");
                        }
                        mFlgNo = GlobalConstants.SHORT_SPEECH;
                    } else {
                        if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE) {
                            speakAndShowTextBar_(level2IconObjects[mArrSort[mLevelTwoItemPos]].getN());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(21,
                                        level2IconObjects[mArrSort[mLevelTwoItemPos]].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "N0", "");
                        } else if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1) {
                            if (isNoTTSLanguage()) {
                                speakInQueue(level2IconObjects[mLevelTwoItemPos].getN());
                            } else {
                                String speechTxt = level2IconObjects[mLevelTwoItemPos].getNN()
                                        .replace("_", getSession().getAddress());
                                speakAndShowTextBar_(speechTxt);
                            }
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(21,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "N0", "");
                        } else {
                            speakAndShowTextBar_(level2IconObjects[mLevelTwoItemPos].getN());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(21,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "N0", "");
                        }
                        mFlgNo = GlobalConstants.LONG_SPEECH;
                    }
                }
                mIvBack.setImageResource(R.drawable.back);
            }
        });
    }

    private void initMoreBtnListener() {
        mIvMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlgLike = mFlgYes = mFlgDntLike = mFlgNo = mFlgLess = GlobalConstants.SHORT_SPEECH;
                mFlgImage = GlobalConstants.MORE;
                if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1)
                    setExpressiveButtonToAboutMe(mFlgImage);
                else
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
                    if (mRecyclerItemsViewList.get(mSelectedItemAdapterPos) != null)
                        LevelUiUtils.setBorderToCategoryIcon(requireActivity(),
                                mRecyclerItemsViewList.get(mSelectedItemAdapterPos), true,
                                mActionBtnClickCount, mFlgImage);
                    if (mFlgMore == GlobalConstants.LONG_SPEECH) {
                        if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE) {
                            speakAndShowTextBar_(level2IconObjects[mArrSort[mLevelTwoItemPos]].getMM());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(18,
                                        level2IconObjects[mArrSort[mLevelTwoItemPos]].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "MM", "");
                        } else if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1) {
                            if (isNoTTSLanguage()) {
                                speakInQueue(level2IconObjects[mLevelTwoItemPos].getMM());
                            } else {
                                String speechTxt = level2IconObjects[mLevelTwoItemPos].getMM();
                                speechTxt = speechTxt.replace("_", getContactWithSpacesBetweenDigits());
                                speakAndShowTextBar_(speechTxt);
                            }
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(18,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "MM", "");
                        } else {
                            speakAndShowTextBar_(level2IconObjects[mLevelTwoItemPos].getMM());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(18,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "MM", "");
                        }
                        mFlgMore = GlobalConstants.SHORT_SPEECH;
                    } else {
                        if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE) {
                            speakAndShowTextBar_(level2IconObjects[mArrSort[mLevelTwoItemPos]].getM());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(17,
                                        level2IconObjects[mArrSort[mLevelTwoItemPos]].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "M0", "");
                        } else if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1) {
                            if (isNoTTSLanguage()) {
                                speakInQueue(level2IconObjects[mLevelTwoItemPos].getM());
                            } else {
                                String speechTxt = level2IconObjects[mLevelTwoItemPos].getMM();
                                speechTxt = speechTxt.replace("_", getContactWithSpacesBetweenDigits());
                                speakAndShowTextBar_(speechTxt);
                            }
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(17,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "M0", "");
                        } else {
                            speakAndShowTextBar_(level2IconObjects[mLevelTwoItemPos].getM());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(17,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "M0", "");
                        }
                        mFlgMore = GlobalConstants.LONG_SPEECH;
                    }
                }
                mIvBack.setImageResource(R.drawable.back);
            }
        });
    }

    private void initLessBtnListener() {
        mIvLess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlgLike = mFlgYes = mFlgMore = mFlgDntLike = mFlgNo = GlobalConstants.SHORT_SPEECH;
                mFlgImage = GlobalConstants.LESS;
                if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1)
                    setExpressiveButtonToAboutMe(mFlgImage);
                else
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
                    if (mRecyclerItemsViewList.get(mSelectedItemAdapterPos) != null)
                        LevelUiUtils.setBorderToCategoryIcon(requireActivity(),
                                mRecyclerItemsViewList.get(mSelectedItemAdapterPos), true,
                                mActionBtnClickCount, mFlgImage);

                    if (mFlgLess == GlobalConstants.LONG_SPEECH) {
                        if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE) {
                            speakAndShowTextBar_(level2IconObjects[mArrSort[mLevelTwoItemPos]].getSS());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(24,
                                        level2IconObjects[mArrSort[mLevelTwoItemPos]].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "SS", "");
                        } else if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1) {
                            if (isNoTTSLanguage()) {
                                speakInQueue(level2IconObjects[mLevelTwoItemPos].getSS());
                            } else {
                                String speechTxt = level2IconObjects[mLevelTwoItemPos].getS()
                                        .replace("_", getBloodGroup());
                                speakAndShowTextBar_(speechTxt);
                            }
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(24,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "SS", "");
                        } else {
                            speakAndShowTextBar_(level2IconObjects[mLevelTwoItemPos].getSS());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(24,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "SS", "");
                        }
                        mFlgLess = GlobalConstants.SHORT_SPEECH;
                    } else {
                        if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE) {
                            speakAndShowTextBar_(level2IconObjects[mArrSort[mLevelTwoItemPos]].getS());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(23,
                                        level2IconObjects[mArrSort[mLevelTwoItemPos]].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "S0", "");
                        } else if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1) {
                            if (isNoTTSLanguage()) {
                                speakInQueue(level2IconObjects[mLevelTwoItemPos].getS());
                            } else {
                                String speechTxt = level2IconObjects[mLevelTwoItemPos].getS()
                                        .replace("_", getBloodGroup());
                                speakAndShowTextBar_(speechTxt);
                            }
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(23,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "S0", "");
                        } else {
                            speakAndShowTextBar_(level2IconObjects[mLevelTwoItemPos].getS());
                            if (mLevelTwoItemPos < mIconCode.length)
                                mUec.createSendFbEventFromTappedView(23,
                                        level2IconObjects[mLevelTwoItemPos].getEvent_Tag()
                                                + "_" + mIconCode[mLevelTwoItemPos] + "S0", "");
                        }
                        mFlgLess = GlobalConstants.LONG_SPEECH;
                    }
                }
                mIvBack.setImageResource(R.drawable.back);
            }
        });
    }

    public void tappedCategoryItemEvent(View view, int position) {
        if (level2IconObjects[position].getEvent_Tag().equals(ADD_BASIC_CUSTOM_ICON)) {
            Bundle args = new Bundle();
            args.putBoolean(IS_HOME_CUSTOM_ICON, true);
            args.putInt(getString(R.string.level_one_intent_pos_tag), mLevelOneItemPos);
            DialogAddEditIcon dialog = DialogAddEditIcon.newInstance(args, null);
            dialog.show(getParentFragmentManager(), DialogAddEditIcon.class.getSimpleName());
            return;
        }
        mFlgLike = mFlgYes = mFlgMore = mFlgDntLike = mFlgNo = mFlgLess = GlobalConstants.SHORT_SPEECH;
        if (!getSession().getBasicCustomIconAddState())
            LevelUiUtils.setExpressiveIconPressedState(expressiveBtn, GlobalConstants.NO_EXPR);
        LevelUiUtils.resetRecyclerAllItems(requireActivity(), mRecyclerView,
                mActionBtnClickCount, mFlgImage);
        mActionBtnClickCount = 0;
        LevelUiUtils.setBorderToCategoryIcon(requireActivity(), view, true,
                mActionBtnClickCount, mFlgImage);
        mShouldReadFullSpeech = true;
        String title = (getArguments() != null ? getArguments().getString(getString(R.string.intent_menu_path_tag), "") : "") + " ";

        if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE) {
            if (isAccessibilityTalkBackOn((AccessibilityManager) requireActivity().getSystemService(ACCESSIBILITY_SERVICE))) {
                showAccessibleDialog(position, view);
                view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                mUec.accessibilityPopupOpenedEvent(mSpeechText[position]);
            } else {
                animateIfEnabled();
                if (!mSearched)
                    speakAndShowTextBar_(mSpeechText[position]);
                else
                    speakWithDelay(mSpeechText[position]);
                mSearched = false;
                mUec.createSendFbEventFromTappedView(12, mDisplayText[position], "");
            }
        } else if (
                (mLevelTwoItemPos == position && mLevelOneItemPos < 9 && mLevelOneItemPos != CATEGORY_ICON_HELP) ||
                        (mLevelTwoItemPos == position && CustomBasicIconHelper.givenCustomIconIsCategory(
                                getAppDatabase(), level2IconObjects[position].getEvent_Tag()
                        ))
        ) {
            if (isAccessibilityTalkBackOn((AccessibilityManager) requireActivity().getSystemService(ACCESSIBILITY_SERVICE))) {
                showAccessibleDialog(position, view);
                view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            } else {
                Bundle bundle = new Bundle();
                bundle.putString("Icon", "Opened " + mDisplayText[position].replace("…", ""));
                bundleEvent("Grid", bundle);

                Bundle args = new Bundle();
                args.putInt(getString(R.string.level_one_intent_pos_tag), mLevelOneItemPos);
                args.putInt(getString(R.string.level_2_item_pos_tag), mLevelTwoItemPos);
                args.putString(getString(R.string.intent_menu_path_tag), mActionBarTitle + "/");

                int CATEGORY_ICON_DAILY_ACT = 1;
                if (mLevelOneItemPos == CATEGORY_ICON_DAILY_ACT && LevelUiUtils.isSequencePosition(position)) {
                    NavHostFragment.findNavController(LevelTwoFragment.this).navigate(R.id.action_levelTwoFragment_to_sequenceFragment, args);
                } else {
                    NavHostFragment.findNavController(LevelTwoFragment.this).navigate(R.id.action_levelTwoFragment_to_levelThreeFragment, args);
                }
            }
        } else {
            if (mLevelOneItemPos == CATEGORY_ICON_HELP && position == 0 &&
                    getSession().isCallingEnabled() &&
                    isDeviceReadyToCall((TelephonyManager) requireActivity().getSystemService(Context.TELEPHONY_SERVICE))) {
            } else {
                if (isAccessibilityTalkBackOn((AccessibilityManager) requireActivity().getSystemService(ACCESSIBILITY_SERVICE)) &&
                        mLevelOneItemPos == CATEGORY_ICON_HELP) {
                    showAccessibleDialog(position, view);
                    view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                    mUec.accessibilityPopupOpenedEvent(mDisplayText[position]);
                } else if (isAccessibilityTalkBackOn((AccessibilityManager) requireActivity().getSystemService(ACCESSIBILITY_SERVICE))) {
                    showAccessibleDialog(position, view);
                    view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                    mUec.createSendFbEventFromTappedView(12, mDisplayText[position]
                            .replace("…", ""), "");
                } else {
                    animateIfEnabled();
                    if (!mSearched)
                        speakAndShowTextBar_(mSpeechText[position]);
                    else
                        speakWithDelay(mSpeechText[position]);
                    mSearched = false;
                    mUec.createSendFbEventFromTappedView(12, mDisplayText[position]
                            .replace("…", ""), "");
                }
            }
        }
        mLevelTwoItemPos = mRecyclerView.getChildLayoutPosition(view);
        mSelectedItemAdapterPos = mRecyclerView.getChildAdapterPosition(view);

        if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE ||
                mLevelOneItemPos == CATEGORY_ICON_HELP)
            title += mDisplayText[mLevelTwoItemPos];
        else
            title += mDisplayText[mLevelTwoItemPos].substring(0, mDisplayText[mLevelTwoItemPos].length() - 1);
        mActionBarTitle = title;

        if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE && level2IconObjects.length == mIconCode.length)
            LevelUiUtils.incrementTouchCountOfItem(mArrPeopleTapCount, mArrSort,
                    mLevelTwoItemPos, mLevelOneItemPos, -1,
                    LanguageFactory.getCurrentLanguageCode(requireContext()), getAppDatabase());

        if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 1)
            setExpressiveButtonToAboutMe(-1);
        if (mLevelOneItemPos == CATEGORY_ICON_HELP && mLevelTwoItemPos == 0 &&
                getSession().isCallingEnabled()) {
            stopSpeaking();
            LevelUiUtils.startCall(requireActivity(),
                    "tel:" + getSession().getCaregiverNumber());
        }
        if (!getSession().getBasicCustomIconAddState())
            LevelUiUtils.setExpressiveIconConditionally(expressiveBtn, level2IconObjects[mLevelTwoItemPos]);
        mIvBack.setImageResource(R.drawable.back);
    }

    private void initializeArrayListOfRecycler() {
        int size = mDisplayText.length;
        mRecyclerItemsViewList = new ArrayList<>(size);
        while (mRecyclerItemsViewList.size() <= size) mRecyclerItemsViewList.add(null);
    }

    private void loadArraysFromResources() {
        mIconCode = IconFactory.getL2IconCodes(
                PathFactory.getJSONFile(requireContext()),
                LanguageFactory.getCurrentLanguageCode(requireContext()),
                getLevel2IconCode(mLevelOneItemPos)
        );

        level2IconObjects = TextFactory.getAllIconsArray(
                requireActivity(), mIconCode,
                CustomBasicIconHelper.getCustomBasicIcons(
                        getAppDatabase(),
                        getSession().getLanguage(),
                        "00," + (mLevelOneItemPos < 10 ? "0" + mLevelOneItemPos : mLevelOneItemPos)
                ),
                getSession().getBasicCustomIconAddState()
        );
        mDisplayText = TextFactory.getDisplayText(level2IconObjects);
        mSpeechText = TextFactory.getSpeechText(level2IconObjects);

        if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE && level2IconObjects.length == mIconCode.length) {
            useSortToLoadArray(mSpeechText, mDisplayText, mIconCode);
        }

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

    private void showAccessibleDialog(final int position, final View disabledView) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(requireActivity());
        final View mView = getLayoutInflater().inflate(R.layout.dialog_layout, null);

        Button enterCategory = mView.findViewById(R.id.enterCategory);
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

        ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIvLike.performClick();
                LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.LIKE);
            }
        });
        ivYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIvYes.performClick();
                LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.YES);
            }
        });
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIvMore.performClick();
                LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.MORE);
            }
        });
        ivDisLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIvDontLike.performClick();
                LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.DONT_LIKE);
            }
        });
        ivNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIvNo.performClick();
                LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.NO);
            }
        });
        ivMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIvLess.performClick();
                LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.LESS);
            }
        });
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE ||
                        mLevelOneItemPos == CATEGORY_ICON_HELP) {
                    mUec.clearPendingEvent();
                }
                mIvBack.performClick();
                dialog.dismiss();
            }
        });
        ivHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE ||
                        mLevelOneItemPos == CATEGORY_ICON_HELP) {
                    mUec.clearPendingEvent();
                }
                mIvHome.performClick();
                dialog.dismiss();
            }
        });
        ivKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE ||
                        mLevelOneItemPos == CATEGORY_ICON_HELP) {
                    mUec.clearPendingEvent();
                }
                clearSelectionAfterAccessibilityDialogClose();
                mIvKeyboard.performClick();
                dialog.dismiss();
            }
        });

        if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE || mLevelOneItemPos == CATEGORY_ICON_HELP) {
            if (mLevelOneItemPos == CATEGORY_ICON_HELP && position == 0) {
                enterCategory.setText(mDisplayText[position]);
            } else {
                enterCategory.setText(mSpeak);
            }
            if (mLevelOneItemPos == CATEGORY_ICON_HELP && position == 1) {
                ivLike.setImageResource(R.drawable.mynameis);
                ivDisLike.setImageResource(R.drawable.caregiver);
                ivYes.setImageResource(R.drawable.email);
                ivNo.setImageResource(R.drawable.address);
                ivAdd.setImageResource(R.drawable.contact);
                ivMinus.setImageResource(R.drawable.bloodgroup);
                ivLike.setContentDescription(getString(R.string.child_s_name_dialog_btn));
                ivDisLike.setContentDescription(getString(R.string.caregiverName_dialog_btn));
                ivYes.setContentDescription(getString(R.string.caregiver_s_email_address_dialog_btn));
                ivNo.setContentDescription(getString(R.string.homeAddress_dialog_btn));
                ivAdd.setContentDescription(getString(R.string.caregiver_s_contact_number_dialog_btn));
                ivMinus.setContentDescription(getString(R.string.bloodGroup_dialog_btn));
            } else if (mLevelOneItemPos == CATEGORY_ICON_HELP) {
                LevelUiUtils.setExpressiveIconConditionally(expressiveBtns, level2IconObjects[position]);
            }
            enterCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    speakAndShowTextBar_(mSpeechText[position]);
                    mUec.createSendFbEventFromTappedView(12, mDisplayText[position].replace("…", ""), "");
                }
            });
        } else {
            enterCategory.setText(mEnterCat);
            enterCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("Icon", "Opened " + mDisplayText[position].replace("…", ""));
                    bundleEvent("Grid", bundle);

                    Bundle args = new Bundle();
                    args.putInt(getString(R.string.level_one_intent_pos_tag), mLevelOneItemPos);
                    args.putInt(getString(R.string.level_2_item_pos_tag), position);
                    args.putString(getString(R.string.intent_menu_path_tag), mActionBarTitle + "/");

                    int CATEGORY_ICON_DAILY_ACT = 1;
                    if (mLevelOneItemPos == CATEGORY_ICON_DAILY_ACT && LevelUiUtils.isSequencePosition(position)) {
                        NavHostFragment.findNavController(LevelTwoFragment.this).navigate(R.id.action_levelTwoFragment_to_sequenceFragment, args);
                    } else {
                        NavHostFragment.findNavController(LevelTwoFragment.this).navigate(R.id.action_levelTwoFragment_to_levelThreeFragment, args);
                    }
                    dialog.dismiss();
                }
            });
        }

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

        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                clearSelectionAfterAccessibilityDialogClose();
                if (mLevelOneItemPos == CATEGORY_ICON_PEOPLE ||
                        mLevelOneItemPos == CATEGORY_ICON_HELP) {
                    singleEvent("Navigation", "Back");
                }
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                disabledView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            }
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
            dialog.show();
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes(lp);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && getLevelActivity().isNotchDevice()) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }

    private String getBloodGroup() {
        switch (getSession().getBlood()) {
            case 1:
                return getString(R.string.aPos);
            case 2:
                return getString(R.string.aNeg);
            case 3:
                return getString(R.string.bPos);
            case 4:
                return getString(R.string.bNeg);
            case 5:
                return getString(R.string.abPos);
            case 6:
                return getString(R.string.abNeg);
            case 7:
                return getString(R.string.oPos);
            case 8:
                return getString(R.string.oNeg);
            default:
                return "";
        }
    }

    private String getContactWithSpacesBetweenDigits() {
        String contact = getSession().getCaregiverNumber();
        contact =
                (SessionManager.ES_ES + SessionManager.FR_FR).contains(getSession().getLanguage()) ?
                        contact.replaceAll("[0-9]{2}", "$0, ").
                                replace("+", "plus ") :
                        contact.replaceAll(".", "$0, ").
                                replace("+", "plus");
        return contact;
    }

    private void setExpressiveButtonToAboutMe(int image_flag) {
        mIvLike.setImageResource(R.drawable.mynameis);
        mIvDontLike.setImageResource(R.drawable.caregiver);
        mIvYes.setImageResource(R.drawable.email);
        mIvNo.setImageResource(R.drawable.address);
        mIvMore.setImageResource(R.drawable.contact);
        mIvLess.setImageResource(R.drawable.bloodgroup);
        switch (image_flag) {
            case GlobalConstants.LIKE:
                mIvLike.setImageResource(R.drawable.mynameis_pressed);
                break;
            case GlobalConstants.YES:
                mIvYes.setImageResource(R.drawable.email_pressed);
                break;
            case GlobalConstants.MORE:
                mIvMore.setImageResource(R.drawable.contact_pressed);
                break;
            case GlobalConstants.DONT_LIKE:
                mIvDontLike.setImageResource(R.drawable.caregiver_pressed);
                break;
            case GlobalConstants.NO:
                mIvNo.setImageResource(R.drawable.address_pressed);
                break;
            case GlobalConstants.LESS:
                mIvLess.setImageResource(R.drawable.blooedgroup_pressed);
                break;
            default:
                break;
        }
    }

    private void useSortToLoadArray(String[] arrSpeechTxt, String[] arrAdapterTxt, String[] level2Icons) {
        String savedString = PreferencesHelper.getPrefString(getAppDatabase(), getIconCode(
                LanguageFactory.getCurrentLanguageCode(requireContext()), mLevelOneItemPos, -1));

        if (!savedString.isEmpty() && savedString.split(",").length != arrAdapterTxt.length) {
            while (savedString.split(",").length != arrAdapterTxt.length)
                savedString = savedString.concat("0,");
        }

        Integer[] mArrIconTapCount = new Integer[arrAdapterTxt.length];
        mArrSort = new Integer[arrAdapterTxt.length];

        for (int i = 0; i < arrAdapterTxt.length; ++i) {
            mArrIconTapCount[i] = 0;
            mArrSort[i] = i;
        }

        if (!savedString.equals("")) {
            StringTokenizer st = new StringTokenizer(savedString, ",");
            for (int i = 0; i < arrAdapterTxt.length; ++i)
                mArrIconTapCount[i] = Integer.parseInt(st.nextToken());
        }

        mArrPeopleTapCount = new Integer[arrAdapterTxt.length];
        for (int i = 0; i < arrAdapterTxt.length; ++i)
            mArrPeopleTapCount[i] = mArrIconTapCount[i];

        IndexSorter<Integer> is = new IndexSorter<Integer>(mArrIconTapCount);
        is.sort();

        mArrSort = new Integer[level2IconObjects.length];
        int j = -1;
        for (Integer i : is.getIndexes()) {
            mArrSort[++j] = i;
        }

        mSpeechText = new String[mArrIconTapCount.length];
        mDisplayText = new String[mArrIconTapCount.length];
        mIconCode = new String[mArrIconTapCount.length];
        int idx;
        for (int i = 0; i < mArrIconTapCount.length; ++i) {
            idx = mArrSort[i];
            mSpeechText[i] = arrSpeechTxt[idx];
            mDisplayText[i] = arrAdapterTxt[idx];
            mIconCode[i] = level2Icons[i];
        }
    }

    private int getSortedIndex(int index) {
        if (mArrSort == null) return -1;
        for (int i = 0; i < mArrSort.length; i++) {
            if (index == mArrSort[i])
                return i;
        }
        return -1;
    }

    private String getLevel2IconCode(int level1Position) {
        if (level1Position + 1 <= 9) {
            return "0" + (level1Position + 1);
        } else {
            return Integer.toString(level1Position + 1);
        }
    }

    public void hideCustomKeyboardDialog() {
        mIvKeyboard.setImageResource(R.drawable.keyboard);
        mIvBack.setImageResource(R.drawable.back);
    }

    @Override
    public void onEditIconClicked(int position) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_HOME_CUSTOM_ICON, true);
        bundle.putInt(getString(R.string.level_one_intent_pos_tag), mLevelOneItemPos);
        bundle.putString(BASIC_ICON_ID, level2IconObjects[position].getEvent_Tag());
        JellowIcon icon = new JellowIcon(
                level2IconObjects[position].getDisplay_Label(),
                level2IconObjects[position].getEvent_Tag(),
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
                level2IconObjects[position].getDisplay_Label()));
        dialog.setOnPositiveClickListener(new DialogCustom.OnPositiveClickListener() {
            @Override
            public void onPositiveClickListener() {
                CustomBasicIconHelper.deleteCustomBasicIcon(
                        requireActivity(),
                        getAppDatabase(),
                        level2IconObjects[position].getEvent_Tag(),
                        position
                );
                ArrayList<Icon> temp = new ArrayList<>(Arrays.asList(level2IconObjects));
                temp.remove(position);
                level2IconObjects = new Icon[temp.size()];
                temp.toArray(level2IconObjects);
                mDisplayText = TextFactory.getDisplayText(level2IconObjects);
                mSpeechText = TextFactory.getSpeechText(level2IconObjects);
                initializeRecyclerViewAdapter();
                if (mRecyclerView.getAdapter() != null) {
                    mRecyclerView.getAdapter().notifyItemRemoved(position);
                }
                mRecyclerItemsViewList.remove(position);
                mRecyclerView.smoothScrollToPosition(position);

                View rootView = getView();
                if (rootView != null) {
                    if ((getSession().getBasicCustomIconAddState() && level2IconObjects.length == 1) ||
                            (!getSession().getBasicCustomIconAddState() && level2IconObjects.length == 0)) {
                        ((TextView) rootView.findViewById(R.id.place_holder_text)).setText(R.string.home_custom_icon_place_holder_text);
                        rootView.findViewById(R.id.place_holder_text).setVisibility(View.VISIBLE);
                    }
                }
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
