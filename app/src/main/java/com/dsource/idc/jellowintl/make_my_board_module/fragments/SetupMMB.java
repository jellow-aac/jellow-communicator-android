package com.dsource.idc.jellowintl.make_my_board_module.fragments;

import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.BOARD_ID;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.activities.BaseActivity;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.databases.TextDatabase;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.helper_classes.GeneralDatabaseCreator;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.SuccessCallBack;
import com.dsource.idc.jellowintl.models.GlobalConstants;
import com.dsource.idc.jellowintl.utility.SessionManager;

public class SetupMMB extends DialogFragment {
    public static final String LCODE = "LCODE";
    public static final String VCODE = "VCODE";
    private RoundCornerProgressBar progressBar;
    private String langCode, voiceCode;
    private String boardId;
    private TextView progressText;

    public static SetupMMB newInstance(String boardId, String langCode, String voiceCode) {
        SetupMMB fragment = new SetupMMB();
        Bundle bundle = new Bundle();
        bundle.putString(BOARD_ID, boardId);
        bundle.putString(LCODE, langCode);
        bundle.putString(VCODE, voiceCode);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Translucent);
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_language_download, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View customAppBar = view.findViewById(R.id.custom_app_bar);
        if (customAppBar != null) customAppBar.setVisibility(View.GONE);
        View toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setVisibility(View.GONE);

        progressBar = view.findViewById(R.id.pg);
        progressText = view.findViewById(R.id.progress_text);
        if (progressBar != null) {
            progressBar.setContentDescription(getString(R.string.setting_up_the_language));
        }

        if (getArguments() != null && getArguments().getString(LCODE) != null) {
            langCode = getArguments().getString(LCODE);
            voiceCode = getArguments().getString(VCODE);
            boardId = getArguments().getString(BOARD_ID);
        } else {
            Toast.makeText(requireContext(), R.string.unable_to_create_board, Toast.LENGTH_LONG).show();
            dismiss();
            return;
        }

        BaseActivity baseAct = (BaseActivity) requireActivity();
        if (!new TextDatabase(requireContext(), langCode, baseAct.getAppDatabase()).checkForTableExists()) {
            if (progressText != null) progressText.setText(R.string.setting_up_the_language);
            createDatabase();
        } else {
            createIconDatabase();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void createDatabase() {
        BaseActivity baseAct = (BaseActivity) requireActivity();
        TextDatabase databaseHelper = new TextDatabase(requireContext(), langCode, baseAct.getAppDatabase());
        GeneralDatabaseCreator<TextDatabase> creator = new GeneralDatabaseCreator<>(databaseHelper, new SuccessCallBack() {
            @Override
            public void setProgressSize(final int progressSize) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) {
                            progressBar.setMax(progressSize);
                        }
                    });
                }
            }

            @Override
            public void updateProgress(final int progress) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) {
                            progressBar.setProgress(progress);
                        }
                    });
                }
            }

            @Override
            public void onSuccess(Object object) {
                createIconDatabase();
            }
        });
        creator.execute();
    }

    private void createIconDatabase() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (progressText != null) progressText.setText(R.string.completed_process);
                if (getActivity() instanceof BaseActivity) {
                    BaseActivity activity = (BaseActivity) getActivity();
                    SessionManager session = activity.getSession();
                    session.setLanguageDataUpdateState(langCode, GlobalConstants.LANGUAGE_STATE_NO_CHANGE);
                    session.setCurrentBoardLanguage(langCode);
                    session.setBoardVoice(voiceCode);
                    session.setBoardDatabaseStatus(true, langCode);

                    Bundle bundle = new Bundle();
                    bundle.putString(BOARD_ID, boardId);
                    bundle.putString(LCODE, langCode);
                    try {
                        NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment);
                        navController.navigate(R.id.iconSelectFragment, bundle);
                    } catch (Exception e) {
                        // ignored
                    }
                }
                dismiss();
            });
        }
    }
}
