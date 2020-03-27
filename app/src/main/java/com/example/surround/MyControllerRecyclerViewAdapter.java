package com.example.surround;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.surround.ControllerFragment.OnListFragmentInteractionListener;
import com.example.surround.dummy.DummyContent.DummyItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyControllerRecyclerViewAdapter extends RecyclerView.Adapter<MyControllerRecyclerViewAdapter.ViewHolder> {

    private final List<Song> songs;
    private final OnListFragmentInteractionListener mListener;

    public MyControllerRecyclerViewAdapter(List<Song> items, OnListFragmentInteractionListener listener) {
        songs = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_controller, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.song = songs.get(position);
        holder.imgv.setImageIcon(songs.get(position).image);
        holder.titTV.setText(songs.get(position).title);
        holder.artTV.setText(songs.get(position).artist);
        holder.durTV.setText(songs.get(position).duration+"");

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.song);

                }
            }
        });
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
            imgv = (ImageView) view.findViewById(R.id.imgView);
            titTV = (TextView) view.findViewById(R.id.titleTV);
            artTV= (TextView) view.findViewById(R.id.artistTV);
            durTV= (TextView) view.findViewById(R.id.durationTV);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + titTV.getText() + "'" + artTV.getText() + "'" + durTV.getText() + "' ";
        }
    }
}
