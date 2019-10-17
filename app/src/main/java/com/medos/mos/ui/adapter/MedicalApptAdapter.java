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
import com.medos.mos.ui.medicalAppointment.MedicalAppointmentDetail;

import java.util.List;

public class MedicalApptAdapter extends RecyclerView.Adapter<MedicalApptAdapter.AppointmentViewHolder> {
    private static final String TAG = "MedicalBookAdapter";
    List<MedicalAppointment> mAppt;
    Context context;
    private final LayoutInflater mInflater;
    Utils util;

    public MedicalApptAdapter(List<MedicalAppointment> mAppt, Context context){
        this.mAppt = mAppt;
        this.context = context;
        mInflater = LayoutInflater.from(context);
        util = new Utils();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.med_appointment_item, parent, false);
        return new MedicalApptAdapter.AppointmentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        if(mAppt!= null) {
            final MedicalAppointment appt = mAppt.get(position);
            holder.tvDate.setText(appt.getMedicalAppointmentDate());
            holder.tvHours.setText(appt.getMedicalAppointmentBookingHours());
            if(appt.getStatus().equals("1")){
                holder.IVStatus.setImageResource(R.drawable.icon_pending);
            }
            else{
                holder.IVStatus.setImageResource(R.drawable.icon_pending);
            }
            holder.cardViewAppt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //navigate to appointment Details
                    Intent appointmentIntent = new Intent(context, MedicalAppointmentDetail.class);
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

    public void setItems(List<MedicalAppointment> appt) {
        this.mAppt = appt;
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
