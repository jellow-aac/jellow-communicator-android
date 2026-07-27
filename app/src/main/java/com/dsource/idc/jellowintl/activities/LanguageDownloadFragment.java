package com.dsource.idc.jellowintl.activities;

import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.SessionManager.LangValueMap;
import static com.dsource.idc.jellowintl.utility.SessionManager.MR_IN;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.factories.LanguageFactory;
import com.dsource.idc.jellowintl.utility.DownloadManager;
import com.dsource.idc.jellowintl.utility.SessionManager;

public class LanguageDownloadFragment extends BaseFragment {
    DownloadManager manager;
    RoundCornerProgressBar progressBar;
    String langCode;
    private String mCheckConn;
    Boolean tutorial = false;
    Boolean finish = true;
    Boolean close = false;
    Boolean isConnected;
    private String strLanguageDownloaded;
    private String strLanguageDownloading;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_language_download, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        getBaseActivity().setupActionBarTitle(View.GONE, "");
        if (view.findViewById(R.id.toolbar) != null) {
            view.findViewById(R.id.toolbar).setVisibility(View.GONE);
        }
        getBaseActivity().applyMonochromeColor();

        try {
            if (getArguments() != null) {
                langCode = getArguments().getString("LCODE");
                finish = getArguments().getBoolean("SPLASH", true);
                tutorial = getArguments().getBoolean("TUTORIAL", false);
                close = getArguments().getBoolean("CLOSE", false);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        progressBar = view.findViewById(R.id.pg);
        progressBar.setMax(1);
        {
            String str = getString(R.string.language_downloaded);
            if (langCode != null && langCode.equals(MR_IN)){
                str = str.replace("_", LangValueMap.get(langCode));
                strLanguageDownloaded = str;
                str = getString(R.string.language_downloading);
                str = str.replace("_",  LangValueMap.get(langCode));
                strLanguageDownloading = str;
            }else{
                str = str.replace("_", "");
                strLanguageDownloaded = str;
                str = getString(R.string.language_downloading);
                str = str.replace("_",  "");
                strLanguageDownloading = str;
            }
        }

        mCheckConn = getString(R.string.checkConnectivity);
        final DownloadManager.ProgressReceiver progressReceiver = new DownloadManager.ProgressReceiver() {
            @Override
            public void onprogress(int soFarBytes, int totalBytes) {
                progressBar.setProgress((float)soFarBytes/totalBytes);
            }

            @Override
            public void onComplete() {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), strLanguageDownloaded,
                        Toast.LENGTH_SHORT).show();

                if(getSession().getLanguage().equals(MR_IN) && !LanguageFactory.
                        isMarathiPackageAvailable(requireContext())){
                    progressBar.setProgress(0);
                    progressBar.invalidate();
                    manager.setLanguage(MR_IN);
                    strLanguageDownloading = strLanguageDownloading.replace(SessionManager.UNIVERSAL_PACKAGE,
                            LangValueMap.get(MR_IN));
                    Toast.makeText(requireContext(), strLanguageDownloading,
                            Toast.LENGTH_SHORT).show();
                    strLanguageDownloaded = strLanguageDownloaded.replace(SessionManager.UNIVERSAL_PACKAGE,
                            LangValueMap.get(MR_IN));
                    manager.start();
                }else if(tutorial) {
                    NavHostFragment.findNavController(LanguageDownloadFragment.this)
                        .navigate(R.id.action_languageDownloadFragment_to_introFragment);
                }else if(close){
                    NavHostFragment.findNavController(LanguageDownloadFragment.this).popBackStack();
                }else if(finish) {
                    NavHostFragment.findNavController(LanguageDownloadFragment.this)
                        .navigate(R.id.action_languageDownloadFragment_to_splashFragment);
                }
            }
        };

        Toast.makeText(requireContext(), strLanguageDownloading, Toast.LENGTH_SHORT).show();

        if(langCode != null) {
            try {
                isConnected = getBaseActivity().isConnectedToNetwork((ConnectivityManager)requireContext().getSystemService(Context.CONNECTIVITY_SERVICE));
                if(isConnected)
                {
                    manager = new DownloadManager(langCode, requireContext(), progressReceiver);
                    manager.start();
                }else {

                    Toast.makeText(requireContext(),mCheckConn,Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!isAnalyticsActive()) {
            resetAnalytics(requireContext(), getSession().getUserId());
        }
        isConnected = getBaseActivity().isConnectedToNetwork((ConnectivityManager)requireContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        if(isConnected) {
            if (manager != null)
                manager.resume();
        } else {
            Toast.makeText(requireContext(),mCheckConn,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(manager != null)
            manager.pause();
    }
}
