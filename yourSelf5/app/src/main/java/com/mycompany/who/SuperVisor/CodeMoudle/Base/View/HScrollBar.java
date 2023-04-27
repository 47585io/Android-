package com.mycompany.who.SuperVisor.CodeMoudle.Base.View;
import android.content.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import android.util.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View.Share.*;

public class HScrollBar extends HorizontalScrollView implements Scroll
{

	Stack<Integer> historyL;
	Stack<Integer> historyN;
	public boolean canSave=true;
	public boolean canScroll=true;
	
	public static final int Left = 0;
	public static final int Right = 1;
	public static final int DontKonw = -1;
	
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
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		if(ev.getAction()==MotionEvent.ACTION_UP&&canSave)
			historyL.push(getScrollX());
		if(canScroll){
		    return super.onTouchEvent(ev);
		}
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

	@Override
	public int isScrollToEdge()
	{
		int width,right,x;
		x = getScrollX();
		if(x==0)
			return Left;
		width = getWidth();
		right = getChildAt(0).getRight();
		if(x+width>=right)
			return Right;
		return DontKonw;
	}
	
}
