package com.mycompany.who.Edit;

import android.content.*;
import android.graphics.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.ListenerVistor.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import com.mycompany.who.Edit.Share.*;
import java.security.acl.*;
import java.util.*;
import java.util.concurrent.*;
import android.util.*;


/*
 在基类上开一些接口，另外的，复杂的函数我都设置成了final

 从现在开始，所有被调函数，例如Drawing，必须自己管理好线程和IsModify安全，然后将真正操作交给另一个函数

 在写代码时，必须保证当前的代码已经优化成最简的了，才能去继续扩展，扩展前先备份
 */
public abstract class CodeEdit extends BaseEdit
{

	public static int Delayed_Draw = 0;

	protected EditListenerInfo Info;
	private EditListenerFactory.EditCompletorBoxes boxes;

	public CodeEdit(Context cont)
	{
		super(cont);
		Info = new myEditInfo();
		boxes = new EditListenerFactory.EditCompletorBoxes(0);
		trimListener();
	}
	public CodeEdit(Context cont, CodeEdit Edit)
	{
		super(cont, Edit);
		Info = Edit.Info;
		boxes = Edit.boxes;
	}

    public class myEditInfo extends EditListenerInfo
	{
		public myEditInfo()
		{
			mlistenerVS = new ArrayList<>();
			mlistenerCS = new ArrayList<>();			
		}	
	}
	public void trimListener()
	{
		setDrawer(EditListenerFactory.getDefaultDrawer());
		getCanvaserList().add(new EditListenerFactory.DefaultCanvaser());
		setFormator(new EditListenerFactory.DefaultFormatorListener());
		setInsertor(new EditListenerFactory.DefaultInsertorListener());
		List<EditListener> cs=getCompletorList();
		cs.add(boxes.getVillBox());
		cs.add(boxes.getObjectBox());
		cs.add(boxes.getFuncBox());
		cs.add(boxes.getTypeBox());
		cs.add(boxes.getDefaultBox());
		cs.add(boxes.getKeyBox());
		cs.add(boxes.getTagBox());
	}

	public void clearListener()
	{
	}
	public void setFinder(EditListener li)
	{
		Info.mlistenerF = li;
	}
	public EditListener getFinder()
	{
		return Info.mlistenerF;
	}
	public void setDrawer(EditListener li)
	{
		Info.mlistenerD = li;
	}
	public EditListener getDrawer()
	{
		return Info.mlistenerD;
	}
	public void setFormator(EditListener li)
	{
	    Info.mlistenerM = (EditFormatorListener)li;
	}
	public void setInsertor(EditListener li)
	{
		Info.mlistenerI = (EditInsertorListener)li;
	}
	public EditListener getFormator()
	{
		return Info.mlistenerM;
	}
	public EditListener getInsertor()
	{
		return Info.mlistenerI;
	}
	public List<EditListener> getCompletorList()
	{
		return Info.mlistenerCS;
	}
	public List<EditListener> getCanvaserList()
	{
		return Info.mlistenerVS;
	}
	public EditListenerInfo getInfo(){
		return Info;
	}

	public void setLuagua(String Lua)
	{
		laugua = Lua;
		boxes. Search_Bit = 0xffffffff;
		switch (Lua)
		{
			case "text":
				setFinder(EditListenerFactory.FinderFactory.getTextFinder());
				boxes.Search_Bit = 0;
				break;
			case "xml":
				setFinder(EditListenerFactory.FinderFactory.getXMLFinder());
				boxes.Search_Bit = 0;
				boxes.Search_Bit = Share.setbitTo_1S(boxes.Search_Bit, Colors.color_tag, Colors.color_attr);
				break;
			case "java":
				setFinder(EditListenerFactory.FinderFactory.getJavaFinder());
				boxes.Search_Bit = Share.setbitTo_0S(boxes.Search_Bit, Colors.color_tag, Colors.color_attr);
				break;
			case "css":
				setFinder(EditListenerFactory.FinderFactory.getCSSFinder());
				boxes.Search_Bit = Share.setbitTo_0S(boxes.Search_Bit, Colors.color_key, Colors.color_const, Colors.color_obj);
				break;
			default:
			    setFinder(null);
				boxes.Search_Bit = 0;
		}
	}


	/*
	 _________________________________________

	 Dreawr

	 继承于父类的函数：

	 FindFor ->

	 Drawing ->

	 新的函数：

	 -> onFindNodes

	 -> onDrawNodes

	 _________________________________________

	 */
	protected final void FindFor(int start, int end, String text, List<wordIndex>nodes, SpannableStringBuilder builder)
	{
		//为了安全，禁止重写
		long last = 0,now = 0;
		last = System.currentTimeMillis();

		Ep.start();//开始记录
		try
		{
		    onFindNodes(start, end, text, nodes, builder);
		}
		catch (Exception e)
		{
			Log.e("FindNodes Error", e.toString());
		}	

		now = System.currentTimeMillis();
		Log.w("After FindNodes", "I take " + (now - last) + " ms, " + Ep.toString());

	}
	protected void onFindNodes(int start, int end, String text, List<wordIndex>nodes, SpannableStringBuilder builder)throws Exception
	{
		EditFinderListener li = ((EditFinderListener)getFinder());
		if(li!=null){
		    List<wordIndex> tmp = li.LetMeFind(start, end, text, WordLib);
		    if (tmp != null)
		    {
			    nodes.addAll(tmp);
			    li.setSapns(text, nodes, builder);
		    }
		}
	}

	@Override
	protected final void Drawing(final int start, final int end, final List<wordIndex> nodes, final SpannableStringBuilder builder)
	{
		//为了安全，禁止重写
		Runnable run= new Runnable(){

			@Override
			public void run()
			{
				IsModify++;
				isDraw = true; //此时会修改文本，isModify

				long last = 0,now = 0;
				last = System.currentTimeMillis();
				try
				{
					if (nodes.size() != 0)
					    onDrawNodes(start, end, nodes, builder);
				}
				catch (Exception e)
				{
					Log.e("DrawNodes Error", e.toString());
				}
				now = System.currentTimeMillis();

				isDraw = false;
				IsModify--;
				Ep.stop();
				//Draw完后申请回收
				Log.w("After DrawNodes", "I take " + (now - last) + " ms, " + Ep.toString());

			}
		};
		if (Delayed_Draw == 0)
			post(run);
		else
			postDelayed(run, Delayed_Draw);
		//为了线程安全，涉及UI操作必须抛到主线程	
	}
	protected void onDrawNodes(int start, int end, List<wordIndex> nodes, SpannableStringBuilder builder)throws Exception
	{
		//应该重写这个
		EditDrawerListener li = ((EditDrawerListener)getDrawer());
		if(li!=null)
		    li.LetMeDraw(start, end, nodes, builder, getText());
	}


	/*
	 _________________________________________

	 Formator

	 继承于父类的函数：

	 Format ->

	 Insert -> 

	 新的函数：
	 
	 -> onBeforeFormat

	 -> onFormat

	 -> onInsert

	 _________________________________________

	 */

	public final Future Format(final int start, final int end)
	{
		//为了安全，禁止重写

		Runnable run = new Runnable(){

			@Override
			public void run()
			{
				long last = 0,now = 0;
				last = System.currentTimeMillis();
				
				String src = onBeforeFormat(start,end);
				//开线程格式化

				now = System.currentTimeMillis();
				Log.w("After Format StringSpilter", "I take " + (now - last) + " ms, " + "Because Str Length is " + src.length() + ", and " + Ep.toString());

				if (src == null)
					src = getText().toString().substring(start, end);
				final String buffer=src;
				
				Runnable run2 = new Runnable(){

					@Override
					public void run()
					{
						IsModify++;
						isFormat = true; //在此时才会修改文本
						
						long last = 0,now = 0;
						last = System.currentTimeMillis();
						try
						{
							onFormat(start, end, buffer);
						}
						catch (Exception e)
						{
							Log.e("Format Error", e.toString());
						}
						now = System.currentTimeMillis();
						Log.w("After Format Replacer", "I take " + (now - last) + " ms," +"The time maybe too Loog！");
						
						isFormat = false;
						IsModify--;

					}
				};
				post(run2); //安全地把任务交给onFormat
			}

		};
		if (pool != null)
			return pool.submit(run);
		else
			run.run();

		return null;
	}
	
	protected String onBeforeFormat(int start,int end){
		String src = null;
		EditFormatorListener li = ((EditFormatorListener)getFormator());
		if(li!=null)
			src = li.LetMeFormat(start, end, getText().toString());
		return src;
	}
	
	protected void onFormat(int start, int end, String buffer)throws Exception
	{
		//为提升效率，将原文本和目标文本装入buffer
		//您可以直接通过测量buffer.getSrc()的下标来修改buffer内部
		getText().replace(start, end, buffer);
		//最后，当所有人完成本次对文本的修改后，一次性将修改后的文件替换至Edit
	}

	public final void Insert(final int index)
	{
		//插入字符
		IsModify++;
		isFormat = true;

		try
		{
		    onInsert(index);
		}
		catch (Exception e)
		{
			Log.e("Insert Error", e.toString());
		}

		isFormat = false;
		IsModify--;
	}	

	protected int onInsert(int index)throws Exception
	{
		EditInsertorListener li = ((EditInsertorListener)getInsertor());
		if(li!=null)
		    return li.LetMeInsert(getText(), index);
		return index;
	}


	/*
	 _________________________________________

	 Completor

	 继承于父类的函数：

	 openWindow ->1

	 onInsertword

	 新的函数：

	 1-> SearchInGroup

	 1-> callOnopenWindow ->2

	 2-> calc

	 _________________________________________

	 */
	final public Future openWindow(final ListView Window, int index, final ThreadPoolExecutor pool)
	{
		final String wantBefore= getWord(index);
		final String wantAfter = getAfterWord(index);
		//获得光标前后的单词，并开始查找

		Runnable run = new Runnable(){
			@Override
			public void run()
			{
				Epp.start();//开始存储
				long last = 0,now = 0;
				last = System.currentTimeMillis();

				List<Icon> Icons = SearchInGroup(wantBefore, wantAfter, 0, wantBefore.length(), getCompletorList(), getPool());
				//经过一次查找，Icons里装满了单词
				now = System.currentTimeMillis();
				Log.w("After SearchWords", "I take " + (now - last) + " ms, " + Epp.toString());

				if (Icons != null)
				{
					final WordAdpter adapter = new WordAdpter(Window.getContext(), Icons, R.layout.WordIcon);
					Runnable run2=new Runnable(){

						@Override
						public void run()
						{

							Window.setAdapter(adapter);
							try
							{
							    callOnopenWindow(Window);
							}
							catch (Exception e)
							{
								Log.e("OpenWindow Error", e.toString());
							}

							Epp.stop();
							Log.w("After OpenWindow", Epp.toString());
							//将单词放入Window后回收
						}
					};
					post(run2);//将UI任务交给主线程
				}
			}
		};
		if (pool != null)
		    return pool.submit(run);//因为含有阻塞，所以将任务交给池子
		else
			run.run();
		return null;
	}

	protected List<Icon> SearchInGroup(final String wantBefore, final String wantAfter, final int before, final int after, Collection<EditListener> Group, ThreadPoolExecutor pool)
	{
		//用多线程在不同集合中找单词

		EditListenerItrator.RunLi<List<Icon>> run = new EditListenerItrator.RunLi<List<Icon>>(){

			@Override
			public List<Icon> run(EditListener li)
			{
				return ((EditCompletorListener)li).LetMeCompelet(wantBefore, wantAfter, before, after, WordLib);
			}
		};
		if (getPool() == null)
			return EditListenerItrator.foreach(Group, run);
		return EditListenerItrator.foreach(Group, run);
		//阻塞以获得所有单词
	}

	protected void onInsertword(String word, int index, int flag)
	{
		Editable editor = getText();	

		wordIndex tmp = tryWordSplit(editor.toString(), index);
		wordIndex tmp2 = tryWordSplitAfter(getText().toString(), index);

		editor.replace(tmp.start, tmp2.end, word);
		setSelection(tmp.start + word.length());
		//把光标移动到最后

		if (flag == 3)
		{
			editor.insert(getSelectionStart(), "(");
		}
		else if (flag == 5)
		{
			if (editor.toString().charAt(tmp.start - 1) != '<')
				editor.insert(tmp.start, "<");
		}
		//函数额外插入字符
	}

	protected void callOnopenWindow(ListView Window)
	{
		if (getWindow().getAdapter() != null && getWindow().getAdapter().getCount() > 0)
		{
			wordIndex pos = calc(this);
			getWindow().setX(pos.start);
			getWindow().setY(pos.end);
		}
		else
		{
			//如果删除字符后没有了单词，则移走
			getWindow().setX(-9999);
			getWindow().setY(-9999);
		}
	}

	abstract public wordIndex calc(EditText Edit);


	/*
	 _________________________________________

	 
	 onDraw with  Canvaser
	 
	 _________________________________________
	 */
    @Override
	protected void onDraw(Canvas canvas)
	{

		//获取当前控件的画笔
        TextPaint paint = getPaint();
		int lines= getLayout().getLineForOffset(getSelectionStart());
		Rect bounds = new Rect();
		getLineBounds(lines, bounds);

		try
		{
			for (EditListener li:getCanvaserList())
				((EditCanvaserListener)li).LetMeCanvaser(this, canvas, paint, bounds);
		}
		catch (Exception e)
		{
			Log.e("OnDraw Error", e.toString());
		}
		
		super.onDraw(canvas);
    }



	
	/*

	 其它函数

	 */

	final public void reSAll(int start, int end, String want, String to)
	{
		IsModify++;
		isFormat = true;
		Editable editor = getText();
		String src=getText().toString().substring(start, end);
		int nowIndex = src.lastIndexOf(want);
		while (nowIndex != -1)
		{
			//从起始位置开始，反向把字符串中的want替换为to
			editor.replace(nowIndex + start, nowIndex + start + want.length(), to);	
			nowIndex = src.lastIndexOf(want, nowIndex - 1);
		}
		isFormat = false;
		IsModify--;
	}


	public void zoomBy(float size)
	{
		setTextSize(size);
	    lines.setWidth(lines.maxWidth());
		lines.setTextSize(size);
	}

	final public wordIndex getCursorPos(int offset)
	{
		//获取光标坐标
		int lines= getLayout().getLineForOffset(offset);
		Rect bounds = new Rect();
		//任何传参取值都必须new
		wordIndex pos = new wordIndex();
		getLineBounds(lines, bounds);
	    pos.start = bounds.centerX();
		pos.end = bounds.centerY();

		int index= tryLine_Start(getText().toString(), offset);
		pos.start = (int)((offset - index) * getTextSize());

		return pos;
	}
	final public wordIndex getRawCursorPos(int offset, int width, int height)
	{
		//获取绝对光标坐标
		wordIndex pos = getCursorPos(offset);
		pos.start = pos.start % width;
		pos.end = pos.end % height;
		return pos;
	}
	final public wordIndex getScrollCursorPos(int offset, int scrollx, int scrolly)
	{
		//获取存在滚动条时的绝对光标坐标
		//当前屏幕起始0相当于scroll滚动量,然后用cursorpos-scroll，就是当前屏幕光标绝对坐标	
		wordIndex pos = getCursorPos(offset);
		pos.start = pos.start - scrollx;
		pos.end = pos.end - scrolly;		
		return pos;
	}

	final public int fromy_getLineOffset(int y)
	{
		float xLine;
		int nowN = 0;
		xLine = y / getLineHeight();

		while (xLine-- > 0)
		{
			nowN = tryLine_End(getText().toString(), nowN + 1);
			//从起始行开始，向后试探至那行的offset
		}
		return nowN;
	}
	final public int fromPos_getCharOffset(int x, int y)
	{
		//从坐标获取光标位置
		int xCount=(int)(x / getTextSize());
		int Line=fromy_getLineOffset(y);
		while (xCount-- != 0 && xCount < getText().toString().length() && getText().toString().charAt(Line) != '\n')
		{
			Line++;
		}
		return Line;
	}


}
