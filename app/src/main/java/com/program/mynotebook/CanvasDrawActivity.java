package com.program.mynotebook;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by Administrator on 2017/06/12.
 * <p>
 * 这是一个activity
 * <p>
 * 可以在上面绘制自己想要的图形
 */

public class CanvasDrawActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "test";
    private static boolean IS_ERASER = false;
    private LinearLayout saveCanvas;
    private LinearLayout dropCanvas;
    private LinearLayout eraser;
    private ImageView showCanvas;
    //绘画需要的三个工具
    private Canvas canvas;
    private Bitmap bitmap;
    private Paint paint = null;
    private Paint circle = null;

    private float startX, startY;
    private float stopX, stopY;

    //底部未做成adapter 做了9个imageView
    private ImageView blue;
    private ImageView blue1;
    private ImageView green;
    private ImageView grey;
    private ImageView orange;
    private ImageView orange1;
    private ImageView purple;
    private ImageView qing;
    private ImageView red;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.drawcanvas_acrivity);
        initView();

        //初始化画笔
        paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setColor(Color.BLACK);

        //初始化橡皮擦
        circle = new Paint();
        circle.setStrokeWidth(5);
        circle.setColor(Color.WHITE);

        //Log.i(TAG, "length" + String.valueOf(showCanvas.getWidth()));
    }

    private void initView() {
        saveCanvas = (LinearLayout) findViewById(R.id.saveCanvas);
        dropCanvas = (LinearLayout) findViewById(R.id.dropCanvas);
        eraser = (LinearLayout) findViewById(R.id.eraser);
        showCanvas = (ImageView) findViewById(R.id.showCanvas);

        blue = (ImageView) findViewById(R.id.blue);
        blue1 = (ImageView) findViewById(R.id.blue1);
        green = (ImageView) findViewById(R.id.green);
        grey = (ImageView) findViewById(R.id.grey);
        orange = (ImageView) findViewById(R.id.orange);
        orange1 = (ImageView) findViewById(R.id.orange1);
        purple = (ImageView) findViewById(R.id.purple);
        qing = (ImageView) findViewById(R.id.qing);
        red = (ImageView) findViewById(R.id.red);
        blue.setOnClickListener(this);
        blue1.setOnClickListener(this);
        green.setOnClickListener(this);
        grey.setOnClickListener(this);
        orange.setOnClickListener(this);
        orange1.setOnClickListener(this);
        purple.setOnClickListener(this);
        qing.setOnClickListener(this);
        red.setOnClickListener(this);

        eraser.setOnClickListener(this);
        dropCanvas.setOnClickListener(this);
        saveCanvas.setOnClickListener(this);
        findViewById(R.id.backToEditActivity).setOnClickListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //定义手指开始的坐标
        switch (event.getAction()) {
            //按下时的操作
            case MotionEvent.ACTION_DOWN:
//                Log.i(TAG, "length" + String.valueOf(showCanvas.getWidth()));
//                Log.i(TAG, "length" + String.valueOf(showCanvas.getWidth()));
                if (bitmap == null) {
                    //控件在onCreate里面 初始化时，获取到的高和宽是为0的
                    //初始化画布
                    bitmap = Bitmap.createBitmap(showCanvas.getWidth(),
                            showCanvas.getHeight(), Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.WHITE);
                }
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //记录刚开始位置
                //startX = event.getX();
                //startY = event.getY();

                //记录移动位置
                stopX = event.getX();
                stopY = event.getY();

                //根据两点坐标 绘制连线  因为纵向上面有个引入的layout 会占据50dp的高度
                if (!IS_ERASER) {
                    canvas.drawLine(startX, startY - 55, stopX, stopY - 55, paint);
                } else {
                    canvas.drawCircle(stopX, stopY - 55, 20, circle);
                }
                //更新起始点的位置
                startX = stopX;
                startY = stopY;
                //将图片展示到ImageView中
                showCanvas.setImageBitmap(bitmap);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dropCanvas:
                createNewCanvas();
                break;
            case R.id.eraser:
                IS_ERASER = !IS_ERASER;
                break;
            case R.id.saveCanvas:
                if (bitmap != null) {
                    saveBitmap();
                }
                finish();
                break;
            case R.id.backToEditActivity:
                if (bitmap != null) {
                    saveBitmap();
                }
                finish();
                break;
            case R.id.blue:
                initImage();
                blue.setImageResource(R.drawable.color_blue_click);
                paint.setColor(Color.rgb(0, 12, 255));
                break;
            case R.id.blue1:
                initImage();
                blue1.setImageResource(R.drawable.color_blue1_click);
                paint.setColor(Color.rgb(122, 92, 199));
                break;
            case R.id.red:
                initImage();
                red.setImageResource(R.drawable.color_red_click);
                paint.setColor(Color.rgb(254, 0, 0));
                break;
            case R.id.green:
                initImage();
                green.setImageResource(R.drawable.color_green_click);
                paint.setColor(Color.rgb(90, 188, 51));
                break;
            case R.id.grey:
                initImage();
                grey.setImageResource(R.drawable.color_grey_click);
                paint.setColor(Color.rgb(74, 29, 67));
                break;
            case R.id.orange:
                initImage();
                orange.setImageResource(R.drawable.color_orange_click);
                paint.setColor(Color.rgb(234, 205, 41));
                break;
            case R.id.orange1:
                initImage();
                orange1.setImageResource(R.drawable.color_orange1_click);
                paint.setColor(Color.rgb(232, 126, 40));
                break;
            case R.id.purple:
                initImage();
                purple.setImageResource(R.drawable.color_purple_click);
                paint.setColor(Color.rgb(212, 37, 202));
                break;
            case R.id.qing:
                initImage();
                qing.setImageResource(R.drawable.color_qing_click);
                paint.setColor(Color.rgb(36, 224, 213));
                break;
        }

    }

    private void initImage() {
        blue.setImageResource(R.drawable.color_blue);
        blue1.setImageResource(R.drawable.color_blue1);
        green.setImageResource(R.drawable.color_green);
        grey.setImageResource(R.drawable.color_grey);
        orange.setImageResource(R.drawable.color_orange);
        orange1.setImageResource(R.drawable.color_orange1);
        purple.setImageResource(R.drawable.color_purple);
        qing.setImageResource(R.drawable.color_qing);
        red.setImageResource(R.drawable.color_red);
    }

    private void createNewCanvas() {
        if (bitmap != null) {
            bitmap = Bitmap.createBitmap(showCanvas.getWidth(),
                    showCanvas.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            showCanvas.setImageBitmap(bitmap);
        }
    }

    private void saveBitmap() {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()
                    + "/" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            //设置回传数据 将文件的绝对路径传回去
            Intent i = new Intent();
            i.putExtra("canvas_path", file.getAbsolutePath());
            setResult(RESULT_OK, i);
            finish();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


}
