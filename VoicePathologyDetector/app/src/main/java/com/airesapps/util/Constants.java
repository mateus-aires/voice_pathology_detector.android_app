package com.airesapps.util;

public class Constants {
    // MainActivity constants
    public static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    public static final int STATE_LOGO = -1;
    public static final int STATE_INSTRUCTION = 0;
    public static final int STATE_RECORDING = 1;
    public static final int STATE_PROCESSING = 2;
    public static final int STATE_RESULT_SUCCESS = 3;
    public static final int STATE_RESULT_ERROR = 4;
    public static final String CHRONOMETER_FORMAT = "mm:ss";

    // Messages
    public static final String TRUE_PREDICTION_MESSAGE = "Você está apresentando sintomas de disfonia";
    public static final String FALSE_PREDICTION_MESSAGE = "Você parece estar saudável";
    public static final String PRESENT_PROBA_FORMAT = "Probabilidade de estar doente: %d";
    public static final String CONNECTION_ERROR_MESSAGE = "Erro de conexão, verifique sua internet.";
    public static final String INTERNAL_ERROR_MESSAGE = "Erro interno, tente novamente.";
    public static final String PERMISSION_DENIED_MESSAGE = "Permissão de gravação negada.";
}
