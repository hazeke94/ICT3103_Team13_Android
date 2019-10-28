package com.medos.mos.ui.profile;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.medos.mos.R;
public class profileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final Button btnProfile = root.findViewById(R.id.btnProfile);
        Button btnUpdateProfile = root.findViewById(R.id.btnUpdateProfile);
        Button btnCancelProfile = root.findViewById(R.id.btnCancelProfile);

        final LinearLayout editLayout = root.findViewById(R.id.EditLayout);

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //btnProfile
                btnProfile.setVisibility(View.GONE);
                //show edit layout
                editLayout.setVisibility(View.VISIBLE);
            }
        });

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnCancelProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //btnProfile
                btnProfile.setVisibility(View.VISIBLE);
                //show edit layout
                editLayout.setVisibility(View.GONE);
            }
        });
        return root;
    }

}
