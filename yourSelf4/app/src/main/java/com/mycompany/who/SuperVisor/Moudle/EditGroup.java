package com.mycompany.who.SuperVisor.Moudle;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.mycompany.who.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Base.Edit.*;
import com.mycompany.who.Edit.ListenerVistor.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import com.mycompany.who.Edit.Share.Share1.*;
import com.mycompany.who.Edit.Share.Share2.*;
import com.mycompany.who.Edit.Share.Share3.*;
import com.mycompany.who.SuperVisor.Moudle.Config.*;
import com.mycompany.who.SuperVisor.Moudle.Share.*;
import com.mycompany.who.View.*;
import java.util.*;
import java.util.concurrent.*;
import android.widget.AdapterView.*;
import com.mycompany.who.Share.*;


/*
  为提升编辑器效率，增加EditGroup
  编辑器卡顿主要原因是单个编辑器文本过多造成的计算刷新卡顿
  解决办法：限制单个编辑器的行，并添加多个编辑器形成编辑器组来均分文本，使效率平衡

  编辑器卡顿二大原因是由于宽高过大导致与父元素分配的空间冲突，导致父元素多次测量来确定子元素大小，进而的测量时间过长
  解决办法：文本变化时计算编辑器的宽高并手动扩展父元素大小，只要使父元素可以分配的空间永远大于子元素大小，就不会再多次测量了
  另外的，除EditText外，其它的View应尽量设置为固定大小，这可以减少测量时间

  编辑器卡顿三大原因是编辑器onDraw时间过长，主要还是它的文本多，每次都要Draw全部的文本，太浪费了
  解决办法：根据当前位置，计算出编辑器能展示的范围，然后onDraw时用clipRect明确绘制范围，将超出的部分放弃绘制

*/

/*
  我什么也不知道
  我只完善了Edit的功能，管理一组的Edit以及如何操作它们
*/
public class EditGroup extends HasAll implements CodeEdit.IlovePool,CodeEdit.IneedWindow,OnClickListener,OnItemClickListener
{
	
	public static int MaxLine=2000,OnceSubLine=0;
	public static int ExpandWidth=1500,ExpandHeight=2000;
	protected Int EditFlag=new Int();
    protected Int EditDrawFlag=new Int();
	protected Int historyId;
	protected CodeEdit.EditChroot root;

	protected ScrollBar Scro;
	protected HScrollBar hScro;
	protected LinearLayout ForEdit;
	protected LinearLayout ForEditSon;
	protected EditLine EditLines;
	protected ListView mWindow;

	private EditBuilder builder;
	private List<CodeEdit> EditList;
	private Stack<Stack<Int>> Last;
	private Stack<Stack<Int>> Next;
	private ThreadPoolExecutor pool=null;
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
	
	public ScrollView getScro(){
		return Scro;
	}
	public HorizontalScrollView getHscro(){
		return hScro;
	}
	public ViewGroup getForEdit(){
		return ForEdit;
	} 
	public ViewGroup getForEditSon(){
		return ForEditSon;
	}
	public EditLine getLines(){
		return EditLines;
	}
	public ListView getWindow(){
		return mWindow;
	}

	public EditBuilder getEditBuilder()
	{
		return builder;
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
	public void setWindow(ListView Window)
	{
		mWindow = Window;
		getEditBuilder().setWindow(Window);
	}
	@Override
	public void setPool(ThreadPoolExecutor pool)
	{
		this.pool = pool;
		getEditBuilder().setPool(pool);
	}
	public void setEditFactory(EditFactory factory)
	{
		if (factory != null)
		    mfactory = factory;	
	}


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
	final protected RCodeEdit creatAEdit(String name)
	{
		RCodeEdit Edit;
		//每个Edit都要配置，但只能内部使用的Clip和Click让我配置给它们吧
		if (EditList.size() == 0)
		{
			//第一个编辑器是get的，然后添加Clip
	        Edit = new RCodeEdit(getContext(), mfactory.getEdit(this));
			Edit.getCanvaserList().add(getOneClipCanvaser());
			mfactory. configEdit(Edit, name, this);
		}
		else
		{
			//剩下的编辑器会复制第一个的Clip，因此不再添加，至于名字嘛...
			CodeEdit E = EditList.get(0);
			Edit = new RCodeEdit(getContext(),E );
			EditListenerInfo Info= E.getInfo();
			E.setInfo(null);  //不允许重复添加Listener
			mfactory. configEdit(Edit,"."+E.getLuagua(), this);
			E.setInfo(Info);
		}
		//组内的每个编辑器都设置Click
		Edit.setOnClickListener(this);
		Edit.compareChroot(root); //设置root
		Edit.setTarget(this);
		Edit.setId(Edit.hashCode());
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
	protected void trimToFather()
	{
		//编辑器的大小变化了，将父元素的大小扩大到比编辑器更大，方便测量与布局
		size size = getEditBuilder().WAndH();
		int height=size.end + ExpandHeight;
		int width=size.start + ExpandWidth;
		Config_Size2. trim(getForEditSon(), width - EditLines.maxWidth(), height);
		Config_Size2. trim(getForEdit(), width, height);
		//为两个Edit的父元素扩展空间，一个ForEdit，一个ForEditSon
		//无需为Scrollview扩展空间，因为它本身就是用于滚动子元素超出自己范围的部分的，若扩展了就不能滚动了
		
		EditGroup.Config_hesSize config = (EditGroup.Config_hesSize) getConfig();
		config.EditWidth = width;
		config.EditHeight = height;
		//记得实际大小
	}


	final class RCodeEdit extends CodeEdit implements Interfaces.BubbleEvent
	{

		public Int index;	
		private Interfaces.BubbleEvent Target;
		private boolean can;
		//别直接赋值，最后其实会在构造对象时赋值，等同于在构造函数中赋值

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
			if (text.toString().indexOf('\n', start) != -1 && lengthAfter != 0)
				sendOutLineToNext(text, start, lengthBefore, lengthAfter);		
			//在某次插入后，若超出最大的行数，截取之后的部分添加到编辑器列表中的下个编辑器开头	
			else
				super.onTextChanged(text, start, lengthBefore, lengthAfter);
			EditFlag.less();

			if (EditFlag.get() == 0)
			{
				int line = getEditBuilder().calaEditLines();
				EditLines. reLines(line);	//最后一个编辑器单独计算行
				Log.w("注意！此消息一次onTextChanged中只出现一次", "trimToFather：" + ((Config_hesSize)config).width + " " + ((Config_hesSize)config).height + " and reLines:" + line + " and Stack size：" + Last.size() + " 注意，Stack Size不会太大");		
			}

		}

		protected void sendOutLineToNext(CharSequence text, int start, int lengthBefore, int lengthAfter)
		{
			/*关键代码*/
			boolean need = true;
			int lineCount= getLineCount();
			if (lineCount > MaxLine)
			{
				//为提升效率，若超出行数，额外截取OnceSubLine行，使当前编辑器可以有一段时间的独自编辑状态
				size j = subLines(MaxLine + 1 - OnceSubLine); //MaxLine+1是指从MaxLine之后的一行的起始开始截
				j.start--; //连带着把MaxLine行的\n也截取
				CharSequence src = getText().subSequence(j.start, j.end); 
				IsModify++;
				getText().delete(j.start, j.end);	
				IsModify--;

				if (start + lengthAfter < j.start){
					super.onTextChanged(text, start, lengthBefore, lengthAfter);		
					need = false; //下个编辑器插入的内容不用染色了
				}
				else
				    super.onTextChanged(text, start, lengthBefore, j.start - start);	
				
				//截取后，只把本次未被截取的前半部分染色，
				//它可能在MAX行之内，即正常染色
				//也可能在MAX行之外，即只染色start～MAX行之间

				if (EditList.size() - 1 == index.get())
					AddEdit("");
				else if (EditList.get(index.get() + 1).getLineCount() + (lineCount - MaxLine) > MaxLine)
					AddEditAt(index.get() + 1);
				
				//若无下个编辑器，则添加一个
				//若有下个编辑器，但它的行也不足，那么在我之后添加一个
				CodeEdit Edit = EditList.get(index.get() + 1);
				if(!need)
					Edit.IsDraw(true);//不用染色了
				Edit.getText().insert(0, src);
				Edit.IsDraw(false);
				//之后将截取的字符添加到编辑器列表中的下个编辑器开头，即MAX行之后的
				//不难看出，这样递归回调，最后会回到第一个编辑器

			}
			else
			{
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
		
		@Override
		public boolean onKeyUp(int keyCode, KeyEvent event)
		{
			//只有获取焦点的View，才能被调用，并继续分发，这里Edit可以获取焦点，将其实现交给外部类
		    return BubbleKeyEvent(keyCode,event);
		}
		@Override
		public boolean onTouchEvent(MotionEvent event)
		{
			//只有被touch的View，才能被调用，并继续分发，这里Edit可以被touch，将其实现交给外部类
			invalidate();
			return BubbleMotionEvent(event);
		}
	
		//向上冒泡，冒泡至EditGroup
		@Override
		public boolean BubbleKeyEvent(int keyCode, KeyEvent event)
		{
			boolean is = super.onKeyUp(keyCode,event);
			if(Target!=null)
			    return Target.onKeyUp(keyCode,event);
			return is;
		}
		@Override
		public boolean BubbleMotionEvent(MotionEvent event)
		{
			boolean is = super.onTouchEvent(event);
			if(Target!=null)  
			    return Target.onTouchEvent(event);
			return is;
		}
		
		@Override
		public void setTarget(Interfaces.BubbleEvent target)
		{
			Target = target;
		}
		@Override
		public Interfaces.BubbleEvent getTarget()
		{
			return Target;
		}
		
	}

	protected size Calc(RCodeEdit Edit, EditGroup self)
	{
	
		//测量并修改Window大小
		EditGroup.Config_hesSize config = (EditGroup.Config_hesSize) getConfig();
		config.ConfigSelf(this);

		//请求测量
		self.historyId = Edit.index;
		//本次窗口谁请求，单词给谁
		int offset=Edit.getSelectionStart();
		int xlen = self.getEditBuilder().calaEditHeight(Edit.index.get());
		size pos = ((CodeEdit)Edit).getScrollCursorPos(offset, getHscro().getScrollX(), getScro().getScrollY() - xlen);

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

	protected final class ClipCanvaser extends EditCanvaserListener
	{

		@Override
		public void afterDraw(EditText self, Canvas canvas, TextPaint paint, Rect Cursor_bounds){}

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

		public Rect selfRect(EditText self){

			int index = ((RCodeEdit)self).index.get();
			//计算编辑器在可视区域中的自己的范围

			EditGroup.Config_hesSize config = (EditGroup.Config_hesSize) getConfig();
			int EditTop=getEditBuilder().calaEditHeight(index); //编辑器较ForEdit的顶部位置
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
		Icon icon = adapter.getItem(p3);
		if(historyId!=null){
			CodeEdit Edit = getHistoryEdit();
			Edit.insertWord(icon.getName(), Edit.getSelectionStart(), icon.getflag());
		}
		getWindow().setX(-9999);
	}
	
	@Override
	public boolean BubbleMotionEvent(MotionEvent p2)
	{
		return super.BubbleMotionEvent(p2);
	}	
	@Override
	public boolean onTouchEvent(MotionEvent p2)
	{	
	    if(p2.getPointerCount()==2&&p2.getHistorySize()!=0){
	        Edit E = EditList.get(0);
	        boolean is = onTouchToZoom.Iszoom(p2);
		    if(is)
		        getEditBuilder().zoomBy(E.TextSize+0.25f);	
		    else	
			    getEditBuilder().zoomBy(E.TextSize-0.25f); 
		}
		return true;
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
		return false;
	}
	
	
	//通过EditBuilder直接操作Edit
	final public class EditBuilder
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
		
		private void dispatchTextBlock(size s,CharSequence str){
			SpannableStringBuilder text = new SpannableStringBuilder(str);
			int index = s.start;
			Editable E = EditList.get(index).getText();
			str = E.subSequence(s.end,E.length());
			text.append(str);
			E.delete(s.end,E.length());
			//要插入的编辑器，从要插入的位置之后截取了文本，添加要插入的文本之后
			//在之后创建一些编辑器，并均分文本
			
			int toIndex = CodeEdit.getLineCount(text.toString())/MaxLine+1+index;
			int nowLine = 0;
			//从下个编辑器开始，一直需要插入到能承受文本溢出的那个编辑器
			//每次向后截取MaxLine，并插入到刚创建的编辑器中
			for(index+=1;index<=toIndex;++index){
				AddEditAt(index);
				s = CodeEdit.subLines(nowLine,nowLine+=MaxLine,text.toString());
				str = text.subSequence(s.start,s.end);
				EditList.get(index).setText(str);
			}
		}
		public void clearText(){
			String lua = getLuagua();
			EditList.clear();
			ForEditSon.removeAllViews();
			EditLines.reLines(1);
			AddEdit("."+lua);
		}
		public void setText(CharSequence str){		
			clearText();
			dispatchTextBlock(new size(0,0),str);
		}
		public void append(CharSequence str){
			size start = new size();
			calaIndex(calaEditLen(),start);
			if(CodeEdit.getLineCount(str.toString())>MaxLine)
			    dispatchTextBlock(start,str);
			else
			    EditList.get(start.start).append(str);
		}
		public void insert(int index,CharSequence str){
			size start = new size();
			calaIndex(calaEditLen(),start);
			if(CodeEdit.getLineCount(str.toString())>MaxLine)
			    dispatchTextBlock(start,str);
			else
			    EditList.get(start.start).getText().insert(start.end,str);
		}
		public void delete(int start,int end){
			
			DoForAnyOnce d= new DoForAnyOnce(){

				@Override
				void doOnce(int start, int end, CodeEdit Edit)
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
		public void setLuagua(String luagua)
		{
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
		public void setFactory(EditListenerFactory f){
			for(CodeEdit E:EditList)
			    E.setFactory(f);
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
		public void setSearchBit(int bit){
			for(CodeEdit E:EditList)  
			    E.setSearchBit(bit);
		}
		
		public String getLuagua(){
			return EditList.get(0).getLuagua();
		}
		public EditListenerInfo getInfo(){
			return EditList.get(0).getInfo();
		}
		public EditListenerFactory getFactory(){
			return EditList.get(0).getFactory();
		}
		public Words getWordLib(){
			return EditList.get(0).getWordLib();
		}
	    public int getSearchBit(){
			return EditList.get(0).getSearchBit();
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
			root = f;
			for(CodeEdit E: EditList)    
			    E.compareChroot(f);
		}
		public CodeEdit.EditChroot getRoot(){
			return root;
		}
		
		public void reSAll(int start,int end,final String want,final String to){
			DoForAnyOnce d= new DoForAnyOnce(){

				@Override
				void doOnce(int start, int end, CodeEdit Edit)
				{
				    Edit.reSAll(start,end,want,to);
				}
			};
			d.dofor(start,end);
		}
		
		public List<Integer> SearchWord(String want){
			StringBuilder b = new StringBuilder();
			for(CodeEdit E:EditList){
				b.append(E.getText().toString());
			}
			return String_Splitor.indexsOf(want,b.toString());
		}
		
		public void setSpan(size[] poses,Object[] spans){
			
			for(int i=0;i<spans.length;++i){
				size pos = poses[i];
				final Object span = spans[i];
				DoForAnyOnce d= new DoForAnyOnce(){
					
					@Override
					void doOnce(int start, int end, CodeEdit Edit)
					{
						Edit.getText().setSpan(start,end,span,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				};
				d.dofor(pos.start,pos.end);
			}
		}
		public<T> void clearSpan(int start,int end,final Class<T>type){
			DoForAnyOnce d= new DoForAnyOnce(){

				@Override
				void doOnce(int start, int end, CodeEdit Edit)
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
				void doOnce(int start, int end, CodeEdit Edit)
				{
				    Edit.reDraw(start, end);
				}
			};
			d.dofor(start,end);
		}
		
		public List<Future> prepare(int start,int end)
		{
			final List<Future> results = new ArrayList<>();
			DoForAnyOnce d= new DoForAnyOnce(){

				@Override
				void doOnce(int start, int end, CodeEdit Edit)
				{
					Future f = Edit.prepare(start,end,Edit.getText().toString());
					results.add(f);
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
				void doOnce(int start, int end, CodeEdit Edit)
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
				void doOnce(int start, int end, CodeEdit Edit)
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

		abstract class DoForAnyOnce
		{
			public void dofor(int start,int end)
			{
				size s = new size();
				size e = new size();
				calaRange(start, end,s,e);
				
				//单个编辑器的情况下
				if(s.start==e.start){
					doOnce(s.end,e.end,EditList.get(s.start));
				}
				
				//多个编辑器的情况下
				doOnce(s.end, EditList.get(s.start).getText().length(), EditList.get(s.start++));
				//第一个编辑器的开头是start.end，结尾是它的长度
				for (;s.start < e.start;s.start++)
				{
					doOnce(0, EditList.get(s.start).getText().length(), EditList.get(s.start));
					//中间编辑器的开头是0,结尾是它的长度
				}
			    doOnce(0, e.end, EditList.get(e.start));
				//最后一个编辑器的开头是0，结尾是end.end
			}
			abstract void doOnce(int start, int end, CodeEdit Edit)
		}

		public int calaEditLen(){
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
	private void creatEditBuilder()
	{
		if (builder == null)
			builder = new EditBuilder();
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
			Edit.setEditLine(self.getLines());
			com.mycompany.who.Share.Share.setEdit(Edit, name);
		}

	}
	private void creatEditFactory()
	{
		if (mfactory == null)
			mfactory = new Factory();
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
		    Group.creatEditBuilder();
			Group.creatEditFactory();
			Group.setId(Group.hashCode());
			init2(Group,root);
		}
		private static void init2( EditGroup Group,View root)
		{	
		    Group. Scro = root.findViewById(R.id.Scro);
			Group. hScro = root.findViewById(R.id.hScro);
		   	Group. ForEdit = root.findViewById(R.id.ForEdit);
			Group. EditLines = root.findViewById(R.id.EditLine);
			Group. ForEditSon = root.findViewById(R.id.ForEditSon);
			Group. mWindow = root.findViewById(R.id.mWindow);
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
			config(target);
		}
		public void config(EditGroup target)
		{
			target. EditLines.setFocusable(false);
			target. ForEdit.setOrientation(LinearLayout.HORIZONTAL);
			target. ForEditSon.setOrientation(LinearLayout.VERTICAL);
			target. mWindow.setBackgroundColor(Colors.Bg);
			target. mWindow.setDivider(null);
			target. mWindow.setOnItemClickListener(target);
			CodeEdit.Enabled_Drawer = true;
			CodeEdit.Enabled_Complete = true;
			CodeEdit.Enabled_Format = true;
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
	
	
	/*  告诉持有我的外部类，要使用我，您必须拥有如下这些  */
	
    public static interface IneedFactory
	{

		public void setEditFactory(EditFactory factory)

		public EditFactory getEditFactory()

	}

}