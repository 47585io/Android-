package com.mycompany.who.SuperVisor.CodeMoudle.Base.View2;
import android.content.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import android.util.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share.*;
import android.graphics.*;


/*
 如之前，在PageList中，我作为它的子元素，检测是否滑动到边界，并作出相应判断，一个更有趣的问题是，我的子元素是ScrollBar

 因此在滑动时，判断一下横向滑动距离是否达到要求，达到要求了就拦截事件并请求父元素不拦截，否则先给子元素判断，而super.dispatchTouchEvent中就做了这个事

 如果我让父元素拦截并直接返回false，我的子元素也接收不到事件，如果我滚动，我的子元素也接收不到事件，只有我不直接返回且不拦截，这时才能让子元素享受事件

 */
public class HScrollBar extends HorizontalScrollView implements Scroll
{	
	private Stack<Integer> historyL;
	private Stack<Integer> historyN;
	private OnTouchToMove mtouch;
	
	private boolean canSave=true;
	private boolean canScroll=true;
	private boolean inter = false;
	
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
		mtouch = new NoThingScroll();
	}

	/* 只保证dispatchTouchEvent必然被调用，而其它的可能不会调用 */
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
			if (Math.abs(x) > Math.abs(y) 
				&& ((pos == Left && x > 15) || (pos == Right && x < -15))){
				getParent().requestDisallowInterceptTouchEvent(false);
			}
			else{
				getParent().requestDisallowInterceptTouchEvent(true);
			}
			//手指倾向于x轴滑动，滚动条滚动到边缘后仍向外划动，且当前速度超出15，请求父元素拦截，否则自己滚动或给子元素滚动
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		if(!canScroll){
			return false;
			//如果不可滚动，则不拦截事件，给子元素
		}
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		if (canScroll){
			if (ev.getAction() == MotionEvent.ACTION_DOWN && canSave){
				historyL.push(getScrollX());
				//记录起始时的位置
			}
		    return super.onTouchEvent(ev);
		}
		return false;
		//不可滚动，则不消耗事件
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
	public void setCanScroll(boolean can){
		canScroll = can;
	}
    @Override
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
	public int size(){
		return historyL.size();
	}

	@Override
	public int isScrollToEdge()
	{
		int x = getScrollX();
		if (x == 0)
			return Left;
			
		int width,right;
		width = getWidth();
		right = getChildAt(0).getRight();
		if (x + width >= right)
			return Right;
			
		return DontKonw;
	}

}
