package com.baraccasoftware.securenotes.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.baraccasoftware.securenotes.object.Note;
import com.baraccasoftware.securenotes.widget.NoteAdapter;
import com.baraccasoftware.securenotes.widget.SwipeDismissListViewTouchListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A list fragment representing a list of Notes. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link NoteDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class NoteListFragment extends ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sListCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * adapter
     */
    private NoteAdapter mAdapter;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(Note note,int position);
        public void saveNote(Note note);
        public void updateNote(Note note);
        public void deleteNote(Note note,int position);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sListCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(Note note,int position) {
        }

        @Override
        public void saveNote(Note note) {

        }

        @Override
        public void updateNote(Note note) {

        }

        @Override
        public void deleteNote(Note note,int position) {

        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NoteListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: replace with a real list adapter.
        mAdapter = new NoteAdapter(getActivity().getApplicationContext(),new ArrayList<Note>());
        //addFakeNote();
        setListAdapter(mAdapter);


        NoteListActivity mActivity = (NoteListActivity) getActivity();
        mActivity.loadNoteFromDB();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
        setDeleteTouchListener();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sListCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        Note note = (Note) mAdapter.getItem(position);
        mCallbacks.onItemSelected(note,position);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    /**
     * method to addnote into list
     */
    public void addNote(Note note){
        mAdapter.addItem(0,note);
        mAdapter.notifyDataSetChanged();
    }

    public void addNote(Note note,int position){
        mAdapter.addItem(position,note);
        mAdapter.notifyDataSetChanged();
    }


    public void addAllNote(List<Note> notes){
        mAdapter.addAll(notes);
        mAdapter.notifyDataSetChanged();
    }

    public void removeNote(Note note){

    }

    public void removeNote(int position){
        mCallbacks.deleteNote((Note)mAdapter.getItem(position),position);
        mAdapter.removeItem(position);
        mAdapter.notifyDataSetChanged();

    }

    /**
     * method to update a note
     */

    public void updateNote(Note note,int position){
        Note noteToUpdate = (Note) mAdapter.getItem(position);
        noteToUpdate.setmName(note.getmName());
        noteToUpdate.setmDesc(note.getmDesc());
        noteToUpdate.setmDate(note.getmDate());
        noteToUpdate.setmImage(note.getmImage());
        mAdapter.notifyDataSetChanged();
    }

    public ArrayList<Note> getmAdapter(){
        return mAdapter.getAllNotes();
    }


    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    private void setDeleteTouchListener(){
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        getListView(),
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                   removeNote(position);
                                }

                            }
                        });

        getListView().setOnTouchListener(touchListener);
        getListView().setOnScrollListener(touchListener.makeScrollListener());
    }





    ///////////////////////
    //metodo prova
    private void addFakeNote(){
        ((NoteListActivity)getActivity()).log("caricamento fake note");
        for(int i =1;i<11;i++){
            mAdapter.addItem(new Note(0,"note "+i,"desc note "+i,new Date(),new byte[1]));
        }
        mAdapter.notifyDataSetChanged();
    }
}
