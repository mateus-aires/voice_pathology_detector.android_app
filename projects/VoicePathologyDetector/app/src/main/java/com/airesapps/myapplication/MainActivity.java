package com.airesapps.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaRecorder;
import android.os.Bundle;
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

    private String predictionResult;


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
        mStopButton = findViewById(R.id.stop_button);
        mProgressBar = findViewById(R.id.progressBar);



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

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPermissionToRecordAccepted) {
                    startRecording();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, REQUEST_RECORD_AUDIO_PERMISSION);
                }
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                if (mStep < 3) {
                    mStep++;
                    updateUI();
                } else {
                    // Perform prediction and display result
                    String predictionResult = performPrediction();
                    Toast.makeText(MainActivity.this, predictionResult, Toast.LENGTH_SHORT).show();
                    // Enable record button to allow user to record again
                    mImageView.setEnabled(true);
                }
            }
        });
    }

//    private void configureProgressBar() {
//        mProgressBar = new ProgressBar(getApplicationContext(), null, android.R.attr.progressBarStyleHorizontal);
//        LayoutParams lp = new LayoutParams(
//                550, // Width in pixels
//                LayoutParams.WRAP_CONTENT // Height of progress bar
//        );
//        mProgressBar.setLayoutParams(lp);
//        LayoutParams params = (LayoutParams) mProgressBar.getLayoutParams();
//        params.addRule(RelativeLayout.ABOVE, mImageView.getId());
//        mProgressBar.setLayoutParams(params);
//        mProgressBar.getProgressDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
//    }

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
        // Stop recording and release resources
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        audioPaths[mStep-1] = mOutputFilePath;

        // Update UI
        mStopButton.setEnabled(false);
        mRecordingTextView.setVisibility(View.INVISIBLE);
        // Delay to prevent recording from being restarted too quickly
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Enable record button to allow user to record again
                mImageView.setEnabled(true);
            }
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

    private String performPrediction() {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        mProgressBar.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            predictionResult =
                    PathologyPredictionClient.predict(new File(audioPaths[0]),
                            new File(audioPaths[1]), new File(audioPaths[2]));
            handler.post(() -> {
                mProgressBar.setVisibility(View.GONE);
            });
        });

        return "Prediction result: " + predictionResult;

//         Perform prediction logic here and return result string

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