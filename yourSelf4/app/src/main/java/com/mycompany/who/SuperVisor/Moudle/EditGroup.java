package com.mycompany.who.SuperVisor.Moudle;

import android.content.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Base.Edit.*;
import com.mycompany.who.Edit.Share.Share1.*;
import java.util.*;
import java.util.concurrent.*;
import com.mycompany.who.Edit.Share.Share4.*;
import android.text.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import android.graphics.*;
import com.mycompany.who.Edit.ListenerVistor.*;
import com.mycompany.who.*;
import com.mycompany.who.SuperVisor.Moudle.Config.*;
import com.mycompany.who.SuperVisor.Moudle.Config.Interfaces.*;
import com.mycompany.who.Edit.Share.Share3.*;

public class EditGroup extends HasAll implements CodeEdit.IlovePool,CodeEdit.IneedWindow,OnTouchListener
{
	
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
	 我只在适时扩展大小
	 我需要Window，请让外部类给我Window，并尽可能地自己展示
  */
	public static int MaxLine=2000,OnceSubLine=0;
	public static int ExpandWidth=1500,ExpandHeight=2000;
	protected Int EditFlag=new Int();
	protected Int historyId;
	protected CodeEdit.EditChroot root;

	protected LinearLayout ForEdit;
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
	public boolean onTouch(View p1, MotionEvent p2)
	{
		return false;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj!=null && obj instanceof EditGroup && ((View)obj).getTag().equals(getTag()))
			return true;
		return false;
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
	public ListView getWindow()
	{
		return mWindow;
	}
	public EditLine getLines(){
		return EditLines;
	}

	public void setWindow(ListView Window)
	{
		mWindow = Window;
		builder.setWindow(Window);
	}
	public void setPool(ThreadPoolExecutor pool)
	{
		this.pool = pool;
		builder.setPool(pool);
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
		ForEdit.addView(Edit);
	}
	/* 为了安全，不允许调用 */
	final private void AddEditAt(int index)
	{
		RCodeEdit Edit= creatAEdit("");
		Edit.index.set(index);
		EditList.add(index, Edit);
		ForEdit.addView(Edit, index);
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
			root = Edit.getChroot(); //复制root
		}
		else
		{
			//剩下的编辑器会复制第一个的Clip，因此不再添加，至于名字嘛...
			CodeEdit E = EditList.get(0);
			Edit = new RCodeEdit(getContext(),E );
			mfactory. configEdit(Edit,"."+E.getLuagua(), this);
			Edit.compareChroot(root); //设置root
		}
		//组内的每个编辑器都设置Click
		Edit.setOnClickListener(getOneClick());
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
		size size = builder.WAndH();
		int height=size.end + ExpandHeight;
		int width=size.start + ExpandWidth;
		trim(ForEdit, width - EditLines.maxWidth(), height);
		trim(this, width, height);
		config.set(width,height,0,this);
		//为两个Edit的父元素扩展空间，一个ForEdit，一个this
		//无需为Scrollview扩展空间，因为它本身就是用于滚动子元素超出自己范围的部分的，若扩展了就不能滚动了
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

			Log.w("onTextChanged", "My index is " + index);

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
				int line = builder.calaEditLines();
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

	
	// 默认是以光标位置显示窗口，子类可以重写 
    protected size Calc(RCodeEdit Edit, EditGroup self)
	{
		historyId = Edit.index;
		return Edit.getCursorPos(Edit.getSelectionStart());
	}
	
	protected class Clip extends EditCanvaserListener
	{

		@Override
		public void afterDraw(EditText self, Canvas canvas, TextPaint paint, Rect Cursor_bounds)
		{
			// TODO: Implement this method
		}


		@Override
		public void onDraw(EditText self, Canvas canvas, TextPaint paint, Rect Cursor_bounds)
		{
			//默认啥也不做，子类可以重写
		}
	}
	protected EditCanvaserListener getOneClipCanvaser(){
		return null;
		//让孑类重写
	}
	
	//这个...
	protected class Click implements OnClickListener
	{
		@Override
		public void onClick(View p1)
		{
			historyId = ((RCodeEdit) p1).index;
			if (mWindow != null)
			    mWindow.setX(-9999);
		}
	}
	protected OnClickListener getOneClick(){
		return new Click();
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
		
		public void append(CharSequence str){
			size start = new size();
			calaIndex(calaEditLen(),start);
			EditList.get( start.start).append(str);
		}
		public void insert(int index,CharSequence str){
			size start = new size();
			calaIndex(calaEditLen(),start);
			EditList.get( start.start).getText().insert(start.end,str);
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
			root.isFormat = is;
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
		public void compareChroot(CodeEdit.EditChroot f){
			for(CodeEdit E: EditList)    
			    E.compareChroot(f);
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
		public void GetString(StringBuilder b,SpannableStringBuilder bu){
			for(CodeEdit E : EditList){
				E.GetString(b,bu);
			}
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
			Edit.setWindow(self. mWindow);
			Edit.setEditLine(self.getLines());
			com.mycompany.who.Share.Share.setEdit(Edit, name);
		}

	}
	private void creatEditFactory()
	{
		if (mfactory == null)
			mfactory = new Factory();
	}


	/*
	 将int类型的数据作为指针传递

	 对于同一个Int对象，可进行安全的读写操作

	 注意，若以指针传递，小心误修改对象，少使用set
	 */
	final public static class Int
	{

		private int date;

		public Int()
		{
			date = 0;
		}
		public Int(int d)
		{
			date = d;
		}

		public int get()
		{
			return date;
		}
		synchronized public void set(int d)
		{
			date = d;
		}
		synchronized public int add()
		{
			int before = date;
			++date;
			return before;
		}
		synchronized public int less()
		{
			int before = date;
			--date;
			return before;
		}
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
		    Group.creatEditBuilder();
			Group.creatEditFactory();
			init2(Group,root);
		}
		private static void init2( EditGroup Group,View root)
		{	
		   	Group. ForEdit = root.findViewById(R.id.ForEdit);
			Group. EditLines = root.findViewById(R.id.EditLine);
		}	
	}

	
	// 如何配置View，不要做创建工作
	final static class Config_hesView implements Level<EditGroup>
	{

		@Override
		public void clearConfig(EditGroup target)
		{
			// TODO: Implement this method
		}

		@Override
		public void ConfigSelf(EditGroup target)
		{
			config(target);
		}
		public void config(EditGroup target)
		{
			target. EditLines.setFocusable(false);
			target. ForEdit.setOrientation(LinearLayout.VERTICAL);
			CodeEdit.Enabled_Drawer = true;
			CodeEdit.Enabled_Complete = true;
			CodeEdit.Enabled_Format = true;
		}
	}
	//配置我的大小
	final public static class Config_hesSize implements Config_Size<EditGroup>
	{
		
		public int width,height;
		
		@Override
		public void ConfigSelf(EditGroup target)
		{
			target.trimToFather();
		}

		@Override
		public void set(int width, int height, int is, EditGroup target){
			this.width = width;
			this.height = height;
		}

		@Override
		public void change(EditGroup target,int is){}

		@Override
		public void onChange(EditGroup target,int src){}
	}
	
	
	/*  告诉持有我的外部类，要使用我，您必须拥有如下这些  */
	
    public static interface IneedFactory
	{

		public void setEditFactory(EditFactory factory)

		public EditFactory getEditFactory()

	}


}
