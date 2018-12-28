package com.dam.nestor_samuel.nsagenda;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ActivityLogin extends AppCompatActivity {

    private ProgressDialog progressDialog;

    @BindView(R.id.et_usuario)
    EditText et_usuario;

    @BindView(R.id.et_password)
    EditText et_password;

    @BindView(R.id.btn_login)
    Button btn_login;

    @OnClick(R.id.btn_login) void login() {
        if(verificarCampos()) {
            btn_login.setEnabled(false);
            mostrarRuedaProgreso();
            new CheckLogin().execute(et_usuario.getText().toString(),
                    et_password.getText().toString());
            //  TODO: llamar clase asíncrona de login
        }
    }

    @OnClick(R.id.tv_crear) void crearCuenta() {
        //  TODO: cambiar a activity de crear usuario antes de cerrar ésta
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    private boolean verificarCampos() {

        boolean camposValidos = true;
        String usuario = et_usuario.getText().toString();
        String password = et_password.getText().toString();

        if(usuario.isEmpty()) {
            et_usuario.setError("El campo no puede estar vacio");
            camposValidos = false;
        }
        else {
            et_usuario.setError(null);
        }

        if(password.isEmpty()) {
            et_password.setError("El campo no puede estar vacio");
            camposValidos = false;
        }
        else {
            et_password.setError(null);
        }

        return camposValidos;

    }

    private void mostrarRuedaProgreso() {

        progressDialog = new ProgressDialog(ActivityLogin.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Autentificando...");
        progressDialog.show();

    }

    public class CheckLogin extends AsyncTask<String, Void, Boolean> {

        OkHttpClient client;
        String datos;

        final String URL = "https://nesdam2018.000webhostapp.com/acceder.php";
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        @Override
        protected Boolean doInBackground(String... params) {

            boolean loginCorrecto = false;
            JSONObject jsonObject;  // Objeto JSON con los datos de los campos
            RequestBody body;       // Cuerpo de la petición con los datos
            Request request;        // Petición a la página web
            Response response;      // Respuesta del servidor

            client = new OkHttpClient();

            try {
                jsonObject = new JSONObject();
                jsonObject.put("nick", params[0]);
                jsonObject.put("password", passwordToMD5(params[1]));

                body = RequestBody.create(JSON, jsonObject.toString());
                request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();

                response = client.newCall(request).execute();
                Log.e("--ERROR pero no--", response.body().string());

                loginCorrecto = true;

                response.close();
            }
            catch (JSONException jsone) {
                Log.e("--ERROR--", "Error al parsear JSON");    //  Borrar más adelante
            }
            catch(IOException ioe) {
                Log.e("--ERROR--", "Error al realizar petición");    //  Borrar más adelante
            }

            return loginCorrecto;
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

            btn_login.setEnabled(true);
            progressDialog.dismiss();
            client = new OkHttpClient();


            if(aBoolean) {

            }
            else {
                Toast.makeText(getBaseContext(), "Acceso incorrecto", Toast.LENGTH_LONG).show();
            }

        }
    }
}
