package com.example.solution_color;


import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.example.CONSTANTS;
import com.library.bitmap_utilities.BitMap_Helpers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity  {
    private File externalStorage;
    private File originalFile;
    private ImageView defaultPic;
    private int screenHeight;
    private int screenWidth;
    private Bitmap imageBitmap;
    private SharedPreferences myPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;
        externalStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        originalFile = new File(externalStorage, "originalFile.jpg");
        imageBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.gutters);
        //Camera_Helpers.saveProcessedImage(imageBitmap,originalFile.getAbsolutePath());
        setContentView(R.layout.activity_main);
        defaultPic = (ImageView)findViewById(R.id.defaultPicture);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        toolbar.getBackground().setAlpha(150);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent myIntent = new Intent(this, SettingsActivity.class);
                startActivity(myIntent);
                break;
            case R.id.reset:
                reset();
                break;
            case R.id.blackAndWhite:
                sketch();
                break;
            case R.id.colorize:
                color();
                break;
            case R.id.share:
                share();
            default:
                break;
        }
        return true;
    }

    private void share() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("application/image");
        sendIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse("file://" + originalFile.getAbsolutePath()));
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, myPrefs.getString("key_share_subject", ""));
        sendIntent.putExtra(Intent.EXTRA_TEXT, myPrefs.getString("key_share_text", ""));
        startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.Title)));

    }

    private void color() {
        Bitmap sketch = getSketchBitmap();
        Bitmap colorBit = getColorBitmap();
        BitMap_Helpers.merge(colorBit, sketch);
        Camera_Helpers.saveProcessedImage(colorBit, originalFile.getAbsolutePath());
        defaultPic.setImageBitmap(colorBit);

    }

    private void sketch() {
        Camera_Helpers.saveProcessedImage(getSketchBitmap(),originalFile.getAbsolutePath());
        defaultPic.setImageBitmap(getSketchBitmap());
    }
    private Bitmap getColorBitmap(){
        String stringThreshold = myPrefs.getString("key_saturation","50");
        int threshold = Integer.parseInt(stringThreshold);
        Bitmap color = BitMap_Helpers.colorBmp(imageBitmap, threshold);

        return color;
    }
    private Bitmap getSketchBitmap(){
        String stringThreshold = myPrefs.getString("key_sketchiness","50");
        int threshold = Integer.parseInt(stringThreshold);
        Bitmap bitmap = BitMap_Helpers.thresholdBmp(imageBitmap, threshold);

        return bitmap;
    }

    private void reset() {
        Camera_Helpers.delSavedImage(originalFile.getAbsolutePath());
        imageBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.gutters);
        Camera_Helpers.saveProcessedImage(imageBitmap,originalFile.getAbsolutePath());
        defaultPic.setImageResource(R.drawable.gutters);
        defaultPic.setScaleType(ImageView.ScaleType.FIT_CENTER);
        defaultPic.setScaleType(ImageView.ScaleType.FIT_XY);
        }

    public void takePicture(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        externalStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        originalFile = new File (externalStorage, "originalFile.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, originalFile);
        startActivityForResult(intent, CONSTANTS.TAKE_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if (resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            Camera_Helpers.saveProcessedImage(imageBitmap,originalFile.getAbsolutePath());
            imageBitmap = Camera_Helpers.loadAndScaleImage(originalFile.getAbsolutePath(),screenHeight,screenWidth);
            defaultPic = (ImageView)findViewById(R.id.defaultPicture);
            defaultPic.setImageBitmap(imageBitmap);
        }
        else {
            System.out.println("close");
        }
    }
}

