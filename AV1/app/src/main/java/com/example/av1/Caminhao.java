package com.example.av1;

public class Caminhao extends Usuario {

    private static final double EFICIENCIA_INICIAL = 3.0;
    private static final double VELOCIDADE_INICIAL = 60.0;
    private static final long TEMPO_CALCULO_VELOCIDADE = 60; // 60 segundos

    public Caminhao() {
        super(EFICIENCIA_INICIAL);
        setVelocidadeIdeal(VELOCIDADE_INICIAL);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            double tempoDecorrido = (System.currentTimeMillis() - getHorarioInicial()) / 1000.0;
            if (tempoDecorrido >= TEMPO_CALCULO_VELOCIDADE) {
                calcularVelocidadeIdeal(getDistanciaTotalPercorrida(), tempoDecorrido / (60 * 60));
                setDistanciaTotalPercorrida(0); // Redefine a distância total percorrida
                setHorarioInicial(System.currentTimeMillis());
            }
        }
    }

    @Override
    public double calcularTaxaDeConsumo(double velocidade) {
        double novaTaxa = 0;
        if (velocidade <= getVelocidadeIdeal()) {
            novaTaxa = getTaxaEm80();
        } else if (velocidade <= 100) {
            novaTaxa = 0.8 * getTaxaEm80();
        } else if (velocidade <= 120) {
            novaTaxa = 0.6 * getTaxaEm80();
        } else if (velocidade > 120) {
            novaTaxa = 0.5 * getTaxaEm80();
        }

        if (novaTaxa > getTaxaEm80()) {
            novaTaxa = getTaxaEm80();
        }

        return novaTaxa;
    }

    @Override
    public void calcularVelocidadeIdeal(double distânciaPercorrida, double tempoDecorrido) {
        double velocidadeSubPista = distânciaPercorrida / tempoDecorrido;
        setVelocidadeMédia(velocidadeSubPista);
        setVelocidadeIdeal(60 * 60 / velocidadeSubPista);
        if (getVelocidadeIdeal() > 120) {
            setVelocidadeIdeal(120);
        }

        logger.info("Velocidade ideal atualizada: " + getVelocidadeIdeal() + " km/h às " + new java.util.Date());
    }
}
