package com.example.av1;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectionsJSONParser {
    private String totalDistance = "";

    private ArrayList<String> durations = new ArrayList<>();
    private ArrayList<Integer> distances = new ArrayList<>();
    /**
     * Analisa o objeto JSON da resposta da API Directions do Google e extrai as informações necessárias.
     *
     * @param jObject objeto JSON da resposta da API Directions
     * @return lista de rotas com seus respectivos pontos no mapa
     */
    public List<List<HashMap<String,String>>> parse(JSONObject jObject){
        List<List<HashMap<String, String>>> routes = new ArrayList<>();
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;

        try {
            jRoutes = jObject.getJSONArray("routes");

            for(int i=0;i<jRoutes.length();i++){
                jLegs = ((JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List<HashMap<String, String>> path = new ArrayList<>();

                for(int j=0;j<jLegs.length();j++){
                    // Obter a distância e o tempo de cada trecho
                    JSONObject jLeg = jLegs.getJSONObject(j);
                    JSONObject distanceObject = jLeg.getJSONObject("distance");
                    JSONObject durationObject = jLeg.getJSONObject("duration");

                    totalDistance = distanceObject.getString("text"); // Recupera da resposta de rota do google a distancia total
                    durationObject.getString("text"); // Recupera do objeto resposta do google a duratção total

                    jSteps = jLeg.getJSONArray("steps"); //Objeto das "etapas"

                    for(int k=0;k<jSteps.length();k++){
                        String polyline;
                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points"); //Recupera as linhas para desenhar a rota
                        List<LatLng> list = decodePoly(polyline);
                        String duration = ((JSONObject) jSteps.get(k)).getJSONObject("duration").getString("text"); //recupera as durações de cada etapa
                        int distance = ((JSONObject) jSteps.get(k)).getJSONObject("distance").getInt("value"); // recupera as distancia de cada etapa

                        distances.add(distance); //adiciona a uma lista que vai ser acessada
                        durations.add(duration);
                        for(int l=0;l<list.size();l++){
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put("lat", Double.toString(list.get(l).latitude));
                            hm.put("lng", Double.toString(list.get(l).longitude));
                            path.add(hm);
                        }
                    }

                    routes.add(path);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return routes;
    }

    /**
     * Obtém a distância total da rota.
     *
     * @return distância total da rota
     */
    public String getTotalDistance() {
        return totalDistance;
    }

    public ArrayList<String> getDurations() {
        return durations;
    }

    /**
     * Decodifica a string codificada do polyline em uma lista de coordenadas LatLng.
     *
     * @param encoded string codificada do polyline
     * @return lista de coordenadas LatLng
     */
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    public ArrayList<Integer> getDistances() {
        return distances;
    }
}
