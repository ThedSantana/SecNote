package com.baraccasoftware.securenotes.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Note implements Parcelable {
	private int mId;
	private String mName;
	private String mDesc;
	private Date mDate;
	private byte[] mImage ;

	private DateFormat dateformat;
	private int intDateformat;
	
	public Note(int id, String name, String desc, Date date, byte[] image) {
		setmId(id);
		setDateFormat(2);
		setmName(name);
		setmDesc(desc);
		setmDate(date);		
		setmImage(image);
	}
	
	public Note(int id, String name, String desc, String date, byte[] image) throws ParseException {
		setmId(id);
		setDateFormat(2);
		setmName(name);
		setmDesc(desc);
		setmDate(date);
		setmImage(image);
	}
	
	public Note(Parcel parcel) throws ParseException{
		setDateFormat(parcel.readInt());
		setmId(parcel.readInt());
		setmName(parcel.readString());
		setmDesc(parcel.readString());
		setmDate(parcel.readString());
        int imageLen = parcel.readInt();
		mImage = new byte[imageLen];
		parcel.readByteArray(mImage);
		
	}
	

	public String getmName() {
		return mName;
	}

	public void setmName(String mName) {
		this.mName = mName;
	}

	public String getmDesc() {
		return mDesc;
	}

	public void setmDesc(String mDesc) {
		this.mDesc = mDesc;
	}

	public Date getmDate() {
		return mDate;
	}
	
	
	
	public String getmDataString(){
		return dateformat.format(mDate);
	}


	public void setmDate(Date mDate) {
		this.mDate = mDate;
	}
	
	public void setmDate(String data) throws ParseException{
		Date d = dateformat.parse(data);
		setmDate(d);
	}
	
	public void setDateFormat(int format){
		intDateformat = format;
		if(intDateformat==1){
			this.dateformat = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
		}else{
			this.dateformat = new SimpleDateFormat("dd/MM/yyyy",Locale.ITALIAN);
		}
	}

	public int getmId() {
		return mId;
	}

	public void setmId(int mId) {
		this.mId = mId;
	}
	
	public byte[] getmImage() {
		return mImage;
	}

	public void setmImage(byte[] mImage) {
		this.mImage = mImage;
	}
	

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flag) {
		parcel.writeInt(intDateformat);
		parcel.writeInt(mId);
		parcel.writeString(mName);
		parcel.writeString(mDesc);
		parcel.writeString(getmDataString());
		parcel.writeInt(mImage.length);
		parcel.writeByteArray(mImage);
	}
	
	

	



	public final static Creator CREATOR = new Creator() {
        @Override
        public Note createFromParcel(Parcel source) {
            try {
				return new Note(source);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return new Note(0,"None", "None", new Date(),new byte[1]);
			}
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };
	

}
