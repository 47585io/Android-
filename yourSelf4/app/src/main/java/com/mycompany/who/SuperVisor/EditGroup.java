package com.mycompany.who.SuperVisor;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.ListenerVistor.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import com.mycompany.who.Edit.Share.*;
import com.mycompany.who.SuperVisor.Config.*;
import java.util.*;
import java.util.concurrent.*;
import android.util.*;
import com.mycompany.who.Edit.Base.Edit.*;


public class EditGroup extends LinearLayout implements Configer<EditGroup>,CodeEdit.IlovePool
{
	
	//为提升编辑器效率，增加EditGroup
	//编辑器卡顿主要原因是单个编辑器文本过多造成的计算刷新卡顿
	//解决办法：限制单个编辑器的行，并添加多个编辑器形成编辑器组来均分文本，使效率平衡

	//编辑器卡顿二大原因是由于宽高过大导致与父元素分配的空间冲突，导致父元素多次测量来确定子元素大小，进而的测量时间过长
	//解决办法：文本变化时计算编辑器的宽高并手动扩展父元素大小，只要使父元素可以分配的空间永远大于子元素大小，就不会再多次测量了
	//另外的，除EditText外，其它的View应尽量设置为固定大小，这可以减少测量时间

	//编辑器卡顿三大原因是编辑器onDraw时间过长，主要还是它的文本多，每次都要Draw全部的文本，太浪费了
	//解决办法：根据当前位置，计算出编辑器能展示的范围，然后onDraw时用clipRect明确绘制范围，将超出的部分放弃绘制
	
	public static int MaxLine=2000,OnceSubLine=0;
	public static int ExpandWidth=1500,ExpandHeight=2000;
	protected int mWidth,mHeight;
	protected Int EditFlag=new Int();
	protected Int historyId;
	
	protected LinearLayout ForEdit;
	protected EditLine EditLines;
	protected ListView mWindow;

	private EditBuilder builder;
	private List<CodeEdit> EditList;
	private Stack<Stack<Int>> Last;
	private Stack<Stack<Int>> Next;
	private ThreadPoolExecutor pool=null;
	private EditFactory mfactory;
	
	EditGroup(Context cont)
	{
		super(cont);
		new EditGroupCreator().ConfigSelf(this); // 初始化成员
		new Config_hesViewLevel().ConfigSelf(this); // 配置层级
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (((View)obj).getTag().equals(getTag()))
			return true;
		return false;
	}

	
	public EditBuilder getEditBuilder(){
		return builder;
	}
	public List<CodeEdit> getEditList(){
		return EditList;
	}
	public CodeEdit getHistoryEdit(){
		return EditList.get(historyId.get());
	}
	@Override
	public ThreadPoolExecutor getPool()
	{
		return pool;
	}
	
	public wordIndex WAndH(){
		return new wordIndex(mWidth,mHeight,(byte)0);
	}
	
	public void setWindow(ListView Window){
		if(Window!=null)
		    mWindow=Window;
	}
	public void setPool(ThreadPoolExecutor pool){
		this.pool = pool;
		builder.setPool(pool);
	}
	public void setEditFactory(EditFactory factory){
		if(factory!=null)
		    mfactory=factory;
	}
	
	
	/* AddEdit只要调用一次，后面的Edit会跟随第一个的配置 */
	public void AddEdit(String name)
	{
		RCodeEdit Edit= creatAEdit(name);
		Edit.index.set( EditList.size());
		EditList.add(Edit);
		ForEdit.addView(Edit);
	}
	/* 为了安全，不允许调用 */
	private void AddEditAt(int index)
	{
		RCodeEdit Edit= creatAEdit("");
		Edit.index.set( index);
		EditList.add(index,Edit);
		ForEdit.addView(Edit,index);
		reIndex();
	}
	
	/* 创建一个Edit */
	protected RCodeEdit creatAEdit(String name)
	{
		RCodeEdit Edit;
		if (EditList.size() == 0)
		{
	        Edit = new RCodeEdit(getContext(),mfactory.getEdit(this));
			mfactory. configEdit(Edit, name,this);
			Edit.lines = EditLines;
		}
		else
		{
			Edit = new RCodeEdit(getContext(), EditList.get(0));
		}
		Edit.setOnClickListener(new Click());
		return Edit;
	}

	/* 重新排列Edit的下标 */
	private void reIndex(){
		for(int i=0;i<EditList.size();++i){
			RCodeEdit e = (EditGroup.RCodeEdit) EditList.get(i);
			if(e.index.get()!=i)
				e.index.set( i);
		}
	}
	
	

	/*关键代码*/
	protected void trimToFather()
	{
		//编辑器的大小变化了，将父元素的大小扩大到比编辑器更大，方便测量与布局
		wordIndex size = builder.WAndH();
		int height=size.end + ExpandHeight;
		int width=size.start + ExpandWidth;
		trim(ForEdit, width - EditLines.maxWidth(), height);
		trim(this, width, height);
		mWidth=width; 
		mHeight=height;
		//为两个Edit的父元素扩展空间，一个ForEdit，一个this
		//无需为Scrollview扩展空间，因为它本身就是用于滚动子元素超出自己范围的部分的，若扩展了就不能滚动了
	}
	final public static void trim(View Father, int width, int height)
	{
		//调整空间
		ViewGroup.LayoutParams p = Father.getLayoutParams();
		p.width = width;
		p.height = height;
		Father.setLayoutParams(p);
	}
	final public static void trimAdd(View Father, int addWidth, int addHeight)
	{
		ViewGroup.LayoutParams p = Father.getLayoutParams();
		p.width += addWidth;
		p.height += addHeight;
		Father.setLayoutParams(p);
	}
	final public static void trimXel(View Father, float WidthX, float HeightX)
	{
		ViewGroup.LayoutParams p = Father.getLayoutParams();
		p.width *= WidthX;
		p.height *= HeightX;
		Father.setLayoutParams(p);
	}


	final class RCodeEdit extends CodeEdit
	{

		public Int index;	
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
		public ListView getWindow()
		{
			return mWindow;
		}
		@Override
		protected void onBeforeTextChanged(CharSequence str, int start, int count, int after)
		{
			if(!isDraw&&!isUR&&!isFormat&& EditFlag.get()==0&& (Last.size()==0 || Last.peek().size()!=0))
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
					
			Log.w("onTextChanged", "My index is "+index);
			
			EditFlag.add();		
			if(text.toString().indexOf('\n',start)!=-1&&lengthAfter!=0)
				sendOutLineToNext(text, start, lengthBefore, lengthAfter);		
			    //在某次插入后，若超出最大的行数，截取之后的部分添加到编辑器列表中的下个编辑器开头	
			else
				super.onTextChanged(text,start,lengthBefore,lengthAfter);
			EditFlag.less();

			if (EditFlag.get() == 0)
			{
				int line = builder.calaEditLines();
				EditLines. reLines(line);	//最后一个编辑器单独计算行
				trimToFather();    //最后一个编辑器扩展大小
				
				Log.w("注意！此消息一次onTextChanged中只出现一次","trimToFather："+mWidth+" "+mHeight+ " and reLines:"+line+" and Stack size："+Last.size()+" 注意，Stack Size不会太大");
				//编辑器的大小变化了，将父元素的大小扩大到比编辑器更大，方便测量与布局
				//注意onTextChange优先于onMesure()调用，并且当前什么事也没做，此时设置最好
				//因为本次事件流未结束，所以EditText的数据未刷新，直接getHeight()是错误的
				//因此，我自己写了几个函数来测宽高，函数是通过文本来计算的，由于onTextChanged是文本变化后调用的，所以文本是对的
				
			}
		
		}

		protected void sendOutLineToNext(CharSequence text, int start, int lengthBefore, int lengthAfter)
		{
			/*关键代码*/
			
			int lineCount= getLineCount();
			if (lineCount > MaxLine)
			{
				//为提升效率，若超出行数，额外截取OnceSubLine行，使当前编辑器可以有一段时间的独自编辑状态
				wordIndex j = subLines(MaxLine+1-OnceSubLine); //MaxLine+1是指从MaxLine之后的一行的起始开始截
				j.start--; //连带着把MaxLine行的\n也截取
				String src = getText().toString().substring(j.start, j.end); 
				IsModify++;
				getText().delete(j.start, j.end);	
				IsModify--;
				
				if (start + lengthAfter < j.start)
					super.onTextChanged(text, start, lengthBefore, lengthAfter);		
				else
				{
				    super.onTextChanged(text, start, lengthBefore, j.start - start);	
				}	
				//截取后，只把本次未被截取的前半部分染色，
				//它可能在MAX行之内，即正常染色
				//也可能在MAX行之外，即只染色start～MAX行之间

				if (EditList.size() - 1 == index.get())
					AddEdit("");
				else if(EditList.get(index.get()+1).getLineCount()+(lineCount-MaxLine)>MaxLine){
					AddEditAt(index.get()+1);
				}
				//若无下个编辑器，则添加一个
				//若有下个编辑器，但它的行也不足，那么在我之后添加一个
				EditList.get(index.get() + 1).getText().insert(0, src);
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
	 	public wordIndex calc(EditText Edit)
		{
			wordIndex pos=Calc(((RCodeEdit)Edit),EditGroup.this);
			return pos;
		}

	}
	
	/*
	    默认是以光标位置显示窗口，子类可以重写
	*/
    protected wordIndex Calc(RCodeEdit Edit,EditGroup self){
		historyId = Edit.index;
		return Edit.getCursorPos(Edit.getSelectionStart());
	}
	

	
	final public class EditBuilder
	{
		//通过EditBuilder直接操作Edit
		private wordIndex start;
		private wordIndex end;

		public EditBuilder()
		{
			start = new wordIndex(0, 0, (byte)0);
			end = new wordIndex(0, 0, (byte)0);
		}
		private void calaIndex(int index)
		{
			//将start转换为 起始编辑器下标+起始位置
			for (CodeEdit e:EditList)
			{
				if (index - e.getText().length() < 0)
				{
					start.start = ((RCodeEdit)e).index.get();
					start.end = index;
					return;
				}
				index -= e.getText().length();
				//每次index减当前Edit.len，若长度小于0，则就是当前Edit，index就是start
			}
		}
		private void calaRange(int start, int end)
		{
			//将start～end的范围转换为 起始编辑器下标+起始位置～末尾编辑器下标+末尾位置
			calaIndex(start);
			calaIndex(end);
		}
		public void setLuagua(String luagua)
		{
			for (CodeEdit e:EditList)
			    e.setLuagua(luagua);
		}
		
		public void setPool(ThreadPoolExecutor pool)
		{
			for (CodeEdit Edit:EditList)
			    Edit.setPool(pool);
		}

		public void reDraw()
		{
			for (CodeEdit e:EditList)
			{
				e.reDraw(0, e.getText().length());
			}
		}
		public void reDraw(int start, int end)
		{
			calaRange(start, end);
			DoForAnyOnce d= new DoForAnyOnce(){

				@Override
				void doOnce(int start, int end, CodeEdit Edit)
				{
				    Edit.reDraw(start, end);
				}
			};
			d.dofor(this.start, this.end);
		}
		public void Format() 
		{
			Last.push(new Stack<Int>());
			for (CodeEdit e:EditList)
			{
			    e.Format(0, e.getText().length());
			}
		}
		public void Format(int start, int end)
		{
			Last.push(new Stack<Int>());
			
			calaRange(start, end);
			DoForAnyOnce d= new DoForAnyOnce(){

				@Override
				void doOnce(int start, int end, CodeEdit Edit)
				{
					Edit.reDraw(start, end);
				}
			};
			d.dofor(this.start, this.end);
		}
		public void Insert(int index)
		{
			calaIndex(index);
			EditList.get(start.start).Insert(start.end);
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
			return next;
		}

		abstract class DoForAnyOnce
		{
			public void dofor(wordIndex start, wordIndex end)
			{
				doOnce(start.end, EditList.get(start.start).getText().length(), EditList.get(start.start++));
				//第一个编辑器的开头是start.end，结尾是它的长度
				for (;start.start < end.start;start.start++)
				{
					doOnce(0, EditList.get(start.start).getText().length(), EditList.get(start.start));
					//中间编辑器的开头是0,结尾是它的长度
				}
				doOnce(0, end.end, EditList.get(end.start));
				//最后一个编辑器的开头是0，结尾是end.end
			}
			abstract void doOnce(int start, int end, CodeEdit Edit)
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
		public wordIndex WAndH()
		{
			//获取最宽和最高
			wordIndex size=new wordIndex();
			for (Edit e:EditList)
			{
				wordIndex tmp=e.WAndH();
				size.end += tmp.end;
				if (size.start < tmp.start)
					size.start = tmp.start;
			}
			return size;
		}

	}
	private void creatEditBuilder(){
		if( builder==null)
			builder=new EditBuilder();
	}
	
	

	@Override
	public void ConfigSelf(EditGroup target)
	{
		//任何时候，立刻配置
	}
	


	/*
	    一顿操作后，EditGroup所有成员都分配好了空间
	*/
	static class EditGroupCreator implements Configer<EditGroup>
	{

		@Override
		public void ConfigSelf(EditGroup Group)
		{
			init(Group);
			init2(Group.getContext(),Group);
		}

		private static void init(EditGroup Group)
		{
			Group. EditList = new ArrayList<>();
			Group. Last = new Stack<>();
			Group. Next = new Stack<>();
		    Group.creatEditBuilder();
			Group.creatEditFactory();
		}
		private static void init2(Context cont,EditGroup Group)
		{	
		   	Group. ForEdit = new LinearLayout(cont);
			Group. EditLines = new EditLine(cont);
		}	
	}

	/*
	    如何配置View层次结构
	*/
	static class Config_hesViewLevel implements Configer<EditGroup>
	{
		@Override
		public void ConfigSelf(EditGroup target)
		{
			target. addView(target.EditLines);
			target. addView(target.ForEdit);
			if(target.mWindow==null){
				target.mWindow=new ListView(target.getContext());
				target.addView(target. mWindow);
			}
			config(target);
		}
		protected void config(EditGroup target)
		{
			target. EditLines.setFocusable(false);
			target. ForEdit.setOrientation(LinearLayout.VERTICAL);
			CodeEdit.Enabled_Drawer=true;
			CodeEdit.Enabled_Complete=true;
			CodeEdit.Enabled_Format=true;
		}
	}

	
	public static interface EditFactory{
		public CodeEdit getEdit(EditGroup self)
		public void configEdit(CodeEdit Edit,String name,EditGroup self)
	}
	class Factory implements EditGroup.EditFactory
	{

		@Override
		public CodeEdit getEdit(EditGroup self)
		{
			return new CodeEdit(self.getContext());
		}

		@Override
		public void configEdit(CodeEdit Edit, String name, EditGroup self)
		{
			Edit.setPool(pool);
			com.mycompany.who.Share.Share.setEdit(Edit, name);
		}

	}
	private void creatEditFactory(){
		if(mfactory==null)
			mfactory=new Factory();
	}
	
	
	/*
	    将int类型的数据作为指针传递
	
		对于同一个Int对象，可进行安全的读写操作
		
		注意，若以指针传递，小心误修改对象，少使用set
	*/
	public static class Int{
		
		private int date;
		
		public Int(){
			date=0;
		}
		public Int(int d){
			date = d;
		}
		
		public int get(){
			return date;
		}
		synchronized public void set(int d){
			date = d;
		}
		synchronized public int add(){
			int before = date;
			++date;
			return before;
		}
		synchronized public int less(){
			int before = date;
			--date;
			return before;
		}
	}
	
	class Click implements OnClickListener
	{
		@Override
		public void onClick(View p1)
		{
			historyId = ((RCodeEdit) p1).index;
			if(mWindow!=null)
			    mWindow.setX(-9999);
		}
	}

	
/*

_________________________________________

   告诉持有我的外部类，要使用我，您必须拥有如下这些

 _________________________________________

*/

    public static interface IneedFactory{
		
		public void setEditFactory(EditFactory factory)
				
		public EditFactory getEditFactory()
		
	}
	
	public static interface IneedWindow{
	
		public ListView getWindow()
		
	}

	
	
}
