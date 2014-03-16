package com.baraccasoftware.securenotes.object;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;


public class DAO {
	private static DAO instance = null;
	final static String PASSWORDFILE = "passwordfile";
    public final static String FOLDER = "/SecureNotesDatabase/";
	 private SQLiteDatabase database;
	 private SecureDatabaseHelper dbHelper;
	 private String[] notes_columns = {SecureDatabaseHelper.ID_NOTE,SecureDatabaseHelper.TITLE_NOTE,
			 SecureDatabaseHelper.TEXT_NOTE,SecureDatabaseHelper.DATA_NOTE, SecureDatabaseHelper.IMG_NOTE}; 
	  
	private DAO(Context context) {
		dbHelper = new SecureDatabaseHelper(context);
	}
	
	public static DAO getInstance(Context context){
		if(instance == null) instance = new DAO(context);
		return instance;
	}
	
	public void openDB() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	  }

	public void closeDB() {
	    dbHelper.close();
	}
	
	public List<Note> getAllNotes(){
		List<Note> notes = new ArrayList<Note>();
		
		Cursor cursor = database.query(SecureDatabaseHelper.NOTES_TABLE_NAME, notes_columns, 
				null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Note nota = cursorToNote(cursor);
			notes.add(nota);
			cursor.moveToNext();
		}
		cursor.close();
		return notes;
	}
	
	public long addNoteToDB(Note note, boolean toReinsert){
		ContentValues value = new ContentValues();
		if(toReinsert) value.put(SecureDatabaseHelper.ID_NOTE, note.getmId());
		value.put(SecureDatabaseHelper.TITLE_NOTE, note.getmName());
		value.put(SecureDatabaseHelper.TEXT_NOTE, note.getmDesc());
		value.put(SecureDatabaseHelper.DATA_NOTE, note.getmDataString());
		value.put(SecureDatabaseHelper.IMG_NOTE, note.getmImage());
		return database.insert(SecureDatabaseHelper.NOTES_TABLE_NAME, null, value);
	}
	
	public int updateNoteToDB(Note note){
		ContentValues value = new ContentValues();
		value.put(SecureDatabaseHelper.TITLE_NOTE, note.getmName());
		value.put(SecureDatabaseHelper.TEXT_NOTE, note.getmDesc());
		value.put(SecureDatabaseHelper.DATA_NOTE, note.getmDataString());
		value.put(SecureDatabaseHelper.IMG_NOTE, note.getmImage());
		return database.update(SecureDatabaseHelper.NOTES_TABLE_NAME,
				value,"id = "+ note.getmId(),null);
	}
	
	public int removeFromDB(Note toRemove){
		return database.delete(SecureDatabaseHelper.NOTES_TABLE_NAME, "id = " + toRemove.getmId(), null);
	}







    public String exportDBCSV (Context context, String filename) throws IOException{
        //path into export db
        final String dirIntoExport = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+ FOLDER;

        File root = Environment.getExternalStorageDirectory();
        Log.d("DAO","External storage: "+root);
        File dir = new File(root.getAbsolutePath(),FOLDER);
        dir.mkdir();
        //file to store
        File fileToStore = new File(dir,filename+".csv");

        fileToStore.createNewFile();
        CSVWriter csvWrite = new CSVWriter(new FileWriter(fileToStore));
        List<Note> dataToExport = this.getAllNotes();
        String[] header = {SecureDatabaseHelper.ID_NOTE,SecureDatabaseHelper.TITLE_NOTE,
                SecureDatabaseHelper.TEXT_NOTE,SecureDatabaseHelper.DATA_NOTE,SecureDatabaseHelper.IMG_NOTE};
        csvWrite.writeNext(header);
        Log.d("DATAEXPORT: ",""+dataToExport.size());
        if(dataToExport.size()>=1){
            for(Note nota:dataToExport){
                String img;
                if(nota.getmImage().length<=1){
                    img = "nonono";
                }else {
                    img = new String(nota.getmImage());
                }
                String nome;
                if(nota.getmName()==null || nota.getmName().equals("") ){
                    nome = "nonono";
                }else{
                    nome = nota.getmName();
                }

                String desc;
                if(nota.getmDesc() == null || nota.getmDesc().equals("")){
                    desc = "nonono";
                }else{
                    desc = nota.getmDesc();
                }

                String[] ss = {""+nota.getmId(),nome,desc,
                nota.getmDataString(),img};
                csvWrite.writeNext(ss);
            }
        }
        csvWrite.close();

        return dir.toString();

    }

    public ArrayList<Note> importNotesFromFileCSV(Context context, String filename)throws IOException,ParseException{
        //path into import db
        final String dirIntoExport = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+ FOLDER;

        File root = Environment.getExternalStorageDirectory();
        Log.d("DAO","External storage: "+root);
        File dir = new File(root.getAbsolutePath(),FOLDER);
        dir.mkdir();
        File fileToImport = new File(dir,filename);
        CSVReader reader = new CSVReader(new FileReader(fileToImport));
        String [] nextLine;
        ArrayList<Note> dataToImport = new ArrayList<Note>();

        //read file rows
        while((nextLine = reader.readNext())!= null){

            String id = nextLine[0];
            String nome = nextLine[1];
            String desc = nextLine[2];
            String data = nextLine[3];
            String imgS = nextLine[4];
            byte[] img;
            if(imgS.equals("nonono")){
                img = new byte[1];
            }else{
                img = imgS.getBytes();
            }

            if (nome.equals("nonono")) nome = "";
            if (desc.equals("nonono")) desc = "";

            if(nome.equals(SecureDatabaseHelper.TITLE_NOTE)){
                //do nothing
            }else{

                dataToImport.add(new Note(Integer.parseInt(id),nome,desc,data,img));
            }
        }
        return dataToImport;
    }

    public static boolean isExternalStorageReadable(){
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        return mExternalStorageAvailable && mExternalStorageWriteable;
    }

    static private int fileLenght(FileInputStream fIn) throws IOException {
        int count = 0;
        while(fIn.read() != -1) count++;
        return count;
    }
	private Note cursorToNote(Cursor cursor){
		Note nota;
		try {
			nota = new Note(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),cursor.getBlob(4));
		} catch (ParseException e) {
			nota= new Note(0, "error", "error", new Date(),new byte[1]);
			e.printStackTrace();
		}
		return nota;
	}

}
