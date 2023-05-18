package com.mycompany.who.SuperVisor.CodeMoudle.Base;

import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View3.*;


/*
  支持页面的切换以及滚动切换
  
  若要setScroll，必须保证没有滚动冲突，但可以使用ScrollBar，HScrollBar和QuickListView，它们优化了一些冲突
  
  当Orientation==VERTICAL，应该设置ScrollBar.inter=true，反之设置HScrollBar.inter=true
  
*/

/*
 PageList默认会拦截事件供自己滑动，但ACTION_DOWN时或滚动方向冲突时会给子元素一次机会

 假设子元素是HScrollBar，它检查自己是否滑动到边缘，如果是，返回false，事件就落到了PageList手里

 如果子元素需要滑动，可以请求父元素不要拦截并返回true，这样父元素不会拦截并且不调用自己之后的子元素

 */
public class PageList extends HasAll
{
	
	private int nowIndex;
	private onTabPage mtabListener;
	private OnTouchToMove mtouchListener;
	
	public PageList(Context cont){
		super(cont);
	}
	public PageList(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/* 添加一个命名的页面 */
	public boolean addView(View EditPage,String name)
	{
		EditPage.setTag(name);
		int index = contrans(EditPage);
		if (index != -1)
			return false;
		//如果是同一个页面，不重复加入
		//EditPage.setId(EditPage.hashCode());	
	    addView(EditPage);

		if(mtabListener!=null){
			mtabListener.onAddPage(EditPage,name);
		}
		tabView(getChildCount()-1);
		//加入页面后就切换过来

		return true;
	}
	
	/* 切换页面 */
	public void tabView(int index)
	{
		if (index>getChildCount()-1 || index < 0){
			return;
		}
        //异常情况下，什么也不做，否则把页面切换
		if(mtabListener!=null){
			mtabListener.onTabPage(index);
		}
		
		nowIndex=index;
		gotoChild(index);
	}
	
	/* 删除页面 */
	public void removeViewAt(int index)
	{
		if(mtabListener!=null){
			mtabListener.onDelPage(index);
		}
		super. removeViewAt(index);
		if(nowIndex==index){
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
			if ( p.getTag().equals(Page.getTag())){
			    return index;
			}
		}
		return -1;
	}

	/* 设置页面切换监听器 */
	public void setonTabListener(onTabPage li){
		mtabListener=li;
	}
	
	/* 获取当前选中的编辑器下标 */
	public int getNowIndex(){
		return nowIndex;
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
		AnimatorColletion.getScroll(this,getScrollX(),getScrollY(),x,y).start();
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

/* 
----------------------------------------------------------------------------------------------------------------------------------------------------------------------

  PageList如何感知事件滑动自己
	
  以下PageTouch提供横向和纵向两种方向的滑动和感知

  当横向时，只有onMoveToLeft与onMoveToRight生效。当纵向时，只有onMoveToTop与onMoveToDown生效

  PageList追踪手指滑动方向来判断DownBar向哪个方向慢慢打开，具体过程是:

  保留上次的坐标，与本次坐标相比计算出差距，然后将PageList的画布滑动
	   
  在手指抬起来时，会根据当前已滚动到的位置进行直接页面切换
  
----------------------------------------------------------------------------------------------------------------------------------------------------------------------
*/
	static class PageTouch extends OnTouchToMove
	{

		@Override
		public void onMoveToLeft(View p1, MotionEvent p2, float dx)
		{
			if(((LinearLayout)p1).getOrientation()==LinearLayout.HORIZONTAL){
				//手指向左移动，实际要向右滚动画面
			    p1.scrollBy((int)dx,0);
			}
		}

		@Override
		public void onMoveToRight(View p1, MotionEvent p2, float dx)
		{
			if(((LinearLayout)p1).getOrientation()==LinearLayout.HORIZONTAL){
			    p1.scrollBy((int)-dx,0);
			}
		}

		@Override
		public void onMoveToTop(View p1, MotionEvent p2, float dy)
		{
			if(((LinearLayout)p1).getOrientation()==LinearLayout.VERTICAL){
				p1.scrollBy(0,(int)dy);
			}
		}

		@Override
		public void onMoveToDown(View p1, MotionEvent p2, float dy)
		{
			if(((LinearLayout)p1).getOrientation()==LinearLayout.VERTICAL){
			    p1.scrollBy(0,(int)-dy);
			}
		}

		@Override
		public boolean onMoveEnd(View p1, MotionEvent p2)
		{
			if(p2.getActionMasked()==MotionEvent.ACTION_UP)
			{
				int x,y,tx,ty;
				x = p1.getScrollX();
				y = p1.getScrollY();
				
				PageList p = (PageList) p1;
				int w=p.getWidth(),h=p.getHeight();	
				int index= p.fromPosGetChild(x,y);
				
				if(index<0){
					index=0;
				}
				// null pointer Exception in this
				if(p.getChildCount()==0){
					return false;
				}
				View v = p.getChildAt(index);
				tx = v.getRight();
				ty = v.getBottom();

				if((p.getOrientation()==HORIZONTAL&& tx-x>w*0.5)||(p.getOrientation()==VERTICAL&& ty-y>h*0.5)){
					;
				}
				else if(index<p.getChildCount()-1){
					index+=1;
				}
				p.tabView(index);
				//上一页和下一页之间，哪个占用更多，就滚动到哪个，但上一页必须大于-1，下一页必须小于getChildCount()-1
			}
			return true;
		}
	}
	
	/* 设置滚动监听 */
	public void setScroll(boolean is)
	{
		if(is){
		    mtouchListener = new PageTouch();
		}
		else{
			mtouchListener = null;
		}
		setOnTouchListener(mtouchListener);
	}

	@Override
	public void setOnTouchListener(View.OnTouchListener l)
	{
		if(l instanceof PageTouch){
			//必须是PageTouch，否则无法设置
		    super.setOnTouchListener(l);
		}
	}

	
   /*  PageList滚动页面的拦截方案  */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		if(mtouchListener==null){
			return super.onInterceptTouchEvent(ev);
		}
			
		if(ev.getActionMasked()==MotionEvent.ACTION_DOWN){
		    mtouchListener.calc(ev); //仍应该记录坐标
			return false;
			//ACTION_DOWN时给子元素一次机会
		}
		else{
			mtouchListener.calc(ev); //仍应该记录坐标
			float x = Math.abs(mtouchListener.MoveX());
			float y = Math.abs(mtouchListener.MoveY());
			mtouchListener.save(ev); //仍应该记录坐标
			if((getOrientation()==HORIZONTAL && x>y) || (getOrientation()==VERTICAL && x<y)){
			    return true;
			}
		    return false;
			//根据子元素排列方向决定翻页所需要的事件(手指滑动方向更倾向于某轴)，如果不是我要的，就不拦截
		}
	}
	

	public static interface onTabPage{

		public void onTabPage(int index);

		public void onAddPage(View v,String name);

		public void onDelPage(int index);

	}

}
