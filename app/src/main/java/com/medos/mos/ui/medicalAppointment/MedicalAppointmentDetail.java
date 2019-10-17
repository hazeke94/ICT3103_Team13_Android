package com.medos.mos.ui.medicalAppointment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.medos.mos.R;
import com.medos.mos.model.MedicalAppointment;

public class MedicalAppointmentDetail extends AppCompatActivity {
    private String TAG = "MedicalAppointmentDetail";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_appointment_detail);
        MedicalAppointment appt = getIntent().getParcelableExtra("appointment");
        TextView tvDate,tvTime,tvStatus,tvNote;

        tvDate = findViewById(R.id.tvDateDetail);
        tvTime = findViewById(R.id.tvTimeDetail);
        tvStatus = findViewById(R.id.tvStatusDetail);
        tvNote = findViewById(R.id.tvNoteDetail);

        tvDate.setText(appt.getMedicalAppointmentDate());
        tvNote.setText(appt.getMedicalAppointmentNotes());
        tvStatus.setText(appt.getStatus());
        tvTime.setText(appt.getMedicalAppointmentBookingHours());
    }
}
