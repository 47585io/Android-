package com.mycompany.who.SuperVisor.CodeMoudle.Base.View;
import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;
import java.util.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View.Share.*;


/*
  如之前，我的父元素为HScrollBar，若我能接受到事件，则HScrollBar一定没有达到边界并继续外滑，或滑动距离未达到
  
  那如果纵向滑动距离达到了，请求父元素不要拦截我并返回true，这样父元素之后一定会调用我
  
  否则父元素可以继续拦截
  
*/
public class ScrollBar extends ScrollView implements Scroll
{

	Stack<Integer> historyL;
	Stack<Integer> historyN;
	public boolean canSave=true;
	public boolean canScroll=true;
	public boolean inter = true;
	private boolean m = false;
	
	public static final int Top = 0;
	public static final int Bottom = 1;
	public static final int DontKonw = -1;

	public ScrollBar(Context cont)
	{
		super(cont);
		init();
	}
	public ScrollBar(Context cont, AttributeSet attrs)
	{
		super(cont, attrs);
		init();
	}
	public void init()
	{
		historyL = new Stack<>();
		historyN = new Stack<>();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		if (inter)
		{
			if(ev.getPointerCount()>1)
				m = true;
			//手指太多无法滚动，交给子元素
			
			int pos = isScrollToEdge();
			float y = OnTouchToMove.MoveY(ev);
			if ((pos == Top && y > 10) || (pos == Bottom && y < -10)){
				getParent().requestDisallowInterceptTouchEvent(false);
				return false;
			}
			else{
				getParent().requestDisallowInterceptTouchEvent(true);
			}
			//滚动条滚动到边缘后仍向外划动，请求父元素拦截滚动，自己return false，否则自己滚动
		}
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		if(m)
			return false;
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		if (ev.getAction() == MotionEvent.ACTION_UP && canSave)
			historyL.push(getScrollY());
		if (canScroll)
		{
		    return super.onTouchEvent(ev);
		}
		return false;
	}

	@Override
	public void setScrollY(int value)
	{
		if (!canScroll)
			return;
		if (canSave)
		    historyL.push(value);
		super.setScrollY(value);
	}

	public void setCanScroll(boolean can)
	{
		canScroll = can;
	}
	public void setCanSave(boolean can)
	{
		canSave = can;
	}
	public void goback()
	{
		if (!canScroll || historyL.size() == 0)
			return;
		canSave = false;
		historyN.push(historyL.peek());
		setScrollY(historyL.pop());
		canSave = true;
	}
	public void gonext()
	{
		if (!canScroll || historyN.size() == 0)
			return;
		canSave = false;
		historyL.push(historyN.peek());
		setScrollY(historyN.pop());
		canSave = true;
	}

	@Override
	public int size()
	{
		return historyL.size();
	}

	/*
	 getWidth()和getHeight()获取的是自己在父View中的可见大小，但自己内部的画布可以无限延伸和滚动
	 */
	@Override
	public int isScrollToEdge()
	{
		int height,bottom,y;
		y = getScrollY();
		if (y == 0)
			return Top;
		height = getHeight();
		bottom = getChildAt(0).getBottom();
		if (y + height >= bottom)
			return Bottom;
		return DontKonw;
	}

}
