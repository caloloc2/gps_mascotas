package com.nibemi.gpsmascotas;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {
    private BluetoothUtils bluetooth;
    private ListView dispositivos;
    private TextView cantidad_pasos;

    private Handler handler = new Handler();

    WebService web = new WebService();
    Button desconectar, posicion;
    TextView peso, kcalorias, kcalorias_perdidas, pasos_por_recorrer;

    JSONArray elementos;
    String latitud, longitud, fecha, hora;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetooth = new BluetoothUtils();
        dispositivos = (ListView) findViewById(R.id.listado_dispositivos);
        String[] nombres = bluetooth.getNames();
        dispositivos.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nombres));
        dispositivos.setOnItemClickListener(this);

        cantidad_pasos = (TextView) findViewById(R.id.cantidad_pasos);

        desconectar = (Button)findViewById(R.id.desconectar);
        posicion = (Button)findViewById(R.id.posicion);

        peso = (TextView)findViewById(R.id.peso_mascota);
        kcalorias = (TextView)findViewById(R.id.kilocalorias_mascota);
        kcalorias_perdidas = (TextView)findViewById(R.id.kilocalorias_perdidas);
        pasos_por_recorrer = (TextView)findViewById(R.id.pasos_por_recorrer);

        latitud = "";
        longitud = "";

        posicion.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Obtener_Datos();

                if ((latitud!="")&&(longitud!="")){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/@"+latitud+","+longitud+",17.5z"));
                    startActivity(browserIntent);
                }else{
                    Toast.makeText(getApplicationContext(), "No se han recuperado los últimos datos de ubicación.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        desconectar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (bluetooth.isConnected()){
                    bluetooth.disconnect();
                    Toast.makeText(getApplicationContext(), "Dispositivo desconectado.",Toast.LENGTH_SHORT).show();
                    dispositivos.setEnabled(true);
                    desconectar.setEnabled(false);
                }
            }
        });


    }

    public void Obtener_Datos(){
        Thread tr = new Thread(){
            @Override
            public void run() {

                final String datos = web.Informacion();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            elementos = new JSONArray(datos);

                            if (elementos.length()>0) {
                                for (int i = 0; i < elementos.length(); i++) {
                                    JSONObject jsonobject = elementos.getJSONObject(i);
                                    latitud = jsonobject.getString("latitud");
                                    longitud = jsonobject.getString("longitud");
                                    fecha = jsonobject.getString("fecha");
                                    hora = jsonobject.getString("hora");
                                }
                            }else{
                                Toast.makeText(getApplicationContext(), "No existen valores registrado.",Toast.LENGTH_SHORT).show();
                            }

                        }catch(Exception e){
                            System.out.println(e.getMessage());
                        }

                    }
                });
            }
        };
        tr.start();
    }

    String Kcalorias(String Peso){
        String calculo = "0";
        if (Peso!=""){
            float aux = 0;
            try{
                aux = Float.parseFloat(Peso);
                float kcal = (aux*30)+70;

                calculo =  Float.toString(kcal);
            }catch(Exception e){
                System.out.println("Error campo no numerico");
            }
        }
        return calculo;
    }

    String Kcalorias_Perdidas(String kcalorias, String pasos){
        String calculo = "0";
        if ((kcalorias!="")&&(pasos!="")){
            float aux = 0;
            float aux_pasos = 0;
            try{
                aux = Float.parseFloat(kcalorias);
                aux_pasos = Float.parseFloat(pasos);
                float kcalp = aux-(aux_pasos/4);

                calculo =  Float.toString(kcalp);
            }catch(Exception e){
                System.out.println("Error campo no numerico");
            }
        }
        return calculo;
    }

    String Pasos_Recorrer(String peso, String pasos){
        String calculo = "0";
        if ((peso!="")&&(pasos!="")){
            float aux = 0;
            float aux_pasos = 0;
            try{
                aux = Float.parseFloat(peso);
                aux_pasos = Float.parseFloat(pasos);
                float pasos_recorrer = (((aux*30)+70)*4) - aux_pasos;

                calculo =  Float.toString(pasos_recorrer);
            }catch(Exception e){
                System.out.println("Error campo no numerico");
            }
        }
        return calculo;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (bluetooth.connect(position)){
            Toast.makeText(this, "Conectado correctamente", Toast.LENGTH_SHORT).show();
            startTimer();
            dispositivos.setEnabled(false);
            desconectar.setEnabled(true);
        }
    }

    // Cuando cerramos la app desconectamos
    protected void onPause() {
        cancelTimer();
        super.onPause();
        bluetooth.disconnect();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String recibe = bluetooth.recibir();
            try{
                if (recibe!="") {
                    cantidad_pasos.setText(recibe);

                    String peso_ingresado = peso.getText().toString();
                    kcalorias.setText(Kcalorias(peso_ingresado));
                    kcalorias_perdidas.setText(Kcalorias_Perdidas(kcalorias.getText().toString(), cantidad_pasos.getText().toString()));
                    pasos_por_recorrer.setText(Pasos_Recorrer(peso_ingresado, cantidad_pasos.getText().toString()));
                }
            }catch(Exception e){
                // cadena vacia
            }
            startTimer();
        }
    };

    public void startTimer() {
        handler.postDelayed(runnable, 1000);
    }

    public void cancelTimer() {
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }
}
