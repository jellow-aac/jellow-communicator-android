package com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.GRID_SIZE;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.GridSelectListener;
import com.dsource.idc.jellowintl.models.GlobalConstants;

public class DialogNoOfIconPerScreen extends DialogFragment {

    public static GridSelectListener mGridSelectionListener;

    public static DialogNoOfIconPerScreen newInstance(int gridSize, GridSelectListener listener) {
        DialogNoOfIconPerScreen fragment = new DialogNoOfIconPerScreen();
        Bundle args = new Bundle();
        args.putInt(GRID_SIZE, gridSize);
        fragment.setArguments(args);
        mGridSelectionListener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.dialog_grid_selection, container, false);
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
        int gridSize = getArguments() != null ? getArguments().getInt(GRID_SIZE, GlobalConstants.NINE_ICONS_PER_SCREEN)
                : GlobalConstants.NINE_ICONS_PER_SCREEN;
        setUpGridDialog(view, gridSize);
    }

    private void setUpGridDialog(View view, int gridSize) {
        AccessibilityManager am = (AccessibilityManager) requireContext().getSystemService(ACCESSIBILITY_SERVICE);
        if (am == null || !am.isEnabled() || !am.isTouchExplorationEnabled()) {
            View ivClose = view.findViewById(R.id.iv_close);
            if (ivClose != null) ivClose.setVisibility(View.GONE);
        } else {
            View ivClose = view.findViewById(R.id.iv_close);
            if (ivClose != null) ivClose.setOnClickListener(v -> dismiss());
        }

        View parent = view.findViewById(R.id.parent);
        if (parent != null) {
            parent.setOnClickListener(v -> dismiss());
        }

        final ImageView GridSize1 = view.findViewById(R.id.grid_size_1x1);
        final ImageView GridSize2 = view.findViewById(R.id.grid_size_1X2);
        final ImageView GridSize3 = view.findViewById(R.id.grid_size_1X3);
        final ImageView GridSize4 = view.findViewById(R.id.grid_size_2x2);
        final ImageView GridSize6 = view.findViewById(R.id.grid_size_3X3);

        GridSize1.setOnClickListener(v -> {
            dismiss();
            if (mGridSelectionListener != null)
                mGridSelectionListener.onGridSelectListener(GlobalConstants.ONE_ICON_PER_SCREEN);
        });
        GridSize2.setOnClickListener(v -> {
            dismiss();
            if (mGridSelectionListener != null)
                mGridSelectionListener.onGridSelectListener(GlobalConstants.TWO_ICONS_PER_SCREEN);
        });
        GridSize3.setOnClickListener(v -> {
            dismiss();
            if (mGridSelectionListener != null)
                mGridSelectionListener.onGridSelectListener(GlobalConstants.THREE_ICONS_PER_SCREEN);
        });
        GridSize4.setOnClickListener(v -> {
            dismiss();
            if (mGridSelectionListener != null)
                mGridSelectionListener.onGridSelectListener(GlobalConstants.FOUR_ICONS_PER_SCREEN);
        });
        GridSize6.setOnClickListener(v -> {
            dismiss();
            if (mGridSelectionListener != null)
                mGridSelectionListener.onGridSelectListener(GlobalConstants.NINE_ICONS_PER_SCREEN);
        });

        switch (gridSize) {
            case GlobalConstants.ONE_ICON_PER_SCREEN:
                view.findViewById(R.id.fl_one_icon).setBackground(
                        ContextCompat.getDrawable(requireContext(), R.drawable.border_number_of_icons_per_screen));
                break;
            case GlobalConstants.TWO_ICONS_PER_SCREEN:
                view.findViewById(R.id.fl_two_icon).setBackground(
                        ContextCompat.getDrawable(requireContext(), R.drawable.border_number_of_icons_per_screen));
                break;
            case GlobalConstants.THREE_ICONS_PER_SCREEN:
                view.findViewById(R.id.fl_three_icon).setBackground(
                        ContextCompat.getDrawable(requireContext(), R.drawable.border_number_of_icons_per_screen));
                break;
            case GlobalConstants.FOUR_ICONS_PER_SCREEN:
                view.findViewById(R.id.fl_four_icon).setBackground(
                        ContextCompat.getDrawable(requireContext(), R.drawable.border_number_of_icons_per_screen));
                break;
            case GlobalConstants.NINE_ICONS_PER_SCREEN:
                view.findViewById(R.id.fl_nine_icon).setBackground(
                        ContextCompat.getDrawable(requireContext(), R.drawable.border_number_of_icons_per_screen));
                break;
        }
    }
}
