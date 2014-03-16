package com.baraccasoftware.securenotes.object;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by angelo on 28/02/14.
 */
public class BitmapUtility {

    public static Bitmap decodeFile(String path){
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inSampleSize = 2;
        return BitmapFactory.decodeFile(path,op);
    }

    public static  byte[] compressBitmap(Bitmap toCompress, int quality) throws Exception{
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] byteC;

            //qualità della foto 45 --- 50
            toCompress.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            byteC = stream.toByteArray();


        return byteC;
    }
}
