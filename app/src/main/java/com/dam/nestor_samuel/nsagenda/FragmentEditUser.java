package com.dam.nestor_samuel.nsagenda;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FragmentEditUser extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Usuario usuario;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;

    private EditText et_nombre;
    private EditText et_apellidos;
    private EditText et_nick;
    private EditText et_email;
    private EditText et_password;
    private EditText et_confirmarPassword;
    private Button btn_modificar;
    private List<EditText> campos;

    public FragmentEditUser() {
        // Required empty public constructor
    }

    public static FragmentEditUser newInstance(Usuario usuario) {
        FragmentEditUser fragment = new FragmentEditUser();
        Bundle args = new Bundle();
        args.putParcelable("Usuario", usuario);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            usuario = getArguments().getParcelable("Usuario");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_user, container, false);

        et_nombre = view.findViewById(R.id.fEditUser_et_nombre);
        et_apellidos = view.findViewById(R.id.fEditUser_et_apellidos);
        et_nick = view.findViewById(R.id.fEditUser_et_nick);
        et_email = view.findViewById(R.id.fEditUser_et_email);
        et_password = view.findViewById(R.id.fEditUser_et_password);
        et_confirmarPassword = view.findViewById(R.id.fEditUser_et_confirmarPassword);
        btn_modificar = view.findViewById(R.id.fEditUser_btn_modificar);
        campos = Arrays.asList(et_nombre, et_apellidos, et_nick, et_email);

        et_nombre.setText(usuario.getNombre());
        et_apellidos.setText(usuario.getApellidos());
        et_nick.setText(usuario.getNick());
        et_email.setText(usuario.getEmail());

        btn_modificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verificarCampos()) {
                    btn_modificar.setEnabled(false);


                    //  Si no ha cambiado el nick no hace falta comprobarlo
                    if(usuario.getNick().equals(et_nick.getText().toString())) {
                        mostrarRuedaProgreso("Actualizando datos...");
                        new ActualizarUsuario().execute(new Usuario(
                                usuario.getId(),
                                et_nick.getText().toString(),
                                et_email.getText().toString(),
                                et_nombre.getText().toString(),
                                et_apellidos.getText().toString(),
                                et_password.getText().toString()
                        ));
                    }
                    else {
                        mostrarRuedaProgreso("Verificando nick...");
                        new CheckNick().execute(et_nick.getText().toString());
                    }
                }
            }
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());

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

    private boolean verificarCampos() {

        boolean camposCorrectos = true;
        String password = et_password.getText().toString().trim();
        String confirmarPassword = et_confirmarPassword.getText().toString().trim();

        //  Comprobar contraseñas coinciden
        for(EditText et : campos) {
            et.setText(et.getText().toString().trim());
            if(et.getText().toString().isEmpty()) {
                et.setError("El campo no puede estar vacio");
                camposCorrectos = false;
            }
            else {
                et.setError(null);
            }
        }

        et_password.setText(et_password.getText().toString().trim());
        et_confirmarPassword.setText(et_confirmarPassword.getText().toString().trim());

        password = et_password.getText().toString();
        confirmarPassword = et_confirmarPassword.getText().toString();

        //  Comprobar contraseñas coinciden
        if(!password.isEmpty() || !confirmarPassword.isEmpty()) {
            if(!password.equals(confirmarPassword)) {
                et_confirmarPassword.setError("Las contraseñas no coinciden");
                camposCorrectos = false;
            }
            else {
                et_confirmarPassword.setError(null);
            }
        }

        return camposCorrectos;

    }

    private void mostrarRuedaProgreso(String mensaje) {

        progressDialog = new ProgressDialog(getContext(), R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(mensaje);
        progressDialog.show();

    }

    public class CheckNick extends AsyncTask<String, Void, Boolean> {

        OkHttpClient client;

        final String URL = ServicioWeb.PAGINA_BASE + "verificar_nick.php";
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        @Override
        protected Boolean doInBackground(String... strings) {

            boolean nickExiste = true;
            JSONObject jsonObject;      // Objeto JSON con el nombre de usuario
            RequestBody body;           // Cuerpo de la petición con los datos
            Request request;            // Petición a la página web
            Response response;          // Respuesta del servidor
            JSONObject responseJSON;    // Objeto JSON con los datos recogidos del servidor

            try {
                client = new OkHttpClient();

                jsonObject = new JSONObject();
                jsonObject.put("nick", strings[0]);

                body = RequestBody.create(JSON, jsonObject.toString());
                request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();

                response = client.newCall(request).execute();
                responseJSON = new JSONObject(response.body().string());
                nickExiste = responseJSON.getBoolean("estado");

                response.close();
            }
            catch (JSONException jsone) {
                Log.e("--ERROR--", "Error al parsear JSON");    //  Borrar más adelante
            }
            catch(IOException ioe) {
                Log.e("--ERROR--", "Error al realizar petición");    //  Borrar más adelante
            }

            return nickExiste;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            progressDialog.dismiss();

            if(aBoolean) {
                et_nick.setError("El nombre de usuario ya existe");
                btn_modificar.setEnabled(true);
            }
            else {
                mostrarRuedaProgreso("Actualizando datos...");
                new ActualizarUsuario().execute(new Usuario(
                        usuario.getId(),
                        et_nick.getText().toString(),
                        et_email.getText().toString(),
                        et_nombre.getText().toString(),
                        et_apellidos.getText().toString(),
                        et_password.getText().toString()
                ));
            }

        }
    }

    public class ActualizarUsuario extends AsyncTask<Usuario, Void, Boolean> {

        OkHttpClient client;

        final String URL = ServicioWeb.PAGINA_BASE + "modificar_usuario.php";
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        @Override
        protected Boolean doInBackground(Usuario... usuarios) {

            boolean usuarioActualizado = false;
            JSONObject jsonObject;      // Objeto JSON con los datos de los campos
            RequestBody body;           // Cuerpo de la petición con los datos
            Request request;            // Petición a la página web
            Response response;          // Respuesta del servidor
            JSONObject responseJSON;    // Objeto JSON con los datos recogidos del servidor

            try {
                client = new OkHttpClient();

                jsonObject = new JSONObject();
                jsonObject.put("id", usuarios[0].getId());
                jsonObject.put("nick", usuarios[0].getNick());
                jsonObject.put("email", usuarios[0].getEmail());
                jsonObject.put("nombre", usuarios[0].getNombre());
                jsonObject.put("apellidos", usuarios[0].getApellidos());
                if(usuarios[0].getPassword().isEmpty())
                    jsonObject.put("password", "");
                else
                    jsonObject.put("password", passwordToMD5(usuarios[0].getPassword()));

                body = RequestBody.create(JSON, jsonObject.toString());
                request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();

                response = client.newCall(request).execute();
                responseJSON = new JSONObject(response.body().string());

                usuarioActualizado = responseJSON.getBoolean("estado");

                if(usuarioActualizado) {
                    if(usuarios[0].getPassword().isEmpty())
                        usuario.copiarUsuario(usuarios[0], "");
                    else
                        usuario.copiarUsuario(usuarios[0], passwordToMD5(usuarios[0].getPassword()));

                    sharedPreferences.edit().putString("nombre", usuario.getNombre()).apply();
                    sharedPreferences.edit().putString("apellidos", usuario.getApellidos()).apply();
                    sharedPreferences.edit().putString("nick", usuario.getNick()).apply();
                    sharedPreferences.edit().putString("email", usuario.getEmail()).apply();
                    sharedPreferences.edit().putString("password", usuario.getPassword()).apply();
                }

                response.close();
            }
            catch (JSONException jsone) {
                Log.e("--ERROR--", "Error al parsear JSON");    //  Borrar más adelante
            }
            catch(IOException ioe) {
                Log.e("--ERROR--", "Error al realizar petición");    //  Borrar más adelante
            }

            return usuarioActualizado;

        }

        private String passwordToMD5(String password) {

            String md5Password = "";

            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] messageDigest = md.digest(password.getBytes());
                BigInteger number = new BigInteger(1, messageDigest);
                md5Password = number.toString(16);
            }
            catch (NoSuchAlgorithmException nsae) {
                Log.e("--ERROR--", "Algoritmo de encriptación incorrecto"); //  Borrar más adelante
            }

            return md5Password;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            progressDialog.dismiss();

            if(aBoolean) {
                TextView cabeceraNombre = (TextView)getActivity().findViewById(R.id.aMain_tv_nombreUsuario);
                TextView cabeceraEmail = (TextView)getActivity().findViewById(R.id.aMain_tv_emailUsuario);
                cabeceraNombre.setText(usuario.getNombre() + " " + usuario.getApellidos());
                cabeceraEmail.setText(usuario.getEmail());

                Toast.makeText(getContext(), "¡Datos actualizados!", Toast.LENGTH_LONG).show();

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(((ViewGroup)getView().getParent()).getId(),
                                FragmentShowTasks.newInstance(usuario.getId()))
                        .addToBackStack(null)
                        .commit();
            }
            else {
                btn_modificar.setEnabled(true);
                Toast.makeText(getContext(), "Error al crear cuenta", Toast.LENGTH_LONG).show();
            }

        }
    }


}
