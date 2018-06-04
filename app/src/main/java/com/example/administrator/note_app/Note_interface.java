package com.example.administrator.note_app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Note_interface extends AppCompatActivity implements  listview_adapter.Callback {
    protected ListView listView;
    protected EditText find_info;
    protected Button add_note;
    protected ImageView imageView_update;
    protected TextView txt_find, btn_back;
    protected Drawable drawable,drawable1;
    protected Handler handler;
    private noteDB noteDB;
    private SQLiteDatabase db;
    private Bundle bundle;
    public static final String NOTE_ID = "id";
    public static final String NOTE_TYPE = "TYPE";
    public static final String NOTE_TIME = "time";
    public static final String NOTE_CONTENT = "content";
    public static final String NOTE_TABLE = "note";
    public static final String NOTE_TITLE = "title";
    protected List<unit_item> list = new ArrayList<>();
    protected listview_adapter adapter;
    protected List<unit_item> lists;
    protected List<unit_item> list_accept = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_interface);
        inview();
        setListern();

    }

    private void inview() {
        View view = LayoutInflater.from(this).inflate(R.layout.note_interface_unit, null);
        imageView_update = view.findViewById(R.id.image_update);
        txt_find = findViewById(R.id.text_find);
        find_info = findViewById(R.id.look_up);
        add_note = findViewById(R.id.btn_addnote);
        listView = findViewById(R.id.listview);
        drawable = getResources().getDrawable(R.drawable.search);
        drawable.setBounds(5, 0, 70, 60);
        find_info.setCompoundDrawables(drawable, null, null, null);
        drawable1=getResources().getDrawable(R.drawable.jiahao);
        drawable1.setBounds(170,0,80,80);
        add_note.setCompoundDrawables(drawable1,null,null,null);
        handler=new Handler();
        lists = new ArrayList<>();
        noteDB = new noteDB(Note_interface.this);
        db = noteDB.getReadableDatabase();
        db.execSQL("delete from note");
        adapter = new listview_adapter(list, Note_interface.this, R.layout.note_interface_unit, this);
        setdata();


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == 2) {
            find_info.setText("");
            setdata();

        }
    }

    private void setdata() {
        updateinterface();
        list = getdata();
        adapter = new listview_adapter(list, Note_interface.this, R.layout.note_interface_unit, this);
        listView.setAdapter(adapter);
    }

    private void updateinterface() {
        list.removeAll(list);
        adapter.notifyDataSetChanged();
    }

    /*
    *  从数据库中获取数据，存储在集合中
    * */
    private List<unit_item> getdata() {
        Cursor cursor = db.query(NOTE_TABLE, null, "content!=\"\" or title!=\"\"", null, null, null, null);
        while (cursor.moveToNext())

        {
            String id = cursor.getString(cursor.getColumnIndex("id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            list_accept.add(new unit_item(id, title, R.drawable.note_update, R.drawable.opened7, time, content));


        }
        cursor.close();

        return list_accept;

    }


    private void setListern() {
        txt_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                find();
            }
        });


        add_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Note_interface.this, Note_content.class);
                bundle = new Bundle();
                bundle.putString(NOTE_TYPE, "0");
                bundle.putString(NOTE_CONTENT, "");
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);

            }
        });
        find_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                find_info.setCursorVisible(true);
                find_info.requestFocus();
                find_info.setSelection(find_info.getText().toString().length());


            }
        });

    }



    /*
    *  内部控件执行的内容在重写的click方法里
    * */
    @Override
    public void click_update(View view) {
        int position = (int) view.getTag();
        unit_item item = list.get(position);
        String id = item.getId();
        String content = item.getContent();
        String title = item.getTitle();
        String time = item.getTime();
        Log.e("数据", content + " " + title + "" + id);
        Intent intent = new Intent(Note_interface.this, Note_content.class);
        bundle.putString(NOTE_ID, id);
        bundle.putString(NOTE_TYPE, "1");
        bundle.putString(NOTE_CONTENT, content);
        bundle.putString(NOTE_TIME, time);
        bundle.putString(NOTE_TITLE, title);
        intent.putExtras(bundle);
        startActivityForResult(intent, 1);

    }
/*
    适配器绑定的数据源对象不能进行更换，一旦更换的对象的引用
    虽然可以重新建立适配器的内容，但是适配器无法进行更新界面的功能。

* */
    @Override
    public void click_delete(final View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除便条");
        builder.setMessage("你确定要删除当前便条？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int position = (int) view.getTag();
                unit_item item = list.get(position);
                String id=item.getId();
                list.remove(position);
                adapter.notifyDataSetChanged();
                Cursor cursor = db.query("note", null, "id="+id , null, null, null, null);
                while (cursor.moveToNext()) {
                    String sql_delete = "update note set content=\"\"  where id=" + id;
                    String sql_delelte_ = "update note set title=\"\"  where id=" + id;
                    db.execSQL(sql_delete);
                    db.execSQL(sql_delelte_);
                }
                Toast.makeText(Note_interface.this, "删除成功!", Toast.LENGTH_SHORT).show();
                find_info.setText("");
                setdata();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();

    }

    public void find() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                list.removeAll(list);
                list=getdata();
                String info_title = find_info.getText().toString();
                if (info_title.equals(""))
                {
                         setdata();
                         return;
                }
                unit_item item;
                for(int i=0;i<list.size();i++)
                {    item=list.get(i);
                     if(info_title.equals(item.getTitle()))
                     {
                         lists.add(item);
                     }
                }
                if(lists.isEmpty())
                {
                    Toast.makeText(Note_interface.this,"没有该笔记存在！",Toast.LENGTH_SHORT).show();
                }
                list.removeAll(list);
                adapter.notifyDataSetChanged();
                list=lists;
                listView.setAdapter(new listview_adapter(list,Note_interface.this,R.layout.note_interface_unit,Note_interface.this));
            }
        });

    }
}



