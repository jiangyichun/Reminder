package com.jyc.reminder.ui.main;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.jyc.reminder.R;
import com.jyc.reminder.db.EventDb;
import com.jyc.reminder.pojo.Event;
import com.jyc.reminder.service.AlarmService;
import com.jyc.reminder.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class EventListAdapter extends BaseAdapter implements ListAdapter {

    private List<Event> list = new ArrayList<>();
    private Context context;

    public EventListAdapter(List<Event> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.event_list, null);
        }

        //Handle TextView and display string from your list
        TextView listItemText = (TextView)view.findViewById(R.id.listItemTxt);
        listItemText.setText(list.get(position).toString());

        //Handle buttons and add onClickListeners
        Button deleteBtn = view.findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                handleDelete(position);
            }
        });

        return view;
    }

    private void handleDelete(int position) {
        Event event = list.get(position);
        // Remove it from the list
        list.remove(position);
        // Delete it from DB
        new EventDb(this.context).deleteEvent(event.getUuid());
        if(event.getOverdue() == null || !event.getOverdue()) {
            // The event is a not overdue one, then also need to cancel the alarms
            this.cancelAlarm(event);
        }
        notifyDataSetChanged();
        ToastUtils.show(context, "删除成功！");
    }

    private void cancelAlarm(Event event) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Before event happens
        Intent intent = new Intent(context, AlarmService.class);
        //action is part of the identity of the PendingIntent instance
        intent.setAction(event.getUuid());
        // Request code '0' for alarm before
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

        // 2. Set alarm when event happens
        intent = new Intent(context, AlarmService.class);
        //action is part of the identity of the PendingIntent instance
        intent.setAction(event.getUuid());
        // Request code '1' for alarm when event happens
        pendingIntent = PendingIntent.getService(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }
}
