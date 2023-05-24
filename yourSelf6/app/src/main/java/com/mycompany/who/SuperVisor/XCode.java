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
import com.mycompany.who.SuperVisor.CodeShare.*;
import android.graphics.*;


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

	
/*
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 
  View的绘制流程是从ViewRoot的performTraversals开始的
  
  performTraversals会依次调用performMeasure，performLayout，performDraw三个方法
  
  他们会分别调用DecorView的measure，layout，draw方法，再由DecorView调用自己子元素的measure，layout，draw方法
  
  从DecorView开始，每个父元素都调用自己子元素的measure，layout，draw方法，一直分发下去
 
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  measure()方法不可重写，但会调用onMeasure()方法
      
     onMeasure方法用于根据父元素传入的大小测量自己的大小，计算好后一定要调用setMeasuredDimension来记录自己的大小，之后就可以使用getMeasuredWidth和getMeasuredHeight获取测量的大小，如果是ViewGroup，则还要调用自己的子元素的measure方法并传递自己的大小
  
  layout()方法不可重写，但会调用onLayout()方法
  
	 onLayout方法用于布局自己的子元素，父元素必须调用每一个子元素的layout方法，利用子元素测量的宽高，将它们摆到合适位置，(在layout中，子元素的getLeft，getRight，getTop，getBottom方法获取的值就已经设置好了，在layout前的值是不对的)
  
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  draw（）会依次调用几个方法：

     1）drawBackground()，根据在 layout 过程中设置的 View 的位置参数，来设置背景的边界，这个方法不可重写

	 2）onDraw()，绘制View本身的内容，一般自定义单一view会重写这个方法，实现一些绘制逻辑

	 3）dispatchDraw()，绘制子View，一般不要重写这个方法
	 
	 4) onDrawForeground(); 绘制装饰（前景色、滚动条）

	 5) drawDefaultFocusHighlight()  绘制默认焦点突出显示
	 
	 6）onDrawScrollBars()，绘制装饰，如 滚动指示器、滚动条、和前景.
 
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  onMeasure(int widthMeasureSpec, int heightMeasureSpec)

	 MeasureSpec是由父View的MeasureSpec和子View的LayoutParams通过简单的计算得出一个针对子View的测量要求，这个测量要求就是MeasureSpec

	 首先，MeasureSpec是一个大小跟模式的组合值,MeasureSpec中的值是一个整型（32位）将size和mode打包成一个int型，其中高两位是mode，后面30位存的是size
	 
	 // 获取测量模式
	 int specMode = MeasureSpec.getMode(measureSpec)

	 // 获取测量大小
	 int specSize = MeasureSpec.getSize(measureSpec)

	 // 通过Mode 和 Size 生成新的SpecMode
	 int measureSpec=MeasureSpec.makeMeasureSpec(size, mode);

---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  onLayout(boolean changed, int l, int t, int r, int b)
  
     changed: 新范围较原来是否变化了
	 
	 l，t，r，b: 新范围的left，top，right，bottom值，相对于父元素

---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  onDraw(Canvas canvas)
  
     

---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ViewGroup及其子类如果要想指定子View的绘制顺序只需两步:

	 1，setChildrenDrawingOrderEnabled(true) 开启自定义子View的绘制顺序;

	 2，用setZ(float),自定义Z值，值越大越优先绘制；

---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------



----------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 
*/

    @Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int width = config.getWidth(),height = config.getHeight();
		setMeasuredDimension(width,height);
		//我的大小不变，在setMeasuredDimension中会设置测量的宽高
		mTitle.measure(width,height);
		mPages.measure(width,height);
		mDownBar.measure(width,height);
		//为三个子元素测量，并传递我的大小
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		// 布局我的子元素，自定义layout
		int now = 0;
		Config_ChildsPos config = (XCode.Config_ChildsPos) getConfig();
		Config_Size configT = config.configT;
		Config_Size configP = config.configP;
		Config_Size configD = config.configD;
		
		if(getOrientation()==VERTICAL)
		{
			mTitle.layout(0,0,configT.getWidth(),now+=configT.getHeight());
			mPages.layout(0,now,configP.getWidth(),now+=configP.getHeight());
			mDownBar.layout(0,now-configD.getHeight(),configD.getWidth(),now);
		}
		else if(getOrientation()==HORIZONTAL)
		{
			mTitle.layout(0,0,now+=configT.getWidth(),configT.getHeight());
			mPages.layout(now,0,now+=configP.getWidth(),configP.getHeight());
			mDownBar.layout(now-configD.getHeight(),0,now,configD.getHeight());	
		}
	}
	
/*
 -----------------------------------------------------------------------------------

 XCode之三大类
 
 ----------------------------------------------------------------------------------- 
*/
	
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
