package com.mycompany.who.SuperVisor.CodeMoudle.Base.View2;
import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.Share.Share2.*;


/*
   顶级窗口 

   只要我出现，父元素一定不能拦截我，我必然返回true，则之后的子元素没有机会了，我便是顶级窗口
   
*/
public class QuickListView extends ListView
{
	
	public QuickListView(Context cont){
		super(cont);
	}
	public QuickListView(Context cont,AttributeSet attrs){
		super(cont,attrs);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt)
	{
		notifyDataSetChanged(this,getAdapter());
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		//只要我出现，父元素一定不能拦截我，我必然返回true，则之后的子元素没有机会了，我便是顶级窗口
		getParent(). requestDisallowInterceptTouchEvent(true);
		return super.dispatchTouchEvent(ev);
	}
	
	public static void notifyDataSetChanged(ListView listView,ListAdapter adpter) {
		/**第一个可见的位置**/
		int firstVisiblePosition = listView.getFirstVisiblePosition();
		/**最后一个可见的位置**/
		int lastVisiblePosition = listView.getLastVisiblePosition();
		
		for(int position=firstVisiblePosition;position<lastVisiblePosition;++position){
			/**获取指定位置view对象**/
			View view = listView.getChildAt(position - firstVisiblePosition);
			adpter.getView(position, view, listView);
			//用adpter中postion位置的元素刷新listview中的项
		}
	}
	
	public static void notifyDataSetChanged(ListView listView, int position,ListAdapter adpter) {
		/**第一个可见的位置**/
		int firstVisiblePosition = listView.getFirstVisiblePosition();
		/**最后一个可见的位置**/
		int lastVisiblePosition = listView.getLastVisiblePosition();

		/**在看见范围内才更新，不可见的滑动后自动会调用getView方法更新**/
		if (position >= firstVisiblePosition && position <= lastVisiblePosition) {
			/**获取指定位置view对象**/
			View view = listView.getChildAt(position - firstVisiblePosition);
			adpter.getView(position, view, listView);
			//用adpter中postion位置的元素刷新listview中的项
		}
	}
	
}
