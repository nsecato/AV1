package com.example.av1;

public class Carro extends Usuario {

    private static final double EFICIENCIA_INICIAL = 21.0;
    private static final double VELOCIDADE_INICIAL = 80.0;
    private static final long TEMPO_CALCULO_VELOCIDADE = 60 * 1000; // 60 segundos

    public Carro() {
        super(EFICIENCIA_INICIAL);
        super.setVelocidadeIdeal(VELOCIDADE_INICIAL);
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
