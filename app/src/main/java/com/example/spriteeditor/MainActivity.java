package com.example.spriteeditor;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class MainActivity extends AppCompatActivity {
    PixelCanvas pixelCanvas;
    Button btnImport, btnExport;
    private static final int PICK_IMAGE = 100;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pixelCanvas = findViewById(R.id.pc);
        pixelCanvas.post(new Runnable() {
            @Override
            public void run() {
                pixelCanvas.setBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.piskel));
            }
        });

        btnImport = findViewById(R.id.btnImport);
        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btnExport = findViewById(R.id.btnExport);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImageToGallery();
            }
        });
    }

    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            try {
                imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                Bitmap bitmap = selectedImage.copy(Bitmap.Config.ARGB_8888, true);
//                bitmap.setWidth(32);
//                bitmap.setHeight(32);
                pixelCanvas.setBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String saveImageToGallery(){
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/Sprites");
        if (!directory.exists())
            Toast.makeText(this,
                    (directory.mkdirs() ? "Directory has been created" : "Directory not created"),
                    Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Directory exists", Toast.LENGTH_SHORT).show();

        File imagePath = new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            imagePath.createNewFile();
            fos = new FileOutputStream(imagePath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            pixelCanvas.bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Toast. makeText(this, directory.getAbsolutePath(), Toast.LENGTH_LONG).show();
        return directory.getAbsolutePath();
    }
}
