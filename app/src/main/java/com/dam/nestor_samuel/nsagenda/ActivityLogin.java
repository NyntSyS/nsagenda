package com.dam.nestor_samuel.nsagenda;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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
    private SharedPreferences sharedPreferences;

    @BindView(R.id.aLogin_et_usuario)
    EditText et_usuario;
    @BindView(R.id.aLogin_et_password)
    EditText et_password;
    @BindView(R.id.aLogin_btn_login)
    Button btn_login;

    @OnClick(R.id.aLogin_btn_login) void login() {
        if(verificarCampos()) {
            btn_login.setEnabled(false);
            mostrarRuedaProgreso("Iniciando sesión...");
            new CheckLogin().execute(et_usuario.getText().toString(),
                    et_password.getText().toString());
        }
    }

    @OnClick(R.id.aLogin_tv_crear) void crearCuenta() {
        Intent intent = new Intent(this, ActivityRegister.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private boolean verificarCampos() {

        boolean camposCorrectos = true;

        //  Comprobar nombre de usuario vacio
        if(et_usuario.getText().toString().isEmpty()) {
            et_usuario.setError("El campo no puede estar vacio");
            camposCorrectos = false;
        }
        else {
            et_usuario.setError(null);
        }

        //  Comprobar contraseña vacia
        if(et_password.getText().toString().isEmpty()) {
            et_password.setError("El campo no puede estar vacio");
            camposCorrectos = false;
        }
        else {
            et_password.setError(null);
        }

        return camposCorrectos;

    }

    private void mostrarRuedaProgreso(String mensaje) {

        progressDialog = new ProgressDialog(ActivityLogin.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(mensaje);
        progressDialog.show();

    }

    public class CheckLogin extends AsyncTask<String, Void, Boolean> {

        OkHttpClient client;
        Usuario usuario;

        final String URL = "https://nesdam2018.000webhostapp.com/acceder.php";
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        @Override
        protected Boolean doInBackground(String... params) {

            boolean loginCorrecto = false;
            JSONObject jsonObject;      // Objeto JSON con los datos de los campos
            RequestBody body;           // Cuerpo de la petición con los datos
            Request request;            // Petición a la página web
            Response response;          // Respuesta del servidor
            JSONObject responseJSON;    // Objeto JSON con los datos recogidos del servidor

            try {
                client = new OkHttpClient();

                jsonObject = new JSONObject();
                jsonObject.put("nick", params[0]);
                jsonObject.put("password", passwordToMD5(params[1]));

                body = RequestBody.create(JSON, jsonObject.toString());
                request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();

                response = client.newCall(request).execute();
                responseJSON = new JSONObject(response.body().string());

                if(responseJSON.getBoolean("estado") == true) {
                    loginCorrecto = true;
                    usuario = new Usuario(responseJSON.getJSONObject("usuario").getInt("id"),
                            responseJSON.getJSONObject("usuario").getString("nick"),
                            responseJSON.getJSONObject("usuario").getString("email"),
                            responseJSON.getJSONObject("usuario").getString("nombre"),
                            responseJSON.getJSONObject("usuario").getString("apellidos"));
                }
                else {
                    usuario = null;
                }

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

            progressDialog.dismiss();

            if(aBoolean) {
                sharedPreferences.edit().putString("nombre", usuario.getNombre()).apply();
                sharedPreferences.edit().putString("apellidos", usuario.getApellidos()).apply();
                sharedPreferences.edit().putString("nick", usuario.getNick()).apply();
                sharedPreferences.edit().putString("email", usuario.getEmail()).apply();
                sharedPreferences.edit().putString("password",
                        passwordToMD5(et_password.getText().toString())).apply();

                Intent intent = new Intent(ActivityLogin.this, ActivityMain.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("Usuario", usuario);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
            else {
                btn_login.setEnabled(true);
                Toast.makeText(getBaseContext(), "Acceso incorrecto", Toast.LENGTH_LONG).show();
            }

        }
    }
}
