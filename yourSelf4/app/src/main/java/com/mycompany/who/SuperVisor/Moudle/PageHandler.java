package com.mycompany.who.SuperVisor.Moudle;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.mycompany.who.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import com.mycompany.who.Edit.Share.Share1.*;
import com.mycompany.who.Edit.Share.Share2.*;
import com.mycompany.who.SuperVisor.Moudle.Config.*;
import com.mycompany.who.View.*;
import java.util.concurrent.*;
import java.util.*;
import android.net.*;
import com.mycompany.who.Share.*;
import com.mycompany.who.SuperVisor.Moudle.Share.*;


public class PageHandler extends PageList implements CodeEdit.IlovePool
{
	private ThreadPoolExecutor pool;
	private ViewBuiler ViewBuilder;
	private List<Object> PageState;
	
	public PageHandler(Context cont){
		super(cont);	
	}	
	public PageHandler(Context cont,AttributeSet set){
		super(cont,set);
	}
	@Override
	public void init()
	{
		super.init();
		Creator = new HandlerCreator(0);
		Creator.ConfigSelf(this);
	}

	public void addEdit(String name){
		EditGroup Group = new EditGroup(getContext());
		Group.config();
		Group.setPool(pool);
		Group.AddEdit(name);
		Group.setTarget(this);
		addView(Group,name);
	}
	
	@Override
	public boolean addView(View v, String name){
		if(super.addView(v,name)){
		    if(ViewBuilder!=null)
			    ViewBuilder.eatView(v,name,this);
			return true;
		}
		return false;
	}
	@Override
	public void tabView(int index)
	{
		super.tabView(index);
		View v=getView(index);
		ViewGroup.LayoutParams pa = v.getLayoutParams();
		Config_Size2. trim(this,pa.width,pa.height);
		//v.requestFocus();
	}
	@Override
	public void removeViewAt(int index)
	{
		super.removeViewAt(index);
	}

	
	public ThreadPoolExecutor getPool()
	{
		return pool;
	}
	public ViewBuiler getViewBuilder()
	{
		return ViewBuilder;
	}
	public void setPool(ThreadPoolExecutor pool){
		this.pool = pool;
	}
	public void setViewBuilder(ViewBuiler b){
		ViewBuilder = b;
	}
	

	//已经到达了最后吗？
	@Override
	public boolean BubbleMotionEvent(MotionEvent p2)
	{
		return super.BubbleMotionEvent(p2);
	}	
	@Override
	public boolean onTouchEvent(MotionEvent p2)
	{
		super.onTouchEvent(p2);
		View v = getView(getNowIndex());
		return ViewBuilder.onPageTouch(v,p2,this);
	}
	
	@Override
	public boolean BubbleKeyEvent(int keyCode, KeyEvent event)
	{
		return super.BubbleKeyEvent(keyCode, event);
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		super.onKeyUp(keyCode,event);
		return ViewBuilder.onPageKey(keyCode,event,this);
	}
	
	
	final public static class Config_hesSize extends Config_Size2<PageHandler>
	{

		@Override
		public void onChange(PageHandler target, int src)
		{
			trim(target,width,height);
			super.onChange(target, src);
		}
		
	}
	@Override
	protected void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	    getConfig().change(this,newConfig.orientation);
	}
	
	
	//一顿操作后，PageHandler所有成员都分配好了空间
	final class HandlerCreator extends Creator<PageHandler>
	{
		
		public HandlerCreator(int i){
			super(i);
		}

		public void init(PageHandler target,View tmp)
		{
			target.config = new Config_hesSize();
			target.ViewBuilder = new Domean();
			target.PageState = new ArrayList<>();
		}

	}
	
	
	/*  在View被加入页面时，可以进行额外配置  */
	public static interface ViewBuiler{
		
		public void eatView(View v, String name, PageHandler self)
		
		public boolean onPageTouch(View p1, MotionEvent p2, PageHandler self)
		
		public boolean onPageKey(int keyCode,KeyEvent p2, PageHandler self)
		
	}
	final static class Domean implements ViewBuiler,OnTouchListener,OnKeyListener
	{

		@Override
		public boolean onPageKey(int keyCode, KeyEvent p2, PageHandler self)
		{
			return false;
		}

		@Override
		public boolean onKey(View p1, int p2, KeyEvent p3)
		{
			return false;
		}

		@Override
		public boolean onTouch(View p1, MotionEvent p2)
		{
			return true;
		}
		
		@Override
		public boolean onPageTouch(View p1, MotionEvent p2, PageHandler self)
		{
			return true;
		}
		
		@Override
		public void eatView(View v, String name,PageHandler self)
		{
			if(!(v instanceof OnTouchListener)) //View已经实现OnTouchListener，不用我啦
			    v. setOnTouchListener(this);
			Config_Size2 config = (HasAll.Config_Size2)(self. getConfig());
			
			if(v instanceof ImageView)
				eatImageView((ImageView)v,name);
			if(v instanceof EditGroup){
				eatEditGroup((EditGroup)v,name);
				((EditGroup)v).loadSize(config.width,config.height,config.flag);
			}
		}
		
		public void eatImageView(ImageView v,String name){
			Bitmap map = BitmapFactory.decodeFile(name);
			v.setImageBitmap(map);
		}
		public void eatEditGroup(EditGroup Group,String name){
			
			myRet ret = new myRet(name);
			String text = ret.r("UTF-8");
			EditGroup.EditBuilder builder = Group.getEditBuilder();
			
			CodeEdit.EditChroot root = new CodeEdit.EditChroot(false,true,true,true,true);
			builder.compareChroot(root);
			builder.setText(text);
			root.set(false,false,false,false,false);
			builder.compareChroot(root);
			
			builder.Format(0,text.length());
			builder.reDraw(0,builder.calaEditLen());
			
		}
	}

	/*
	  我的外部类啊，请帮我实现主要功能吧
	*/
	public static interface IneedBuilder{
		
		public ViewBuiler getViewBuilder()
		
		public void setViewBuilder(ViewBuiler b)
	}
	
	public static interface requestWithPageHandler{
		
		public void addEdit(String name)
		
		public void addView(View v, String name)
		
		public PageHandler getPages()
		 
	}
	
}
