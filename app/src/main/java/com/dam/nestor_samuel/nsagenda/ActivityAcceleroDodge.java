package com.dam.nestor_samuel.nsagenda;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ActivityAcceleroDodge extends AppCompatActivity implements SensorEventListener {

    private float xPosition, xAcceleration, xVelocity = 0.0f;
    private float yPosition, yAcceleration, yVelocity = 0.0f;
    private float xMax, yMax;
    private float frameTime = 0.666f;

    private int vidas;
    private int puntuacion;
    private boolean jugando;
    private int diametroBola;           //  Basado en ancho de pantalla
    private int diametroBolaEnemiga;    //  Basado en ancho de pantalla
    private int margenHorizontal;
    private int margenVertical;
    private long intervaloCrearEnemigos;
    private float velocidadEnemigos;    //  Basado en ancho de pantalla
    private Ball ball;

    private final static int DIAMETRO_BOLA_BASE = 60;
    private final static int ANCHO_PANTALLA_BASE = 1080;
    private final static int ALTO_PANTALLA_BASE = 1920;
    private final static float PORCENTAJE_MARGENES = 0.10f;
    private final static long INTERVALO_INICIAL = 3000;
    private final static long INTERVALO_MINIMO = 600;
    private final static float VELOCIDAD_INICIAL_BASE = 10f;

    private Bitmap ballBitmap;
    private Bitmap enemyBitmap;
    private SensorManager sensorManager = null;

    private boolean mostrarDialogoFin;
    private boolean saliendo;
    private boolean musicaDesactivada;
    private MediaPlayer mediaPlayer;
    private int sonido;
    private SoundPool soundPool;
    private SharedPreferences sharedPreferences;
    private String nick;
    private SQLiteDatabase db;
    private DatabaseAcceleroDodge database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_accelero_dodge);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        musicaDesactivada = sharedPreferences.getBoolean("musicaJuego", false);

        Bundle bundle = getIntent().getExtras();
        nick = bundle.getString("Nick");

        mostrarDialogoFin = false;
        saliendo = false;

        database = new DatabaseAcceleroDodge(this, "Records", null, 1);
        db = database.getWritableDatabase();

        //  Hacer pantalla completa y posición Portrait fija
        configurarFlagsPantalla();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //  Calcular resolucion
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        xMax = (float)size.x - ((DIAMETRO_BOLA_BASE * size.x) / ANCHO_PANTALLA_BASE);   //  Ancho de la pantalla de la bola
        yMax = (float)size.y - ((DIAMETRO_BOLA_BASE * size.y) / ALTO_PANTALLA_BASE);    //  Alto de la pantalla de la bola
        margenHorizontal = (int) (xMax*PORCENTAJE_MARGENES);
        margenVertical = (int) (yMax*PORCENTAJE_MARGENES);

        //  Referencia al SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);

        cargarImagenesObjetos();
        crearPartida();

    }

    private void configurarFlagsPantalla() {

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);


    }

    private void crearPartida() {

        //  Configuración de la bola
        xPosition = xMax/2;     //  Posición horizontal centrada inicialmente
        yPosition = yMax/2;     //  Posición vertical centrada inicialmente
        ball = new Ball(this);
        setContentView(ball);

        vidas = 3;
        puntuacion = 0;
        intervaloCrearEnemigos = INTERVALO_INICIAL;
        velocidadEnemigos = (VELOCIDAD_INICIAL_BASE*xMax) / ANCHO_PANTALLA_BASE;

        final Handler crearEnemigos = new Handler();
        final Handler reducirIntervaloAparicion = new Handler();
        final Handler incrementarVelocidadEnemigo = new Handler();

        crearEnemigos.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(jugando) {
                    ball.addEnemyBall(new EnemyBall(velocidadEnemigos));
                    crearEnemigos.postDelayed(this, intervaloCrearEnemigos);
                }
            }
        }, intervaloCrearEnemigos);

        reducirIntervaloAparicion.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(jugando && intervaloCrearEnemigos > INTERVALO_MINIMO) {
                    intervaloCrearEnemigos -= 200;
                    reducirIntervaloAparicion.postDelayed(this, 2000);
                }
            }
        }, 2000);

        incrementarVelocidadEnemigo.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(jugando) {
                    velocidadEnemigos += 0.5f;
                    incrementarVelocidadEnemigo.postDelayed(this, 3000);
                }
            }
        }, 3000);

        jugando = true;

        if(!musicaDesactivada) {
            soundPool = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
            sonido = soundPool.load(this, R.raw.impact, 1);
            cargarMusica();
        }

    }

    private void pararMusica() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private void cargarMusica() {
        mediaPlayer = MediaPlayer.create(this, R.raw.musica);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                pararMusica();
                cargarMusica();
            }
        });

        mediaPlayer.start();
    }

    private void cargarImagenesObjetos() {

        Bitmap ball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
        diametroBola = (int) ((DIAMETRO_BOLA_BASE * xMax) / ANCHO_PANTALLA_BASE);
        ballBitmap = Bitmap.createScaledBitmap(ball, diametroBola, diametroBola, true);

        Bitmap enemyBall = BitmapFactory.decodeResource(getResources(), R.drawable.enemyball);
        diametroBolaEnemiga = (int) ((DIAMETRO_BOLA_BASE * xMax) / ANCHO_PANTALLA_BASE);
        enemyBitmap = Bitmap.createScaledBitmap(enemyBall, diametroBolaEnemiga, diametroBolaEnemiga, true);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(!jugando)    //  No hacer nada si no se está en una partida
            return;

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            //  Valores del sensor
            xAcceleration = event.values[0];
            yAcceleration = event.values[2]-6f; //  Para no usar la pantalla totalmente vertical

            actualizarBola();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void actualizarBola() {

        //  Calcular nueva velocidad
        xVelocity += (xAcceleration * frameTime);
        yVelocity += (yAcceleration * frameTime);

        //  Calculate distancia viajada en ese tiempo
        float xS = (xVelocity/2) * frameTime;
        float yS = (yVelocity/2) * frameTime;

        //  Invertir valores debido a que los valores del sensor son opuestos a los que se necesita
        xPosition -= xS;
        yPosition -= yS;

        //  Coordenada x
        if((xPosition + margenHorizontal) > xMax) {
            xVelocity = -xVelocity/4f;
            xPosition = xMax - margenHorizontal;
        }
        else if((xPosition - margenHorizontal) < 0) {
            xVelocity = -xVelocity/4f;
            xPosition = margenHorizontal;
        }

        //  Coordenada y
        if((yPosition + margenVertical) > yMax) {
            yVelocity = -yVelocity/4f;
            yPosition = yMax - margenVertical;
        }
        else if((yPosition - margenVertical) < 0) {
            yVelocity = -yVelocity/4f;
            yPosition = margenVertical;
        }

        setContentView(ball);

    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(this);

        super.onStop();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onBackPressed() {
        if(mostrarDialogoFin) {
            finish();
        }
        else {
            mostrarDialogoFin = true;


            jugando = false;
            if (!musicaDesactivada && mediaPlayer != null && mediaPlayer.isPlaying()) {
                pararMusica();
            }

            if (!saliendo) {
                dialogoFinPartida("Partida cancelada");
            }
        }
    }

    @Override
    protected void onPause() {
        if(mostrarDialogoFin) {
            finish();
        }
        else {
            mostrarDialogoFin = true;


            jugando = false;
            if (!musicaDesactivada && mediaPlayer != null && mediaPlayer.isPlaying()) {
                pararMusica();
            }

            if (!saliendo) {
                dialogoFinPartida("Partida cancelada");
            }
        }
        super.onPause();
    }

    public class Ball extends View {

        private Paint cuadradoRelleno;
        private Paint cuadradoBorde;
        private RectF r;
        private List<EnemyBall> enemyBalls;

        private final static int RADIO_ESQUINA = 20;

        public Ball(Context context) {
            super(context);

            enemyBalls = new ArrayList<>();

            cuadradoRelleno = new Paint();
            cuadradoBorde = new Paint();
            r = new RectF(
                    margenHorizontal,
                    margenVertical,
                    xMax + diametroBola - margenHorizontal,
                    yMax + diametroBola - margenVertical);

            cuadradoRelleno.setStyle(Paint.Style.FILL);
            cuadradoRelleno.setColor(Color.parseColor("#CDCDCD"));

            cuadradoBorde.setStyle(Paint.Style.STROKE);
            cuadradoBorde.setColor(Color.BLACK);
            cuadradoBorde.setStrokeWidth(5);
        }

        public List<EnemyBall> getEnemyBalls() {
            return enemyBalls;
        }

        public void addEnemyBall(EnemyBall enemyBall) {
            enemyBalls.add(enemyBall);
        }

        @Override
        protected void onDraw(Canvas canvas) {

            canvas.drawRoundRect(r, RADIO_ESQUINA, RADIO_ESQUINA, cuadradoRelleno);
            canvas.drawRoundRect(r, RADIO_ESQUINA, RADIO_ESQUINA, cuadradoBorde);

            final Bitmap bitmapEnemy = enemyBitmap;
            for(int i=0; i<enemyBalls.size(); i++) {
                if(enemyBalls.get(i).eliminarBola()) {
                    puntuacion++;
                    enemyBalls.remove(enemyBalls.get(i));
                }
                else if(enemyBalls.get(i).colision(xPosition, yPosition)) {
                    if(!musicaDesactivada) {
                        soundPool.play(sonido, 1f, 1f, 0, 0, 1);
                    }

                    enemyBalls.remove(enemyBalls.get(i));
                    vidas--;

                    if(vidas <= 0) {
                        mostrarDialogoFin = true;
                        jugando = false;
                        if(!musicaDesactivada) {
                            pararMusica();
                        }
                        actualizarBaseDatos(puntuacion);
                        dialogoFinPartida("Puntuación: " + puntuacion);

                    }
                }
                else {
                    enemyBalls.get(i).aumentarMovimiento();
                    canvas.drawBitmap(bitmapEnemy, enemyBalls.get(i).getxPos(),
                            enemyBalls.get(i).getyPos(), null);
                }
            }

            final Bitmap bitmap = ballBitmap;
            canvas.drawBitmap(bitmap, xPosition, yPosition, null);

            invalidate();

        }

    }

    private void dialogoFinPartida(String mensaje) {

        AlertDialog alertDialog = new AlertDialog.Builder(ActivityAcceleroDodge.this)
                .setMessage(mensaje)
                .setPositiveButton("Jugar otra vez", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        configurarFlagsPantalla();
                        crearPartida();
                    }
                })
                .setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saliendo = true;
                        finish();
                    }
                })
                .create();

        alertDialog.show();

    }

    private void actualizarBaseDatos(int puntuacionObtenida) {

        String insert = "INSERT INTO RECORDS (nick, puntuacion) " +
                "VALUES (?,?)";
        String[] args = new String[] { nick, ""+puntuacionObtenida };
        db.execSQL(insert, args);


    }

    public class EnemyBall {

        private float xPos;
        private float yPos;
        private float incrementoMovimiento;
        private int orientacion;


        public EnemyBall(float incrementoMovimiento) {
            this.incrementoMovimiento = incrementoMovimiento;
            orientacion = ThreadLocalRandom.current().nextInt(0, 4);

            calcularPosicionInicial();

        }

        public float getxPos() {
            return xPos;
        }

        public float getyPos() {
            return yPos;
        }

        private void calcularPosicionInicial() {

            switch (orientacion) {

                case 0:     //  Izquierda
                    xPos = -diametroBola;
                    yPos = ThreadLocalRandom.current().nextInt(0, (int)yMax-diametroBola);
                    break;

                case 1:     //  Arriba
                    xPos = ThreadLocalRandom.current().nextInt(0, (int)xMax-diametroBola);
                    yPos = -diametroBola;
                    break;

                case 2:     //  Derecha
                    xPos = xMax + diametroBola;
                    yPos = ThreadLocalRandom.current().nextInt(0, (int)yMax-diametroBola);
                    break;

                case 3:     //  Abajo
                    xPos = ThreadLocalRandom.current().nextInt(0, (int)xMax-diametroBola);
                    yPos = yMax + diametroBola;
                    break;
            }

        }

        public void aumentarMovimiento() {

            switch (orientacion) {

                case 0:     //  Izquierda
                    xPos += incrementoMovimiento;
                    break;

                case 1:     //  Arriba
                    yPos += incrementoMovimiento;
                    break;

                case 2:     //  Derecha
                    xPos -= incrementoMovimiento;
                    break;

                case 3:     //  Abajo
                    yPos -= incrementoMovimiento;
                    break;
            }

        }

        public boolean eliminarBola() {

            boolean eliminar = false;

            switch (orientacion) {

                case 0:     //  Izquierda
                    if(xPos > (xMax+diametroBola))
                        eliminar = true;
                    break;

                case 1:     //  Arriba
                    if(yPos > (yMax+diametroBola))
                        eliminar = true;
                    break;

                case 2:     //  Derecha
                    if(xPos < -diametroBola)
                        eliminar = true;
                    break;

                case 3:     //  Abajo
                    if(yPos < -diametroBola)
                        eliminar = true;
                    break;
            }

            return eliminar;

        }

        public boolean colision(float xBola, float yBola) {

            boolean colision = false;

            if((xPos+diametroBola) > xBola &&
                    xPos < (xBola+diametroBola) &&
                    (yPos+diametroBola) > yBola &&
                    yPos < (yBola+diametroBola)) {
                colision = true;
            }

            return colision;

        }

    }
}
