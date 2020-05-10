package com.example.surround.Controller;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

//import java.sql.SQLOutput;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.surround.R;
import com.example.surround.Common.Song;
import com.example.surround.Utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ControllerFragment extends Fragment {
    private RecyclerView recyclerView;
    private ImageView imageView;
    private TextView title;
    private TextView artist;
    private TextView duration;
    private ArrayList<Song> controllerArrayList;
    private MyControllerRecyclerViewAdapter adapter;

    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    public ControllerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_controller_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            imageView = view.findViewById(R.id.imgView);
            title = view.findViewById(R.id.titleTV);
            artist = view.findViewById(R.id.artistTV);
            duration = view.findViewById(R.id.durationTV);
            controllerArrayList = new ArrayList<>();
            //Icon i = new Icon(R.drawable.summer69);

            controllerArrayList.add(
                    new Song("384", "", "Summer of 69", "Bryan Adams", 150)
            );controllerArrayList.add(
                    new Song("384", "", "Summer of 69", "Bryan Adams", 150)
            );controllerArrayList.add(
                    new Song("384", "", "Summer of 69", "Bryan Adams", 150)
            );controllerArrayList.add(
                    new Song("384", "", "Summer of 69", "Bryan Adams", 150)
            );controllerArrayList.add(
                    new Song("384", "", "Summer of 69", "Bryan Adams", 150)
            );controllerArrayList.add(
                    new Song("384", "", "Summer of 69", "Bryan Adams", 150)
            );
            controllerArrayList.add(
                    new Song("384", "", "Summer of 69", "Bryan Adams", 150)
            );controllerArrayList.add(
                    new Song("384", "", "Summer of 69", "Bryan Adams", 150)
            );controllerArrayList.add(
                    new Song("384", "", "Summer of 69", "Bryan Adams", 150)
            );controllerArrayList.add(
                    new Song("384", "", "Summer of 69", "Bryan Adams", 150)
            );controllerArrayList.add(
                    new Song("384", "", "Summer of 69", "Bryan Adams", 150)
            );controllerArrayList.add(
                    new Song("384", "", "Summer of 69", "Bryan Adams", 150)
            );controllerArrayList.add(
                    new Song("384", "", "Summer of 69", "Bryan Adams", 150)
            );controllerArrayList.add(
                    new Song("384", "", "Summer of 69", "Bryan Adams", 150)
            );controllerArrayList.add(
                    new Song("384", "", "Summer of 69", "Bryan Adams", 150)
            );controllerArrayList.add(
                    new Song("384", "", "Summer of 69", "Bryan Adams", 150)
            );controllerArrayList.add(
                    new Song("384", "", "Summer of 69", "Bryan Adams", 150)
            );

            getMusic();

            adapter = new MyControllerRecyclerViewAdapter(controllerArrayList, mListener, getContext());
            //recyclerView.setAdapter(adapter);

            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(adapter);
        }

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void scrollToStart(){
        recyclerView.scrollToPosition(0);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Song song);

        void onFragmentInteraction(Uri uri);
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
