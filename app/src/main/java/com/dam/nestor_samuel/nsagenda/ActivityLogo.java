package com.dam.nestor_samuel.nsagenda;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ActivityLogo extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;

    private static final int MULTIPLE_PERMISSIONS = 1;
    private static final String[] PERMISOS = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @BindView(R.id.aLogo_iv_logo)
    ImageView iv_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        AndroidThreeTen.init(this);
        ButterKnife.bind(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            comprobarPermisos();
        }
        else {
            iniciarPrograma();
        }

    }

    private void comprobarPermisos() {

        int resultado;

        List<String> permisosPendientes = new ArrayList<>();

        for(String permiso : PERMISOS) {
            resultado = ContextCompat.checkSelfPermission(this, permiso);

            if(resultado != PackageManager.PERMISSION_GRANTED) {
                permisosPendientes.add(permiso);
            }
        }

        if(permisosPendientes.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    permisosPendientes.toArray(new String[0]), MULTIPLE_PERMISSIONS);
        }
        else {
            iniciarPrograma();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        boolean permisosDados = true;

        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:
                if(grantResults.length > 0) {
                    for(int resultadoPermiso : grantResults) {
                        if(resultadoPermiso == PackageManager.PERMISSION_DENIED) {
                            permisosDados = false;
                        }
                    }

                    if(permisosDados) {
                        iniciarPrograma();
                    }
                    else {
                        Toast.makeText(this, "Permiso denegado", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                break;
        }
    }

    private void iniciarPrograma() {

        int duracion = 2000;    //  Duración del logo

        if(sharedPreferences.getBoolean("ocultarLogo", false)) {
            duracion = 0;
            iv_logo.setVisibility(View.INVISIBLE);
        }

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(sharedPreferences.getBoolean("firstRun", true)) {
                    sharedPreferences.edit().putBoolean("firstRun", false).apply();

                    Intent intent = new Intent(ActivityLogo.this, ActivityRegister.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    if(sharedPreferences.getBoolean("loginAutomatico", false)) {
                        String nick = sharedPreferences.getString("nick", "");
                        String password = sharedPreferences.getString("password", "");

                        mostrarRuedaProgreso("Iniciando sesión...");
                        new CheckLogin().execute(nick, password);
                    }
                    else {
                        Intent intent = new Intent(ActivityLogo.this, ActivityLogin.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        }, duracion);

    }

    private void mostrarRuedaProgreso(String mensaje) {

        progressDialog = new ProgressDialog(ActivityLogo.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(mensaje);
        progressDialog.show();

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
                Log.e("--ERROR--", "Error al parsear JSON");    //  Borrar más adelante
            }
            catch(IOException ioe) {
                Log.e("--ERROR--", "Error al realizar petición");    //  Borrar más adelante
            }

            return loginCorrecto;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            progressDialog.dismiss();

            if(aBoolean) {
                sharedPreferences.edit().putString("nombre", usuario.getNombre()).apply();
                sharedPreferences.edit().putString("apellidos", usuario.getApellidos()).apply();
                sharedPreferences.edit().putString("email", usuario.getEmail()).apply();

                Intent intent = new Intent(ActivityLogo.this, ActivityMain.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("Usuario", usuario);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
            else {
                Toast.makeText(getBaseContext(), "No se pudo iniciar sesión", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ActivityLogo.this, ActivityLogin.class);
                startActivity(intent);
                finish();
            }

        }
    }
}
