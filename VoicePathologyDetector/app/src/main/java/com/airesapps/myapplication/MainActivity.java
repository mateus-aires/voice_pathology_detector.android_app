package com.airesapps.myapplication;

import android.Manifest;
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
import com.airesapps.instructions.InstructionsAdapter;
import com.airesapps.instructions.InstructionsUtil;
import com.airesapps.util.Constants;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO
    };

    private TextView mInstructionTextView;
    private ImageView mStartStopImageView;
    private ImageView mAudioWave;
    private TextView mRecordingTextView;
    private ImageView mRestart;

    private ProgressBar mProgressBar;
    private Chronometer mChronometer;

    private PredictionDTO predictionDTO;


    private boolean mPermissionToRecordAccepted = false;
    private String mOutputFilePath;
    private String[] audioPaths;
    private MediaRecorder mRecorder;

    private ListView lvInstructions;
    private ImageView mHelp;

    private int mStep = 1;

    private boolean mIsRecording = false;

    private TextView mObservations;
    private TextView mListening;
    private TextView mListeningStep;
    private ImageView mBaloonView;
    private TextView mProcessing;
    private ImageView mBaloonView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mObservations = findViewById(R.id.observations);
        mBaloonView = findViewById(R.id.baloon);
        mInstructionTextView = findViewById(R.id.instruction);
        mListening = findViewById(R.id.listening);
        mListeningStep = findViewById(R.id.listening_step);
        mAudioWave = findViewById(R.id.audiowave);
        mProgressBar = findViewById(R.id.progress_bar);
        mChronometer = findViewById(R.id.chronometer);
        mProcessing = findViewById(R.id.processing);

        mStartStopImageView = findViewById(R.id.microphone_view);
        mHelp = findViewById(R.id.help_view);
        mRestart = findViewById(R.id.restart_view);


//        mHelp.setOnClickListener(v -> dialog.show());

        mRestart.setOnClickListener(v -> {
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
                startRecording();
                mStartStopImageView.setImageResource(R.drawable.stop_ic);

                mAudioWave.setVisibility(View.VISIBLE);
                mRecordingTextView.setVisibility(View.VISIBLE);

                mChronometer.setVisibility(View.VISIBLE);
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.setFormat("mm:ss");
                mChronometer.start();
                mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                    @Override
                    public void onChronometerTick(Chronometer chronometer) {
                        long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
                        if (elapsedMillis >= 8000) {
                            stopRecording();
                            mStartStopImageView.setImageResource(R.drawable.record_ic);
                            mChronometer.stop();
                            mChronometer.setVisibility(View.INVISIBLE);
                        } else {
                            chronometer.setText(DateFormat.format("mm:ss", elapsedMillis));
                        }
                    }
                });
            } else {
                stopRecording();
                mStartStopImageView.setImageResource(R.drawable.record_ic);

                mAudioWave.setVisibility(View.INVISIBLE);
                mRecordingTextView.setVisibility(View.INVISIBLE);

                mChronometer.stop();
                mChronometer.setVisibility(View.INVISIBLE);
            }
        });
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

        audioPaths[mStep - 1] = mOutputFilePath;

        // Update UI
        mIsRecording = false;
//        mRecordingTextView.setVisibility(View.INVISIBLE);
        mStep++;
        updateUI();
        if (mStep > 3) {
            // Perform prediction and display result
            performPrediction();
        }

    }

    private void updateUI() {
        switch (mStep) {
            case 1:
                mInstructionTextView.setText(R.string.step_1);
                break;
            case 2:
                mInstructionTextView.setText(R.string.step_2);
                break;
            case 3:
                mInstructionTextView.setText(R.string.step_3);
                break;
            case 4:
                mInstructionTextView.setText(R.string.processing);
                break;
        }
    }

    private void performPrediction() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        mProgressBar.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            try {
                predictionDTO =
                        PathologyPredictionClient.predict(new File(audioPaths[0]),
                                                          new File(audioPaths[1]),
                                                          new File(audioPaths[2]));
                handler.post(() -> {
                    mProgressBar.setVisibility(View.GONE);
                    handleResponse();
                });
            } catch (Exception e) {
                handler.post(() -> {
                    mProgressBar.setVisibility(View.GONE);
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
        mStartStopImageView.setImageResource(R.drawable.result_ic);
        mStartStopImageView.setClickable(false);
        String presentResult = getPredictionString();
        Toast.makeText(this, presentResult, Toast.LENGTH_SHORT).show();
        mInstructionTextView.setText(presentResult);

    }

    private void displayError() {
        String errorMessage = Constants.INTERNAL_ERROR_MESSAGE;
        if (!mPermissionToRecordAccepted) {
            errorMessage = Constants.PERMISSION_DENIED_MESSAGE;
        }

        else if (predictionDTO != null) {
            errorMessage = predictionDTO.getErrorCause();
        }
        mStartStopImageView.setImageResource(R.drawable.error_ic);
        mInstructionTextView.setText(errorMessage);
        mStartStopImageView.setClickable(false);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private String getPredictionString() {
        int predictionProbability = (int) Math.round(predictionDTO.getProbability() * 100);
        String presentResult = Constants.FALSE_PREDICTION_MESSAGE;
        if (predictionDTO.getResult()) {
            presentResult = Constants.TRUE_PREDICTION_MESSAGE;
        }
        return String.format(Constants.PRESENT_MESSAGE_FORMAT, presentResult, predictionProbability);
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