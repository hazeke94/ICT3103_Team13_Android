package com.medos.mos.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MedicineAppointment implements Parcelable {
    String medicineAppointmentDate,medicineAppointmentNotes;
    String medicineAppointmentBookingHours;
    int medicineBookHourID;
    int medicineID;
    int summaryID;
    String status;

    public MedicineAppointment(String medicineAppointmentDate, String medicineAppointmentNotes, String medicineAppointmentBookingHours, int medicineBookHourID, int medicineID, int summaryID, String status) {
        this.medicineAppointmentDate = medicineAppointmentDate;
        this.medicineAppointmentNotes = medicineAppointmentNotes;
        this.medicineAppointmentBookingHours = medicineAppointmentBookingHours;
        this.medicineBookHourID = medicineBookHourID;
        this.medicineID = medicineID;
        this.summaryID = summaryID;
        this.status = status;
    }

    protected MedicineAppointment(Parcel in) {
        medicineAppointmentDate = in.readString();
        medicineAppointmentNotes = in.readString();
        medicineAppointmentBookingHours = in.readString();
        medicineBookHourID = in.readInt();
        status = in.readString();
        summaryID = in.readInt();
    }

    public static final Creator<MedicineAppointment> CREATOR = new Creator<MedicineAppointment>() {
        @Override
        public MedicineAppointment createFromParcel(Parcel in) {
            return new MedicineAppointment(in);
        }

        @Override
        public MedicineAppointment[] newArray(int size) {
            return new MedicineAppointment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(medicineAppointmentDate);
        parcel.writeString(medicineAppointmentNotes);
        parcel.writeString(medicineAppointmentBookingHours);
        parcel.writeInt(medicineBookHourID);
        parcel.writeString(status);
        parcel.writeInt(summaryID);
    }

    public String getMedicineAppointmentDate() {
        return medicineAppointmentDate;
    }

    public void setMedicineAppointmentDate(String medicineAppointmentDate) {
        this.medicineAppointmentDate = medicineAppointmentDate;
    }

    public String getMedicineAppointmentNotes() {
        return medicineAppointmentNotes;
    }

    public void setMedicineAppointmentNotes(String medicineAppointmentNotes) {
        this.medicineAppointmentNotes = medicineAppointmentNotes;
    }

    public String getMedicinrAppointmentBookingHours() {
        return medicineAppointmentBookingHours;
    }

    public void setMedicinrAppointmentBookingHours(String medicinrAppointmentBookingHours) {
        this.medicineAppointmentBookingHours  = medicinrAppointmentBookingHours;
    }

    public int getMedicineBookHourID() {
        return medicineBookHourID;
    }

    public void setMedicineBookHourID(int medicineBookHourID) {
        this.medicineBookHourID = medicineBookHourID;
    }

    public int getMedicineID() {
        return medicineID;
    }

    public void setMedicineID(int medicineID) {
        this.medicineID = medicineID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSummaryID() {
        return summaryID;
    }

    public void setSummaryID(int summaryID) {
        this.summaryID = summaryID;
    }
}
