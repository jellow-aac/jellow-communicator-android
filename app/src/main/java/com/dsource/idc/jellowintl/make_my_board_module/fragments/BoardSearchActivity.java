package com.dsource.idc.jellowintl.make_my_board_module.fragments;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.BOARD_ID;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.ENABLE_DROPDOWN_SPEAKER;
import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.startMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.activities.BaseActivity;
import com.dsource.idc.jellowintl.activities.SpeechEngineBaseActivity;
import com.dsource.idc.jellowintl.make_my_board_module.adapters.BoardSearchAdapter;
import com.dsource.idc.jellowintl.make_my_board_module.datamodels.BoardIconModel;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.data_models.BoardModel;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.databases.BoardDatabase;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.databases.IconDatabaseFacade;
import com.dsource.idc.jellowintl.make_my_board_module.managers.ModelManager;
import com.dsource.idc.jellowintl.make_my_board_module.models.BoardListModel;
import com.dsource.idc.jellowintl.models.JellowIcon;

import java.util.ArrayList;

public class BoardSearchActivity extends DialogFragment {

    public interface SearchResultCallback {
        void onSearchResult(JellowIcon icon, String resultString);
    }

    public static final String BASE_ICON_SEARCH = "base_icon_search";
    public static final String SEARCH_MODE = "search_mode";
    public static final String NORMAL_SEARCH = "normal_search";
    public static final String ICON_SEARCH = "icon_search";
    public static final String SEARCH_IN_BOARD = "board_search";
    public static final String SEARCH_FOR_BOARD = "search_the_board";

    private RecyclerView mRecyclerView;
    private BoardSearchAdapter adapter;
    private ArrayList<JellowIcon> iconList;
    private BoardModel currentBoard;
    private String mode;
    private EditText searchBox;
    private SearchResultCallback callback;
    private Context mContext;

    public static BoardSearchActivity newInstance(Bundle args, SearchResultCallback callback) {
        BoardSearchActivity fragment = new BoardSearchActivity();
        fragment.setArguments(args);
        fragment.setCallback(callback);
        return fragment;
    }

    public void setCallback(SearchResultCallback callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.activity_search, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = requireContext();

        Bundle args = getArguments();
        String boardId = args != null ? args.getString(BOARD_ID) : null;
        if (boardId != null && getActivity() instanceof BaseActivity) {
            currentBoard = new BoardDatabase(((BaseActivity) getActivity()).getAppDatabase()).getBoardById(boardId);
        }

        initFields(view);

        EditText searchEditText = view.findViewById(R.id.search_auto_complete);
        mode = args != null ? args.getString(SEARCH_MODE) : null;

        AccessibilityManager am = (AccessibilityManager) mContext.getSystemService(ACCESSIBILITY_SERVICE);
        boolean talkBackOn = am != null && am.isEnabled() && am.isTouchExplorationEnabled();
        if (!talkBackOn) {
            view.findViewById(R.id.close_button).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.close_button).setOnClickListener(v -> dismiss());
        }

        searchEditText.setHint(getString(R.string.enter_icon_name));

        if (mode != null) {
            switch (mode) {
                case NORMAL_SEARCH:
                    normalSearch(view);
                    break;
                case SEARCH_IN_BOARD:
                    searchInBoard(view, currentBoard);
                    break;
                case ICON_SEARCH:
                    searchForIcon(view);
                    break;
                case BASE_ICON_SEARCH:
                    searchInBaseDatabase(view);
                    break;
                case SEARCH_FOR_BOARD:
                    searchEditText.setHint(getString(R.string.enter_board_name_to_search));
                    searchForBoard(view);
                    break;
            }
        }

        view.findViewById(R.id.parent).setOnClickListener(v -> dismiss());
    }

    private void searchInBaseDatabase(View view) {
        if (!(getActivity() instanceof BaseActivity)) return;
        BaseActivity baseAct = (BaseActivity) getActivity();
        final IconDatabaseFacade database = new IconDatabaseFacade(baseAct.getSession().getLanguage(), baseAct.getAppDatabase());
        EditText searchEditText = view.findViewById(R.id.search_auto_complete);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                iconList.clear();
                ArrayList<JellowIcon> icon = database.query(query + "%");
                if (icon != null && icon.size() > 0) {
                    iconList.addAll(icon);
                }
                if (iconList.size() == 0) {
                    JellowIcon noIconFound = new JellowIcon(getResources().getString(R.string.icon_not_found), "NULL", -1, -1, -1);
                    iconList.add(noIconFound);
                }
                if (iconList.size() > 0 && adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        adapter.setOnItemClickListener(position -> {
            if (position >= 0 && position < iconList.size()) {
                JellowIcon icon = iconList.get(position);
                if (icon.getParent0() != -1) {
                    if (callback != null) {
                        callback.onSearchResult(icon, icon.getIconDrawable());
                    }
                    dismiss();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof BaseActivity) {
            BaseActivity baseAct = (BaseActivity) getActivity();
            if (!isAnalyticsActive()) {
                resetAnalytics(mContext, baseAct.getSession().getUserId());
            }
            startMeasuring();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof BaseActivity) {
            BaseActivity baseAct = (BaseActivity) getActivity();
            long sessionTime = validatePushId(baseAct.getSession().getSessionCreatedAt());
            baseAct.getSession().setSessionCreatedAt(sessionTime);
            stopMeasuring(BoardSearchActivity.class.getSimpleName());
        }
    }

    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() instanceof BaseActivity) {
            BaseActivity baseAct = (BaseActivity) getActivity();
            if (baseAct.getWindow() != null && baseAct.getWindow().getDecorView() != null) {
                baseAct.getWindow().getDecorView().post(() -> baseAct.setupToolbarMenu(null));
            } else {
                baseAct.setupToolbarMenu(null);
            }
        }
    }

    private void searchForBoard(View view) {
        if (!(getActivity() instanceof BaseActivity)) return;
        BaseActivity baseAct = (BaseActivity) getActivity();
        final BoardListModel blm = new BoardListModel(baseAct.getAppDatabase());
        EditText searchEditText = view.findViewById(R.id.search_auto_complete);
        adapter.setSearchingBoardName(true);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String query = s.toString().trim();
                iconList.clear();
                ArrayList<BoardModel> boardModels = blm.getAllBoardsStartWithName(query + "%");
                if (boardModels != null && boardModels.size() > 0) {
                    for (BoardModel bm : boardModels) {
                        JellowIcon icon = new JellowIcon(bm.getBoardName(), bm.getBoardId(), -1, -1, -1);
                        iconList.add(icon);
                    }
                } else {
                    JellowIcon noIconFound = new JellowIcon(getResources().getString(R.string.board_not_found), "NULL", -1, -1, -1);
                    iconList.add(noIconFound);
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        adapter.setOnItemClickListener(position -> {
            if (position >= 0 && position < iconList.size()) {
                JellowIcon icon = iconList.get(position);
                if (!icon.getIconDrawable().isEmpty() && !icon.getIconDrawable().equals("NULL")) {
                    if (callback != null) {
                        callback.onSearchResult(icon, icon.getIconTitle());
                    }
                    dismiss();
                }
            }
        });
    }

    private void searchForIcon(View view) {
        if (!(getActivity() instanceof BaseActivity)) return;
        BaseActivity baseAct = (BaseActivity) getActivity();
        String lang = currentBoard != null ? currentBoard.getLanguage() : baseAct.getSession().getLanguage();
        final IconDatabaseFacade iconDatabase = new IconDatabaseFacade(lang, baseAct.getAppDatabase());

        EditText searchEditText = view.findViewById(R.id.search_auto_complete);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                ArrayList<JellowIcon> icon = iconDatabase.query(query);
                iconList.clear();
                if (icon != null && icon.size() > 0) {
                    iconList.addAll(icon);
                }
                if (iconList.size() == 0) {
                    JellowIcon noIconFound = new JellowIcon("Icon not found", "NULL", -1, -1, -1);
                    iconList.add(noIconFound);
                }
                if (iconList.size() > 0 && adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        adapter.setOnItemClickListener(position -> {
            if (position >= 0 && position < iconList.size()) {
                JellowIcon icon = iconList.get(position);
                if (icon.getParent0() != -1) {
                    if (callback != null) {
                        callback.onSearchResult(icon, icon.getIconDrawable());
                    }
                    dismiss();
                }
            }
        });
    }

    private void searchInBoard(View view, final BoardModel currentBoard) {
        if (currentBoard != null && getActivity() instanceof BaseActivity) {
            BaseActivity baseAct = (BaseActivity) getActivity();
            BoardIconModel model = currentBoard.getIconModel();
            final ModelManager modelManager = new ModelManager(model);
            searchBox.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();
                    iconList.clear();
                    iconList = modelManager.searchIconsForText(query);
                    if (iconList.size() == 0) {
                        JellowIcon noIconFound = new JellowIcon(getString(R.string.not_found), "NULL", -1, -1, -1);
                        iconList.add(noIconFound);
                    }

                    if (iconList.size() > 0) {
                        adapter = new BoardSearchAdapter(mContext, iconList, currentBoard.getLanguage(), baseAct.getAppDatabase());
                        Bundle args = getArguments();
                        if (args != null && args.getBoolean(ENABLE_DROPDOWN_SPEAKER, false)) {
                            adapter.activateSearchDropdownSpeaker();
                        }
                        adapter.setOnItemClickListener(position -> {
                            if (position >= 0 && position < iconList.size()) {
                                if (!iconList.get(position).getIconTitle().equals(getString(R.string.not_found))) {
                                    if (callback != null) {
                                        callback.onSearchResult(iconList.get(position), iconList.get(position).getIconTitle());
                                    }
                                    dismiss();
                                }
                            }
                        });
                        mRecyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void normalSearch(View view) {
        if (!(getActivity() instanceof BaseActivity)) return;
        BaseActivity baseAct = (BaseActivity) getActivity();
        String lang = currentBoard != null ? currentBoard.getLanguage() : baseAct.getSession().getLanguage();
        final IconDatabaseFacade iconDatabase = new IconDatabaseFacade(lang, baseAct.getAppDatabase());

        EditText searchEditText = view.findViewById(R.id.search_auto_complete);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                ArrayList<JellowIcon> icon = iconDatabase.query(query);
                iconList.clear();
                if (icon != null && icon.size() > 0) {
                    iconList.addAll(icon);
                }
                if (iconList.size() == 0) {
                    JellowIcon noIconFound = new JellowIcon(getString(R.string.not_found), "NULL", -1, -1, -1);
                    iconList.add(noIconFound);
                }
                if (iconList.size() > 0 && adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        adapter.setOnItemClickListener(position -> {
            if (position >= 0 && position < iconList.size()) {
                JellowIcon icon = iconList.get(position);
                if (icon.getParent0() != -1) {
                    if (callback != null) {
                        callback.onSearchResult(icon, icon.getIconTitle());
                    }
                    dismiss();
                }
            }
        });
    }

    private void initFields(View view) {
        if (!(getActivity() instanceof BaseActivity)) return;
        BaseActivity baseAct = (BaseActivity) getActivity();
        searchBox = view.findViewById(R.id.search_auto_complete);
        iconList = new ArrayList<>();
        if (currentBoard != null) {
            adapter = new BoardSearchAdapter(mContext, iconList, currentBoard.getLanguage(), baseAct.getAppDatabase());
        } else {
            adapter = new BoardSearchAdapter(mContext, iconList, baseAct.getSession().getLanguage(), baseAct.getAppDatabase());
        }
        mRecyclerView = view.findViewById(R.id.icon_search_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void closeSearchBar(View view) {
        dismiss();
    }

    public void speakOnly(int position) {
        if (getActivity() instanceof SpeechEngineBaseActivity && position >= 0 && position < iconList.size()) {
            ((SpeechEngineBaseActivity) getActivity()).speakFromMMB(iconList.get(position).getIconSpeech());
        }
    }
}


