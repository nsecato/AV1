package com.example.av1;

import static java.lang.Math.round;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.av1.Usuario;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocalizacaoThread extends Thread {
    private final Context context;
    private final AtualizacaoVelocidadeCallback atualizacaoVelocidadeCallback;
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationRequest locationRequest;
    private Location localizacaoAnterior;
    private final Usuario user;

    private boolean rastreando = false;

    public LocalizacaoThread(Context context, Usuario user, AtualizacaoVelocidadeCallback atualizacaoVelocidadeCallback) {
        this.context = context;
        this.atualizacaoVelocidadeCallback = atualizacaoVelocidadeCallback;
        this.user = user;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        locationRequest = criarSolicitacaoLocalizacao();
    }

    /**
     * Interface de retorno de chamada para atualização de velocidade.
     */
    public interface AtualizacaoVelocidadeCallback {
        void onAtualizacaoVelocidade(float velocidade, Location novaLocalizacao);
    }

    @Override
    public void run() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permissão não concedida
            return;
        }
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location localizacao : locationResult.getLocations()) {
                    if (rastreando && localizacaoAnterior != null) {
                        double distanciaEmMetros = localizacao.distanceTo(localizacaoAnterior);
                        double velocidadeT = localizacao.getSpeed();
                        double velocidadeAtual = round(velocidadeT);
                        float velocidadeKmph = round((velocidadeAtual * 3.6));
                        user.atualizarDistânciaTotal(distanciaEmMetros / 1000);
                        atualizacaoVelocidadeCallback.onAtualizacaoVelocidade(velocidadeKmph, localizacao);
                    }
                    localizacaoAnterior = localizacao;
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    /**
     * Inicia o rastreamento da localização.
     */
    public void iniciarRastreamento() {
        rastreando = true;
        user.iniciarSimulação();
    }

    /**
     * Interrompe o rastreamento da localização.
     */
    public void pararRastreamento() {
        rastreando = false;
        if (user != null) {
            user.pararSimulação();
        }
    }

    /**
     * Cria uma solicitação de localização com configurações personalizadas.
     *
     * @return LocationRequest
     */
    protected LocationRequest criarSolicitacaoLocalizacao() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }
}
