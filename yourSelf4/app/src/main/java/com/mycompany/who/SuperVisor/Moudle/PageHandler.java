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
import java.util.*;
import android.net.*;
import com.mycompany.who.Share.*;


/*
  负责任的PageHandler组件
  
  它负责管理Page的切换
  
  重要的是，它给每一个Page加上了一个滚动条
  
  它还拥有一个能游走于自己的范围的Window
  
  一切都与EditGroup需要的不谋而和，因此有了REditGroup

*/
public class PageHandler extends HasAll implements CodeEdit.IlovePool,EditGroup.IneedFactory
{

	protected ScrollBar Scro;
	protected HScrollBar hScro;
	protected PageList mEditGroupPages;
	protected ListView mWindow;
	
	private CodeEdit HistoryEdit;
	private ThreadPoolExecutor pool;
	private EditGroup.EditFactory mfactory;
	private ViewBuiler ViewBuilder;
	
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
		Creator = new HandlerCreator(R.layout.PageHandler);
		Creator.ConfigSelf(this);
	}
	@Override
	public void config()
	{
		super.config();
		
	}
	
	
	public PageList getEditGroupPages(){
		return mEditGroupPages;
	}
	public ScrollView getScro(){
		return Scro;
	}
	public HorizontalScrollView gethScro(){
		return hScro;
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
	  addEdit ？ addView
	
	  添加一个EditGroup，并自动配置 ？ 添加View
	*/
	private EditGroup creatEdit(String name){
		EditGroup Group = new REditGroup(getContext());
		Group.config();
		Group.setPool(pool);
		Group.setWindow(mWindow);
		Group.setEditFactory(mfactory);
		Group.AddEdit(name);
		Group.setTarget(this);
		return Group;
	}
	public void addEdit(String name){
		EditGroup Group = creatEdit(name);
		mEditGroupPages.addView(Group,name);
		if(ViewBuilder!=null)
			ViewBuilder.eatView(Group,name);
	}
	public void addView(View v, String name){
		mEditGroupPages.addView(v,name);
		if(ViewBuilder!=null)
			ViewBuilder.eatView(v,name);
	}

	public ThreadPoolExecutor getPool()
	{
		return pool;
	}
	public EditGroup.EditFactory getEditFactory()
	{
		return mfactory;
	}
	public ViewBuiler getViewBuilder()
	{
		return ViewBuilder;
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
	public void setViewBuilder(ViewBuiler b){
		ViewBuilder = b;
	}
	public void setPageListener(PageList.onTabPage li){
		mEditGroupPages.setonTabListener(li);
	}
	
	
	final class REditGroup extends EditGroup{
		
		Int EditDrawFlag=new Int();
		
		REditGroup(Context cont){
			super(cont);
		}

		@Override
		protected void trimToFather()
		{
			super.trimToFather();
			EditGroup. Config_hesSize config = (EditGroup.Config_hesSize) getConfig();
			trim((View)getParent(),config.width,config.height);
			//已知parent为PageList，可以扩展parent的大小
		}

		@Override
		protected size Calc(RCodeEdit Edit, EditGroup self)
		{
			
			super.Calc(Edit,self);
			//测量并修改Window大小
			PageHandler.Config_hesSize config = (PageHandler.Config_hesSize)PageHandler.this. getConfig();
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
			int selfWidth=config.width;
			int selfHeight=config.height;
			
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
					//第一个编辑器还要遍历所有其它编辑器，并显示
					EditDrawFlag.set(getEditList().size()); //当前还有size个编辑器要显示
					Int id = historyId;
					historyId=((RCodeEdit)self).index;
					for(CodeEdit e: getEditList()){
						//如果是第一个编辑器，则不用重新绘制
						if(((RCodeEdit)e).index.get()!=historyId.get())
							e.invalidate();
					}
					historyId=id;
				}
				EditDrawFlag.less();
				//一个编辑器绘制完成了，将Flag--，当Flag==0，则所有编辑器绘制完成了
			}

			public Rect selfRect(EditText self){

				int index = ((RCodeEdit)self).index.get();
				//计算编辑器在可视区域中的自己的范围

				PageHandler.Config_hesSize config = (PageHandler.Config_hesSize)PageHandler.this. getConfig();
				int EditTop=getEditBuilder().calaEditHeight(index); //编辑器较ForEdit的顶部位置
				int SeeTop = Scro.getScrollY(); //可视区域较ForEdit的顶部位置
				int SeeLeft = hScro.getScrollX();//可视区域较ForEdit的左边位置

				int left = SeeLeft - EditLines.maxWidth();
				//编辑器左边是当前可视区域左边减EditLines的宽
				int top = SeeTop - EditTop;
				//编辑器顶部为从0开始至可视区域顶部的偏移
				int right = config. width + left;
				//编辑器右边是左边加一个可视区域的宽
				int bottom= top+ config.height;
				//编辑器底部是上面加一个可视区域的高
				return new Rect(left,top,right,bottom);
			}

		}
		protected EditCanvaserListener getClipCanvaser()
		{
			//一直不知道，为什么明明EditCanvaserListener需要EditGroup内部的成员，却还允许返回并作为其它Edit的监听器
			//在调试时，发现EditCanvaserListener内部还有一个this$0成员，原来这个成员就是EditGroup
			//原来每个内部类，还有一个额外的成员，就是指向外部类实例的指针，在创建一个内部类对象时，内部类对象就与外部类实例绑定了
			//其实不安全
			return new ClipCanvaser();
		}
		
	}

	//已经到达了最后吗？
	@Override
	public boolean BubbleMotionEvent(MotionEvent p2)
	{
		return super.BubbleMotionEvent(p2);
	}	
	@Override
	public boolean onTouchEvent(MotionEvent p2)
	{
		if(p2.getPointerCount()==2&&p2.getHistorySize()!=0){
			Scro.setCanScroll(false);
			hScro.setCanScroll(false);
		}
		if(p2.getAction()==MotionEvent.ACTION_UP||p2.getAction()==MotionEvent.ACTION_DOWN){
			Scro.setCanScroll(true);
			hScro.setCanScroll(true);
		}
		View v = getView(getNowIndex());
		return ViewBuilder.onTouch(v,p2);
	}
	
	@Override
	public boolean BubbleKeyEvent(int keyCode, KeyEvent event)
	{
		return super.BubbleKeyEvent(keyCode, event);
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if((Scro.size()!=0||hScro.size()!=0)&&keyCode==KeyEvent.KEYCODE_BACK){
			Scro.goback();
			hScro.goback();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	
	
	
	/*
	 Config_hesSize 

	 锁定我的大小，使用设置的大小

	 自动根据横竖屏改变大小

	 */
	final public static class Config_hesSize extends Config_Size2<PageHandler>
	{
		
		public int WindowHeight=600, WindowWidth=600;
		
		@Override
		public void ConfigSelf(PageHandler target)
		{
			int Wheight=MeasureWindowHeight(target.mWindow);
			if(flag==Configuration.ORIENTATION_PORTRAIT){
				WindowWidth=WindowHeight=(int)(width*0.9);
			}
			else if(flag==Configuration.ORIENTATION_LANDSCAPE){
				WindowWidth=(int)(width*0.7);
				WindowHeight= (int)(height*0.3);
			}	
			if (Wheight < WindowHeight)
				WindowHeight=Wheight;
			trim(target.mWindow,WindowWidth,WindowHeight);
			//立即设置Window大小
		}

		//每次change，改变我的大小，即我自己和滚动条的大小
		public void onChange(PageHandler target,int src){
		    trim(target,width,height);
			trim(target.Scro,width,height);
		    trim(target.hScro,width,height);
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
	@Override
	protected void onConfigurationChanged(Configuration newConfig)
	{
		/*
		  横竖屏切换了 ，改变我的大小
		  为什么要手动改变大小呢？
		  
		  因为我设置的是固定的宽高，在旋转屏幕时，屏幕坐标轴会旋转，此时原屏幕的宽变为高，原来的高变为宽，但View宽高不变！
		*/
		super.onConfigurationChanged(newConfig);
	    getConfig().change(this,newConfig.orientation);
	}
	
	//一顿操作后，PageHandler所有成员都分配好了空间
	final class HandlerCreator extends Creator<PageHandler>
	{
		
		public HandlerCreator(int i){
			super(i);
		}

		public void init(PageHandler target,View tmp)
		{
			target. Configer = new Config_hesView();
			target.config = new Config_hesSize();
			target.ViewBuilder = new Domean();
			target.Scro = tmp.findViewById(R.id.Scro);
			target.hScro = tmp.findViewById(R.id.hScro);
			target.mEditGroupPages = tmp.findViewById(R.id.mPages);
			target.mWindow = tmp.findViewById(R.id.mWindow);
		}

	}
	
	// 如何配置View
	final class Config_hesView implements Level<PageHandler>
	{

		@Override
		public void clearConfig(PageHandler target)
		{
			// TODO: Implement this method
		}


		@Override
		public void config(PageHandler target)
		{
			target. mWindow.setBackgroundColor(Colors.Bg);
			target. mWindow.setDivider(null);
			target. mWindow.setOnItemClickListener(new onMyWindowClick());
			target. mEditGroupPages.setOnTouchListener(new onTouch());
		}

		@Override
		public void ConfigSelf(PageHandler target)
		{
			config(target);
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
		final class onTouch implements OnTouchListener
		{
			@Override
			public boolean onTouch(View p1, MotionEvent p2)
			{
				return PageHandler.this.onTouchEvent(p2);
			}
		}
	}
	
	
	/*
	  在View被加入页面时，可以进行额外配置
	*/
	public static interface ViewBuiler extends OnTouchListener{
		
		public void eatView(View v, String name)
		
	}
	final class Domean implements ViewBuiler
	{

		@Override
		public boolean onTouch(View p1, MotionEvent p2)
		{
			if(p2.getPointerCount()==2&&p2.getHistorySize()!=0){
				Scro.setCanScroll(false);
				hScro.setCanScroll(false);
				if (((Math.pow(Math.abs(p2.getX(0) - p2.getX(1)), 2)+Math.pow(Math.abs(p2.getY(0) - p2.getY(1)), 2))
					>
					(Math.pow(Math.abs(p2.getHistoricalX(0, p2.getHistorySize() - 1) - p2.getHistoricalX(1, p2.getHistorySize() - 1)), 2)	+Math.pow( Math.abs(p2.getHistoricalY(0, p2.getHistorySize() - 1) - p2.getHistoricalY(1, p2.getHistorySize() - 1)), 2)))
					){
					trimXel(p1,1.05f,1.05f);
				}
				else{
					trimXel(p1,0.95f,0.95f);
				}
				ViewGroup.LayoutParams pa = p1.getLayoutParams();
				trim((View) p1.getParent(),pa.width,pa.height);
			}
			if(p2.getAction()==MotionEvent.ACTION_UP||p2.getAction()==MotionEvent.ACTION_DOWN){
				Scro.setCanScroll(true);
				hScro.setCanScroll(true);
			}
			return true;
		}
		
		@Override
		public void eatView(View v, String name)
		{
			if(!(v instanceof OnTouchListener)) //View已经实现OnTouchListener，不用我啦
			    v. setOnTouchListener(this);
			Config_Size2 config = (HasAll.Config_Size2) getConfig();
			trim(v,config.width,config.height);
			
			if(v instanceof ImageView)
				eatImageView((ImageView)v,name);
			if(v instanceof EditGroup)
				eatEditGroup((EditGroup)v,name);
		}
		
		public void eatImageView(ImageView v,String name){
			Bitmap map = BitmapFactory.decodeFile(name);
			v.setImageBitmap(map);
		}
		public void eatEditGroup(EditGroup Group,String name){
			myRet ret = new myRet(name);
			String text = ret.r("UTF-8");
			Group.getEditBuilder().insert(0,text);
		}
	}

	/*
	  我的外部类啊，请帮我实现主要功能吧
	*/
	public static interface IneedBuilder{
		
		public ViewBuiler getViewBuilder()
		
		public void setViewBuilder(ViewBuiler b)
	}
	
	public static interface requestWithPageHandler{
		
		public void addEdit(String name)
		
		public void addView(View v, String name)
		
		public PageHandler getPages()
		 
	}
	
}
