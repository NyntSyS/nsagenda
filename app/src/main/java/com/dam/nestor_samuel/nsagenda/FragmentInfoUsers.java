package com.dam.nestor_samuel.nsagenda;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FragmentInfoUsers extends Fragment {

    private OnFragmentInteractionListener mListener;
    private LinearLayout linearLayout;      //  Layout con los contactos
    private ProgressDialog progressDialog;

    public FragmentInfoUsers() {
        // Required empty public constructor
    }

    public static FragmentInfoUsers newInstance() {

        FragmentInfoUsers fragment = new FragmentInfoUsers();

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info_users, container, false);

        linearLayout = view.findViewById(R.id.fInfoUsers_linearLayout);
        mostrarRuedaProgreso("Buscando contactos...");
        new GetContacts().execute();

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

    private void mostrarRuedaProgreso(String mensaje) {

        progressDialog = new ProgressDialog(getContext(), R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(mensaje);
        progressDialog.show();

    }

    private class GetContacts extends AsyncTask<Void, Void, HashMap<String, String>> {

        OkHttpClient client;

        final String URL = ServicioWeb.PAGINA_BASE + "leer_contactos.php";
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        @Override
        protected HashMap<String, String> doInBackground(Void... voids) {

            JSONObject jsonObject;      // Objeto JSON con los datos de los campos
            JSONArray jsonContactos;    // Array JSON con la lista de contactos
            RequestBody body;           // Cuerpo de la petición con los datos
            Request request;            // Petición a la página web
            Response response;          // Respuesta del servidor
            JSONObject responseJSON;    // Objeto JSON con los datos recogidos del servidor
            JSONArray jsonArray;        // Array JSON con los contactos
            TelephonyManager tManager;  // Objeto para obtener información del teléfono del usuario
            Cursor contactos;           // Cursor para recorrer la lista de contactos

            List<String> contactosEnBD = new ArrayList<>();
            HashMap<String, String> listaContactos = new HashMap<>();

            try {
                client = new OkHttpClient();

                tManager = (TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);
                String imei = tManager.getDeviceId();
                String telefono = tManager.getLine1Number();

                if(telefono != null && !telefono.isEmpty() &&
                        imei != null && !imei.isEmpty()) {

                    jsonObject = new JSONObject();
                    jsonObject.put("imei", imei);
                    jsonObject.put("telefono", telefono);

                    body = RequestBody.create(JSON, jsonObject.toString());
                    request = new Request.Builder()
                            .url(URL)
                            .post(body)
                            .build();

                    response = client.newCall(request).execute();
                    responseJSON = new JSONObject(response.body().string());

                    if(responseJSON.getBoolean("estado") == true) {
                        jsonArray = responseJSON.getJSONArray("contactos");
                        for(int i=0; i<jsonArray.length(); i++) {
                            contactosEnBD.add(jsonArray.getJSONObject(i).getString("telefono_contacto"));
                        }

                        contactos = getActivity().getContentResolver()
                                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,null,null, null);

                        while (contactos.moveToNext()) {
                            String nombre = contactos.getString(contactos
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            String numero = contactos.getString(contactos
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                            //  Para ver qué contactos actuales han usado alguna vez la aplicación
                            if(contactosEnBD.contains(numero)) {
                                listaContactos.put(numero, nombre);
                            }
                        }

                        contactos.close();
                    }

                    response.close();

                }
                else {
                    listaContactos.put("-1", "");
                }

            }
            catch (JSONException jsone) {
                Log.e("--ERROR--", "Error al parsear JSON");
            }
            catch(IOException ioe) {
                Log.e("--ERROR--", "Error al realizar petición");
            }
            catch (SecurityException se) {
                Log.e("--ERROR--", "Error al obtener información del teléfono");
            }

            return listaContactos;
        }

        @Override
        protected void onPostExecute(HashMap<String, String> listaContactos) {

            progressDialog.dismiss();

            if(listaContactos.size() == 0) {                //  Ningún contacto tiene la aplicación instalada
                TextView sinContactos = new TextView(getContext());
                sinContactos.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                sinContactos.setTextColor(Color.GRAY);
                sinContactos.setText("Ningún contacto tiene la aplicación instalada");
                sinContactos.setTextSize(28);
                sinContactos.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                linearLayout.addView(sinContactos);
            }
            else if(listaContactos.containsKey("-1")) {     //  No se puede leer la información del dispositivo
                TextView errorLeerDatos = new TextView(getContext());
                errorLeerDatos.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                errorLeerDatos.setTextColor(Color.GRAY);
                errorLeerDatos.setText("No se puede leer información de este dispositivo");
                errorLeerDatos.setTextSize(28);
                errorLeerDatos.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                linearLayout.addView(errorLeerDatos);
            }
            else {                                          //  Hay contactos con la aplicación instalada
                for(Map.Entry<String, String> contacto : listaContactos.entrySet()) {

                    CardView cardView = new CardView(getContext());
                    cardView.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));

                    RelativeLayout relativeLayout = new RelativeLayout(getContext());
                    relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));

                    ImageView imagenContacto = new ImageView(getContext());
                    imagenContacto.setLayoutParams(new ViewGroup.LayoutParams(
                            convertirDpPx(90f), convertirDpPx(75f)
                    ));
                    int paddingPx = convertirDpPx(4f);
                    imagenContacto.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                    imagenContacto.setImageResource(R.mipmap.imagen_contacto);
                    imagenContacto.setScaleType(ImageView.ScaleType.FIT_CENTER);

                    TextView nombreContacto = new TextView(getContext());
                    nombreContacto.setLayoutParams(new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                    ));
                    RelativeLayout.LayoutParams nc_lp = (RelativeLayout.LayoutParams) nombreContacto.getLayoutParams();
                    nc_lp.addRule(RelativeLayout.RIGHT_OF, imagenContacto.getId());
                    nc_lp.setMargins(convertirDpPx(80f), convertirDpPx(25f), 0, 0);
                    nombreContacto.setLayoutParams(nc_lp);
                    nombreContacto.setTextColor(Color.BLACK);
                    nombreContacto.setTextSize(20f);
                    nombreContacto.setText(contacto.getValue());    //  contacto.getKey() para el nº de teléfono

                    relativeLayout.addView(imagenContacto);
                    relativeLayout.addView(nombreContacto);
                    cardView.addView(relativeLayout);
                    linearLayout.addView(cardView);

                }

            }

        }

        //  Para mantener la proporción en distintos dispositivos
        private int convertirDpPx(float dp) {

            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            float fPixels = metrics.density * dp;

            return (int) (fPixels + 0.5f);

        }
    }
}
