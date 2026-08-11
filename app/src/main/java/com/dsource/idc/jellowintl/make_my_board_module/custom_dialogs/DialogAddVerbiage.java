package com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.BOARD_ID;
import static com.dsource.idc.jellowintl.models.GlobalConstants.BASIC_IS_CATEGORY;
import static com.dsource.idc.jellowintl.models.GlobalConstants.ICON_POSITION;
import static com.dsource.idc.jellowintl.models.GlobalConstants.IS_HOME_CUSTOM_ICON;
import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.startMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.dsource.idc.jellowintl.Presentor.CustomBasicIconHelper;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.activities.BaseActivity;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.data_models.BoardModel;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.databases.BoardDatabase;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.databases.TextDatabase;
import com.dsource.idc.jellowintl.models.CustomIconsModel;
import com.dsource.idc.jellowintl.models.GlobalConstants;
import com.dsource.idc.jellowintl.models.Icon;
import com.dsource.idc.jellowintl.models.JellowIcon;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.util.ArrayList;

public class DialogAddVerbiage extends DialogFragment implements View.OnClickListener {

    public static final String JELLOW_ID = "icon";
    public static final String FETCH_FLAG = "fetch_flag";
    public static final String IS_PRIMARY_FLAG = "is_primary_flag";
    public static OnSuccessListener<String> callback;

    private String id;
    private Context context;
    private LinearLayout expList;
    private ArrayList<LinearLayout> expListLayouts;
    private ArrayList<String> verbiageList;
    private ArrayList<String> defaultVerbiage;
    private JellowIcon thisIcon = null;
    private Icon presentVerbiage = null;
    private TextDatabase database;
    private boolean iconUpdate = false;
    private boolean isCustomizedHomeIcon;

    public static DialogAddVerbiage newInstance(Bundle args, OnSuccessListener<String> listener) {
        DialogAddVerbiage fragment = new DialogAddVerbiage();
        fragment.setArguments(args);
        callback = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.dialog_add_verbiage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = requireContext();

        Bundle args = getArguments();
        if (args != null) {
            isCustomizedHomeIcon = args.containsKey(IS_HOME_CUSTOM_ICON);
            if (args.getString(BOARD_ID) != null) {
                id = args.getString(BOARD_ID);
            }
            thisIcon = (JellowIcon) args.getSerializable(JELLOW_ID);
        }
        initViews(view);
        setUpFields();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof BaseActivity) {
            BaseActivity baseAct = (BaseActivity) getActivity();
            if (!isAnalyticsActive()) {
                resetAnalytics(context, baseAct.getSession().getUserId());
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
            stopMeasuring(DialogAddVerbiage.class.getSimpleName());
        }
    }

    private void setUpFields() {
        if (!(getActivity() instanceof BaseActivity)) return;
        BaseActivity baseAct = (BaseActivity) getActivity();

        if (isCustomizedHomeIcon) {
            presentVerbiage = CustomBasicIconHelper.getCustomBasicIcon(baseAct.getAppDatabase(), thisIcon.getIconDrawable());
            updateUI(presentVerbiage);
            iconUpdate = presentVerbiage != null;
        } else {
            BoardDatabase boardDatabase = new BoardDatabase(baseAct.getAppDatabase());
            BoardModel thisBoard = boardDatabase.getBoardById(id);
            if (thisBoard != null) {
                this.database = new TextDatabase(context, thisBoard.getLanguage(), baseAct.getAppDatabase());
            }

            Bundle args = getArguments();
            String fetchFlag = args != null ? args.getString(FETCH_FLAG) : null;
            String primaryFlag = args != null ? args.getString(IS_PRIMARY_FLAG) : null;

            if (fetchFlag != null && primaryFlag != null && database != null) {
                if (fetchFlag.equals("NULL") && primaryFlag.equals("NULL")) {
                    presentVerbiage = null;
                    updateUI(null);
                    iconUpdate = false;
                } else if (!fetchFlag.equals("NULL") && primaryFlag.equals("NULL")) {
                    presentVerbiage = database.getVerbiageById(fetchFlag);
                    updateUI(presentVerbiage);
                    iconUpdate = true;
                } else {
                    presentVerbiage = database.getVerbiageById(fetchFlag);
                    updateUI(presentVerbiage);
                    iconUpdate = false;
                }
            }
        }
    }

    private void updateUI(Icon currentVerbiage) {
        if (currentVerbiage == null) {
            initVerbiageDialog();
            enableAllViews(false);
        } else {
            presentVerbiage(currentVerbiage);
        }
    }

    private void initViews(View rootView) {
        Button save = rootView.findViewById(R.id.save_button);
        Button btnReset = rootView.findViewById(R.id.cancel_button);
        expList = rootView.findViewById(R.id.exp_verbiage_list);
        verbiageRelatedViews();

        rootView.findViewById(R.id.parent).setOnClickListener(v -> dismiss());
        rootView.findViewById(R.id.top_container).setOnClickListener(v -> {});
        btnReset.setOnClickListener(v -> updateUI(presentVerbiage));
        save.setText(getResources().getString(R.string.txtSave));
        btnReset.setText(getResources().getString(R.string.reset));
        save.setOnClickListener(v -> {
            saveToDatabase();
            dismiss();
        });

        rootView.findViewById(R.id.close).setOnClickListener(v -> dismiss());
    }

    private void saveToDatabase() {
        if (!(getActivity() instanceof BaseActivity)) return;
        BaseActivity baseAct = (BaseActivity) getActivity();

        if (isCustomizedHomeIcon) {
            Bundle args = getArguments();
            CustomBasicIconHelper.insertCustomBasicIcon(baseAct.getAppDatabase(),
                    new CustomIconsModel(
                            thisIcon.getVerbiageId(),
                            args != null ? args.getString(ICON_POSITION) : "",
                            baseAct.getSession().getLanguage(),
                            new Gson().toJson(saveVerbiage(true)),
                            args != null && args.getBoolean(BASIC_IS_CATEGORY, false)
                    )
            );
        } else if (database != null) {
            if (!iconUpdate) {
                database.addNewVerbiage(thisIcon.getVerbiageId(), saveVerbiage(false));
            } else {
                database.updateVerbiage(thisIcon.getVerbiageId(), saveVerbiage(false));
            }
        }
        if (callback != null) {
            callback.onSuccess("Success");
        }
    }

    private void verbiageRelatedViews() {
        defaultVerbiage = new ArrayList<>();
        verbiageList = new ArrayList<>();
        defaultVerbiage.add(getResources().getString(R.string.i_like));
        defaultVerbiage.add(getResources().getString(R.string.really_like));
        defaultVerbiage.add(getResources().getString(R.string.i_want));
        defaultVerbiage.add(getResources().getString(R.string.really_want));
        defaultVerbiage.add(getResources().getString(R.string.want_more));
        defaultVerbiage.add(getResources().getString(R.string.really_want_more));
        defaultVerbiage.add(getResources().getString(R.string.i_dont_like));
        defaultVerbiage.add(getResources().getString(R.string.really_dont_like));
        defaultVerbiage.add(getResources().getString(R.string.dont_want));
        defaultVerbiage.add(getResources().getString(R.string.really_dont_want));
        defaultVerbiage.add(getResources().getString(R.string.dont_want_more));
        defaultVerbiage.add(getResources().getString(R.string.really_dont_want_more));

        @SuppressLint("Recycle")
        TypedArray iconImages = context.getResources().obtainTypedArray(R.array.expressive_icon_unpressed);
        String[] contentDescExpression = {
                getString(R.string.like), getString(R.string.yes), getString(R.string.more),
                getString(R.string.dont_like), getString(R.string.no), getString(R.string.less)
        };
        expListLayouts = new ArrayList<>();

        AccessibilityManager am = (AccessibilityManager) context.getSystemService(ACCESSIBILITY_SERVICE);
        boolean talkBackOn = am != null && am.isEnabled() && am.isTouchExplorationEnabled();

        for (int i = 0; i < 6; i++) {
            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(context).inflate(R.layout.verbiage_list_item, null);

            if (talkBackOn) {
                ImageView iv = view.findViewById(R.id.add_remove);
                ((LinearLayout) view).removeViewAt(3);
                ((LinearLayout) view).addView(iv, 1);
            }

            expListLayouts.add((LinearLayout) view);
            view.findViewById(R.id.add_remove).setOnClickListener(this);
            ((ImageView) view.findViewById(R.id.icon)).setImageDrawable(iconImages.getDrawable(i));
            view.findViewById(R.id.icon).setContentDescription(contentDescExpression[i]);
            expListLayouts.get(i).findViewById(R.id.add_remove).
                    setContentDescription(getString(R.string.tap_icon_to_deselect_expression));
            expList.addView(view);
        }
        iconImages.recycle();
    }

    @SuppressLint("SetTextI18n")
    public void initVerbiageDialog() {
        if (thisIcon == null) return;
        String name = thisIcon.getIconTitle();
        int j = 0;
        for (int i = 0; i < 6; i++) {
            ((EditText) expListLayouts.get(i).findViewById(R.id.verbiage_text)).setText(defaultVerbiage.get(j++) + " " + name);
            ((EditText) expListLayouts.get(i).findViewById(R.id.verbiage_really_text)).setText(defaultVerbiage.get(j++) + " " + name);
        }
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < 6; i++) {
            if (v == expListLayouts.get(i).findViewById(R.id.add_remove)) {
                if (expListLayouts.get(i).findViewById(R.id.verbiage_text).isEnabled()) {
                    expListLayouts.get(i).findViewById(R.id.verbiage_text).setEnabled(false);
                    expListLayouts.get(i).findViewById(R.id.verbiage_text).setAlpha(GlobalConstants.DISABLE_ALPHA);
                    expListLayouts.get(i).findViewById(R.id.verbiage_really_text).setEnabled(false);
                    expListLayouts.get(i).findViewById(R.id.verbiage_really_text).setAlpha(GlobalConstants.DISABLE_ALPHA);
                    ((ImageView) expListLayouts.get(i).findViewById(R.id.add_remove)).
                            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.plus));
                    expListLayouts.get(i).findViewById(R.id.add_remove).
                            setContentDescription(getString(R.string.tap_icon_to_select_expression));
                } else {
                    expListLayouts.get(i).findViewById(R.id.verbiage_text).setEnabled(true);
                    expListLayouts.get(i).findViewById(R.id.verbiage_text).setAlpha(GlobalConstants.ENABLE_ALPHA);
                    expListLayouts.get(i).findViewById(R.id.verbiage_really_text).setEnabled(true);
                    expListLayouts.get(i).findViewById(R.id.verbiage_really_text).setAlpha(GlobalConstants.ENABLE_ALPHA);
                    ((ImageView) expListLayouts.get(i).findViewById(R.id.add_remove)).
                            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.minus));
                    expListLayouts.get(i).findViewById(R.id.add_remove).
                            setContentDescription(getString(R.string.tap_icon_to_deselect_expression));
                }
            }
        }
    }

    private Icon saveVerbiage(boolean isBasicCustomIcon) {
        verbiageList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            if ((expListLayouts.get(i).findViewById(R.id.verbiage_text)).isEnabled()) {
                if (((EditText) expListLayouts.get(i).findViewById(R.id.verbiage_text))
                        .getText().toString().trim().isEmpty())
                    verbiageList.add("NA");
                else
                    verbiageList.add(((EditText) expListLayouts.get(i).findViewById(R.id.verbiage_text))
                            .getText().toString().trim());

                if (((EditText) expListLayouts.get(i).findViewById(R.id.verbiage_really_text))
                        .getText().toString().trim().isEmpty())
                    verbiageList.add("NA");
                else
                    verbiageList.add(((EditText) expListLayouts.get(i).findViewById(R.id.verbiage_really_text))
                            .getText().toString().trim());
            } else {
                verbiageList.add("NA");
                verbiageList.add("NA");
            }
        }

        Icon newIcon = new Icon();
        newIcon.setDisplay_Label(thisIcon.getText());
        newIcon.setSpeech_Label(thisIcon.getText());
        newIcon.setSearchTag(thisIcon.getText());
        newIcon.setEvent_Tag(String.valueOf(thisIcon.getIconDrawable()));
        newIcon.setL(verbiageList.get(0));
        newIcon.setLL(verbiageList.get(1));
        newIcon.setY(verbiageList.get(2));
        newIcon.setYY(verbiageList.get(3));
        newIcon.setM(verbiageList.get(4));
        newIcon.setMM(verbiageList.get(5));
        newIcon.setD(verbiageList.get(6));
        newIcon.setDD(verbiageList.get(7));
        newIcon.setN(verbiageList.get(8));
        newIcon.setNN(verbiageList.get(9));
        newIcon.setS(verbiageList.get(10));
        newIcon.setSS(verbiageList.get(11));

        return newIcon;
    }

    private void enableAllViews(boolean disable) {
        if (disable) {
            for (int i = 0; i < 6; i++) {
                expListLayouts.get(i).findViewById(R.id.verbiage_text).setEnabled(true);
                expListLayouts.get(i).findViewById(R.id.verbiage_text).setAlpha(GlobalConstants.ENABLE_ALPHA);
                expListLayouts.get(i).findViewById(R.id.verbiage_really_text).setEnabled(true);
                expListLayouts.get(i).findViewById(R.id.verbiage_really_text).setAlpha(GlobalConstants.ENABLE_ALPHA);
                ((ImageView) expListLayouts.get(i).findViewById(R.id.add_remove)).
                        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.minus));
                expListLayouts.get(i).findViewById(R.id.add_remove).
                        setContentDescription(getString(R.string.tap_icon_to_deselect_expression));
            }
        } else {
            for (int i = 0; i < 6; i++) {
                expListLayouts.get(i).findViewById(R.id.verbiage_text).setEnabled(false);
                expListLayouts.get(i).findViewById(R.id.verbiage_text).setAlpha(GlobalConstants.DISABLE_ALPHA);
                expListLayouts.get(i).findViewById(R.id.verbiage_really_text).setEnabled(false);
                expListLayouts.get(i).findViewById(R.id.verbiage_really_text).setAlpha(GlobalConstants.DISABLE_ALPHA);
                ((ImageView) expListLayouts.get(i).findViewById(R.id.add_remove)).
                        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.plus));
                expListLayouts.get(i).findViewById(R.id.add_remove).
                        setContentDescription(getString(R.string.tap_icon_to_select_expression));
            }
        }
    }

    private boolean isVerbiageEmpty(String text) {
        return text == null || text.trim().isEmpty() || text.equals("NA");
    }

    public void presentVerbiage(Icon verbiageModel) {
        this.presentVerbiage = verbiageModel;
        if (verbiageModel != null) {
            if (isVerbiageEmpty(verbiageModel.getL()) && isVerbiageEmpty(verbiageModel.getLL())) {
                disableVerbiage(0, true);
            } else {
                disableVerbiage(0, false);
                ((EditText) expListLayouts.get(0).findViewById(R.id.verbiage_text)).setText(isVerbiageEmpty(verbiageModel.getL()) ? "" : verbiageModel.getL());
                ((EditText) expListLayouts.get(0).findViewById(R.id.verbiage_really_text)).setText(isVerbiageEmpty(verbiageModel.getLL()) ? "" : verbiageModel.getLL());
            }
            if (isVerbiageEmpty(verbiageModel.getY()) && isVerbiageEmpty(verbiageModel.getYY())) {
                disableVerbiage(1, true);
            } else {
                disableVerbiage(1, false);
                ((EditText) expListLayouts.get(1).findViewById(R.id.verbiage_text)).setText(isVerbiageEmpty(verbiageModel.getY()) ? "" : verbiageModel.getY());
                ((EditText) expListLayouts.get(1).findViewById(R.id.verbiage_really_text)).setText(isVerbiageEmpty(verbiageModel.getYY()) ? "" : verbiageModel.getYY());
            }
            if (isVerbiageEmpty(verbiageModel.getM()) && isVerbiageEmpty(verbiageModel.getMM())) {
                disableVerbiage(2, true);
            } else {
                disableVerbiage(2, false);
                ((EditText) expListLayouts.get(2).findViewById(R.id.verbiage_text)).setText(isVerbiageEmpty(verbiageModel.getM()) ? "" : verbiageModel.getM());
                ((EditText) expListLayouts.get(2).findViewById(R.id.verbiage_really_text)).setText(isVerbiageEmpty(verbiageModel.getMM()) ? "" : verbiageModel.getMM());
            }
            if (isVerbiageEmpty(verbiageModel.getD()) && isVerbiageEmpty(verbiageModel.getDD())) {
                disableVerbiage(3, true);
            } else {
                disableVerbiage(3, false);
                ((EditText) expListLayouts.get(3).findViewById(R.id.verbiage_text)).setText(isVerbiageEmpty(verbiageModel.getD()) ? "" : verbiageModel.getD());
                ((EditText) expListLayouts.get(3).findViewById(R.id.verbiage_really_text)).setText(isVerbiageEmpty(verbiageModel.getDD()) ? "" : verbiageModel.getDD());
            }
            if (isVerbiageEmpty(verbiageModel.getN()) && isVerbiageEmpty(verbiageModel.getNN())) {
                disableVerbiage(4, true);
            } else {
                disableVerbiage(4, false);
                ((EditText) expListLayouts.get(4).findViewById(R.id.verbiage_text)).setText(isVerbiageEmpty(verbiageModel.getN()) ? "" : verbiageModel.getN());
                ((EditText) expListLayouts.get(4).findViewById(R.id.verbiage_really_text)).setText(isVerbiageEmpty(verbiageModel.getNN()) ? "" : verbiageModel.getNN());
            }
            if (isVerbiageEmpty(verbiageModel.getS()) && isVerbiageEmpty(verbiageModel.getSS())) {
                disableVerbiage(5, true);
            } else {
                disableVerbiage(5, false);
                ((EditText) expListLayouts.get(5).findViewById(R.id.verbiage_text)).setText(isVerbiageEmpty(verbiageModel.getS()) ? "" : verbiageModel.getS());
                ((EditText) expListLayouts.get(5).findViewById(R.id.verbiage_really_text)).setText(isVerbiageEmpty(verbiageModel.getSS()) ? "" : verbiageModel.getSS());
            }
        }
    }

    public void disableVerbiage(int index, boolean disable) {
        if (disable) {
            expListLayouts.get(index).findViewById(R.id.verbiage_text).setEnabled(false);
            expListLayouts.get(index).findViewById(R.id.verbiage_text).setAlpha(GlobalConstants.DISABLE_ALPHA);
            expListLayouts.get(index).findViewById(R.id.verbiage_really_text).setEnabled(false);
            expListLayouts.get(index).findViewById(R.id.verbiage_really_text).setAlpha(GlobalConstants.DISABLE_ALPHA);
            ((ImageView) expListLayouts.get(index).findViewById(R.id.add_remove)).
                    setImageDrawable(ContextCompat.getDrawable(context, R.drawable.plus));
            ((EditText) expListLayouts.get(index).findViewById(R.id.verbiage_text)).setText(null);
            ((EditText) expListLayouts.get(index).findViewById(R.id.verbiage_really_text)).setText(null);
            expListLayouts.get(index).findViewById(R.id.add_remove).
                    setContentDescription(getString(R.string.tap_icon_to_select_expression));
        } else {
            expListLayouts.get(index).findViewById(R.id.verbiage_text).setEnabled(true);
            expListLayouts.get(index).findViewById(R.id.verbiage_text).setAlpha(GlobalConstants.ENABLE_ALPHA);
            expListLayouts.get(index).findViewById(R.id.verbiage_really_text).setEnabled(true);
            expListLayouts.get(index).findViewById(R.id.verbiage_really_text).setAlpha(GlobalConstants.ENABLE_ALPHA);
            ((ImageView) expListLayouts.get(index).findViewById(R.id.add_remove)).
                    setImageDrawable(ContextCompat.getDrawable(context, R.drawable.minus));
            expListLayouts.get(index).findViewById(R.id.add_remove).
                    setContentDescription(getString(R.string.tap_icon_to_deselect_expression));
        }
    }
}
