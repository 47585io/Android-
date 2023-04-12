package com.mycompany.who.View;
import android.content.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import android.util.*;

public class HScrollBar extends HorizontalScrollView implements Scroll
{
	Stack<Integer> historyL;
	Stack<Integer> historyN;
	boolean canSave=true;
	public boolean canScroll=true;
	
	public HScrollBar(Context cont){
		super(cont);
		init();
	}
	public HScrollBar(Context cont,AttributeSet attrs){
		super(cont,attrs);
		init();
	}
	public void init(){
		historyL=new Stack<>();
		historyN=new Stack<>();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		if(ev.getAction()==MotionEvent.ACTION_UP&&canSave)
			historyL.push(getScrollX());
		if(canScroll)
		    return super.onTouchEvent(ev);
		return false;
	}

	@Override
	public void setScrollX(int value)
	{
		if(!canScroll)
			return;
		if(canSave)
		    historyL.push(value);
		super.setScrollX(value);
	}
	
	@Override
	public void setCanScroll(boolean can)
	{
		canScroll=can;
	}

	@Override
	public void setCanSave(boolean can)
	{
		canSave = can;
	}
	
	public void goback(){
		if(!canScroll||historyL.size()==0)
			return;
		canSave=false;
		historyN.push(historyL.peek());
		setScrollX(historyL.pop());
		canSave=true;
	}
	public void gonext(){
		if(!canScroll||historyN.size()==0)
			return;
		canSave=false;
		historyL.push(historyN.peek());
		setScrollX(historyN.pop());
		canSave=true;
	}
	@Override
	public int size()
	{
		return historyL.size();
	}

}
