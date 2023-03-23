package com.mycompany.who.SuperVisor;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;

import com.mycompany.who.Edit.*;

import java.io.*;
import java.util.concurrent.*;
import com.mycompany.who.Edit.Share.*;
import com.mycompany.who.View.Backgroud;
import com.mycompany.who.Share.myRet;
import com.mycompany.who.R;
import com.mycompany.who.Share.Share;
import java.util.*;

import android.graphics.drawable.Drawable;
import java.util.jar.*;
import android.util.*;
import java.security.acl.*;
import com.mycompany.who.View.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import com.mycompany.who.Edit.Base.Moudle.*;
import com.mycompany.who.SuperVisor.Config.*;
import android.text.*;
import com.mycompany.who.SuperVisor.EditGroup.*;


public class XCode extends LinearLayout implements Configer<XCode>, CodeEdit.IlovePool, EditGroup.IneedWindow, EditGroup.IneedFactory
{
	
	@Override
	public void ConfigSelf(XCode target)
	{
		// TODO: Implement this method
	}
	
	
	private PageList mEditGroupPages;
	protected ScrollView Scro;
	protected HorizontalScrollView hScro;
	protected ListView mWindow;
	
	private CodeEdit HistoryEdit;
	private KeyPool keyPool;
	private HashMap<String,Runnable> keysRunnar;
	private ThreadPoolExecutor pool;
	private EditGroup. EditFactory mfactory;
	
	public Config_hesSize config;
	
	
	public XCode(Context cont){
		super(cont);
		init(cont);
		init2();
	}	
	public XCode(Context cont,AttributeSet set){
		super(cont,set);
		init(cont);
		init2();
	}
	private void init(Context cont){
		mEditGroupPages = new PageList(cont);
		Scro = new ScrollView(cont);
		hScro = new HorizontalScrollView(cont);
		mWindow = new ListView(cont);
		mWindow.setOnItemClickListener(new onMyWindowClick());
		
		Scro.addView(hScro);
		hScro. addView(mEditGroupPages);
		addView(Scro);
		addView(mWindow);
	}	
	private void init2(){
		config = new Config_hesSize();
	}
	
	public PageList getEditGroupPages(){
		return mEditGroupPages;
	}
	
	public void addEdit(String name){
		EditGroup Group = new REditGroup(getContext());
		Group.setPool(pool);
		Group.setWindow(mWindow);
		Group.setEditFactory(mfactory);
		Group.AddEdit(name);
		mEditGroupPages.addView(Group);
	}
	

	
	public ThreadPoolExecutor getPool()
	{
		return pool;
	}
	public EditGroup.EditFactory getEditFactory()
	{
		return mfactory;
	}
	public ListView getWindow()
	{
		return mWindow;
	}
	
	public void setPool(ThreadPoolExecutor pool){
		this.pool = pool;
	}
	public void setEditFactory(EditGroup.EditFactory fa){
		mfactory=fa;
	}
	
	class onMyWindowClick implements OnItemClickListener
	{

		@Override
		public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
		{
			//如果点击了就插入单词并关闭窗口
			WordAdpter adapter = (WordAdpter) p1.getAdapter();
			Icon icon = (Icon) adapter.getItem(p3);
			HistoryEdit.insertWord(icon.getName(), HistoryEdit.getSelectionStart(), icon.getflag());
			
			mWindow.setX(-9999);
			mWindow.setY(-9999);
		}
	}
	
	
	class REditGroup extends EditGroup{
		
		Int EditDrawFlag=new Int();
		
		REditGroup(Context cont){
			super(cont);
			setEditFactory(new Factory2());
		}

		@Override
		protected void trimToFather()
		{
			super.trimToFather();
			trim((View)getParent(),mWidth,mWidth);
			//已知parent为LinearLayout，可以扩展parent的大小
		}

		@Override
		protected wordIndex Calc(RCodeEdit Edit, EditGroup self)
		{
			
			super.Calc(Edit,self);
			//测量并修改Window大小
			config.ConfigSelf(XCode. this);

			//请求测量
			HistoryEdit = Edit;
			//本次窗口谁请求，单词给谁
			int offset=Edit.getSelectionStart();
			int xlen = self.getEditBuilder().calaEditHeight(Edit.index.get());
			wordIndex pos = ((CodeEdit)Edit).getScrollCursorPos(offset, hScro.getScrollX(), Scro.getScrollY() - xlen);

			pos.start += EditLines.getWidth();
			int WindowWidth=config.WindowWidth;
			int WindowHeight=config.WindowHeight;
			int selfWidth=config.selfWidth;
			int selfHeight=config.selfHeight;

			if (pos.start + WindowWidth >selfWidth)
				pos.start =selfWidth - WindowWidth;
			//如果x超出屏幕，总是设置在最右侧

			if (pos.end + WindowHeight + Edit.getLineHeight() >selfHeight)
				pos.end = pos.end - WindowHeight - Edit.getLineHeight();
			//如果y超出屏幕，将其调整为光标之前，否则调整在光标后
			else
				pos.end = pos.end + Edit.getLineHeight();

			return pos;
		}
		
		
		public class ClipCanvaser extends EditCanvaserListener
		{

			//提升效率，不想用可以remove
			@Override
			public void onDraw(EditText self, Canvas canvas, TextPaint paint, Rect Cursor_bounds)
			{	
				/*关键代码*/
				Rect rect =selfRect(self);
				canvas.clipRect(rect);
				//clipRect可以指示一块相对于自己的矩形区域，超出区域的部分会被放弃绘制

				if(EditDrawFlag.get()==0){
					//遍历所有其它编辑器，并显示
					EditDrawFlag.add();
					Int id = historyId;
					historyId=((RCodeEdit)self).index;
					for(CodeEdit e: getEditList()){
						if(((RCodeEdit)e).index.get()!=historyId.get())
							e.invalidate();
					}
					EditDrawFlag.less();
					historyId=id;
				}
			}

			public Rect selfRect(EditText self){

				int index = ((RCodeEdit)self).index.get();
				//计算编辑器在可视区域中的自己的范围

				int EditTop=getEditBuilder().calaEditHeight(index); //编辑器较ForEditSon的顶部位置
				int SeeTop = Scro.getScrollY(); //可视区域较ForEditSon的顶部位置
				int SeeLeft = hScro.getScrollX();//可视区域较ForEditSon的左边位置

				int left = SeeLeft - EditLines.maxWidth();
				//编辑器左边是当前可视区域左边减EditLines的宽
				int top = SeeTop - EditTop;
				//编辑器顶部为从0开始至可视区域顶部的偏移
				int right = config. selfWidth + left;
				//编辑器右边是左边加一个可视区域的宽
				int bottom= top+ config.selfHeight;
				//编辑器底部是上面加一个可视区域的高
				return new Rect(left,top,right,bottom);
			}

		}
		EditCanvaserListener getClipCanvaser()
		{
			//一直不知道，为什么明明EditCanvaserListener需要EditGroup内部的成员，却还允许返回并作为其它Edit的监听器
			//在调试时，发现EditCanvaserListener内部还有一个this$0成员，原来这个成员就是EditGroup
			//原来每个内部类，还有一个额外的成员，就是指向外部类实例的指针，在创建一个内部类对象时，内部类对象就与外部类实例绑定了
			//其实不安全
			return new ClipCanvaser();
		}
		
		class Factory2 implements EditGroup.EditFactory
		{

			@Override
			public CodeEdit getEdit(EditGroup self)
			{
				CodeEdit E= new CodeEdit(self.getContext());
				E.getCanvaserList().add(getClipCanvaser());
				return E;
			}

			@Override
			public void configEdit(CodeEdit Edit, String name, EditGroup self)
			{
				Edit.setPool(pool);
				com.mycompany.who.Share.Share.setEdit(Edit, name);
			}
		}
		
	}
	
	
	public static class Config_hesSize implements Configer<XCode>
	{
		public boolean portOrLand=true;
		public int WindowHeight=600, WindowWidth=600;
		public int selfWidth=1000,selfHeight=2000;
		
		@Override
		public void ConfigSelf(XCode target)
		{
			int height=MeasureWindowHeight(target.mWindow);
			if(portOrLand==ConfigViewWith_PortAndLand.Port){
				WindowWidth=WindowHeight=(int)(selfWidth*0.9);
			}
			else{
				WindowWidth=(int)(selfWidth*0.7);
				WindowHeight= (int)(selfHeight*0.3);
			}	
			if (height < WindowHeight)
				WindowHeight=height;
			EditGroup.trim(target.mWindow,WindowWidth,WindowHeight);
		}

		public void set(int width,int height,boolean is,XCode target){
			selfWidth=width;
			selfHeight=height;
			portOrLand=is;
			onChange(target);
		}
		public void change(XCode target){
			int tmp = selfWidth;
			selfWidth=selfHeight;
			selfHeight=tmp;
			portOrLand=!portOrLand;
			onChange(target);
		}
		protected void onChange(XCode target){
			EditGroup.trim(target,selfWidth,selfHeight);
			EditGroup.trim(target.Scro,selfWidth,selfHeight);
			EditGroup.trim(target.hScro,selfWidth,selfHeight);
		}
		public static int MeasureWindowHeight(ListView mWindow)
		{
			int height=0;
			int i;
			WordAdpter adapter= (WordAdpter) mWindow.getAdapter();
			if(adapter==null)
				return 0;
			for (i = 0;i < adapter.getCount();i++)
			{
				View view = adapter.getView(i, null, mWindow);
				view.measure(0, 0);
				height += view.getMeasuredHeight();
				//若View没有明确设定width和height时，它的大小为0
				//可以measure方法测量它的大小，这样测量的大小会被保存，然后获取测量的高
				//注意，getWidth不等于getMeasuredHeight
			}

			return height;
		}

	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig)
	{
		config.change(this);
		super.onConfigurationChanged(newConfig);
	}

	
}
