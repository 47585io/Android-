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


public class EditGroup extends LinearLayout
{

	//为提升编辑器效率，增加EditGroup
	//编辑器卡顿主要原因是单个编辑器文本过多造成的计算刷新卡顿
	//解决办法：限制单个编辑器的行，并添加多个编辑器形成编辑器组来均分文本，使效率平衡

	//编辑器卡顿二大原因是由于宽高过大导致与父元素分配的空间冲突，导致父元素多次测量来确定子元素大小，进而的测量时间过长
	//解决办法：文本变化时计算编辑器的宽高并手动扩展父元素大小，只要使父元素可以分配的空间永远大于子元素大小，就不会再多次测量了
	//另外的，除EditText外，其它的View应尽量设置为固定大小，这可以减少测量时间

	//编辑器卡顿三大原因是编辑器onDraw时间过长，主要还是它的文本多，每次都要Draw全部的文本，太浪费了
	//解决办法：根据当前位置，计算出编辑器能展示的范围，然后onDraw时用clipRect明确绘制范围，将超出的部分放弃绘制
	
	public int MaxLine=4000,OnceSubLine=1;
	public int ExpandWidth=1500,ExpandHeight=2000;
	public Config_hesSize config;
	public Config_hesEdit configEdit;
	private int EditFlag=0,EditDrawFlag=0;
	private int historyId;

	protected ScrollView EditScro;
	protected HorizontalScrollView EdithScro;
	protected LinearLayout ForEdit;
	protected LinearLayout ForEditSon;
	protected EditLine EditLines;
	protected ListView mWindow;

	private EditBuilder builder;
	private List<CodeEdit> EditList;
	private Stack<Stack<Integer>> Last;
	private Stack<Stack<Integer>> Next;
	private ThreadPoolExecutor pool=null;

	EditGroup(Context cont)
	{
		super(cont);
	}

	@Override
	public void scrollTo(int x, int y)
	{
		EditScro.setScrollY(y);
		EdithScro.setScrollX(x);
	}
	
	public static class EditGroupGetter{
		public static EditGroup GetEditGroup(Context cont,int width,int height,boolean is){
			EditGroup Group= EditGroupCreator.CreatSelf(cont);
			new Config_hesViewLevel().ConfigSelf(Group);
			return Group;
		}
	}
	
	static class EditGroupCreator{
		
		public static EditGroup CreatSelf(Context cont){
			EditGroup Group = new EditGroup(cont);
			init(Group);
			init2(cont,Group);
			return Group;
		}	
		private static void init(EditGroup Group)
		{
			Group. EditList = new ArrayList<>();
			Group. Last = new Stack<>();
			Group. Next = new Stack<>();
			Group. Last.add(new Stack<Integer>());
			Group. config = new Config_hesSize();
		    Group.creatEditBuilder();
			
			Group.configEdit=new Config_hesEdit();
		}
		private static void init2(Context cont,EditGroup Group)
		{	
			Group. EditScro = new ScrollView(cont);
			Group. EdithScro = new HorizontalScrollView(cont);
			Group. ForEdit = new LinearLayout(cont);
			Group. ForEditSon = new LinearLayout(cont);
			Group. EditLines = new EditLine(cont);
			Group. mWindow = new ListView(cont);
		}	
	}
	
	public static class Config_hesSize implements Configer<EditGroup>
	{
		private boolean portOrLand=true;
		private int WindowHeight=600, WindowWidth=600;
		private int selfWidth=2000,selfHeight=1000;
		
		@Override
		public void ConfigSelf(EditGroup target)
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

		public void set(int width,int height,boolean is,EditGroup target){
			selfWidth=width;
			selfHeight=height;
			portOrLand=is;
			onChange(target);
		}
		public void change(EditGroup target){
			int tmp = selfWidth;
			selfWidth=selfHeight;
			selfHeight=tmp;
			portOrLand=!portOrLand;
			onChange(target);
		}
		protected void onChange(EditGroup target){
			EditGroup.trim(target,selfWidth,selfHeight);
			EditGroup.trim(target.EditScro,selfWidth,selfHeight);
			EditGroup.trim(target.EdithScro,selfWidth,selfHeight);
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
	
	static class Config_hesViewLevel implements Configer<EditGroup>
	{
		@Override
		public void ConfigSelf(EditGroup target)
		{
			target. addView(target. EdithScro);
			target. EdithScro.addView(target. EditScro);
			target. EditScro.addView(target. ForEdit);
			target. ForEdit.addView(target. EditLines);
			target. ForEdit.addView(target. ForEditSon);
			target. addView(target. mWindow);
			config(target);
		}
		protected void config(EditGroup target)
		{
			target. EditLines.setFocusable(false);
			target. ForEditSon.setOrientation(LinearLayout.VERTICAL);
			target. mWindow.setBackgroundColor(0xFF1E2126);
			target. mWindow.setDivider(null);
			target.setWindowListener();
			CodeEdit.Enabled_Drawer=true;
			CodeEdit.Enabled_Complete=true;
			CodeEdit.Enabled_Format=true;
		}
	}
	
	public static class Config_hesEdit implements Configer<List<RCodeEdit>>
	{
		List<RCodeEdit> target;
		@Override
		public void ConfigSelf(List<EditGroup.RCodeEdit> target)
		{
			
		}
		public void set(List<RCodeEdit> target){
			this.target=target;
		}
		public void setLuagua(){
			
		}
		
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (((View)obj).getTag().equals(getTag()))
			return true;
		return false;
	}

	/*必须调用*/
	public void loadSize(int width,int height,boolean is){
		config.set(width, height,is,this);
		config.ConfigSelf(this);
	}
	public EditBuilder getEditBuilder()
	{
		return builder;
	}
	public List<CodeEdit> getEditList()
	{
		return EditList;
	}

	public void AddEdit(String name)
	{
		RCodeEdit Edit= creatAEdit(name);
		Edit.index = EditList.size();
		EditList.add(Edit);
		ForEditSon.addView(Edit);
	}
	protected RCodeEdit creatAEdit(String name)
	{
		RCodeEdit Edit;
		if (EditList.size() == 0)
		{
	        Edit = new RCodeEdit(getContext());
			configEdit(Edit, name);
			Edit.lines = EditLines;
			Edit.getCanvaserList().add(new ClipCanvaser());
			setTag(name);
		}
		else
		{
			Edit = new RCodeEdit(getContext(), EditList.get(0));
		}
		Edit.setOnClickListener(new Click());
		return Edit;
	}
	protected void configEdit(CodeEdit Edit, String name)
	{
		Edit.setPool(pool);
		Edit.setRunner(new EditListenerRunnerInfo());
		com.mycompany.who.Share.Share.setEdit(Edit, name);
	}


	/*关键代码*/
	protected void trimToFather()
	{
		//编辑器的大小变化了，将父元素的大小扩大到比编辑器更大，方便测量与布局
		wordIndex size = builder.WAndH();
		int height=size.end + ExpandHeight;
		int width=size.start + ExpandWidth;
		trim(ForEditSon, width - EditLines.maxWidth(), height);
		trim(ForEdit, width, height);
		//为两个Edit的父元素扩展空间，一个Lines的父元素ForEdit，一个ForEditSon
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
	final public static void trimXel(View Father, int WidthX, int HeightX)
	{
		ViewGroup.LayoutParams p = Father.getLayoutParams();
		p.width *= WidthX;
		p.height *= HeightX;
		Father.setLayoutParams(p);
	}


	final class RCodeEdit extends CodeEdit
	{

		public int index;	
		private boolean can;
		//别直接赋值，最后其实会在构造对象时赋值，等同于在构造函数中赋值

		public RCodeEdit(Context cont)
		{
			super(cont);
			can = true;
		}
		public RCodeEdit(Context cont, CodeEdit Edit)
		{
			super(cont, Edit);
			can = true;
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
		protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
		{

			if (!can)	
				return;
			//在构造对象前，会调用一次onTextChanged

			if (IsModify != 0 || IsModify2)
				return ;
			//已经被修改，不允许再修改
			
			Log.e("onTextChange",String.valueOf(index));
			
			EditFlag++;		
			sendOutLineToNext(text, start, lengthBefore, lengthAfter);		
			//在某次插入后，若超出最大的行数，截取之后的部分添加到编辑器列表中的下个编辑器开头	
			EditFlag--;

			if (EditFlag == 0)
			{
				EditLines. reLines(builder.calaEditLines());
				Last.push(new Stack<Integer>());
				trimToFather();
				//编辑器的大小变化了，将父元素的大小扩大到比编辑器更大，方便测量与布局
				//注意onTextChange优先于onMesure()调用，并且当前什么事也没做，此时设置最好
				//因为本次事件流未结束，所以EditText的数据未刷新，直接getHeight()是错误的
				//因此，我自己写了几个函数来测宽高，函数是通过文本来计算的，由于onTextChanged是文本变化后调用的，所以文本是对的
				
			}
			//最后一个编辑器扩展大小
			//最后一个编辑器单独计算行
			//从第一个调用onTextChanged的编辑器开始，之后的一组的联动修改都存储在同一个Stack
	        //先开辟一个空间，待之后存储

		}

		protected void sendOutLineToNext(CharSequence text, int start, int lengthBefore, int lengthAfter)
		{
			/*关键代码*/
			
			int lineCount= getLineCount();
			if (lineCount > MaxLine)
			{
				wordIndex j = subLines(MaxLine-OnceSubLine);
				String src = getText().toString().substring(j.start, j.end);
				IsModify++;
				getText().delete(j.start, j.end);	
				IsModify--;
				//为提升效率，若超出行数，一次性截取OnceSubLine行，使当前编辑器可以有一段时间的独自编辑状态

				if (start + lengthAfter < j.start)
					super.onTextChanged(text, start, lengthBefore, lengthAfter);		
				else
				{
				    super.onTextChanged(text, start, lengthBefore, j.start - start);	
				}	
				//截取后，只把本次未被截取的前半部分染色，
				//它可能在MAX行之内，即正常染色
				//也可能在MAX行之外，即只染色start～MAX行之间

				if (EditList.size() - 1 <= index)
					AddEdit("");
				//若无编辑器，则添加一个
				EditList.get(index + 1).getText().insert(0, src);
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

			//测量并修改Window大小
			config.ConfigSelf(EditGroup.this);

			//请求测量
			historyId = ((RCodeEdit)Edit).index;
			//本次窗口谁请求，单词给谁
			int offset=Edit.getSelectionStart();
			wordIndex pos = ((CodeEdit)Edit).getScrollCursorPos(offset, EdithScro.getScrollX(), EditScro.getScrollY() - EditGroup.this.builder.calaEditHeight(index));

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

	}

	class Click implements OnClickListener
	{
		@Override
		public void onClick(View p1)
		{
			historyId = ((RCodeEdit) p1).index;
			mWindow.setX(-9999);
		}
	}

	public class ClipCanvaser extends EditCanvaserListener
	{
		//提升效率，不想用可以remove
		@Override
		public void onDraw(EditText self, Canvas canvas, TextPaint paint, Rect Cursor_bounds)
		{	
		    /*关键代码*/
			canvas.clipRect(selfRect(self));
			//clipRect可以指示一块相对于自己的矩形区域，超出区域的部分会被放弃绘制
			
			if(EditDrawFlag==0){
				//遍历所有其它编辑器，并显示
				EditDrawFlag++;
				int id = historyId;
				historyId=((RCodeEdit)self).index;
				for(CodeEdit e: EditList){
					if(((RCodeEdit)e).index!=historyId)
					    e.invalidate();
				}
				EditDrawFlag--;
				historyId=id;
			}
		}
		
		public Rect selfRect(EditText self){
			
			int index = ((RCodeEdit)self).index;
			//计算编辑器在可视区域中的自己的范围

			int EditTop=builder.calaEditHeight(index); //编辑器较ForEditSon的顶部位置
			int SeeTop = EditScro.getScrollY(); //可视区域较ForEditSon的顶部位置
			int SeeLeft = EdithScro.getScrollX();//可视区域较ForEditSon的左边位置

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
		//在调试时，发现EditCanvaserListener内部还有一个$this成员，原来这个成员就是EditGroup
		//原来每个内部类，还有一个额外的成员，就是指向外部类实例的指针，在创建一个内部类对象时，内部类对象就与外部类实例绑定了
		return new ClipCanvaser();
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
					start.start = ((RCodeEdit)e).index;
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

		public wordIndex searchWord(String word)
		{
			for (CodeEdit e:EditList)
			{
				start.end = e.getText().toString().indexOf(word);
				if (start.end != -1)
				{
					start.start = ((RCodeEdit)(e)).index;
					return start;
				}
			}
			return null;
		}
		public void setLuagua(String luagua)
		{
			for (CodeEdit e:EditList)
			    e.setLuagua(luagua);
		}
		public void setRunner(EditListenerRunnerInfo Runner)
		{
			for (CodeEdit Edit:EditList)
			    Edit.setRunner(Runner);
		}
		public void setPool(ThreadPoolExecutor pool)
		{
			for (CodeEdit Edit:EditList)
			    Edit.setPool(pool);
		}

		public String reDraw()
		{
			StringBuilder builder=new StringBuilder();
			for (CodeEdit e:EditList)
			{
				e.reDraw(0, e.getText().length());
			}
			return builder.toString();
		}
		public String reDraw(int start, int end)
		{
			calaRange(start, end);
			final StringBuilder builder=new StringBuilder();
			DoForAnyOnce d= new DoForAnyOnce(){

				@Override
				void doOnce(int start, int end, CodeEdit Edit)
				{
				    Edit.reDraw(start, end);
				}
			};
			d.dofor(this.start, this.end);
			return builder.toString();
		}
		public void Format()
		{
			Last.add(new Stack<Integer>());
			for (CodeEdit e:EditList)
			{
				e.Format(0, e.getText().length());
			}
		}
		public void Format(int start, int end)
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
		public void Insert(int index)
		{
			calaIndex(index);
			EditList.get(start.start).Insert(start.end);
		}

		public void Uedo()
		{
			//与顺序无关的Uedo，它只存储一轮次的修改的编辑器下标，具体顺序由编辑器内部管理
	        //Bug: 多个编辑器之间会各自管理，因此任何一个的修改可能与另一个无关，造成单次Uedo不同步，但一直Uedo下去，结果是一样的
			if (Last.size() < 2)
				return;
			Stack<Integer> last= Last.get(Last.size() - 2);
			Last.remove(Last.size() - 2);
			Next.push(last);
			for (int l:last)
				EditList.get(l).Uedo();
			EditLines. reLines(calaEditLines());
		}
		public void Redo()
		{
			//与顺序无关的Redo，它只存储一轮次的修改的编辑器下标，具体顺序由编辑器内部管理
			if (Next.size() < 1)
				return;
			Stack<Integer> next= Next.pop();
			Last.push(next);
			for (int l:next)
				EditList.get(l).Redo();
			EditLines.reLines(calaEditLines());
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

		public int calaEditHeight(int index)
		{
			int height=0;
			for (int i=0;i < index;++i)
				height += EditList.get(i).maxHeight();
			return height;
		}
		public int calaEditLines()
		{
			int line=0;
			for (Edit e:EditList)
				line += e.getLineCount();
			return line;
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


	class onMyWindowClick implements OnItemClickListener
	{

		@Override
		public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
		{
			//如果点击了就插入单词并关闭窗口
			WordAdpter adapter = (WordAdpter) p1.getAdapter();
			Icon icon = (Icon) adapter.getItem(p3);
			CodeEdit Edit = EditList.get(historyId);
			Edit.insertWord(icon.getName(), Edit.getSelectionStart(), icon.getflag());
			mWindow.setX(-9999);
			mWindow.setY(-9999);
		}
	}
	class onMyWindowLongClick implements OnItemLongClickListener
	{
		@Override
		public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
		{
			//如果长按了就去到单词第一次出现的地方
			Icon icon = (Icon) p1.getAdapter().getItem(p3);
			wordIndex node= builder.searchWord(icon.getName());
			if (node != null)
			{
				CodeEdit Edit = EditList.get(node.start);
				wordIndex pos = Edit.getCursorPos(node.end);
				EdithScro.setScrollX(pos.start);
				EditScro.setScrollY(pos.end + builder.calaEditHeight(((RCodeEdit)(Edit)).index));   	
				Edit.setSelection(node.end, node.end + icon.getName().length());
			}
			return true;
		}
	}
	private void setWindowListener(){
		mWindow.setOnItemClickListener(new onMyWindowClick());
		mWindow.setOnItemLongClickListener(new onMyWindowLongClick());	
	}
	
	
	

	public void setPool(ThreadPoolExecutor pool)
	{
		this.pool = pool;
		builder.setPool(pool);
	}


	@Override
	protected void onConfigurationChanged(final Configuration config)
	{
		EditGroup.this.config.change(this);
		EditGroup.this.config.ConfigSelf(this);
		super.onConfigurationChanged(config);
	}


}
