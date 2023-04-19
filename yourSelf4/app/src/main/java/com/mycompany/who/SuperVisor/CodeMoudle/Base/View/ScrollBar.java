package com.mycompany.who.SuperVisor.CodeMoudle.Base.View;
import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;
import java.util.*;

public class ScrollBar extends ScrollView implements Scroll
{

	Stack<Integer> historyL;
	Stack<Integer> historyN;
	boolean canSave=true;
	public boolean canScroll=true;
	
	public ScrollBar(Context cont){
		super(cont);
		init();
	}
	public ScrollBar(Context cont,AttributeSet attrs){
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
			historyL.push(getScrollY());
		if(canScroll){
		    return super.onTouchEvent(ev);
		}
		return false;
	}

	@Override
	public void setScrollY(int value)
	{
		if(!canScroll)
			return;
		if(canSave)
		    historyL.push(value);
		super.setScrollY(value);
	}
	
	public void setCanScroll(boolean can){
		canScroll=can;
	}
	public void setCanSave(boolean can){
		canSave=can;
	}
	public void goback(){
		if(!canScroll||historyL.size()==0)
			return;
		canSave=false;
		historyN.push(historyL.peek());
		setScrollY(historyL.pop());
		canSave=true;
	}
	public void gonext(){
		if(!canScroll||historyN.size()==0)
			return;
		canSave=false;
		historyL.push(historyN.peek());
		setScrollY(historyN.pop());
		canSave=true;
	}

	@Override
	public int size()
	{
		return historyL.size();
	}

	
}
