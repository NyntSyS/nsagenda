package com.dam.nestor_samuel.nsagenda;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
/*import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;*/
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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
}
