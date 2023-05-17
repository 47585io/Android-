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
public class TopListView extends ListView
{
	
	public TopListView(Context cont){
		super(cont);
	}
	public TopListView(Context cont,AttributeSet attrs){
		super(cont,attrs);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		//只要我出现，父元素一定不能拦截我，我必然返回true，则之后的子元素没有机会了，我便是顶级窗口
		getParent(). requestDisallowInterceptTouchEvent(true);
		return super.dispatchTouchEvent(ev);
	}
	
}
