package com.example.spriteeditor;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    PixelCanvas pixelCanvas;
    private static final int PICK_IMAGE = 100;
    Uri imageUri;
    ImageButton btnPencil, btnEraser, btnColorPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pixelCanvas = findViewById(R.id.pc);
        pixelCanvas.post(new Runnable() {
            @Override
            public void run() {
                pixelCanvas.setBitmap(Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888));
                pixelCanvas.getRes();
            }
        });

        btnPencil = findViewById(R.id.btnPencil);
        btnPencil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pixelCanvas.brushColor = 0xFF000000;
            }
        });
        btnEraser = findViewById(R.id.btnEraser);
        btnEraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pixelCanvas.brushColor = 0x00000000;
            }
        });
        btnColorPicker = findViewById(R.id.btnColorPicker);
        btnColorPicker.setBackgroundColor(0xFF000000);
        btnColorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnColorPicker.setBackgroundColor(0xFF31FAB5);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sprite_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.importPicture:
                openGallery();
                break;
            case R.id.exportPicture:
                saveImageToGallery();
                break;
            case R.id.newSprite:
                newCanvas();
                default:
                    break;
        }
        return true;
    }

    public void newCanvas(){
        final String[] listSizes = getResources().getStringArray(R.array.canvasSizes);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Select a canvas size");
        dialogBuilder.setSingleChoiceItems(listSizes, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int canvasSize = Integer.parseInt(listSizes[which]);
                Bitmap newBitmap = Bitmap.createBitmap(canvasSize, canvasSize, Bitmap.Config.ARGB_8888);
                pixelCanvas.setBitmap(newBitmap);
                pixelCanvas.getRes();
                dialog.dismiss();
            }
        });
        dialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
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
                pixelCanvas.setBitmap(selectedImage);
                pixelCanvas.getRes();
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
            pixelCanvas.bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

//            MediaScannerConnection.scanFile(this,
//                    new String[] { imagePath.toString() }, null,
//                    new MediaScannerConnection.OnScanCompletedListener() {
//                        public void onScanCompleted(String path, Uri uri) {
//                            Log.i("ExternalStorage", "Scanned " + path + ":");
//                            Log.i("ExternalStorage", "-> uri=" + uri);
//                        }
//                    });
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
