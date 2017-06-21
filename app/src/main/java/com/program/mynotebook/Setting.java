package com.program.mynotebook;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/06/02.
 * 首先去判断getSharedPreference是否密码已存在
 * 根据是否有密码进入不同的界面
 */

public class Setting extends Activity implements View.OnClickListener {
    private String TAG = "TAG";

    private TextView textView;
    private TextView error;
    private Button confirm;
    private EditText pwd;
    private EditText pwd_again;

    //判断点击button是否响应的按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.setting_activity);
        initView();
    }

    private void initView() {
        textView = (TextView) findViewById(R.id.setting_tv);
        confirm = (Button) findViewById(R.id.setting_confirm);
        pwd = (EditText) findViewById(R.id.setting_et1);
        pwd_again = (EditText) findViewById(R.id.setting_et2);
        error = (TextView) findViewById(R.id.setting_error);
        //先将确认按钮设置为不可点击
        confirm.setEnabled(false);

        //设置按键监听事件
        initListener();

        SharedPreferences sp = getSharedPreferences(Password.PASSWORD, MODE_PRIVATE);
        String pwd = sp.getString(Password.PASSWORD, null);
        //如果密码已存在
        if (TextUtils.isEmpty(pwd)) {
            textView.setText("设置密码");
        }

    }

    private void initListener() {
        confirm.setOnClickListener(this);
        findViewById(R.id.backToEditActivity).setOnClickListener(this);

        //设置EditText的输入事件
        //为 输入密码的第一个框设置监听事件
        //密码只能为数字

        //为第二个密码输入框设置监听事件
        //判断是否与第一个框所输入的密码一致
        pwd_again.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 4 && pwd.getText().toString().length() == 4) {
                    if (pwd_again.getText().toString().equals(pwd.getText().toString())) {
                        error.setText("");
                        confirm.setEnabled(true);
                    } else {
                        confirm.setEnabled(false);
                        error.setText("两次输入的密码不相同");
                    }
                } else {
                    confirm.setEnabled(false);
                    error.setText("密码长度必须为4个单位");
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //当点击确定按钮时
            case R.id.setting_confirm:
                Log.i(TAG, "onClick: ");
                savePassword();
                Toast.makeText(this, "密码修改成功", Toast.LENGTH_LONG).show();
                finish();
                break;
            //当点击返回按钮时
            case R.id.backToEditActivity:
                finish();
                break;
        }
    }

    private void savePassword() {
        String password = pwd_again.getText().toString();

        SharedPreferences sp = getSharedPreferences(Password.PASSWORD, MODE_PRIVATE);
        String isExist = sp.getString(Password.PASSWORD, null);
        SharedPreferences.Editor editor = sp.edit();
        if (TextUtils.isEmpty(isExist)) {
            editor.putString(Password.PASSWORD, password);
            editor.apply();
            editor.commit();
        } else {
            editor.remove(Password.PASSWORD);
            editor.putString(Password.PASSWORD, password);
            editor.apply();
            editor.commit();
        }
    }
}
