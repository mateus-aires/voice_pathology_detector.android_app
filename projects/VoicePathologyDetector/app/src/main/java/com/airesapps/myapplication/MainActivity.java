package com.airesapps.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import androidx.annotation.NonNull;
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

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO
    };

    private TextView mTextView;
    private ImageView mImageView;
    private TextView mRecordingTextView;
    private Button mStopButton;

    private ProgressBar mProgressBar;

    private PredictionDTO predictionDTO;


    private boolean mPermissionToRecordAccepted = false;
    private String mOutputFilePath;
    private String[] audioPaths;
    private MediaRecorder mRecorder;

    private int mStep = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text_view);
        mImageView = findViewById(R.id.image_view);
        mRecordingTextView = findViewById(R.id.recording_text_view);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);


        this.audioPaths = new String[3];

        // Disable stop button until recording starts
        mStopButton.setEnabled(false);

        // Check if permission to record audio is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            mPermissionToRecordAccepted = true;
        }

        mImageView.setOnClickListener(v -> {
            if (mPermissionToRecordAccepted) {
                startRecording();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, REQUEST_RECORD_AUDIO_PERMISSION);
            }
        });

        mStopButton.setOnClickListener(v -> {
            stopRecording();
            if (mStep < 3) {
                mStep++;
                updateUI();
            } else {
                // Perform prediction and display result
                performPrediction();
                // Enable record button to allow user to record again
                mImageView.setEnabled(true);
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
        mImageView.setEnabled(false);
        mStopButton.setEnabled(true);
        mRecordingTextView.setVisibility(View.VISIBLE);
        updateUI();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;

        audioPaths[mStep - 1] = mOutputFilePath;

        // Update UI
        mStopButton.setEnabled(false);
        mRecordingTextView.setVisibility(View.INVISIBLE);
        // Delay to prevent recording from being restarted too quickly
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            // Enable record button to allow user to record again
            mImageView.setEnabled(true);
        }, 500);

    }

    private void updateUI() {
        switch (mStep) {
            case 1:
                mTextView.setText(R.string.step_1);
                break;
            case 2:
                mTextView.setText(R.string.step_2);
                break;
            case 3:
                mTextView.setText(R.string.step_3);
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
                                new File(audioPaths[1]), new File(audioPaths[2]));
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
        String presentResult = getPredictionString();
        Toast.makeText(this, presentResult, Toast.LENGTH_SHORT).show();
        mTextView.setText(presentResult);
    }

    private void displayError() {
        String errorMessage = Constants.INTERNAL_ERROR_MESSAGE;
        if (predictionDTO != null) {
            errorMessage = predictionDTO.getErrorCause();
        }

        mTextView.setText(errorMessage);
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
                Toast.makeText(this, "Permission denied to record audio", Toast.LENGTH_SHORT).show();
            }
        }
    }
}