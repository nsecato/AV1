package com.example.av1;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Usuario extends Thread {

    UUID id = UUID.randomUUID();
    private final double taxaEm80; // Taxa de consumo de combustível em km/l a 80 km/h

    private double taxaAtualizada; // Taxa de consumo de combustível atualizada em km/l
    private Location localizacaoAtual;
    private LatLng destino;
    private Rota rota;
    private double multiplicadorCross = 1.0;
    private double tempoParaChegar;

    public double getTaxaEm80() {
        return taxaEm80;
    }

    public void setVelocidadeIdeal(double velocidadeIdeal) {
        this.velocidadeIdeal = velocidadeIdeal;
    }

    private double velocidadeIdeal; // Velocidade ideal em km/h

    private int iteracaoReconciliacao = 0;

    public void setVelocidadeMédia(double velocidadeMédia) {
        this.velocidadeMédia = velocidadeMédia;
    }

    private double velocidadeMédia;

    public double getDistanciaTotalPercorrida() {
        return distanciaTotalPercorrida;
    }

    public void setDistanciaTotalPercorrida(double distanciaTotalPercorrida) {
        this.distanciaTotalPercorrida = distanciaTotalPercorrida;
    }

    private double distanciaTotalPercorrida = 0; // Distância total percorrida desde o início do rastreamento
    private double distânciaTotal = 0;

    public long getHorarioInicial() {
        return horarioInicial;
    }

    public void setHorarioInicial(long horarioInicial) {
        this.horarioInicial = horarioInicial;
    }

    private long horarioInicial = 0; // Horário de início do rastreamento
    private long tempoSimulação = 0;
    private boolean emExecução = false;

    public static final Logger logger = Logger.getLogger(Usuario.class.getName());

    public Usuario(double taxa) {
        this.taxaEm80 = taxa;
        this.velocidadeIdeal = 80.0; // Velocidade ideal padrão
    }

    public String getTaxaAtualizada() {
        return String.format(Locale.US, "%.2f", taxaAtualizada) + " km/L";
    }

    public double getVelocidadeMédia() {
        return velocidadeMédia;
    }

    public double getDistanciaTotal() {
        return distânciaTotal;
    }

    public double getVelocidadeIdeal() {
        return velocidadeIdeal;
    }

    public void iniciarSimulação() {
        if (emExecução) {
            return; // Se a thread já estiver em execução, não faz nada
        }
        emExecução = true;
        horarioInicial = System.currentTimeMillis();
        tempoSimulação = horarioInicial;
        start();
    }

    public void pararSimulação() {
        this.interrupt();
    }

    public String getTempoTotalFormatado() {
        long tempoTotalPercorrido = (System.currentTimeMillis() - tempoSimulação) / 1000;
        long horas = tempoTotalPercorrido / 3600;
        long minutos = (tempoTotalPercorrido % 3600) / 60;
        long segundos = tempoTotalPercorrido % 60;

        return String.format(Locale.US, "%02d:%02d:%02d", horas, minutos, segundos);
    }

    public void atualizarDistânciaTotal(double distânciaPercorrida) {
        distanciaTotalPercorrida += distânciaPercorrida;
        distânciaTotal += distânciaPercorrida;
    }

    public String getConsumo() {
        taxaAtualizada = calcularTaxaDeConsumo(velocidadeMédia);
        return String.format(Locale.US, "%.2f", distânciaTotal / calcularTaxaDeConsumo(velocidadeMédia)) + " L";
    }

    public double calcularTaxaDeConsumo(double velocidade) {
        double novaTaxa = 0;
        if (velocidade <= 80) {
            novaTaxa = taxaEm80;
        } else if (velocidade <= 100) {
            novaTaxa = 0.8 * taxaEm80;
        } else if (velocidade <= 120) {
            novaTaxa = 0.6 * taxaEm80;
        } else if (velocidade > 120) {
            novaTaxa = 0.5 * taxaEm80;
        }

        if (novaTaxa > taxaEm80) {
            novaTaxa = taxaEm80;
        }

        return novaTaxa;
    }

    @Override
    public void run() {
        calcularVelocidadeIdeal(distanciaTotalPercorrida, System.currentTimeMillis() - tempoSimulação);
    }

    public void calcularVelocidadeIdeal(double distânciaPercorrida, double tempoDecorrido) {
        // Distância que deveria ter sido percorrida a 80 km/h em 0,75 minutos
        // Ajuste de tempo
        double velocidadeSubPista = distânciaPercorrida / tempoDecorrido;
        velocidadeMédia = velocidadeSubPista;
    }

    public void atualizarLocalizacao(Location localizacao) {
        this.localizacaoAtual = localizacao;
    }

    public void atualizarDestino(LatLng posicaoChegada) {
        this.destino = posicaoChegada;
    }

    public void atribuirRota(Rota rota) {
        this.rota = rota;
    }

    public double calcularTempoParaChegar(double[] temposReconciliados){
        double tempoTotal = 0;
        for(double value : temposReconciliados) {
            tempoTotal += value;
        }
        return tempoTotal;
    }

    public static double[][] matrizDeIncidencia(int numNodes) {
        double[][] matriz = new double[numNodes - 1][numNodes];

        // Preencha a matriz para F1->F2->...FN
        for (int i = 0; i < numNodes - 1; i++) {

            matriz[i][i] = -1;

            matriz[i][i + 1] = 1;
        }
        //Exemplo de matriz com 3 fluxos [ -1, 1, 0 ]
        //                               [ 0, -1, 1 ]
        //-1 entrada e 1 saída no nó
        return matriz;
    }

    public void reconciliar(){

        rota.obterDuracoesRota(new LatLng(localizacaoAtual.getLatitude(), localizacaoAtual.getLongitude()), destino, new Rota.CallbackDuracao() {
            @Override
            public void onDuracaoObtida(ArrayList<Double> duracoes, ArrayList<Integer> distanciasPercurso) {
                if (duracoes.size() != iteracaoReconciliacao) { //checagem para reconciliar somente depois de acabar o fluxo

                    ArrayList<Double> desvioPadrao = new ArrayList<>();
                    desvioPadrao.add(0.1); //Adiciona uma primeira variação
                    double[] y = duracoes.stream().mapToDouble(Double::doubleValue).toArray();

                    if(multiplicadorCross != 1.0){
                        System.out.println("Multiplicador: (" + multiplicadorCross + "), Durações sem multiplicador: ");
                        new Reconciliation().printMatrix(y);
                    }

                    for (int i = 0; i < y.length; i++) {
                        y[i] = y[i] * multiplicadorCross;
                    }
                    for (int i = 1; i < y.length; i++) {
                        if(y[i-1] > y[i]){
                            desvioPadrao.add(0.1 * (y[i]/y[i-1]));
                        } else {
                            desvioPadrao.add(0.1* (y[i-1]/y[i]));
                        }
                    }

                    double[] v = desvioPadrao.stream().mapToDouble(Double::doubleValue).toArray();
                    double[][] A = matrizDeIncidencia(y.length);

                    System.out.println("tempo para chegar: " + tempoParaChegar);
                    Reconciliation rec = new Reconciliation();
                    rec.reconcile(y, v, A);
                    System.out.println("Durações iniciais tamanho: " + y.length);
                    rec.printMatrix(y);
                    System.out.println("Durações Reconciliadas tamanho: " + rec.getReconciledFlow().length);
                    rec.printMatrix(rec.getReconciledFlow()); //print depois de reconciliar
                    double tempoEmHr = rec.getReconciledFlow()[0] / 60;
                    System.out.println("Tempo em hr de cada fluxo reconciliado: " + tempoEmHr);
                    double distanciaEmKm = distanciasPercurso.get(0) / 1000.0;
                    System.out.println("Distancia do fluxo em km: " + distanciaEmKm);
                    velocidadeIdeal = distanciaEmKm / tempoEmHr;
                    System.out.println("Velocidade reconciliação: " + velocidadeIdeal);
                    tempoParaChegar = calcularTempoParaChegar(rec.getReconciledFlow());
                    System.out.println("novo tempo para chegar: " + tempoParaChegar);
                    iteracaoReconciliacao = duracoes.size();
                    sendEncryptedJson(id.toString(), tempoParaChegar);
                }
                if(tempoParaChegar > 0) {
                    checkIdServer(id.toString(), tempoParaChegar);
                }
            }
        });
    }

    public void sendEncryptedJson(String id, Double tempoParaChegar) {
        JSONManager jsonManager = new JSONManager();
        CryptoManager cryptoManager = new CryptoManager();

        // Cria JSON
        JSONObject json = jsonManager.createJson(id, tempoParaChegar);

        // Criptografa JSON
        JSONObject cipherText = cryptoManager.encrypt(json.toString());

        // Envia dados criptografados para o servidor
        Call<Void> call = NetworkManager.getServerApi().sendJson(cipherText);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                System.out.println("success: " + response);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                System.out.println("failure: " + t);
            }
        });
    }


    public void checkIdServer(final String yourId, final double yourTimeToArrive) {
        Call<JsonObject> call = NetworkManager.getServerApi().receiveJson();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject jsonData = response.body(); //cria obj JSON com a resposta do servidor
                    if (jsonData != null && jsonData.has("nameValuePairs")) {
                        // Obtenha a string criptografada
                        String encryptedData = jsonData.getAsJsonObject("nameValuePairs").get("dados").getAsString();

                        // Descriptografar
                        String decryptedData = null;
                        try {
                            decryptedData = CryptoManager.decrypt(encryptedData);
                        } catch (Exception e) {
                            System.out.println("Erro ao descriptografar os dados: " + e.getMessage());
                            return;
                        }

                        // Converte a string descriptografada em Json
                        JsonObject decryptedJsonData = new JsonParser().parse(decryptedData).getAsJsonObject();
                        if(!decryptedJsonData.has("id")){ //checagem se ta vazio
                            return;
                        }
                        String id = decryptedJsonData.get("id").getAsString(); //recupera campo id do veiculo do servidor

                        if(!decryptedJsonData.has("time")){
                            return;
                        }

                        double timeToArrive = decryptedJsonData.get("time").getAsDouble(); //recupera valor do campo time do servidor

                        if (!yourId.equals(id)) {
                            // id é diferente do seu
                            System.out.println("Tempo este veiculo: " + tempoParaChegar + " Tempo outro veiculo servidor: " + timeToArrive);

                            // Comparar tempos para chegar
                            if (tempoParaChegar > timeToArrive) {
                                // Mantenha o multiplicador como 1
                                multiplicadorCross = 1.0;
                            } else {
                                // Ajustar o multiplicador para igualar os tempos
                                multiplicadorCross = timeToArrive / yourTimeToArrive ;
                            }
                            System.out.println("Multiplicador: " + multiplicadorCross);
                        } else {
                            // id é o mesmo que o seu
                            //System.out.println("Id é igual ao do servidor");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // Trate o erro aqui
                System.out.println("Erro ao recuperar os dados: " + t.getMessage());
            }
        });
    }

}
