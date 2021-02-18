package com.jyc.reminder.service;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import com.jyc.reminder.R;
import com.jyc.reminder.db.EventDb;

public class AlarmService extends Service {

    public static final int PLAY_MAX_TIMES = 10;

    private MediaPlayer mediaPlayer;
    private int soundPlayCount = 0;

    public AlarmService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer = MediaPlayer.create(this, R.raw.jyx);
        mediaPlayer.setLooping(false);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer arg0) {
                        try {
                            if(soundPlayCount < PLAY_MAX_TIMES) {
                                mediaPlayer.start();
                                ++ soundPlayCount;
                            }
                        }  catch(Exception e) {
                            Log.e("Sound replay", "Replay error", e);
                        }
                    }
                });
        soundPlayCount = 0;
        mediaPlayer.start();
        boolean isMarkOverdue = intent.getIntExtra("overdue", 0) == 1;
        if(isMarkOverdue) {
            // Mark the event overdue
            new EventDb(this).markOverdue(intent.getAction());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(AlarmService.this)
                .setIcon(R.mipmap.reminder_dialog_icon)
                .setTitle("提醒").setMessage(intent.getStringExtra("message"))
                .setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        soundPlayCount = PLAY_MAX_TIMES;
                        mediaPlayer.stop();
                        dialog.dismiss();
                    }
                });

        Dialog dialog = builder.create();
        // 设置点击其他地方不可取消此 Dialog
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        // 8.0系统加强后台管理，禁止在其他应用和窗口弹提醒弹窗，如果要弹，必须使用TYPE_APPLICATION_OVERLAY，否则弹不出
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
        }else {
            dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        }
        dialog.show();
        return super.onStartCommand(intent, flags, startId);
    }
}