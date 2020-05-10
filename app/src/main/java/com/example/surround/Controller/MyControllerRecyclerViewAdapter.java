package com.example.surround.Controller;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.surround.R;
import com.example.surround.Common.Song;
import com.example.surround.dummy.DummyContent.DummyItem;

import java.util.List;

public class MyControllerRecyclerViewAdapter extends RecyclerView.Adapter<MyControllerRecyclerViewAdapter.ViewHolder> {

    private final List<Song> songs;
    private Context mContext;

    public MyControllerRecyclerViewAdapter(List<Song> items, Context con) {
        songs = items;
        mContext = con;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.song = songs.get(position);
        // TODO: change imageview to image from web
        //holder.imgv.setImageResource(songs.get(position).getImageRes());
        holder.titTV.setText(songs.get(position).getTitle());
        holder.artTV.setText(songs.get(position).getArtist());
        holder.durTV.setText(songs.get(position).getDuration()+"");

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentJump(holder.song);
            }
        });
    }

    private void fragmentJump(Song song) {
        SongFragment songF = new SongFragment(song);
        Bundle bundle = new Bundle();
        bundle.putParcelable("song", song);
        songF.setArguments(bundle);
        switchContent(songF);
    }

    public void switchContent(Fragment songF) {
            ControllerMainActivity mainActivity = (ControllerMainActivity)mContext;
            mainActivity.replaceFragment(songF);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView imgv;
        public final TextView titTV;
        public final TextView artTV;
        public final TextView durTV;
        public Song song;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            Log.d("Se creo el mView", mView.toString());
            imgv = (ImageView) view.findViewById(R.id.imgView);
            titTV = (TextView) view.findViewById(R.id.titleTV);
            artTV= (TextView) view.findViewById(R.id.artistTV);
            durTV= (TextView) view.findViewById(R.id.durationTV);
        }

        @Override
        public String toString() {
            return super.toString() +
                    " '" + titTV.getText().toString() +
                    "'" + artTV.getText().toString() +
                    "'" + durTV.getText().toString() +
                    "' ";
        }
    }
}
