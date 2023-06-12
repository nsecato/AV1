package com.example.av1;

import java.util.Locale;
import java.util.logging.Logger;

public class Usuario extends Thread {

    private final double taxaEm80; // Taxa de consumo de combustível em km/l a 80 km/h

    private double taxaAtualizada; // Taxa de consumo de combustível atualizada em km/l

    public double getTaxaEm80() {
        return taxaEm80;
    }

    public void setVelocidadeIdeal(double velocidadeIdeal) {
        this.velocidadeIdeal = velocidadeIdeal;
    }

    private double velocidadeIdeal; // Velocidade ideal em km/h

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
        while (!Thread.currentThread().isInterrupted()) {
            double tempoDecorrido = (System.currentTimeMillis() - horarioInicial) / 1000.0;
            if (tempoDecorrido >= 45) { // Verifica se a distância percorrida é de 1 km ou mais
                calcularVelocidadeIdeal(distanciaTotalPercorrida, tempoDecorrido / (60 * 60));
                distanciaTotalPercorrida = 0; // Redefine a distância total percorrida
                horarioInicial = System.currentTimeMillis();
            }
        }
    }

    public void calcularVelocidadeIdeal(double distânciaPercorrida, double tempoDecorrido) {
        // Distância que deveria ter sido percorrida a 80 km/h em 0,75 minutos
        // Ajuste de tempo
        double velocidadeSubPista = distânciaPercorrida / tempoDecorrido;
        velocidadeMédia = velocidadeSubPista;
        velocidadeIdeal = 80 * 80 / velocidadeSubPista;
        if (velocidadeIdeal > 120) {
            velocidadeIdeal = 120;
        }

        logger.info("Velocidade ideal atualizada: " + velocidadeIdeal + " km/h às " + new java.util.Date());
    }
}
