package com.program.mynotebook;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener, SwipeMenuListView.OnMenuItemClickListener {

    public final static int PASSWORD_ACTIVITY = 0;
    public final static int SETTING_ACTIVITY = 1;
    public final static int EDITNOTE_ACTIVITY = 2;
    private static final String TAG = "test";
    private static boolean IS_FIRST_CREATED = true;
    private static String DELETE_TABLE = "delete from " + ListData.TABLE_NAME_NOTES;

    private boolean IS_EDIT = false;
    private SwipeMenuCreator creator;

    //为list设置数据源
    private SwipeMenuListView itemList;
    private ListItemAdapter adapter;
    private ListData data;
    private SQLiteDatabase db;
    private Cursor c;
    private TextView noNote;

    //主界面顶部四个LinearLayout
    private LinearLayout ll_setting;
    private LinearLayout ll_name;
    private LinearLayout ll_editNote;
    private LinearLayout ll_cancelNote;
    private TextView edit_note_tv;

    //主界面底部四个LinearLayout
    private LinearLayout ll_attachment;
    private LinearLayout ll_createNewNote;
    private LinearLayout ll_deleteSelectedNote;
    private LinearLayout ll_deleteAllNote;
    private LinearLayout ll_noteNumber;
    private TextView note_number;

    //几个点击变换的ImageView
    private ImageView setting_iv;


    //如果在onResume中 设置pwd之后会导致返回主activity需要输入密码
    @Override
    protected void onStart() {
        super.onStart();
        myActivityManager.activityList.add(MainActivity.this);
        if (IS_FIRST_CREATED) {
            String password = "";
            SharedPreferences sp = getSharedPreferences(Password.PASSWORD, MODE_PRIVATE);
            password = sp.getString(Password.PASSWORD, null);
            //这里不能够用startActivity去启动 必须要用StartActivityForResult去启动
            if (TextUtils.isEmpty(password)) {
                IS_FIRST_CREATED = false;
            } else {
                Intent i = new Intent(this, Password.class);
                startActivityForResult(i, PASSWORD_ACTIVITY);
            }
        }
    }

    //用来判断返回的是哪一个activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PASSWORD_ACTIVITY:
                if (data != null) {
                    IS_FIRST_CREATED = data.getBooleanExtra("password", false);
                } else {
                    IS_FIRST_CREATED = false;
                    Log.i(TAG, "onActivityResult: ");
                    System.exit(0);
                    //finish();
                }
                break;
            case SETTING_ACTIVITY:
                setting_iv.setImageResource(R.drawable.setting_32);
                break;
            //是编辑界面返回 刷新list
            case EDITNOTE_ACTIVITY:
                initAdapter();
                break;
        }

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        //初始化View 并为其设置点击事件监听器
        initView();
        //初始化Adapter
        initAdapter();

    }

    private void queryDB() {
        data = new ListData(this);
        db = data.getReadableDatabase();
        c = db.query(ListData.TABLE_NAME_NOTES, null, null, null, null, null, null);
    }

    private void initAdapter() {
        queryDB();
        //为SwipeListView设置左滑菜单
        initMenuCreator();
        //设置菜单创建器
        itemList.setMenuCreator(creator);
        //设置项目点击事件
        itemList.setOnMenuItemClickListener(this);
        //左滑动
        itemList.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        //如果数据库中有数据
        if (c.getCount() >= 1) {
            noNote.setVisibility(View.GONE);
            itemList.setVisibility(View.VISIBLE);
            if (IS_EDIT) {
                adapter = new ListItemAdapter(this, c, true);
            } else {
                adapter = new ListItemAdapter(this, c, false);
            }
            note_number.setText("有" + c.getCount() + "篇记事本");
            itemList.setAdapter(adapter);
            edit_note_tv.setTextColor(Color.rgb(255, 153, 0));
            ll_editNote.setClickable(true);

        } else {
            noNote.setVisibility(View.VISIBLE);
            itemList.setVisibility(View.GONE);
            noNote.setText("备忘录为空");
            note_number.setText("无备忘录");
            edit_note_tv.setTextColor(Color.rgb(220, 220, 220));
            ll_editNote.setClickable(false);
        }

        itemList.setOnItemClickListener(new SwipeMenuListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //获取Cursor对象
                //将Cursor移动到对应的位置
                queryDB();
                c.moveToPosition(position);
                Intent i = new Intent(MainActivity.this, EditNoteActivity.class);
                i.putExtra("ownerID", c.getInt(c.getColumnIndex(ListData.TABLE_COLUMN_NAME_ID)));
                i.putExtra("content", c.getString(c.getColumnIndex(ListData.TABLE_COLUMN_NAME_CONTENT)));
                if (!TextUtils.equals(c.getString(c.getColumnIndex(ListData.TABLE_COLUMN_NAME_MEDIA_PATH)), "")) {
                    i.putExtra("media_path", c.getString(c.getColumnIndex(ListData.TABLE_COLUMN_NAME_MEDIA_PATH)));
                }
                //Log.i(TAG, "media_path: " + c.getString(c.getColumnIndex(ListData.TABLE_COLUMN_NAME_MEDIA_PATH)));
                startActivityForResult(i, EDITNOTE_ACTIVITY);
                //将内容放入Extra 传递到另一个Intent
                c.close();
            }
        });
    }

    private void initView() {
        itemList = (SwipeMenuListView) findViewById(R.id.content_listView);
        noNote = (TextView) findViewById(R.id.no_notes);
        ll_setting = (LinearLayout) findViewById(R.id.setting);
        ll_name = (LinearLayout) findViewById(R.id.note_name);
        ll_editNote = (LinearLayout) findViewById(R.id.note_edit);
        ll_cancelNote = (LinearLayout) findViewById(R.id.note_cancel);
        ll_attachment = (LinearLayout) findViewById(R.id.attachment_note);
        ll_noteNumber = (LinearLayout) findViewById(R.id.ll_noteNumber);

        note_number = (TextView) findViewById(R.id.note_number_tv);
        edit_note_tv = (TextView) findViewById(R.id.editnotetv);

        ll_createNewNote = (LinearLayout) findViewById(R.id.create_new_note);

        ll_deleteSelectedNote = (LinearLayout) findViewById(R.id.delete_select_note);
        ll_deleteAllNote = (LinearLayout) findViewById(R.id.deleteAllNote);

        setting_iv = (ImageView) findViewById(R.id.setting_iv);

        //设置点击监听事件
        initListener();
    }

    private void initListener() {
        ll_attachment.setOnClickListener(this);
        ll_setting.setOnClickListener(this);
        ll_editNote.setOnClickListener(this);
        ll_cancelNote.setOnClickListener(this);
        ll_createNewNote.setOnClickListener(this);
        ll_deleteAllNote.setOnClickListener(this);
        ll_deleteSelectedNote.setOnClickListener(this);
        ll_name.setOnClickListener(this);
    }

    private void initMenuCreator() {
        creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                //新增一个删除的按钮
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                //deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xc9, 0xC9, 0xCE)));
                deleteItem.setWidth(56);
                deleteItem.setTitleColor(Color.WHITE);
                deleteItem.setIcon(R.drawable.delete_72);
                menu.addMenuItem(deleteItem);
            }
        };
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            //点击了设置按钮 跳转到密码设置界面
            case R.id.setting:
                setting_iv.setImageResource(R.drawable.setting_click);
                i = new Intent(this, Setting.class);
                startActivity(i);
                break;
            //点击删除按钮 setting attachment noteNumber createNewNote 按钮消失 delete deleteSelected出现
            case R.id.note_edit:
                ll_attachment.setVisibility(View.GONE);
                ll_noteNumber.setVisibility(View.GONE);
                ll_createNewNote.setVisibility(View.GONE);
                ll_deleteSelectedNote.setVisibility(View.VISIBLE);
                ll_deleteAllNote.setVisibility(View.VISIBLE);
                ll_editNote.setVisibility(View.GONE);
                ll_cancelNote.setVisibility(View.VISIBLE);
                ll_setting.setClickable(false);
                IS_EDIT = true;
                initAdapter();
                itemList.setSwipeDirection(0);  //如果设置为0  就是不能滑动
                break;
            //点击取消按钮
            case R.id.note_cancel:
                IS_EDIT = false;
                ll_attachment.setVisibility(View.VISIBLE);
                ll_noteNumber.setVisibility(View.VISIBLE);
                ll_createNewNote.setVisibility(View.VISIBLE);
                ll_deleteSelectedNote.setVisibility(View.GONE);
                ll_deleteAllNote.setVisibility(View.GONE);
                ll_editNote.setVisibility(View.VISIBLE);
                ll_cancelNote.setVisibility(View.GONE);
                ll_setting.setClickable(true);
                initAdapter();
                break;
            //点击新建按钮
            case R.id.create_new_note:
                Intent intent = new Intent(this, EditNoteActivity.class);
                startActivityForResult(intent, EDITNOTE_ACTIVITY);
                break;
            //点击全部删除按钮
            case R.id.deleteAllNote:
                db.execSQL(DELETE_TABLE);
                initAdapter();
                break;
            //点击删除选中的item
            case R.id.delete_select_note:
                List<Integer> item = adapter.getSelectedList();
                //对List进行倒序排列
                Collections.sort(item, Collections.<Integer>reverseOrder());
                for (int ids : item) {
                    queryDB();
                    c.moveToPosition(ids);
                    String whereClause = ListData.TABLE_COLUMN_NAME_ID + "=?";
                    String[] whereArgs = {String.valueOf(c.getInt(c.getColumnIndex(ListData.TABLE_COLUMN_NAME_ID)))};
                    db.delete(ListData.TABLE_NAME_NOTES, whereClause, whereArgs);
                }
                initAdapter();
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
        switch (index) {
            //只设置了一个 就是删除按钮
            case 0:
                Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                queryDB();
                c.moveToPosition(position);
                String whereClause = ListData.TABLE_COLUMN_NAME_ID + "=?";
                String[] whereArgs = {String.valueOf(c.getInt(c.getColumnIndex(ListData.TABLE_COLUMN_NAME_ID)))};
                db.delete(ListData.TABLE_NAME_NOTES, whereClause, whereArgs);
                initAdapter();
                break;
        }

        // false : close the menu; true : not close the menu
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        c.close();
    }
}
