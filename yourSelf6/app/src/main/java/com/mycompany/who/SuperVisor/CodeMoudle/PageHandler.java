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
import android.text.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.*;


/*
  页面管理者
  
  PageHandler可以对页面进行增加，删除，切换，配置
  
  PageHandler可以感知触摸并切换页面
  
*/
public class PageHandler extends PageList implements EditGroup.requestByEditGroup
{
	
	private ThreadPoolExecutor pool;
	private ViewBuiler ViewBuilder;
	private EditGroup.EditFactory mfactory;
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
		setScroll(true); //启用滚动翻页
	}
	
	public ThreadPoolExecutor getPool(){
		return pool;
	}
	@Override
	public EditGroup.EditFactory getEditFactory(){
		return mfactory;
	}
	public ViewBuiler getViewBuilder(){
		return ViewBuilder;
	}

	public void setPool(ThreadPoolExecutor pool){
		this.pool = pool;
	}
	public void setViewBuilder(ViewBuiler b){
		ViewBuilder = b;
	}
	@Override
	public void setEditFactory(EditGroup.EditFactory factory){
		mfactory = factory;
	}

	
/*
-----------------------------------------------------------------------------------

  PageHandler继承了PageList，并拓展了一些功能
  
  无论怎样，都会在添加View时，调用ViewBuilder配置
  
  您可以手动调用requestBuildView配置一个View
  
-----------------------------------------------------------------------------------
*/

	public void addEdit(String name)
	{
		EditGroup Group = new EditGroup(getContext());
		Group.setPool(pool);
		Group.AddEdit(name);
		addView(Group,name);
	}
	
	@Override
	public boolean addView(View v, String name)
	{
		if(super.addView(v,name)){
		    requestBuildView(v,name);
			return true;
		}
		return false;
	}
	
	/* 添加一个带标题的页面 */
	public boolean addATitleView(View v, String name)
	{
		LinearLayout l = new LinearLayout(getContext());
		l.setOrientation(VERTICAL);
		TextView text = new TextView(getContext());
		text.setText(name);
		l.addView(text);
		l.addView(v);
		if(super.addView(l,name)){
		    requestBuildView(v,name);
			return true;
		}
		return false;
	}

	public void requestBuildView(View v, String name)
	{
		if(ViewBuilder!=null){
			ViewBuilder.eatView(v,name,this);
		}
	}
	public void requestBuildView(int index)
	{
		View v = getChildAt(index);
		String name = (String) v.getTag();
		requestBuildView(v,name);
	}
	
/*

-------------------------------------------------------------------
 
  EditGroup对触摸和键事件的处理
  
  没有什么特殊操作，只是如果设置了ViewBuilder会额外分享事件
  
-------------------------------------------------------------------
*/

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
	
/*  
-----------------------------------------------------------------------------------
	 
  ViewBuiler
  
  在View被加入页面时，可以进行额外配置
  
  即使在View享受不到事件时，PageHandler也会传递事件到ViewBuiler
  
-----------------------------------------------------------------------------------
*/
	public static interface ViewBuiler
	{
		
		public void eatView(View v, String name, PageHandler self)
		
		public boolean onPageTouch(View p1, MotionEvent p2, PageHandler self)
		
		public boolean onPageKey(int keyCode,KeyEvent p2, PageHandler self)
		
	}
	
	/* 默认的ViewBuiler */
	final static class Domean implements ViewBuiler
	{

		@Override
		public boolean onPageKey(int keyCode, KeyEvent p2, PageHandler self){
			return false;
		}
		
		@Override
		public boolean onPageTouch(View p1, MotionEvent p2, PageHandler self){
			return true;
		}
		
		@Override
		public void eatView(View v, String name,PageHandler self)
		{
			Config_Size2 config = (HasAll.Config_Size2)(self. getConfig());
			
			if(v instanceof BubbleEvent){
				((BubbleEvent)v).setTarget(self);
			}
			
			if(v instanceof CodeBlock){
				CodeBlock block = (CodeBlock) v;
				block.config();
			    block.loadSize(config.width,config.height,config.flag);
			}
			else{
				Config_Size2.trim(v,config.width,config.height);
			}
			
			if(v instanceof ImageView){
				eatImageView((ImageView)v,name);
			}
			if(v instanceof EditGroup){
				eatEditGroup((EditGroup)v,name);	
			}
		}
		
		/* 让我们吃掉一个ImageView */
		public void eatImageView(ImageView v,String name)
		{
			Bitmap map = BitmapFactory.decodeFile(name);
			v.setImageBitmap(map);
		}
		
		/* 让我们大量吃掉一个EditGroup */
		public void eatEditGroup(final EditGroup Group,final String name)
		{
			final SpannableStringBuilder spanStr = new SpannableStringBuilder();
			
			//可能还没找完，但并不妨碍我们调整大小
			final Runnable refreshLineAndSize = new Runnable()
			{
				@Override
				public void run()
				{
					Group.refreshLineAndSize();
					Group.requestFocus();
				}
			};
			//设置文本后赶紧染色，实际上正在孑线程中找nodes
			final Runnable DrawText = new Runnable()
			{
				@Override
				public void run()
				{
					List<CodeEdit> List = Group.getEditList();
					for(CodeEdit E:List)
					{
						Editable editor = E.getText();
						E.getPool().execute(E.ReDraw(0,editor.length()));
						E.clearStackDate();
					}
					Group.post(refreshLineAndSize);
				}
			};
			//格式化后设置文本
			final Runnable setText = new Runnable()
			{
				@Override
				public void run()
				{
					EditGroup.EditManipulator builder = Group.getEditManipulator();	
					builder.setEditFlags(0xffffffff);
					builder.setText(spanStr);	
					builder.setEditFlags(0);
					Group.post(DrawText);
				}
			};
			//在子线程中读和格式化文本
			final Runnable readAndFormatText = new Runnable()
			{
				@Override
				public void run()
				{
					myRet ret = new myRet(name);
					String text = ret.r("UTF-8");
					spanStr.append(text);
					myEditFormatorListener.reSAll(0,text.length(),"\t","    ",spanStr);	
					Group.post(setText);
				}
			};
			//第一次加载文本，可以使用线程
			Group.getPool().execute(readAndFormatText);
		}
	}
	
/*
-----------------------------------------------------------------------------------

 PageHandler之三大类

-----------------------------------------------------------------------------------
*/

    /* 一顿操作后，PageHandler所有成员都分配好了空间 */
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

	/* 配置我的大小 */
	final public static class Config_hesSize extends Config_Size2<PageHandler>
	{

		@Override
		public void ConfigSelf(PageHandler target)
		{
			//重新锁定自己的大小
			onChange(target,0);
			super.ConfigSelf(target);
		}

		@Override
		public void onChange(final PageHandler target, int src)
		{
			//屏幕旋转时旋转自己
			super.onChange(target, src);
			trim(target,width,height);
			target.post(new Runnable()
			{
				@Override
				public void run()
				{
					target.tabView(target.getNowIndex());
					//在屏幕旋转后，PageList的某页面可能会重新改变大小(例如EditGroup)，但画布滚动位置还是上次的位置，此时将画布滚动到正确位置
				}
			});
			//这里为什么用post?
			//因为每次屏幕旋转，都会从Activity开始，遍历子View的onConfigurationChanged方法，在方法中才会刷新View的数据(例如宽高)
			//用post以延迟到本次onConfigurationChanged事件之后执行tabView，使数据已经刷新
		}

	}

/*
-----------------------------------------------------------------------------------

  我的外部类啊，请帮我实现主要功能吧
  
-----------------------------------------------------------------------------------
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
