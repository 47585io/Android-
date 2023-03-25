package com.mycompany.who.SuperVisor;
import com.mycompany.who.SuperVisor.Moudle.*;
import com.mycompany.who.SuperVisor.Moudle.Config.*;
import android.content.*;
import android.util.*;



/*
  XCode封装了Title，PageHandler和DownBar
  
  并且，XCode将它们关联起来

*/
public class XCode extends HasAll
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
	}
	
	
	
}
