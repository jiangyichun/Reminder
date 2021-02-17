package com.jyc.reminder;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jyc.reminder.db.EventDb;
import com.jyc.reminder.pojo.Event;
import com.jyc.reminder.util.ToastUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;

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
        if(this.saveEvent()) {
            this.navToMain();
        }
    }

    private boolean saveEvent() {
        String name = eventNameInput.getText().toString();
        String date = eventDateInput.getText().toString();
        String time = eventTimeInput.getText().toString();
        String remind = eventRemindInput.getText().toString();

        if(name == null || "".equals(name.trim())) {
            //Toast.makeText(this,"事件名称不能为空！", Toast.LENGTH_SHORT).show();
            ToastUtils.show(this, "事件名称不能为空！");
            return false;
        }

        if(date == null || "".equals(date.trim())) {
            Toast.makeText(this,"日期不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(time == null || "".equals(time.trim())) {
            Toast.makeText(this,"时间不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(remind == null || "".equals(remind.trim())) {
            Toast.makeText(this,"多久提醒不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            try {
                Integer.parseInt(remind);
            } catch(Exception e) {
                Toast.makeText(this,"多久提醒必须为整数！", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        Event event = new Event();
        event.setTitle(name);
        event.setDatetime(date + " " + time);
        event.setOverdue(false);

        EventDb eventDb = new EventDb(this);
        eventDb.addEvent(event);
        return true;
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