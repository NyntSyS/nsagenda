package com.dam.nestor_samuel.nsagenda;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adammcneilly.ActionButton;
import com.adammcneilly.ActionCardView;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FragmentShowTasks extends Fragment implements View.OnClickListener {

    private int id, diaElegido, mesElegido, anyElegido;
    private ProgressDialog progressDialog;
    private List<Tarea> tareas;
    private List<ActionCardView> cardViews;
    private OnFragmentInteractionListener mListener;

    private LinearLayout linearLayout;      //  Layout con las tareas
    private ImageButton ibtn_exportar;
    private ImageButton ibtn_actualizar;
    private ImageButton ibtn_fecha;

    public FragmentShowTasks() {
        // Required empty public constructor
    }

    public static FragmentShowTasks newInstance(int id) {

        FragmentShowTasks fragment = new FragmentShowTasks();
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

        cardViews = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_tasks, container, false);

        AndroidThreeTen.init(view.getContext());
        diaElegido = LocalDate.now().getDayOfMonth();
        mesElegido = LocalDate.now().getMonthValue();
        anyElegido = LocalDate.now().getYear();

        linearLayout = view.findViewById(R.id.fShowTasks_linearLayout);
        ibtn_exportar = view.findViewById(R.id.fShowTasks_ibtn_exportar);
        ibtn_actualizar = view.findViewById(R.id.fShowTasks_ibtn_actualizar);
        ibtn_fecha = view.findViewById(R.id.fShowTasks_ibtn_fecha);

        ibtn_exportar.setOnClickListener(this);
        ibtn_actualizar.setOnClickListener(this);
        ibtn_fecha.setOnClickListener(this);

        mostrarRuedaProgreso("Leyendo datos...");
        new GetTasks().execute(""+id, anyElegido+"-"+mesElegido+"-"+diaElegido);

        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.fShowTasks_ibtn_exportar:
                String[] lista = {"Fecha elegida", "Dentro de un mes", "Todas las tareas"};

                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                        .setTitle("Elige que tareas exportar:")
                        .setIcon(R.drawable.export)
                        .setItems(lista, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:     //  Fecha elegida
                                        new ExportTasks().execute(""+id, "1", anyElegido+"-"+mesElegido+"-"+diaElegido);
                                        break;

                                    case 1:     //  Dentro de un mes
                                        String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                        new ExportTasks().execute(""+id, "2", fechaActual);
                                        break;

                                    case 2:     //  Todas las tareas
                                        new ExportTasks().execute(""+id, "3", "");  //  No hace falta fecha
                                        break;
                                }

                            }
                        })
                        .create();

                alertDialog.show();
                break;

            case R.id.fShowTasks_ibtn_actualizar:
                limpiarTareas();
                mostrarRuedaProgreso("Leyendo datos...");
                new GetTasks().execute(""+id, anyElegido+"-"+mesElegido+"-"+diaElegido);
                break;

            case R.id.fShowTasks_ibtn_fecha:
                int diaActual = LocalDate.now().getDayOfMonth();
                int mesActual = LocalDate.now().getMonthValue()-1;  //  Mes empieza por 0
                int anyActual = LocalDate.now().getYear();

                DatePickerDialog dpd = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        diaElegido = dayOfMonth;
                        mesElegido = month+1;
                        anyElegido = year;
                        limpiarTareas();
                        mostrarRuedaProgreso("Leyendo datos...");
                        new GetTasks().execute(""+id, year+"-"+(month+1)+"-"+dayOfMonth);
                    }
                }, anyActual, mesActual, diaActual);

                dpd.show();
                break;

        }

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

        progressDialog = new ProgressDialog(getContext(), R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(mensaje);
        progressDialog.show();

    }

    private void limpiarTareas() {

        linearLayout.removeAllViews();
        cardViews.clear();

    }

    public class GetTasks extends AsyncTask<String, Void, List<Tarea>> {

        OkHttpClient client;

        final String URL = ServicioWeb.PAGINA_BASE + "leer_tareas.php";
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        @Override
        protected List<Tarea> doInBackground(String... strings) {

            tareas = new ArrayList<>(); // Lista con las tareas a recoger de la BD
            JSONObject jsonObject;      // Objeto JSON con los datos de los campos
            RequestBody body;           // Cuerpo de la petición con los datos
            Request request;            // Petición a la página web
            Response response;          // Respuesta del servidor
            JSONObject responseJSON;    // Objeto JSON con los datos recogidos del servidor
            JSONArray jsonArray;        // Array JSON con todas las tareas

            try {
                client = new OkHttpClient();

                jsonObject = new JSONObject();
                jsonObject.put("id", strings[0]);
                jsonObject.put("fecha", strings[1]);

                body = RequestBody.create(JSON, jsonObject.toString());
                request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();

                response = client.newCall(request).execute();
                responseJSON = new JSONObject(response.body().string());

                if(responseJSON.getBoolean("estado") == true) {
                    jsonArray = responseJSON.getJSONArray("tareas");

                    for(int i=0; i<jsonArray.length(); i++) {
                        tareas.add(new Tarea(jsonArray.getJSONObject(i).getInt("id"),
                                jsonArray.getJSONObject(i).getInt("color"),
                                jsonArray.getJSONObject(i).getString("nombre"),
                                jsonArray.getJSONObject(i).getString("descripcion"),
                                LocalDateTime.parse(jsonArray.getJSONObject(i).getString("fecha"),
                                        DateTimeFormatter.ofPattern("yyyy-M-d H:m:s"))));
                    }
                }

                response.close();
            }
            catch (JSONException jsone) {
                Log.e("--ERROR--", "Error al parsear JSON");
            }
            catch(IOException ioe) {
                Log.e("--ERROR--", "Error al realizar petición");
            }

            return tareas;
        }

        @Override
        protected void onPostExecute(List<Tarea> tareas) {

            progressDialog.dismiss();       //  Comentar si se ha desactivado en OnCreateView

            if(tareas.size() > 0) {
                for(final Tarea tarea : tareas) {
                    GradientDrawable border = new GradientDrawable();
                    border.setStroke(2, Color.BLACK);

                    ActionCardView actionCardView = new ActionCardView(getContext());

                    actionCardView.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));
                    actionCardView.setTitle(tarea.getNombreTarea() + " " + tarea.getFechaTarea().format(DateTimeFormatter.ofPattern("HH:mm")));
                    actionCardView.setDescription(tarea.getDescripcion());
                    actionCardView.setDividerHeight(1);
                    border.setColor(devolverColor(tarea.getColor()));
                    actionCardView.setBackground(border);
                    ActionButton modificar = new ActionButton.Builder(getContext())
                            .setText("Modificar")
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    getActivity().getSupportFragmentManager().beginTransaction()
                                            .replace(((ViewGroup)getView().getParent()).getId(), FragmentModifyTask.newInstance(id, tarea))
                                            .addToBackStack(null)
                                            .commit();
                                }
                            })
                            .setTextColor(Color.BLUE)
                            .create();

                    modificar.setTextColor(Color.BLACK);
                    actionCardView.addActionButton(modificar);



                    cardViews.add(actionCardView);
                    linearLayout.addView(actionCardView);

                }

                linearLayout.setPadding(0, 5, 0, 5);
            }
            else {
                TextView sinTareas = new TextView(getContext());
                sinTareas.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                sinTareas.setTextColor(Color.GRAY);
                sinTareas.setText("No hay tareas en el día elegido");
                sinTareas.setTextSize(28);
                sinTareas.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                linearLayout.addView(sinTareas);
            }
        }

        private int devolverColor(int idColor) {

            switch (idColor) {

                case 1:     //  Azul
                    return Color.parseColor("#4da6ff");

                case 2:     //  Verde
                    return Color.parseColor("#66ff66");

                case 3:     //  Morado
                    return Color.parseColor("#666699");

                case 4:     //  Marrón
                    return Color.parseColor("#bf8040");

                case 5:     //  Rojo
                    return Color.parseColor("#ff5050");

                default:
                    return Color.WHITE;

            }

        }
    }

    public class ExportTasks extends AsyncTask<String, Void, Integer> {

        OkHttpClient client;

        final String URL = ServicioWeb.PAGINA_BASE + "exportar_tareas.php";
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        @Override
        protected Integer doInBackground(String... strings) {

            int estado = 0;
            JSONObject jsonObject;          // Objeto JSON con los datos de los campos
            RequestBody body;               // Cuerpo de la petición con los datos
            Request request;                // Petición a la página web
            Response response;              // Respuesta del servidor
            JSONObject responseJSON;        // Objeto JSON con los datos recogidos del servidor
            JSONArray jsonArray;            // Array JSON con todas las tareas
            PrintWriter printWriter = null; // PrintWriter para exportar a fichero de texto

            if(Environment.getExternalStorageState() == null) {
                estado = 1;
                return estado;
            }

            try {
                client = new OkHttpClient();

                jsonObject = new JSONObject();
                jsonObject.put("id", Integer.parseInt(strings[0]));
                jsonObject.put("type", Integer.parseInt(strings[1]));
                jsonObject.put("fecha", strings[2]);

                body = RequestBody.create(JSON, jsonObject.toString());
                request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();

                response = client.newCall(request).execute();
                responseJSON = new JSONObject(response.body().string());

                if(responseJSON.getBoolean("estado") == true) {
                    jsonArray = responseJSON.getJSONArray("tareas");
                    boolean carpetaExiste = false;
                    String rutaGuardar = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            .getAbsolutePath() + "/nsagenda";
                    File file = new File(rutaGuardar);

                    if(!file.exists()) {
                        carpetaExiste = file.mkdirs();
                    }
                    else {
                        carpetaExiste = true;
                    }

                    if(carpetaExiste) {
                        LocalDateTime momentoActual = LocalDateTime.now();
                        String archivo = rutaGuardar + "/"
                                + momentoActual.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                                + ".txt";
                        printWriter = new PrintWriter(archivo);

                        printWriter.println("\n /  Fecha de exportación: "
                                + momentoActual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                                + "  /\n");

                        for(int i=0; i<jsonArray.length(); i++) {
                            String fechaAdaptada = LocalDateTime
                                    .parse(jsonArray.getJSONObject(i).getString("fecha"),
                                            DateTimeFormatter.ofPattern("yyyy-M-d H:m:s"))
                                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                            printWriter.println("\\");
                            printWriter.println(" \\  " + jsonArray.getJSONObject(i).getString("nombre"));
                            printWriter.println("  \\______________________________________");
                            printWriter.println("  |");
                            printWriter.println("  | " + jsonArray.getJSONObject(i).getString("descripcion"));
                            printWriter.println("  |");
                            printWriter.println("  | (" + fechaAdaptada + ")\n" );
                        }

                        estado = 4;
                    }
                    else {
                        estado = 3;
                    }
                }
                else {
                    estado = 2;
                }

                response.close();
            }
            catch (JSONException jsone) {
                Log.e("--ERROR--", "Error al parsear JSON");
            }
            catch(IOException ioe) {
                Log.e("--ERROR--", "Error al realizar petición");
            }
            catch (Exception e) {
                Log.e("--ERROR--", "Error general");
            }
            finally {
                if(printWriter != null)
                    printWriter.close();
            }

            return estado;
        }

        @Override
        protected void onPostExecute(Integer estado) {

            String mensaje = "";

            switch (estado) {
                case 1:     //  Sin acceso a almacenamiento externo
                    mensaje = "No es posible acceder al almacenamiento externo";
                    break;

                case 2:     //  No hay tareas a exportar
                    mensaje = "No hay tareas que exportar";
                    break;

                case 3:     //  Error al crear ruta para guardar archivo
                    mensaje = "No se ha podido crear el archivo de tareas";
                    break;

                case 4:     //  Tareas exportadas correctamente
                    mensaje = "¡Tareas guardadas en carpeta Descargas/nsagenda!";
                    break;
            }

            if(!mensaje.isEmpty()) {
                Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
            }
        }
    }
}
