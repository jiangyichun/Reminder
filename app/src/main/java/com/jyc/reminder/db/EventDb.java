package com.jyc.reminder.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.jyc.reminder.pojo.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventDb extends SQLiteOpenHelper {

    public EventDb(@Nullable Context context) {
        super(context, "events_db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table events(uuid varchar(32) primary key, title text, datetime text, remind_minute int, overdue boolean)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public List<Event> findAll() {
        List<Event> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from events ", null);
        Event event = null;
        while (cursor.moveToNext()) {
            event = new Event();
            event.setUuid(cursor.getString(cursor.getColumnIndex("uuid")));
            event.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            event.setDatetime(cursor.getString(cursor.getColumnIndex("datetime")));
            event.setRemindMinute(cursor.getInt(cursor.getColumnIndex("remind_minute")));
            event.setOverdue(cursor.getInt(cursor.getColumnIndex("overdue")) == 1);
            result.add(event);
        }
        db.close();
        return result;
    }

    public void addEvent(Event event) {
        if(event.getUuid() == null) {
            event.setUuid(UUID.randomUUID().toString());
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("insert into events (uuid, title, datetime, remind_minute, overdue) values(?, ?, ?, ?, ?)",
                new Object[] { event.getUuid(), event.getTitle(), event.getDatetime(), event.getRemindMinute(), event.getOverdue() });
        db.close();
    }

    public void deleteEvent(String uuid) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from events where uuid = ?", new Object[] { uuid });
        db.close();
    }

    public void markOverdue(String uuid) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("update events set overdue = 1 where uuid = ?",
                new Object[] { uuid });
        db.close();
    }
}
