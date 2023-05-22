package com.airesapps.util;

import android.Manifest;

public class Constants {

    // MainActivity constants
    public static String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO};
    public static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    public static final int STATE_LOGO = -1;
    public static final int STATE_INSTRUCTION = 0;
    public static final int STATE_RECORDING = 1;
    public static final int STATE_PROCESSING = 2;
    public static final int STATE_RESULT_SUCCESS = 3;
    public static final int STATE_RESULT_ERROR = 4;
    public static final String CHRONOMETER_FORMAT = "mm:ss";
    public static final String CHRONOMETER_INIT_TEXT = "00:00";
    public static final String AUDIO_NAME_FORMAT = "/audio%d.mp4";

    // PathologyPredictionClient constants
    public static final String PROD_URL = "https://flask-production-a030.up.railway.app/predict";
    public static final String LOCAL_URL = "http://10.0.2.2:5000/predict";

    // Messages related constants
    public static final String TRUE_PREDICTION_MESSAGE = "Sua voz está apresentando sintomas de disfonia.";
    public static final String FALSE_PREDICTION_MESSAGE = "Sua voz parece estar saudável.";
    public static final String PRESENT_PROBA_FORMAT = "Probabilidade de estar doente: %d";
    public static final String CONNECTION_ERROR_MESSAGE = "Erro de conexão, verifique sua internet.";
    public static final String INTERNAL_ERROR_MESSAGE = "Erro interno, tente novamente.";
    public static final String PERMISSION_DENIED_MESSAGE = "Permissão de gravação negada.";
    public static final String AUDIO_TOO_SHORT_ERROR_MESSAGE = "Gravação audível muito curta. Fale por mais de três segundos.";
}
