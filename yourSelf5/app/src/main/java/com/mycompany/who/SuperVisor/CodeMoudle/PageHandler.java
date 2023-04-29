package com.mycompany.who.SuperVisor.CodeMoudle;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View3.*;
import java.util.*;
import java.util.concurrent.*;


public class PageHandler extends PageList implements EditGroup.requestWithEditGroup
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
		setScroll();
	}

	public void addEdit(String name){
		EditGroup Group = new EditGroup(getContext());
		Group.config();
		Group.setPool(pool);
		Group.AddEdit(name);
		Group.setTarget(this);
		addView(Group,name);
	   // requestDisallowInterceptTouchEvent(true);
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
		getConfig().ConfigSelf(this);
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
	@Override
	public EditGroup.EditFactory getEditFactory()
	{
		return null;
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
	@Override
	public void setEditFactory(EditGroup.EditFactory factory){}
	

	@Override
	public boolean onTouchEvent(MotionEvent p2)
	{
		super.onTouchEvent(p2);
		View v = getChildAt(getNowIndex());
		return ViewBuilder.onPageTouch(v,p2,this);
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
		public void ConfigSelf(PageHandler target)
		{
			onChange(target,0);
			super.ConfigSelf(target);
		}

		@Override
		public void onChange(PageHandler target, int src)
		{
			trim(target,width,height);
			super.onChange(target, src);
		}
		
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
	public static interface ViewBuiler extends OnTouchListener,OnKeyListener{
		
		public void eatView(View v, String name, PageHandler self)
		
		public boolean onPageTouch(View p1, MotionEvent p2, PageHandler self)
		
		public boolean onPageKey(int keyCode,KeyEvent p2, PageHandler self)
		
	}
	final static class Domean implements ViewBuiler
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
			Config_Size2 config = (HasAll.Config_Size2)(self. getConfig());
			
			if(v instanceof ImageView)
				eatImageView((ImageView)v,name);
			if(v instanceof EditGroup){
				((EditGroup)v).loadSize(config.width,config.height,config.flag);
				eatEditGroup((EditGroup)v,name);	
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
			
			CodeEdit.EditChroot root = new CodeEdit.EditChroot(true,true,true,true,true);
			builder.compareChroot(root);
			builder.setText(text);
			root.set(false,false,false,false,false);
			builder.compareChroot(root);
			
			Group.getEditLine().reLines(builder.calaEditLines());
			Group.trimToFather();
			
			for(CodeEdit E:Group.getEditList()){
				int end = E.getText().length();
				E.Format(0,end);
				end = E.getText().length();
				E.getPool().execute(E.ReDraw(0,end));
			}
			
		}
	}

	/*
	  我的外部类啊，请帮我实现主要功能吧
	*/
	public static interface IneedBuilder{
		
		public ViewBuiler getViewBuilder()
		
		public void setViewBuilder(ViewBuiler b)
	}
	
	public static interface requestWithPageHandler extends CodeEdit.IlovePool{
		
		public void addEdit(String name)
		
		public void addView(View v, String name)
		
		public PageHandler getPages()
		 
	}
	
}
