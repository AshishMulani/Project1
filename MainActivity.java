package com.example.myapplication9;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private EditText editText;
    private Button speakButton;
    private Button selectFileButton;
    private TextToSpeech textToSpeech;
    private File selectedFile;
    private Button clearButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        speakButton = findViewById(R.id.speakButton);
        selectFileButton = findViewById(R.id.selectfileButton);
        clearButton = findViewById(R.id.clearButton);


        textToSpeech = new TextToSpeech(getApplicationContext(), this);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }


        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                // speak the text
                int result = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                if (result == TextToSpeech.ERROR) {
                    Log.e("TTS", "Error in converting Text to Speech!");
                }            }
        });

        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, 1);

            }
        });


        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editText.setText("");
            }
        });
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // set language
            int result = textToSpeech.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // show error if language is not supported
                speakButton.setEnabled(false);
                Toast.makeText(MainActivity.this, "Language not supported!", Toast.LENGTH_SHORT).show();
            } else {
                speakButton.setEnabled(true);
            }
        } else {
            // show error if TTS is not initialized
            speakButton.setEnabled(false);
            Toast.makeText(MainActivity.this, "TTS not initialized!", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            // get the URI of the selected file
            Uri uri = data.getData();
            selectedFile = new File(uri.getPath());

            try {
                ContentResolver contentResolver = getContentResolver();

                InputStream inputStream = getContentResolver().openInputStream(uri);


                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n"); // add line break
                }
                reader.close();
                inputStream.close();
                String formattedText = sb.toString().trim(); // remove any leading/trailing white spaces

                // remove unwanted characters and replace with space
                formattedText = formattedText.replaceAll("[^a-zA-Z0-9\\s+]", " ");

                // split text into lines
                String[] lines = formattedText.split("\n");

                // create a StringBuilder to hold the formatted text
                StringBuilder formattedTextBuilder = new StringBuilder();

                // loop through each line and append it to the StringBuilder
                for (String l : lines) {
                    formattedTextBuilder.append(l.trim());
                    formattedTextBuilder.append("\n");
                }

                // set the formatted text on the EditText
                editText.setText(formattedTextBuilder.toString());

                // speak the text
                int result = textToSpeech.speak(formattedText, TextToSpeech.QUEUE_FLUSH, null);
                if (result == TextToSpeech.ERROR) {
                    Log.e("TTS", "Error in converting Text to Speech!");
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Error reading file", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted, do your work
            } else {
                // permission denied, show message to user
                Toast.makeText(MainActivity.this, "Permission denied to read external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // shutdown TTS engine when activity is destroyed
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
