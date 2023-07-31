package com.example.av1;

public class Carro extends Usuario {

    private static final double EFICIENCIA_INICIAL = 21.0;
    private static final double VELOCIDADE_INICIAL = 80.0;
    private static final long TEMPO_CALCULO_VELOCIDADE = 45; // 60 segundos

    public Carro() {
        super(EFICIENCIA_INICIAL);
        super.setVelocidadeIdeal(VELOCIDADE_INICIAL);
    }

    @Override
    public void run() {
    }

    @Override
    public void calcularVelocidadeIdeal(double distânciaPercorrida, double tempoDecorrido) {
        double velocidadeSubPista = distânciaPercorrida / tempoDecorrido;
        setVelocidadeMédia(velocidadeSubPista);
        setVelocidadeIdeal(80 * 80 / velocidadeSubPista);
        if (getVelocidadeIdeal() > 120) {
            setVelocidadeIdeal(120);
        }

        logger.info("Velocidade ideal atualizada: " + getVelocidadeIdeal() + " km/h às " + new java.util.Date());
    }
}
