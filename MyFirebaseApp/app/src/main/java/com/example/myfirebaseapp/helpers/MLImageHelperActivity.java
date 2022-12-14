package com.example.myfirebaseapp.helpers;

import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.example.myfirebaseapp.R;

public abstract class MLImageHelperActivity extends AppCompatActivity {

    public final String LOG_TAG = "MLImageHelper";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public final static int PICK_IMAGE_ACTIVITY_REQUEST_CODE = 1064;
    public final static int REQUEST_READ_EXTERNAL_STORAGE = 2031;
    File photoFile;

    private ImageView inputImageView;
    private TextView outputTextView;
    private TextView outputTextView2;
    private TextView outputTextView4;
    private TextView outputTextResult;
    private TextView outputTextTranslation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_helper);

        inputImageView = findViewById(R.id.imageView);
        outputTextView = findViewById(R.id.textView);
        outputTextView2 = findViewById(R.id.textView2);
        outputTextView4 = findViewById(R.id.textView4);
        outputTextResult = findViewById(R.id.textResult);
        outputTextTranslation = findViewById(R.id.textTranslation);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
            }
        }

    }

    public void onTakeImage(View view) {
        // Cria intent para tirar uma foto e devolver o controle ao aplicativo de chamada
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Cria uma refer??ncia de arquivo para acesso futuro
        photoFile = getPhotoFileUri(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg");

        // envolve o objeto File em um provedor de conte??do
        Uri fileProvider = FileProvider.getUriForFile(this, "com.brenda.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);


        if (intent.resolveActivity(getPackageManager()) != null) {
            // Inicia a tentativa de captura de imagem para tirar uma foto
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Retorna o arquivo para uma foto armazenada no disco dado o nome do arquivo
    public File getPhotoFileUri(String fileName) {
        // Obt??m um diret??rio de armazenamento seguro para fotos
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), LOG_TAG);

        // Cria o diret??rio de armazenamento se ele n??o existir
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(LOG_TAG, "falha ao criar diret??rio");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    /**
     * getCapturedImage():
     * Decodes and crops the captured image from camera.
     */
    private Bitmap getCapturedImage() {
        // Get the dimensions of the View
        int targetW = inputImageView.getWidth();
        int targetH = inputImageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH));

        bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inMutable = true;
        return BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
    }

    private void rotateIfRequired(Bitmap bitmap) {
        try {
            ExifInterface exifInterface = new ExifInterface(photoFile.getAbsolutePath());
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
            );

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                rotateImage(bitmap, 90f);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                rotateImage(bitmap, 180f);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                rotateImage(bitmap, 270f);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Rotate the given bitmap.
     */
    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(
                source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true
        );
    }

    public void onPickImage(View view) {
        // cria intent para tirar uma foto e devolver o controle ao aplicativo de chamada
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        if (intent.resolveActivity(getPackageManager()) != null) {
            // Inicia a intent de captura de imagem para tirar a foto
            startActivityForResult(intent, PICK_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    protected TextView getOutputTextView() {
        return outputTextView;
    }

    protected TextView getOutputTextView2() {
        return outputTextView2;
    }

    protected TextView getOutputTextView4() {
        return outputTextView4;
    }

    protected TextView getOutputTextResult() {
        return outputTextResult;
    }

    protected TextView getOutputTextTranslation() {
        return outputTextTranslation;
    }

    protected ImageView getInputImageView() {
        return inputImageView;
    }

    protected abstract void runDetection(Bitmap bitmap);

    protected Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;

        try {
            if (Build.VERSION.SDK_INT > 27) {
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = getCapturedImage();
                rotateIfRequired(bitmap);
                inputImageView.setImageBitmap(bitmap);
                runDetection(bitmap);
            } else { // resultado falhou
                Toast.makeText(this, "A foto n??o foi tirada!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap takenImage = loadFromUri(data.getData());
                inputImageView.setImageBitmap(takenImage);
                runDetection(takenImage);
            } else { // resultado falhou
                Toast.makeText(this, "A imagem n??o foi selecionada!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}