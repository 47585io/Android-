package com.mycompany.who.Activity;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.*;


public class BaseActivity extends Activity
{
	protected static boolean can=false;
	protected InputorDialog input;
	
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		input = new InputorDialog(this);
		if(can){
	        dismiss_Title(this);
			dismiss_ActionBar(this);
		    dismiss_DownBar(this);
		}
		getWindow().setBackgroundDrawable(null);
    }

	public static void dismiss_Title(Activity act){
		//取消标题
		act.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}
	public static void dismiss_ActionBar(Activity act){
		//取消状态栏		
		act.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
								 WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	public static void dismiss_DownBar(Activity act){
		//隐藏底部工具栏
	    act.getWindow().getDecorView().setSystemUiVisibility(
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}
	public static void restore_ActionBar(Activity act){
		//显示状态栏
		act.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	public static void restore_Title(Activity act){
		act.getActionBar().show();
		
	}
	/*  每一个Activity中都有一个Window，Window中有三大组件，状态栏，标题，底部栏，在这之中还有一个DecorView，这里用于放我们自己的布局  */
	
	public void onWindowFocusChanged(boolean hasFocus)
	{
        //被切换到后台及切回前台窗口焦点都会变化，而只有切回才重新隐藏系统UI控件
		super.onWindowFocusChanged(hasFocus);    
		
		if(can)
		    dismiss_DownBar(this);
		else
			getWindow().setNavigationBarColor(Colors.Bg);	
    }
	
	public static class InputorDialog extends Dialog{
		
		private WhenInputEnter listener;
		public EditText text;
		
		public InputorDialog(Context cont){
			super(cont);
			setTitle("输入者");
			text=new EditText(getContext());
			setContentView(text);
		}
		public void setlistener(WhenInputEnter li){
			listener=li;
		}
		
		public void dismiss_DownBar(Dialog dlog){
			//隐藏底部工具栏
			dlog.getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}

		public void onWindowFocusChanged(boolean hasFocus)
		{
			//展示Dialog时，Dialog获得焦点，配置一下
			super.onWindowFocusChanged(hasFocus);
			
			if(can)
				dismiss_DownBar(this);
			else
				getWindow().setNavigationBarColor(Colors.Bg);
				
		}
		@Override
		public boolean onKeyUp(int keyCode, KeyEvent event)
		{
			if(keyCode==KeyEvent.KEYCODE_ENTER){
				String src=text.getText().toString().trim();
				src=src.replaceAll("\n","");
				listener.whenenter(src);
				cancel();
			}
			return super.onKeyUp(keyCode, event);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event)
		{
			return false;
		}

		@Override
		public void show()
		{
			Edit.openInputor(text.getContext(),text);
			super.show();
		}	
		
		
	}
	
}


