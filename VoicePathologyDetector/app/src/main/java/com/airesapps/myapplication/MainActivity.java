package com.airesapps.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.airesapps.client.PathologyPredictionClient;
import com.airesapps.dto.PredictionDTO;
import com.airesapps.util.Constants;

public class MainActivity extends AppCompatActivity {

    // Constants
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int STATE_LOGO = -1;
    private static final int STATE_INSTRUCTION = 0;
    private static final int STATE_RECORDING = 1;
    private static final int STATE_PROCESSING = 2;
    private static final int STATE_RESULT_SUCCESS = 3;
    private static final int STATE_RESULT_ERROR = 4;
    private static String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO};

    // Views
    private TextView tInstruction;
    private TextView mRecordingTextView;
    private TextView tObservations;
    private TextView tListening;
    private TextView tInitText;
    private TextView tListeningStep;
    private TextView tProcessing;
    private TextView tResult;
    private TextView tErrorMessage;
    private TextView tResultProba;
    private ImageView mStartStopImageView;
    private ImageView mAudioWave;
    private ImageView mRestart;
    private ImageView mHelp;
    private ImageView mBaloonView;
    private ImageView mInit;
    private ImageView mMicrophoneView;
    private ImageView close;
    private ProgressBar progressBar;
    private ListView lvInstructions;
    private RelativeLayout mRelativeLayoutParent;
    private View helpDialogBox;
    private Chronometer chronometer;

    // Other variables
    private PredictionDTO predictionDTO;
    private boolean mPermissionToRecordAccepted = false;
    private boolean mIsRecording = false;
    private int mStep = 1;
    private int mState = STATE_LOGO;
    private String mOutputFilePath;
    private String[] audioPaths;
    private MediaRecorder mRecorder;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tObservations = findViewById(R.id.observations);
        mBaloonView = findViewById(R.id.baloon);
        tInstruction = findViewById(R.id.instruction);
        tListening = findViewById(R.id.listening);
        tListeningStep = findViewById(R.id.listening_step);
        mAudioWave = findViewById(R.id.audiowave);
        progressBar = findViewById(R.id.progress_bar);
        chronometer = findViewById(R.id.chronometer);
        tProcessing = findViewById(R.id.processing);
        tResult = findViewById(R.id.result);
        tResultProba = findViewById(R.id.result_proba);

        mRelativeLayoutParent = findViewById(R.id.parent_view);

        mStartStopImageView = findViewById(R.id.microphone_view);
        mHelp = findViewById(R.id.help_view);
        mRestart = findViewById(R.id.restart_view);
        mInit = findViewById(R.id.init);
        tInitText = findViewById(R.id.init_text);
        tErrorMessage = findViewById(R.id.error_message);

        mInit.setOnClickListener(v -> {
            setState(STATE_INSTRUCTION);
        });

        helpDialogBox = getLayoutInflater().inflate(R.layout.instructions, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(helpDialogBox);
        AlertDialog dialog = builder.create();

        close = helpDialogBox.findViewById(R.id.close);
        close.setOnClickListener(v -> dialog.dismiss());


        mHelp.setOnClickListener(v -> dialog.show());

//        mHelp.setOnClickListener(v -> dialog.show());

        mRestart.setOnClickListener(v -> {
            setState(STATE_INSTRUCTION);
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        });


        this.audioPaths = new String[3];


        // Check if permission to record audio is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            mPermissionToRecordAccepted = true;
        }

        mStartStopImageView.setOnClickListener(v -> {
            if (!mIsRecording) {
                setState(STATE_RECORDING);
                startRecording();
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.setFormat("mm:ss");
                chronometer.start();
                chronometer.setOnChronometerTickListener(chronometer -> {
                    long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
                    if (elapsedMillis >= 8000) {
                        stopRecording();
//                        MainActivity.this.chronometer.stop();

                        if (mStep <= 3) {
                            setState(STATE_INSTRUCTION);
                        } else {
                            performPrediction();
                        }

//                        mStartStopImageView.setImageResource(R.drawable.record_ic);

//                        MainActivity.this.chronometer.setVisibility(View.INVISIBLE);
                    } else {
                        chronometer.setText(DateFormat.format("mm:ss", elapsedMillis));
                    }
                });
            } else {
                stopRecording();
//                chronometer.stop();

                if (mStep <= 3) {
                    setState(STATE_INSTRUCTION);
                } else {
                    performPrediction();
                }
            }
        });
    }

    private void setState(int state) {

        if (mState == STATE_LOGO) {
            mHelp.setVisibility(View.VISIBLE);
            mRestart.setVisibility(View.VISIBLE);

        }

        mState = state;

        setInvisible();
        switch (state) {
            case STATE_INSTRUCTION:


                mBaloonView.setVisibility(View.VISIBLE);
                tInstruction.setVisibility(View.VISIBLE);
                mStartStopImageView.setVisibility(View.VISIBLE);

                break;


            // startRecording
            case STATE_RECORDING:
                tListening.setVisibility(View.VISIBLE);
                tListeningStep.setVisibility(View.VISIBLE);
                mAudioWave.setVisibility(View.VISIBLE);
                mStartStopImageView.setImageResource(R.drawable.stop_ic);
                mStartStopImageView.setVisibility(View.VISIBLE);
                chronometer.setVisibility(View.VISIBLE);


                break;
            // startChronometer

            case STATE_PROCESSING:
                mRelativeLayoutParent.setBackgroundResource(R.drawable.backgroung_wave);
                progressBar.setVisibility(View.VISIBLE);
                tProcessing.setVisibility(View.VISIBLE);
                mHelp.setClickable(false);
                break;
            // startChronometer
            case STATE_RESULT_SUCCESS:
                mRelativeLayoutParent.setBackgroundResource(R.drawable.backgroung_result);
                tResult.setVisibility(View.VISIBLE);
                tResultProba.setVisibility(View.VISIBLE);
                mHelp.setClickable(true);
                break;
            case STATE_RESULT_ERROR:
                mRelativeLayoutParent.setBackgroundResource(R.drawable.backgroung_error);
                tErrorMessage.setVisibility(View.VISIBLE);
                mHelp.setClickable(true);
                break;
        }

    }

    private void setInvisible() {
//        mHelp.setVisibility(View.INVISIBLE);
//        mRestart.setVisibility(View.INVISIBLE);
//        mRecordingTextView.setVisibility(View.INVISIBLE);

        mRelativeLayoutParent.setBackgroundResource(R.drawable.backgroung_dark);
        mInit.setVisibility(View.INVISIBLE);
        tInitText.setVisibility(View.INVISIBLE);
        tObservations.setVisibility(View.INVISIBLE);

        mBaloonView.setVisibility(View.INVISIBLE);
        tInstruction.setVisibility(View.INVISIBLE);
        mStartStopImageView.setVisibility(View.INVISIBLE);
        mStartStopImageView.setImageResource(R.drawable.record_ic);

        tListening.setVisibility(View.INVISIBLE);
        tListeningStep.setVisibility(View.INVISIBLE);
        mAudioWave.setVisibility(View.INVISIBLE);
        chronometer.setVisibility(View.INVISIBLE);

        progressBar.setVisibility(View.INVISIBLE);
        tProcessing.setVisibility(View.INVISIBLE);

        tResult.setVisibility(View.INVISIBLE);
        tResultProba.setVisibility(View.INVISIBLE);

        tErrorMessage.setVisibility(View.INVISIBLE);

    }

    private void startRecording() {
        // Set output file path for audio recording
        mOutputFilePath = getExternalCacheDir().getAbsolutePath() + "/audio" + mStep + ".mp4";
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mOutputFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioSamplingRate(50000);
        mRecorder.setAudioEncodingBitRate(800000);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Start recording and update UI
        mRecorder.start();
        mIsRecording = true;
//        mRecordingTextView.setVisibility(View.VISIBLE);
//        mChronometer.setVisibility(View.VISIBLE);
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
        chronometer.stop();


        audioPaths[mStep - 1] = mOutputFilePath;

        // Update UI
        mIsRecording = false;
//        mRecordingTextView.setVisibility(View.INVISIBLE);
        mStep++;
        updateUI();
//        if (mStep > 3) {
//            // Perform prediction and display result
//            performPrediction();
//        }

    }

    private void updateUI() {
        switch (mStep) {
            case 1:
                tInstruction.setText(R.string.step_1);
                tListeningStep.setText(R.string.step_1_listening);
                break;
            case 2:
                tInstruction.setText(R.string.step_2);
                tListeningStep.setText(R.string.step_2_listening);
                break;
            case 3:
                tInstruction.setText(R.string.step_3);
                tListeningStep.setText(R.string.step_3_listening);
                break;
        }
    }

    private void performPrediction() {
        setState(STATE_PROCESSING);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());


        executor.execute(() -> {
            try {
                predictionDTO =
                        PathologyPredictionClient.predict(new File(audioPaths[0]),
                                new File(audioPaths[1]),
                                new File(audioPaths[2]));
                handler.post(() -> {
//                    progressBar.setVisibility(View.GONE);
                    handleResponse();
                });
            } catch (Exception e) {
                handler.post(() -> {
//                    progressBar.setVisibility(View.GONE);
                    handleResponse();
                });
            }
        });
    }

    private void handleResponse() {
        if (predictionDTO.isSuccessful()) {
            displayResult();
        } else {
            displayError();
        }
    }

    private void displayResult() {

        String presentResult = setPredictionString();

        Toast.makeText(this, presentResult, Toast.LENGTH_SHORT).show();
        setState(STATE_RESULT_SUCCESS);

    }

    private void displayError() {
        String errorMessage = Constants.INTERNAL_ERROR_MESSAGE;
        if (!mPermissionToRecordAccepted) {
            errorMessage = Constants.PERMISSION_DENIED_MESSAGE;
        } else if (predictionDTO != null) {
            errorMessage = predictionDTO.getErrorCause();
        }
//        mStartStopImageView.setImageResource(R.drawable.error_ic);
        tErrorMessage.setText(errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        setState(STATE_RESULT_ERROR);
    }

    private String setPredictionString() {
        int predictionProbability = (int) Math.round(predictionDTO.getProbability() * 100);
        String presentResult = Constants.FALSE_PREDICTION_MESSAGE;
        String presentResultProba = String.format(Constants.PRESENT_PROBA_FORMAT, predictionProbability) + "%";

        if (predictionDTO.getResult()) {
            presentResult = Constants.TRUE_PREDICTION_MESSAGE;
        }
        tResult.setText(presentResult);
        tResultProba.setText(presentResultProba);

        return presentResult;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mPermissionToRecordAccepted = true;
            } else {
                displayError();
            }
        }
    }
}