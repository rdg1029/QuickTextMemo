package com.quick.simplememo;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.fsn.cauly.*;
import android.provider.*;
import android.support.v4.app.*;
import android.net.*;

public class MainActivity extends Activity
{

    NotificationManager nm;
    NotificationCompat.Builder notiBuilder;
    Boolean bFirst;
    EditText memo;
    String str;
    ToggleButton quickMemoToggle;
    Boolean toggleState;

    CaulyCloseAd closeAdExit, closeAdSave;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirstDialog();

        statusBarColor();
        CaulyAd();

        nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(579);

        memo = (EditText)findViewById(R.id.memo_main);
        quickMemoToggle = (ToggleButton)findViewById(R.id.tb_quickmemo);

        ToggleBackgroundState();

        SharedPreferences pref = getSharedPreferences("pref", 0);
        str = pref.getString("memo", str);
        toggleState = pref.getBoolean("tb_quickmemo", true);
        memo.setText(str);
        quickMemoToggle.setChecked(toggleState);
    }

    public void FirstDialog()
    {
        try{
            SharedPreferences mPref = getSharedPreferences("isFirst", Activity.MODE_PRIVATE);
            bFirst = mPref.getBoolean("isFirst", false);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))
            {
                new AlertDialog.Builder(this).setTitle("").setMessage(getResources().getString(R.string.tv_first_check))
                        .setPositiveButton(getResources().getString(R.string.tv_first_check_confirm), new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                                startActivity(myIntent);
                                finish();
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.tv_first_check_restart), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.tv_first_check_exit), new DialogInterface.OnClickListener()
                        {

                            @Override
                            public void onClick(DialogInterface p1, int p2)
                            {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
            else
            {
                bFirst = true;
            }

        }
        catch(Exception e) {}
    }

    public void share(View v)
    {
        CommitState();

        Intent i = new Intent(android.content.Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, str);
        startActivity(i);
    }

    public void clear(View v)
    {
        if(closeAdSave.isModuleLoaded()) {
            closeAdSave.show(this);
        }
        else {
            ClearDialog();
        }
    }

    public void ClearDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.tv_cleardialog_msg))
                .setPositiveButton(getResources().getString(R.string.tv_cleardialog_btn_clear), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        memo.setText("");
                    }
                })
                .setNegativeButton(getResources().getString(R.string.tv_cleardialog_btn_cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void ToggleBackgroundState()
    {
        if(quickMemoToggle.isChecked())
        {
            quickMemoToggle.setBackgroundDrawable(getResources().getDrawable(R.drawable.toggle_bg_on));
        }
        else
        {
            quickMemoToggle.setBackgroundDrawable(getResources().getDrawable(R.drawable.toggle_bg_off));
        }

        quickMemoToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked == true)
                {
                    quickMemoToggle.setBackgroundDrawable(getResources().getDrawable(R.drawable.toggle_bg_on));
                }
                else
                {
                    quickMemoToggle.setBackgroundDrawable(getResources().getDrawable(R.drawable.toggle_bg_off));
                }
            }

        });
    }

    public void ToggleQuickMemo()
    {
        if(bFirst == false) return;
        if(quickMemoToggle.isChecked())
        {
            BuildNotification();
            nm.notify(579, notiBuilder.build());
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.tv_toast_quickmemo), Toast.LENGTH_SHORT).show();
        }
        else
        {
            nm.cancel(579);
        }
    }

    public void BuildNotification()
    {
        Intent i = new Intent(this, MemoService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, i, PendingIntent.FLAG_UPDATE_CURRENT);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel("QuickMemoPush", "QuickMemoPush", NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(notificationChannel);
            notiBuilder = new NotificationCompat.Builder(getApplicationContext(), notificationChannel.getId());
        }
        else
        {
            notiBuilder = new NotificationCompat.Builder(getApplicationContext());
        }
        notiBuilder.setContentText(getResources().getString(R.string.tv_noti_msg))
                .setSmallIcon(R.mipmap.ic_notification)
                .setPriority(Notification.PRIORITY_MIN)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    public void CommitState()
    {
        SharedPreferences pref = getSharedPreferences("pref", 0);
        SharedPreferences.Editor edit = pref.edit();
        str = memo.getText().toString();
        edit.putString("memo", str);
        edit.putBoolean("tb_quickmemo", quickMemoToggle.isChecked());
        edit.apply();
    }

    public void statusBarColor()
    {
        View view = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (view != null) {
                // 23 버전 이상일 때
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
            }
        }else if (Build.VERSION.SDK_INT >= 21) {
            // 21 버전 이상일 때
            getWindow().setStatusBarColor(Color.BLACK);
        }
    }

    @Override
    protected void onStop()
    {
        CommitState();
        ToggleQuickMemo();
        super.onStop();
    }

    @Override
    protected void onResume()
    {
        if(closeAdExit == null & closeAdSave == null) return;
        closeAdExit.resume(this);
        closeAdSave.resume(this);
        super.onResume();
    }

    public void CaulyAd()
    {
        CaulyAdInfo caulyAdInfo = new CaulyAdInfoBuilder("jClcoxFa").build();

        closeAdExit = new CaulyCloseAd();
        closeAdExit.setButtonText(getResources().getString(R.string.tv_main_exit_n), getResources().getString(R.string.tv_main_exit_y));
        closeAdExit.setDescriptionText(getResources().getString(R.string.tv_main_exit));
        closeAdExit.setAdInfo(caulyAdInfo);
        closeAdExit.setCloseAdListener(new CaulyCloseAdListener()
        {

            @Override
            public void onReceiveCloseAd(CaulyCloseAd p1, boolean p2)
            {
                // TODO: Implement this method
            }

            @Override
            public void onShowedCloseAd(CaulyCloseAd p1, boolean p2)
            {
                // TODO: Implement this method
            }

            @Override
            public void onFailedToReceiveCloseAd(CaulyCloseAd p1, int p2, String p3)
            {
                // TODO: Implement this method
            }

            @Override
            public void onLeftClicked(CaulyCloseAd p1)
            {
                // TODO: Implement this method
            }

            @Override
            public void onRightClicked(CaulyCloseAd p1)
            {
                finish();
            }

            @Override
            public void onLeaveCloseAd(CaulyCloseAd p1)
            {
                // TODO: Implement this method
            }

        });

        closeAdSave = new CaulyCloseAd();
        closeAdSave.setButtonText(getResources().getString(R.string.tv_cleardialog_btn_cancel), getResources().getString(R.string.tv_cleardialog_btn_clear));
        closeAdSave.setDescriptionText(getResources().getString(R.string.tv_cleardialog_msg));
        closeAdSave.setAdInfo(caulyAdInfo);
        closeAdSave.setCloseAdListener(new CaulyCloseAdListener()
        {

            @Override
            public void onReceiveCloseAd(CaulyCloseAd p1, boolean p2)
            {
                // TODO: Implement this method
            }

            @Override
            public void onShowedCloseAd(CaulyCloseAd p1, boolean p2)
            {
                // TODO: Implement this method
            }

            @Override
            public void onFailedToReceiveCloseAd(CaulyCloseAd p1, int p2, String p3)
            {
                // TODO: Implement this method
            }

            @Override
            public void onLeftClicked(CaulyCloseAd p1)
            {
                // TODO: Implement this method
            }

            @Override
            public void onRightClicked(CaulyCloseAd p1)
            {
                memo.setText("");
            }

            @Override
            public void onLeaveCloseAd(CaulyCloseAd p1)
            {
                // TODO: Implement this method
            }

        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(closeAdExit.isModuleLoaded()) {
                closeAdExit.show(this);
            }
            else {
                showDefaultClosePopup();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void showDefaultClosePopup()
    {
        new AlertDialog.Builder(this).setTitle("").setMessage(getString(R.string.tv_main_exit))
                .setPositiveButton(getString(R.string.tv_main_exit_y), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.tv_main_exit_n),null)
                .show();
    }

}
