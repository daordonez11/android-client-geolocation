package com.example.danielordonez.labredes5;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by danielordonez on 3/4/16.
 */
public class ThreadComunicacion extends Thread {
    private String protocolo;
    private Socket canalTCP;
    private String serverIP;
    private int port;
    private DatagramSocket canalUDP;
    private InetAddress address;
    private boolean connected;
    private PrintStream out;
    private LocationManager lm;
    private View cont;
    private TextView mensajes;
    private LocationManager mlocManager;
    private  GoogleApiClient mGoogleApiClient;

    public ThreadComunicacion(String protocolon, String serverIP, int port, View view, GoogleApiClient mGoogleApiClient,LocationManager mlocManager) {
        this.protocolo = protocolon;
        this.serverIP = serverIP;
        this.port = port;
        this.mlocManager = mlocManager;
        cont = view;
this.mGoogleApiClient=mGoogleApiClient;

    }

    public void terminarConexion() {
        connected = false;
        if (canalTCP != null) {
            try {
                out.write("FIN".getBytes());
                canalTCP.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (canalUDP != null) {
            canalUDP.close();
        }
    }

    @Override
    public void run() {
        if (protocolo.equals("TCP")) {
            try {
                address = Inet4Address.getByName(serverIP);
                canalTCP = new Socket(address, port);
                connected = true;
                out = new PrintStream(canalTCP.getOutputStream());

                Snackbar snackbar = Snackbar
                        .make(cont, "Se conectó correctamente", Snackbar.LENGTH_SHORT);

                snackbar.show();

                while (connected) {
                    enviarUbicacionTCP();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                }


            } catch (Exception e) {
                Snackbar snackbar = Snackbar
                        .make(cont, e.getMessage(), Snackbar.LENGTH_LONG);

                snackbar.show();
                e.printStackTrace();
            }
        }
        if (protocolo.equals("UDP")) {
            try {
                address = InetAddress.getByName(serverIP);
                canalUDP = new DatagramSocket();


                connected = true;
                Snackbar snackbar = Snackbar
                        .make(cont, "Se conectó correctamente", Snackbar.LENGTH_SHORT);

                snackbar.show();
                while (connected) {
                    enviarUbicacionUDP();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void enviarUbicacionTCP() {

        try {
            Location mLastLocation =   mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if(mLastLocation!=null)
            {
                out.println(("Latitud: "+String.valueOf(mLastLocation.getLatitude())+" - Longitud: "+String.valueOf(mLastLocation.getLongitude())+" - Altitud: "+String.valueOf(mLastLocation.getAltitude())+" - Velocidad: "+String.valueOf(mLastLocation.getSpeed())));

            }
            else
            {
                out.println("No encuentra ubicación");
                Snackbar snackbar = Snackbar
                        .make(cont, "No encuentra ubicacion", Snackbar.LENGTH_SHORT);

                snackbar.show();
            }
        }catch(SecurityException e)
        {
            Snackbar snackbar = Snackbar
                    .make(cont, e.getMessage(), Snackbar.LENGTH_LONG);

            snackbar.show();
        }
    }
    private void enviarUbicacionUDP() {
        try {
            Location mLastLocation =   mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


            if(mLastLocation!=null)
            {
                String data = "Latitud: "+String.valueOf(mLastLocation.getLatitude())+" - Longitud: "+String.valueOf(mLastLocation.getLongitude())+" - Altitud: "+String.valueOf(mLastLocation.getAltitude())+" - Velocidad: "+String.valueOf(mLastLocation.getSpeed());
                byte btest[] = new byte[50];
                btest = data.getBytes();
                DatagramPacket p1 = new DatagramPacket(btest, btest.length,address, port);
                try {
                    canalUDP.send(p1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Snackbar snackbar = Snackbar
                        .make(cont, "No encuentra ubicacion", Snackbar.LENGTH_SHORT);

                snackbar.show();
            }
        }catch(SecurityException e)
        {
            Snackbar snackbar = Snackbar
                    .make(cont, e.getMessage(), Snackbar.LENGTH_LONG);

            snackbar.show();
        }

    }

    static void stringToPacket(String s, DatagramPacket packet) {
        byte[] bytes = s.getBytes();
        System.arraycopy(bytes, 0, packet.getData(), 0, bytes.length);
        packet.setLength(bytes.length);
    }
    static String stringFromPacket(DatagramPacket packet) {
        return new String(packet.getData(), 0, packet.getLength());
    }
}
