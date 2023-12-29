package com.editor.view;
import android.view.*;
import android.content.*;
import android.util.*;
import com.editor.view.Share.*;


public class PageHandler extends ViewGroup
{

	private int mNowIndex;
	private PageListener mPageListener;
	
	public PageHandler(Context cont){
		super(cont);
	}
	public PageHandler(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		int y = 0;
		int childCount = getChildCount();
		for(int i=0;i<childCount;++i)
		{
			View v = getChildAt(i);
			int width = v.getMeasuredWidth();
			int height = v.getMeasuredHeight();
			v.layout(y, 0, y+=width, height>b-t ? b-t:height);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		return super.onInterceptTouchEvent(ev);
	}
	
	/* 添加一个命名的页面 */
	public boolean addView(View EditPage,Object name)
	{
		int index = contrans(EditPage);
		if (index != -1){
			//如果是同一个页面，不重复加入
			return false;
		}
			
	    addView(EditPage);
		EditPage.setTag(name);
		if(mPageListener!=null){
			mPageListener.onAddPage(EditPage,name);
		}
		//加入页面后就切换过来
		tabView(getChildCount()-1);
		return true;
	}

	/* 切换页面 */
	public void tabView(int index)
	{
		if (index>getChildCount()-1 || index < 0){
			return;
		}
        //异常情况下，什么也不做，否则把页面切换
		if(mPageListener!=null){
			mPageListener.onTabPage(index);
		}

		mNowIndex=index;
		gotoChild(index);
	}

	/* 删除页面 */
	public void removeViewAt(int index)
	{
		if(mPageListener!=null){
			mPageListener.onDelPage(index);
		}
		super.removeViewAt(index);
		if(mNowIndex==index){
			//如果删除了当前页面，切换到上页
			tabView(index-1);
		}
	}

	/* 是否已存在一个页面 */
	public int contrans(View Page)
	{
		int index;
		for (index = 0;index < getChildCount();index++)
		{
			View p = getChildAt(index);
			if (p.getTag().equals(Page.getTag())){
			    return index;
			}
		}
		return -1;
	}
	
	/* 滚动画布到指定孑View位置 */
	protected void gotoChild(int index)
	{
		if(index>=getChildCount()){
			return;
		}
		View v = getChildAt(index);
		int x = v.getLeft();
		int y = v.getTop();
		AnimationCollection.getScroll(this,getScrollX(),getScrollY(),x,y).start();
	}

	/* 通过坐标获取子元素 */
	protected int fromPosGetChild(int x,int y)
	{
		for(int i=0;i<getChildCount();++i)
		{
			View v = getChildAt(i);
		    int vl=v.getLeft(),vr=v.getRight(),vt=v.getTop(),vb=v.getBottom();
			if(x>=vl&&x<=vr&&y>=vt&&y<=vb){
				return i;
			}
		}
		return -1;
	}

	/* 设置页面切换监听器 */
	public void setonTabListener(PageListener li){
		mPageListener=li;
	}
	
	/* 获取当前选中的View下标 */
	public int getNowIndex(){
		return mNowIndex;
	}

	public static interface PageListener{

		public void onTabPage(int index);

		public void onAddPage(View v,Object name);

		public void onDelPage(int index);

	}
}
