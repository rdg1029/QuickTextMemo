package com.quick.simplememo;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.provider.*;
import android.support.v4.app.*;
import android.net.*;

public class MemoService extends Service
{
	private NotificationManager nm;
	private NotificationCompat.Builder notiBuilder;
	private WindowManager wm;
	private int flag;
	private View view, menu, dialog;
	private ViewGroup memo_parentView, memo_childView, menu_parentView, menu_childView, dialog_parentView, dialog_childView;
	private EditText memo;
	private String str;
	private Animation scale_in, scale_out;

	@Override
	public void onCreate()
	{
		scale_in = AnimationUtils.loadAnimation(this, R.anim.scale_in);
		scale_out = AnimationUtils.loadAnimation(this, R.anim.scale_out);
		
		try
		{
			Memo();
		}
		catch(RuntimeException e)
		{
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))
			{
				Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
				startActivity(i);
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.tv_first_check) + "\n" + getResources().getString(R.string.tv_first_check_restart), Toast.LENGTH_LONG).show();
			}
		}
		
		super.onCreate();
	}
	
	public void Memo()
	{
		LayoutInflater inflater_memo = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater_memo.inflate(R.layout.memo, null);

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
			WindowManager.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.WRAP_CONTENT,
			OverlayType(),
			WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
			PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.CENTER;

		memo_parentView = (FrameLayout)view.findViewById(R.id.memo_pv);
		memo_childView = (LinearLayout)view.findViewById(R.id.memo_cv);

		wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		wm.addView(memo_parentView, params);

		memo_childView.startAnimation(scale_in);

		memo = (EditText)view.findViewById(R.id.memo);
		SharedPreferences pref = getSharedPreferences("pref", 0);
		str = pref.getString("memo", str);
		memo.setText(str);

		final Button close = (Button)view.findViewById(R.id.memo_close);
		final Button menu = (Button)view.findViewById(R.id.memo_menu);
		close.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					memo_childView.startAnimation(scale_out);
					new Handler().postDelayed(new Runnable()
						{
							@Override
							public void run()
							{
								if(view == null) return;
								wm.removeView(view);
								view = null;
								stopSelf();
							}
						}, 300);
					close.setClickable(false);
					menu.setClickable(false);
				}
			});
		menu.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					Menu();
				}
			});
	}
	
	public void Menu()
	{
		LayoutInflater inflater_menu = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		menu = inflater_menu.inflate(R.layout.menu, null);
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
			WindowManager.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.WRAP_CONTENT,
			OverlayType(),
			WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
			PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.CENTER;

		menu_parentView = (FrameLayout)menu.findViewById(R.id.menu_pv);
		menu_childView = (LinearLayout)menu.findViewById(R.id.menu_cv);
		
		menu_childView.startAnimation(scale_in);

		final WindowManager wm_menu = (WindowManager)getSystemService(WINDOW_SERVICE);
		wm_menu.addView(menu_parentView, params);

		final Button clear = (Button)menu.findViewById(R.id.menu_clear);
		final Button share = (Button)menu.findViewById(R.id.menu_share);
		final Button launchapp = (Button)menu.findViewById(R.id.menu_launchapp);
		final Button close_menu = (Button)menu.findViewById(R.id.menu_close);
		clear.setOnClickListener(new OnClickListener()
		{
				@Override
				public void onClick(View p1)
				{
					wm_menu.removeView(menu);
					ClearDialog();
				}
		});
		share.setOnClickListener(new OnClickListener()
		{
				@Override
				public void onClick(View p1)
				{
					CommitMemoString();
					
					Intent i = new Intent(Intent.ACTION_SEND);
					i.setType("text/plain");
					i.putExtra(Intent.EXTRA_TEXT, str);
					startActivity(i);
					
					wm_menu.removeView(menu);
					wm.removeView(view);
					stopSelf();
				}
		});
		launchapp.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View p1)
				{
					CommitMemoString();
					Intent i = new Intent(getApplicationContext(), MainActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(i);
					
					wm_menu.removeView(menu);
					wm.removeView(view);
					stopSelf();
				}
		});
		close_menu.setOnClickListener(new OnClickListener()
		{
				@Override
				public void onClick(View p1)
				{
					menu_childView.startAnimation(scale_out);
					new Handler().postDelayed(new Runnable()
						{
							@Override
							public void run()
							{
								if(menu == null) return;
								wm_menu.removeView(menu);
								menu = null;
							}
						}, 300);
					clear.setClickable(false);
					share.setClickable(false);
					launchapp.setClickable(false);
					close_menu.setClickable(false);
				}
		});
	}
	
	public void ClearDialog()
	{
		LayoutInflater inflater_dialog = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialog = inflater_dialog.inflate(R.layout.clear_dialog, null);
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
		WindowManager.LayoutParams.WRAP_CONTENT,
		WindowManager.LayoutParams.WRAP_CONTENT,
		OverlayType(),
		WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
		PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.CENTER;

		dialog_parentView = (FrameLayout)dialog.findViewById(R.id.dialog_pv);
		dialog_childView = (LinearLayout)dialog.findViewById(R.id.dialog_cv);
		
		dialog_childView.startAnimation(scale_in);

		final WindowManager wm_dialog = (WindowManager)getSystemService(WINDOW_SERVICE);
		wm_dialog.addView(dialog_parentView, params);

		final Button clear = (Button)dialog.findViewById(R.id.clear);
		final Button cancel = (Button)dialog.findViewById(R.id.cancel);
		clear.setOnClickListener(new OnClickListener() 
		{
				@Override
				public void onClick(View p1)
				{
					memo.setText("");
					dialog_childView.startAnimation(scale_out);
					new Handler().postDelayed(new Runnable()
						{
							@Override
							public void run()
							{
								if(dialog == null) return;
								wm_dialog.removeView(dialog);
								dialog = null;
							}
						}, 300);
					clear.setClickable(false);
					cancel.setClickable(false);
				}
		});
		cancel.setOnClickListener(new OnClickListener() 
		{
				@Override
				public void onClick(View p1)
				{
					dialog_childView.startAnimation(scale_out);
					new Handler().postDelayed(new Runnable()
						{
							@Override
							public void run()
							{
								if(dialog == null) return;
								wm_dialog.removeView(dialog);
								dialog = null;
							}
						}, 300);
					cancel.setClickable(false);
					clear.setClickable(false);
				}
		});
	}
	
	int OverlayType()
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			flag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
		}
		else
		{
			flag = WindowManager.LayoutParams.TYPE_PHONE;
		}
		return flag;
	}
	
	public void CommitMemoString()
	{
		SharedPreferences pref = getSharedPreferences("pref", 0);
		SharedPreferences.Editor edit = pref.edit();
		str = memo.getText().toString();
		edit.putString("memo", str);
		edit.apply();
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

	@Override
	public void onDestroy()
	{
		CommitMemoString();
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent p1)
	{
		// TODO: Implement this method
		return null;
	}

}
