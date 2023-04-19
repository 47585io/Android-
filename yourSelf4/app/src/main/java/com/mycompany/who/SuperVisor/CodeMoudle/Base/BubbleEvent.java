package com.mycompany.who.SuperVisor.CodeMoudle.Base;

import android.view.*;

public interface BubbleEvent
{

	public boolean onKeyUp(int keyCode, KeyEvent event)

	public boolean onTouchEvent(MotionEvent event)

	public boolean BubbleKeyEvent(int keyCode,KeyEvent event)

	public boolean BubbleMotionEvent(MotionEvent event)

	public void setTarget(BubbleEvent target)

	public BubbleEvent getTarget()

}
