package com.dam.nestor_samuel.nsagenda;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.llollox.androidtoggleswitch.widgets.MultipleToggleSwitch;

import java.util.Arrays;
import java.util.List;

public class ActivityMain extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        FragmentNewTask.OnFragmentInteractionListener,
        FragmentModifyTask.OnFragmentInteractionListener,
        FragmentMapbox.OnFragmentInteractionListener,
        FragmentShowTasks.OnFragmentInteractionListener,
        FragmentGames.OnFragmentInteractionListener,
        FragmentInfoUsers.OnFragmentInteractionListener {

    private Usuario usuario;


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
        navigationView.setNavigationItemSelectedListener(this);

        Bundle bundle = getIntent().getExtras();
        usuario = bundle.getParcelable("Usuario");

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
        } else {
            super.onBackPressed();
        }
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
        else if (id == R.id.modificarTarea) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, FragmentModifyTask.newInstance(usuario.getId()))
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
