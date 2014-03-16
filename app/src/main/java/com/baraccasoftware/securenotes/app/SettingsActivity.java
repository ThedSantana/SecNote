package com.baraccasoftware.securenotes.app;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.baraccasoftware.securenotes.object.ActivityUtilityInterface;
import com.baraccasoftware.securenotes.object.PasswordPreference;

public class SettingsActivity extends FragmentActivity implements ActivityUtilityInterface {
    private SharedPreferences mSharedPreferences;
    private PasswordPreference passwordPreference;

    private SettingsFragment mSettingsFragment;
    private LockedAppFragment mLockedAppFragment;
    boolean sameApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        passwordPreference = new PasswordPreference(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setSameApp(false);
        setLayout();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            setSameApp(true);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(passwordPreference.isAppLocked()){
            if(mSettingsFragment != null) getFragmentManager().beginTransaction().remove(mSettingsFragment).commit();
            mLockedAppFragment = new LockedAppFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, mLockedAppFragment)
                    .commit();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        boolean toLock = mSharedPreferences.getBoolean("lockapponpause",true);
        if(toLock &&  !sameApp){
            passwordPreference.setLockedApp(true);
        }
    }

    @Override
    public void onBackPressed() {
        setSameApp(true);
        super.onBackPressed();
    }

    @Override
    public void setSameApp(boolean sameApp) {
        this.sameApp = sameApp;
    }

    @Override
    public boolean getSameApp() {
        return this.sameApp;
    }

    @Override
    public void setLayout(){
        if(mSettingsFragment != null) getFragmentManager().beginTransaction().remove(mSettingsFragment).commit();
        if(mLockedAppFragment != null) getSupportFragmentManager().beginTransaction()
                .remove(mLockedAppFragment).commit();
        if(passwordPreference.isAppLocked()){
            mLockedAppFragment = new LockedAppFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new LockedAppFragment())
                    .commit();
        }else {
            mSettingsFragment = new SettingsFragment();
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, mSettingsFragment)
                    .commit();
        }
    }




}
