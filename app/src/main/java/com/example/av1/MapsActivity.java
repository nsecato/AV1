package com.example.av1;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.av1.databinding.ActivityMapsBinding;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mapaGoogle;
    private LatLng posicaoPartida;
    private LatLng posicaoChegada;
    private PlacesClient clienteLugares;

    private TextView campoDistanciaTotal;
    private TextView campoTempoTotal;
    private TextView campoVelocidadeAtual;
    private LocalizacaoThread localizacao;
    private TextView campoDistanciaPercorrida;
    private TextView campoTempoAtual;
    private TextView campoVelocidadeIdeal;
    private TextView campoConsumo;
    private TextView campoTaxa;
    private TextView campoVelocidadeMedia;

    private Location localizacaoUsuario;
    private Rota rota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.av1.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        campoDistanciaTotal = findViewById(R.id.total_distance);
        campoTempoTotal = findViewById(R.id.total_time);
        campoVelocidadeAtual = findViewById(R.id.current_speed);
        campoVelocidadeIdeal = findViewById(R.id.optmal_speed);
        campoDistanciaPercorrida = findViewById(R.id.current_distance);
        campoTempoAtual = findViewById(R.id.current_time);
        campoConsumo = findViewById(R.id.fuel_consumption);
        campoTaxa = findViewById(R.id.eficiency);
        campoVelocidadeMedia = findViewById(R.id.average_speed);


        AutoCompleteTextView destinoFinal = findViewById(R.id.end_location);
        ImageButton botaoGerarRota = findViewById(R.id.button_generate_route);
        ImageButton botaoIniciar = findViewById(R.id.button_start_simulation);
        ImageButton botaoParar = findViewById(R.id.button_stop_simulation);
        botaoIniciar.setEnabled(false);
        botaoParar.setEnabled(false);

        destinoFinal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                obterSugestoesLugar(s.toString(), sugestoes -> destinoFinal.setAdapter(new ArrayAdapter<>(MapsActivity.this, android.R.layout.simple_dropdown_item_1line, sugestoes)));
            }

            @Override
            public void afterTextChanged(Editable s) {
                obterSugestoesLugar(s.toString(), sugestoes -> destinoFinal.setAdapter(new ArrayAdapter<>(MapsActivity.this, android.R.layout.simple_dropdown_item_1line, sugestoes)));
            }
        });


        botaoIniciar.setOnClickListener(v->{
            Usuario veiculo = criarVeiculo();
            botaoParar.setEnabled(true);
            botaoIniciar.setEnabled(false);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                localizacao = new LocalizacaoThread(this, veiculo , (velocidade, novaLocalizacao) -> runOnUiThread(() -> {

                    if (mapaGoogle != null) {
                        LatLng novaLatLng = new LatLng(novaLocalizacao.getLatitude(), novaLocalizacao.getLongitude());
                        float nivelZoom = 16.0f;
                        mapaGoogle.moveCamera(CameraUpdateFactory.newLatLngZoom(novaLatLng, nivelZoom));
                    }

                    String strVelocidadeIdeal = String.format(Locale.US, "%.2f",veiculo.getVelocidadeIdeal())+ "km/h ";
                    campoVelocidadeIdeal.setText(strVelocidadeIdeal);
                    String strTempoAtual = veiculo.getTempoTotalFormatado();
                    campoTempoAtual.setText(strTempoAtual);
                    String strConsumo = veiculo.getConsumo();
                    campoConsumo.setText(strConsumo);
                    String strTaxa = veiculo.getTaxaAtualizada();
                    campoTaxa.setText(strTaxa);
                    String strVelocidadeMedia = String.format(Locale.US, "%.2f",veiculo.getVelocidadeMédia()) + " km/h";
                    campoVelocidadeMedia.setText(strVelocidadeMedia);
                    String strVelocidadeAtual = velocidade + " km/h ";
                    campoVelocidadeAtual.setText(strVelocidadeAtual);
                    String strDistanciaPercorrida = String.format(Locale.US, "%.1f", veiculo.getDistanciaTotal()) + " km ";
                    campoDistanciaPercorrida.setText(strDistanciaPercorrida);

                }));
                localizacao.start();
                localizacao.iniciarRastreamento();
            }
        });

        botaoParar.setOnClickListener(v -> {
            if (localizacao != null) {
                localizacao.pararRastreamento();
                localizacao = null;
            }
            botaoParar.setEnabled(false);
            campoVelocidadeAtual.setText("");
            campoVelocidadeIdeal.setText("");
            campoDistanciaPercorrida.setText("");
            campoTempoAtual.setText("");
            campoConsumo.setText("");
            campoTaxa.setText("");
            destinoFinal.setText("");
            campoVelocidadeMedia.setText("");
            campoDistanciaTotal.setText("");
            campoTempoTotal.setText("");
            mapaGoogle.clear();
        });

        botaoGerarRota.setOnClickListener(v -> {
            posicaoPartida = new LatLng(localizacaoUsuario.getLatitude(), localizacaoUsuario.getLongitude());
            String nomeLocalChegada = destinoFinal.getText().toString();

            botaoIniciar.setEnabled(true);

            obterIdLugarDePredicao(nomeLocalChegada, idLocalChegada -> obterLatLngDeIdLugar(idLocalChegada, latLngChegada -> {
                posicaoChegada = latLngChegada;
                if(mapaGoogle != null && posicaoPartida != null && posicaoChegada != null) {
                    mapaGoogle.clear();
                    rota = new Rota(mapaGoogle, "AIzaSyBXnwq5dbzApG6WTQoVD3mob1ie6pezbGY", (distanciaTotal, tempoTotal) -> {
                        runOnUiThread(() -> {
                            String distanciaTotalStr = distanciaTotal + " ";
                            campoDistanciaTotal.setText(distanciaTotalStr);
                            String tempoTotalStr = tempoTotal + " ";
                            campoTempoTotal.setText(tempoTotalStr);
                        });
                    });
                    rota.desenharRota(posicaoPartida, posicaoChegada);
                    mapaGoogle.addMarker(new MarkerOptions().position(posicaoChegada).title("Posição Final"));
                    LatLngBounds.Builder construtor = new LatLngBounds.Builder();
                    construtor.include(posicaoPartida);
                    construtor.include(posicaoChegada);
                    LatLngBounds limites = construtor.build();
                    int margem = 200;
                    CameraUpdate atualizacaoCamera = CameraUpdateFactory.newLatLngBounds(limites, margem);
                    mapaGoogle.setOnMapLoadedCallback(() -> mapaGoogle.animateCamera(atualizacaoCamera));
                }
            }));
        });

        Places.initialize(getApplicationContext(), "AIzaSyBXnwq5dbzApG6WTQoVD3mob1ie6pezbGY");
        clienteLugares = Places.createClient(this);



        SupportMapFragment fragmentoMapa = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert fragmentoMapa != null;
        fragmentoMapa.getMapAsync(this);
    }

    private Usuario criarVeiculo() {
        Intent intent = getIntent();
        String tipoVeiculo = intent.getStringExtra("tipoVeiculo");

        if (tipoVeiculo.equals("carro")) {
            return new Carro();
        } else if (tipoVeiculo.equals("caminhao")) {
            return new Caminhao();
        }
        return null;
    }

    private void obterIdLugarDePredicao(String nomeLocal, OnPlaceIdReadyCallback onPlaceIdReadyCallback) {
        // Construímos uma solicitação para buscar previsões de autocomplete
        FindAutocompletePredictionsRequest solicitacao = FindAutocompletePredictionsRequest.builder()
                .setQuery(nomeLocal)  // Definimos a query como o nome do local fornecido
                .build();

        // Fazemos a solicitação ao cliente de lugares
        clienteLugares.findAutocompletePredictions(solicitacao).addOnSuccessListener((response) -> {
            for (AutocompletePrediction previsao : response.getAutocompletePredictions()) {
                // Se a previsão corresponder ao nome do local, recuperamos o ID do local e chamamos o callback
                if(previsao.getFullText(null).toString().equals(nomeLocal)){
                    onPlaceIdReadyCallback.onPlaceIdReady(previsao.getPlaceId());
                    break;
                }
            }
        }).addOnFailureListener((exception) -> {
            // Em caso de falha, registramos o erro
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Local não encontrado: " + apiException.getStatusCode());
            }
        });
    }

    public interface OnLatLngReadyCallback {
        void onLatLngReady(LatLng latLng);
    }

    public interface OnPlaceIdReadyCallback {
        void onPlaceIdReady(String placeId);
    }

    private void obterSugestoesLugar(String query, CallbackSugestoesLugar callback) {
        // Cria uma instância de RectangularBounds
        RectangularBounds limites = RectangularBounds.newInstance(
                new LatLng(-33.880490, 151.184363),
                new LatLng(-33.858754, 151.229596));

        // Usa a API Places para obter as previsões de sugestões de lugares
        FindAutocompletePredictionsRequest solicitacao = FindAutocompletePredictionsRequest.builder()
                .setLocationBias(limites)
                .setQuery(query)
                .build();

        // Faz uma chamada assíncrona para obter as sugestões de lugares
        clienteLugares.findAutocompletePredictions(solicitacao).addOnSuccessListener((response) -> {
            List<String> sugestoes = new ArrayList<>();
            for (AutocompletePrediction previsao : response.getAutocompletePredictions()) {
                sugestoes.add(previsao.getFullText(null).toString());
            }
            // Chama o callback informando as sugestões de lugares encontradas
            callback.onSuggestionsReady(sugestoes);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Local não encontrado: " + apiException.getStatusCode());
            }
        });
    }

    private void obterLatLngDeIdLugar(String placeId, OnLatLngReadyCallback onLatLngReadyCallback) {
        List<Place.Field> camposLocal = Collections.singletonList(Place.Field.LAT_LNG);

        // Criando uma solicitação para buscar informações do local a partir do ID do local
        FetchPlaceRequest solicitacao = FetchPlaceRequest.newInstance(placeId, camposLocal);

        // Realizando a solicitação de maneira assíncrona
        clienteLugares.fetchPlace(solicitacao).addOnSuccessListener((response) -> {
            // No caso de sucesso, pegamos o local da resposta
            Place lugar = response.getPlace();

            // E chamamos o callback passando as coordenadas do local
            onLatLngReadyCallback.onLatLngReady(lugar.getLatLng());
        }).addOnFailureListener((exception) -> {
            // Em caso de falha, verificamos se a exceção é uma ApiException
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                // Se for, logamos o erro com o código de status
                Log.e(TAG, "Local não encontrado: " + apiException.getStatusCode());
            }
        });
    }

    /**
     * Manipula o mapa assim que estiver disponível.
     * Esse retorno de chamada é acionado quando o mapa estiver pronto para ser usado.
     * Aqui podemos adicionar marcadores ou linhas, adicionar ouvintes ou mover a câmera.
     * Neste caso, apenas adicionamos um marcador perto de Sydney, Austrália.
     * Se o Google Play services não estiver instalado no dispositivo, o usuário será solicitado a instalá-lo
     * dentro do SupportMapFragment. Este método só será acionado quando o usuário tiver
     * instalado o Google Play services e voltou para o aplicativo.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.mapaGoogle = googleMap;

        // Verifica se a permissão de localização foi concedida
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permissão não concedida, solicite ao usuário
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Habilita a camada de localização do mapa
        this.mapaGoogle.setMyLocationEnabled(true);

        // Obtém a última localização conhecida do dispositivo
        FusedLocationProviderClient clienteLocalizacaoFusao = LocationServices.getFusedLocationProviderClient(this);
        clienteLocalizacaoFusao.getLastLocation().addOnSuccessListener(localizacao -> {
            localizacaoUsuario = localizacao;
            if (localizacao != null) {
                // Obtém as coordenadas da localização
                LatLng latLng = new LatLng(localizacao.getLatitude(), localizacao.getLongitude());

                // Move a câmera para a localização do usuário
                this.mapaGoogle.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
            }
        });
    }

    public interface CallbackSugestoesLugar {
        void onSuggestionsReady(List<String> suggestions);
    }

    public interface CallbackRota {
        void onRouteDrawn(String totalDistance, String totalTime);
    }
}
