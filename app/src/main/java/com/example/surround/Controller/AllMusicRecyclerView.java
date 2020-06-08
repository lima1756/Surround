package com.example.surround.Controller;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.surround.R;
import com.example.surround.Utils.Constants;
import com.example.surround.Utils.Song;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AllMusicRecyclerView extends RecyclerView.Adapter<AllMusicRecyclerView.ViewHolder> {

    private final List<Song> songs;
    private Context mContext;

    AllMusicRecyclerView(List<Song> items, Context con) {
        songs = items;
        mContext = con;
    }

    @NotNull
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
        //.setImageResource(songs.get(position).getImageRes());
        Glide.with(mContext)
                .load(songs.get(position).getImageRes())
                .placeholder(R.drawable.vinil)
                .fitCenter()
                .into(holder.imgv);
        holder.titTV.setText(songs.get(position).getTitle());
        holder.artTV.setText(songs.get(position).getArtist());
        int minutes = songs.get(position).getDuration()/60;
        int seconds = songs.get(position).getDuration()%60;
        holder.durTV.setText(String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds));
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

    private void switchContent(Fragment songF) {
            ControllerActivity mainActivity = (ControllerActivity)mContext;
            mainActivity.pushFragment(songF);
    }

    @Override
    public int getItemCount() {
        if(songs == null)
            return 0;
        return songs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView imgv;
        final TextView titTV;
        final TextView artTV;
        final TextView durTV;
        Song song;

        ViewHolder(View view) {
            super(view);
            mView = view;
            Log.d("Se creo el mView", mView.toString());
            imgv = (ImageView) view.findViewById(R.id.imgView);
            titTV = (TextView) view.findViewById(R.id.titleTV);
            artTV= (TextView) view.findViewById(R.id.artistTV);
            durTV= (TextView) view.findViewById(R.id.durationTV);
        }

        @NotNull
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
