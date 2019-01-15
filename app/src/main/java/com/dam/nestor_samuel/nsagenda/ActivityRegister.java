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
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ActivityRegister extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;

    @BindView(R.id.aRegister_et_nombre)
    EditText et_nombre;
    @BindView(R.id.aRegister_et_apellidos)
    EditText et_apellidos;
    @BindView(R.id.aRegister_et_nick)
    EditText et_nick;
    @BindView(R.id.aRegister_et_email)
    EditText et_email;
    @BindView(R.id.aRegister_et_password)
    EditText et_password;
    @BindView(R.id.aRegister_et_confirmarPassword)
    EditText et_confirmarPassword;
    @BindView(R.id.aRegister_btn_registrar)
    Button btn_registrar;
    @BindViews({
            R.id.aRegister_et_nombre,
            R.id.aRegister_et_apellidos,
            R.id.aRegister_et_nick,
            R.id.aRegister_et_email,
            R.id.aRegister_et_password,
            R.id.aRegister_et_confirmarPassword})
    List<EditText> campos;

    @OnClick(R.id.aRegister_btn_registrar) void crearCuenta() {
        if(verificarCampos()) {
            btn_registrar.setEnabled(false);
            mostrarRuedaProgreso("Verificando nick...");
            new CheckNick().execute(et_nick.getText().toString());
        }
    }

    @OnClick(R.id.aRegister_tv_login) void iniciarSesion() {
        Intent intent = new Intent(this, ActivityLogin.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private boolean verificarCampos() {

        boolean camposCorrectos = true;
        String password;
        String confirmarPassword;

        //  Comprobar si campos están vacios
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

        password = et_password.getText().toString();
        confirmarPassword = et_confirmarPassword.getText().toString();

        //  Comprobar contraseñas coinciden
        if(!password.isEmpty() && !confirmarPassword.isEmpty()) {
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

        progressDialog = new ProgressDialog(ActivityRegister.this,
                R.style.AppTheme_Dark_Dialog);
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
                Log.e("--ERROR--", "Error al parsear JSON");
            }
            catch(IOException ioe) {
                Log.e("--ERROR--", "Error al realizar petición");
            }

            return nickExiste;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            progressDialog.dismiss();

            if(aBoolean) {
                et_nick.setError("El nombre de usuario ya existe");
                btn_registrar.setEnabled(true);
            }
            else {
                mostrarRuedaProgreso("Creando cuenta...");
                new CrearCuenta().execute(
                        et_nombre.getText().toString(),
                        et_apellidos.getText().toString(),
                        et_nick.getText().toString(),
                        et_email.getText().toString(),
                        et_password.getText().toString());
            }

        }
    }

    public class CrearCuenta extends AsyncTask<String, Void, Boolean> {

        OkHttpClient client;

        final String URL = ServicioWeb.PAGINA_BASE + "insertar_usuario.php";
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        @Override
        protected Boolean doInBackground(String... strings) {

            boolean usuarioCreado = false;
            JSONObject jsonObject;      // Objeto JSON con los datos de los campos
            RequestBody body;           // Cuerpo de la petición con los datos
            Request request;            // Petición a la página web
            Response response;          // Respuesta del servidor
            JSONObject responseJSON;    // Objeto JSON con los datos recogidos del servidor

            try {
                client = new OkHttpClient();

                jsonObject = new JSONObject();
                jsonObject.put("nombre", strings[0]);
                jsonObject.put("apellidos", strings[1]);
                jsonObject.put("nick", strings[2]);
                jsonObject.put("email", strings[3]);
                jsonObject.put("password", passwordToMD5(strings[4]));

                body = RequestBody.create(JSON, jsonObject.toString());
                request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();

                response = client.newCall(request).execute();
                responseJSON = new JSONObject(response.body().string());

                usuarioCreado = responseJSON.getBoolean("estado");

                response.close();
            }
            catch (JSONException jsone) {
                Log.e("--ERROR--", "Error al parsear JSON");
            }
            catch(IOException ioe) {
                Log.e("--ERROR--", "Error al realizar petición");
            }

            return usuarioCreado;

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
                Log.e("--ERROR--", "Algoritmo de encriptación incorrecto");
            }

            return md5Password;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            progressDialog.dismiss();

            if(aBoolean) {
                mostrarRuedaProgreso("Iniciando sesión...");
                new CheckLogin().execute(et_nick.getText().toString(),
                        passwordToMD5(et_password.getText().toString()));
            }
            else {
                btn_registrar.setEnabled(true);
                Toast.makeText(getBaseContext(), "Error al crear cuenta", Toast.LENGTH_LONG).show();
            }

        }
    }

    public class CheckLogin extends AsyncTask<String, Void, Boolean> {

        OkHttpClient client;
        Usuario usuario;
        String md5Password;

        final String URL = ServicioWeb.PAGINA_BASE + "acceder.php";
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        @Override
        protected Boolean doInBackground(String... params) {

            boolean loginCorrecto = false;
            JSONObject jsonObject;      // Objeto JSON con los datos de los campos
            RequestBody body;           // Cuerpo de la petición con los datos
            Request request;            // Petición a la página web
            Response response;          // Respuesta del servidor
            JSONObject responseJSON;    // Objeto JSON con los datos recogidos del servidor

            md5Password = params[1];

            try {
                client = new OkHttpClient();

                jsonObject = new JSONObject();
                jsonObject.put("nick", params[0]);
                jsonObject.put("password", md5Password);

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
                Log.e("--ERROR--", "Error al parsear JSON");
            }
            catch(IOException ioe) {
                Log.e("--ERROR--", "Error al realizar petición");
            }

            return loginCorrecto;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            progressDialog.dismiss();

            if(aBoolean) {
                sharedPreferences.edit().putString("nombre", usuario.getNombre()).apply();
                sharedPreferences.edit().putString("apellidos", usuario.getApellidos()).apply();
                sharedPreferences.edit().putString("nick", usuario.getNick()).apply();
                sharedPreferences.edit().putString("email", usuario.getEmail()).apply();
                sharedPreferences.edit().putString("password", md5Password).apply();

                Intent intent = new Intent(ActivityRegister.this, ActivityMain.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("Usuario", usuario);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
            else {
                Toast.makeText(getBaseContext(), "No se pudo iniciar sesión", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ActivityRegister.this, ActivityLogin.class);
                startActivity(intent);
                finish();
            }

        }
    }
}
