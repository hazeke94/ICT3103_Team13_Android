package com.medos.mos.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MedicalAppointment implements Parcelable {
    String medicalAppointmentDate,medicalAppointmentNotes;
    String medicalAppointmentBookingHours;
    int medicalBookHourID;
    String status;

    public MedicalAppointment(String medicalAppointmentDate, String medicalAppointmentNotes, String medicalAppointmentBookingHours, int medicalBookHourID) {
        this.medicalAppointmentDate = medicalAppointmentDate;
        this.medicalAppointmentNotes = medicalAppointmentNotes;
        this.medicalAppointmentBookingHours = medicalAppointmentBookingHours;
        this.medicalBookHourID = medicalBookHourID;
        this.status = "";
    }

    protected MedicalAppointment(Parcel in) {
        medicalAppointmentDate = in.readString();
        medicalAppointmentNotes = in.readString();
        medicalAppointmentBookingHours = in.readString();
        medicalBookHourID = in.readInt();
        status = in.readString();
    }

    public static final Creator<MedicalAppointment> CREATOR = new Creator<MedicalAppointment>() {
        @Override
        public MedicalAppointment createFromParcel(Parcel in) {
            return new MedicalAppointment(in);
        }

        @Override
        public MedicalAppointment[] newArray(int size) {
            return new MedicalAppointment[size];
        }
    };

    public String getMedicalAppointmentDate() {
        return medicalAppointmentDate;
    }

    public void setMedicalAppointmentDate(String medicalAppointmentDate) {
        this.medicalAppointmentDate = medicalAppointmentDate;
    }

    public String getMedicalAppointmentNotes() {
        return medicalAppointmentNotes;
    }

    public void setMedicalAppointmentNotes(String medicalAppointmentNotes) {
        this.medicalAppointmentNotes = medicalAppointmentNotes;
    }

    public String getMedicalAppointmentBookingHours() {
        return medicalAppointmentBookingHours;
    }

    public void setMedicalAppointmentBookingHours(String medicalAppointmentBookingHours) {
        this.medicalAppointmentBookingHours = medicalAppointmentBookingHours;
    }

    public int getMedicalBookHourID() {
        return medicalBookHourID;
    }

    public void setMedicalBookHourID(int medicalBookHourID) {
        this.medicalBookHourID = medicalBookHourID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(medicalAppointmentDate);
        parcel.writeString(medicalAppointmentNotes);
        parcel.writeString(medicalAppointmentBookingHours);
        parcel.writeInt(medicalBookHourID);
        parcel.writeString(status);
    }

    @Override
    public String toString() {
        return "MedicalAppointment{" +
                "medicalAppointmentDate='" + medicalAppointmentDate + '\'' +
                ", medicalAppointmentNotes='" + medicalAppointmentNotes + '\'' +
                ", medicalAppointmentBookingHours='" + medicalAppointmentBookingHours + '\'' +
                ", medicalBookHourID=" + medicalBookHourID +
                ", status='" + status + '\'' +
                '}';
    }
}
