package com.example.spriteeditor;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

public class MainActivity extends AppCompatActivity {
    PixelCanvas pixelCanvas;
    ImageView imageView;
    private static final int PICK_IMAGE = 100;
    public static final int SET_COLOR = 1;
    Uri imageUri;
    ImageButton btnTool, btnPencil, btnLine, btnFloodFill, btnCut, btnRect, btnCircle, btnEraser, btnBrushColor, btnBrushSize, btnUndo, btnRedo;
    int[] brushImageId;
    HorizontalScrollView colorBar, toolBar;
    LinearLayout colorBarContainer, toolBarContainer;
    ConstraintLayout colorPickBar;
    View coverView;
    String[] colorCodes;
    private Resources resources;

    int[] argb = new int[4];
    SeekBar[] argbSeekBar = new SeekBar[4];
    EditText editText;

    String imageName;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resources = getResources();
        imageView = findViewById(R.id.imageView);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SET_COLOR:
                        btnBrushColor.setBackgroundColor(msg.getData().getInt("color"));
                        break;
                    default:
                        imageView.setImageBitmap(Bitmap.createScaledBitmap
                                (pixelCanvas.bitmap, 256, 256, false));
                }
            }
        };

        pixelCanvas = findViewById(R.id.pc);
        pixelCanvas.post(new Runnable() {
            @Override
            public void run() {
                pixelCanvas.setBitmap(
                        Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888));
                pixelCanvas.getRes();
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
        ImageButton button = findViewById(R.id.btnColorPicker);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pixelCanvas.setMode(PixelCanvas.DRAWMODE.PICK);
                btnTool.setImageResource(R.drawable.color_picker);
                btnTool.setTag(R.drawable.color_picker);
                hidePopupBar();
            }
        });
        colorPickBar = findViewById(R.id.colorPickBar);
        ImageButton btnCustomColor = findViewById(R.id.btnCustomColor);
        btnCustomColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupBar();
                setSeekBarValues();
                colorPickBar.setVisibility(View.VISIBLE);
                coverView.setVisibility(View.VISIBLE);
            }
        });
        editText = findViewById(R.id.editText);
        editText.setText(Integer.toHexString(pixelCanvas.brushColor));

        btnTool = findViewById(R.id.btnTool);
        btnTool.setImageResource(R.drawable.pencil);
        btnTool.setTag(R.drawable.pencil);
        btnTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolBar.setVisibility(View.VISIBLE);
                coverView.setVisibility(View.VISIBLE);
            }
        });

        btnPencil = findViewById(R.id.btnPencil);
        btnPencil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pixelCanvas.eraser) {
                    pixelCanvas.setMode(PixelCanvas.DRAWMODE.PEN);
                }
                btnTool.setImageResource(R.drawable.pencil);
                btnTool.setTag(R.drawable.pencil);
                hidePopupBar();
            }
        });

        btnLine = findViewById(R.id.btnLine);
        btnLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pixelCanvas.eraser) {
                    pixelCanvas.setMode(PixelCanvas.DRAWMODE.LINE);
                }
                btnTool.setImageResource(R.drawable.line);
                btnTool.setTag(R.drawable.line);
                hidePopupBar();
            }
        });

        btnFloodFill = findViewById(R.id.btnFloodFill);
        btnFloodFill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pixelCanvas.eraser) {
                    pixelCanvas.setMode(PixelCanvas.DRAWMODE.FILL);
                }
                btnTool.setImageResource(R.drawable.flood_fill);
                btnTool.setTag(R.drawable.flood_fill);
                hidePopupBar();
            }
        });

        btnCut = findViewById(R.id.btnCut);
        btnCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pixelCanvas.eraser) {
                    pixelCanvas.setMode(PixelCanvas.DRAWMODE.CUT);
                }
                btnTool.setImageResource(R.drawable.cut);
                btnTool.setTag(R.drawable.cut);
                hidePopupBar();
            }
        });

        btnRect = findViewById(R.id.btnRect);
        btnRect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pixelCanvas.eraser) {
                    pixelCanvas.setMode(PixelCanvas.DRAWMODE.RECT);
                }
                btnTool.setImageResource(R.drawable.rect);
                btnTool.setTag(R.drawable.rect);
                hidePopupBar();
            }
        });

        btnCircle = findViewById(R.id.btnCircle);
        btnCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pixelCanvas.eraser) {
                    pixelCanvas.mode = PixelCanvas.DRAWMODE.CIRCLE;
                }
                btnTool.setImageResource(R.drawable.circle);
                btnTool.setTag(R.drawable.circle);
                hidePopupBar();
            }
        });

        btnEraser = findViewById(R.id.btnEraser);
        btnEraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pixelCanvas.eraser) {
                    pixelCanvas.eraser = false;
                    Integer drawableId = (Integer) btnTool.getTag();
                    switch (drawableId) {
                        case R.drawable.pencil:
                            pixelCanvas.setMode(PixelCanvas.DRAWMODE.PEN);
                            break;
                        case R.drawable.line:
                            pixelCanvas.setMode(PixelCanvas.DRAWMODE.LINE);
                            break;
                        case R.drawable.flood_fill:
                            pixelCanvas.mode = PixelCanvas.DRAWMODE.FILL;
                            break;
                        case R.drawable.cut:
                            pixelCanvas.mode = PixelCanvas.DRAWMODE.CUT;
                            break;
                        case R.drawable.rect:
                            pixelCanvas.mode = PixelCanvas.DRAWMODE.RECT;
                            break;
                    }
                    pixelCanvas.brushColor = ColorUtils.setAlphaComponent(pixelCanvas.brushColor, 255);
                    btnEraser.setBackgroundColor(0x00000000);

                } else {
                    pixelCanvas.eraser = true;
                    pixelCanvas.setMode(PixelCanvas.DRAWMODE.PEN);
                    pixelCanvas.brushColor = ColorUtils.setAlphaComponent(pixelCanvas.brushColor, 0);
                    btnEraser.setBackgroundColor(0xFF9E9E9E);
                }
            }
        });

        btnBrushColor = findViewById(R.id.btnBrushColor);
        btnBrushColor.setBackgroundColor(0xFF000000);
        btnBrushColor.setOnClickListener(new View.OnClickListener() {
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
                if (pixelCanvas.historyCounter < 4) {
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

        brushImageId = new int[3];
        brushImageId[0] = R.drawable.brush_small;
        brushImageId[1] = R.drawable.brush_medium;
        brushImageId[2] = R.drawable.brush_large;
        btnBrushSize = findViewById(R.id.btnBrushSize);

        btnBrushSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pixelCanvas.brushSize >= 2) {
                    pixelCanvas.brushSize = 0;
                    btnBrushSize.setImageResource(brushImageId[pixelCanvas.brushSize]);
                } else {
                    btnBrushSize.setImageResource(brushImageId[++pixelCanvas.brushSize]);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            try {
                imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                pixelCanvas.setBitmap(selectedImage);
                pixelCanvas.getRes();
                pixelCanvas.newHistory();

                String fileName = "";
                if (imageUri.getScheme().equals("file")) {
                    fileName = imageUri.getLastPathSegment();
                } else {
                    Cursor cursor = null;
                    try {
                        cursor = getContentResolver().query(imageUri, new String[]{
                                MediaStore.Images.ImageColumns.DISPLAY_NAME
                        }, null, null, null);

                        if (cursor != null && cursor.moveToFirst()) {
                            fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                        }
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                System.out.println(fileName);
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
        switch (id){
            case R.id.newSprite:
                newCanvas();
                break;
            case R.id.saveDraft:
                saveImageToGallery("Draft");
                break;
            case R.id.importPicture:
                openGallery();
                break;
            case R.id.exportPicture:
                saveImageToGallery("");
                break;
                default:
                    break;
        }
        return true;
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

    private String saveImageToGallery(String subFolder){
        File directory = createDirectory(subFolder);
        if(!getSupportActionBar().getTitle().toString().equals("PixarT")&&
                subFolder.endsWith("Draft")){
            imageName = getSupportActionBar().getTitle().toString().trim();
            saveFile(directory);
        }else{
            openSpriteNameDialog(directory);
        }

        return directory.getAbsolutePath();
    }

    private File createDirectory(String subFolder){
        String directoryPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Sprites";
        if(!(subFolder.isEmpty()||subFolder==null)){
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

    private void openSpriteNameDialog(final File directory){
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

    private void saveFile(File directory){
        if(!imageName.endsWith(".jpg")){
            imageName += ".jpg";
        }
        File imagePath = new File(directory,imageName);
        FileOutputStream fos = null;
        try {
            if(directory.getAbsolutePath().toString().endsWith("Draft")){
                imagePath.createNewFile();
                fos = new FileOutputStream(imagePath);
                pixelCanvas.bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }else{
                final int destWidth = 512;
                final int destHeight = 512;
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(pixelCanvas.bitmap, destWidth, destHeight,false);
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                // compress to the format you want, JPEG, PNG...
                // 70 is the 0-100 quality percentage
                scaledBitmap.compress(Bitmap.CompressFormat.PNG,100 , outStream);
                imagePath.createNewFile();
                fos = new FileOutputStream(imagePath);
                fos.write(outStream.toByteArray());
            }
            notifyMediaStoreScanner(imagePath);
            Toast.makeText(this,"File "+imageName+" is saved!",Toast.LENGTH_LONG).show();
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
            MediaStore.Images.Media.insertImage(this.getContentResolver(),
                    file.getAbsolutePath(), file.getName(), null);
            this.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void customColor(View view) {
        int i = 0;
        pixelCanvas.setBrushColor(Color.argb(argb[i++], argb[i++], argb[i++], argb[i]));
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
                pixelCanvas.getRes();
                pixelCanvas.newHistory();
                btnBrushColor.setBackgroundColor(pixelCanvas.brushColor);
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
        if (colorPickBar.isShown()) {
            colorPickBar.setVisibility(View.GONE);
        }
        coverView.setVisibility(View.GONE);
    }

    public void loadColorBar() {
        String rawColorCodes = loadFile("color_codes");
        colorCodes = rawColorCodes.split("\\r?\\n");
        ImageButton[] colorButtons = new ImageButton[colorCodes.length];

        int length40 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        int length20 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

        for (int i = 0; i < colorButtons.length; i++) {
            colorButtons[i] = new ImageButton(this);
            final int colorCode = Color.parseColor("#" + colorCodes[i]);
            colorButtons[i].setBackgroundColor(colorCode);
            colorButtons[i].setTag(i);
            colorButtons[i].setId(i);

            colorButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!pixelCanvas.eraser) {
                        pixelCanvas.setBrushColor(colorCode);
                    }
                    hidePopupBar();
                }
            });

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(length40, length40);
            layoutParams.setMargins(length20, length20, length20, length20);
            colorButtons[i].setLayoutParams(layoutParams);

            colorBarContainer.addView(colorButtons[i]);
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
        int hex = pixelCanvas.brushColor;
        for (int i = 0; i < 4; i++) {
            int bit = 8 * (3 - i);
            argb[i] = (hex >> bit) & 255;
            argbSeekBar[i].setProgress(argb[i]);
        }
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }
}
