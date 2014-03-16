package com.baraccasoftware.securenotes.app;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.baraccasoftware.securenotes.object.ActivityUtilityInterface;
import com.baraccasoftware.securenotes.object.PasswordPreference;

public class InfoActivity extends FragmentActivity implements ActivityUtilityInterface{
    private PasswordPreference passwordPreference;
    private SharedPreferences mSharedPreferences;
    private Fragment fragment;
    boolean sameApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_info);
        passwordPreference = new PasswordPreference(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setSameApp(false);
        setLayout();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            setSameApp(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {

        super.onStart();
        if(passwordPreference.isAppLocked()){
            if(fragment != null) getSupportFragmentManager().beginTransaction()
                    .remove(fragment).commit();
            fragment = new LockedAppFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_info_activity,fragment).commit();
        }

    }

    @Override
    protected void onStop() {

        boolean toLock = mSharedPreferences.getBoolean("lockapponpause",true);
        if(toLock &&  !sameApp){
            passwordPreference.setLockedApp(true);
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        if(fragment != null) getSupportFragmentManager().beginTransaction()
                .remove(fragment).commit();
        if(passwordPreference.isAppLocked()){
            fragment = new LockedAppFragment();
        }else{
            fragment = new InfoFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_info_activity,fragment).commit();
    }

}
