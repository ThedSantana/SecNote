package com.baraccasoftware.securenotes.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baraccasoftware.securenotes.object.ActivityUtilityInterface;
import com.baraccasoftware.securenotes.object.PasswordPreference;

/**
 * Created by angelo on 25/02/14.
 */
public class LockedAppFragment extends Fragment {
    private Activity mActivity;
    private EditText insertEDT;
    private Button unlockButton;
    private PasswordPreference passwordPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.locked_app_fragment,container,false);
        insertEDT = (EditText) rootview.findViewById(R.id.editText_password_locked_app);
        unlockButton = (Button) rootview.findViewById(R.id.button_unlock_app);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPassword();
            }
        });
        return rootview;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        passwordPreference = new PasswordPreference(mActivity.getApplicationContext());
    }

    private void checkPassword(){
        String in = insertEDT.getText().toString().trim();
        String password = passwordPreference.getPassword();
        if(password.equals(in)){
            //ok sblocco
            passwordPreference.setLockedApp(false);
            setActivityLayout();
        }else {
            insertEDT.setText("");
            Toast.makeText(mActivity.getApplicationContext(),R.string.esito_no,Toast.LENGTH_LONG).show();
        }
    }

    private void setActivityLayout(){
        if(mActivity instanceof ActivityUtilityInterface){
            ((ActivityUtilityInterface) mActivity).setLayout();
        }else{
            //Error
            log("ERRORE, Activity must be an ActivityUtilityInterface instance");
        }
    }

    public void log(String toLog){
        String TAG = "EXECUTION:LOCKEDUPFRAGMENT";
        Log.i(TAG,toLog);
    }
}
