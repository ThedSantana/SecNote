package com.baraccasoftware.securenotes.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.baraccasoftware.securenotes.object.ActivityUtilityInterface;
import com.baraccasoftware.securenotes.object.PasswordPreference;

public class MainActivity extends Activity implements ActivityUtilityInterface{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setLayout();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public void setSameApp(boolean sameApp) {
        //nothing
    }

    @Override
    public boolean getSameApp() {
        return false;
    }

    @Override
    public void setLayout(){
        PasswordPreference passwordPreference = new PasswordPreference(getApplicationContext());
        if(passwordPreference.isSettedPassword()){
            log("login fragment");
            //password setted; open log in fragment
            getFragmentManager().beginTransaction().add(R.id.container_main_activity,new LogInFragment()).commit();
        }else {
            log("register fragment");
            //password not setted: open register fragment
            getFragmentManager().beginTransaction().add(R.id.container_main_activity,new RegisterFragment()).commit();
        }
    }

    public void startNoteActivity(){
        Intent intent = new Intent(this,NoteListActivity.class);
        startActivity(intent);
    }


    private void log(String toLog){

        String TAG = "MAIN ACTIVITY";
        Log.i(TAG, toLog);
    }

}
