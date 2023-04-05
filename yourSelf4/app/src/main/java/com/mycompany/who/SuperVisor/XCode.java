package com.mycompany.who.SuperVisor;
import com.mycompany.who.SuperVisor.Moudle.*;
import com.mycompany.who.SuperVisor.Moudle.Config.*;
import android.content.*;
import android.util.*;
import com.mycompany.who.Edit.*;
import java.util.concurrent.*;
import com.mycompany.who.SuperVisor.Moudle.Share.*;
import java.util.*;
import android.view.*;
import com.mycompany.who.R;
import android.content.res.*;
import android.widget.*;
import com.mycompany.who.View.*;
import android.widget.AdapterView.*;

import com.mycompany.who.Edit.Share.Share2.*;
import java.security.acl.*;


/*
  XCode封装了Title，PageHandler和DownBar
  
  并且，XCode将它们关联起来
  
  麻烦啊，XCode要计算它们的大小并把它们放到合适位置

*/
public class XCode extends HasAll implements CodeEdit.IlovePool, PageHandler.requestWithPageHandler
{

	private Title mTitle;
	private PageHandler mPages;
	private DownBar mDownBar;
	
	private ThreadPoolExecutor pool;
	private ExtensionChooser Extension;
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
			
			target.Configer = new Config_hesView(target);
			target.config = new Config_ChildsPos(target);
		}
	}
	
	//如何配置View
	final static class Config_hesView implements Level<XCode>
	{
	
		ReSpinner spinner;
		PageList pages;
		LinearLayout ButtonBar;
		XCode target;
		
		public Config_hesView(XCode target){
			spinner = target.mTitle.getReSpinner();
			ButtonBar = target.mTitle.getButtonBar();
			pages = target.mPages.getEditGroupPages();
			this.target = target;
		}
		
		@Override
		public void ConfigSelf(XCode target)
		{
			//把子元素也配置一遍
			target. mTitle.setTarget(target);
			target. mPages.setTarget(target);
			target. mDownBar.setTarget(target);
			target.mTitle.config();
			target.mPages.config();
			config(target);
		}

		@Override
		public void config(XCode target)
		{
			spinner.setOnItemSelectedListener(new onitemSeletion());
			spinner.setonSelectionListener(new onSeletion());
			ButtonBar.getChildAt(0).setOnClickListener(new Click());
			pages.setonTabListener(new onTabPage());
			target.setBackgroundColor(0xff222222);
		}
		
		@Override
		public void clearConfig(XCode target)
		{
			// TODO: Implement this method
		}

		class Click implements OnClickListener
		{

			@Override
			public void onClick(View p1)
			{
				PageHandler h = target.getPages();
				EditGroup Group = h.getEditGroup(h.getNowIndex());
				Group.getEditBuilder().Uedo();
			}
		}
		
		class onSeletion implements ReSpinner.onSelectionListener
		{

			@Override
			public void onRepeatSelected(int postion)
			{
				
			}
		}
		class onitemSeletion implements OnItemSelectedListener
		{

			@Override
			public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4)
			{
				pages.tabView(p3);
				View v=pages.getView(p3);
				ViewGroup.LayoutParams pa = v.getLayoutParams();
			    trim(pages,pa.width,pa.height);
				v.requestFocus();
			}

			@Override
			public void onNothingSelected(AdapterView<?> p1)
			{
				// TODO: Implement this method
			}
		}
		class onTabPage implements PageList.onTabPage
		{

			@Override
			public void onTabPage(int index)
			{
				
			}

			@Override
			public void onAddPage(View v,String name)
			{
				List<Icon> list = new ArrayList<>();
				pages.toList(list);
				spinner.setAdapter(new WordAdpter(target.getContext(),list,R.layout.FileIcon));
			}

			@Override
			public void onDelPage(int index)
			{
				// TODO: Implement this method
			}
		}
		
	}
	
	//配置孩子们的位置
	final public static class Config_ChildsPos extends Config_Size2<XCode>
	{

		Title.Config_hesSize configT;
		PageHandler.Config_hesSize configP;
		
		public Config_ChildsPos(XCode target){
			configT=(Title.Config_hesSize) target.mTitle.getConfig();
			configP=(PageHandler.Config_hesSize) target.mPages.getConfig();
		}
		
		@Override
		public void ConfigSelf(XCode target)
		{
			
		}

		/* 只要在初始化时设置好孩子们的位置，之后孩子们会自动改变 */
		@Override
		public void set(int width, int height, int is, XCode target)
		{
			if(is==Configuration.ORIENTATION_PORTRAIT){
			    configT.set(width,(int)(height*0.1),is,target.mTitle);
			    configP.set(width,(int)(height*0.9),is,target.mPages);
			}
			else if(is==Configuration.ORIENTATION_LANDSCAPE){
				configT.set((int)(width*0.1),height,is,target.mTitle);
				configP.set(height,(int)(width*0.9),Configuration.ORIENTATION_PORTRAIT,target.mPages);
				configP.change(target.mPages,is);
			}
			super.set(width,height,is,target);
		}

		@Override
		public void onChange(XCode target,int src)
		{
			trim(target,width,height);
			target.setOrientation(CastFlag(flag));
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
	

	@Override
	protected void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		getConfig(). change(this,newConfig.orientation);
		
	}
	
	
}
