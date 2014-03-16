package com.baraccasoftware.securenotes.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;


/**
 * Created by angelo on 24/02/14.
 */
public class SettingsFragment extends PreferenceFragment {
    private static final String KEY_HINT_PREF = "hints_value" ;
    SettingsActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setPreferences();

    }

    @Override
    public void onResume(){
        super.onResume();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (SettingsActivity) activity;
    }

    private void setPreferences(){
        addPreferencesFromResource(R.xml.settingsfile);
        Preference openInfoB =  getPreferenceScreen().findPreference("infobaracca");
        openInfoB.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.baraccasoftware.com"));
                startActivity(browserIntent);

                return true;
            }
        });

        Preference infoActivity = getPreferenceScreen().findPreference("infoactivityrun");
        infoActivity.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mActivity.setSameApp(true);
                Intent infoIntent = new Intent(mActivity,InfoActivity.class);
                startActivity(infoIntent);
                return true;
            }
        });
        Preference setPassword =  getPreferenceScreen().findPreference("setPasswordKey");
        setPassword.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startPasswordDialog();
                return true;
            }
        });
    }


    private void startPasswordDialog(){
        SetPasswordFragment newf = new SetPasswordFragment();
        newf.show(getFragmentManager(), "password");
    }

}
