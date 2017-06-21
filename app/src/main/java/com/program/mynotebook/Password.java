package com.program.mynotebook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/06/07.
 * 用户在进入程序时，如果检测到有密码，那么就先进入这个界面 否则提示密码输入错误
 * 为了避免暴力破解密码，当输入密码错误次数超过3次 那么就设置一个EditText 30s之后再输入
 */


public class Password extends Activity implements View.OnClickListener {

    public final static String PASSWORD = "password";
    private static final String TAG = "TAG";
    //代表每次插入COUNT的位置
    private int POSITION = 0;

    private int[] COUNT = {1, 2, 3, 4};
    private String password = "";
    private Vibrator vibrator;
    //四个小圆点  显示已经输入了多少个密码
    private List<ImageView> list = new ArrayList<ImageView>();
    private ImageView pwd_1;
    private ImageView pwd_2;
    private ImageView pwd_3;
    private ImageView pwd_4;

    //当应用程序创建时 读取SharedPreference
    @Override
    protected void onStart() {
        super.onStart();
        myActivityManager.activityList.add(Password.this);
        SharedPreferences sp = getSharedPreferences(PASSWORD, MODE_PRIVATE);
        password = sp.getString(PASSWORD, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Black_NoTitleBar);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_password);
        //为imageView设置点击监听器
        initListener();
    }

    private void initListener() {
        pwd_1 = (ImageView) findViewById(R.id.code_1);
        pwd_2 = (ImageView) findViewById(R.id.code_2);
        pwd_3 = (ImageView) findViewById(R.id.code_3);
        pwd_4 = (ImageView) findViewById(R.id.code_4);
        list.add(pwd_1);
        list.add(pwd_2);
        list.add(pwd_3);
        list.add(pwd_4);

        findViewById(R.id.number_0).setOnClickListener(this);
        findViewById(R.id.number_1).setOnClickListener(this);
        findViewById(R.id.number_2).setOnClickListener(this);
        findViewById(R.id.number_3).setOnClickListener(this);
        findViewById(R.id.number_4).setOnClickListener(this);
        findViewById(R.id.number_5).setOnClickListener(this);
        findViewById(R.id.number_6).setOnClickListener(this);
        findViewById(R.id.number_7).setOnClickListener(this);
        findViewById(R.id.number_8).setOnClickListener(this);
        findViewById(R.id.number_9).setOnClickListener(this);
        findViewById(R.id.pwd_delete).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.number_0:
                isPwdCorrect(POSITION, 0);
                break;
            case R.id.number_1:
                isPwdCorrect(POSITION, 1);
                break;
            case R.id.number_2:
                isPwdCorrect(POSITION, 2);
                break;
            case R.id.number_3:
                isPwdCorrect(POSITION, 3);
                break;
            case R.id.number_4:
                isPwdCorrect(POSITION, 4);
                break;
            case R.id.number_5:
                isPwdCorrect(POSITION, 5);
                break;
            case R.id.number_6:
                isPwdCorrect(POSITION, 6);
                break;
            case R.id.number_7:
                isPwdCorrect(POSITION, 7);
                break;
            case R.id.number_8:
                isPwdCorrect(POSITION, 8);
                break;
            case R.id.number_9:
                isPwdCorrect(POSITION, 9);
                break;
            case R.id.pwd_delete:
                deletePwd(POSITION);
                break;
        }
    }

    //点击删除时，只有当有密码的时候才可以点击
    private void deletePwd(int position) {
        if (position != 0) {
            COUNT[position - 1] = 0;
            POSITION--;
            list.get(POSITION).setImageResource(R.drawable.circle_outline_20);
        }
    }

    //当密码输入失败是 调用系统震动功能
    private void startVibrate() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] pattern = {100, 400, 100, 400}; // 停止 开启 停止 开启
        vibrator.vibrate(pattern, -1);  //index = -1 表示只重复一次
        pwd_1.setImageResource(R.drawable.circle_outline_20);
        pwd_2.setImageResource(R.drawable.circle_outline_20);
        pwd_3.setImageResource(R.drawable.circle_outline_20);
        pwd_4.setImageResource(R.drawable.circle_outline_20);
        POSITION = 0;
    }

    private void pwdCorrect() {
        if (vibrator != null) {
            vibrator.cancel();
        }
        Intent i = new Intent();
        i.putExtra("password", false);
        setResult(MainActivity.PASSWORD_ACTIVITY, i);
        //startActivity(i);
        finish();
    }

    private void isPwdCorrect(int position, int number) {
        COUNT[position] = number;
        list.get(POSITION).setImageResource(R.drawable.circle_filled_20);
        if (position == 3) {
            String s = "";
            s = String.valueOf(COUNT[0]) + COUNT[1] + COUNT[2] + COUNT[3];
            if (TextUtils.equals(s, password)) {
                pwdCorrect();
            } else {
                startVibrate();
            }
        } else {
            POSITION++;
        }
    }

    //重新onKeyDown事件 当点击系统返回按键时，也将页面保存
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("exit")
                    .setMessage("是否退出程序")
                    .setNegativeButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //直接让程序退出
                            myActivityManager.exit();
                        }
                    })
                    .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create();
            dialog.show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
