package com.dam.nestor_samuel.nsagenda;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.format.DateTimeFormatter;

public class ActivityLogo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        AndroidThreeTen.init(this);
        Tarea tarea = new Tarea("test1","test1");
        Log.e("--mensaje",tarea.getFechaTarea().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
}
