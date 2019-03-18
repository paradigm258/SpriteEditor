package com.example.spriteeditor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public static final int TOGGLE_ERASER = 2;
    private static final int PICK_IMAGE = 100;
    public static final int SET_COLOR = 1;

    PixelCanvas pixelCanvas;
    ImageView overView;
    ImageButton btnTool, btnPencil, btnLine, btnFloodFill, btnCut,
            btnColorPicker, btnCustomColor, btnRect, btnCircle,
            btnEraser, btnBrushColor, btnBrushSize, btnUndo, btnRedo;
    final int[] brushImageId = {
            R.drawable.brush_small,
            R.drawable.brush_medium,
            R.drawable.brush_large
    };
    HorizontalScrollView colorBar, toolBar;
    LinearLayout colorBarContainer, toolBarContainer;
    ConstraintLayout customColorBar;
    View coverView;
    String[] colorCodes;
    private Resources resources;
    String rawColorCodes;

    int[] argb = new int[4];
    SeekBar[] argbSeekBar = new SeekBar[4];
    EditText editText;
    ImageButton customColor;

    String imageName;
    MyHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resources = getResources();
        overView = findViewById(R.id.overView);

        handler = new MyHandler(this);

        pixelCanvas = findViewById(R.id.pc);
        pixelCanvas.post(new Runnable() {
            @Override
            public void run() {
                pixelCanvas.setBitmap(
                        Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888));
                pixelCanvas.setResolution();
                loadColorBar();
                setupSeekBars();
            }
        });
        pixelCanvas.setHandler(handler);

        colorBar = findViewById(R.id.colorBar);
        colorBarContainer = findViewById(R.id.colorBarContainer);
        toolBar = findViewById(R.id.toolBar);
        toolBarContainer = findViewById(R.id.toolBarContainer);
        coverView = findViewById(R.id.coverView);
        coverView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupBar();
            }
        });

        customColorBar = findViewById(R.id.customColorBar);
        btnCustomColor = findViewById(R.id.btnCustomColor);
        btnCustomColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupBar();
                setSeekBarValues();
                customColorBar.setVisibility(View.VISIBLE);
                coverView.setVisibility(View.VISIBLE);
            }
        });
        editText = findViewById(R.id.editText);
        editText.setText(Integer.toHexString(pixelCanvas.getBrushColor()));
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        customColor = findViewById(R.id.customColor);

        btnTool = findViewById(R.id.btnTool);
        btnTool.setImageResource(R.drawable.pencil);
        btnTool.setTag(R.drawable.pencil);
        btnTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupBar();
                toolBar.setVisibility(View.VISIBLE);
                coverView.setVisibility(View.VISIBLE);
            }
        });

        btnBrushColor = findViewById(R.id.btnBrushColor);
        btnBrushColor.setBackgroundColor(0xFF000000);
        btnBrushColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupBar();
                colorBar.setVisibility(View.VISIBLE);
                coverView.setVisibility(View.VISIBLE);
            }
        });

        btnPencil = findViewById(R.id.btnPencil);
        btnPencil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToolBarButtonHandler(PixelCanvas.DRAWMODE.PEN, R.drawable.pencil);
            }
        });

        btnLine = findViewById(R.id.btnLine);
        btnLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToolBarButtonHandler(PixelCanvas.DRAWMODE.LINE, R.drawable.line);
            }
        });

        btnFloodFill = findViewById(R.id.btnFloodFill);
        btnFloodFill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToolBarButtonHandler(PixelCanvas.DRAWMODE.FILL, R.drawable.flood_fill);
            }
        });

        btnColorPicker = findViewById(R.id.btnColorPicker);
        btnColorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToolBarButtonHandler(PixelCanvas.DRAWMODE.PICK, R.drawable.color_picker);
            }
        });

        btnCut = findViewById(R.id.btnCut);
        btnCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToolBarButtonHandler(PixelCanvas.DRAWMODE.CUT, R.drawable.cut);
            }
        });

        btnRect = findViewById(R.id.btnRect);
        btnRect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToolBarButtonHandler(PixelCanvas.DRAWMODE.RECT, R.drawable.rect);
            }
        });

        btnCircle = findViewById(R.id.btnCircle);
        btnCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToolBarButtonHandler(PixelCanvas.DRAWMODE.CIRCLE, R.drawable.circle);
            }
        });

        btnEraser = findViewById(R.id.btnEraser);
        btnEraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pixelCanvas.setEraser(!pixelCanvas.getEraser());
            }
        });

        btnUndo = findViewById(R.id.btnUndo);
        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pixelCanvas.historyCounter >= 1) {
                    if (pixelCanvas.lastBitmap == null) {
                        pixelCanvas.lastBitmap = pixelCanvas.bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    }
                    pixelCanvas.setBitmap(pixelCanvas.bitmapHistory[--pixelCanvas.historyCounter]);
                }
            }
        });
        btnRedo = findViewById(R.id.btnRedo);
        btnRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pixelCanvas.historyCounter < pixelCanvas.historySize - 1) {
                    if (pixelCanvas.bitmapHistory[pixelCanvas.historyCounter + 1] != null) {
                        pixelCanvas.setBitmap(pixelCanvas.bitmapHistory[++pixelCanvas.historyCounter]);
                    } else if (pixelCanvas.bitmapHistory[pixelCanvas.historyCounter + 1] == null && pixelCanvas.lastBitmap != null) {
                        pixelCanvas.setBitmap(pixelCanvas.lastBitmap);
                        pixelCanvas.lastBitmap = null;
                        pixelCanvas.historyCounter++;
                    }
                } else {
                    if (pixelCanvas.lastBitmap != null) {
                        pixelCanvas.setBitmap(pixelCanvas.lastBitmap);
                        pixelCanvas.lastBitmap = null;
                        pixelCanvas.historyCounter++;
                    }
                }
            }
        });


        btnBrushSize = findViewById(R.id.btnBrushSize);

        btnBrushSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pixelCanvas.getBrushSize() >= 2) {
                    pixelCanvas.setBrushSize(0);
                    btnBrushSize.setImageResource(brushImageId[pixelCanvas.getBrushSize()]);
                } else {
                    int newBrushSize = pixelCanvas.getBrushSize() + 1;
                    btnBrushSize.setImageResource(brushImageId[newBrushSize]);
                    pixelCanvas.setBrushSize(newBrushSize);
                }
            }
        });

        checkPermission();
    }

    public void setToolBarButtonHandler(PixelCanvas.DRAWMODE drawmode, int resource) {
        if (!pixelCanvas.getEraser()) {
            pixelCanvas.setMode(drawmode);
        } else {
            pixelCanvas.setPreMode(drawmode);
        }
        btnTool.setImageResource(resource);
        btnTool.setTag(resource);
        hidePopupBar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                pixelCanvas.setBitmap(selectedImage);
                pixelCanvas.setResolution();
                pixelCanvas.newHistory();

                String fileName = "";
                if (imageUri.getScheme().equals("file")) {
                    fileName = imageUri.getLastPathSegment();
                } else {
                    try (Cursor cursor = getContentResolver().query(imageUri, new String[]{
                            MediaStore.Images.ImageColumns.DISPLAY_NAME
                    }, null, null, null)) {

                        if (cursor != null && cursor.moveToFirst()) {
                            fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                        }
                    }
                }
                getSupportActionBar().setTitle(fileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sprite_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.newSprite:
                newCanvas();
                break;
            case R.id.saveDraft:
                saveImageToGallery("Draft", false);
                break;
            case R.id.saveDraftAs:
                saveImageToGallery("Draft", true);
                break;
            case R.id.importPicture:
                openGallery();
                break;
            case R.id.exportPicture:
                saveImageToGallery("", true);
                break;
            default:
                break;
        }
        return true;
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    public String loadFile(String fileName) {
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

    private String saveImageToGallery(String subFolder, boolean newName) {
        File directory = createDirectory(subFolder);
        if (!getSupportActionBar().getTitle().toString().equals("PixarT") &&
                subFolder.endsWith("Draft")) {
            if (!newName) {
                imageName = getSupportActionBar().getTitle().toString().trim();
                saveFile(directory);
            } else {
                openSpriteNameDialog(directory);
            }
        } else {
            openSpriteNameDialog(directory);
        }

        return directory.getAbsolutePath();
    }

    private File createDirectory(String subFolder) {
        String directoryPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Sprites";
        if (!(subFolder == null || subFolder.isEmpty())) {
            directoryPath += "/" + subFolder;
        }

        File directory = new File(directoryPath);
        if (!directory.exists()) {
            Toast.makeText(this,
                    (directory.mkdirs() ? "Directory has been created" : "Directory not created"),
                    Toast.LENGTH_SHORT).show();
        }
        return directory;
    }

    private void openSpriteNameDialog(final File directory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sprite Name");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                imageName = input.getText().toString().trim();
                saveFile(directory);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void saveFile(File directory) {
        if (!imageName.endsWith(".jpg")) {
            imageName += ".jpg";
        }
        File imagePath = new File(directory, imageName);
        FileOutputStream fos = null;
        try {
            if (directory.getAbsolutePath().endsWith("Draft")) {
                imagePath.createNewFile();
                fos = new FileOutputStream(imagePath);
                pixelCanvas.bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } else {
                final int destWidth = 512;
                final int destHeight = 512;
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(pixelCanvas.bitmap, destWidth, destHeight, false);
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                // compress to the format you want, JPEG, PNG...
                // 70 is the 0-100 quality percentage
                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                imagePath.createNewFile();
                fos = new FileOutputStream(imagePath);
                fos.write(outStream.toByteArray());
            }
            notifyMediaStoreScanner(imagePath);
            getSupportActionBar().setTitle(imageName);
            Toast.makeText(this, "File " + imageName + " is saved!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public final void notifyMediaStoreScanner(final File file) {
        try {
            this.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        if (pixelCanvas.bitmapHistory[0] != null) {
            if (getSupportActionBar().getTitle().toString().equals("PixarT")) {
                String directoryPath = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Sprites/Draft";
                File directory = new File(directoryPath);
                if (!directory.exists()) {
                    if (directory.mkdirs()) {
                        Toast.makeText(this, "Can't create directory", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                }
                @SuppressLint("SimpleDateFormat")
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                imageName = "IMG_" + timeStamp + ".jpg";
                saveFile(directory);
            } else {
                saveImageToGallery("Draft", false);
            }
        }
        super.onStop();
    }

    public void newCanvas() {
        final String[] listSizes = getResources().getStringArray(R.array.canvasSizes);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Select a canvas size");
        dialogBuilder.setSingleChoiceItems(listSizes, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int canvasSize = Integer.parseInt(listSizes[which]);
                Bitmap newBitmap = Bitmap.createBitmap(canvasSize, canvasSize, Bitmap.Config.ARGB_8888);
                pixelCanvas.setBitmap(newBitmap);
                pixelCanvas.setResolution();
                pixelCanvas.newHistory();
                getSupportActionBar().setTitle("PixarT");
                btnBrushColor.setBackgroundColor(pixelCanvas.getBrushColor());
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

    public void hidePopupBar() {
        if (colorBar.isShown()) {
            colorBar.setVisibility(View.GONE);
        }
        if (toolBar.isShown()) {
            toolBar.setVisibility(View.GONE);
        }
        if (customColorBar.isShown()) {
            customColorBar.setVisibility(View.GONE);
        }
        coverView.setVisibility(View.GONE);
    }

    public void loadColorBar() {
        rawColorCodes = loadFile("color_codes");
        colorCodes = rawColorCodes.split("\\r?\\n");

        for (String colorCode1 : colorCodes) {
            int colorCode = Color.parseColor("#" + colorCode1);
            addNewColor(colorCode1, false);
        }
    }

    public void addNewColor(String color, boolean toTop) {
        int length40 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        int length20 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

        int a = Color.parseColor("#" + color);
        final int colorCode = a;

        ImageButton colorButton = new ImageButton(this);
        colorButton.setBackgroundColor(colorCode);
        colorButton.setTag(colorCode);
        colorButton.setId(colorCode + 0);

        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pixelCanvas.getEraser()) {
                    pixelCanvas.setBrushColor(colorCode);
                }
                hidePopupBar();
            }
        });

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(length40, length40);
        layoutParams.setMargins(length20, length20, length20, length20);
        colorButton.setLayoutParams(layoutParams);

        if (toTop) {
            colorBarContainer.addView(colorButton, 0);
        } else {
            colorBarContainer.addView(colorButton);
        }
    }

    private void setupSeekBars() {
        final int[] seekBarID = {R.id.aSeekBar, R.id.rSeekBar, R.id.gSeekBar, R.id.bSeekBar};
        SeekBar.OnSeekBarChangeListener changeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                StringBuilder color = new StringBuilder("#");
                for (int i = 0; i < 4; i++) {
                    if (seekBar.getId() == seekBarID[i]) {
                        argb[i] = progress;
                    }
                    color.append(String.format("%02X", argb[i]));
                }
                editText.setText(color.toString());
                customColor.setBackgroundColor(Color.argb(argb[0], argb[1], argb[2], argb[3]));
                hideKeyboard(editText);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        for (int i = 0; i < 4; i++) {
            argbSeekBar[i] = findViewById(seekBarID[i]);
            argbSeekBar[i].setOnSeekBarChangeListener(changeListener);
        }
        setSeekBarValues();
    }

    private void setSeekBarValues() {
        int hex = pixelCanvas.getBrushColor();
        for (int i = 0; i < 4; i++) {
            int bit = 8 * (3 - i);
            argb[i] = (hex >> bit) & 255;
            argbSeekBar[i].setProgress(argb[i]);
        }
        customColor.setBackgroundColor(Color.argb(argb[0], argb[1], argb[2], argb[3]));
    }

    public void customColor(View view) {
        String hex = editText.getText().toString().trim();
        if (hex.length() == 9) {
            for (int i = 0; i < 4; i++) {
                int start = 1 + i * 2;
                int end = start + 2;
                try {
                    argb[i] = Integer.parseInt(hex.substring(start, end), 16);
                } catch (Exception e) {
                    e.printStackTrace();
                    StringBuilder color = new StringBuilder("#");
                    for (int j = 0; j < 4; j++) {
                        argb[j] = argbSeekBar[j].getProgress();
                        color.append(String.format("%02X", argb[j]));
                    }
                    editText.setText(color.toString());
                }
                argbSeekBar[i].setProgress(argb[i]);
            }
            customColor.setBackgroundColor(Color.argb(argb[0], argb[1], argb[2], argb[3]));
        } else {
            StringBuilder color = new StringBuilder("#");
            for (int i = 0; i < 4; i++) {
                argb[i] = argbSeekBar[i].getProgress();
                color.append(String.format("%02X", argb[i]));
            }
            editText.setText(color.toString());
        }
        customColor.setBackgroundColor(Color.argb(argb[0], argb[1], argb[2], argb[3]));
        pixelCanvas.setBrushColor(Color.argb(argb[0], argb[1], argb[2], argb[3]));
        String newColor = editText.getText().toString().trim().substring(3);
        if (!rawColorCodes.contains(newColor)) {
            rawColorCodes += "\n" + newColor;
            addNewColor(newColor, true);
        }
        hideKeyboard(view);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void checkPermission() {
        int readPermission = this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (readPermission != PackageManager.PERMISSION_GRANTED ||
                writePermission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Permission required")
                        .setMessage("PixarT app need permission to save image to and " +
                                "import image from you storage!");
                builder.setPositiveButton("Give permission",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermission();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                requestPermission();
            }
        }
    }

    private final int PERMISSION_REQUEST_CODE = 100;

    public void requestPermission() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> activity;

        private MyHandler(MainActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = activity.get();
            if (mainActivity != null) {
                mainActivity.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case SET_COLOR:
                btnBrushColor.setBackgroundColor(msg.getData().getInt("color"));
                break;
            case TOGGLE_ERASER:
                btnEraser.setBackgroundColor(msg.getData().getInt("color"));
                break;
            default:
                overView.setImageBitmap(Bitmap.createScaledBitmap
                        (pixelCanvas.bitmap, 256, 256, false));
        }
    }

}
