package com.mycompany.who.View;
import android.widget.*;
import android.content.*;
import android.view.*;
import java.util.*;

public class ScrollBar extends ScrollView
{
	
	Stack<Integer> historyL;
	Stack<Integer> historyN;
	boolean canSave=true;
	
	public ScrollBar(Context cont){
		super(cont);
		historyL=new Stack<>();
		historyN=new Stack<>();
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		if(ev.getAction()==MotionEvent.ACTION_UP||ev.getAction()==MotionEvent.ACTION_DOWN&&canSave)
			historyL.push(getScrollX());
		return super.onTouchEvent(ev);
	}

	@Override
	public void setScrollX(int value)
	{
		if(canSave)
		    historyL.push(value);
		super.setScrollX(value);
	}
	
	public void goback(){
		canSave=false;
		historyN.push(historyL.peek());
		setScrollX(historyL.pop());
		canSave=true;
	}
	public void gonext(){
		canSave=false;
		historyL.push(historyN.peek());
		setScrollX(historyN.pop());
		canSave=true;
	}
	
}
