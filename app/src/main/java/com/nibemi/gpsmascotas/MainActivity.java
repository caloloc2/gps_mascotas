package com.nibemi.gpsmascotas;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    WebService web = new WebService();

    Button actualizar, posicion;
    TextView pasos, fecha_text, hora_text, peso, kcalorias, kcalorias_perdidas;

    JSONArray elementos;
    String latitud, longitud, cantidad_pasos, fecha, hora;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actualizar = (Button)findViewById(R.id.refrescar);
        posicion = (Button)findViewById(R.id.posicion);

        pasos = (TextView)findViewById(R.id.pasos);
        fecha_text = (TextView)findViewById(R.id.fecha);
        hora_text = (TextView)findViewById(R.id.hora);

        peso = (TextView)findViewById(R.id.peso_mascota);
        kcalorias = (TextView)findViewById(R.id.kilocalorias_mascota);
        kcalorias_perdidas = (TextView)findViewById(R.id.kilocalorias_perdidas);

        latitud = "";
        longitud = "";

        actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Obtener_Datos();
            }
        });

        posicion.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if ((latitud!="")&&(longitud!="")){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/@"+latitud+","+longitud+",17.5z"));
                    startActivity(browserIntent);
                }else{
                    Toast.makeText(getApplicationContext(), "No se han recuperado los últimos datos de ubicación.",Toast.LENGTH_SHORT).show();
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
                                    cantidad_pasos = jsonobject.getString("cantidad_pasos");
                                    fecha = jsonobject.getString("fecha");
                                    hora = jsonobject.getString("hora");

                                    pasos.setText(cantidad_pasos.toString());
                                    fecha_text.setText("Fecha: "+fecha.toString());
                                    hora_text.setText("Hora: "+hora.toString());

                                    String peso_ingresado = peso.getText().toString();
                                    kcalorias.setText(Kcalorias(peso_ingresado));
                                    kcalorias_perdidas.setText(Kcalorias_Perdidas(kcalorias.getText().toString(), pasos.getText().toString()));

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
}
