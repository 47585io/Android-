package com.mycompany.who.SuperVisor.CodeMoudle.Base.View3;

import android.view.*;


/* 事件冒泡 */
public abstract interface BubbleEvent
{

	public abstract boolean onKeyUp(int keyCode, KeyEvent event)

	public abstract boolean onTouchEvent(MotionEvent event)

	public abstract boolean BubbleKeyEvent(int keyCode,KeyEvent event)

	public abstract boolean BubbleMotionEvent(MotionEvent event)

	public abstract void setTarget(BubbleEvent target)

	public abstract BubbleEvent getTarget()

}
