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
import com.example.surround.R;
import com.example.surround.Utils.Song;
import com.example.surround.Utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;


public class AllMusicFragment extends Fragment {
    private RecyclerView recyclerView;
    private ArrayList<Song> controllerArrayList;
    private AllMusicRecyclerView adapter;


    public AllMusicFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_music, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            controllerArrayList = new ArrayList<>();

           controllerArrayList.add(
                    new Song("384", "", "Test", "default", 1500)
            );

            getMusic();

            adapter = new AllMusicRecyclerView(controllerArrayList, getContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(adapter);
        }

        return view;
    }

    public void scrollToStart(){
        recyclerView.scrollToPosition(0);
    }


    private void getMusic() {
        RequestQueue queue = Volley.newRequestQueue(this.getContext());

        // Request a string response from the provided URL.
        JsonArrayRequest stringRequest = new JsonArrayRequest(
                Request.Method.GET,
                Constants.SERVER_URL+ Constants.SERVER_GET_ALL_MUSIC,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for(int i  = 0; i < response.length(); i++) {
                            try {
                                controllerArrayList.add(Song.CreateFromJSON(response.getJSONObject(i)));
                            } catch (JSONException err) {
                                Log.e("MUSIC", err.toString());
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("MUSIC", error.toString());
                    }
                }
        );

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

}
