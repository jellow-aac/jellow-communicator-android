package com.dsource.idc.jellowintl.fragments;

import static com.dsource.idc.jellowintl.factories.PathFactory.getIconPath;
import static com.dsource.idc.jellowintl.utility.Analytics.bundleEvent;
import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.startMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.TalkBack.TalkbackHints_SingleClick;
import com.dsource.idc.jellowintl.activities.BaseActivity;
import com.dsource.idc.jellowintl.activities.SpeechEngineBaseActivity;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.databases.IconDatabaseFacade;
import com.dsource.idc.jellowintl.models.AppDatabase;
import com.dsource.idc.jellowintl.models.JellowIcon;
import com.dsource.idc.jellowintl.utility.SessionManager;

import java.util.ArrayList;

public class SearchDialogFragment extends DialogFragment {

    private SearchDialogViewIconAdapter adapter;
    private ArrayList<JellowIcon> iconList;
    private boolean iconNotFound = false;
    private String notFoundIconText = "Null";

    private int beforeTextChanged;
    private int afterTextChanged;
    private IconDatabaseFacade database;
    private boolean firedEvent = false;

    public static SearchDialogFragment newInstance() {
        return new SearchDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Translucent_NoTitleBar);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_search, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setGravity(Gravity.CENTER);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }

        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).applyMonochromeColor();
        }

        SessionManager session = getSession();
        AppDatabase appDatabase = getAppDatabase();
        if (session != null && appDatabase != null) {
            database = new IconDatabaseFacade(session.getLanguage(), appDatabase);
        }

        EditText searchEditText = view.findViewById(R.id.search_auto_complete);
        View closeBtn = view.findViewById(R.id.close_button);

        AccessibilityManager am = (AccessibilityManager) requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (getActivity() instanceof BaseActivity && !((BaseActivity) getActivity()).isAccessibilityTalkBackOn(am)) {
            closeBtn.setVisibility(View.GONE);
        } else {
            closeBtn.setVisibility(View.VISIBLE);
        }

        closeBtn.setOnClickListener(v -> dismiss());
        searchEditText.setHint(getString(R.string.enter_icon_name));

        // To Close on touch outside
        view.findViewById(R.id.parent).setOnClickListener(v -> {
            if (iconNotFound && !(notFoundIconText.equals("Null"))) {
                Bundle bundle = new Bundle();
                bundle.putString("IconSpeak", "");
                bundle.putString("IconOpened", "");
                bundle.putString("IconNotFound", notFoundIconText);
                bundleEvent("SearchBar", bundle);
            }
            dismiss();
        });

        initFields(view);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                beforeTextChanged = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                iconNotFound = false;
                afterTextChanged = s.length();
                String query = s.toString().trim();
                ArrayList<JellowIcon> icon = (database != null) ? database.query(query.concat("%")) : new ArrayList<>();
                
                iconList.clear();
                if (icon != null && icon.size() > 0) {
                    for (int i = 0; i < icon.size(); i++) {
                        iconList.add(icon.get(i));
                    }
                }
                
                if (iconList.isEmpty()) {
                    iconNotFound = true;
                    notFoundIconText = s.toString();
                    JellowIcon noIconFound = new JellowIcon(getResources().getString(R.string.icon_not_found), "NULL", -1, -1, -1);
                    iconList.add(noIconFound);
                }

                if (beforeTextChanged > afterTextChanged) {
                    if ((!firedEvent) && iconNotFound) {
                        Bundle bundle = new Bundle();
                        bundle.putString("IconSpeak", "");
                        bundle.putString("IconOpened", "");
                        bundle.putString("IconNotFound", notFoundIconText);
                        bundleEvent("SearchBar", bundle);
                        firedEvent = true;
                    }
                } else {
                    firedEvent = false;
                }

                if (iconList.size() > 0 && adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private SessionManager getSession() {
        if (getActivity() instanceof BaseActivity) {
            return ((BaseActivity) getActivity()).getSession();
        }
        return new SessionManager(requireContext());
    }

    private AppDatabase getAppDatabase() {
        if (getActivity() instanceof BaseActivity) {
            return ((BaseActivity) getActivity()).getAppDatabase();
        }
        return null;
    }

    public void speakIconText(String text) {
        if (getActivity() instanceof SpeechEngineBaseActivity) {
            ((SpeechEngineBaseActivity) getActivity()).speak(text);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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
        stopMeasuring(SearchDialogFragment.class.getSimpleName());
    }

    private void initFields(View view) {
        iconList = new ArrayList<>();
        adapter = new SearchDialogViewIconAdapter(this, iconList, getAppDatabase());
        RecyclerView mRecyclerView = view.findViewById(R.id.icon_search_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void onIconSelected(JellowIcon icon) {
        if (icon.getLevelOne() == -1) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("IconSpeak", "");
        bundle.putString("IconOpened", icon.getIconTitle());
        bundle.putString("IconNotFound", "");
        bundleEvent("SearchBar", bundle);

        dismiss();

        NavController navController = null;
        try {
            androidx.fragment.app.Fragment navHost = requireActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            if (navHost != null) {
                navController = NavHostFragment.findNavController(navHost);
            }
        } catch (Exception ignored) {}

        if (navController != null) {
            Context ctx = requireContext();
            int currentDestId = navController.getCurrentDestination() != null ? navController.getCurrentDestination().getId() : -1;
            
            androidx.navigation.NavOptions.Builder baseNavOptions = new androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.levelOneFragment, false, true)
                    .setRestoreState(true);

            if (icon.getLevelTwo() == -1 && icon.getLevelThree() == -1) {
                Bundle args = new Bundle();
                args.putInt(ctx.getString(R.string.search_parent_0), icon.getLevelOne());
                args.putString(ctx.getString(R.string.from_search), ctx.getString(R.string.search_tag));
                
                androidx.navigation.NavOptions navOptions;
                if (currentDestId == R.id.levelOneFragment) {
                    navOptions = new androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.levelOneFragment, true)
                            .setLaunchSingleTop(false)
                            .build();
                } else {
                    navOptions = new androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.levelOneFragment, true).build();
                }
                navController.navigate(R.id.levelOneFragment, args, navOptions);
            } else if (icon.getLevelOne() != -1 && icon.getLevelTwo() != -1 && icon.getLevelThree() == -1) {
                Bundle args = new Bundle();
                args.putInt(ctx.getString(R.string.level_one_intent_pos_tag), icon.getLevelOne());
                args.putInt(ctx.getString(R.string.search_parent_1), icon.getLevelTwo());
                args.putString(ctx.getString(R.string.from_search), ctx.getString(R.string.search_tag));
                String breadCrumbPath = ctx.getString(R.string.home) + "/ " +
                        getLevel1IconLabels()[icon.getLevelOne()].replace("…", "") + "/ ";
                args.putString(ctx.getString(R.string.intent_menu_path_tag), breadCrumbPath);
                
                androidx.navigation.NavOptions navOptions = baseNavOptions.build();
                if (currentDestId == R.id.levelTwoFragment) {
                    navOptions = new androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.levelTwoFragment, true)
                            .setLaunchSingleTop(false)
                            .build();
                }
                navController.navigate(R.id.levelTwoFragment, args, navOptions);
            } else if (!icon.isSequenceIcon()) {
                Bundle args = new Bundle();
                args.putString(ctx.getString(R.string.from_search), ctx.getString(R.string.search_tag));
                args.putInt(ctx.getString(R.string.level_one_intent_pos_tag), icon.getLevelOne());
                args.putInt(ctx.getString(R.string.level_2_item_pos_tag), icon.getLevelTwo());
                args.putInt(ctx.getString(R.string.search_parent_2), icon.getLevelThree());
                String breadCrumbPath = ctx.getString(R.string.home) + "/ " +
                        getLevel1IconLabels()[icon.getLevelOne()].replace("…", "") + "/ "
                        + getIconTitleLevel2(icon.getLevelOne())[icon.getLevelTwo()].replace("…", "") + "/ ";
                args.putString(ctx.getString(R.string.intent_menu_path_tag), breadCrumbPath);
                
                Bundle l2Args = new Bundle();
                l2Args.putInt(ctx.getString(R.string.level_one_intent_pos_tag), icon.getLevelOne());
                l2Args.putString(ctx.getString(R.string.intent_menu_path_tag), ctx.getString(R.string.home) + "/ " + getLevel1IconLabels()[icon.getLevelOne()].replace("…", "") + "/ ");
                
                navController.navigate(R.id.levelTwoFragment, l2Args, baseNavOptions.build());
                if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.levelTwoFragment) {
                    androidx.navigation.NavOptions navOptions = null;
                    if (currentDestId == R.id.levelThreeFragment) {
                        navOptions = new androidx.navigation.NavOptions.Builder()
                                .setPopUpTo(R.id.levelThreeFragment, true)
                                .setLaunchSingleTop(false)
                                .build();
                    }
                    navController.navigate(R.id.levelThreeFragment, args, navOptions);
                }
            } else {
                Bundle args = new Bundle();
                args.putString(ctx.getString(R.string.from_search), ctx.getString(R.string.search_tag));
                args.putInt(ctx.getString(R.string.level_2_item_pos_tag), icon.getLevelTwo());
                args.putInt(ctx.getString(R.string.search_parent_2), icon.getLevelThree());
                String breadCrumbPath = ctx.getString(R.string.home) + "/ " +
                        getLevel1IconLabels()[icon.getLevelOne()].replace("…", "") + "/ "
                        + getIconTitleLevel2(icon.getLevelOne())[icon.getLevelTwo()].replace("…", "") + "/ ";
                args.putString(ctx.getString(R.string.intent_menu_path_tag), breadCrumbPath);
                
                Bundle l2Args = new Bundle();
                l2Args.putInt(ctx.getString(R.string.level_one_intent_pos_tag), 1 /* Daily Activities */);
                l2Args.putString(ctx.getString(R.string.intent_menu_path_tag), ctx.getString(R.string.home) + "/ " + getLevel1IconLabels()[1].replace("…", "") + "/ ");
                
                navController.navigate(R.id.levelTwoFragment, l2Args, baseNavOptions.build());
                if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.levelTwoFragment) {
                    androidx.navigation.NavOptions navOptions = null;
                    if (currentDestId == R.id.activitySequenceFragment) {
                        navOptions = new androidx.navigation.NavOptions.Builder()
                                .setPopUpTo(R.id.activitySequenceFragment, true)
                                .setLaunchSingleTop(false)
                                .build();
                    }
                    navController.navigate(R.id.activitySequenceFragment, args, navOptions);
                }
            }
        }
    }

    @NonNull
    public String[] getLevel1IconLabels() {
        if (database == null) return new String[0];
        ArrayList<String> list = database.getLevelOneIconsTitles();
        return list.toArray(new String[0]);
    }

    public String[] getIconTitleLevel2(int pos) {
        if (database == null) return new String[0];
        ArrayList<String> list = database.getLevelTwoIconsTitles(pos);
        if (list != null && list.size() > 0)
            list.remove(0);
        return list.toArray(new String[0]);
    }

    public IconDatabaseFacade getDatabase() {
        return database;
    }
}

class SearchDialogViewIconAdapter extends RecyclerView.Adapter<SearchDialogViewIconAdapter.ViewHolder> {

    private final SearchDialogFragment mFragment;
    private final ArrayList<JellowIcon> mDataSource;

    public SearchDialogViewIconAdapter(SearchDialogFragment fragment, ArrayList<JellowIcon> items, AppDatabase appDatabase) {
        mFragment = fragment;
        mDataSource = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView iconImage;
        public TextView iconTitle;
        TextView iconDir;
        ImageView speakIcon;
        LinearLayout llSearchParent;

        ViewHolder(View v) {
            super(v);
            iconImage = v.findViewById(R.id.search_icon_drawable);
            iconTitle = v.findViewById(R.id.search_icon_title);
            iconDir = v.findViewById(R.id.parent_directory);
            llSearchParent = v.findViewById(R.id.llSearchParent);
            ViewCompat.setAccessibilityDelegate(v.findViewById(R.id.llSearchParent), new TalkbackHints_SingleClick());
            speakIcon = v.findViewById(R.id.speak_button);
            speakIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && pos < mDataSource.size()) {
                        mFragment.speakIconText(mDataSource.get(pos).getIconSpeech());
                        Bundle bundle = new Bundle();
                        bundle.putString("IconSpeak", mDataSource.get(pos).getIconTitle());
                        bundle.putString("IconOpened", "");
                        bundle.putString("IconNotFound", "");
                        bundleEvent("SearchBar", bundle);
                    }
                }
            });
            llSearchParent.setOnClickListener(this);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && pos < mDataSource.size()) {
                mFragment.onIconSelected(mDataSource.get(pos));
            }
        }
    }

    @NonNull
    @Override
    public SearchDialogViewIconAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.icon_search_list_item, parent, false);
        return new SearchDialogViewIconAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchDialogViewIconAdapter.ViewHolder holder, int position) {
        JellowIcon thisIcon = mDataSource.get(position);
        Context context = holder.itemView.getContext();

        if (thisIcon.getLevelOne() == -1) {
            holder.iconTitle.setText(R.string.icon_not_found);
            holder.speakIcon.setVisibility(View.GONE);
            holder.iconDir.setVisibility(View.GONE);
            holder.iconImage.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_icon_not_found));
            return;
        } else {
            String path = (thisIcon.isCustomIcon() ?
                    com.dsource.idc.jellowintl.factories.PathFactory.getBasicCustomIconsPath(context, thisIcon.getIconDrawable()) :
                    getIconPath(context, thisIcon.getIconDrawable())) + ".png";
            Glide.with(context)
                    .load(path)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(false)
                    .centerCrop()
                    .dontAnimate()
                    .into(holder.iconImage);

            holder.speakIcon.setVisibility(View.VISIBLE);
            holder.iconDir.setVisibility(View.VISIBLE);
        }

        holder.iconTitle.setText(thisIcon.getIconTitle());

        String[] arr = mFragment.getLevel1IconLabels();
        for (int i = 0; i < arr.length; i++) {
            arr[i] = arr[i].split("…")[0];
        }

        String dir = "";
        if (thisIcon.getLevelTwo() == -1) {
            dir = context.getResources().getString(R.string.home);
        } else if (thisIcon.getLevelThree() == -1) {
            if (thisIcon.getLevelOne() < arr.length) {
                dir = arr[thisIcon.getLevelOne()];
            }
        } else {
            try {
                String levelTitle = mFragment.getIconTitleLevel2(thisIcon.getLevelOne())[thisIcon.getLevelTwo()].replace("…", "");
                if (thisIcon.getLevelOne() < arr.length) {
                    dir = arr[thisIcon.getLevelOne()] + "->" + levelTitle;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        holder.iconDir.setText(dir);
    }

    @Override
    public int getItemCount() {
        return mDataSource.size();
    }
}
