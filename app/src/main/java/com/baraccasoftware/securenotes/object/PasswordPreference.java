package com.baraccasoftware.securenotes.object;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by angelo on 24/02/14.
 */
public class PasswordPreference {
    public static final String KEY_PREFS_PASSWORD = "password_key";
    public static final String KEY_PREFS_LOCKED_APP="lockedapp";
    public static final String KEY_PREFS_SETTED_PASSWORD = "setted_password";
    private static final String APP_SHARED_PREFS = "passwordpreference"; //  Name of the file -.xml
    private SharedPreferences _sharedPrefs;
    private SharedPreferences.Editor _prefsEditor;

    public PasswordPreference(Context context){
        this._sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this._prefsEditor = _sharedPrefs.edit();
    }

    public String getPassword(){
        return _sharedPrefs.getString(KEY_PREFS_PASSWORD,"");
    }

    public void savePassword(String password){
        _prefsEditor.putString(KEY_PREFS_PASSWORD,password);
        _prefsEditor.commit();
        setKeyPrefsSettedPassword(true);
    }

    public void setLockedApp(boolean locked){
        _prefsEditor.putBoolean(KEY_PREFS_LOCKED_APP,locked);
        _prefsEditor.commit();
    }

    public boolean isAppLocked(){
        return _sharedPrefs.getBoolean(KEY_PREFS_LOCKED_APP,true);
    }

    public void setKeyPrefsSettedPassword(boolean setted){
        _prefsEditor.putBoolean(KEY_PREFS_SETTED_PASSWORD,setted);
        _prefsEditor.commit();
    }

    public boolean isSettedPassword(){
        return _sharedPrefs.getBoolean(KEY_PREFS_SETTED_PASSWORD,false);
    }
}
