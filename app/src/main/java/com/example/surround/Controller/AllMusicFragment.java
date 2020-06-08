package com.example.surround.Controller;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

//import java.sql.SQLOutput;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.surround.Controller.Utils.SongListener;
import com.example.surround.R;
import com.example.surround.Utils.Song;
import com.example.surround.Utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;


public class AllMusicFragment extends Fragment implements SongListener {
    private RecyclerView recyclerView;

    private AllMusicRecyclerView adapter;
    private ArrayList<Song> songs;


    public AllMusicFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_music, container, false);
        this.songs = ((ControllerActivity)getActivity()).getSongs();

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            adapter = new AllMusicRecyclerView(songs, getContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(adapter);
            ((ControllerActivity)getActivity()).retrieveMusic(this);
        }

        return view;
    }

    public void scrollToStart(){
        recyclerView.scrollToPosition(0);
    }


    @Override
    public void updateSongs(ArrayList<Song> songs) {
        Log.d("FRAGMENT", ""+songs.size());
        this.songs = songs;
        adapter.notifyDataSetChanged();
    }
}
