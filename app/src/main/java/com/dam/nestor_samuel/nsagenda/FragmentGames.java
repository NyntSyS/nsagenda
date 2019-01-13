package com.dam.nestor_samuel.nsagenda;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FragmentGames extends Fragment {

    private OnFragmentInteractionListener mListener;
    private String nick;
    private TextView tv_maxima_puntuacion;
    private Button btn_jugar;

    private SQLiteDatabase db;
    private DatabaseAcceleroDodge database;

    public FragmentGames() {
        // Required empty public constructor
    }

    public static FragmentGames newInstance(String nick) {

        FragmentGames fragment = new FragmentGames();
        Bundle args = new Bundle();
        args.putString("Nick", nick);
        fragment.setArguments(args);

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nick = getArguments().getString("Nick");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_games, container, false);

        database = new DatabaseAcceleroDodge(view.getContext(), "Records", null, 1);
        db = database.getWritableDatabase();

        tv_maxima_puntuacion = view.findViewById(R.id.fGames_tv_maximaPuntuacion);
        consultarMaximaPuntuacion();

        btn_jugar = view.findViewById(R.id.fGames_btn_jugar);
        btn_jugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ActivityAcceleroDodge.class);
                Bundle bundle = new Bundle();
                bundle.putString("Nick", nick);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        return view;
    }

    private void consultarMaximaPuntuacion() {

        String query = "SELECT nick, puntuacion " +
                "FROM RECORDS " +
                "WHERE puntuacion = (SELECT MAX(puntuacion) FROM RECORDS)";
        String[] args = new String[] {};

        Cursor c = db.rawQuery(query, args);

        if(c.moveToNext()) {
            tv_maxima_puntuacion.setText(c.getString(0) + " " + c.getLong(1));
        }
        else {
            tv_maxima_puntuacion.setText("(Sin registros)");
        }

        c.close();

    }

    @Override
    public void onResume() {
        super.onResume();

        consultarMaximaPuntuacion();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
