package com.mycompany.who.SuperVisor.CodeMoudle.Base.View2;
import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;
import java.util.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share.*;


/*
 如之前，我的父元素为HScrollBar，若我能接受到事件，则HScrollBar一定没有达到边界且没有继续外滑，或滑动距离未达到

 那如果纵向滑动距离达到了，请求父元素不要拦截我并返回true，这样父元素之后一定会调用我

 否则父元素可以继续拦截

 */
public class ScrollBar extends ScrollView implements Scroll
{
	
	private Stack<Integer> historyL;
	private Stack<Integer> historyN;
	private OnTouchToMove mtouch;

	private boolean canSave = true;
	private boolean canScroll = true;
	private boolean inter = false;

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
		mtouch = new NoThingScroll();
	}

	/*  只保证dispatchTouchEvent必然被调用，而其它的可能不会调用 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		if (inter)
		{
			int pos = isScrollToEdge();
			mtouch.calc(ev);
			float x = mtouch.MoveX(ev);
			float y = mtouch.MoveY(ev);
			mtouch.save(ev);
			if (Math.abs(x) < Math.abs(y) 
				&& ((pos == Top && y > 15) || (pos == Bottom && y < -15))){
				getParent().requestDisallowInterceptTouchEvent(false);    
			}
			else{
				getParent().requestDisallowInterceptTouchEvent(true);
			}
			//手指倾向于y轴滑动，且滚动条滚动到边缘后仍向外划动，且速度超出15，请求父元素拦截滚动，否则自己滚动
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		if (ev.getAction() == MotionEvent.ACTION_DOWN && canSave){
			historyL.push(getScrollY());
		}
		if (canScroll){
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

	public void setCanScroll(boolean can){
		canScroll = can;
	}
	public void setCanSave(boolean can){
		canSave = can;
	}
	@Override
	public void setTouchInter(boolean can){
		inter = can;
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
	public int size(){
		return historyL.size();
	}

	/*
	 getWidth()和getHeight()获取的是自己在父View中的可见大小，但自己内部的画布可以无限延伸和滚动
	 */
	@Override
	public int isScrollToEdge()
	{
		int y = getScrollY();
		if (y == 0)
			return Top;
		
		int height,bottom;
		height = getHeight();
		bottom = getChildAt(0).getBottom();
		if (y + height >= bottom)
			return Bottom;
			
		return DontKonw;
	}

}
