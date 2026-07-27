package com.dsource.idc.jellowintl.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.models.GlobalConstants;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import static android.content.Context.ACCESSIBILITY_SERVICE;

public class IntroFragment extends BaseFragment {
    private ViewPager2 viewPager;
    private Button btnNext;
    private Button btnPrev;
    private Button btnDone;
    
    private String intro_title, intro_caption, intro2_title, intro2_caption, intro3_title,
            intro3_caption, intro4_title, intro4_caption, intro5_title, intro5_caption, intro7title,
            intro7_btn_getStarted;
            
    private final int[] layouts = new int[]{
            R.layout.intro,
            R.layout.intro5,
            R.layout.intro2,
            R.layout.intro3,
            R.layout.intro4,
            R.layout.intro7
    };
    
    private final String[] layoutNames = new String[]{
            "intro",
            "intro5",
            "intro2",
            "intro3",
            "intro4",
            "intro7"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_intro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        getBaseActivity().setupActionBarTitle(View.GONE, "");
        if (view.findViewById(R.id.toolbar) != null) {
            view.findViewById(R.id.toolbar).setVisibility(View.GONE);
        }
        
        getSession().setLanguageDataUpdateState(getSession().getLanguage(), GlobalConstants.LANGUAGE_STATE_CREATE_DB);
        
        viewPager = view.findViewById(R.id.viewPager);
        btnNext = view.findViewById(R.id.btnNext);
        btnPrev = view.findViewById(R.id.btnPrev);
        btnDone = view.findViewById(R.id.btnDone);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        
        getViewResource();
        
        IntroPagerAdapter adapter = new IntroPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                FirebaseCrashlytics.getInstance().log("Slide visible:" + layoutNames[position]);
                
                if (position == layouts.length - 1) {
                    btnNext.setVisibility(View.GONE);
                    btnDone.setVisibility(View.VISIBLE);
                } else {
                    btnNext.setVisibility(View.VISIBLE);
                    btnDone.setVisibility(View.GONE);
                }
                
                if (position == 0) {
                    btnPrev.setVisibility(View.GONE);
                } else {
                    btnPrev.setVisibility(View.VISIBLE);
                }
            }
        });
        
        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < layouts.length - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });
        
        btnPrev.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });
        
        btnDone.setOnClickListener(v -> {
            getSession().setCompletedIntro(true);
            NavHostFragment.findNavController(IntroFragment.this).navigate(R.id.action_introFragment_to_splashFragment);
        });
    }
    
    public void changeDemoScreen(View view) {
        if (view.getId() == R.id.btnMoveLeft) {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        } else if (view.getId() == R.id.btnMoveRight) {
            if (viewPager.getCurrentItem() < layouts.length - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if(getBaseActivity().isAccessibilityTalkBackOn((AccessibilityManager) requireContext().getSystemService(ACCESSIBILITY_SERVICE))){
            if (btnNext.getVisibility() == View.VISIBLE) {
                btnNext.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
            } else {
                btnDone.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void getViewResource() {
        intro_title = getString(R.string.txt_intro1_central9btn);
        intro_caption = getString(R.string.txt_intro1_categorybtn);
        intro2_title = getString(R.string.txt_intro2_appUsageDesc);
        intro2_caption = getString(R.string.txt_intro2_speakUsingJellow);
        intro3_title = getString(R.string.txt_intro3_level2CatDesc);
        intro3_caption = getString(R.string.txt_intro3_navWithJellow);
        intro4_title = getString(R.string.txt_intro4_customizeAppDesc);
        intro4_caption = getString(R.string.txt_intro4_customizeJellow);
        intro5_title = getString(R.string.txt_intro5_jellowUsageDesc);
        intro5_caption = getString(R.string.txt_intro5_expressiveBtn);
        intro7title = getString(R.string.txt_intro7_getStartedDesc);
        intro7_btn_getStarted = getString(R.string.txt_intro7_getStarted);
    }

    public void setupNextSlide(SampleSlideFragment newFragment) {
        View view = newFragment.getView();
        if (view != null) {
            if (view.findViewById(R.id.toolbar) != null) {
                view.findViewById(R.id.toolbar).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
            }
            if (view.findViewById(R.id.tvActionbarTitle) != null) {
                ((TextView) view.findViewById(R.id.tvActionbarTitle)).setTextColor(ContextCompat.getColor(requireContext(), R.color.app_background));
            }
        }

        switch(newFragment.getLayoutName()){
            case "intro":
                setText2TextView(newFragment, R.id.tv_intro_title, intro_title);
                setText2TextView(newFragment, R.id.tv_intro_caption, intro_caption);
                break;
            case "intro2":
                setText2TextView(newFragment, R.id.tv_intro2_title, intro2_title);
                setText2TextView(newFragment, R.id.tv_intro2_caption, intro2_caption);
                break;
            case "intro3":
                setText2TextView(newFragment, R.id.tv_intro3_title, intro3_title);
                setText2TextView(newFragment, R.id.tv_intro3_caption, intro3_caption);
                break;
            case "intro4":
                setText2TextView(newFragment, R.id.tv_intro4_title, intro4_title);
                setText2TextView(newFragment, R.id.tv_intro4_caption, intro4_caption);
                break;
            case "intro5":
                setText2TextView(newFragment, R.id.tv_intro5_title, intro5_title);
                setText2TextView(newFragment, R.id.tv_intro5_caption, intro5_caption);
                break;
            case "intro7":
                setText2TextView(newFragment, R.id.intro7_tvtop, intro7title);
                setText2Button(newFragment, R.id.btn_getStarted, intro7_btn_getStarted);
                break;
        }
    }

    private void setText2TextView(SampleSlideFragment parent, int tv, String text) {
        try {
            if (parent.getView() != null && parent.getView().findViewById(tv) != null) {
                ((TextView) parent.getView().findViewById(tv)).setText(text);
            }
        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private void setText2Button(SampleSlideFragment parent, int btn, String text) {
        try {
            if (parent.getView() != null && parent.getView().findViewById(btn) != null) {
                ((Button) parent.getView().findViewById(btn)).setText(text);
                
                // Bind the getStarted method to this button if it's the last slide
                if (btn == R.id.btn_getStarted) {
                    parent.getView().findViewById(btn).setOnClickListener(v -> {
                        getSession().setCompletedIntro(true);
                        NavHostFragment.findNavController(IntroFragment.this).navigate(R.id.action_introFragment_to_splashFragment);
                    });
                }
            }
        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }
    
    private class IntroPagerAdapter extends FragmentStateAdapter {
        public IntroPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return SampleSlideFragment.newInstance(layouts[position], layoutNames[position]);
        }

        @Override
        public int getItemCount() {
            return layouts.length;
        }
        
        @Override
        public long getItemId(int position) {
            return layouts[position];
        }

        @Override
        public boolean containsItem(long itemId) {
            for (int id : layouts) {
                if (id == itemId) return true;
            }
            return false;
        }
    }
}
