package com.mycompany.who.View;

import android.content.*;
import android.text.method.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.View.*;
import java.util.*;
import java.util.concurrent.*;
import com.mycompany.who.R;
import com.mycompany.who.Edit.Share.*;
import com.mycompany.who.Share.*;

public class PageList extends LinearLayout
{
	private static int noRepeatId=-1;
	private int nowIndex;
	private onTabPag mtabListener;
	private List<View> mPages;
	
	public PageList(Context cont)
	{
		super(cont);
		mPages=new ArrayList<>();
		//setOnTouchListener(new OnTouchToMoveSelf());
	}

	public void addView(View EditPage)
	{
		//添加一个命名的编辑器
		int index = contrans(EditPage);
		if (index != -1)
			return ;
			//如果是同一个文件，不重复加入
		
		if(mtabListener!=null)
			mtabListener.onAddPage(EditPage);
		EditPage.setId(++noRepeatId);	
		super. addView(EditPage);
		mPages.add(EditPage);
	}
	public void tabView(int index)
	{
		if (index>getChildCount()-1 || index < 0)
		{
			return;
		}
		nowIndex=index;
		if(mtabListener!=null)
			mtabListener.onTabPage(index);
		//异常情况下，什么也不做
		//否则把编辑器切换
		
		removeAllViews();
		super. addView(mPages.get(index));
	}
	public void removeViewAt(int index)
	{
		if(mtabListener!=null)
			mtabListener.onDelPage(index);
		removeViewAt(index);
		mPages.remove(index);
		if(nowIndex==index)
			tabView(index-1);
	}

	public int contrans(View Page)
	{
		int index;
		for (index = 0;index < mPages.size();index++)
		{
			View p =  getChildAt(index);
			if (p.getId()==Page.getId()||p.getTag().equals(Page.getTag()))
			{
			    return index;
			}
		}
		return -1;
	}

	public void setListener(onTabPag li){
		mtabListener=li;
	}
	public int getNowIndex()
	{
		//获取当前选中的编辑器下标
		return nowIndex;
	}
	public int getNoRepeatId()
	{
		//获取当前的编辑器id
		return noRepeatId;
	}
	public View getView(int index){
		return mPages.get(index);
	}
	
	class OnTouchToMoveSelf extends OnTouchToMove
	{

		@Override
		public void onMoveToLeft(View p1, MotionEvent p2, float dx)
		{
			if(getOrientation()==LinearLayout.HORIZONTAL&&getX()>0)
				setX(getX()-dx);		
		}

		@Override
		public void onMoveToRight(View p1, MotionEvent p2, float dx)
		{
			if(getOrientation()==LinearLayout.HORIZONTAL&&getX()<getWidth())
				setX(getX()+dx);	
		}

		@Override
		public void onMoveToTop(View p1, MotionEvent p2, float dy)
		{
			
		}

		@Override
		public void onMoveToDown(View p1, MotionEvent p2, float dy)
		{
			// TODO: Implement this method
		}

		@Override
		public boolean onMoveEnd(View p1, MotionEvent p2)
		{
			
			return true;
		}
		
	}
	
	public static interface onTabPag{
		public void onTabPage(int index);
		public void onAddPage(View v);
		public void onDelPage(int index);
	}


}
