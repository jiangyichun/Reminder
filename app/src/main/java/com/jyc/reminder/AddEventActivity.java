package com.jyc.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jyc.reminder.db.EventDb;
import com.jyc.reminder.pojo.Event;
import com.jyc.reminder.service.AlarmService;
import com.jyc.reminder.util.ToastUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.UUID;

public class AddEventActivity extends AppCompatActivity implements DatePicker.OnDateChangedListener, TimePicker.OnTimeChangedListener {

    private TextView eventDateInput;
    private TextView eventTimeInput;
    private int year, month, day, hour, minute;
    private EditText eventNameInput;
    private EditText eventRemindInput;
    private Button addBtn;
    private Button cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        this.initializeDateTime();
        this.initializeView();
    }

    private void initAndShowDateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LocalDate localDate = LocalDate.of(year, month, day);
                eventDateInput.setText(localDate.toString());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(this, R.layout.dialog_date, null);
        final DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.datePicker);
        datePicker.setOnDateChangedListener(this);
        dialog.setTitle("选择日期");
        dialog.setView(dialogView);
        dialog.show();
        datePicker.init(year, month - 1, day, this);
    }

    private void initAndShowTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LocalTime localTime = LocalTime.of(hour, minute);
                eventTimeInput.setText(localTime.toString());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        View dialogView = View.inflate(this, R.layout.dialog_time, null);
        TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.timePicker);
        timePicker.setHour(hour);
        timePicker.setMinute(minute);
        timePicker.setIs24HourView(true); //设置24小时制
        timePicker.setOnTimeChangedListener(this);
        dialog.setTitle("选择时间");
        dialog.setView(dialogView);
        dialog.show();
    }

    private void initializeDateTime() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR);
        minute = calendar.get(Calendar.MINUTE);
    }

    private void initializeView() {
        eventDateInput = findViewById(R.id.eventDateInput);
        eventDateInput.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AddEventActivity.this.initAndShowDateDialog();
            }
        });

        eventTimeInput = findViewById(R.id.eventTimeInput);
        eventTimeInput.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AddEventActivity.this.initAndShowTimeDialog();
            }
        });

        eventNameInput = findViewById(R.id.eventNameInput);
        eventRemindInput = findViewById(R.id.remindValueInput);

        addBtn = findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AddEventActivity.this.handleAddEvent();
            }
        });

        cancelBtn = findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AddEventActivity.this.navToMain();
            }
        });
    }

    private void handleAddEvent() {
        Event event = this.saveEvent();
        if(event != null) {
            this.setUpEventAlarm(event);
            this.navToMain();
        }
    }

    private Event saveEvent() {
        String name = eventNameInput.getText().toString();
        String date = eventDateInput.getText().toString();
        String time = eventTimeInput.getText().toString();
        String remind = eventRemindInput.getText().toString();

        if(name == null || "".equals(name.trim())) {
            //Toast.makeText(this,"事件名称不能为空！", Toast.LENGTH_SHORT).show();
            ToastUtils.show(this, "事件名称不能为空！");
            return null;
        }

        if(date == null || "".equals(date.trim())) {
            Toast.makeText(this,"日期不能为空！", Toast.LENGTH_SHORT).show();
            return null;
        }

        if(time == null || "".equals(time.trim())) {
            Toast.makeText(this,"时间不能为空！", Toast.LENGTH_SHORT).show();
            return null;
        }

        if(remind == null || "".equals(remind.trim())) {
            Toast.makeText(this,"多久提醒不能为空！", Toast.LENGTH_SHORT).show();
            return null;
        } else {
            try {
                Integer.parseInt(remind);
            } catch(Exception e) {
                Toast.makeText(this,"多久提醒必须为整数！", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        Event event = new Event();
        event.setUuid(UUID.randomUUID().toString());
        event.setTitle(name);
        event.setDatetime(date + " " + time);
        event.setRemindMinute(Integer.parseInt(remind));
        event.setOverdue(false);

        EventDb eventDb = new EventDb(this);
        eventDb.addEvent(event);
        return event;
    }

    private void setUpEventAlarm(Event event) {
        String[] dateTimeStr = event.getDatetime().split(" ");
        String[] dateStr = dateTimeStr[0].split("-");
        String[] timeStr = dateTimeStr[1].split(":");
        int year = Integer.parseInt(dateStr[0]);
        int month = Integer.parseInt(dateStr[1]);
        int day = Integer.parseInt(dateStr[2]);
        int hour = Integer.parseInt(timeStr[0]);
        int minute = Integer.parseInt(timeStr[1]);
        Log.i("alarm", String.format("Alarm year: %d, month: %d, day: %d, hour: %d, minute: %d", year, month, day, hour, minute));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // The alarm time of reminding before event happens
        long millisOfAlarmAhead = calendar.getTimeInMillis() - event.getRemindMinute() * 60 * 1000;
        String alarmMessageAhead = String.format("还有 %d 分钟：'%s'", event.getRemindMinute(), event.getTitle());

        // The alarm time of reminding when event really happens
        long millisOfAlarmFinal = calendar.getTimeInMillis();

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // 1. Set alarm before event happens
        Intent intent = new Intent(this, AlarmService.class);
        //action is part of the identity of the PendingIntent instance
        intent.setAction(event.getUuid());
        intent.putExtra("message", alarmMessageAhead);
        // Request code '0' for alarm before
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, millisOfAlarmAhead, pendingIntent);

        // 2. Set alarm when event happens
        intent = new Intent(this, AlarmService.class);
        //action is part of the identity of the PendingIntent instance
        intent.setAction(event.getUuid());
        intent.putExtra("message", event.getTitle());
        intent.putExtra("overdue", 1);
        // Request code '1' for alarm when event happens
        pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, millisOfAlarmFinal, pendingIntent);
    }

    private void navToMain() {
        Intent i= new Intent(this, MainActivity.class);
        startActivity(i);
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        this.month = monthOfYear + 1;
        this.day = dayOfMonth;
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;
    }
}