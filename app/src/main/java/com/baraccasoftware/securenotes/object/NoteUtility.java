package com.baraccasoftware.securenotes.object;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

/**
 * Created by angelo on 25/02/14.
 */
public class NoteUtility {

    /**
     * this method creates an encrypted Note to store into DB
     * @param toEncrypt
     * @return Encrypted note
     */
    public static Note encryptNote(Note toEncrypt, Context mContext){

        PBKDF2Encryptor encryptor = new PBKDF2Encryptor();

        PasswordPreference preference = new PasswordPreference(mContext);
        Encryptor.password = preference.getPassword();

        String nameEn = encryptor.encrypt(toEncrypt.getmName(),Encryptor.password);
        String descEn = encryptor.encrypt(toEncrypt.getmDesc(),Encryptor.password);

        Note encrypted;
        if(toEncrypt.getmImage().length>1){
            //criptiamo immagine
            String imgString = Base64.encodeToString(toEncrypt.getmImage(), Base64.DEFAULT);
            String enimgString = encryptor.encrypt(imgString,Encryptor.password);
            byte[] imgEn = enimgString.getBytes();

            //Note encrypted = new Note(toEncrypt.getmId(),nameEn,descEn,toEncrypt.getmDate(),
            //      toEncrypt.getImageLen(),toEncrypt.getmImage());
            encrypted = new Note(toEncrypt.getmId(),nameEn,descEn,toEncrypt.getmDate(),
                    imgEn);
        }else{
            encrypted = new Note(toEncrypt.getmId(),nameEn,descEn,toEncrypt.getmDate()
                    ,toEncrypt.getmImage());
        }
        return encrypted;
    }

    /**
     * this method creates a decrypted note loaded from db
     * @param toDecrypt
     * @return decrypted note
     */
    public static Note decryptNote(Note toDecrypt,Context mContext){
        PBKDF2Encryptor encryptor = new PBKDF2Encryptor();
        if(Encryptor.password == null){
            Log.d("FFFFFFFFFFFFF","PASSWORD NULL");
            PasswordPreference preference = new PasswordPreference(mContext);
            Encryptor.password = preference.getPassword();
            Log.d("PASSWORD IN PREFERENCE", Encryptor.password);
        }

        String nameEn = encryptor.decrypt(toDecrypt.getmName(), Encryptor.password);
        String descEn = encryptor.decrypt(toDecrypt.getmDesc(), Encryptor.password);

        Note decrypted;

        decrypted = new Note(toDecrypt.getmId(),nameEn,descEn,toDecrypt.getmDate(),
                toDecrypt.getmImage());
        //}
        return decrypted;
    }


    public static byte[] decryptImg(byte[] imgToDecrypt,Context mContext){
        PBKDF2Encryptor encryptor = new PBKDF2Encryptor();
        PasswordPreference preference = new PasswordPreference(mContext);
        Encryptor.password = preference.getPassword();
        byte[] imgEncryptedFromNote = imgToDecrypt;
        String enimgString = new String(imgEncryptedFromNote);
        String imgString = encryptor.decrypt(enimgString, Encryptor.password);
        return Base64.decode(imgString, Base64.DEFAULT);
    }
}
