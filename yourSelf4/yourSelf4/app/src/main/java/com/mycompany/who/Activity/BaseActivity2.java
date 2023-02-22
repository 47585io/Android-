package com.mycompany.who.Activity;
import android.view.*;
import android.view.View.*;
import com.mycompany.who.*;
import android.os.*;
import android.content.res.*;

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

	public abstract class onmyTouchListener implements OnTouchListener
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

	public void setOnTouchListenrS(View ... S){
		for(View s:S){
			s.setOnTouchListener(new onmyTouchListener(){

					@Override
					public boolean ontouch(View p1, MotionEvent p2)
					{
						if(p2.getAction()==MotionEvent.ACTION_DOWN)
						    whenTouchView();
						return false;
					}
				});
		}
	}
	
	public void whenTouchView(){}
	
}
