package com.baraccasoftware.securenotes.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.baraccasoftware.securenotes.object.ActivityUtilityInterface;
import com.baraccasoftware.securenotes.object.BitmapUtility;
import com.baraccasoftware.securenotes.object.Note;
import com.baraccasoftware.securenotes.object.NoteUtility;
import com.baraccasoftware.securenotes.widget.SlidingDrawer;
import com.baraccasoftware.securenotes.widget.UndoBarController;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * A fragment representing a single Note detail screen.
 * This fragment is either contained in a {@link NoteListActivity}
 * in two-pane mode (on tablets) or a {@link NoteDetailActivity}
 * on handsets.
 */
public class NoteDetailFragment extends Fragment implements UndoBarController.UndoListener {

    public final static int CAMERA_PIC_REQUEST = 557;
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM = "item_note";
    public static final String NOTE_TO_MOD = "note_to_mod";
    public static final String POSITION_ID = "item_position_id";
    public static final String IMG_COMPRESSED = "img_compressed";


    private Note mItem;
    private boolean toModifyNote;
    private EditText mTitle;
    private EditText mText;
    private TextView mDate;
    private ImageView mImageView;
    private SlidingDrawer mSlidingDrawer;
    private ImageButton deleteImage;

    private UndoBarController muUndoBarController;

    boolean isTextLayoutVisible;

    private int idNota;
    private byte[] imgCompressed;
    private int notePosition;

    private String pathFile;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NoteDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_ITEM)) {

           mItem =  getArguments().getParcelable(ARG_ITEM);
           idNota = mItem.getmId();
            log("ID NOTE LOADED: "+ idNota);

        }

        if(getArguments().containsKey(NOTE_TO_MOD)){
            toModifyNote = getArguments().getBoolean(NOTE_TO_MOD);
        }

        if(getArguments().containsKey(POSITION_ID)){
            notePosition = getArguments().getInt(POSITION_ID);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_note_detail, container, false);
        isTextLayoutVisible = true;
        mTitle = (EditText) rootView.findViewById(R.id.editText_titolo_addnote);
        mText = (EditText) rootView.findViewById(R.id.editText_text_addnote);
        mDate = (TextView) rootView.findViewById(R.id.textView_data_addnote);
        mImageView = (ImageView) rootView.findViewById(R.id.imageView1_addnote);
        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            mTitle.setText(mItem.getmName());
            mText.setText(mItem.getmDesc());
            mDate.setText(getDateLastModification(mItem.getmDate(),mItem.getmDataString()));
            if(mItem.getmImage().length>1){
                LoadPicTask task = new LoadPicTask(null,mItem);
                task.execute();
            }
        }

        muUndoBarController = new UndoBarController(rootView.findViewById(R.id.undobar_detail_activity),this);




        deleteImage = (ImageButton) rootView.findViewById(R.id.imageButton_delete_img);
        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imgCompressed != null && imgCompressed.length>1){
                    mImageView.setImageBitmap(null);
                    //mostro undobar
                    Intent intent = new Intent();
                    intent.putExtra(IMG_COMPRESSED,imgCompressed);
                    imgCompressed = new byte[1];
                    muUndoBarController.showUndoBar(false,getString(R.string.img_deleted),intent);
                }
            }
        });
        deleteImage.setVisibility(View.GONE);

        mSlidingDrawer = (SlidingDrawer) rootView.findViewById(R.id.sliding);
        mSlidingDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                deleteImage.setVisibility(View.VISIBLE);
            }
        });
        mSlidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                deleteImage.setVisibility(View.GONE);
            }
        });
        mSlidingDrawer.open();







        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem addPhotoMenuItem = menu.add("camera");
        addPhotoMenuItem.setIcon(android.R.drawable.ic_menu_camera);
        addPhotoMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        MenuItem saveMenuItem = menu.add("save");
        saveMenuItem.setIcon(R.drawable.ic_save_accept);
        saveMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String title =  item.getTitle().toString();
        if(title.equals("save")  ){
            //save new Note
            saveNote();
        }else if(title.equals("camera")){
            ((ActivityUtilityInterface)getActivity()).setSameApp(true);
            startCameraActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setImage(Bitmap bitmap,byte[] imgCompressed){
        mImageView.setImageBitmap(bitmap);
        this.imgCompressed = imgCompressed;

    }

    /**
     * this method return note position in mAdapter
     * position id different than idNota, because position is relative to mAdapter
     * and idNota is relative to Database
     * @return position
     */
    public int getNotePosition(){
        return notePosition;
    }

    /**
     * this method return id of current note displayed
     * @return id
     */
    public int getIdNota(){ return  idNota; }

    /**
     * this method set id note. it's important when we have two pane and we
     * save new note. We, in fact, must update id of the current note
     * @param idNota
     */
    public void setIdNota(int idNota){ this.idNota = idNota; }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_PIC_REQUEST && resultCode == getActivity().RESULT_OK){
            //ok there is pic

            LoadPicTask task = new LoadPicTask(pathFile,null);
            task.execute();
            //BitmapFactory.Options op = new BitmapFactory.Options();
            //op.inSampleSize = 2;
            //setImage(BitmapFactory.decodeFile(pathFile,op),null);
        }
    }

    @Override
    public void onUndo(Parcelable token) {
        Intent intent = (Intent) token;
        byte[] img = intent.getByteArrayExtra(IMG_COMPRESSED);
        Bitmap bitmap = BitmapFactory.decodeByteArray(img,0,img.length);
        setImage(bitmap,img);
    }

    /**
     * method to save new note or modified note
     */
    private void saveNote(){
        String title = mTitle.getText().toString().trim();
        String text = mText.getText().toString().trim();
        Note note = null;
        if(imgCompressed == null) imgCompressed = new byte[1];
        if(!(title.equals("") && text.equals("") && imgCompressed.length==1)){
            note = new Note(0,title,text,new Date(),new byte[1]);
            if( title.equals(mItem.getmName()) && text.equals(mItem.getmDesc()) && imgCompressed.length == mItem.getmImage().length){
                note.setmDate(mItem.getmDate());
            }
        }

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_ITEM,note);
        intent.putExtras(bundle);
        if(getActivity() instanceof NoteDetailActivity){
            //handset device
            int result;
            if(note != null){
                note.setmId(idNota);
                note.setmImage(imgCompressed);
                result = ((NoteDetailActivity) getActivity()).RESULT_OK;
            }else{
                result = ((NoteDetailActivity) getActivity()).RESULT_CANCELED;
            }

            ((NoteDetailActivity) getActivity()).setSameApp(true);
            getActivity().setResult(result,intent);
            getActivity().finish();
        }else{
            //other --  tablet device
            if(note != null){
                if(toModifyNote){
                    note.setmId(idNota);
                    note.setmImage(imgCompressed);
                    ((NoteListActivity)getActivity()).updateNote(note);
                }else{
                    toModifyNote = true;
                    ((NoteListActivity)getActivity()).saveNote(note);
                }
            }
        }
    }

    /**
     * method to start camera activity to get photo
     */
    public void startCameraActivity(){
        final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SecureNotesPicFolder/";
        File newdir = new File(dir);
        newdir.mkdirs();
        pathFile = dir + "file.jpg";

        File newfile = new File(pathFile);
        try {
            newfile.createNewFile();
        } catch (IOException e) {
            Log.e("FILE CREATES", "errore");
        }

        Uri outputFileUri = Uri.fromFile(newfile);


        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cameraIntent, NoteDetailFragment.CAMERA_PIC_REQUEST);
    }

    /**
     * thi method return a animation top to bottom
     * @param first
     * @param second
     * @return
     */
    private TranslateAnimation getAnimation(float first,float second){
        TranslateAnimation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, first, Animation.RELATIVE_TO_SELF, second
        );
        animation.setDuration(500);
        return animation;
    }

    private String getDateLastModification(Date date, String dateString){
        String toReturn = getString(R.string.element_data_modified)+": ";
        String scurrentDate = DateFormat.getDateInstance().format(new Date());

        Date current = new Date();
        //diifference in day
        long diff = (current.getTime() - date.getTime())/(24 * 60 * 60 * 1000);
        if(diff < 1){
            toReturn += getString(R.string.today_mod);
        }else if(diff<2){
            toReturn+= getString(R.string.yesterday_mod);
        }else{
            toReturn+=dateString;
        }

        return toReturn;
    }



    private void log(String toLog){
        String TAG = "EXECUTION: NOTE DETAIL FRAGMENT";
        Log.i(TAG,toLog);
    }




    public class LoadPicTask extends AsyncTask<Void,Void,Boolean> {


        Bitmap bitmap;
        byte[] arrayPic;
        String filePath;
        Note fromFile;

        public  LoadPicTask(String filePath, Note fromFile){

            this.filePath = filePath;
            this.fromFile = fromFile;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(fromFile == null){
                bitmap = BitmapUtility.decodeFile(filePath);
                try{
                    arrayPic = BitmapUtility.compressBitmap(bitmap,45);
                }catch (Exception e){
                    arrayPic = new byte[1];
                    return false;
                }
            }else {
                //from note
                try{

                    arrayPic = NoteUtility.decryptImg(fromFile.getmImage(),getActivity().getApplicationContext());

                }catch (Exception e){
                    //nothing
                    arrayPic = fromFile.getmImage();
                }finally {
                    bitmap = BitmapFactory.decodeByteArray(arrayPic,0,arrayPic.length);
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean){
                setImage(bitmap, arrayPic);
            }else{
                //
                setImage(bitmap,null);
            }
        }
    }


}
