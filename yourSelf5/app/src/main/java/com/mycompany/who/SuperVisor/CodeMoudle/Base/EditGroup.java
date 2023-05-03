package com.mycompany.who.SuperVisor.CodeMoudle.Base;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.mycompany.who.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import com.mycompany.who.Edit.Base.Share.Share2.*;
import com.mycompany.who.Edit.Base.Share.Share3.*;
import com.mycompany.who.Edit.Base.Share.Share4.*;
import com.mycompany.who.Edit.CodeEdit.*;
import com.mycompany.who.Edit.ListenerVistor.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View3.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;


/*
 为提升编辑器效率，增加EditGroup
 
 编辑器卡顿主要原因是单个编辑器文本过多造成的计算刷新卡顿
 解决办法：限制单个编辑器的行，并添加多个编辑器形成编辑器组来均分文本，使效率平衡

 编辑器卡顿二大原因是由于宽高过大导致与父元素分配的空间冲突，导致父元素多次测量来确定子元素大小，进而的测量时间过长
 解决办法：文本变化时计算编辑器的宽高并手动扩展父元素大小，只要使父元素可以分配的空间永远大于子元素大小，就不会再多次测量了
 另外的，除EditText外，其它的View应尽量设置为固定大小，这可以减少测量时间(EditText会自己计算宽高)

 编辑器卡顿三大原因是编辑器onDraw时间过长，主要还是它的文本多，每次都要Draw全部的文本，太浪费了
 解决办法：根据当前位置，计算出编辑器能展示的范围，然后onDraw时用clipRect明确绘制范围，将超出的部分放弃绘制
 
*/

/*
 我什么也不知道，我只完善了Edit的功能，管理一组的Edit以及如何操作它们
 使用装饰者模式，实现了EditListenerInfoUser接口，但返回的Info其实是内部实现了EditListenerInfoUser成员的Info
 通常，我返回的Info是CodeEdit的，EditLine的Info默认不返回，因为您应该无需操作行
 
*/
public class EditGroup extends HasAll implements IlovePool,EditListenerInfoUser,OnClickListener,OnLongClickListener,OnItemClickListener
{
	
	public static int MaxLine=2000,OnceSubLine=0;
	public static int ExpandWidth=1500,ExpandHeight=2000;
	
	private Int EditFlag=new Int();
    private Int EditDrawFlag=new Int();
	private Int historyId;
	private CodeEdit.EditChroot root;
	private EditGroupListenerInfo Info;

	private ScrollBar Scro;
	private HScrollBar hScro;
	private LinearLayout ForEdit;
	private LinearLayout ForEditSon;
	private LineGroup EditLines;
	private ListView mWindow;

	private EditManipulator manipulator;
	private List<CodeEdit> EditList;
	private Stack<Stack<Int>> Last;
	private Stack<Stack<Int>> Next;
	private ThreadPoolExecutor pool;
	private EditFactory mfactory;
	
	
	public EditGroup(Context cont)
	{
		super(cont);	
	}
	public EditGroup(Context cont,AttributeSet attrs)
	{
		super(cont,attrs);
	}
	public void init(){
		super.init();
		Creator = new GroupCreator(R.layout.EditGroup);
		Creator.ConfigSelf(this);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj)||(obj!=null && obj instanceof EditGroup && ((View)obj).getTag().equals(getTag())))
			return true;
		return false;
	}
	
	public View getScro(){
		return Scro;
	}
	public View getHscro(){
		return hScro;
	}
	public ViewGroup getForEdit(){
		return ForEdit;
	} 
	public ViewGroup getForEditSon(){
		return ForEditSon;
	}
	public LineGroup getEditLine(){
		return EditLines;
	}
	public ListView getWindow(){
		return mWindow;
	}

	public EditManipulator getEditManipulator()
	{
		return manipulator;
	}
	public List<CodeEdit> getEditList()
	{
		return EditList;
	}
	public CodeEdit getHistoryEdit()
	{
		return EditList.get(historyId.get());
	}
	@Override
	public ThreadPoolExecutor getPool()
	{
		return pool;
	}
	
	@Override
	public void setPool(ThreadPoolExecutor pool)
	{
		this.pool = pool;
		getEditManipulator().setPool(pool);
	}
	public void setEditFactory(EditFactory factory)
	{
		if (factory != null)
		    mfactory = factory;	
	}

	@Override
	public EditListenerInfo getInfo()
	{
		if(Info!=null){
		    Info.CodeInfo=(CodeEdit.CodeEditListenerInfo) getEditManipulator().getInfo();
		    Info.LineInfo=(EditLine.EditLineListenerInfo) EditLines.getInfo();
		}
		return Info;
	}
	@Override
	public void setInfo(EditListenerInfo i)
	{
		if(i instanceof EditGroupListenerInfo){
			Info=(EditGroup.EditGroupListenerInfo) i;
			if(Info==null){
				getEditManipulator().setInfo(null);
				EditLines.setInfo(null);
			}
			else{
			    getEditManipulator().setInfo(Info.CodeInfo);
			    EditLines.setInfo(Info.LineInfo);
			}
		}
	}

	@Override
	public void trimListener(){}
	
	@Override
	public void clearListener(){}
	

	/* AddEdit只要调用一次，后面的Edit会跟随第一个的配置 */
	public void AddEdit(String name)
	{
		RCodeEdit Edit= creatAEdit(name);
		Edit.index.set(EditList.size());
		EditList.add(Edit);
		getForEditSon().addView(Edit);
	}
	/* 为了安全，不允许调用 */
	final private void AddEditAt(int index)
	{
		RCodeEdit Edit= creatAEdit("");
		Edit.index.set(index);
		EditList.add(index, Edit);
		getForEditSon().addView(Edit, index);
		reIndex();
	}

	/* 创建一个Edit */
	final private RCodeEdit creatAEdit(String name)
	{
		RCodeEdit Edit;
		//每个Edit都要配置，但只能内部使用的Clip和Click让我配置给它们吧
		if (EditList.size() == 0)
		{
			//第一个编辑器是get的，然后添加Clip
	        Edit = new RCodeEdit(getContext(), mfactory.getEdit(this));
			mfactory.configEdit(Edit, name, this);
			Edit.getCanvaserList().getList().add(getOneClipCanvaser());
		}
		else
		{
			//剩下的编辑器会复制第一个的Clip，因此不再添加，至于名字嘛...
			CodeEdit E = EditList.get(0);
			Edit = new RCodeEdit(getContext(),E);
			EditListenerInfo Info= Edit.getInfo();
			Edit.setInfo(null);  //不允许重复添加Listener
			mfactory. configEdit(Edit,"."+E.getLuagua(), this);
			Edit.setInfo(Info);
		}
		
		Edit.setOnClickListener(this);//组内的每个编辑器都设置Click
		//Edit.setOnLongClickListener(this);//组内的每个编辑器都设置LongClick
		Edit.compareChroot(root); //设置root
		//Edit.setTarget(this);//设置target，将事件冒泡给EditGroup
		//Edit.setId(Edit.hashCode());//拥有id的控件系统自动保存状态
		return Edit;
	}

	/* 重新排列Edit的下标 */
	final private void reIndex()
	{
		for (int i=0;i < EditList.size();++i)
		{
			RCodeEdit e = (EditGroup.RCodeEdit) EditList.get(i);
			if (e.index.get() != i)
				e.index.set(i);
		}
	}


	/*关键代码*/
	public void trimToFather()
	{
		//编辑器的大小变化了，将父元素的大小扩大到比编辑器更大，方便测量与布局
		size size = getEditManipulator().WAndH();
		int height=size.end + ExpandHeight;
		int width=size.start + ExpandWidth;
		Config_Size2. trim(getForEditSon(), width, height);
		Config_Size2. trim(getForEdit(), width + EditLines.maxWidth(), height);
		//为两个Edit的父元素扩展空间，一个ForEdit，一个ForEditSon
		//无需为Scrollview扩展空间，因为它本身就是用于滚动子元素超出自己范围的部分的，若扩展了就不能滚动了
		
		EditGroup.Config_hesSize config = (EditGroup.Config_hesSize) getConfig();
		config.EditWidth = width;
		config.EditHeight = height;
		//记得实际大小
	}


	final class RCodeEdit extends CodeEdit
	{

		private Int index;	
		private boolean can;
		//如果不是无关紧要的，别直接赋值，最后其实会在构造对象时赋值，等同于在构造函数最后赋值

		public RCodeEdit(Context cont)
		{
			super(cont);
			can = true;
			index = new Int();
		}
		public RCodeEdit(Context cont, CodeEdit Edit)
		{
			super(cont, Edit);
			can = true;
			index = new Int();
		}

		@Override
		protected void onPutUR(EditDate.Token token)
		{
			Last.peek().push(index);
			//监听器优先调用，所以Last会先开一个空间让我push
			//每一轮次onTextChanged执行后紧跟其后再开一个空间
		}		

		@Override
		protected void onBeforeTextChanged(CharSequence str, int start, int count, int after)
		{
			if (!isDraw && !isUR && !isFormat && EditFlag.get() == 0 && (Last.size() == 0 || Last.peek().size() != 0))
				Last.push(new Stack<Int>());  
			//从第一个调用onTextChanged的编辑器开始，之后的一组的联动修改都存储在同一个Stack
			//让第一个编辑器先开辟一个空间，待之后存储
			
			super.onBeforeTextChanged(str, start, count, after);
		}

		@Override
		protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
		{
			
			if (!can)	
				return;
			//在构造对象前，会调用一次onTextChanged

			if (IsModify != 0 || IsModify2)
				return ;
			//已经被修改，不允许再修改

			Log.w("onTextChanged", "My index is " + index.get());

			if(EditFlag.get()==0)
				trimToFather();    //第一个编辑器扩展大小
			/*
			   编辑器的大小变化了，将父元素的大小扩大到比编辑器更大，方便测量与布局
			   注意onTextChange优先于onMesure()调用，并且当前什么事也没做，此时设置最好
			   因为本次事件流未结束，所以EditText的数据未刷新，直接getHeight()是错误的
			   因此，我自己写了几个函数来测宽高，函数是通过文本来计算的，由于onTextChanged是文本变化后调用的，所以文本是对的
			  
			   但是，为什么我要将trimToFather交给第一个编辑器？
			   这是因为，在下面的代码中，会调用super.onTextChanged()
			   在其中，若有超长的文本，会自动换行，就出现与行号对不上，显示的大小就出问题，那么通过文本计算大小就是错的了
			   
			*/
			
			EditFlag.add();		
			if (lengthAfter != 0 && text.toString().indexOf('\n', start) != -1 )
				sendOutLineToNext(text, start, lengthBefore, lengthAfter);		
			//在某次插入后，若超出最大的行数，截取之后的部分添加到编辑器列表中的下个编辑器开头	
			else
				super.onTextChanged(text, start, lengthBefore, lengthAfter);
			EditFlag.less();

			if (EditFlag.get() == 0)
			{
				int line = getEditManipulator().calaEditLines();
				EditLines. reLines(line);	
				//最后一个编辑器单独计算行，这个太卡了，得优化一下
				Log.w("注意！此消息一次onTextChanged中只出现一次", "trimToFather：" + ((Config_hesSize)config).width + " " + ((Config_hesSize)config).height + " and reLines:" + line + " and Stack size：" + Last.size() + " 注意，Stack Size不会太大");		
			}
			
		}

		/* 在本次输入后，将自己内部超出的行截取到下个编辑器开头 */
		protected void sendOutLineToNext(CharSequence text, int start, int lengthBefore, int lengthAfter)
		{
			/*关键代码*/
			int lineCount= getLineCount();
			
			if (lineCount > MaxLine)
			{
				boolean need = true;
				Editable editor = getText();
				int selfIndex = index.get();		
				size j = subLines(MaxLine + 1 - OnceSubLine); 
				//为提升效率，若超出行数，额外截取OnceSubLine行，使当前编辑器可以有一段时间的独自编辑状态
				//MaxLine+1是指从MaxLine之后的一行的起始开始截
				j.start--;
				//连带着把MaxLine行的\n也截取
				
				CharSequence src = editor.subSequence(j.start, j.end); 
				++IsModify; 
				editor.delete(j.start, j.end);	
				--IsModify;

				if (start + lengthAfter < j.start){
					super.onTextChanged(text, start, lengthBefore, lengthAfter);		
					need = false; //下个编辑器插入的内容不用染色了
				}
				else{
				    super.onTextChanged(text, start, lengthBefore, j.start - start);	
				}
				//截取后，只把本次未被截取的前半部分染色，
				//它可能在MAX行之内，即正常染色
				//也可能在MAX行之外，即只染色start～MAX行之间

				if(CodeEdit.getLineCount(src.toString())>MaxLine){
					//大段文本需要插入，必须使用dispatchTextBlock
					j.set(selfIndex,editor.length());
					getEditManipulator().dispatchTextBlock(j,src);
					return;
				}
				
				if (EditList.size() - 1 == selfIndex){
					AddEdit("");
				}
				else if (EditList.get(selfIndex + 1).getLineCount() + (lineCount - MaxLine) > MaxLine){
					AddEditAt(selfIndex + 1);
				}
				//若无下个编辑器，则添加一个
				//若有下个编辑器，但它的行也不足，那么在我之后添加一个
				
				CodeEdit Edit = EditList.get(selfIndex + 1);
				if(!need){
					Edit.IsDraw(true);//不用染色了
				}
				Edit.getText().insert(0, src);
				Edit.IsDraw(false);
				//之后将截取的字符添加到编辑器列表中的下个编辑器开头，即MAX行之后的
				//下个编辑器也可以将自己超出的行截取到下下个编辑器
				//不难看出，这样递归回调，最后会回到第一个编辑器
			}
			else{
			    super.onTextChanged(text, start, lengthBefore, lengthAfter);
				//否则正常调用
			}
			
		}

		@Override
	 	public size calc(EditText Edit)
		{
			size pos=Calc(((RCodeEdit)Edit), EditGroup.this);
			return pos;
		}
		
	}

	protected size Calc(RCodeEdit Edit, EditGroup self)
	{
		//测量并修改Window大小
		EditGroup.Config_hesSize config = (EditGroup.Config_hesSize)self. getConfig();
		config.ConfigSelf(self);

		//请求测量
		self.historyId = Edit.index;
		//本次窗口谁请求，单词给谁
		int offset=Edit.getSelectionStart();
		int xlen = self.getEditManipulator().calaEditHeight(Edit.index.get());
		size pos = Edit.getScrollCursorPos(offset, getHscro().getScrollX(), getScro().getScrollY() - xlen);

		pos.start += self.EditLines.maxWidth();
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

	
	protected final class ClipCanvaser extends EditCanvaserListener
	{
		
		@Override
		public void afterDraw(EditText self, Canvas canvas, TextPaint paint, size pos){}

		/* 提升效率，不想用可以remove */
		@Override
		public void onDraw(EditText self, Canvas canvas, TextPaint paint, size pos)
		{	
			/*关键代码*/
			Rect rect =selfRect(self);
			addRect(rect,1f);  //扩大范围，以便下次滚动时可见
			canvas.clipRect(rect);
			//clipRect可以指示一块相对于自己的矩形区域，超出区域的部分会被放弃绘制

			if(EditDrawFlag.get()==0){
				//第一个编辑器还要遍历所有其它编辑器，并显示
				EditDrawFlag.set(EditList.size()); //当前还有size个编辑器要显示
				Int id = historyId;
				historyId=((RCodeEdit)self).index;
				for(CodeEdit e: EditList){
					//如果是第一个编辑器，则不用重新绘制
					if(((RCodeEdit)e).index.get()!=historyId.get())
						e.invalidate();
				}
				historyId=id;
			}
			EditDrawFlag.less();
			//一个编辑器绘制完成了，将Flag--，当Flag==0，则所有编辑器绘制完成了
		}

		/* 计算编辑器在可视区域中的自己的范围 */
		public Rect selfRect(EditText self)
		{
			int index = ((RCodeEdit)self).index.get();
			EditGroup.Config_hesSize config = (EditGroup.Config_hesSize) getConfig();
			int EditTop=getEditManipulator().calaEditHeight(index); //编辑器较ForEdit的顶部位置
			int SeeTop = getScro().getScrollY(); //可视区域较ForEdit的顶部位置
			int SeeLeft = getHscro().getScrollX();//可视区域较ForEdit的左边位置

			int left = SeeLeft - EditLines.maxWidth();
			//编辑器左边是当前可视区域左边减EditLines的宽
			int top = SeeTop - EditTop;
			//编辑器顶部为从0开始至可视区域顶部的偏移
			int right = config.width + left;
			//编辑器右边是左边加一个可视区域的宽
			int bottom= top+ config.height;
			//编辑器底部是上面加一个可视区域的高
			return new Rect(left,top,right,bottom);
		}
		
		/* 扩大Rect的范围 */
		public void addRect(Rect rect,float x){
			float width = rect.width()*x/2;
			float height = rect.height()*x/2;
			rect.left -= width;
			rect.right += width;
			rect.top -= height;
			rect.bottom += height;
		}

	}
	protected EditCanvaserListener getOneClipCanvaser()
	{
		//一直不知道，为什么明明EditCanvaserListener需要EditGroup内部的成员，却还允许返回并作为其它Edit的监听器
		//在调试时，发现EditCanvaserListener内部还有一个this$0成员，原来这个成员就是EditGroup
		//原来每个内部类，还有一个额外的成员，就是指向外部类实例的指针，在创建一个内部类对象时，内部类对象就与外部类实例绑定了
		//其实不安全
		return new ClipCanvaser();
	}
		
	
	@Override
	public void onClick(View p1)
	{
		historyId = ((RCodeEdit) p1).index;
		if (getWindow() != null)
			getWindow().setX(-9999);
	}

	@Override
	public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
	{
		//如果点击了就插入单词并关闭窗口
		WordAdpter adapter = (WordAdpter) p1.getAdapter();
		IconX icon = (IconX) adapter.getItem(p3);
		if(historyId!=null){
			CodeEdit Edit = getHistoryEdit();
			Edit.insertWord(icon.getName(), Edit.getSelectionStart(), adapter.posFlag(p3));
		}
		getWindow().setX(-9999);
	}

	@Override
	public boolean onLongClick(View p1)
	{
		// TODO: Implement this method
		return false;
	}
	
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		if(ev.getPointerCount()==2){
		    requestDisallowInterceptTouchEvent(false);
			getParent().requestDisallowInterceptTouchEvent(true);
			//缩放手势，父元素一定不能拦截我，我一定要拦截子元素
		}
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		if(ev.getPointerCount()==2){
			return true;
			//缩放手势，拦截事件进行缩放
		}
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent p2)
	{	
	    super.onTouchEvent(p2);
		//事件拦截到自己手里，开始缩放，并消耗事件
		if(p2.getPointerCount()==2&&p2.getHistorySize()!=0){	
		    Edit E = EditList.get(0);
		    float is = onTouchToZoom.Iszoom(p2);
		    if(is>1)
			    getEditManipulator().zoomBy(E.TextSize+0.25f);	
		    else if(is<1)
			    getEditManipulator().zoomBy(E.TextSize-0.25f); 
		}
		return true;
	}

	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		//如果返回了，先让滚动到上个位置
		int keyCode = event.getKeyCode();
		if((Scro.size()!=0||hScro.size()!=0)&&keyCode==KeyEvent.KEYCODE_BACK){
		    Scro.goback();
		    hScro.goback();
		    return true;
		}
		return super.dispatchKeyEvent(event);
	}
	
	
	final public static class LineGroup extends LinearLayout implements EditLine.LineSpiltor,EditMoudle.Sizer,EditListenerInfoUser
	{

		public static int MaxLine = 5000;
		private int lineCount;
		private List<EditLine> LineList;

		public LineGroup(Context cont){
			super(cont);
			init();
		}
		public LineGroup(Context cont,AttributeSet attrs){
			super(cont,attrs);
			init();
		}
		protected void init(){
			LineList = new ArrayList<>();
			addEditLine();
			setOrientation(VERTICAL);
		}

		public List<EditLine> getLineList(){
			return LineList;
		}

		public void addEditLine()
		{
			EditLine Line = creatEditLine();
			LineList.add(Line);
			addView(Line);
		}
		protected EditLine creatEditLine()
		{
			EditLine Line;
			int size = LineList.size();
			if(size>0){
				Line = LineList.get(size-1);
				Editable editor = Line.getText();
				Line.getText().delete(editor.length()-1,editor.length());
				Line = new EditLine(getContext(),Line);	
			}
			else{
				Line = new EditLine(getContext());
			}
			Line.setKeyListener(null);
			return Line;
		}

		@Override
		public void reLines(int line)
		{
			int caline= line-lineCount;
			if(caline<0){
				delLines(-caline);
			}
			else if(caline>0){
				addLines(caline);
			}
		}

		@Override
		public void addLines(int count)
		{
			lineCount+=count;
			while(count>0)
			{
				EditLine Line = LineList.get(LineList.size()-1);
				int hasLine = Edit.getLineCount(Line.getText().toString());
				int freeLine = MaxLine - hasLine;
				if(freeLine>count){
					Line.addLines(count);
					count -= count;
				}
				else{
					Line.addLines(freeLine);
					count -= freeLine;
					addEditLine();
				}
			}
			onLineChange(lineCount-count,0,count);
		}

		@Override
		public void delLines(int count)
		{
			lineCount-=count;
			while(count>0){
				int index = LineList.size()-1;
				EditLine Line = LineList.get(index);
				int hasLine = Edit.getLineCount(Line.getText().toString());
				int delLine = hasLine>count ? count:hasLine;
				if(hasLine==delLine){
					LineList.remove(index);
					removeViewAt(index);
				}
				else{
					Line.delLines(delLine);
				}
				count-=delLine;
			}
			onLineChange(lineCount+count,count,0);
		}

		@Override
		public int getLineCount(){
			return lineCount;
		}

		@Override
		public void onLineChange(int start, int before, int after){
			CodeBlock.Config_Size2.trim(this,maxWidth(),maxHeight());
		}

		@Override
		public int maxWidth(){	
			float TextSize = getTextSize();
			int count = String.valueOf(lineCount).length()+1;
			return (int)(count*TextSize);
		}

		@Override
		public int maxHeight()
		{
			int lineHeight = getLineHeight();
			return (lineCount*lineHeight);
		}

		@Override
		public size WAndH(){
			return new size(maxWidth(),maxHeight());
		}

		@Override
		public EditListenerInfo getInfo(){
			return LineList.get(0).getInfo();
		}

		@Override
		public void setInfo(EditListenerInfo Info)
		{
			for(EditLine Line:LineList){
				Line.setInfo(Info);
			}
		}

		@Override
		public void trimListener(){
			LineList.get(0).trimListener();
		}

		@Override
		public void clearListener(){
			LineList.get(0).clearListener();
		}

		public void zoomBy(){

		}
		
		public float getTextSize(){
			return LineList.get(0).getTextSize();
		}
		public int getLineHeight(){
			return LineList.get(0).getLineHeight()+1;
		}
		
	}
	
	
	//通过EditBuilder直接操作Edit
	final public class EditManipulator
	{
		private void calaIndex(int index,size start)
		{
			//将start转换为 起始编辑器下标+起始位置
			for (CodeEdit e:EditList)
			{
				if (index - e.getText().length() <= 0)
				{
					start.start = ((RCodeEdit)e).index.get();
					start.end = index;
					return;
				}
				index -= e.getText().length();
				//每次index减当前Edit.len，若长度小于0，则就是当前Edit，index就是start
			}
		}
		private void calaRange(int start, int end,size s,size e)
		{
			//将start～end的范围转换为 起始编辑器下标+起始位置～末尾编辑器下标+末尾位置
			calaIndex(start,s);
			calaIndex(end,e);
		}
		
		/*  只管分发文本块，不管怎样，大段文本块都可给我  */
		void dispatchTextBlock(size s,CharSequence str)
		{
			SpannableStringBuilder text = new SpannableStringBuilder(str);
			int index = s.start;
			Editable E = EditList.get(index).getText();
			str = E.subSequence(s.end,E.length());
			text.append(str);
			E.delete(s.end,E.length());
			//要插入的编辑器，从要插入的位置之后截取了文本，添加要插入的文本之后
			//在之后创建一些编辑器，并均分文本
			
			String src = text.toString();
			int toIndex = CodeEdit.getLineCount(text.toString())/MaxLine+1+index;
			int nowLine = 0;
			//从下个编辑器开始，一直需要插入到能承受文本溢出的那个编辑器
			//每次向后截取MaxLine，并插入到刚创建的编辑器中
			for(index+=1;index<=toIndex;++index){
				AddEditAt(index);
				s = CodeEdit.subLines(nowLine,nowLine+=MaxLine,src);
				str = text.subSequence(s.start,s.end);
				EditList.get(index).setText(str);
			}
		}
		public void clearText()
		{
			String lua = getLuagua();
			EditList.clear();
			ForEditSon.removeAllViews();
			EditLines.reLines(1);
			AddEdit("."+lua);
		}
		public void setText(CharSequence str)
		{		
			clearText();
			if(CodeEdit.getLineCount(str.toString())>MaxLine)
			    dispatchTextBlock(new size(0,0),str);
			else
				EditList.get(0).setText(str);
		}
		public void append(CharSequence str)
		{
			size start = new size();
			calaIndex(calaEditLen(),start);
			if(CodeEdit.getLineCount(str.toString())>MaxLine)
			    dispatchTextBlock(start,str);
			else
			    EditList.get(start.start).append(str);
		}
		public void insert(int index,CharSequence str)
		{
			size start = new size();
			calaIndex(calaEditLen(),start);
			if(CodeEdit.getLineCount(str.toString())>MaxLine)
			    dispatchTextBlock(start,str);
			else
			    EditList.get(start.start).getText().insert(start.end,str);
		}
		public void delete(int start,int end)
		{
			DoForAnyOnce d= new DoForAnyOnce(){

				@Override
				public void doOnce(int start, int end, CodeEdit Edit)
				{
				    Edit.getText().delete(start,end);
				}
			};
		    d.dofor(start,end);
		}
		public void replace(int start,int end,CharSequence str){
			delete(start,end);
			insert(start,str);
		}
		
		/*
		  一组的Edit共享Info，对于Info的操作都是内存空间的修改
		*/
		public void setLuagua(String luagua){
			EditList.get(0).setLuagua(luagua);
		}
		public void setListener(EditListener li){
			EditList.get(0).getInfo().addAListener(li);
		}
		public void delListener(EditListener li){
			EditList.get(0).getInfo().delAListener(li);
		}
		
		/*
		  一组的Edit的Info成员却是指针
		*/
		public void setInfo(EditListenerInfo Info){
			for(CodeEdit E:EditList)
			    E.setInfo(Info);
		}
		public void setEditBuilder(EditBuilder f){
			for(CodeEdit E:EditList)
			    E.setEditBuilder(f);
		}
		public void setWindow(ListView Window){
			for(CodeEdit E:EditList)
			    E.setWindow(Window);
		}
		public void setPool(ThreadPoolExecutor pool){
			for(CodeEdit E:EditList)
			    E.setPool(pool);
		}
		public void setWordLib(Words Lib){
			for(CodeEdit E:EditList)  
			    E.setWordLib(Lib);
		}
		
		public String getLuagua(){
			return EditList.get(0).getLuagua();
		}
		public EditListenerInfo getInfo(){
			return EditList.get(0).getInfo();
		}
		public EditBuilder getEditBuilder(){
			return EditList.get(0).getEditBuilder();
		}
		public Words getWordLib(){
			return EditList.get(0).getWordLib();
		}
		
		public void lockThem(boolean is){
			for(CodeEdit E:EditList)
			    E.lockSelf(is);
		}
		public void zoomBy(float x){
			for(CodeEdit E: EditList){
				E.zoomBy(x);
			}
			trimToFather();
		}
		
		public void IsModify(boolean is){
			root.IsModify = is;
			compareChroot(root);
		}
		public void IsDraw(boolean is){
			root.isDraw=is;
			compareChroot(root);
		}
		public void IsFormat(boolean is){
			root.isFormat = is;
			compareChroot(root);
		}
		public void IsComplete(boolean is){
			root.isComplete = is;
			compareChroot(root);
		}
		public void IsUR(boolean is){
			root.isUR = is;
			compareChroot(root);
		}
		public void compareChroot(CodeEdit.EditChroot f){
			root.compare(f);
			for(CodeEdit E: EditList)    
			    E.compareChroot(f);
		}
		public CodeEdit.EditChroot getRoot(){
			return root;
		}
		
		public void reSAll(int start,int end,final String want,final String to)
		{
			DoForAnyOnce d= new DoForAnyOnce(){

				@Override
				public void doOnce(int start, int end, CodeEdit Edit)
				{
				    Edit.reSAll(start,end,want,to);
				}
			};
			d.dofor(start,end);
		}
		
		public List<Integer> SearchWord(String want)
		{
			StringBuilder b = new StringBuilder();
			for(CodeEdit E:EditList){
				b.append(E.getText().toString());
			}
			return String_Splitor.indexsOf(want,b.toString());
		}
		
		public void setSpan(size[] poses,Object[] spans)
		{
			for(int i=0;i<spans.length;++i){
				size pos = poses[i];
				final Object span = spans[i];
				DoForAnyOnce d= new DoForAnyOnce(){
					
					@Override
					public void doOnce(int start, int end, CodeEdit Edit)
					{
						Edit.getText().setSpan(start,end,span,Colors.SpanFlag);
					}
				};
				d.dofor(pos.start,pos.end);
			}
		}
		public<T> void clearSpan(int start,int end,final Class<T>type)
		{
			DoForAnyOnce d= new DoForAnyOnce(){

				@Override
				public void doOnce(int start, int end, CodeEdit Edit)
				{
				    Edit.clearSpan(start,end,type);
				}
			};
			d.dofor(start,end);
		}
		
		public void reDraw(int start, int end)
		{
			DoForAnyOnce d= new DoForAnyOnce(){

				@Override
				public void doOnce(int start, int end, CodeEdit Edit)
				{
					Runnable run = Edit.ReDraw(start, end);
					if(pool!=null)
				        pool.execute(run);
					else
						run.run();
				}
			};
			d.dofor(start,end);
		}
		
		public List<Future> prepare(int start,int end)
		{
			final List<Future> results = new LinkedList<>();
			DoForAnyOnce d= new DoForAnyOnce(){

				@Override
				public void doOnce(int start, int end, CodeEdit Edit)
				{
					Runnable run = Edit.Prepare(start,end,Edit.getText().toString());
					if(pool!=null){
				        Future f = pool.submit(run);
						results.add(f);
					}
					else
						run.run();
				}

			};
		    d.dofor(start,end);
			return results;
		}
		public void GetString(StringBuilder b,SpannableStringBuilder bu){
			for(CodeEdit E : EditList){
				E.GetString(b,bu);
			}
		}
		public CharSequence subSequence(int start,int end)
		{
			final SpannableStringBuilder text = new SpannableStringBuilder();
			DoForAnyOnce d= new DoForAnyOnce(){

				@Override
				public void doOnce(int start, int end, CodeEdit Edit)
				{
				   CharSequence tmp = Edit.getText().subSequence(start,end);
				   text.append(tmp);
				}

			};
			d.dofor(start,end);
			return text;
		}
		
		public void Format(int start, int end)
		{
			DoForAnyOnce d= new DoForAnyOnce(){

				@Override
				public void doOnce(int start, int end, CodeEdit Edit)
				{
					 Edit.Format(start, end);
				}
			};
			d.dofor(start,end);
		}

		public Stack<Int> Uedo()
		{
			//与顺序无关的Uedo，它只存储一轮次的修改的编辑器下标，具体顺序由编辑器内部管理
	        //Bug: 多个编辑器之间会各自管理，因此任何一个的修改可能与另一个无关，造成单次Uedo不同步，但一直Uedo下去，结果是一样的
			if (Last.size() < 1)
				return null;
			Stack<Int> last= Last.pop();
			Next.push(last);
			for (Int l:last)
				EditList.get(l.get()).Uedo();
			EditLines. reLines(calaEditLines());
			trimToFather();
			return last;
		}
		public Stack<Int> Redo()
		{
			//与顺序无关的Redo，它只存储一轮次的修改的编辑器下标，具体顺序由编辑器内部管理
			if (Next.size() < 1)
				return null;
			Stack<Int> next= Next.pop();
			Last.push(next);
			for (Int l:next)
				EditList.get(l.get()).Redo();
			EditLines.reLines(calaEditLines());
			trimToFather();
			return next;
		}

		public abstract class DoForAnyOnce
		{
			public void dofor(int start,int end)
			{
				size s = new size();
				size e = new size();
				calaRange(start, end,s,e);
				
				//单个编辑器的情况下
				if(s.start==e.start){
					doOnce(s.end,e.end,EditList.get(s.start));
					return;
				}
				
				//多个编辑器的情况下
				doOnce(s.end, EditList.get(s.start).getText().length(), EditList.get(s.start++));
				//第一个编辑器的开头是s.end，结尾是它的长度
				for (;s.start < e.start;s.start++)
				{
					doOnce(0, EditList.get(s.start).getText().length(), EditList.get(s.start));
					//中间编辑器的开头是0,结尾是它的长度
				}
			    doOnce(0, e.end, EditList.get(e.start));
				//最后一个编辑器的开头是0，结尾是e.end
			}
			
			public void doRecursion(int start,int end)
			{
				size s = new size();
				size e = new size();
				calaRange(start, end,s,e);
				//单个编辑器的情况下
				if(s.start==e.start){
					doOnce(s.end,e.end,EditList.get(s.start));
					return;
				}
				Recursion(s,e,s.start);
			}
			
			/* 递归进行post，如果单个任务需要进行长时间前台操作必须使用 */
			private void Recursion(final size s,final size e, final int index)
			{
				if(index<s.start||index>e.start){
					return;
					//index已经递归到了末尾
				}
					
				Runnable run = new Runnable(){

					@Override
					public void run()
					{
						int start,end;
						CodeEdit Edit = EditList.get(index);
						
						if(index==s.start){
							start = s.start;
							end = Edit.getText().length();
							//第一个编辑器的开头是s.end，结尾是它的长度
						}
						else if(index==e.start){
							start = 0;
							end = e.end;
							//最后一个编辑器的开头是0，结尾是e.end
						}
						else{
							start = 0;
							end = Edit.getText().length();
							//中间编辑器的开头是0,结尾是它的长度
						}
						
						doOnce(start,end,Edit);
						Recursion(s,e,index+1);
						//执行完doOnce后再调用Recursion去post下个index的任务
						//这样每执行完一个任务，主线程都可以先顺着执行下去，缓口气，接下来继续执行下个任务
					}
				};
				
				post(run);
				//递归地抛并执行任务
			}
			
			public abstract void doOnce(int start, int end, CodeEdit Edit)
			
		}	

		public int calaEditLen()
		{
			int len = 0;
			for (Edit e:EditList)
			    len+=e.getText().length();
			return len;
		}
		public int calaEditLines()
		{
			int line=0;
			for (Edit e:EditList)
				line += e.getLineCount();
			return line;
		}
		public int calaEditHeight(int index)
		{
			int height=0;
			for (int i=0;i < index;++i)
				height += EditList.get(i).maxHeight();
			return height;
		}
		public int calaEditWidth()
		{
			int width=0;
			for (Edit e:EditList)
			{
				int w=e.maxWidth();
				if (w > width)
					width = w;
			}
			return width;
		}
		public size WAndH()
		{
			//获取最宽和最高
			size size=new size();
			for (Edit e:EditList)
			{
				size tmp=e.WAndH();
				size.end += tmp.end;
				if (size.start < tmp.start)
					size.start = tmp.start;
			}
			return size;
		}
		
		public CodeEdit getFocusEdit(){
			return (CodeEdit)ForEditSon.getFocusedChild();
		}

	}
	private void creatEditManipulator()
	{
		if (manipulator == null)
			manipulator = new EditManipulator();
	}
	
    //创造Edit的工厂，当然可能没什么用，毕竟不是真创建，而是复制
	public static interface EditFactory
	{
		public CodeEdit getEdit(EditGroup self)
		
		public void configEdit(CodeEdit Edit, String name, EditGroup self)
	}
	//默认的工厂
	final static class Factory implements EditGroup.EditFactory
	{
		
		@Override
		public CodeEdit getEdit(EditGroup self)
		{
			return new CodeEdit(self.getContext());
		}

		@Override
		public void configEdit(CodeEdit Edit, String name, EditGroup self)
		{
			Edit.setPool(self. pool);
			Edit.setWindow(self. getWindow());
			Share.setEdit(Edit, name);
		}

	}
	private void creatEditFactory()
	{
		if (mfactory == null)
			mfactory = new Factory();
	}
	
	
	public static class EditGroupListenerInfo implements EditListenerInfo
	{

		CodeEdit.CodeEditListenerInfo CodeInfo;
		EditLine.EditLineListenerInfo LineInfo;
		
		@Override
		public boolean addAListener(EditListener li)
		{
			if(CodeInfo.addAListener(li)||LineInfo.addAListener(li))
				return true;
			return false;
		}

		@Override
		public boolean delAListener(EditListener li)
		{
			if(CodeInfo.delAListener(li)||LineInfo.delAListener(li))
				return true;
			return false;
		}

		@Override
		public EditListener findAListener(String name)
		{
			EditListener li = CodeInfo.findAListener(name);
			EditListener li2 = LineInfo.findAListener(name);
			return li!=null?li:li2;
		}
		
		public EditListenerInfo getCodeInfo(){
			return CodeInfo;
		}
		public EditListenerInfo getLineInfo(){
			return LineInfo;
		}
		
		@Override
		public boolean addListenerTo(EditListener li, int toIndex)
		{
			return false;
		}

		@Override
		public boolean delListenerFrom(int fromIndex)
		{
			return false;
		}

		@Override
		public EditListener findAListener(int fromIndex)
		{
			return null;
		}
		
	}
	private void creatInfo(){
		if(Info!=null)
			Info = new EditGroupListenerInfo();
	}


	//一顿操作后，EditGroup所有成员都分配好了空间
	final static class GroupCreator extends Creator<EditGroup>
	{

		public GroupCreator(int resid){
			super(resid);
		}

		public void init(EditGroup Group,View root)
		{
			Group.Configer = new Config_hesView();
			Group.config = new Config_hesSize();
			Group.EditList = new ArrayList<>();
			Group.Last = new Stack<>();
			Group.Next = new Stack<>();
			Group.root = new CodeEdit.EditChroot();
			Group.creatInfo();
			Group.creatEditManipulator();
			Group.creatEditFactory();
			init2(Group,root);
		}
		private static void init2( EditGroup Group,View root)
		{	
		    Group. Scro = root.findViewById(R.id.Scro);
			Group. hScro = root.findViewById(R.id.hScro);
		   	Group. ForEdit = root.findViewById(R.id.ForEdit);
			//Group. EditLines = root.findViewById(R.id.EditLine);
			Group. ForEditSon = root.findViewById(R.id.ForEditSon);
			Group. mWindow = root.findViewById(R.id.mWindow);
			
			Group.EditLines = new LineGroup(Group.getContext());
			
			//重新设置一个不同的id，防止xml文件被多次加载，内存中有多个相同id的View
			//Group.setId(Group.hashCode());
			//Group.mWindow.setId(Group.mWindow.hashCode());
			//Group.Scro.setId(Group.Scro.hashCode());
			//Group.hScro.setId(Group.hScro.hashCode());
			//Group.ForEdit.setId(Group.ForEdit.hashCode());
			//Group.ForEditSon.setId(Group.ForEditSon.hashCode());
			//Group.EditLines.setId(Group.EditLines.hashCode());
		}	
		
	}

	
	// 如何配置View，不要做创建工作
	final static class Config_hesView implements Level<EditGroup>
	{
		
		@Override
		public void clearConfig(EditGroup target){}

		@Override
		public void ConfigSelf(EditGroup target)
		{
			target.ForEdit.addView(target.EditLines,0);
			config(target);
		}
		
		public void config(EditGroup target)
		{
			target. ForEdit.setOrientation(LinearLayout.HORIZONTAL);
			target. ForEditSon.setOrientation(LinearLayout.VERTICAL);
			target. mWindow.setBackgroundColor(Colors.Bg2);
			target. mWindow.setDivider(null);
			target. mWindow.setOnItemClickListener(target);
			target. hScro.setTouchInter(true);
			//启用横向翻页
		}
		
	}
	
	
	//配置我的大小
	final public static class Config_hesSize extends Config_Size2<EditGroup>
	{
		
		public int WindowHeight=600, WindowWidth=600;
		public int EditWidth,EditHeight;

		@Override
		public void ConfigSelf(EditGroup target)
		{
			int Wheight=MeasureWindowHeight(target.mWindow);
			if(flag==Configuration.ORIENTATION_PORTRAIT){
				WindowWidth=WindowHeight=(int)(height*0.475);
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
		public void onChange(EditGroup target,int src){
		    trim(target,width,height);
			trim(target.Scro,width,height);
		    trim(target.hScro,width,height);
		}
		
		//测量窗口高度
		public static int MeasureWindowHeight(ListView mWindow)
		{
			int height=0;
			int i;
			ListAdapter adapter = mWindow.getAdapter();
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
	
	
	/*  告诉持有我的外部类，要使用我，您必须拥有如下这些  */
	
    public static interface IneedFactory
	{

		public void setEditFactory(EditFactory factory)

		public EditFactory getEditFactory()

	}
	
	/*  第一次知道，interface可以继承多个interface，天哪  */
	
	public static interface requestWithEditGroup extends IneedFactory,IlovePool{}

	
}
