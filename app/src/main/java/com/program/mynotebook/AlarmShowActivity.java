package com.program.mynotebook;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

/**
 * Created by Administrator on 2017/06/23.
 * 展示notification打开的界面
 * 与EditNoteActivity的解析时一样的
 */

public class AlarmShowActivity extends Activity {

    private static final String TAG = "fuck";
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alarmshow_activity);
        editText = (EditText) findViewById(R.id.show_alarm_et);

        findViewById(R.id.alarmShowConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //判断传递过来的数据
        //是否有Media 如果没有Media 就直接显示 如果有 就解析
        if (getIntent().getExtras() != null) {
            String media = getIntent().getExtras().getString("media_path", null);
            if (media != null) {
                Log.i(TAG, "onCreate: ");
                initEditTextView();
            } else {
                Log.i(TAG, "onCreate: asdasdasd");
                String text = getIntent().getExtras().getString("content");
                editText.setText(text);
                editText.setSelection(text != null ? text.length() : 0);
            }
        }
        editText.setFocusable(false);
    }

    //存在图片 去解析 并显示
    private void initEditTextView() {
        String mediaPath = getIntent().getExtras().getString("media_path");
        String content = getIntent().getExtras().getString("content");

        String[] contents = content.split("\\+");
        //字符串最后也有一个加号，所以下面循环时 length要-1
        String[] mediaPaths = mediaPath.split("\\+");
        Log.i(TAG, "media" + mediaPaths[0]);
        if (contents.length > 0) {
            editText.append(contents[0]);
            editText.setSelection(contents[0].length());
        }
        for (int i = 0; i < mediaPaths.length; i++) {
            insertBitmapIntoEditText(getBitmap(mediaPaths[i]), mediaPaths[i]);
            if (contents.length >= i + 2) {
                editText.append(contents[i + 1]);
                editText.setSelection(contents[i + 1].length());
            }
        }
    }

    private Bitmap getBitmap(String bitmap_path) {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;    //屏幕宽度（像素）
        //在这里还要处理一下 如果Bitmap过大通常会导致OOM
        Bitmap bitmap = BitmapFactory.decodeFile(bitmap_path);
        if (bitmap != null) {
            int bWidth = bitmap.getWidth();
            int bHeight = bitmap.getHeight();
            if ((bWidth / width) >= 0.5) {
                float scale = 0.4f;
                Matrix mx = new Matrix();
                mx.postScale(scale, scale);
                return Bitmap.createBitmap(bitmap, 0, 0, bWidth, bHeight, mx, true);
            }
        }
        return bitmap;
    }

    private void insertBitmapIntoEditText(Bitmap bitmap, String path) {
        int start = editText.getSelectionStart();
        SpannableString ss = new SpannableString(path);
        ImageSpan imageSpan = new ImageSpan(this, bitmap);
        String extentThing = " ";
        ss.setSpan(imageSpan, 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        editText.append(ss);
        editText.append(extentThing);
        editText.setSelection(start + path.length() + extentThing.length());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                finish();
                break;
        }


        return super.onKeyDown(keyCode, event);
    }
}
