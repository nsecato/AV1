package com.example.av1;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Rota {
    private final GoogleMap mMap;
    private final String apiKey;
    private String distanciaTotal;
    private ArrayList<Double> duracoesPercurso = new ArrayList<>();
    private String tempoTotal;
    private final MapsActivity.CallbackRota routeCallback;
    private Polyline polyline;
    private ArrayList<Integer> distanciasPercurso = new ArrayList<>();

    /**
     * Cria um novo RouteDrawer para desenhar rotas no mapa.
     *
     * @param mMap            objeto GoogleMap para desenhar as rotas
     * @param apiKey          chave de API do Google Maps
     * @param routeCallback   callback para notificar quando a rota for desenhada
     */
    public Rota(GoogleMap mMap, String apiKey, MapsActivity.CallbackRota routeCallback) {
        this.mMap = mMap;
        this.apiKey = apiKey;
        this.routeCallback = routeCallback;
    }

    /**
     * Desenha uma rota no mapa entre dois pontos com uma velocidade desejada.
     *
     * @param start            ponto de partida
     * @param end              ponto de chegada
     */
    public void desenharRota(LatLng start, LatLng end) {
        String url = obterUrlDirecoes(start, end); // cria a url
        new DownloadTask(true, null).execute(url); // baixa os dados da url
    }

    /**
     * Atualiza a rota no mapa com base em novos pontos de partida e chegada.
     *
     * @param start            novo ponto de partida
     * @param end              novo ponto de chegada
     */
    public void atualizarRota(LatLng start, LatLng end) {
        if (polyline != null) {
            polyline.remove();
        }
        desenharRota(start, end);
    }

    /**
     * Obtém a URL da API Directions do Google com as coordenadas de origem e destino.
     *
     * @param origem  coordenadas de origem
     * @param destino    coordenadas de destino
     * @return URL da API Directions do Google
     */
    private String obterUrlDirecoes(LatLng origem, LatLng destino) {
        String strOrigem = "origin=" + origem.latitude + "," + origem.longitude;
        String strDestino = "destination=" + destino.latitude + "," + destino.longitude;
        String sensor = "sensor=false";
        String parametros = strOrigem + "&" + strDestino + "&" + sensor + "&key=" + apiKey;
        return "https://maps.googleapis.com/maps/api/directions/json?" + parametros;
    }

    /**
     * Converte a distância total da rota em quilômetros.
     *
     * @return distância total em quilômetros
     */
    public double getDistanciaTotalEmKm() {
        double valorDistancia = 0.0;
        if (distanciaTotal.contains("km")) {
            valorDistancia = Double.parseDouble(distanciaTotal.replace(" km", ""));
        } else if (distanciaTotal.contains("m")) {
            valorDistancia = Double.parseDouble(distanciaTotal.replace(" m", "")) / 1000;
        }
        return valorDistancia;
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<String, Void, String> {

        private final boolean desenharRota;
        private final CallbackDuracao callback;

        public DownloadTask(boolean desenharRota, CallbackDuracao callback) {
            this.desenharRota = desenharRota;
            this.callback = callback;
        }
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = baixarUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (desenharRota) {
                new ParserTask().execute(result);
            } else {
                new ParserTask(false).execute(result);
                if (callback != null) {
                    callback.onDuracaoObtida(duracoesPercurso, distanciasPercurso);
                }
            }
        }
    }

    /**
     * Faz o download dos dados JSON da rota a partir da URL fornecida.
     *
     * @param strUrl URL da rota
     * @return dados JSON da rota
     * @throws IOException se ocorrer um erro durante o download
     */
    private String baixarUrl(String strUrl) throws IOException {
        String dados = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();
            String linha;
            while ((linha = br.readLine()) != null) {
                sb.append(linha);
            }
            dados = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            assert iStream != null;
            iStream.close();
            urlConnection.disconnect();
        }
        return dados;
    }

    public static ArrayList<Double> convertToDouble(ArrayList<String> minutes) {
        ArrayList<Double> result = new ArrayList<>();
        for (String min : minutes) {
            String cleaned = min.replace("min", "").replace("s", "").trim();
            result.add(Double.parseDouble(cleaned));
        }
        return result;
    }


    public void obterDuracoesRota(LatLng start, LatLng end, CallbackDuracao callback) { //
        String url = obterUrlDirecoes(start, end);
        new DownloadTask(false, callback).execute(url);
    }

    public interface CallbackDuracao {
        void onDuracaoObtida(ArrayList<Double> duracoes, ArrayList<Integer> distanciasPercurso);
    }

    public ArrayList<Double> getDuracoesPercurso() {
        return duracoesPercurso;
    }

    @SuppressLint("StaticFieldLeak")
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        private final boolean desenharRota;
        public ParserTask() {
            this(true);
        }
        public ParserTask(boolean desenharRota) {
            this.desenharRota = desenharRota;
        }
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> rotas = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                rotas = parser.parse(jObject);
                distanciaTotal = parser.getTotalDistance();
                duracoesPercurso = convertToDouble(parser.getDurations());
                distanciasPercurso = parser.getDistances();
                double distanciaTotalDouble = getDistanciaTotalEmKm();
                double valorTempoTotal = 60 * distanciaTotalDouble / 80;
                tempoTotal = valorTempoTotal + " min";
            } catch (Exception e) {
                e.printStackTrace();
            }
            return rotas;
        }


        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> resultado) {
            if (desenharRota) {
                ArrayList<LatLng> pontos;
                polyline = null;
                new MarkerOptions();
                for (int i = 0; i < resultado.size(); i++) {
                    pontos = new ArrayList<>();
                    polyline = null;
                    List<HashMap<String, String>> caminho = resultado.get(i);

                    for (int j = 0; j < caminho.size(); j++) {
                        HashMap<String, String> ponto = caminho.get(j);
                        double lat = Double.parseDouble(Objects.requireNonNull(ponto.get("lat")));
                        double lng = Double.parseDouble(Objects.requireNonNull(ponto.get("lng")));
                        LatLng posicao = new LatLng(lat, lng);
                        pontos.add(posicao);
                    }

                    PolylineOptions options = new PolylineOptions();
                    options.addAll(pontos);
                    options.width(10);
                    options.color(Color.BLACK);
                    polyline = mMap.addPolyline(options);
                }
            }

            // Notificar o callback
            if (polyline != null && routeCallback != null) {
                routeCallback.onRouteDrawn(distanciaTotal, tempoTotal);
            }
        }
    }
}
