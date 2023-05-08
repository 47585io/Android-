package com.mycompany.who.Activity;

import android.os.*;
import android.view.*;
import android.view.View.*;

public class BaseActivity2 extends BaseActivity
{
	public static int Displaywidth,Displayheight;
	//显示屏大小
	public static float MouseRx,MouseRy;
	//当前用户手的绝对坐标
	public static View nowView;
	public static float Mousex,Mousey;
    //用户手在View上的坐标
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
	    Display display = getWindowManager().getDefaultDisplay();  
		Displaywidth=display.getWidth();
	    Displayheight=display.getHeight();
		
		super.onCreate(savedInstanceState);
	}

	public static abstract class TouchCollector implements OnTouchListener
	{
		@Override
		public boolean onTouch(View p1, MotionEvent p2)
		{
			Mousex=p2.getX();
			Mousey=p2.getY();
			MouseRx=p2.getRawX();
			MouseRy=p2.getRawY();
			nowView=p1;
			return ontouch(p1,p2);
		}

		abstract public boolean ontouch(View p1,MotionEvent p2);

	}

	public void TouchCollector(){
		//鼠标收集
	}

	public static void setOnTouchListenrS(View ... S){
		for(View s:S){
			s.setOnTouchListener(new TouchCollector(){

					@Override
					public boolean ontouch(View p1, MotionEvent p2)
					{
						return false;
					}
				});
		}
	}
	
}
