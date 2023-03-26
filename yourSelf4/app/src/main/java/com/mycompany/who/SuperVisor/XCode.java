package com.mycompany.who.SuperVisor;
import com.mycompany.who.SuperVisor.Moudle.*;
import com.mycompany.who.SuperVisor.Moudle.Config.*;
import android.content.*;
import android.util.*;
import com.mycompany.who.Edit.*;
import java.util.concurrent.*;



/*
  XCode封装了Title，PageHandler和DownBar
  
  并且，XCode将它们关联起来
  
  麻烦啊，XCode要计算它们的大小并把它们放到合适位置

*/
public class XCode extends HasAll implements CodeEdit.IlovePool
{

	private Title mTitle;
	private PageHandler mPages;
	private DownBar mDownBar;
	
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
		Context cont = getContext();
		mTitle = new Title(cont);
		mPages = new PageHandler(cont);
		mDownBar = new DownBar(cont);
		mTitle.setTarget(this);
		mPages.setTarget(this);
		mDownBar.setTarget(this);
		config();
	}
	public void config(){
		addView(mTitle);
		addView(mPages);
		addView(mDownBar);
		
	}
	@Override
	public void setPool(ThreadPoolExecutor pool)
	{
		// TODO: Implement this method
	}

	@Override
	public ThreadPoolExecutor getPool()
	{
		// TODO: Implement this method
		return null;
	}
	
	public static class Config_ChildsPos extends Config_Size2<XCode>
	{

		Title.Config_hesSize configT;
		PageHandler.Config_hesSize configP;
		
		@Override
		public void ConfigSelf(XCode target)
		{
			
		}

		@Override
		public void set(int width, int height, int is, XCode target)
		{
			configT.set(width,(int)(height*0.1),portOrLand,target.mTitle);
			configP.set(width,(int)(height*0.9),portOrLand,target.mPages);
			super.set(width,height,is,target);
		}

		@Override
		public void change(XCode target)
		{
			target. setOrientation(portOrLand);
			configT.change(target.mTitle);
			configP.change(target.mPages);
			super.change(target);
		}

		@Override
		public void onChange(XCode target)
		{
			trim(target,width,height);
		}
		
	}
	
}
