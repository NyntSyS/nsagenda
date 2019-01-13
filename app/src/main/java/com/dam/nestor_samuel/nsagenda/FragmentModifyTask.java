package com.dam.nestor_samuel.nsagenda;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.libizo.CustomEditText;

public class FragmentModifyTask extends Fragment {

    private OnFragmentInteractionListener mListener;
    private int id;
    private CustomEditText nombreTarea;
    private CustomEditText descripcionTarea;

    private Tarea tarea;

    public FragmentModifyTask() {
        // Required empty public constructor
    }

    public static FragmentModifyTask newInstance(int id, Tarea tarea) {

        FragmentModifyTask fragment = new FragmentModifyTask();
        Bundle args = new Bundle();
        args.putInt("ID", id);
        args.putParcelable("Tarea", tarea);
        fragment.setArguments(args);

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getInt("ID");
            tarea = getArguments().getParcelable("Tarea");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_modify_task, container, false);

        //  Hacer findViewById en esta parte, añadiendo view. antes, por ejemplo:
        //  editText = view.findViewById(R.id.editText)

        return view;
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
