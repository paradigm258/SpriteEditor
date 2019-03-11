package com.example.spriteeditor;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    PixelCanvas pixelCanvas;
    private static final int PICK_IMAGE = 100;
    Uri imageUri;
    ImageButton btnPencil, btnEraser, btnColorPicker, btnBrushSize, btnUndo, btnRedo;
    int[] brushImageId;
    HorizontalScrollView colorBar;
    LinearLayout colorBarContainer;
    View coverView;
    String[] colorCodes;
    private Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resources = getResources();


        pixelCanvas = findViewById(R.id.pc);
        pixelCanvas.post(new Runnable() {
            @Override
            public void run() {
                pixelCanvas.setBitmap(Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888));
                pixelCanvas.getRes();
                loadColorBar();
            }
        });

        colorBar = findViewById(R.id.colorBar);
        colorBarContainer = findViewById(R.id.colorBarContainer);
        coverView = findViewById(R.id.coverView);
        coverView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupBar();
            }
        });

        btnPencil = findViewById(R.id.btnPencil);
        btnPencil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pixelCanvas.brushColor = 0xFF000000;
                btnColorPicker.setBackgroundColor(0xFF000000);
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
                colorBar.setVisibility(View.VISIBLE);
                coverView.setVisibility(View.VISIBLE);
            }
        });

        btnUndo = findViewById(R.id.btnUndo);
        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pixelCanvas.historyCounter>=1){
                    if(pixelCanvas.lastBitmap==null){
                        pixelCanvas.lastBitmap = pixelCanvas.bitmap.copy(Bitmap.Config.ARGB_8888,true);
                    }
                    pixelCanvas.setBitmap(pixelCanvas.bitmapHistory[--pixelCanvas.historyCounter]);
                }
            }
        });
        btnRedo = findViewById(R.id.btnRedo);
        btnRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pixelCanvas.historyCounter<4){
                    if(pixelCanvas.bitmapHistory[pixelCanvas.historyCounter+1]!= null){
                        pixelCanvas.setBitmap(pixelCanvas.bitmapHistory[++pixelCanvas.historyCounter]);
                    }else if(pixelCanvas.bitmapHistory[pixelCanvas.historyCounter+1]== null&&pixelCanvas.lastBitmap!=null){
                        pixelCanvas.setBitmap(pixelCanvas.lastBitmap);
                        pixelCanvas.lastBitmap = null;
                        pixelCanvas.historyCounter++;
                    }
                }else{
                    if(pixelCanvas.lastBitmap!=null) {
                        pixelCanvas.setBitmap(pixelCanvas.lastBitmap);
                        pixelCanvas.lastBitmap = null;
                        pixelCanvas.historyCounter++;
                    }
                }
            }
        });

        brushImageId = new int[3];
        brushImageId[0] = R.drawable.brush_small;
        brushImageId[1] = R.drawable.brush_medium;
        brushImageId[2] = R.drawable.brush_large;
        btnBrushSize = findViewById(R.id.btnBrushSize);

        btnBrushSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    public void hidePopupBar(){
        if(colorBar.isShown()){
            colorBar.setVisibility(View.GONE);
        }
        coverView.setVisibility(View.GONE);
    }

    public void loadColorBar(){
        String rawColorCodes = loadFile("color_codes");
        colorCodes = rawColorCodes.split("\\r?\\n");
        ImageButton[] colorButtons = new ImageButton[colorCodes.length];

        int length40 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        int length20 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

        for (int i = 0; i < colorButtons.length; i++) {
            colorButtons[i] = new ImageButton(this);
            final int colorCode = Color.parseColor("#"+colorCodes[i]);
            colorButtons[i].setBackgroundColor(colorCode);
            colorButtons[i].setTag(i);
            colorButtons[i].setId(i);

            colorButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pixelCanvas.brushColor = colorCode;
                    btnColorPicker.setBackgroundColor(colorCode);
                    hidePopupBar();
                }
            });

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(length40, length40);
            layoutParams.setMargins(length20, length20, length20, length20);
            colorButtons[i].setLayoutParams(layoutParams);

            colorBarContainer.addView(colorButtons[i]);
        }
    }

    public String loadFile(String fileName){
        InputStream inputStream;
        int rID = resources.getIdentifier(fileName, "raw", getPackageName());
        inputStream = resources.openRawResource(rID);
        String output = "";
        byte[] buffer;

        try {
            buffer = new byte[inputStream.available()];
            //read the text file as a stream, into the buffer
            inputStream.read(buffer);
            //create a output stream to write the buffer into
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //write this buffer to the output stream
            outputStream.write(buffer);
            //Close the Input and Output streams
            outputStream.close();
            inputStream.close();
            output = outputStream.toString();
            //return the output stream as a String
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
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
                pixelCanvas.newHistory();
                btnColorPicker.setBackgroundColor(pixelCanvas.brushColor);
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
                Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Sprites");
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
