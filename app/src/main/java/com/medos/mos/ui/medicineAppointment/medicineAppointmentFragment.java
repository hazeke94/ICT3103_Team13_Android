package com.medos.mos.ui.medicineAppointment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.medos.mos.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class medicineAppointmentFragment extends Fragment {


    public medicineAppointmentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_medicine_appointment, container, false);
        return root;
    }

}
