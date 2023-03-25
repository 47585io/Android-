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


/*
  负责任的PageHandler组件
  
  它负责管理Page的切换
  
  重要的是，它给每一个Page加上了一个滚动条
  
  它还拥有一个能游走于自己的范围的Window
  
  一切都与EditGroup需要的不谋而和，因此有了REditGroup

*/
public class PageHandler extends HasAll implements CodeEdit.IlovePool, EditGroup.IneedWindow, EditGroup.IneedFactory
{

	protected ScrollBar Scro;
	protected HScrollBar hScro;
	protected PageList mEditGroupPages;
	protected ListView mWindow;
	
	private CodeEdit HistoryEdit;
	private ThreadPoolExecutor pool;
	private EditGroup.EditFactory mfactory;
	
	public Config_hesSize config;
	
	
	public PageHandler(Context cont){
		super(cont);	
	}	
	public PageHandler(Context cont,AttributeSet set){
		super(cont,set);
	}
	@Override
	public void init()
	{
		new HandlerCreator(R.layout.PageHandler).ConfigSelf(this);
		new Config_Level().ConfigSelf(this);
	}
	
	
	public PageList getEditGroupPages(){
		return mEditGroupPages;
	}
	/* 不管如何，
	
	   您都应该避免使用getEdit，而是使用getView
	   
	   getEdit只返回Edit，若Page不是Edit返回null
	*/
	public EditGroup getEditGroup(int index){
	    View v = mEditGroupPages.getView(index);
		if(v instanceof EditGroup)
			return (EditGroup)v;
		return null;
	}
	
	public CodeEdit getEdit(size s){
		EditGroup Group = getEditGroup(s.start);
		if(Group!=null){
			return Group.getEditList().get(s.end);
		}
		return null;
	}
	
	public View getView(int index){
		return mEditGroupPages.getView(index);
	}
	
	public int getNowIndex(){
		return mEditGroupPages.getNowIndex();
	}
	
	/*
	  addEdit ？ addViewS
	
	  添加一个EditGroup，并自动配置 ？ 添加许多View
	*/
	final private EditGroup creatEdit(String name){
		EditGroup Group = new REditGroup(getContext());
		Group.setPool(pool);
		Group.setWindow(mWindow);
		Group.setEditFactory(mfactory);
		Group.AddEdit(name);
		Group.setTag(name);
		Group.setTarget(this);
		return Group;
	}
	final public void addEdit(String name){
		EditGroup Group = creatEdit(name);
		mEditGroupPages.addView(Group);
	}
	public void addViewS(View... S, String name){
		LinearLayout page = new LinearLayout(getContext());
		for(View v:S){
			if(v instanceof Interfaces.BubbleEvent)
				((Interfaces.BubbleEvent)v).setTarget(this);
		    page.addView(v);
		}
		page.setTag(name);
		mEditGroupPages.addView(page);
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
	public void setPageListener(PageList.onTabPage li){
		mEditGroupPages.setListener(li);
	}
	
	
	final class REditGroup extends EditGroup{
		
		Int EditDrawFlag=new Int();
		
		REditGroup(Context cont){
			super(cont);
			setEditFactory(new Factory2());
		}

		@Override
		protected void trimToFather()
		{
			super.trimToFather();
			trim((View)getParent(),mWidth,mHeight);
			//已知parent为PageList，可以扩展parent的大小
		}

		@Override
		protected size Calc(RCodeEdit Edit, EditGroup self)
		{
			
			super.Calc(Edit,self);
			//测量并修改Window大小
			config.ConfigSelf(PageHandler. this);

			//请求测量
			HistoryEdit = Edit;
			//本次窗口谁请求，单词给谁
			int offset=Edit.getSelectionStart();
			int xlen = self.getEditBuilder().calaEditHeight(Edit.index.get());
			size pos = ((CodeEdit)Edit).getScrollCursorPos(offset, hScro.getScrollX(), Scro.getScrollY() - xlen);

			pos.start += EditLines.getWidth();
			int WindowWidth=config.WindowWidth;
			int WindowHeight=config.WindowHeight;
			int selfWidth=config.selfWidth;
			int selfHeight=config.selfHeight;
			
			if (pos.start + WindowWidth >selfWidth)
				pos.start =selfWidth - WindowWidth;
			//如果x超出屏幕，总是设置在最右侧

			if (pos.end + WindowHeight + Edit.getLineHeight() > selfHeight)
				pos.end = pos.end - WindowHeight - Edit.getLineHeight();
			//如果y超出屏幕，将其调整为光标之前，否则调整在光标后
			else
				pos.end = pos.end + Edit.getLineHeight() ;

			return pos;
		}
		
		
		final public class ClipCanvaser extends Clip
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
		public EditCanvaserListener getClipCanvaser()
		{
			//一直不知道，为什么明明EditCanvaserListener需要EditGroup内部的成员，却还允许返回并作为其它Edit的监听器
			//在调试时，发现EditCanvaserListener内部还有一个this$0成员，原来这个成员就是EditGroup
			//原来每个内部类，还有一个额外的成员，就是指向外部类实例的指针，在创建一个内部类对象时，内部类对象就与外部类实例绑定了
			//其实不安全
			return new ClipCanvaser();
		}
		
		//推荐使用
		class Factory2 implements EditGroup.EditFactory
		{

			@Override
			public CodeEdit getEdit(EditGroup self)
			{
				CodeEdit E= new RCodeEdit(self.getContext());
				E.getCanvaserList().add(getClipCanvaser());
				E.setOnClickListener(new Click());
				return E;
			}

			@Override
			public void configEdit(CodeEdit Edit, String name, EditGroup self)
			{
				com.mycompany.who.Share.Share.setEdit(Edit, name);
			}
		}
		
	}

	//已经到达了最后吗？
	@Override
	public boolean BubbleMotionEvent(MotionEvent event)
	{
		super.BubbleMotionEvent(event);
		return true;
	}

	@Override
	public boolean BubbleKeyEvent(int keyCode, KeyEvent event)
	{
		super.BubbleKeyEvent(keyCode, event);
		if(Scro.size()!=0||hScro.size()!=0){
			Scro.goback();
			hScro.goback();
			return true;
		}
		return false;
	}
	
	
	/*
	 Config_hesSize 

	 锁定我的大小，使用设置的大小

	 自动根据横竖屏改变大小

	 */
	final public static class Config_hesSize implements Config_Size<PageHandler>
	{
		public boolean portOrLand=true;
		public int WindowHeight=600, WindowWidth=600;
		public int selfWidth=1000,selfHeight=2000+200;
		
		@Override
		public void ConfigSelf(PageHandler target)
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
			//立即设置Window大小
		}

		public void set(int width,int height,boolean is,PageHandler target){
			//设置我的大小
			selfWidth=width;
			selfHeight=height;
			portOrLand=is;
			onChange(target);//立即生效
		}
		public void change(PageHandler target){
			//屏幕旋转了，将宽高互换
			int tmp = selfWidth;
			selfWidth=selfHeight;
			selfHeight=tmp;
			portOrLand=!portOrLand;
			onChange(target);//立刻生效
		}
		//每次change，改变我的大小，即我自己和滚动条的大小
		public void onChange(PageHandler target){
			EditGroup.trim(target,selfWidth,selfHeight);
			EditGroup.trim(target.Scro,selfWidth,selfHeight);
			EditGroup.trim(target.hScro,selfWidth,selfHeight);
		}
		//测量窗口高度
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

	public void loadSize(int width,int height,boolean is){
		config.set(width,height,is,this);
	}
	
	@Override
	protected void onConfigurationChanged(Configuration newConfig)
	{
		config.change(this);//屏幕旋转了
		super.onConfigurationChanged(newConfig);
	}
	
	
	
	//一顿操作后，PageHandler所有成员都分配好了空间
	final static class HandlerCreator extends Creator<PageHandler>
	{
		
		public HandlerCreator(int i){
			super(i);
		}

		public void init(PageHandler target,View tmp)
		{
			target.Scro = tmp.findViewById(R.id.Scro);
			target.hScro = tmp.findViewById(R.id.hScro);
			target.mEditGroupPages = tmp.findViewById(R.id.mPages);
			target.mWindow = tmp.findViewById(R.id.mWindow);
			target. config = new Config_hesSize();
		}

	}
	
	// 如何配置View层次结构
	final class Config_Level implements Level<PageHandler>
	{

		@Override
		public void config(PageHandler target)
		{
			target.mWindow.setBackgroundColor(Colors.Bg);
			target. mWindow.setDivider(null);
			target. mWindow.setOnItemClickListener(new onMyWindowClick());
		}

		@Override
		public void ConfigSelf(PageHandler target)
		{
			config(target);
		}
	}
	
	final class onMyWindowClick implements OnItemClickListener
	{

		@Override
		public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
		{
			//如果点击了就插入单词并关闭窗口
			WordAdpter adapter = (WordAdpter) p1.getAdapter();
			Icon icon = (Icon) adapter.getItem(p3);
			if(HistoryEdit!=null)
			    HistoryEdit.insertWord(icon.getName(), HistoryEdit.getSelectionStart(), icon.getflag());

			mWindow.setX(-9999);
			mWindow.setY(-9999);
		}
	}

}
