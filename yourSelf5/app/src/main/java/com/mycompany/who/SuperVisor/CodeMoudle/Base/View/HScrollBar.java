package com.mycompany.who.SuperVisor.CodeMoudle.Base.View;
import android.content.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import android.util.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View.Share.*;


/*
  如之前，在PageHandler中，我作为它的子元素，检测是否滑动到边界，并作出相应判断，一个更有趣的问题是，我的子元素是ScrollBar

  因此在滑动时，判断一下横向滑动距离是否达到要求，达到要求了就拦截事件并请求父元素不拦截，否则先给子元素判断，而super.dispatchTouchEvent中就做了这个事
  
  如果我让父元素拦截并直接返回false，我的子元素也接收不到事件，如果我滚动，我的子元素也接收不到事件，只有我不直接返回且不拦截，这时才能让子元素享受事件
  
*/
public class HScrollBar extends HorizontalScrollView implements Scroll
{

	Stack<Integer> historyL;
	Stack<Integer> historyN;
	public boolean canSave=true;
	public boolean canScroll=true;
	public boolean inter = true;
	private boolean m = false;

	public static final int Left = 0;
	public static final int Right = 1;
	public static final int DontKonw = -1;

	public HScrollBar(Context cont)
	{
		super(cont);
		init();
	}
	public HScrollBar(Context cont, AttributeSet attrs)
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
			float x = OnTouchToMove.MoveX(ev);
			if ((pos == Left && x > 10) || (pos == Right && x < -10)){
				getParent().requestDisallowInterceptTouchEvent(false);
				return false;
			}
			else{
				getParent().requestDisallowInterceptTouchEvent(true);
			}
			//滚动条滚动到边缘后仍向外划动，请求父元素拦截滚动，自己return false，否则自己滚动或给子元素滚动
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
			historyL.push(getScrollX());
		if (canScroll)
		{
		    return super.onTouchEvent(ev);
		}
		return false;
	}

	@Override
	public void setScrollX(int value)
	{
		if (!canScroll)
			return;
		if (canSave)
		    historyL.push(value);
		super.setScrollX(value);
	}

	@Override
	public void setCanScroll(boolean can)
	{
		canScroll = can;
	}

	@Override
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
		setScrollX(historyL.pop());
		canSave = true;
	}
	public void gonext()
	{
		if (!canScroll || historyN.size() == 0)
			return;
		canSave = false;
		historyL.push(historyN.peek());
		setScrollX(historyN.pop());
		canSave = true;
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
		if (x == 0)
			return Left;
		width = getWidth();
		right = getChildAt(0).getRight();
		if (x + width >= right)
			return Right;
		return DontKonw;
	}

}
