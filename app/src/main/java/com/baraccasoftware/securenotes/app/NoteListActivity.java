package com.baraccasoftware.securenotes.app;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.baraccasoftware.securenotes.object.ActivityUtilityInterface;
import com.baraccasoftware.securenotes.object.DAO;
import com.baraccasoftware.securenotes.object.Note;
import com.baraccasoftware.securenotes.object.NoteUtility;
import com.baraccasoftware.securenotes.object.PasswordPreference;
import com.baraccasoftware.securenotes.widget.UndoBarController;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;


/**
 * An activity representing a list of Notes. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link NoteDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link NoteListFragment} and the item details
 * (if present) is a {@link NoteDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link NoteListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class NoteListActivity extends FragmentActivity
        implements NoteListFragment.Callbacks, ActivityUtilityInterface{

    public static final int REQUEST_ADD_NEW_NOTE_CODE = 555;
    public static final int REQUEST_UPDATE_NEW_NOTE_CODE = 556;

    public static final String TAG_NO_LOCKED_APP = "notlockedapp";
    public static final String INTENT_REFRESH_NOTE = "com.baraccasoftare.securenotes.NOTE_REFRESHED";
    public static final String REFRESH_NOTE_TAG = "refreshNote";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private int mItemPosition;

    private boolean sameApp;

    //fragment fisso
    Fragment mFragment;
    Fragment secondFragment;

    //preferencePassword
    private PasswordPreference passwordPreference;
    private SharedPreferences mSharedPreferences;

    ProgressDialog progressDialog;
    Context mContext = this;
    DAO dao = DAO.getInstance(this);


    BroadcastReceiver mReceiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        passwordPreference = new PasswordPreference(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setSameApp(false);
        log("OnCreate");



        if (findViewById(R.id.note_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            /*((NoteListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.note_list))
                    .setActivateOnItemClick(true);*/
        }
        setLayout();

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String toRefreshNote = intent.getStringExtra(REFRESH_NOTE_TAG);
                log(toRefreshNote);
                if(toRefreshNote != null){
                    //refresh note
                    //TODO - ricordare
                    log("Intent received - Broadcast Receiver");
                    RefreshNoteTask task = new RefreshNoteTask(toRefreshNote);
                    task.execute();
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            Bundle bundle = data.getExtras();
            Note note =  bundle.getParcelable(NoteDetailFragment.ARG_ITEM);
            if(requestCode == REQUEST_ADD_NEW_NOTE_CODE){
                //nuova note

               saveNote(note);
            }else {
                //nota da aggiornare
                updateNote(note);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            setSameApp(true);
            return true;
        }else if(id == R.id.action_add){
            showDetailNote(null);
            setSameApp(true);
            return true;
        }else if(id == R.id.action_export){
            if(DAO.getInstance(this).isExternalStorageReadable()){
                //export notes
                //new ExportNotesTask().execute();
                ExportDialogFragment frgm = new ExportDialogFragment();
                frgm.show(getFragmentManager(),"export notes");
                return true;
            }else{
                Toast.makeText(getApplicationContext(),R.string.error_export_db,Toast.LENGTH_LONG).show();
            }
            return false;
        }else if(id == R.id.action_import){
            if(DAO.getInstance(this).isExternalStorageReadable()){
                //export notes
                ImportDialogFragment fragment = new ImportDialogFragment();
                fragment.show(getFragmentManager(),"import notes");
                return true;
            }else{
                Toast.makeText(getApplicationContext(),R.string.error_export_db,Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        log("OnStart");
        setSameApp(false);
        if(passwordPreference.isAppLocked()){
            if(mFragment != null) getSupportFragmentManager().beginTransaction().remove(mFragment).commit();
            //app bloccata
            if(this.mTwoPane){
                //primo fragment vuoto, secondo lockedAppFragment
                secondFragment = new LockedAppFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.note_detail_container,secondFragment).commit();
                log("two panes and locked");
            }else{
                //primo lockedAppFragment
                mFragment = new LockedAppFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.note_list,mFragment).commit();

            }
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        log("OnRestart");

    }

    @Override
    protected void onStop() {
        log("OnStop");
        boolean toLock = mSharedPreferences.getBoolean("lockapponpause",true);
        log("ToLock: "+toLock);
        log("SameApp: "+getSameApp());
        if(toLock &&  !sameApp){
            passwordPreference.setLockedApp(true);
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        log("onResume");
        registerReceiver(mReceiver,new IntentFilter(INTENT_REFRESH_NOTE));

        if(mTwoPane && !passwordPreference.isAppLocked()) ((NoteListFragment)mFragment).setActivateOnItemClick(true);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log("onDestroy");
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        log("onSavedInstanceState");
        if(mTwoPane){
            //different situation
            if(secondFragment instanceof NoteDetailFragment){
                outState.putBoolean(TAG_NO_LOCKED_APP,true);
                log("not to lock");
            }
        }else{
            if(mFragment instanceof NoteListFragment){
                outState.putBoolean(TAG_NO_LOCKED_APP,true);
                log("not to lock");
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        log("onRestoreInstanceState");
        if(savedInstanceState != null){
            boolean b = savedInstanceState.getBoolean(TAG_NO_LOCKED_APP);
            if(b){
                passwordPreference.setLockedApp(false);
            }

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        log("onConfigurationChanged");
    }

    /**
     * Callback method from {@link NoteListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(Note note,int position) {
        showDetailNote(note);
        mItemPosition = position;
    }

    @Override
    public void saveNote(Note note){
        //if (mFragment instanceof NoteListFragment) ((NoteListFragment)mFragment).addNote(note);
        StoreNoteToDBTask task = new StoreNoteToDBTask(note);
        task.execute(false);
    }
    @Override
    public void updateNote(Note note){
        //if(mFragment instanceof NoteListFragment) ((NoteListFragment)mFragment).updateNote(note,mItemPosition);
        UpdateNoteToDBTask task = new UpdateNoteToDBTask(note);
        task.execute();
    }
    @Override
    public void deleteNote(Note note, int position){
        DeleteNoteFromDB task = new DeleteNoteFromDB(note);
        task.execute();
        mItemPosition = position;


        if(mTwoPane && secondFragment != null && note.getmId() == ((NoteDetailFragment)secondFragment).getIdNota() ){
            log("to reload fragment");
            getSupportFragmentManager().beginTransaction().remove(secondFragment).commit();
            secondFragment = new NoteDetailFragment();
            secondFragment.setArguments(new Bundle());
            getSupportFragmentManager().beginTransaction().replace(R.id.note_detail_container, secondFragment).commit();
        }
    }

    @Override
    public void setSameApp(boolean sameApp) {
        this.sameApp = sameApp;
    }

    @Override
    public boolean getSameApp() {
        return this.sameApp;
    }

    @Override
    public void setLayout() {
        if(passwordPreference.isAppLocked()){
            if(mFragment != null) getSupportFragmentManager().beginTransaction().remove(mFragment).commit();
            //app bloccata
            if(this.mTwoPane){
                //primo fragment vuoto, secondo lockedAppFragment
                secondFragment = new LockedAppFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.note_detail_container,secondFragment).commit();
            }else{
                //primo lockedAppFragment
                mFragment = new LockedAppFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.note_list,mFragment).commit();

            }
        }else{
            //app non bloccata
            //primo fragment normale
            if(mFragment != null) getSupportFragmentManager().beginTransaction().remove(mFragment).commit();
            if(secondFragment != null) getSupportFragmentManager().beginTransaction().remove(secondFragment).commit();
            mFragment = new NoteListFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.note_list,mFragment).commit();

        }

    }






    public void loadNoteFromDB(){
        NoteLoaderTask task = new NoteLoaderTask();
        task.execute();
    }

    public void exportNotes(String filename){
        ExportNotesTask task = new ExportNotesTask(filename);
        task.execute();
    }

    public void importNotes(String filename){
        ImportNotesTask task = new ImportNotesTask(filename);
        task.execute();
    }

    private void showDetailNote(Note note){
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle bun = new Bundle();
            secondFragment = new NoteDetailFragment();
            if(note != null){
                bun.putParcelable(NoteDetailFragment.ARG_ITEM, note);
                bun.putBoolean(NoteDetailFragment.NOTE_TO_MOD,true);
                bun.putInt(NoteDetailFragment.POSITION_ID,mItemPosition);
            }
            secondFragment.setArguments(bun);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.note_detail_container, secondFragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            setSameApp(true);
            Intent detailIntent = new Intent(this, NoteDetailActivity.class);
            Bundle bun = new Bundle();
            if(note != null){
                bun.putParcelable(NoteDetailFragment.ARG_ITEM,note);
                detailIntent.putExtras(bun);
                startActivityForResult(detailIntent, REQUEST_UPDATE_NEW_NOTE_CODE);
            }else{
                detailIntent.putExtras(bun);
                startActivityForResult(detailIntent,REQUEST_ADD_NEW_NOTE_CODE);
            }

        }
    }

    public void log(String toLog){
        String TAG = "EXECUTION:NOTELISTACTIVITY";
        Log.d(TAG,toLog);
    }

    /**
     * this method set prograss dialog
     */
    private void setProgressDialog(String message){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
    }



    private class StoreNoteToDBTask  extends AsyncTask<Boolean,Void,Boolean> {
        Note mNote;
        boolean isToRestore;

        public StoreNoteToDBTask(Note n){
            mNote = n;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Boolean doInBackground(Boolean... voids) {
            int jint ;
            dao.openDB();
            int i = 0;
            isToRestore = voids[0];
            while(i<3){
                if((jint = (int) dao.addNoteToDB(NoteUtility.encryptNote(mNote,mContext), isToRestore))!=-1){
                    mNote.setmId(jint);
                    dao.closeDB();
                    return true;
                }else{
                    i++;
                }
            }
            dao.closeDB();
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(!aBoolean){
                Toast.makeText(mContext, R.string.error_write_db, Toast.LENGTH_LONG).show();
            }else{
                try {
                    if(isToRestore){
                        ((NoteListFragment)mFragment).addNote(mNote,mItemPosition);
                    }else{
                        ((NoteListFragment)mFragment).addNote(mNote);
                    }
                    if(mTwoPane) ((NoteDetailFragment)secondFragment).setIdNota(mNote.getmId());
                }catch (NullPointerException ex){

                }
            }

        }

    }

    private class UpdateNoteToDBTask  extends AsyncTask<Void,Void,Boolean>{
        Note note;

        public UpdateNoteToDBTask(Note n){
            note = n;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            dao.openDB();
            Note en = NoteUtility.encryptNote(note, mContext);
            int i = 0;
            while(i<3){

                int d = dao.updateNoteToDB(en);

                if(d==0){
                    i++;

                }else{
                    return true;
                }
            }

            dao.closeDB();
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(!aBoolean){
                Toast.makeText(mContext, R.string.error_update_db, Toast.LENGTH_LONG).show();
            }else{
                try{
                    ((NoteListFragment)mFragment).updateNote(note,mItemPosition);
                }catch (NullPointerException ex){}
            }
        }


    }

    private class DeleteNoteFromDB extends AsyncTask<Void,Void,Boolean>{
        Note toRemove;

        public DeleteNoteFromDB(Note toRemove){
            this.toRemove = toRemove;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            dao.openDB();
            dao.removeFromDB(toRemove);
            dao.closeDB();
            return null;
        }


    }


    private class NoteLoaderTask extends AsyncTask<Void,Void,Boolean>{
        ArrayList<Note> mData;

        public NoteLoaderTask(){
            mData = new ArrayList<Note>();
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            setProgressDialog(mContext.getResources().getString(R.string.loading_notes));
            progressDialog.show();
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            dao.openDB();
            Log.d("LOAD NOTE", "Stiamo caricando le note");
            ArrayList<Note> fromDB = (ArrayList<Note>) dao.getAllNotes();
            if(fromDB.size()!=0){
                for(Note note:dao.getAllNotes()){
                    mData.add(NoteUtility.decryptNote(note, mContext));
                }
            }
            dao.closeDB();
            return (mData.size() == 0)? false:true;

        }

        @Override
        protected void onPostExecute(Boolean esito){
            progressDialog.dismiss();
            if(esito){
                //aggiorno listview
                Collections.reverse(mData);
                if(mFragment instanceof NoteListFragment) ((NoteListFragment)mFragment).addAllNote(mData);
            }else{
                //mostro toast di info
            }


        }
    }


    private class ExportNotesTask extends AsyncTask<Void,Void,Boolean>{

        String filename;
        String path = "";


        public ExportNotesTask(String filename){
            this.filename = filename;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgressDialog(mContext.getResources().getString(R.string.exporting_notes));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean esito;
            dao.openDB();
            try{
                path = dao.exportDBCSV(mContext,filename);
                esito = true;
            }catch (IOException ex){
                ex.printStackTrace();
                esito = false;
            }finally {
                dao.closeDB();
            }
            return esito;
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            if(aVoid){
                //Toast ok
                Toast.makeText(mContext,getResources().getString(R.string.generated_file)+path+"/"+filename+".csv",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(mContext,R.string.error_write_db,Toast.LENGTH_LONG).show();
            }
        }
    }


    private class ImportNotesTask extends AsyncTask<Void,Void,Boolean>{

        boolean esito;
        String filename;
        ArrayList<Note> mData;

        public ImportNotesTask(String filename){
            this.filename = filename;
            mData = new ArrayList<Note>();
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgressDialog(mContext.getResources().getString(R.string.import_notes));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                ArrayList<Note> data = dao.importNotesFromFileCSV(mContext,filename);
                for(Note note:data){
                    int jint ;
                    dao.openDB();
                    Note newNote = NoteUtility.decryptNote(note, mContext);
                    if((jint = (int) dao.addNoteToDB(note, false))!=-1){

                        newNote.setmId(jint);
                        mData.add(newNote);
                        esito = true;
                    }else{Log.d("ERRORE IMPORT", "-1 id");
                        esito = false;
                    }
                    dao.closeDB();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                esito = false;
            }  catch (ParseException e) {
                e.printStackTrace();
                esito = false;
            } catch (IOException e) {
                e.printStackTrace();
                esito = false;
            } catch (RuntimeException e){
                e.printStackTrace();
                esito = false;
            }
            return esito;
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            Collections.reverse(mData);
            if(aVoid){
                ((NoteListFragment)mFragment).addAllNote(mData);

                Toast.makeText(mContext,getString(R.string.import_ok_esito)+filename,Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(mContext, getString(R.string.error_import_note), Toast.LENGTH_LONG).show();
            }
        }
    }


    private class RefreshNoteTask extends AsyncTask<Void,Integer,Boolean>{

        NotificationCompat.Builder mBuilder;
        NotificationManager mNotifyManager;
        private final static int _ID_NOTIFICATION = 0;

        String newPassword;
        PasswordPreference passwordPreference;

        public RefreshNoteTask(String newPassword){
            this.newPassword = newPassword;
            passwordPreference = new PasswordPreference(getApplicationContext());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mNotifyManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(getApplicationContext());
            mBuilder.setContentTitle(getString(R.string.refresh_note))
                    .setContentText(getString(R.string.refreshing_note))
                    .setSmallIcon(android.R.drawable.ic_menu_upload);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            dao.openDB();

            //get all note from db
            ArrayList<Note> notes = (ArrayList) dao.getAllNotes();
            ArrayList<Note> notesDe = new ArrayList<Note>();

            //delete note from db
            for(Note n:notes){
                dao.removeFromDB(n);
                publishProgress(0,0);
            }
            this.log("Removed all notes");


            //foreach note decrypt it and re-encrypt it whit new password
            for(int i = 0;i<notes.size();i++){
                // into for loop -- decrypt text
                Note note = NoteUtility.decryptNote(notes.get(i),getApplicationContext());
                // into for loop -- decrypt img
                if( note.getmImage().length>1){
                    byte[] img = NoteUtility.decryptImg(note.getmImage(),getApplicationContext());
                    note.setmImage(img);
                }
                notesDe.add(note);
                publishProgress(0,0);
            }

            this.log("decrypted all notes");
            notes = null;
            passwordPreference.savePassword(newPassword);

            publishProgress(0, notesDe.size());
            for (int i = 0;i< notesDe.size();i++ ){
                Note note = notesDe.get(i);
                // into for loop -- encrypt text
                Note toReinsert = NoteUtility.encryptNote(note,getApplicationContext());


                //insert note with old id
                dao.addNoteToDB(toReinsert,true);
                publishProgress(i+1,notesDe.size());
            }
            this.log("saved all notes");



            dao.closeDB();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {//########################################################################
            super.onProgressUpdate(values);
            int level = values[0];
            int max = values[1];

            // Sets an activity indicator for an operation of indeterminate length
            mBuilder.setProgress(max, level, true);
// Issues the notification
            mNotifyManager.notify(_ID_NOTIFICATION, mBuilder.build());
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            // When the loop is finished, updates the notification
            mBuilder.setContentText(getString(R.string.refresh_completed))
                    // Removes the progress bar
                    .setProgress(0,0,false);
            mNotifyManager.notify(_ID_NOTIFICATION, mBuilder.build());
        }


        private void log (String toLog){
            Log.i("REFRESH NOTE",toLog);
        }
    }
}
