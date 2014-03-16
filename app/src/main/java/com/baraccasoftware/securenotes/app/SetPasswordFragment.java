package com.baraccasoftware.securenotes.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.baraccasoftware.securenotes.object.PasswordPreference;

/**
 * Created by angelo on 07/03/14.
 */
public class SetPasswordFragment extends DialogFragment {
    private EditText currentPassword;
    private EditText newPassword;
    private EditText confirmNewPassword;

    private Activity mActivity;
    private PasswordPreference passwordPreference;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder  = new AlertDialog.Builder(getActivity());
        LayoutInflater mInflater = getActivity().getLayoutInflater();
        View mView = mInflater.inflate(R.layout.password_alert_dialog_layout,null);

        passwordPreference = new PasswordPreference(mActivity.getApplicationContext());

        currentPassword = (EditText) mView.findViewById(R.id.editText_pass1_alertdialog_password);
        newPassword = (EditText) mView.findViewById(R.id.editText_pass2_alertdialog_password);
        confirmNewPassword = (EditText) mView.findViewById(R.id.editText_pass3_alertdialog_password);

        currentPassword.setTypeface(Typeface.DEFAULT);
        newPassword.setTypeface(Typeface.DEFAULT);
        confirmNewPassword.setTypeface(Typeface.DEFAULT);

        builder.setTitle(R.string.titolo_alert_dialog_mod_password)
                .setCancelable(true)
                .setPositiveButton(R.string.ok_button,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        savePassword();
                    }
                })
                .setNegativeButton(R.string.annulla_button,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //nothing
                    }
                })
                .setView(mView);

        return  builder.create();
    }

    private void savePassword(){
        if(passwordPreference.getPassword().equals(currentPassword.getText().toString().trim())){
            //ok current password checked
            String newp = newPassword.getText().toString().trim();
            String confnewp = confirmNewPassword.getText().toString().trim();
            if(newp.equals(confnewp)){
                //ok we can save new password
                Intent intent = new Intent(NoteListActivity.INTENT_REFRESH_NOTE);
                //send new password to asynctask to refresh db
                intent.putExtra(NoteListActivity.REFRESH_NOTE_TAG,newp);
                mActivity.sendBroadcast(intent);
                Toast.makeText(mActivity.getApplicationContext(),R.string.password_impostata,Toast.LENGTH_LONG).show();
            }else{
                //no, we can't save it
                Toast.makeText(mActivity.getApplicationContext(),R.string.password_diverse,Toast.LENGTH_LONG).show();
            }
        }else{
            //wrong current password
            Toast.makeText(mActivity.getApplicationContext(),R.string.password_diverse,Toast.LENGTH_LONG).show();
        }
    }
}
