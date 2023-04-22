package com.mycompany.who.SuperVisor.CodeMoudle.Base.View;

import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.Share.Share2.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View.Share.*;
import java.util.*;

public class PageList extends HasAll
{
	private int nowIndex;
	private onTabPage mtabListener;
	private OnTouchListener mtouchListener;
	private List<View> mPages;
	
	public PageList(Context cont)
	{
		super(cont);
		mPages=new ArrayList<>();
	}
	public PageList(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPages=new ArrayList<>();
		//setScroll();
	}

	public boolean addView(View EditPage,String name)
	{
		//添加一个命名的编辑器
		int index = contrans(EditPage);
		if (index != -1)
			return false;
			//如果是同一个文件，不重复加入
		EditPage.setId(EditPage.hashCode());	
		EditPage.setTag(name);
		super. addView(EditPage);
		mPages.add(EditPage);
		
		if(mtabListener!=null)
			mtabListener.onAddPage(EditPage,name);
		tabView(mPages.size()-1);
			
		return true;
	}
	public void tabView(int index)
	{
		if (index>mPages.size()-1 || index < 0)
		{
			return;
		}
		
		if(mtabListener!=null)
			mtabListener.onTabPage(index);
		//异常情况下，什么也不做
		//否则把编辑器切换
		nowIndex=index;
		gotoChild(index);
	}
	public void removeViewAt(int index)
	{
		if(mtabListener!=null)
			mtabListener.onDelPage(index);
		super. removeViewAt(index);
		mPages.remove(index);
		if(nowIndex==index)
			tabView(index-1);
	}

	public int contrans(View Page)
	{
		int index;
		for (index = 0;index < mPages.size();index++)
		{
			View p =  mPages.get(index);
			if (p.getId()==Page.getId()||p.getTag().equals(Page.getTag()))
			{
			    return index;
			}
		}
		return -1;
	}

	public void setonTabListener(onTabPage li){
		mtabListener=li;
	}
	public int getNowIndex()
	{
		//获取当前选中的编辑器下标
		return nowIndex;
	}
	public View getView(int index){
		return mPages.get(index);
	}
	protected void gotoChild(int index){
		View v = getChildAt(index);
		int x = v.getLeft();
		int y = v.getTop();
		AnimatorColletion.getScroll(this,getScrollX(),getScrollY(),x,y).start();
		//scrollTo(left,top);
	}
	protected int fromPosGetChild(int x,int y){
		for(int i=0;i<getChildCount();++i){
			View v = getChildAt(i);
		    int vl=v.getLeft(),vr=v.getRight(),vt=v.getTop(),vb=v.getBottom();
			if(x>=vl&&x<=vr&&y>=vt&&y<=vb)
				return i;
		}
		return -1;
	}
	
	class PageTouch extends OnTouchToMove
	{

		@Override
		public void onMoveToLeft(View p1, MotionEvent p2, float dx)
		{
			if(((LinearLayout)p1).getOrientation()==LinearLayout.HORIZONTAL){
			    int x = p1.getScrollX()+(int)dx;
				
				p1.scrollBy((int)dx,0);
			}
		}

		@Override
		public void onMoveToRight(View p1, MotionEvent p2, float dx)
		{
			if(((LinearLayout)p1).getOrientation()==LinearLayout.HORIZONTAL){
				int x = p1.getScrollX();
				x = x-(int)dx<0 ? -x:-(int)dx;
			    p1.scrollBy(x,0);
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
			    int y = p1.getScrollY();
				y = y-(int)dy<0 ? -y:-(int)dy;
				p1.scrollBy(0,y);
			}
		}

		@Override
		public boolean onMoveEnd(View p1, MotionEvent p2)
		{
			if(p2.getActionMasked()==MotionEvent.ACTION_UP){
				int x,y,tx,ty;
				x = p1.getScrollX();
				y = p1.getScrollY();
				PageList p = (PageList) p1;
				int index= p.fromPosGetChild(x,y);
				View v = p.getView(index);
				tx = v.getRight();
				ty = v.getBottom();
				
				int w=p.getWidth(),h=p.getHeight();
				if((getOrientation()==HORIZONTAL&& tx-x>w*0.5)||(getOrientation()==VERTICAL&& ty-y>h*0.5))
					;
				else
					index+=1;
				if(p.mtabListener!=null)
					p.mtabListener.onTabPage(index);
				gotoChild(index);
			}
			return true;
		}
	}
	public void setScroll(){
		mtouchListener=new PageTouch();
	}

	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		super.onTouchEvent(event);
		if(mtouchListener!=null)
		    return mtouchListener.onTouch(this,event);
		return false;
	}
	
	
	public static interface onTabPage{
		
		public void onTabPage(int index);
		
		public void onAddPage(View v,String name);
		
		public void onDelPage(int index);
		
	}
	
	public void toList(List<Icon> list){
		list.clear();
		for(View v:mPages){
			String name = (String) v.getTag();
			Icon icon = new Icon3(Share.getFileIcon(name),name);
			list.add(icon);
		}
	}

}
