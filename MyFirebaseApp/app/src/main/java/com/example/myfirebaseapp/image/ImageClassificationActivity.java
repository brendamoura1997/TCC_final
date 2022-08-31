package com.example.myfirebaseapp.image;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.myfirebaseapp.ChangePasswordActivity;
import com.example.myfirebaseapp.DeleteProfileActivity;
import com.example.myfirebaseapp.MainActivity;
import com.example.myfirebaseapp.PhotoActivity;
import com.example.myfirebaseapp.R;
import com.example.myfirebaseapp.UpdateEmailActivity;
import com.example.myfirebaseapp.UpdateProfileActivity;
import com.example.myfirebaseapp.UserProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import com.example.myfirebaseapp.helpers.MLImageHelperActivity;
import com.example.myfirebaseapp.translate.Language;
import com.example.myfirebaseapp.translate.TranslateAPI;

public class ImageClassificationActivity extends MLImageHelperActivity {
    private ImageLabeler imageLabeler;
    private final static String TAG = "ImageClassification";
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageLabeler = ImageLabeling.getClient(new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.4f) //Confidence percentage
                .build());

        authProfile = FirebaseAuth.getInstance();
    }

    //CREATING ACTION BAR MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if(id == R.id.menu_foto){
            Intent intent = new Intent(ImageClassificationActivity.this, PhotoActivity.class);
            startActivity(intent);
        } else if(id == R.id.menu_view_profile){
            Intent intent = new Intent(ImageClassificationActivity.this, UserProfileActivity.class);
            startActivity(intent);
        }else if(id == R.id.menu_update_profile){
            Intent intent = new Intent(ImageClassificationActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        }else if(id == R.id.menu_update_email){
            Intent intent = new Intent(ImageClassificationActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
        }else if(id == R.id.menu_settings){
            Toast.makeText(this, "SETTINGS", Toast.LENGTH_SHORT).show();
        }else if(id == R.id.menu_change_password){
            Intent intent = new Intent(ImageClassificationActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        }else if(id == R.id.menu_delete_profile){
            Intent intent = new Intent(ImageClassificationActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
        } else if(id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(ImageClassificationActivity.this, "Deslogado", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ImageClassificationActivity.this, MainActivity.class);
            startActivity(intent);

            //Clear stack to prevent user coming back to UserProfileActivity pressing back button after Loggin out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); //CLOSE UserProfileActivity

        }else{
            Toast.makeText(ImageClassificationActivity.this, "Algo de errado aconteceu!", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void runDetection(Bitmap bitmap) {
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        Bitmap finalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);

        imageLabeler.process(inputImage).addOnSuccessListener(imageLabels -> {
            StringBuilder sb = new StringBuilder();
            //List<BoxWithText> boxes = new ArrayList();

            for (ImageLabel label : imageLabels) {

                sb.append(label.getText()).append("\n");

                TranslateAPI translateAPI = new TranslateAPI(
                        Language.ENGLISH,
                        Language.PORTUGUESE,
                        sb.toString());

                translateAPI.setTranslateListener(new TranslateAPI.TranslateListener() {
                    @Override
                    public void onSuccess(String translatedText) {
                        getOutputTextView4().setText("");
                        getOutputTextResult().setText("Resultado");
                        getOutputTextTranslation().setText("Tradução");
                        getOutputTextView().setText(translatedText);
                    }

                    @Override
                    public void onFailure(String ErrorText) {
                        //Log.d(TAG, "onFailure: " + ErrorText);
                    }
                });

                TranslateAPI translateAPI2 = new TranslateAPI(
                        Language.ENGLISH,
                        Language.RUSSIAN,
                        sb.toString());

                translateAPI2.setTranslateListener(new TranslateAPI.TranslateListener() {
                    @Override
                    public void onSuccess(String translatedText) {
                        getOutputTextView2().setText(translatedText);
                    }

                    @Override
                    public void onFailure(String ErrorText) {
                        Log.d(TAG, "onFailure: " + ErrorText);
                        if (ErrorText == "Network Error"){
                            Toast.makeText(ImageClassificationActivity.this, "Erro de rede da Api", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

            if (imageLabels.isEmpty()) {
                getOutputTextView().setText("Não pôde ser classificado!!!");
            }
        }).addOnFailureListener(e -> {
            e.printStackTrace();
        });
    }
}
