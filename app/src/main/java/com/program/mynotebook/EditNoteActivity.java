package com.program.mynotebook;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2017/06/02
 */

public class EditNoteActivity extends Activity implements View.OnClickListener {

    public final static int CAMERA = 0;
    public final static int CANVAS = 1;
    public final static int CAMERA_CROP = 1;
    private static final String TAG = "test";
    //保存图片path路径
    private static String[] img_paths = new String[]{};
    private boolean IS_SET_CLOCK = false;

    private String time = null;
    private long[] dateTime = {0, 0, 0};
    private Calendar setAlarmDate = Calendar.getInstance();


    private ImageView editNotice;
    private int ownerID = 10000;
    private ArrayAdapter<String> adapter = null;
    //拍照保存的path 会在截图的时候被删掉
    private File img_path;
    //截图后保存的path
    private File path;

    private EditText editText;
    private ImageSpan[] is;
    private Spanned s;

    //底部四个LinearLayout 即四个功能 暂时没用 但是有单击事件
    private LinearLayout ll_delete;
    private LinearLayout ll_photo;
    private LinearLayout ll_paint;
    private LinearLayout ll_newNote;

    private ListData data;
    private SQLiteDatabase db;
    private Cursor c;

    //因为有两个启动方式都会启动这个activity
    //一个是点击事件（查看） 一个是新建
    //所以我们在这里要去获得 是否有值传递过来

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_notesedit_layout);
        initView();

        //判断传递过来的数据
        //是否有Media 如果没有Media 就直接显示 如果有 就解析
        if (getIntent().getExtras() != null) {
            ownerID = getIntent().getExtras().getInt("ownerID");
            if (!TextUtils.equals(getIntent().getExtras().getString("media_path"), null)) {
                initEditTextView();
            } else {
                String text = getIntent().getExtras().getString("content");
                editText.setText(text);
                editText.setSelection(text != null ? text.length() : 0);
            }
            editText.setSelection(editText.getEditableText().toString().length());
        }
    }

    private boolean isHavePic(Editable et) {
        //有几张图片 is的length就是几
        s = et;
        // 如果没有is 那么is的length为0
        is = s.getSpans(0, s.length(), ImageSpan.class);
        return is.length != 0;
    }

    //存在图片 去解析 并显示
    private void initEditTextView() {
        String mediaPath = getIntent().getExtras().getString("media_path");
        String content = getIntent().getExtras().getString("content");
        Log.i(TAG, content);
        String[] contents = content != null ? content.split("\\+") : new String[0];
        //字符串最后也有一个加号，所以下面循环时 length要-1
        String[] mediaPaths = mediaPath != null ? mediaPath.split("\\+") : new String[0];
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

    private void initView() {
        editText = (EditText) findViewById(R.id.editText);
        ll_delete = (LinearLayout) findViewById(R.id.delete_note);
        editNotice = (ImageView) findViewById(R.id.edit_notice); //设置闹钟
        findViewById(R.id.edit_notice).setOnClickListener(this);
        //为四个LinearLayout设置按键监听
        findViewById(R.id.delete_note).setOnClickListener(this);
        findViewById(R.id.camera_note).setOnClickListener(this);
        findViewById(R.id.canvas_note).setOnClickListener(this);
        findViewById(R.id.new_note).setOnClickListener(this);
        findViewById(R.id.backToEditActivity).setOnClickListener(this);
        findViewById(R.id.backToEditActivity).setOnClickListener(this);
    }

    private void queryDB() {
        data = new ListData(this);
        db = data.getReadableDatabase();
        c = db.query(ListData.TABLE_NAME_NOTES, null, null, null, null, null, null);
    }

    //我们在保存数据的时候，只需要将EditText里面的内容保存到我们指定的path当中就好了
    //我们会将第一句话当做title来保存 最多截取10个字符 每张图片的长度都是33
    //在存储ID时，如果ID已存在，那么则是保存ID
    private void saveData() {
        Editable et = editText.getEditableText();
        Editable ct = editText.getEditableText(); //将内容分段相连保存为内容
        Editable set = editText.getEditableText();
        editText.setText(set);
        editText.setSelection(set.toString().length()); //让editActivity在退出的时候不会出现editText文字变化的小bug
        String content = editText.getText().toString();
        String text;
        String title;
        String mediaPath = "";
        boolean isHavePicture = false;
        if (isHavePic(et)) {
            for (int i = is.length - 1; i >= 0; i--) {
                int Start = s.getSpanStart(is[i]);
                int End = s.getSpanEnd(is[i]);
                et.delete(Start, End);
                ct.replace(Start, Start + 1, "+"); //将图片替换成+
                //存储的时候加一个"+" 解析的时候用split就可以
                mediaPath += content.substring(Start, End) + "+";
            }
            isHavePicture = true;
            //删除所有照片后只剩下文字 用来剪切设置title
            content = et.toString();
            //这是我们需要保存的为本内容
            text = ct.toString();
            if (content.length() > 10) {
                title = content.replace("+", "").substring(0, 8);
            } else {
                title = content.replace("+", "");
            }
        } else {
            if (content.length() > 10) {
                title = content.substring(0, 8);
            } else {
                title = content;
            }
            text = ct.toString();
        }
        queryDB();
        ContentValues cv_note = new ContentValues();
        cv_note.put(ListData.TABLE_COLUMN_NAME_CONTENT, text);
        cv_note.put(ListData.TABLE_COLUMN_NAME_TIME, getTime());
        cv_note.put(ListData.TABLE_COLUMN_NAME_TITLE, title);
        if (isHavePicture) {
            cv_note.put(ListData.TABLE_COLUMN_NAME_MEDIA_PATH, mediaPath);
        } else {
            cv_note.put(ListData.TABLE_COLUMN_NAME_MEDIA_PATH, "");
        }
        if (IS_SET_CLOCK) {
            String setAlarm = "true";
            cv_note.put(ListData.TABLE_COLUMN_NAME_RING, setAlarm);
        }
        if (ownerID == 10000) {
            db.insert(ListData.TABLE_NAME_NOTES, null, cv_note);
        } else {
            String whereClause = ListData.TABLE_COLUMN_NAME_ID + "=?";
            String[] whereArgs = {String.valueOf(ownerID)};
            db.update(ListData.TABLE_NAME_NOTES, cv_note, whereClause, whereArgs);
        }
    }

    //返回一个时间类型的字符串 表示当前时间
    //格式 2017/01/02 21:23

    private String getTime() {
        String time;
//        Calendar c = Calendar.getInstance();
//        time = String.valueOf(c.get(Calendar.YEAR)) + "/" + c.get(Calendar.MONTH)
//                + "/" + c.get(Calendar.DAY_OF_MONTH)
//                + " " + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        time = format.format(date);

        return time;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //删除按钮
            case R.id.delete_note:
                //如果这是一个新建的页面 清空界面并退出
                if (ownerID == 10000) {
                    editText.setText("");
                    finish();
                } else {
                    //如果这是一个修改的页面 从数据库中删除对应的界面即可
                    queryDB();
                    String whereClause = ListData.TABLE_COLUMN_NAME_ID + "=?";
                    String[] whereArgs = {String.valueOf(ownerID)};
                    db.delete(ListData.TABLE_NAME_NOTES, whereClause, whereArgs);
                    finish();
                }
                break;
            //拍照按钮
            case R.id.camera_note:
                int currentapiVersion = Build.VERSION.SDK_INT;
                Intent image = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                img_path = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/" + System.currentTimeMillis() + ".jpg");
                //兼容android 7.0 打开Camera
                if (currentapiVersion < 24) {
                    image.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(img_path));
                } else {
                    ContentValues cv = new ContentValues(1);
                    cv.put(MediaStore.Images.Media.DATA, img_path.getAbsolutePath());
                    image.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(img_path));
                }
                startActivityForResult(image, CAMERA);
                break;
            //画板按钮
            case R.id.canvas_note:
                Intent canvasDraw = new Intent(EditNoteActivity.this, CanvasDrawActivity.class);
                startActivityForResult(canvasDraw, CANVAS);
                break;
            //新建按钮
            case R.id.new_note:
                editText.setText("");
                break;
            //返回按钮 这里我们也可以调用保存
            case R.id.backToEditActivity:
                if (!TextUtils.equals(editText.getText().toString().trim(), "")) {
                    saveData();
                }
                finish();
                break;
            //闹钟设置按钮
            case R.id.edit_notice:
                addAlarmClock();
                break;
        }
    }

    private void addAlarmClock() {
        final AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        final Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(EditNoteActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                //将秒数和毫秒数 清零
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                int id;
                //获取当前时间 currenTime
                Calendar currenTime = Calendar.getInstance();
                //如果我们设置的时间比当前的时间大，那么说明设置的是今天的时间
                //如果比当前时间小，那么就是明天的闹钟
                //getTimeInMillis()返回的是当前的时间的一个毫秒数 与1970.01.01 之间想差的毫秒数
                if (c.getTimeInMillis() <= currenTime.getTimeInMillis()) {
                    c.setTimeInMillis(+24 * 60 * 60 * 1000);
                }
                //如果是更新界面，那么就有id
                if (ownerID == 10000) {
                    id = 1;
                } else {
                    id = ownerID;
                }
                Intent receive = new Intent(getApplicationContext(), AlarmReceiver.class);
                //将id传递过去 便于获取数据
                receive.putExtra("id", id);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                        c.getTimeInMillis(),
                        5 * 60 * 1000,
                        PendingIntent.getBroadcast(getApplicationContext(), id, receive, 0));
                IS_SET_CLOCK = true;
            }
            //千万不要忘记.show()方法
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        //true 代表用24小时显示时间
    }

    //重新onKeyDown事件 当点击系统返回按键时，也将页面保存
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!TextUtils.equals(editText.getText().toString().trim(), "")) {
                saveData();
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //拍照Activity界面
            case CAMERA:
                CropImage.activity(Uri.fromFile(img_path)).start(this);
                break;
            //截图Activity返回
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    //resultUri即是返回的图片Uri对象 保存在data目录下的cache中
                    //我们需要先将这个Uri转换成Bitmap保存在我们制定的文件
                    //再展示到屏幕中 我们保存到数据库中的实际是图片的地址
                    //将图片 放入editText
                    ContentResolver resolver = getContentResolver();
                    try {
                        //uri  to  bitmap
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, resultUri);
                        //存储bitmap
                        saveBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //显示Bitmap到EditText中

                    insertBitmapIntoEditText(getBitmap(path.getAbsolutePath()), path.getAbsolutePath());

                }
                break;
            //绘画界面
            case CANVAS:
                if (resultCode == RESULT_OK) {
                    String bitmapPath = data.getExtras().getString("canvas_path");
                    insertBitmapIntoEditText(getBitmap(bitmapPath), bitmapPath);
                }
                break;


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveBitmap(Bitmap bitmap) {
        path = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/" + System.currentTimeMillis() + ".jpg");
        //我们放图片的路径是img_path
        if (path.exists()) {
            path.delete();
        }
        try {
            FileOutputStream fOut = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Bitmap getBitmap(String bitmap_path) {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;    //屏幕宽度（像素）
        //在这里还要处理一下 如果Bitmap过大通常会导致OOM
        Bitmap bitmap = BitmapFactory.decodeFile(bitmap_path);
        int bWidth = bitmap.getWidth();
        int bHeight = bitmap.getHeight();
        if ((bWidth / width) >= 0.5) {
            float scale = 0.4f;
            Matrix mx = new Matrix();
            mx.postScale(scale, scale);
            return Bitmap.createBitmap(bitmap, 0, 0, bWidth, bHeight, mx, true);
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


}
