package com.mycompany.who.SuperVisor.CodeMoudle.Base.View.Share;
import android.view.View.*;
import android.view.*;

public abstract class OnTouchToMove implements OnTouchListener
{

	public float lastX,lastY,nowX,nowY;

	abstract public void onMoveToLeft(View p1,MotionEvent p2,float dx);
	abstract public void onMoveToRight(View p1,MotionEvent p2,float dx)
	abstract public void onMoveToTop(View p1,MotionEvent p2,float dy);
	abstract public void onMoveToDown(View p1,MotionEvent p2,float dy)
	abstract public boolean onMoveEnd(View p1,MotionEvent p2)

	@Override
	public boolean onTouch(View p1, MotionEvent p2)
	{
		if(p2.getActionMasked()==MotionEvent.ACTION_DOWN){
			lastX=p2.getRawX();
			lastY=p2.getRawY();
		}
		else if(p2.getHistorySize()>0){
			nowX=p2.getRawX();
			nowY=p2.getRawY();

			if(lastX>nowX)
				onMoveToLeft(p1,p2,lastX-nowX);
			else if(lastX<nowX)
				onMoveToRight(p1,p2,nowX-lastX);

			if(lastY>nowY)
				onMoveToTop(p1,p2,lastY-nowY);
			else if(lastY<nowY)
				onMoveToDown(p1,p2,nowY-lastY);

			lastX=nowX;
			lastY=nowY;
		}

		return onMoveEnd(p1,p2);
	}
	
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
	
	public static interface OnTouchToMove{
		
		abstract public void onMoveToLeft(View p1,MotionEvent p2,float dx);
		
		abstract public void onMoveToRight(View p1,MotionEvent p2,float dx)
		
		abstract public void onMoveToTop(View p1,MotionEvent p2,float dy);
		
		abstract public void onMoveToDown(View p1,MotionEvent p2,float dy)
		
		abstract public boolean onMoveEnd(View p1,MotionEvent p2)

		abstract public boolean onTouch(View p1, MotionEvent p2)
		
	}

}

/*
public abstract class OnTouchToMove implements OnTouchListener
{

	abstract public void onMoveToLeft(View p1,MotionEvent p2,float dx);
	abstract public void onMoveToRight(View p1,MotionEvent p2,float dx)
	abstract public void onMoveToTop(View p1,MotionEvent p2,float dy);
	abstract public void onMoveToDown(View p1,MotionEvent p2,float dy)
	abstract public boolean onMoveEnd(View p1,MotionEvent p2)
	
	@Override
	public boolean onTouch(View p1, MotionEvent p2)
	{
		if(p2.getHistorySize()>0){
		float lastX= p2.getHistoricalX(0, p2.getHistorySize() - 1);
		float nowX= p2.getX();
		float lastY= p2.getHistoricalY(0, p2.getHistorySize() - 1);
		float nowY= p2.getY();
		
		if(lastX>nowX)
			onMoveToLeft(p1,p2,lastX-nowX);
		else
			onMoveToRight(p1,p2,nowX-lastX);
		
		if(lastY>nowY)
			onMoveToTop(p1,p2,lastY-nowY);
		else
			onMoveToDown(p1,p2,nowY-lastY);
		}
		return onMoveEnd(p1,p2);
	}
	
}
*/
