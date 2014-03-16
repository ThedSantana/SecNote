package com.baraccasoftware.securenotes.app;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baraccasoftware.securenotes.object.PasswordPreference;

/**
 * Created by angelo on 24/02/14.
 * RegisterFragment
 * user must register new password to use app because this is first time
 */
public class RegisterFragment extends Fragment {
    TextView ins_password;
    TextView reins_password;
    Button enter;

    MainActivity mActivity;

    public RegisterFragment(){}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_fragment_layout,container,false);
        ins_password = (TextView) view.findViewById(R.id.editText_ins_password);
        reins_password = (TextView) view.findViewById(R.id.editText_reins_password);
        enter = (Button) view.findViewById(R.id.button_signin);
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPassword();
            }
        });
        return view;
    }

    private void checkPassword(){
        String pss1 = ins_password.getText().toString().trim();
        String pss2 = reins_password.getText().toString().trim();

        if(pss1.equals(pss2)){
            //password ok
            ins_password.setHint(R.string.esito_ok);
            ins_password.setHintTextColor(Color.GREEN);
            PasswordPreference passwordPreference = new PasswordPreference(mActivity.getApplicationContext());
            passwordPreference.savePassword(pss1);
            passwordPreference.setLockedApp(false);
            startNoteActivity();
        }else{
            //password differenti
            ins_password.setText("");
            ins_password.setHint(R.string.esito_no);
            ins_password.setHintTextColor(Color.RED);
            Toast.makeText(mActivity.getApplicationContext(), R.string.password_diverse,Toast.LENGTH_LONG).show();
        }
    }

    private void startNoteActivity(){
        //start note activity
        //Toast.makeText(mActivity.getApplicationContext(),"caricamento nuova activity",Toast.LENGTH_LONG).show(); //DEBUG
        mActivity.startNoteActivity();
    }
}
