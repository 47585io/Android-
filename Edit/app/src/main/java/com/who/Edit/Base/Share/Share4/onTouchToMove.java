package com.who.Edit.Base.Share.Share4;

import android.view.*;
import android.view.View.*;

public abstract class onTouchToMove implements OnTouchListener
{

	public int id;
	public float lastX,lastY,nowX,nowY;

	public abstract boolean sendMovePos(View v, MotionEvent event, float dx, float dy);

	@Override
	public boolean onTouch(View p1, MotionEvent p2)
	{
		boolean consume = true;
		calc(p2);
		consume = sendMovePos(p1,p2,nowX-lastX,nowY-lastY);
		save(p2);
		return consume;
	}
	
	public void calc(MotionEvent p2)
	{
		if(p2.getActionMasked()==MotionEvent.ACTION_DOWN){
			id = p2.getPointerId(0);
			lastX=p2.getX(0);
			lastY=p2.getY(0);
		}
		else if(p2.getHistorySize()>0){
			int index = p2.findPointerIndex(id);
			if(index!=-1){
			    nowX=p2.getX(index);
			    nowY=p2.getY(index);
			}
			//手指上升了，就不能移动了
		}
	}
	public void save(MotionEvent p2)
	{
		if(p2.getHistorySize()>0){	
		    lastX=nowX;
		    lastY=nowY;
		}
	}

	public float MoveX(){
		return nowX-lastX;
	}
	public float MoveY(){
		return nowY-lastY;
	}

	/* 未预料的滑动，在多个手指时下标会出现异常 */
	public static float MoveX(MotionEvent p2){
		float lastX = 0,nowX = 0;
		if(p2.getHistorySize()>0){
		    nowX=p2.getX(0);
		    lastX=p2.getHistoricalX(0,p2.getHistorySize()-1);
		}
		return nowX-lastX;
	}
	public static float MoveY(MotionEvent p2){
		float lastY = 0,nowY = 0;
		if(p2.getHistorySize()>0){
		    nowY=p2.getY(0);
		    lastY=p2.getHistoricalY(0,p2.getHistorySize()-1);
		}
		return nowY-lastY;
	}

	public static abstract interface OnTouchToMove extends OnTouchListener{

		public abstract void onMoveToLeft(View p1,MotionEvent p2,float dx);

		public abstract void onMoveToRight(View p1,MotionEvent p2,float dx)

		public abstract void onMoveToTop(View p1,MotionEvent p2,float dy);

		public abstract void onMoveToDown(View p1,MotionEvent p2,float dy)

	    public abstract boolean onMoveEnd(View p1,MotionEvent p2)

	}

}

