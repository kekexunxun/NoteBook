package com.program.mynotebook;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


/**
 * Created by Administrator on 2017/06/23.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "fuck";

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getExtras().getInt("id");
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //根据请求码来移除闹钟 通过给定的RequestCode取消闹钟
        am.cancel(PendingIntent.getBroadcast(context, id, new Intent(context, AlarmReceiver.class), 0));

        //更新数据库操作
        ListData data = new ListData(context);
        SQLiteDatabase db = data.getReadableDatabase();
        Cursor cursor = db.query(ListData.TABLE_NAME_NOTES, null, null, null, null, null, null);
        ContentValues cv = new ContentValues();
        cv.put(ListData.TABLE_COLUMN_NAME_RING, "false");
        db.update(ListData.TABLE_NAME_NOTES, cv, ListData.TABLE_COLUMN_NAME_ID + "=?", new String[]{String.valueOf(id)});
        cursor.close();

        //创建一个notification，当用户打开notification的时候可以看到指定的内容
        //获取要传递的内容
        Cursor c = db.query(ListData.TABLE_NAME_NOTES, new String[]{ListData.TABLE_COLUMN_NAME_ID,
                ListData.TABLE_COLUMN_NAME_CONTENT,
                ListData.TABLE_COLUMN_NAME_MEDIA_PATH,
                ListData.TABLE_COLUMN_NAME_RING},
                ListData.TABLE_COLUMN_NAME_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        c.moveToFirst();
        Intent i = new Intent(context, AlarmShowActivity.class);
        i.putExtra("content", c.getString(c.getColumnIndex(ListData.TABLE_COLUMN_NAME_CONTENT)));
        i.putExtra("media_path", c.getString(c.getColumnIndex(ListData.TABLE_COLUMN_NAME_MEDIA_PATH)));
        c.close();
        //设置notification
        PendingIntent pd = PendingIntent.getActivity(context, id, i, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ring)
                .setTicker("闹钟响啦")
                .setContentTitle("Title")
                .setContentText("请查看")
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pd).getNotification();
        //n.flags |= Notification.FLAG_AUTO_CANCEL;
        nm.notify(id, n);
    }
}
