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
import com.airesapps.util.ObservationsUtil;

public class MainActivity extends AppCompatActivity {


    // Views
    private TextView tInstruction;
    private TextView tListening;
    private TextView tInitText;
    private TextView tListeningStep;
    private TextView tProcessing;
    private TextView tResult;
    private TextView tErrorMessage;
    private TextView tResultProbability;
    private TextView tDialogObservations;
    private ImageView mStartStopImageView;
    private ImageView mAudioWave;
    private ImageView mRestart;
    private ImageView mHelp;
    private ImageView mBalloonView;
    private ImageView mInit;
    private ImageView close;
    private ProgressBar progressBar;
    private RelativeLayout mRelativeLayoutParent;
    private View helpDialogBox;
    private Chronometer chronometer;

    // Other variables
    private PredictionDTO predictionDTO;
    private boolean mPermissionToRecordAccepted = false;
    private boolean mIsRecording = false;
    private int mStep = 1;
    private int currentState = Constants.STATE_LOGO;
    private String mOutputFilePath;
    private String[] audioPaths;
    private MediaRecorder mRecorder;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        initViews();

        // Configure init button
        mInit.setOnClickListener(v -> {
            setState(Constants.STATE_INSTRUCTION);
        });

        // Configure help dialog box
        helpDialogBox = getLayoutInflater().inflate(R.layout.instructions, null);
        tDialogObservations = helpDialogBox.findViewById(R.id.dialog_text);
        tDialogObservations.setText(ObservationsUtil.getObservations());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(helpDialogBox);
        AlertDialog dialog = builder.create();

        close = helpDialogBox.findViewById(R.id.close);
        close.setOnClickListener(v -> dialog.dismiss());

        mHelp.setOnClickListener(v -> dialog.show());

        // Configure the reset button
        mRestart.setOnClickListener(v -> {
            setState(Constants.STATE_INSTRUCTION);
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        });

        // Initialize audio paths
        this.audioPaths = new String[3];

        // Checks if permission to record audio has been granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, Constants.PERMISSIONS, Constants.REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            mPermissionToRecordAccepted = true;
        }

        // Configure the record/stop button
        mStartStopImageView.setOnClickListener(v -> {
            if (!mIsRecording) {
                // Start recording
                setState(Constants.STATE_RECORDING);
                startRecording();

                chronometer.setOnChronometerTickListener(chronometer -> {
                    long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
                    if (elapsedMillis >= 8000) {
                        // Stop recording
                        stopRecording();

                        if (mStep <= 3) {
                            setState(Constants.STATE_INSTRUCTION);
                        } else {
                            performPrediction();
                        }

                    } else {
                        chronometer.setText(DateFormat.format(Constants.CHRONOMETER_FORMAT, elapsedMillis));
                    }
                });
            } else {
                // Stop recording
                stopRecording();

                if (mStep <= 3) {
                    setState(Constants.STATE_INSTRUCTION);
                } else {
                    performPrediction();
                }
            }
        });
    }


    private void setState(int state) {
        if (currentState == Constants.STATE_LOGO) {
            mHelp.setVisibility(View.VISIBLE);
            mRestart.setVisibility(View.VISIBLE);
        }

        // Update current state
        currentState = state;

        // Hide views
        setViewsInvisibility();

        // Makes specific views visible for each state
        switch (state) {
            case Constants.STATE_INSTRUCTION:
                mBalloonView.setVisibility(View.VISIBLE);
                tInstruction.setVisibility(View.VISIBLE);
                mStartStopImageView.setVisibility(View.VISIBLE);
                break;

            case Constants.STATE_RECORDING:
                tListening.setVisibility(View.VISIBLE);
                tListeningStep.setVisibility(View.VISIBLE);
                mAudioWave.setVisibility(View.VISIBLE);
                mStartStopImageView.setImageResource(R.drawable.stop_ic);
                mStartStopImageView.setVisibility(View.VISIBLE);
                chronometer.setVisibility(View.VISIBLE);
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.setText(Constants.CHRONOMETER_INIT_TEXT);
                chronometer.start();
                chronometer.setFormat(Constants.CHRONOMETER_FORMAT);
                break;

            case Constants.STATE_PROCESSING:
                mRelativeLayoutParent.setBackgroundResource(R.drawable.backgroung_wave);
                progressBar.setVisibility(View.VISIBLE);
                tProcessing.setVisibility(View.VISIBLE);
                mHelp.setClickable(false);
                mRestart.setClickable(false);
                break;

            case Constants.STATE_RESULT_SUCCESS:
                mRelativeLayoutParent.setBackgroundResource(R.drawable.backgroung_result);
                tResult.setVisibility(View.VISIBLE);

                if (predictionDTO.getResult()) {
                    tResultProbability.setVisibility(View.VISIBLE);
                }
                break;

            case Constants.STATE_RESULT_ERROR:
                mRelativeLayoutParent.setBackgroundResource(R.drawable.backgroung_error);
                tErrorMessage.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void startRecording() {
        // Sets the output file path for audio recording
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

        // Starts recording and updates the UI
        mRecorder.start();
        mIsRecording = true;
        chronometer.start();
    }

    private void stopRecording() {
        // Stops recording and releases MediaRecorder resources
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
        chronometer.stop();

        // Saves the recorded audio file path and updates the current step
        audioPaths[mStep - 1] = mOutputFilePath;
        mStep++;

        // Updates the UI
        mIsRecording = false;
        updateUI();

        // If all steps have been completed, perform the prediction
        if (mStep > 3) {
            performPrediction();
        }
    }

    private void updateUI() {
        // Atualiza o texto das instruções e a mensagem de "ouvindo"
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
        //Sets the UI state to "processing"
        setState(Constants.STATE_PROCESSING);

        // Creates a new thread to perform the prediction
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                // Perform prediction with recorded audio files
                predictionDTO = PathologyPredictionClient.predict(
                        new File(audioPaths[0]),
                        new File(audioPaths[1]),
                        new File(audioPaths[2])
                );

                handler.post(() -> {
                    handleResponse();
                });
            } catch (Exception e) {
                handler.post(() -> {
                    handleResponse();
                });
            }
        });
    }

    private void handleResponse() {
        //Checks whether the prediction was successful and displays the result or error message
        if (predictionDTO.isSuccessful()) {
            displayResult();
        } else {
            displayError();
        }
    }

    private void displayResult() {
        String presentResult = setPredictionString();
        Toast.makeText(this, presentResult, Toast.LENGTH_SHORT).show();
        setState(Constants.STATE_RESULT_SUCCESS);
    }


    private void displayError() {
        String errorMessage = Constants.INTERNAL_ERROR_MESSAGE;
        if (!mPermissionToRecordAccepted) {
            errorMessage = Constants.PERMISSION_DENIED_MESSAGE;
        } else if (predictionDTO != null) {
            errorMessage = predictionDTO.getErrorCause();
        }

        tErrorMessage.setText(errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        setState(Constants.STATE_RESULT_ERROR);
    }


    private String setPredictionString() {
        int predictionProbability = (int) Math.round(predictionDTO.getProbability() * 100);
        String presentResult = Constants.FALSE_PREDICTION_MESSAGE;
        String presentResultProba = String.format(Constants.PRESENT_PROBA_FORMAT, predictionProbability) + "%";

        if (predictionDTO.getResult()) {
            presentResult = Constants.TRUE_PREDICTION_MESSAGE;
        }

        tResult.setText(presentResult);
        tResultProbability.setText(presentResultProba);

        return presentResult;
    }

    private void initViews() {
        mRelativeLayoutParent = findViewById(R.id.parent_view);
        mBalloonView = findViewById(R.id.baloon);
        tInstruction = findViewById(R.id.instruction);
        tListening = findViewById(R.id.listening);
        tListeningStep = findViewById(R.id.listening_step);
        mAudioWave = findViewById(R.id.audiowave);
        progressBar = findViewById(R.id.progress_bar);
        chronometer = findViewById(R.id.chronometer);
        tProcessing = findViewById(R.id.processing);
        tResult = findViewById(R.id.result);
        tResultProbability = findViewById(R.id.result_proba);
        mStartStopImageView = findViewById(R.id.microphone_view);
        mHelp = findViewById(R.id.help_view);
        mRestart = findViewById(R.id.restart_view);
        mInit = findViewById(R.id.init);
        tInitText = findViewById(R.id.init_text);
        tErrorMessage = findViewById(R.id.error_message);

    }

    private void setViewsInvisibility() {

        mRelativeLayoutParent.setBackgroundResource(R.drawable.backgroung_dark);
        mInit.setVisibility(View.INVISIBLE);
        tInitText.setVisibility(View.INVISIBLE);
        mBalloonView.setVisibility(View.INVISIBLE);
        tInstruction.setVisibility(View.INVISIBLE);
        mStartStopImageView.setVisibility(View.INVISIBLE);

        tListening.setVisibility(View.INVISIBLE);
        tListeningStep.setVisibility(View.INVISIBLE);
        mAudioWave.setVisibility(View.INVISIBLE);
        chronometer.setVisibility(View.INVISIBLE);

        progressBar.setVisibility(View.INVISIBLE);
        tProcessing.setVisibility(View.INVISIBLE);

        tResult.setVisibility(View.INVISIBLE);
        tResultProbability.setVisibility(View.INVISIBLE);

        tErrorMessage.setVisibility(View.INVISIBLE);

        mHelp.setClickable(true);
        mRestart.setClickable(true);
        mStartStopImageView.setImageResource(R.drawable.record_ic);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mPermissionToRecordAccepted = true;
            } else {
                displayError();
            }
        }
    }
}