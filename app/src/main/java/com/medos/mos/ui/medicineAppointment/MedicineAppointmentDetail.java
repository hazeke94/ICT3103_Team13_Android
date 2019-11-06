package com.medos.mos.ui.medicineAppointment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.model.MedicalAppointment;
import com.medos.mos.model.MedicineAppointment;

public class MedicineAppointmentDetail extends AppCompatActivity {
    private String TAG = "MedicineAppointmentDetail";
    SharedPreferences pref;
    Utils util;
    MedicineAppointment appt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_appointment_detail);

        appt = getIntent().getParcelableExtra("appointment");
        TextView tvDate, tvTime, tvNote, tvStatus;

        pref = this.getSharedPreferences("Session", 0); // 0 - for private mode
        util = new Utils();

        tvDate = findViewById(R.id.tvDateDetail);
        tvTime = findViewById(R.id.tvTimeDetail);
        tvNote = findViewById(R.id.tvNoteDetail);
        tvStatus = findViewById(R.id.tvStatusDetail);

        tvDate.setText(appt.getMedicineAppointmentDate());
        tvNote.setText(appt.getMedicineAppointmentNotes());
        tvStatus.setText("Collection of Medication");
        tvTime.setText(appt.getMedicinrAppointmentBookingHours());
    }
}
