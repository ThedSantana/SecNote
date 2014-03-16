package com.baraccasoftware.securenotes.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by angelo on 25/02/14.
 */
public class InfoFragment extends Fragment {

    TextView code;
    InfoActivity mActivity;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (InfoActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_info, container, false);

        code = (TextView) rootView.findViewById(R.id.textView_code);
        code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startBroserActivity(code.getText().toString());
            }
        });
        return rootView;
    }
    private void startBroserActivity(String url){
        String mUrl = "https://"+url;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
        startActivity(browserIntent);
    }
}






