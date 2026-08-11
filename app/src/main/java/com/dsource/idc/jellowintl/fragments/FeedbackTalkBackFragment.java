package com.dsource.idc.jellowintl.fragments;

import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.dsource.idc.jellowintl.BuildConfig;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.activities.AppActivity;
import com.dsource.idc.jellowintl.utility.SessionManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class FeedbackTalkBackFragment extends Fragment{
    Spinner mEasyToUse, mClearPicture, mClearVoice, mEaseToNavigate;
    ArrayAdapter<CharSequence> adapter;
    private String strRateJellow;
    private SessionManager session;

    private SessionManager getSession() {
        if (session == null) session = new SessionManager(requireContext());
        return session;
    }

    private AppActivity getAppActivity() {
        return (AppActivity) requireActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_feedback_talkback, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getAppActivity().setVisibleAct(FeedbackTalkBackFragment.class.getSimpleName());
        getAppActivity().setupBottomBar();
        getAppActivity().setupActionBarTitle(view, View.VISIBLE, getString(R.string.home)+"/ "+getString(R.string.menuFeedback));
        getAppActivity().setupToolbarMenu(view);
        getAppActivity().applyMonochromeColor();

        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        view.findViewById(R.id.comments).clearFocus();
        view.findViewById(R.id.tv1).setFocusable(true);
        view.findViewById(R.id.tv1).setFocusableInTouchMode(true);
        strRateJellow = getString(R.string.rate_jellow);
        addListenerOnSpinner(view);

        View btnSendFeedback = view.findViewById(R.id.btn_send_feedback);
        if (btnSendFeedback != null) {
            btnSendFeedback.setOnClickListener(v -> sendFeedbackToDevelopers(v));
        }

        View btnRate = view.findViewById(R.id.btn_rate_jellow);
        if (btnRate != null) {
            btnRate.setOnClickListener(v -> rateTheJellowApp(v));
        }

        View btnShare = view.findViewById(R.id.btn_share_jellow);
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> shareJellowApp(v));
        }
        
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!NavHostFragment.findNavController(FeedbackTalkBackFragment.this).popBackStack()) {
                    NavHostFragment.findNavController(FeedbackTalkBackFragment.this).navigate(R.id.mainFragment);
                }
            }
        });
        
        addAccessibilityDelegateToSpinners(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        getAppActivity().setVisibleAct(FeedbackTalkBackFragment.class.getSimpleName());
        getAppActivity().setupActionBarTitle(getView(), View.VISIBLE, getString(R.string.home)+"/ "+getString(R.string.menuFeedback));
        getAppActivity().setupToolbarMenu(getView());
        if(!isAnalyticsActive()) {
            resetAnalytics(requireContext(), getSession().getUserId());
        }
    }

    public void addAccessibilityDelegateToSpinners(View view) {
        mEasyToUse.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
                super.onInitializeAccessibilityEvent(host, event);
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                    view.findViewById(R.id.tv2).sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                }
            }
        });
        mClearPicture.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
                super.onInitializeAccessibilityEvent(host, event);
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                    view.findViewById(R.id.tv3).sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                }
            }
        });
        mClearVoice.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
                super.onInitializeAccessibilityEvent(host, event);
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                    view.findViewById(R.id.tv4).sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                }
            }
        });
        mEaseToNavigate.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
                super.onInitializeAccessibilityEvent(host, event);
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                    view.findViewById(R.id.tv5).sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                }
            }
        });
    }

    public void addListenerOnSpinner(View view) {
        adapter = ArrayAdapter.createFromResource(requireContext(), R.array.ratings,
                R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEasyToUse = view.findViewById(R.id.easytouse);
        mEasyToUse.setAdapter(adapter);
        mClearPicture = view.findViewById(R.id.clearpictures);
        mClearPicture.setAdapter(adapter);
        mClearVoice = view.findViewById(R.id.clearvoice);
        mClearVoice.setAdapter(adapter);
        mEaseToNavigate = view.findViewById(R.id.navigate);
        mEaseToNavigate.setAdapter(adapter);
    }

    public void sendFeedbackToDevelopers(View v){
        if((mEasyToUse != null) && (mClearPicture != null) && (mClearVoice != null) && (mEaseToNavigate != null)) {
            FirebaseDatabase mDB = FirebaseDatabase.getInstance();
            DatabaseReference mRef = mDB.getReference(BuildConfig.DB_TYPE +"/in-app-reviews");
            HashMap<String, Object> map = new HashMap<>();
            map.put("Easy to use", mEasyToUse.getSelectedItem());
            map.put("Clear pictures", mClearPicture.getSelectedItem());
            map.put("Clear voice", mClearVoice.getSelectedItem());
            map.put("Easy to navigate", mEaseToNavigate.getSelectedItem());
            map.put("Platform", "Android");
            map.put("Comments", ((EditText)getView().findViewById(R.id.comments)).getText().toString());
            mRef.child(mRef.push().getKey())
                .setValue(map)
                .addOnSuccessListener(aVoid -> {
                    Intent email = new Intent(Intent.ACTION_SEND);
                    email.putExtra(Intent.EXTRA_EMAIL, new String[]{"jellowcommunicator@gmail.com"});
                    email.putExtra(Intent.EXTRA_SUBJECT, "Jellow Feedback");
                    email.putExtra(Intent.EXTRA_TEXT, "Easy to use: " + mEasyToUse.getSelectedItem()
                            + "\nClear Pictures: " + mClearPicture.getSelectedItem()
                            + "\nClear Voices: " + mClearVoice.getSelectedItem()
                            + "\nEasy to Navigate: " + mEaseToNavigate.getSelectedItem()
                            + "\n\nComments and Suggestions:-\n" +
                            ((EditText)getView().findViewById(R.id.comments)).getText().toString());
                    email.setType("message/rfc822");
                    PackageManager packageManager = requireActivity().getPackageManager();
                    List<ResolveInfo> activities = packageManager.queryIntentActivities(email, 0);
                    boolean isIntentSafe = activities.size() > 0;
                    if (isIntentSafe)
                        startActivity(Intent.createChooser(email, "Choose an Email client :"));
                    Toast.makeText(requireContext(),
                            getString(R.string.received_your_feedback), Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(FeedbackTalkBackFragment.this).popBackStack();
                })
                .addOnFailureListener(e -> NavHostFragment.findNavController(FeedbackTalkBackFragment.this).popBackStack());
        }else{
            Toast.makeText(requireContext(), strRateJellow, Toast.LENGTH_SHORT).show();
        }
    }

    public void rateTheJellowApp(View v){
        Uri uri = Uri.parse("market://details?id=" + requireActivity().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            startActivity(goToMarket);
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + requireActivity().getPackageName())));
        }
    }

    public void shareJellowApp(View v){
        String message = getString(R.string.share_string);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }
}
