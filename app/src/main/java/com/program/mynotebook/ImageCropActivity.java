package com.program.mynotebook;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.edmodo.cropper.CropImageView;

/**
 * Created by Administrator on 2017/06/08.
 */

public class ImageCropActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String path = getIntent().getExtras().getString("path", null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.actuivity_image_crop);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        CropImageView cropImageView = (CropImageView) findViewById(R.id.cropImageView);
        cropImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onClick(View v) {

    }
}
