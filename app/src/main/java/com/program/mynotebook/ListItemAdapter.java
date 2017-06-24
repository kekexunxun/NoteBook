package com.program.mynotebook;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/06/02.
 */

public class ListItemAdapter extends BaseAdapter {

    private List<Integer> selectedList = new ArrayList<>();
    private boolean IS_CHECKBOX_SHOW = false;
    private Context context;
    private Cursor cursor;

    public ListItemAdapter(Context context, Cursor cursor, boolean show) {
        this.context = context;
        this.cursor = cursor;
        this.IS_CHECKBOX_SHOW = show;
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        return cursor.getPosition();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        View view;
        if (convertView == null) {
            //获取list_item布局文件
            view = LayoutInflater.from(context).inflate(R.layout.notes_list_cell, null);
            //创建一个新的viewHolder
            viewHolder = new ViewHolder();
            //获取控件对象
            viewHolder.content = (TextView) view.findViewById(R.id.list_item_content);
            viewHolder.title = (TextView) view.findViewById(R.id.list_item_title);
            viewHolder.extra_content = (TextView) view.findViewById(R.id.list_item_content_extra);
            viewHolder.checkBox = (CheckBox) view.findViewById(R.id.list_item_checkbox);
            viewHolder.thumbnail = (ImageView) view.findViewById(R.id.list_item_image);
            viewHolder.clock = (ImageView) view.findViewById(R.id.list_item_clock);
            viewHolder.ll_checkbox = (LinearLayout) view.findViewById(R.id.ll_checkbox);
            viewHolder.ll_clock = (LinearLayout) view.findViewById(R.id.ll_clock);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        //将cursor移动到制定位置
        cursor.moveToPosition(position);
        //获取我们content 对应的内容
        String content = cursor.getString(cursor.getColumnIndex(ListData.TABLE_COLUMN_NAME_CONTENT));
        content = content.replace("+", "");   //去掉+号
        if (content.length() <= 8) {
            viewHolder.extra_content.setText("无附加文本");
        } else {
            if (content.length() > 30) {
                viewHolder.extra_content.setText(content.substring(8, 25));
            } else {
                viewHolder.extra_content.setText(content.substring(8, content.length()));
            }
        }
        String mediaPath = cursor.getString(cursor.getColumnIndex(ListData.TABLE_COLUMN_NAME_MEDIA_PATH));
        //如果有图片的话
        if (mediaPath != null) {
            String[] mediaPaths = mediaPath.split("\\+");
            Bitmap thumbnail = getImageThumbnail(mediaPaths[0], 100, 100);
            viewHolder.thumbnail.setImageBitmap(thumbnail);
        }
        //如果有设置闹钟的话 显示闹钟图片
        String ring = cursor.getString(cursor.getColumnIndex(ListData.TABLE_COLUMN_NAME_RING));
        if (ring != null) {
            viewHolder.ll_checkbox.setVisibility(View.GONE);
            viewHolder.clock.setVisibility(View.VISIBLE);
            if (ring.equals("false")) {
                viewHolder.clock.setImageResource(R.drawable.ring);
            }
        }

        //获取对应数据
        viewHolder.title.setText(cursor.getString(cursor.getColumnIndex(ListData.TABLE_COLUMN_NAME_TITLE)));
        viewHolder.content.setText(cursor.getString(cursor.getColumnIndex(ListData.TABLE_COLUMN_NAME_TIME)));
        if (IS_CHECKBOX_SHOW) {
            viewHolder.ll_clock.setVisibility(View.GONE);
            viewHolder.ll_checkbox.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(context, String.valueOf(position), Toast.LENGTH_SHORT).show();
                    if (viewHolder.checkBox.isChecked()) {
                        selectedList.add(position);
                    } else {
                        selectedList.remove(position);
                    }
                }
            });
        }

        return view;
    }

    //如果对应的Item有图片 则获取图片缩略图
    private Bitmap getImageThumbnail(String path, int width, int height) {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        bitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        int beWidth = options.outWidth / width;
        int beHeight = options.outHeight / height;
        int be = 1;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        bitmap = BitmapFactory.decodeFile(path, options);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    public List<Integer> getSelectedList() {
        return selectedList;
    }

    private class ViewHolder {
        TextView content;
        TextView title;
        TextView extra_content;
        CheckBox checkBox;
        ImageView thumbnail;
        ImageView clock;
        LinearLayout ll_checkbox;
        LinearLayout ll_clock;
    }
}
