package com.mycompany.who.SuperVisor;

import android.content.*;
import android.content.res.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.mycompany.who.R;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Base.Share.Share2.*;
import com.mycompany.who.SuperVisor.CodeMoudle.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View3.*;
import java.util.*;
import java.util.concurrent.*;

import android.view.View.OnClickListener;
import java.io.*;
import com.mycompany.who.SuperVisor.Share.*;


/*
  XCode封装了Title，PageHandler和DownBar
  
  并且，XCode将它们关联起来
  
  麻烦啊，XCode要计算它们的大小并把它们放到合适位置

*/
public class XCode extends HasAll implements PageHandler.requestWithPageHandler
{

	private Title mTitle;
	private PageHandler mPages;
	private DownBar mDownBar;
	
	private ThreadPoolExecutor pool;
	private KeyPool KeyPool;
	private Map<Integer,Runnable> KeysRunner;
	
	
	public XCode(Context cont){
		super(cont);
	}
	public XCode(Context cont,AttributeSet attrs){
		super(cont,attrs);
	}
	@Override
	public void init()
	{
		super.init();
		Creator = new CodeCreator(R.layout.XCode);
		Creator.ConfigSelf(this);
	}
	
	@Override
	public void setPool(ThreadPoolExecutor pool)
	{
		this.pool=pool;
		mPages.setPool(pool);
	}
	@Override
	public ThreadPoolExecutor getPool()
	{
		return pool;
	}
	
	public void addEdit(String name){
		mPages.addEdit(name);
	}
	@Override
	public void addView(View S, String name)
	{
		mPages.addView(S,name);
	}
	
	public Title getTitle(){
		return mTitle;
	}
	public PageHandler getPages(){
		return mPages;
	}
	public DownBar getDownBar(){
		return mDownBar;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		
		return super.dispatchKeyEvent(event);
	}
	
	
	
	//一顿操作后，Code所有成员都分配好了空间
	final static class CodeCreator extends Creator<XCode>
	{
		
		public CodeCreator(int id){
			super(id);
		}

		@Override
		public void init(XCode target, View root)
		{
			target.mTitle = root.findViewById(R.id.Title);
			target.mPages = root.findViewById(R.id.PageHandler);
			target.mDownBar = root.findViewById(R.id.DownBar);
			
			target.Configer = new Config_hesView();
			target.config = new Config_ChildsPos(target);
		}
	}
	
	//如何配置View
	final static class Config_hesView implements Level<XCode>
	{
			
		@Override
		public void ConfigSelf(XCode target)
		{
			//把子元素也配置一遍
			target.mTitle.setTarget(target);
			target.mPages.setTarget(target);
			target.mDownBar.setTarget(target);
			target.mTitle.config();
			target.mPages.config();
			target.mDownBar.config();
			config(target);
		}

		@Override
		public void config(XCode target)
		{
			target.setBackgroundColor(Colors.Bg);
			new myCodeBuilder().ConfigSelf(target);
		}
		
		@Override
		public void clearConfig(XCode target){}
		
	}
	
	//配置孩子们的位置
	final public static class Config_ChildsPos extends Config_Size2<XCode>
	{

	    public Config_Size configT;
		public Config_Size configP;
		public Config_Size configD;
		
		public Config_ChildsPos(XCode target)
		{
			configT= target.mTitle.getConfig();
			configP= target.mPages.getConfig();
			configD= target.mDownBar.getConfig();
		}

		/* 在初始化时设置好孩子们的位置 */
		@Override
		public void set(int width, int height, int is, XCode target)
		{
			if(is==Configuration.ORIENTATION_PORTRAIT)
			{
			    configT.set(width,(int)(height*0.1),is,target.mTitle);
			    configP.set(width,(int)(height*0.9),is,target.mPages);
				configD.set(width,(int)(height*0.5),is,target.mDownBar);
			}
			else if(is==Configuration.ORIENTATION_LANDSCAPE)
			{
				configT.set(height,(int)(width*0.1),Configuration.ORIENTATION_PORTRAIT,target.mTitle);
				configT.change(target.mTitle,is);
				configP.set((int)(width*0.9),height,is,target.mPages);
				configD.set((int)(width*0.5),height,is,target.mDownBar);
			}
			super.set(width,height,is,target);
		}

		/* 改变孩子们 */
		@Override
		public void onChange(final XCode target,int src)
		{
			trim(target,width,height);
			target.setOrientation(CastFlag(flag));
			configT.change(target.mTitle,flag);
			configP.change(target.mPages,flag);
			configD.change(target.mDownBar,flag);
		}
		
	}

	@Override
	public boolean BubbleMotionEvent(MotionEvent event)
	{
		return true;
	}
	@Override
	public boolean BubbleKeyEvent(int keyCode, KeyEvent event)
	{
		return false;
	}
		
}
