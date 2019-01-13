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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.libizo.CustomEditText;
import com.llollox.androidtoggleswitch.widgets.MultipleToggleSwitch;
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FragmentNewTask extends Fragment {

    private OnFragmentInteractionListener mListener;
    private int id;
    private MultipleToggleSwitch multipleToggleSwitch;
    private ToggleSwitch toggleSwitchColores;
    private RadioGroup rgDiaSemana;
    private RadioGroup rgPeriodo;
    private Button bFecha;
    private Button bHora;
    private CustomEditText nombreTarea;
    private CustomEditText descripcionTarea;
    private Button bElegirFecha;
    private Button bElegirHora;
    private TextView tvMostrarFecha;
    private TextView tvMostrarHora;
    private Button bAddTarea;
    private int idRGdiaSemana;
    private int idRGperiodo;
    private LinearLayout lyElegirFecha;
    private LinearLayout lyMultipleToogle;
    private TextView tvRepetirPara;
    private TextView tvErrorToogle;
    private RadioButton rbEstaSemana;
    private RadioButton rb1Semana;
    private RadioButton rb1Mes;
    LocalDateTime localDateTime;
    DatePickerDialog dpd;
    TimePickerDialog tpd;
    ProgressDialog progressDialog;
    int idColor;

    public FragmentNewTask() {
        // Required empty public constructor
    }

    public static FragmentNewTask newInstance(int id) {

        FragmentNewTask fragment = new FragmentNewTask();
        Bundle args = new Bundle();
        args.putInt("ID", id);
        fragment.setArguments(args);

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            id = getArguments().getInt("ID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_task, container, false);

        idRGdiaSemana = 1;
        idRGperiodo = 1;
        bFecha = view.findViewById(R.id.fNewTask_b_elegirFecha);
        bHora = view.findViewById(R.id.fNewTask_b_elegirHora);
        nombreTarea = view.findViewById(R.id.fNewTask_et_nombreTarea);
        descripcionTarea = view.findViewById(R.id.fNewTask_et_descripcionTarea);
        tvMostrarFecha = view.findViewById(R.id.fNewTask_tv_mostrarFecha);
        tvMostrarHora = view.findViewById(R.id.fNewTask_tv_mostrarHora);
        bAddTarea = view.findViewById(R.id.fNewTask_b_AddTarea);
        lyElegirFecha = view.findViewById(R.id.fNewTask_LY_elegirFecha);
        lyMultipleToogle = view.findViewById(R.id.fNewTask_ly_multipleToggleSwitch);
        tvRepetirPara = view.findViewById(R.id.fNewTask_tv_Repetir);
        tvErrorToogle = view.findViewById(R.id.fNewTask_tv_errorToogle);
        rbEstaSemana = view.findViewById(R.id.fNewTask_rb_estaSemana);
        rb1Semana = view.findViewById(R.id.fNewTask_rb_1Semana);
        rb1Mes = view.findViewById(R.id.fNewTask_rb_1mes);

        multipleToggleSwitch = view.findViewById(R.id.fNewTask_mts_multipleToggleSwitch);

        toggleSwitchColores = view.findViewById(R.id.fNewTask_mts_ToggleSwitchColores);

        final List<String> valoresBotones = Arrays.asList("L", "M", "X", "J", "V", "S", "D");
        multipleToggleSwitch.setEntries(valoresBotones);

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

        toggleSwitchColores.setCheckedPosition(0);

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

        rgDiaSemana = view.findViewById(R.id.fNewTask_rg_elegirDiaSemana);
        rgPeriodo = view.findViewById(R.id.fNewTask_rg_elegirPeriodoRepeticion);
        rgDiaSemana.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRB = (RadioButton)group.findViewById(checkedId);

                switch (checkedRB.getId()){
                    case R.id.fNewTask_rb_Dia:
                        idRGdiaSemana = 1;
                        lyElegirFecha.setVisibility(View.VISIBLE);
                        lyMultipleToogle.setVisibility(View.GONE);
                        tvRepetirPara.setVisibility(View.GONE);
                        rgPeriodo.setVisibility(View.GONE);
                        break;
                    case R.id.fNewTask_rb_Semana:
                        idRGdiaSemana = 2;
                        lyElegirFecha.setVisibility(View.GONE);
                        lyMultipleToogle.setVisibility(View.VISIBLE);
                        tvRepetirPara.setVisibility(View.VISIBLE);
                        rgPeriodo.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        rgPeriodo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRB = (RadioButton)group.findViewById(checkedId);

                switch (checkedRB.getId()){
                    case R.id.fNewTask_rb_estaSemana:
                        idRGperiodo = 1;
                        break;
                    case R.id.fNewTask_rb_1Semana:
                        idRGperiodo = 2;
                        break;
                    case R.id.fNewTask_rb_1mes:
                        idRGperiodo = 3;
                        break;
                }
            }
        });

        bAddTarea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verificarCampos()){
                    bAddTarea.setEnabled(false);
                    mostrarRuedaProgreso("Añadiendo...");
                    new AddTarea().execute();
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

        if(idRGdiaSemana  == 1 && tvMostrarFecha.getText().toString().isEmpty()){
            tvMostrarFecha.requestFocus();
            tvMostrarFecha.setError("El campo no puede estar vacío");
            camposCorrectos = false;
        }

        if(tvMostrarHora.getText().toString().isEmpty()){
            tvMostrarHora.requestFocus();
            tvMostrarHora.setError("El campo no puede estar vacío");
            camposCorrectos = false;
        }

        if(idRGdiaSemana == 2){
            boolean diaSeleccionado = false;
            int i = 0;

            while( i < multipleToggleSwitch.getButtons().size() && !diaSeleccionado){
                diaSeleccionado = multipleToggleSwitch.getButtons().get(i).isChecked();
                i++;
            }
            if(!diaSeleccionado){
                tvErrorToogle.setText("Tiene que haber un día seleccionado al menos");
                camposCorrectos = false;
            }
            else{
                tvErrorToogle.setText("");
            }
        }
        return camposCorrectos;
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

    private void mostrarRuedaProgreso(String mensaje) {

        progressDialog = new ProgressDialog(getContext(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(mensaje);
        progressDialog.show();

    }

    public class AddTarea extends AsyncTask<Void, Void, Boolean> {

        OkHttpClient client;

        final String URL = "https://nesdam2018.000webhostapp.com/insertar_multiple_tarea.php";
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        @Override
        protected Boolean doInBackground(Void... voids) {

            boolean tareasCreadas = false;
            JSONObject jsonObject;      // Objeto JSON con los datos de los campos
            RequestBody body;           // Cuerpo de la petición con los datos
            Request request;            // Petición a la página web
            Response response;          // Respuesta del servidor
            JSONObject responseJSON;    // Objeto JSON con los datos recogidos del servidor
            JSONArray jsonTareas;       // Para almacenar varias tareas
            JSONObject tarea;           // Almacena una tarea

            try {
                client = new OkHttpClient();

                jsonObject = new JSONObject();

                if (idRGdiaSemana == 1) {
                    LocalDate fechaElegida = LocalDate.parse(tvMostrarFecha.getText(), DateTimeFormatter.ofPattern("d/M/yyyy"));
                    String fecha = fechaElegida.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String hora = tvMostrarHora.getText().toString();
                    LocalDateTime aux = LocalDateTime.parse(fecha + " " + hora, DateTimeFormatter.ofPattern("yyyy-M-d H:m"));
                    tarea = new JSONObject();
                    tarea.put("nombre", nombreTarea.getText());
                    tarea.put("descripcion", descripcionTarea.getText());
                    tarea.put("fecha", aux.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    tarea.put("color", idColor);
                    tarea.put("id_usuario", id);

                    jsonTareas = new JSONArray();
                    jsonTareas.put(tarea);
                    jsonObject.put("tareas", jsonTareas);
                } else {
                    LocalDate inicioSemana = LocalDate.now();
                    while (!inicioSemana.getDayOfWeek().name().equalsIgnoreCase("monday")) {
                        inicioSemana = inicioSemana.minusDays(1);
                    }
                    List<LocalDateTime> fechas = new ArrayList<LocalDateTime>();
                    for (int i = 0; i < multipleToggleSwitch.getButtons().size(); i++) {
                        if (multipleToggleSwitch.getButtons().get(i).isChecked()) {
                            fechas.add(LocalDateTime.parse(inicioSemana.
                                    format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " " +
                                    tvMostrarHora.getText().toString(), DateTimeFormatter.ofPattern("yyyy-M-d H:m")));
                        }
                        inicioSemana = inicioSemana.plusDays(1);
                    }
                    if (idRGperiodo == 2) {
                        int sizeLista = fechas.size();

                        for (int i = 0; i < sizeLista; i++) {
                            fechas.add(fechas.get(i).plusDays(7));
                        }
                    } else if (idRGperiodo == 3) {
                        int sizeLista = fechas.size();

                        for (int i = 0; i < sizeLista; i++) {
                            fechas.add(fechas.get(i).plusDays(7));
                            fechas.add(fechas.get(i).plusDays(14));
                            fechas.add(fechas.get(i).plusDays(21));
                        }
                    }
                    jsonTareas = new JSONArray();

                    for (int i = 0; i < fechas.size(); i++) {
                        tarea = new JSONObject();
                        tarea.put("nombre", nombreTarea.getText());
                        tarea.put("descripcion", descripcionTarea.getText());
                        tarea.put("fecha", fechas.get(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                        tarea.put("color", idColor);
                        tarea.put("id_usuario", id);
                        jsonTareas.put(tarea);
                    }
                    jsonObject.put("tareas", jsonTareas);
                }


                body = RequestBody.create(JSON, jsonObject.toString());
                request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();

                response = client.newCall(request).execute();
                responseJSON = new JSONObject(response.body().string());

                tareasCreadas = responseJSON.getBoolean("estado");

                response.close();
            } catch (JSONException jsone) {
                Log.e("--ERROR--", "Error al parsear JSON");    //  Borrar más adelante
            } catch (IOException ioe) {
                Log.e("--ERROR--", "Error al realizar petición");    //  Borrar más adelante
            }

            return tareasCreadas;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDialog.dismiss();
            if(aBoolean){
                Toast.makeText(getContext(),"Tareas añadidas",Toast.LENGTH_LONG).show();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(((ViewGroup)getView().getParent()).getId(),FragmentShowTasks.newInstance(id))
                        .addToBackStack(null).commit();
            }
            else{
                Toast.makeText(getContext(),"Error al añadir tareas",Toast.LENGTH_LONG).show();
                bAddTarea.setEnabled(true);
            }
        }
    }
}
