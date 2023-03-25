package com.mycompany.who.SuperVisor.Moudle.Config;
import android.view.*;

public class Interfaces
{
	
	public static interface Init{

		public void loadSize(int width, int height ,boolean is)

		public void init()

	}
	
	public static interface BubbleEvent{
		
		public boolean onKeyUp(int keyCode, KeyEvent event)
		
		public boolean onTouchEvent(MotionEvent event)
		
		public boolean BubbleKeyEvent(int keyCode,KeyEvent event)
		
		public boolean BubbleMotionEvent(MotionEvent event)
		
		public void setTarget(BubbleEvent target)
		
	}
	
}
