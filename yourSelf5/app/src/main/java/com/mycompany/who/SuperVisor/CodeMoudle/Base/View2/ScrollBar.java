package com.mycompany.who.SuperVisor.CodeMoudle.Base.View2;
import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;
import java.util.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share.*;
import android.graphics.*;


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
	private onTouchToZoom mzoom;
	
	private boolean canSave = true;
	private boolean canScroll = true;
	private boolean inter = false;
	private boolean iszoom = false;
	
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
			if(ev.getPointerCount()==2){
				requestDisallowInterceptTouchEvent(false);
				getParent().requestDisallowInterceptTouchEvent(true);
				return super.dispatchTouchEvent(ev);
				//缩放手势，父元素一定不能拦截我，我一定拦截子元素
			}
			
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
			//手指倾向于y轴滑动，且滚动条滚动到边缘后仍向外划动，且速度超出15，请求父元素拦截滚动，否则自己滚动或给子元素
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		if(ev.getPointerCount()==2){
			return true;
			//缩放手势，拦截事件进行缩放
		}
		if(!canScroll){
			return false;
			//如果不可滚动，则不拦截事件，给子元素
		}
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		if(ev.getPointerCount()==2||(ev.getActionMasked()==ev.ACTION_UP&&iszoom))
		{
			iszoom=true;
			if(ev.getActionMasked()==ev.ACTION_UP)
				iszoom=false;
			if(mzoom!=null)
				return mzoom.onTouch(this,ev);
			return true;
			//缩放手势，消耗事件缩放
		}
		if (canScroll){
			if (ev.getAction() == MotionEvent.ACTION_DOWN && canSave){
				historyL.push(getScrollY());
				//记录起始时的位置
			}
		    return super.onTouchEvent(ev);
		}
		return false;
		//不可滚动，则不消耗事件
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
	@Override
	public void setzoomListener(onTouchToZoom zoom){
		mzoom = zoom;
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
