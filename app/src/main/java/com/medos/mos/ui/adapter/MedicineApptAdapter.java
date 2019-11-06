package com.medos.mos.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.model.MedicalAppointment;
import com.medos.mos.model.MedicineAppointment;
import com.medos.mos.ui.medicalAppointment.MedicalAppointmentDetail;
import com.medos.mos.ui.medicineAppointment.MedicineAppointmentDetail;

import java.util.List;

public class MedicineApptAdapter extends RecyclerView.Adapter<MedicineApptAdapter.AppointmentViewHolder>{
    private static final String TAG = "MedicineBookAdapter";
    List<MedicineAppointment> mAppt;
    Context context;
    private final LayoutInflater mInflater;
    Utils util;

    public MedicineApptAdapter(List<MedicineAppointment> mAppt, Context context){
        this.mAppt = mAppt;
        this.context = context;
        mInflater = LayoutInflater.from(context);
        util = new Utils();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.med_appointment_item, parent, false);
        return new MedicineApptAdapter.AppointmentViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        if(mAppt!= null) {
            final MedicineAppointment appt = mAppt.get(position);
            holder.tvDate.setText(appt.getMedicineAppointmentDate());
            holder.tvHours.setText(appt.getMedicinrAppointmentBookingHours());
            holder.IVStatus.setImageResource(R.drawable.icon_medicine);

            holder.cardViewAppt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //navigate to appointment Details
                    Intent appointmentIntent = new Intent(context, MedicineAppointmentDetail.class);
                    appointmentIntent.putExtra("appointment", appt);
                    context.startActivity(appointmentIntent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mAppt.size();
    }

    public class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate, tvHours;
        private final ImageView IVStatus;
        private final CardView cardViewAppt;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate =itemView.findViewById(R.id.tvApptDate);
            tvHours = itemView.findViewById(R.id.tvApptHour);
            cardViewAppt = itemView.findViewById(R.id.cardViewAppt);
            IVStatus = itemView.findViewById(R.id.IVStatus);
        }
    }
}
