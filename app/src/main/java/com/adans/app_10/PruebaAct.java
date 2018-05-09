package com.adans.app_10;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Random;

import android.graphics.Color;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class PruebaAct extends AppCompatActivity implements SensorEventListener,LocationListener {


    Button btnMostrar;

    private Handler mHandler = new Handler();

    //Delay en sensores y Handler
    public int dly=1;

    //Var Boolean estado del GPS
    boolean EDOGPSBoo=false;

    //tv y imageview Indicador;
    TextView tvVel;
    Button btnIndic;
    int Veld = 40;

    //////
    ImageView ivCel;
    TextView tvNot1,tvNot2;
    TextView EdoGPS;
    //TextView locationText;
    //TextView locationText2;

    Button Start,Stop,Mostrar;

    LocationManager locationManager;
    //////

    float VAX,VAY,VAZ,VGX,VGY,VGZ;
    float LOG,LAT;

    int CdP=1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prueba);


        ivCel=(ImageView)findViewById(R.id.ivCel);
        tvNot1=(TextView)findViewById(R.id.tvNot1);
        tvNot2=(TextView)findViewById(R.id.tvNot2);

        Start=(Button)findViewById(R.id.btnStart);

        Stop=(Button)findViewById(R.id.btnStop);
        Stop.setVisibility(View.GONE);

        Mostrar=(Button)findViewById(R.id.btnMostrar);
        Mostrar.setVisibility(View.GONE);

        EdoGPS=(TextView)findViewById(R.id.tvEdoGPS);

        tvVel=(TextView)findViewById(R.id.tvVel);
        tvVel.setVisibility(View.GONE);
        btnIndic=(Button) findViewById(R.id.btnIndic);
        btnIndic.setVisibility(View.GONE);

        ////Recibe nombre Bundle
        Bundle XBundle=PruebaAct.this.getIntent().getExtras();

        if (XBundle!=null){
            String Nombre = XBundle.getString("nomb");
        }

        ////
///

        //permisos usuario
        /////////
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }
        /////////

        btnMostrar=(Button)findViewById(R.id.btnMostrar);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        List<Sensor> listaSensores = sensorManager.getSensorList(Sensor.TYPE_ALL);


        listaSensores = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

        if (!listaSensores.isEmpty()) {
            Sensor acelerometerSensor = listaSensores.get(0);
            sensorManager.registerListener(this, acelerometerSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);}

        listaSensores = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);

        if (!listaSensores.isEmpty()) {
            Sensor giroscopioSensor = listaSensores.get(0);
            sensorManager.registerListener(this, giroscopioSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);}


        btnMostrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mostrarCursos= new Intent(getApplicationContext(),Cursos.class);
                startActivity(mostrarCursos);
            }
        });

        getLocation();
    }

    //////
    void getLocation() {

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, dly*1000, 1, this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }

    }
    ///////


    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            switch(event.sensor.getType()) {

                case Sensor.TYPE_ACCELEROMETER:
                    VAX=event.values[0];
                    VAY=event.values[1];
                    VAZ=event.values[2];

                    break;

                case Sensor.TYPE_GYROSCOPE:
                    VGX=event.values[0];
                    VGY=event.values[1];
                    VGZ=event.values[2];

                    break;
            }
        }
    }

    ///BtnStar
    public void startRepeating(View v) {


        if(EDOGPSBoo=true) {
            //mHandler.postDelayed(mToastRunnable, 5000);
            mToastRunnable.run();
            Toast.makeText(this, "Guardando Datos, Cada " + dly + " Segundos", dly * 1000).show();

            Stop.setVisibility(View.VISIBLE);

            ivCel.setVisibility(View.GONE);
            tvNot1.setVisibility(View.GONE);
            tvNot2.setVisibility(View.GONE);
            EdoGPS.setVisibility(View.GONE);
            Start.setVisibility(View.GONE);

            btnIndic.setVisibility(View.VISIBLE);
            tvVel.setVisibility(View.VISIBLE);


        }
        else {Toast.makeText(getApplicationContext(),"Espera la conexión del GPS",Toast.LENGTH_LONG).show();
        }
    }

    ///BtnStop
    public void stopRepeating(View v) {
        mHandler.removeCallbacks(mToastRunnable);
        exportDatabse("BDDSensors");
        Intent intentE = new Intent(getApplicationContext(),GasYEmisAct.class);
        startActivity(intentE);
    }

    private Runnable mToastRunnable = new Runnable() {
        @Override
        public void run() {

            final MantBDD mantBDD=new MantBDD(getApplicationContext());
            getLocation();

            mantBDD.agregarCurso(VAX,VAY,VAZ,VGX,VAY,VAZ,LAT,LOG);

            //Valor random
            final int min = 0;
            final int max = 110;
            final int Vrandom = new Random().nextInt((max - min) + 1) + min;
            tvVel.setText(Vrandom+" Km");
            if(Vrandom>0&& Vrandom<40){
                btnIndic.setBackgroundColor(Color.BLUE);
            }
            if(Vrandom>40&& Vrandom<70){
                btnIndic.setBackgroundColor(Color.GREEN);
            }
            if(Vrandom>70&& Vrandom<110){
                btnIndic.setBackgroundColor(Color.RED);
            }
            //  Toast.makeText(getApplicationContext(),"Se agregaron correctamente",Toast.LENGTH_SHORT).show();
            int dlyto=1;//Segundos
            mHandler.postDelayed(this, dlyto*1000);
        }
    };

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}




    @Override
    public void onLocationChanged(Location location) {

        LAT= (float) location.getLatitude();
        LOG= (float) location.getLongitude();

        if (LAT != 0) {
            EdoGPS.setText("Conexión con GPS Disponible.");
            EDOGPSBoo=true;
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

        Toast.makeText(this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //Sql-Memory

    public void exportDatabse(String BDDSensors) {

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            File sdDow = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);

            if (sd.canWrite()) {
                String currentDBPath = "//data//"+getPackageName()+"//databases//"+BDDSensors+"";
                String backupDBPath = "backupBDD"+CdP+".db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sdDow, backupDBPath);
                Toast.makeText(this, "Guardando BDD"+"C:"+CdP, Toast.LENGTH_SHORT).show();

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
            CdP=CdP+1;
        } catch (Exception e) {

        }
    }
}