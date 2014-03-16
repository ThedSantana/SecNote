package com.baraccasoftware.securenotes.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

import com.baraccasoftware.securenotes.object.ActivityUtilityInterface;
import com.baraccasoftware.securenotes.object.PasswordPreference;

import java.io.File;
import java.io.IOException;

/**
 * An activity representing a single Note detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link NoteListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link NoteDetailFragment}.
 */
public class NoteDetailActivity extends FragmentActivity implements ActivityUtilityInterface {
    private Bundle argumentToFragment;
    private PasswordPreference passwordPreference;
    private SharedPreferences mSharedPreferences;
    private Fragment fragment;


    boolean sameApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);


        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        passwordPreference = new PasswordPreference(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setSameApp(false);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            argumentToFragment = getIntent().getExtras();
            setLayout();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            //NavUtils.navigateUpTo(this, new Intent(this, NoteListActivity.class));
            //NavUtils.n
            setSameApp(true);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        log("OnStart");

        super.onStart();
        setSameApp(false);
        if(passwordPreference.isAppLocked()){
            if(fragment != null) getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            fragment = new LockedAppFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commit();
        }
    }

    @Override
    protected void onStop() {
        log("OnStop");
        boolean toLock = mSharedPreferences.getBoolean("lockapponpause",true);
        log("ToLock: "+toLock);
        log("SameApp: "+getSameApp());
        if(toLock &&  !sameApp){
            passwordPreference.setLockedApp(true);
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        setSameApp(true);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        log("onConfigurationChanged");
    }

    @Override
    public void setLayout(){
        if(fragment != null) getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        if(passwordPreference.isAppLocked()){
            log("LockedApp");
            fragment = new LockedAppFragment();

        }else{
            fragment = new NoteDetailFragment();
            if(argumentToFragment != null) fragment.setArguments(argumentToFragment);

        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.note_detail_container, fragment)
                .commit();
    }

    @Override
    public void setSameApp(boolean sameApp) {
        this.sameApp = sameApp;
    }

    @Override
    public boolean getSameApp() {
        return this.sameApp;
    }



    public void log(String toLog){
        String TAG = "EXECUTION:NOTEDETAILACTIVITY";
        Log.i(TAG,toLog);
    }
}
