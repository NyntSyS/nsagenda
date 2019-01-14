package com.dam.nestor_samuel.nsagenda;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.libizo.CustomEditText;
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FragmentModifyTask extends Fragment {

    private OnFragmentInteractionListener mListener;
    private int id;
    private CustomEditText nombreTarea;
    private CustomEditText descripcionTarea;
    private TextView tvMostrarFecha;
    private TextView tvMostrarHora;
    private ToggleSwitch toggleSwitchColores;
    private Button bFecha;
    private Button bHora;
    private Button bModificarTarea;
    LocalDateTime localDateTime;
    DatePickerDialog dpd;
    TimePickerDialog tpd;
    ProgressDialog progressDialog;
    int idColor;

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
        View view = inflater.inflate(R.layout.fragment_modify_task, container, false);

        nombreTarea = view.findViewById(R.id.fModifyTask_et_nombreTarea);
        descripcionTarea = view.findViewById(R.id.fModifyTask_et_descripcionTarea);
        bFecha = view.findViewById(R.id.fModifyTask_b_elegirFecha);
        bHora = view.findViewById(R.id.fModifyTask_b_elegirHora);
        tvMostrarFecha = view.findViewById(R.id.fModifyTask_tv_mostrarFecha);
        tvMostrarHora = view.findViewById(R.id.fModifyTask_tv_mostrarHora);
        bModificarTarea = view.findViewById(R.id.fModifyTask_b_UpdateTarea);

        toggleSwitchColores = view.findViewById(R.id.fModifyTask_mts_ToggleSwitchColores);

        final List<String> valoresColores= Arrays.asList("", "", "", "", "", "");
        toggleSwitchColores.setEntries(valoresColores);

        toggleSwitchColores.getButtons().get(0).setCheckedBackgroundColor(Color.WHITE);
        toggleSwitchColores.getButtons().get(0).setUncheckedBackgroundColor(Color.parseColor("#fafafa"));
        toggleSwitchColores.getButtons().get(1).setCheckedBackgroundColor(Color.parseColor("#4da6ff"));
        toggleSwitchColores.getButtons().get(1).setUncheckedBackgroundColor(Color.parseColor("#99ccff"));
        toggleSwitchColores.getButtons().get(2).setCheckedBackgroundColor(Color.parseColor("#66ff66"));
        toggleSwitchColores.getButtons().get(2).setUncheckedBackgroundColor(Color.parseColor("#b3ffb3"));
        toggleSwitchColores.getButtons().get(3).setCheckedBackgroundColor(Color.parseColor("#666699"));
        toggleSwitchColores.getButtons().get(3).setUncheckedBackgroundColor(Color.parseColor("#9494b8"));
        toggleSwitchColores.getButtons().get(4).setCheckedBackgroundColor(Color.parseColor("#bf8040"));
        toggleSwitchColores.getButtons().get(4).setUncheckedBackgroundColor(Color.parseColor("#d2a679"));
        toggleSwitchColores.getButtons().get(5).setCheckedBackgroundColor(Color.parseColor("#ff5050"));
        toggleSwitchColores.getButtons().get(5).setUncheckedBackgroundColor(Color.parseColor("#ff8080"));

        toggleSwitchColores.getButtons().get(0).uncheck();
        toggleSwitchColores.getButtons().get(1).uncheck();
        toggleSwitchColores.getButtons().get(2).uncheck();
        toggleSwitchColores.getButtons().get(3).uncheck();
        toggleSwitchColores.getButtons().get(4).uncheck();
        toggleSwitchColores.getButtons().get(5).uncheck();

        toggleSwitchColores.setCheckedPosition(tarea.getColor());

        nombreTarea.setText(tarea.getNombreTarea());
        descripcionTarea.setText(tarea.getDescripcion());

        tvMostrarFecha.setText(tarea.getFechaTarea().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        tvMostrarHora.setText(tarea.getFechaTarea().format(DateTimeFormatter.ofPattern("HH:mm")));


        AndroidThreeTen.init(getContext());

        bFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localDateTime = LocalDateTime.now();
                int day  = localDateTime.getDayOfMonth();
                int month = localDateTime.getMonthValue()-1;    // Mes empieza por 0
                int year = localDateTime.getYear();

                dpd = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        tvMostrarFecha.setText(dayOfMonth + "/" + (month+1) + "/" + year);    // Mes empieza por 0
                    }
                },year, month, day);    // Fecha al iniciar el diálogo

                dpd.show();
            }
        });

        bHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tpd = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        tvMostrarHora.setText(String.format("%02d",hourOfDay) + ":" + String.format("%02d",minute));
                    }
                }, 12, 00, true);   // Tiempo al iniciar el diálogo

                tpd.show();
            }
        });

        toggleSwitchColores.setOnChangeListener(new ToggleSwitch.OnChangeListener() {
            @Override
            public void onToggleSwitchChanged(int i) {
                idColor = i;
            }
        });

        bModificarTarea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verificarCampos()){
                    bModificarTarea.setEnabled(false);
                    mostrarRuedaProgreso("Modificando...");
                    new UpdateTarea().execute();
                }
            }
        });

        return view;
    }

    private boolean verificarCampos(){
        boolean camposCorrectos = true;

        nombreTarea.setText(nombreTarea.getText().toString().trim());
        descripcionTarea.setText(descripcionTarea.getText().toString().trim());

        if(nombreTarea.getText().toString().isEmpty()){
            nombreTarea.setError("El campo no puede estar vacío");
            camposCorrectos = false;
        }
        else{
            nombreTarea.setError(null);
        }

        return camposCorrectos;
    }

    private void mostrarRuedaProgreso(String mensaje) {

        progressDialog = new ProgressDialog(getContext(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(mensaje);
        progressDialog.show();

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

    public class UpdateTarea extends AsyncTask<Void, Void, Boolean> {

        OkHttpClient client;

        final String URL = "https://nesdam2018.000webhostapp.com/modificar_tarea.php";
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        @Override
        protected Boolean doInBackground(Void... voids) {

            boolean tareaActualizada = false;
            JSONObject jsonObject;      // Objeto JSON con los datos de los campos
            RequestBody body;           // Cuerpo de la petición con los datos
            Request request;            // Petición a la página web
            Response response;          // Respuesta del servidor
            JSONObject responseJSON;    // Objeto JSON con los datos recogidos del servidor

            try {
                client = new OkHttpClient();

                jsonObject = new JSONObject();

                LocalDate fechaElegida = LocalDate.parse(tvMostrarFecha.getText(), DateTimeFormatter.ofPattern("d/M/yyyy"));
                String fecha = fechaElegida.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String hora = tvMostrarHora.getText().toString();
                LocalDateTime aux = LocalDateTime.parse(fecha + " " + hora, DateTimeFormatter.ofPattern("yyyy-M-d H:m"));

                jsonObject.put("id",tarea.getId());
                jsonObject.put("nombre",nombreTarea.getText().toString());
                jsonObject.put("descripcion",descripcionTarea.getText().toString());
                jsonObject.put("fecha",aux.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                jsonObject.put("color",idColor);
                jsonObject.put("id_usuario",id);

                body = RequestBody.create(JSON, jsonObject.toString());
                request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();

                response = client.newCall(request).execute();
                responseJSON = new JSONObject(response.body().string());

                tareaActualizada = responseJSON.getBoolean("estado");

                response.close();
            } catch (JSONException jsone) {
                Log.e("--ERROR--", "Error al parsear JSON");    //  Borrar más adelante
            } catch (IOException ioe) {
                Log.e("--ERROR--", "Error al realizar petición");    //  Borrar más adelante
            }

            return tareaActualizada;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDialog.dismiss();
            if(aBoolean){
                Toast.makeText(getContext(),"Tarea modificada",Toast.LENGTH_LONG).show();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(((ViewGroup)getView().getParent()).getId(),FragmentShowTasks.newInstance(id))
                        .addToBackStack(null).commit();
            }
            else{
                Toast.makeText(getContext(),"Error al modificar tarea",Toast.LENGTH_LONG).show();
                bModificarTarea.setEnabled(true);
            }
        }
    }
}
