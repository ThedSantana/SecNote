package com.baraccasoftware.securenotes.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baraccasoftware.securenotes.app.R;
import com.baraccasoftware.securenotes.object.ImageLoader;
import com.baraccasoftware.securenotes.object.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by angelo on 24/02/14.
 */
public class NoteAdapter extends BaseAdapter {

    private class ViewHolder{
        public TextView nome;
        public TextView desc;
        public ImageView image;
    }

    private Context mContext;
    private ArrayList<Note> mData;
    private LayoutInflater mLayoutInflater;
    private ViewHolder mViewHolder;
    ImageLoader mImgLoader;

    public NoteAdapter(Context context, ArrayList<Note> data) {
        this.mData = data;
        this.mContext = context;
        this.mLayoutInflater = (LayoutInflater)
                this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mImgLoader = new ImageLoader(mContext);

    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public int getItemId(Object item){
        return mData.indexOf(item);
    }

    public void addItem(Object item){
        mData.add((Note)item);
    }

    public void addItem(int id,Object item){
        mData.add(id,(Note)item);
    }

    public void removeItem(Object item){
        mData.remove(item);

    }

    public void removeItem(int position){
        mData.remove(mData.get(position));

    }

    public  void addAll(List<Note> notes){
        mData.addAll(notes);
    }

    public ArrayList<Note> getAllNotes(){
        return mData;
    }




    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        mViewHolder = null;
        Note note = (Note) getItem(i);
        //get Texts
        String title = note.getmName();
        String text = note.getmDesc();
        //check if only image
        boolean onlyImage = title.equals("") && text.equals("");
        //check if there is image
        boolean isImage = note.getmImage().length > 1;
        mViewHolder = new ViewHolder();
        if(isImage) {

            if(onlyImage){
                //solo img
                view = mLayoutInflater.inflate(R.layout.layout_note_only_img, null);

            }else{
                view = mLayoutInflater.inflate(R.layout.layout_note_img_text, null);
                mViewHolder.nome = (TextView) view.findViewById(R.id.textView_Nome);
                mViewHolder.desc = (TextView) view.findViewById(R.id.textView_Desc);
                setText(note);
            }

            mViewHolder.image = (ImageView) view.findViewById(R.id.imageView1_listnotes);
            mImgLoader.loadBitmap(note, mViewHolder.image,onlyImage);
            view.setTag(mViewHolder);


        }else{
            view = mLayoutInflater.inflate(R.layout.layout_note_text, null);
            mViewHolder.nome = (TextView) view.findViewById(R.id.textView_Nome);
            mViewHolder.desc = (TextView) view.findViewById(R.id.textView_Desc);
            setText(note);
            view.setTag(mViewHolder);

        }
        return view;
    }

    private void setText(Note note){
        mViewHolder.nome.setText(note.getmName());
        if(note.getmName().equals("") && !note.getmDesc().equals("")){
            mViewHolder.nome.setText(note.getmDesc());
        }else{
            mViewHolder.desc.setText(note.getmDesc());
        }
    }
}
