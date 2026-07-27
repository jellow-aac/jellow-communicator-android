package com.dsource.idc.jellowintl.activities;

import androidx.fragment.app.Fragment;
import com.dsource.idc.jellowintl.utility.SessionManager;
import com.dsource.idc.jellowintl.models.AppDatabase;

public class BaseFragment extends Fragment {
    
    protected BaseActivity getBaseActivity() {
        return (BaseActivity) requireActivity();
    }
    
    protected SessionManager getSession() {
        return getBaseActivity().getSession();
    }
    
    protected AppDatabase getAppDatabase() {
        return getBaseActivity().getAppDatabase();
    }
}
