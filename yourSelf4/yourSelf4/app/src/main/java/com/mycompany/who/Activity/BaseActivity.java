package com.mycompany.who.Activity;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.mycompany.who.Edit.*;
import android.content.res.*;
import android.graphics.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Base.Edit.*;

public class BaseActivity extends Activity
{
	protected static boolean can=false;
	protected InputorDialog input;
	
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		input = new InputorDialog(this);
		if(can){
	        dismiss_Title_And_ActionBar(this);
		    dismiss_DownBar(this);
		}
		getWindow().setBackgroundDrawable(null);
    }

	public static void dismiss_Title_And_ActionBar(Activity act){
		act.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //取消标题
        act.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
								 WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//取消状态栏		
	}
	public static void dismiss_DownBar(Activity act){
		//隐藏底部工具栏
	    act.getWindow().getDecorView().setSystemUiVisibility(
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}
	public static void setActionBarColor(Activity act,int color){
	    Window window = act.getWindow();
	    window.setStatusBarColor(color);
	}
	
	public void onWindowFocusChanged(boolean hasFocus)
	{
        //被切换到后台及切回前台窗口焦点都会变化，而只有切回才重新隐藏系统UI控件
		super.onWindowFocusChanged(hasFocus);
        if (hasFocus&&can)
			dismiss_DownBar(this);
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
			if (hasFocus&&can)
				dismiss_DownBar(this);
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


