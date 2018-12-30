package com.dam.nestor_samuel.nsagenda;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
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

    @BindView(R.id.aLogo_iv_logo)
    ImageView iv_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        //  TODO: verificar permisos

        int duracion = 2000;

        AndroidThreeTen.init(this);
        ButterKnife.bind(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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
