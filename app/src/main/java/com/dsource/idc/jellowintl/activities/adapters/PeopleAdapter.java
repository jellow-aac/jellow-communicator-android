package com.dsource.idc.jellowintl.activities.adapters;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static com.dsource.idc.jellowintl.factories.IconFactory.EXTENSION;
import static com.dsource.idc.jellowintl.factories.PathFactory.getBasicCustomIconsPath;
import static com.dsource.idc.jellowintl.factories.PathFactory.getIconPath;
import static com.dsource.idc.jellowintl.models.GlobalConstants.ADD_BASIC_CUSTOM_ICON;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.TalkBack.TalkbackHints_SingleClick;
import com.dsource.idc.jellowintl.activities.LevelTwoActivity;
import com.dsource.idc.jellowintl.factories.IconFactory;
import com.dsource.idc.jellowintl.factories.TextFactory;
import com.dsource.idc.jellowintl.models.GlobalConstants;
import com.dsource.idc.jellowintl.models.Icon;
import com.dsource.idc.jellowintl.utility.SessionManager;

/**
 * Created by HP on 22/01/2017.
 */
public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.MyViewHolder> {
    private final LevelTwoActivity mAct;
    private final SessionManager mSession;
    private final String[] iconNameArray;
    private final String[] belowTextArray;
    private final int libIconSize;


    public PeopleAdapter(Context context, Icon[] level2IconObjects, Integer[] arrSort, int size) {
        mAct = (LevelTwoActivity) context;
        mSession = mAct.getSession();
        libIconSize = size;

        Icon[] tempIconArray = new Icon[level2IconObjects.length];
        try {
            for (int i = 0; i < level2IconObjects.length; i++) {
                tempIconArray[i] = level2IconObjects[arrSort[i]];
            }
        }catch (Exception exception){
            tempIconArray = level2IconObjects;
        }

        iconNameArray = IconFactory.getIconNames(tempIconArray);
        belowTextArray = TextFactory.getDisplayText(tempIconArray);
    }

    @Override
    public PeopleAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView;
        if(mSession.getGridSize() == GlobalConstants.ONE_ICON_PER_SCREEN){
            rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_level_xadapter_1_icon, parent, false);
        }else if(mSession.getGridSize() == GlobalConstants.TWO_ICONS_PER_SCREEN){
            rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_level_xadapter_2_icons, parent, false);
        }else if(mSession.getGridSize() == GlobalConstants.THREE_ICONS_PER_SCREEN){
            rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_level_xadapter_3_icons, parent, false);
        }else if(mSession.getGridSize() == GlobalConstants.FOUR_ICONS_PER_SCREEN){
            rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_level_xadapter_4_icons, parent, false);
        }else{
            rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_level_xadapter_9_icons, parent, false);
        }
        return new PeopleAdapter.MyViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        ViewCompat.setAccessibilityDelegate(holder.menuItemLinearLayout,
                new TalkbackHints_SingleClick());

        if (mSession.getPictureViewMode() == GlobalConstants.DISPLAY_PICTURE_ONLY)
            holder.menuItemBelowText.setVisibility(View.INVISIBLE);
        holder.menuItemBelowText.setText(belowTextArray[position]);

        Glide.with(mAct)
                .load(getIconPath(mAct, iconNameArray[position] + EXTENSION))
                .error(Drawable.createFromPath(getBasicCustomIconsPath(mAct, iconNameArray[position] + EXTENSION)))
                .into(holder.menuItemImage);

        if (iconNameArray[position].equals(ADD_BASIC_CUSTOM_ICON)){
            Glide.with(mAct).load(R.drawable.ic_plus).into(holder.menuItemImage);
        }

        if(position >=libIconSize
                && !iconNameArray[position].equals(ADD_BASIC_CUSTOM_ICON)
                && mSession.getBasicCustomIconAddState()){
            holder.listItemEditLayout.setVisibility(View.VISIBLE);
            holder.listItemEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAct.onEditIconClicked(position);
                }
            });
            holder.listItemDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAct.onDeleteIconClicked(position);
                }
            });
        }

        holder.menuItemLinearLayout.setContentDescription(belowTextArray[position]);
        holder.menuItemLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAct.tappedCategoryItemEvent(holder.menuItemLinearLayout, position);
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return iconNameArray.length;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout menuItemLinearLayout;
        private final RelativeLayout listItemEditLayout;
        private final ImageView menuItemImage;
        private final ImageView listItemDelete;
        private final ImageView listItemEdit;
        private final TextView menuItemBelowText;

        MyViewHolder(final View view) {
            super(view);
            menuItemImage = view.findViewById(R.id.icon1);
            menuItemLinearLayout = view.findViewById(R.id.linearlayout_icon1);
            menuItemBelowText = view.findViewById(R.id.te1);
            menuItemBelowText.setTextColor(Color.rgb(64, 64, 64));
            listItemEditLayout=view.findViewById(R.id.edit_remove_container);
            listItemEdit = view.findViewById(R.id.edit_icons);
            listItemDelete = view.findViewById(R.id.delete_icons);

            if(mAct.isAccessibilityTalkBackOn((AccessibilityManager) mAct.getSystemService(ACCESSIBILITY_SERVICE))) {
                Typeface tf = ResourcesCompat.getFont(mAct, R.font.mukta_semibold);
                menuItemBelowText.setTypeface(tf);
            }
            GradientDrawable gd = (GradientDrawable) view.findViewById(R.id.borderView).getBackground();
            gd.setColor(ContextCompat.getColor(mAct, android.R.color.transparent));
        }
    }
}
