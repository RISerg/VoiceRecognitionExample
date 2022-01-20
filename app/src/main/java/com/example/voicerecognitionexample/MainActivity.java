package com.example.voicerecognitionexample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import voicerecognitionexample.R;

public class MainActivity extends AppCompatActivity {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
    private Button btnSpeak;
    private TextView tvText;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private Boolean listening = false;
    private MainActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkVoiceRecognition();

        activity = this;
        btnSpeak = findViewById(R.id.btn_speak);
        tvText = findViewById(R.id.tv_recognised_text);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        prepareSpeechRecognizerIntent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
        }
    }

    public void onBtnSpeakClick(View view) {
        if (listening) {
            stopListening();
        } else {
            startListening();
        }
    }

    public void onBtnClearClick(View view) {
        tvText.setText("");
    }

    public void checkVoiceRecognition() {
        // Check if voice recognition is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0) {
            btnSpeak.setEnabled(false);
            btnSpeak.setText(R.string.voice_recognizer_not_present);
            Toast.makeText(this, R.string.voice_recognizer_not_present, Toast.LENGTH_SHORT).show();
        }
        // Check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        }
    }

    public void prepareSpeechRecognizerIntent() {
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Display an hint to the user about what he should say.
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.you_may_speak_now));

        // Given an hint to the recognizer about what the user is going to say
        //There are two form of language model available
        //1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
        //2.LANGUAGE_MODEL_FREE_FORM  : If not sure about the words or phrases and its domain.
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                stopListening();
            }

            @Override
            public void onError(int error) {
                stopListening();
                String errorText = "Error detected";
                switch (error) {
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        errorText += " network timeout";
//                        startListening();
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        errorText += " network";
                        return;
                    case SpeechRecognizer.ERROR_AUDIO:
                        errorText += " audio";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        errorText += " server";
//                        startListening();
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        errorText += " client";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        errorText += " speech time out";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        errorText += " no match";
//                        startListening();
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        errorText += " recogniser busy";
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        errorText += " insufficient permissions";
                        break;
                }
                Toast.makeText(activity, errorText, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (!data.isEmpty())
                    appendText(data.get(0));
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
    }

    private void startListening() {
        btnSpeak.setText(R.string.btnSpeak_listening);
        speechRecognizer.startListening(speechRecognizerIntent);
        this.listening = true;
    }

    private void stopListening() {
        btnSpeak.setText(R.string.btn_speak);
        speechRecognizer.stopListening();
        listening = false;
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, VOICE_RECOGNITION_REQUEST_CODE);
        }
    }

    private void appendText(String text) {
        @SuppressLint("DefaultLocale")
        String newText = String.format("%tT > %s\n", Calendar.getInstance(), text);
        tvText.append(newText);
    }
}