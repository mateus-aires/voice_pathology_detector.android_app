package com.airesapps.util;

public class ObservationsUtil {

    private final static String OBSERVATION_1 = "- Certifique-se que está em um ambiente silencioso. O detector é sensível a ruídos.";
    private final static String OBSERVATION_2 = "- Certifique-se que está conectado à internet.";
    private final static String OBSERVATION_3 = "- Quando a instrução surgir, fale de forma sustentada as vogais \"a\", \"i\" e \"u\" por mais de três segundos.";
    private final static String OBSERVATION_4 = "- Após emitir as três vogais, o resultado será mostrado na tela. Se este for positivo, a probabilidade de você estar doente também aparecerá na tela: probabilidades acima de " + Threshold.getThresholdString() + " implicam em um diagnóstico positivo.";
    private final static String OBSERVATION_5 = "- O detector necessita de, no mínimo, três segundos de som audível (sem silêncio) para detectar patologias.";
    private final static String OBSERVATION_6 = "- Lembre-se: esta não é uma ferramenta de pré-diagnóstico, somente um profissional da área de saúde pode diagnosticar disfonias. O propósito dessa aplicação é incentivar usuários a buscar assistência médica.";

    public static String getObservations() {
        String[] observationsArray = new String[]{OBSERVATION_1, OBSERVATION_2, OBSERVATION_3, OBSERVATION_4, OBSERVATION_5, OBSERVATION_6};
        StringBuilder observationsString = new StringBuilder();
        for (String observation: observationsArray) {
            observationsString.append(observation);
            if (observation != OBSERVATION_6) {
                observationsString.append("\n\n");
            }
        }
        return observationsString.toString();
    }


}
