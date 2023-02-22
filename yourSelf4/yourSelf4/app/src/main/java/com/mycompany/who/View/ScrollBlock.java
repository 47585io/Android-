package com.mycompany.who.View;
import android.view.*;
import android.content.*;
import android.graphics.*;

public class ScrollBlock extends View
{
	
	float x,y;
	onBlockScrollListener listener;
	Paint pen;
	
	ScrollBlock(Context cont){
		super(cont);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawRect(x,y,x+20,y+20,pen);
		super.onDraw(canvas);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		x=event.getX();
		y=event.getY();
		onDraw(new Canvas());
		float bili = (x/getWidth()+y/getHeight())/2;
		listener.onBlockScroll(bili);
		super.onTouchEvent(event);
		return true;
	}
	
	public void setColor(int color){
		pen.setColor(color);
	}
	abstract static public class onBlockScrollListener 
	{
		abstract public void onBlockScroll(float bili);
	}
}
