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
import com.mycompany.who.SuperVisor.CodeMoudle.Share.*;
import java.util.*;
import java.util.concurrent.*;

import android.view.View.OnClickListener;


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
			
			target.Configer = new Config_hesView(target);
			target.config = new Config_ChildsPos(target);
		}
	}
	
	//如何配置View
	final static class Config_hesView implements Level<XCode>,OnItemSelectedListener,ReSpinner.onSelectionListener,PageList.onTabPage
	{
	
		ReSpinner spinner;
		LinearLayout ButtonBar;
		ReSpinner More;
		PageList pages;
		DownBar downBar;
		XCode target;
		
		public Config_hesView(XCode target)
		{
			spinner = target.mTitle.getReSpinner();
			ButtonBar = target.mTitle.getButtonBar();
			More = target.mTitle.getMore();
			pages = target.mPages;
			downBar = target.mDownBar;
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
			target.mDownBar.config();
			config(target);
		}

		@Override
		public void config(XCode target)
		{
			spinner.setOnItemSelectedListener(this);
			spinner.setonSelectionListener(this);
			pages.setonTabListener(this);
			target.setBackgroundColor(Colors.Bg);
			
			onButtonBarChildClick ButtonBarListener = new onButtonBarChildClick();
			ButtonBar.getChildAt(0).setOnClickListener(ButtonBarListener.Uedo());
			ButtonBar.getChildAt(1).setOnClickListener(ButtonBarListener.Redo());
			ButtonBar.getChildAt(2).setOnClickListener(ButtonBarListener.Read());
		}
		
		@Override
		public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4){
			pages.tabView(p3);
		}

		@Override
		public void onRepeatSelected(int postion){}

		@Override
		public void onNothingSelected(AdapterView<?> p1){}
		
		
		@Override
		public void onTabPage(int index){
			spinner.setSelection(index);
		}

		@Override
		public void onAddPage(View v,String name)
		{
			name = name.substring(name.lastIndexOf('/')+1,name.length());
			WordAdpter adapter = (WordAdpter) spinner.getAdapter();
			Icon icon = new Icon3(Share.getFileIcon(name),name);
			
			if(adapter!=null){
				adapter.getList().add(icon);
				adapter.notifyDataSetChanged();	
			}
			else{
				List<Icon> list = new ArrayList<>();
				list.add(icon);
				spinner.setAdapter(new WordAdpter(list,R.layout.FileIcon,0));
			}
		}

		@Override
		public void onDelPage(int index)
		{
			WordAdpter adapter = (WordAdpter) spinner.getAdapter();
			if(adapter!=null){
				adapter.getList().remove(index);
				adapter.notifyDataSetChanged();	
			}
		}
		
		class onButtonBarChildClick
		{
			
			public OnClickListener Uedo(){
				return new Uedo();
			}
			public OnClickListener Redo(){
				return new Redo();
			}
			public OnClickListener Read(){
				return new Read();
			}
			public OnClickListener Write(){
				return new Write();
			}
			
			public class Uedo implements OnClickListener
			{
				@Override
				public void onClick(View p1)
				{
					EditGroup Group = (EditGroup) pages.getChildAt(pages.getNowIndex());
					Group.getEditManipulator().Uedo();
				}
			}
			
			public class Redo implements OnClickListener
			{
				@Override
				public void onClick(View p1)
				{
					EditGroup Group = (EditGroup) pages.getChildAt(pages.getNowIndex());
					Group.getEditManipulator().Redo();
				}
			}
			
			public class Read implements OnClickListener
			{
				@Override
				public void onClick(View p1)
				{
					p1.setBackgroundResource(R.drawable.Read);
					EditGroup Group = (EditGroup) pages.getChildAt(pages.getNowIndex());
					Group.getEditManipulator().lockThem(true);
					p1.setOnClickListener(Write());
				}
		    }
			
			public class Write implements OnClickListener
			{
				@Override
				public void onClick(View p1)
				{
					p1.setBackgroundResource(R.drawable.Write);
					EditGroup Group = (EditGroup) pages.getChildAt(pages.getNowIndex());
					Group.getEditManipulator().lockThem(false);
					p1.setOnClickListener(Read());
				}
			}
			
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
			
			target. post(new Runnable()
			{
				@Override
				public void run()
				{
					target.mPages.tabView(target.mPages.getNowIndex());
					//在屏幕旋转后，PageList的某页面可能会重新改变大小(例如EditGroup)，但画布滚动位置还是上次的位置，此时将画布滚动到正确位置
				}
			});
			//这里为什么用post?
			//因为每次屏幕旋转，都会从Activity开始，遍历子View的onConfigurationChanged方法，在方法中才会刷新View的数据(例如宽高)
			//用post以延迟到本次onConfigurationChanged事件之后执行tabView，使数据已经刷新
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
