package com.dsource.idc.jellowintl.make_my_board_module.fragments;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.ACCESSIBILITY_SERVICE;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.BOARD_ID;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.ENABLE_DROPDOWN_SPEAKER;
import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.startMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.TalkBack.TalkbackHints_SingleClick;
import com.dsource.idc.jellowintl.fragments.BaseFragment;
import com.dsource.idc.jellowintl.make_my_board_module.adapters.HomeActivityAdapter;
import com.dsource.idc.jellowintl.make_my_board_module.datamodels.DragNDropDataProvider;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.data_models.BoardModel;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.databases.BoardDatabase;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.databases.TextDatabase;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.GridSelectListener;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.OnItemClickListener;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.OnItemMoveListener;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.OnSelectionClearListener;
import com.dsource.idc.jellowintl.make_my_board_module.managers.ExpressiveIconManager;
import com.dsource.idc.jellowintl.make_my_board_module.managers.ModelManager;
import com.dsource.idc.jellowintl.make_my_board_module.managers.SearchScrollManager;
import com.dsource.idc.jellowintl.make_my_board_module.utility.Nomenclature;
import com.dsource.idc.jellowintl.models.ExpressiveIcon;
import com.dsource.idc.jellowintl.models.GlobalConstants;
import com.dsource.idc.jellowintl.models.Icon;
import com.dsource.idc.jellowintl.models.JellowIcon;
import com.dsource.idc.jellowintl.utility.DialogKeyboardUtterance;
import com.dsource.idc.jellowintl.utility.LevelUiUtils;
import com.dsource.idc.jellowintl.utility.interfaces.TextToSpeechCallBacks;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import java.util.ArrayList;

public class BoardHomeFragment extends BaseFragment implements TextToSpeechCallBacks {

    private static final int SEARCH = 1221;
    private RecyclerView rvRecycler;
    private ImageView ivHome, ivBack;
    private ModelManager modelManager;
    private ArrayList<JellowIcon> displayList;
    private int Level = 0;
    private String boardId;
    private BoardDatabase database;
    private BoardModel currentBoard;
    private HomeActivityAdapter adapter;
    private int LevelOneParent = -1;
    private Icon selectedIconVerbiage;
    private TextDatabase verbiageDatabase;
    private ExpressiveIconManager expIconManager;
    private ArrayList<ExpressiveIcon> expIconVerbiage;
    private ImageView ivKeyboard;
    private SearchScrollManager searchScrollManager;
    private GridLayoutManager mLayoutManager;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerView.Adapter mWrappedAdapter;
    private int mode = HomeActivityAdapter.NORMAL_MODE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_levelx_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupParent();
        applyMonochromeColor(view);

        database = new BoardDatabase(getAppDatabase());
        if (getArguments() != null) {
            boardId = getArguments().getString(BOARD_ID);
        }
        if (boardId == null || boardId.isEmpty()) {
            if (requireActivity().getIntent() != null && requireActivity().getIntent().getExtras() != null) {
                boardId = requireActivity().getIntent().getExtras().getString(BOARD_ID);
            }
        }

        registerSpeechEngineErrorHandle(this);
        currentBoard = database.getBoardById(boardId);
        if (currentBoard != null) {
            setupActionBarTitle(view, View.VISIBLE, getString(R.string.home) + "/" +
                    getString(R.string.my_boards) + "/" +
                    currentBoard.getBoardName() + " " + getString(R.string.board));
            verbiageDatabase = new TextDatabase(requireContext(), currentBoard.getLanguage(), getAppDatabase());
            modelManager = new ModelManager(currentBoard.getIconModel());
            displayList = modelManager.getLevelOneFromModel();
        } else {
            displayList = new ArrayList<>();
        }

        prepareRecyclerView(view);
        initViews(view);
        expIconManager = new ExpressiveIconManager(requireContext(), view.findViewById(R.id.parent));
        expIconManager.setClickListener(new ExpressiveIconManager.expIconClickListener() {
            @Override
            public void expressiveIconClicked(int expIconPos, int time) {
                if (adapter != null) {
                    adapter.setExpIconPos(expIconPos);
                    adapter.notifyDataSetChanged();
                }
                speakVerbiage(expIconPos, time);
            }
        });
        if (currentBoard != null) {
            loadExpressiveIconVerbiage();
        }
        ActivateView(ivBack, false);

        searchScrollManager = new SearchScrollManager(requireContext(), rvRecycler);
        manageKeyboard();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBoardBack();
            }
        });
    }

    private void prepareRecyclerView(View view) {
        rvRecycler = view.findViewById(R.id.recycler_view);
        adapter = new HomeActivityAdapter(new DragNDropDataProvider(displayList), requireContext(), getLevelXAdapterLayout());
        mLayoutManager = new GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false);
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setInitiateOnLongPress(mode == HomeActivityAdapter.REPOSITION_MODE);
        mRecyclerViewDragDropManager.setInitiateOnMove(false);
        mRecyclerViewDragDropManager.setLongPressTimeout(750);
        mRecyclerViewDragDropManager.setDragStartItemAnimationDuration(250);
        mRecyclerViewDragDropManager.setDraggingItemAlpha(0.8f);
        mRecyclerViewDragDropManager.setDraggingItemScale(1.3f);
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(adapter);
        GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        rvRecycler.setLayoutManager(mLayoutManager);
        rvRecycler.setAdapter(mWrappedAdapter);
        rvRecycler.setItemAnimator(animator);
        mRecyclerViewDragDropManager.attachRecyclerView(rvRecycler);

        if (currentBoard != null) {
            switch (currentBoard.getGridSize()) {
                case 0:
                    rvRecycler.setLayoutManager(new GridLayoutManager(requireContext(), 1));
                    break;
                case 1:
                case 3:
                    rvRecycler.setLayoutManager(new GridLayoutManager(requireContext(), 2));
                    break;
                default:
                    rvRecycler.setLayoutManager(new GridLayoutManager(requireContext(), 3));
                    break;
            }
        }

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (adapter == null || position >= displayList.size()) return;
                adapter.setExpIconPos(-1);
                ivHome.setImageDrawable(getResources().getDrawable(R.drawable.home));
                adapter.setSelectedPosition(position);
                prepareSpeech(displayList.get(position));
                AccessibilityManager am = (AccessibilityManager) requireContext().getSystemService(ACCESSIBILITY_SERVICE);
                if (isAccessibilityTalkBackOn(am)) {
                    showAccessibleDialog();
                }
            }
        });
        adapter.setOnItemMoveListener(new OnItemMoveListener() {
            @Override
            public void onItemMove(int to, int from) {
                moveIcon(to, from);
            }
        });

        adapter.setSelectionClearListener(new OnSelectionClearListener() {
            @Override
            public void onSelectionCleared() {
                selectedIconVerbiage = null;
                expIconManager.resetSelection();
                if (mode == HomeActivityAdapter.REPOSITION_MODE) {
                    expIconManager.disableExpressiveIcons(true);
                }
            }
        });
    }

    private void moveIcon(int to, int from) {
        if (currentBoard != null && currentBoard.getIconModel() != null) {
            currentBoard.getIconModel().move(to, from);
            database.updateBoardIntoDatabase(currentBoard);
        }
    }

    private void loadExpressiveIconVerbiage() {
        expIconVerbiage = new ArrayList<>();
        if (verbiageDatabase == null || currentBoard == null) return;
        for (int i = 0; i < 6; i++) {
            expIconVerbiage.add(verbiageDatabase.getExpressiveIconsById(Nomenclature.getNameForExpressiveIcons(i, currentBoard.getLanguage())));
        }
    }

    private void speakVerbiage(int expIconPos, int time) {
        String verbiage = "";
        if (selectedIconVerbiage != null) {
            switch (expIconPos) {
                case 0:
                    verbiage = (time == 0) ? selectedIconVerbiage.getL() : selectedIconVerbiage.getLL();
                    break;
                case 1:
                    verbiage = (time == 0) ? selectedIconVerbiage.getY() : selectedIconVerbiage.getYY();
                    break;
                case 2:
                    verbiage = (time == 0) ? selectedIconVerbiage.getM() : selectedIconVerbiage.getMM();
                    break;
                case 3:
                    verbiage = (time == 0) ? selectedIconVerbiage.getD() : selectedIconVerbiage.getDD();
                    break;
                case 4:
                    verbiage = (time == 0) ? selectedIconVerbiage.getN() : selectedIconVerbiage.getNN();
                    break;
                case 5:
                    verbiage = (time == 0) ? selectedIconVerbiage.getS() : selectedIconVerbiage.getSS();
                    break;
            }
        } else if (expIconVerbiage != null && expIconPos < expIconVerbiage.size()) {
            verbiage = (time == 0) ? expIconVerbiage.get(expIconPos).getL() : expIconVerbiage.get(expIconPos).getLL();
        }

        if (verbiage != null && !verbiage.equals("NA") && !verbiage.isEmpty()) {
            speakFromMMB(verbiage);
        }
    }

    private void manageKeyboard() {
        ivKeyboard.setOnClickListener(v -> {
            new DialogKeyboardUtterance().show(requireActivity());
            speakFromMMB(getResources().getString(R.string.keyboard));
            ivHome.setImageDrawable(getResources().getDrawable(R.drawable.home));
            ivKeyboard.setImageDrawable(getResources().getDrawable(R.drawable.keyboard_pressed));
        });
    }

    private void prepareSpeech(JellowIcon jellowIcon) {
        if (verbiageDatabase != null && jellowIcon != null) {
            selectedIconVerbiage = verbiageDatabase.getVerbiageById(jellowIcon.getVerbiageId());
            expIconManager.setAccordingVerbiage(selectedIconVerbiage);
            if (selectedIconVerbiage != null) {
                speakFromMMB(selectedIconVerbiage.getSpeech_Label());
            }
        }
    }

    private void initViews(View view) {
        ivKeyboard = view.findViewById(R.id.keyboard);
        ivKeyboard.setVisibility(View.VISIBLE);
        ivHome = view.findViewById(R.id.ivhome);
        ivHome.setOnClickListener(v -> {
            ivKeyboard.setImageDrawable(getResources().getDrawable(R.drawable.keyboard));
            selectedIconVerbiage = null;
            if (currentBoard != null) {
                speakFromMMB(currentBoard.getBoardName() + " " + getString(R.string.board) + " " + getString(R.string.home));
            }
            ActivateView(ivHome, true);
            ActivateView(ivBack, false);
            expIconManager.resetSelection();
            LevelOneParent = -1;
            if (adapter != null) {
                adapter.setSelectedPosition(-1);
                adapter.setExpIconPos(-1);
            }
            Level = 0;
            if (rvRecycler != null && rvRecycler.getLayoutManager() != null) {
                rvRecycler.getLayoutManager().smoothScrollToPosition(rvRecycler, null, 0);
            }
            ivHome.setImageDrawable(getResources().getDrawable(R.drawable.home_pressed));
        });
        ivBack = view.findViewById(R.id.ivback);
        ivBack.setOnClickListener(v -> {
            speakFromMMB(getResources().getString(R.string.back));
            selectedIconVerbiage = null;
            handleBoardBack();
        });
        if (rvRecycler != null && rvRecycler.getItemAnimator() != null) {
            ((SimpleItemAnimator) rvRecycler.getItemAnimator()).setSupportsChangeAnimations(false);
        }
        int[] icons = {
                R.id.ivlike, R.id.ivyes, R.id.ivadd,
                R.id.ivdislike, R.id.ivno, R.id.ivminus,
                R.id.ivhome, R.id.ivback, R.id.keyboard
        };
        for (int icon : icons) {
            View target = view.findViewById(icon);
            if (target != null) {
                ViewCompat.setAccessibilityDelegate(target, new TalkbackHints_SingleClick());
            }
        }
    }

    private int getLevelXAdapterLayout() {
        if (currentBoard == null) return R.layout.layout_level_xadapter_9_icons;
        switch (currentBoard.getGridSize()) {
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

    private void handleBoardBack() {
        expIconManager.resetSelection();
        if (adapter != null) {
            adapter.setSelectedPosition(-1);
            adapter.setExpIconPos(-1);
        }
        selectedIconVerbiage = null;
        if (Level == 2) {
            if (LevelOneParent != -1 && modelManager != null) {
                displayList = modelManager.getLevelTwoFromModel(LevelOneParent);
                if (adapter != null) {
                    adapter.setSelectedPosition(LevelOneParent);
                }
                if (getView() != null) prepareRecyclerView(getView());
                Level--;
            }
        } else if (Level == 1) {
            if (modelManager != null) {
                displayList = modelManager.getLevelOneFromModel();
            }
            LevelOneParent = -1;
            if (getView() != null) prepareRecyclerView(getView());
            Level--;
            ActivateView(ivBack, false);
        } else if (Level == 0) {
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

    private void ActivateView(ImageView view, boolean activate) {
        if (view == null) return;
        if (activate) {
            view.setAlpha(GlobalConstants.ENABLE_ALPHA);
            view.setClickable(true);
        } else {
            view.setAlpha(GlobalConstants.DISABLE_ALPHA);
            view.setClickable(false);
        }
    }

    private void showGridDialog() {
        showGridDialog(new GridSelectListener() {
            @Override
            public void onGridSelectListener(int size) {
                if (currentBoard != null) {
                    currentBoard.setGridSize(size);
                    database.updateBoardIntoDatabase(currentBoard);
                    if (getView() != null) prepareRecyclerView(getView());
                }
            }
        }, currentBoard != null ? currentBoard.getGridSize() : 4);
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
        } else if (itemId == R.id.reposition_lock) {
            mode = (mode == HomeActivityAdapter.NORMAL_MODE) ?
                    HomeActivityAdapter.REPOSITION_MODE : HomeActivityAdapter.NORMAL_MODE;
            if (mode == HomeActivityAdapter.REPOSITION_MODE) {
                Toast.makeText(requireContext(), getString(R.string.reposition_text), Toast.LENGTH_SHORT).show();
                mRecyclerViewDragDropManager.setInitiateOnLongPress(true);
                disableLayout(true);

                if (adapter != null) {
                    adapter.setSelectedPosition(-1);
                }
                selectedIconVerbiage = null;
                expIconManager.resetSelection();
                expIconManager.disableExpressiveIcons(true);
                stopSpeaking();

                if (getMenu() != null && getMenu().findItem(R.id.reposition_lock) != null) {
                    getMenu().findItem(R.id.reposition_lock).setIcon(R.drawable.ic_unlocked);
                    getMenu().findItem(R.id.reposition_lock).setTitle(getString(R.string.disable_reposition_icons));
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.reposition_complete_msg), Toast.LENGTH_LONG).show();
                mRecyclerViewDragDropManager.setInitiateOnLongPress(false);
                disableLayout(false);

                if (getMenu() != null && getMenu().findItem(R.id.reposition_lock) != null) {
                    getMenu().findItem(R.id.reposition_lock).setIcon(R.drawable.ic_locked);
                    getMenu().findItem(R.id.reposition_lock).setTitle(getString(R.string.enable_reposition_icons));
                }
                expIconManager.disableExpressiveIcons(false);
            }
            return true;
        } else if (itemId == R.id.action_home_app) {
            exitToBoardListScreen();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exitToBoardListScreen() {
        if (getSession() != null) {
            getSession().setCurrentBoardLanguage("");
            getSession().setBoardVoice("");
        }
        stopSpeaking();
        boolean popped = NavHostFragment.findNavController(this).popBackStack(R.id.boardListFragment, false);
        if (!popped) {
            NavHostFragment.findNavController(this).navigate(R.id.boardListFragment);
        }
    }

    private void disableLayout(boolean disable) {
        ActivateView(ivKeyboard, !disable);
        ActivateView(ivHome, !disable);
        if (expIconManager != null) {
            expIconManager.disableExpressiveIcons(disable);
        }
    }

    private void showAccessibleDialog() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(requireContext());
        final View mView = getLayoutInflater().inflate(R.layout.dialog_layout, null);

        Button enterCategory = mView.findViewById(R.id.enterCategory);
        final Button closeDialog = mView.findViewById(R.id.btnClose);
        ImageView ivLike = mView.findViewById(R.id.ivlike);
        ImageView ivYes = mView.findViewById(R.id.ivyes);
        ImageView ivAdd = mView.findViewById(R.id.ivadd);
        ImageView ivDisLike = mView.findViewById(R.id.ivdislike);
        ImageView ivNo = mView.findViewById(R.id.ivno);
        ImageView ivMinus = mView.findViewById(R.id.ivminus);
        final ImageView ivBack_ = mView.findViewById(R.id.back);
        ivBack_.setEnabled(false);
        ivBack_.setAlpha(GlobalConstants.DISABLE_ALPHA);
        final ImageView ivHome_ = mView.findViewById(R.id.home);
        final ImageView ivKeyboard_ = mView.findViewById(R.id.keyboard);
        ViewCompat.setAccessibilityDelegate(ivLike, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(ivYes, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(ivAdd, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(ivDisLike, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(ivNo, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(ivMinus, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(ivBack_, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(ivHome_, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(ivKeyboard_, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(enterCategory, new TalkbackHints_SingleClick());
        ViewCompat.setAccessibilityDelegate(closeDialog, new TalkbackHints_SingleClick());
        mBuilder.setView(mView);
        applyMonochromeColor(mView);

        final AlertDialog dialog = mBuilder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        enterCategory.setText(getString(R.string.speak));
        enterCategory.setOnClickListener(v -> {
            if (selectedIconVerbiage != null) {
                speak(selectedIconVerbiage.getSpeech_Label());
            }
        });

        enterCategory.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
                super.onPopulateAccessibilityEvent(host, event);
                if (event.getEventType() != AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
                    mView.findViewById(R.id.txTitleHidden).setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }
            }
        });
        closeDialog.setOnClickListener(v -> {
            clearSelectionAfterAccessibilityDialogClose();
            dialog.dismiss();
        });

        final ImageView[] expressiveBtns = {ivLike, ivYes, ivAdd, ivDisLike, ivNo, ivMinus};
        ivLike.setOnClickListener(v -> {
            if (getView() != null) getView().findViewById(R.id.ivlike).performClick();
            LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.LIKE);
        });
        ivYes.setOnClickListener(v -> {
            if (getView() != null) getView().findViewById(R.id.ivyes).performClick();
            LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.YES);
        });
        ivAdd.setOnClickListener(v -> {
            if (getView() != null) getView().findViewById(R.id.ivadd).performClick();
            LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.MORE);
        });
        ivDisLike.setOnClickListener(v -> {
            if (getView() != null) getView().findViewById(R.id.ivdislike).performClick();
            LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.DONT_LIKE);
        });
        ivNo.setOnClickListener(v -> {
            if (getView() != null) getView().findViewById(R.id.ivno).performClick();
            LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.NO);
        });
        ivMinus.setOnClickListener(v -> {
            if (getView() != null) getView().findViewById(R.id.ivminus).performClick();
            LevelUiUtils.setExpressiveIconPressedState(expressiveBtns, GlobalConstants.LESS);
        });
        ivBack_.setOnClickListener(v -> dialog.dismiss());
        ivHome_.setOnClickListener(v -> {
            ivHome.performClick();
            dialog.dismiss();
        });
        ivKeyboard_.setOnClickListener(v -> {
            ivKeyboard.performClick();
            dialog.dismiss();
        });
        LevelUiUtils.setExpressiveIconConditionally(expressiveBtns, selectedIconVerbiage);

        dialog.setOnCancelListener(dialog1 -> clearSelectionAfterAccessibilityDialogClose());

        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
            dialog.show();
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes(lp);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isNotchDevice()) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }

    private void clearSelectionAfterAccessibilityDialogClose() {
        ivHome.setImageDrawable(getResources().getDrawable(R.drawable.home));
        if (expIconManager != null) {
            expIconManager.resetSelection();
        }
        LevelOneParent = -1;
        if (adapter != null) {
            adapter.setSelectedPosition(-1);
            adapter.setExpIconPos(-1);
        }
        Level = 0;
    }

    private void searchInBoard() {
        Bundle args = new Bundle();
        args.putString(BoardSearchActivity.SEARCH_MODE, BoardSearchActivity.SEARCH_IN_BOARD);
        if (currentBoard != null) {
            args.putString(BOARD_ID, currentBoard.getBoardId());
        }
        args.putBoolean(ENABLE_DROPDOWN_SPEAKER, true);
        BoardSearchActivity searchDialog = BoardSearchActivity.newInstance(args, (icon, resultString) -> {
            if (icon != null && modelManager != null) {
                ArrayList<Integer> iconPos = modelManager.getIconPositionInModel(icon);
                if (iconPos.size() > 0) {
                    ActivateView(ivBack, Level > 0);
                    ivHome.setImageDrawable(getResources().getDrawable(R.drawable.home_pressed));
                    if (getNumberOfIconPerScreen() <= iconPos.get(0) || iconPos.get(0) < getLastVisibleItem()) {
                        searchScrollManager.scrollToPosition(iconPos.get(0));
                    }
                    if (adapter != null) {
                        adapter.tapSearchedItem(iconPos.get(0));
                    }
                    return;
                }
            }
            selectedIconVerbiage = null;
        });
        searchDialog.show(getParentFragmentManager(), BoardSearchActivity.class.getSimpleName());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEARCH && resultCode == RESULT_OK && data != null) {
            JellowIcon icon = (JellowIcon) data.getSerializableExtra(getString(R.string.search_result));
            if (icon != null && modelManager != null) {
                ArrayList<Integer> iconPos = modelManager.getIconPositionInModel(icon);
                if (iconPos.size() > 0) {
                    ActivateView(ivBack, Level > 0);
                    ivHome.setImageDrawable(getResources().getDrawable(R.drawable.home_pressed));
                    if (getNumberOfIconPerScreen() <= iconPos.get(0) || iconPos.get(0) < getLastVisibleItem()) {
                        searchScrollManager.scrollToPosition(iconPos.get(0));
                    }
                    if (adapter != null) {
                        adapter.tapSearchedItem(iconPos.get(0));
                    }
                    return;
                }
            }
            selectedIconVerbiage = null;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Integer getLastVisibleItem() {
        if (rvRecycler != null && rvRecycler.getLayoutManager() != null) {
            return ((GridLayoutManager) rvRecycler.getLayoutManager()).findFirstVisibleItemPosition();
        }
        return -1;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLevelActivity().setVisibleAct(BoardHomeFragment.class.getSimpleName());
        getLevelActivity().setupToolbarMenu(getView());

        if (currentBoard != null) {
            initiateSpeechEngineWithLanguage(getSession().getBoardVoice().split(",")[0], currentBoard.getLanguage());
        }
        if (!getSession().getToastMessage().isEmpty()) {
            Toast.makeText(requireContext(), getSession().getToastMessage(), Toast.LENGTH_SHORT).show();
            getSession().setToastMessage("");
        }
        if (!isAnalyticsActive()) {
            resetAnalytics(requireContext(), getSession().getUserId());
        }
        startMeasuring();
    }

    @Override
    public void onPause() {
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.cancelDrag();
        }
        stopSpeaking();
        super.onPause();
        long sessionTime = validatePushId(getSession().getSessionCreatedAt());
        getSession().setSessionCreatedAt(sessionTime);
        stopMeasuring(BoardHomeFragment.class.getSimpleName());
    }

    @Override
    public void onDestroyView() {
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (rvRecycler != null) {
            rvRecycler.setItemAnimator(null);
            rvRecycler.setAdapter(null);
            rvRecycler = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }

        mLayoutManager = null;
        super.onDestroyView();
    }

    private int getNumberOfIconPerScreen() {
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
        }
        return 9;
    }

    @Override
    public void sendSpeechEngineLanguageNotSetCorrectlyError() {}

    @Override
    public void speechEngineNotFoundError() {}

    @Override
    public void speechSynthesisCompleted() {}

    public void hideCustomKeyboardDialog() {
        if (ivKeyboard != null) {
            ivKeyboard.setImageDrawable(getResources().getDrawable(R.drawable.keyboard));
        }
    }
}
