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


/*
 在基类上开一些接口，另外的，复杂的函数我都设置成了final

 从现在开始，所有被调函数，例如Drawing，必须自己管理好线程和IsModify安全，然后将真正操作交给另一个函数

 在写代码时，必须保证当前的代码已经优化成最简的了，才能去继续扩展，扩展前先备份
*/
public abstract class CodeEdit extends BaseEdit
{
	
	public static int Delayed_Draw = 0;
	
	protected EditListenerInfo Info;
	protected EditListenerRunnerInfo Runner;
	private final FinderFactory mfactory=new FinderFactory();
	private final EditCompletorBoxes boxes=new EditCompletorBoxes();
	private int Search_Bit=0xefffffff;
	
	public CodeEdit(Context cont)
	{
		super(cont);
		Info=new myEditInfo();
		trimListener();
	}
	public CodeEdit(Context cont, CodeEdit Edit)
	{
		super(cont, Edit);
		Info=Edit.Info;
		Runner=Edit.getRunner();
		Search_Bit=Edit.Search_Bit;
	}

    public class myEditInfo extends EditListenerInfo{
		public myEditInfo(){
			mlistenerVS=new ArrayList<>();
			mlistenerCS=new ArrayList<>();			
		}	
	}
	public void trimListener(){
		setDrawer(new DefaultDrawerListener());
		getCanvaserList().add(new DefaultCanvaser());
		setFormator(new DefaultFormatorListener());
		setInsertor(new DefaultInsertorListener());
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
	public void setDrawer(EditListener li)
	{
		Info.mlistenerD = li;
	}
	public EditListener getFinder()
	{
		return Info.mlistenerF;
	}
	public EditListener getDrawer()
	{
		return Info.mlistenerD;
	}
	public void setFormator(EditListener li)
	{
	    Info.mlistenerM= (EditFormatorListener)li;
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
	public void setRunner(EditListenerRunnerInfo run)
	{
		Runner = run;
	}
	public EditListenerRunnerInfo getRunner()
	{
		return Runner;
	}
	public void setLuagua(String Lua)
	{
		laugua=Lua;
		Search_Bit=0xffffffff;
		switch(Lua){
			case "text":
				setFinder(mfactory.getTextFinder());
				Search_Bit = Share.setbitTo_0S(Search_Bit, Colors.color_key, Colors.color_const, Colors.color_func, Colors.color_villber, Colors.color_obj, Colors.color_type);
				break;
			case "xml":
				setFinder(mfactory.getXMLFinder());
				break;
			case "java":
				setFinder(mfactory.getJavaFinder());
				Search_Bit = Share.setbitTo_0S(Search_Bit, Colors.color_tag, Colors.color_attr);
				break;
			case "css":
				setFinder(mfactory.getCSSFinder());
				Search_Bit = Share.setbitTo_0S(Search_Bit, Colors.color_key, Colors.color_const, Colors.color_obj);
				break;
			case "html":
				setFinder(mfactory.getHTMLFinder());
				Search_Bit = 0xefffffff;
				break;
			default:
			    setFinder(null);
				Search_Bit = 0;
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
	protected final void FindFor(int start, int end, String text,List<wordIndex>nodes,SpannableStringBuilder builder)
	{
		//为了安全，禁止重写
		Ep.start(); //开始记录
		
		try{
		    onFindNodes(start,end,text,nodes,builder);
		}catch(Exception e){}
		
	}
	protected void onFindNodes(int start, int end, String text,List<wordIndex>nodes,SpannableStringBuilder builder)throws Exception{
		if(Runner!=null)
		    Runner.FindForLi(start, end, text, WordLib, nodes,builder, (EditFinderListener)getFinder());
	}

	@Override
	protected final void Drawing(final int start, final int end, final List<wordIndex> nodes,final SpannableStringBuilder builder)
	{
		//为了安全，禁止重写
		Runnable run= new Runnable(){

			@Override
			public void run()
			{
				IsModify++;
				isDraw = true; //此时会修改文本，isModify
				
				try{
				    onDrawNodes(start,end,nodes,builder);
				}catch(Exception e){}
				
				isDraw = false;
				IsModify--;
				Ep.stop(); //Draw完后回收nodes
			}
		};
		if(Delayed_Draw==0)
			post(run);
		else
			postDelayed(run,Delayed_Draw);
		//为了线程安全，涉及UI操作必须抛到主线程	
	}
	protected void onDrawNodes(int start, int end, List<wordIndex> nodes,SpannableStringBuilder builder)throws Exception{
		//应该重写这个
		if(Runner!=null)
	        Runner.DrawingForLi(start, end, nodes,builder, getText(),(EditDrawerListener)getDrawer());
	}


/*
 _________________________________________

 Formator

 继承于父类的函数：

	 Format ->
	 
	 onInsert 
	 
 新的函数：
	 
	 -> onFormat
	 
 _________________________________________

*/
	
	public final void Format(final int start, final int end)
	{
		//为了安全，禁止重写
		if (Runner != null)
		{
			Runnable run = new Runnable(){

				@Override
				public void run()
				{

					String src = Runner.FormatForLi(start, end, getText().toString(), (EditFormatorListener)getFormator());
					//开线程格式化
					if(src==null)
						src=getText().toString().substring(start,end);
					final String buffer=src;
					Runnable run2 = new Runnable(){

						@Override
						public void run()
						{
							IsModify++;
							isFormat = true; //在此时才会修改文本
							try{
								onFormat(start, end, buffer);
							}catch(Exception e){}
							isFormat = false;
							IsModify--;
						}
					};
					post(run2); //安全地把任务交给onFormat
				}

			};
			if (pool != null)
				pool.execute(run);
			else
				run.run();
		}
	}
	protected void onFormat(int start, int end, String buffer)throws Exception
	{
		//为提升效率，将原文本和目标文本装入buffer
		//您可以直接通过测量buffer.getSrc()的下标来修改buffer内部
		getText().replace(start, end, buffer);
		//最后，当所有人完成本次对文本的修改后，一次性将修改后的文件替换至Edit
	}


	protected void onInsert(int index)throws Exception
	{
		if (Runner != null) 
		    Runner.InsertForLi(getText(), index, (EditInsertorListener)getInsertor());
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
	final public void openWindow(final ListView Window, int index, final ThreadPoolExecutor pool)
	{
		
		final String wantBefore= getWord(index);
		final String wantAfter = getAfterWord(index);
		//获得光标前后的单词，并开始查找

		Runnable run = new Runnable(){
			@Override
			public void run()
			{
				Epp.start();//开始存储
				List<Icon> Icons = SearchInGroup(wantBefore,wantAfter,0,wantBefore.length(),getCompletorList(),getPool());
				//经过一次查找，Icons里装满了单词
				if(Icons!=null){
					final WordAdpter adapter = new WordAdpter(Window.getContext(), Icons, R.layout.WordIcon);
					Runnable run2=new Runnable(){

						@Override
						public void run()
						{
							Window.setAdapter(adapter);
							try{
							    callOnopenWindow(Window);
							}catch(Exception e){}
							//将单词设置到Window后回收单词
							Epp.stop();
						}
					};
					post(run2);//将UI任务交给主线程
				}
			}
		};
		if(pool!=null)
		    pool.execute(run);//因为含有阻塞，所以将任务交给池子
		else
			run.run();

	}
	
	final protected List<Icon> SearchInGroup(final String wantBefore,final String wantAfter,final int before,final int after,Collection<EditListener> Group,ThreadPoolExecutor pool){
		//用多线程在不同集合中找单词
		if(Runner==null)
			return null;

		EditListenerItrator.RunLi<List<Icon>> run = new EditListenerItrator.RunLi<List<Icon>>(){

			@Override
			public List<Icon> run(EditListener li)
			{
				return Runner.CompeletForLi(wantBefore,wantAfter,before,after,(EditCompletorListener)li);
			}
		};
		if(getPool()==null)
			return EditListenerItrator.foreach(Group,run);
		return EditListenerItrator.foreach(Group,run);
		//阻塞以获得所有单词
	}

	protected void onInsertword(String word,int index,int flag){
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
		if (getWindow().getAdapter()!=null&&getWindow().getAdapter().getCount()>0)
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
		if(Runner!=null){
			try{
				for(EditListener li:getCanvaserList())
					Runner.CanvaserForLi(this,canvas,paint,bounds,(EditCanvaserListener)li);
			}catch(Exception e){}
		}
		super.onDraw(canvas);
    }

//	___________________________________________________________________________________________________________________________

	//CanvaserListener
	public static class DefaultCanvaser extends EditCanvaserListener
	{

		@Override
		public void onDraw(EditText self,Canvas canvas, TextPaint paint,Rect bounds)
		{
			//设置画笔的描边宽度值
			paint.setStrokeWidth(0.2f);
			paint.setStyle(Paint.Style.FILL_AND_STROKE);

			//任何修改都会触发重绘，这里在光标位置画矩形

			paint.setColor(CursorRect_Color);
			canvas.drawRect(bounds,paint);
		}
	}
	
	
//	___________________________________________________________________________________________________________________________

	//DrawerListener
	public static class DefaultDrawerListener extends EditDrawerListener
	{
		@Override
		public void onDraw(final int start, final int end, List<wordIndex> nodes, SpannableStringBuilder builder, Editable editor)
		{
	    	editor.replace(start, end, builder);
		}

	}
	
	
//	___________________________________________________________________________________________________________________________
	
	//FormatorListener
	final public static class DefaultFormatorListener extends EditFormatorListener
	{

		public String START="{";
		public String END="}";
		public String SPILT="\n";
		public String INSERT=" ";
		public int CaCa=4;

		public  int dothing_Run(ModifyBuffer editor, int nowIndex)
		{
			String src=editor.getSrc();
			int nextIndex= src.indexOf(SPILT, nowIndex + 1);
			//从上次的\n接着往后找一个\n

			//如果到了另一个代码块，不直接缩进
			int start_bindow = src.indexOf(START, nowIndex + 1);
			int end_bindow=src.indexOf(END, nowIndex + 1);

			if (nowIndex == -1 || nextIndex == -1)
				return -1;

			int nowCount,nextCount;
			nowCount = String_Splitor. calaN(src, nowIndex + 1);
			nextCount = String_Splitor. calaN(src, nextIndex + 1);
			//统计\n之后的分隔符数量

			String is= src.substring(tryLine_Start(src, nextIndex + 1), tryLine_End(src, nextIndex + 1));
			//如果下个的分隔符数量小于当前的，缩进至与当前的相同的位置
			if (nowCount >= nextCount && is.indexOf(START) == -1)
			{
				if (end_bindow < nextIndex && end_bindow != -1)
				{
					//如果当前的nextindex出了代码块，将}设为前面的代码块中与{相同位置
					int index= String_Splitor.getBeforeBindow(src, end_bindow , START, END);
					if (index == -1)
						return nextIndex;
					int linestart=tryLine_Start(src, index);
					int noline= tryAfterIndex(src, linestart);
					int bindowstart=tryLine_Start(src, end_bindow);
					int nobindow=tryAfterIndex(src, bindowstart);
					if (nobindow - bindowstart != noline - linestart)
					{		
						editor.replace(bindowstart, nobindow, String_Splitor.getNStr(INSERT, noline - linestart));
						return nextIndex + (noline - linestart) - (nobindow - bindowstart);
					}
					editor.insert(nextIndex + 1, String_Splitor. getNStr(INSERT, noline - linestart - CaCa));
					return nextIndex;
				}
				if (start_bindow < nextIndex && start_bindow != -1)
				{
					//如果它是{之内的，并且缩进位置小于{，则将其缩进至{内
					editor.insert(nextIndex + 1, String_Splitor. getNStr(INSERT, nowCount - nextCount + CaCa));
					return nextIndex;
				}

				editor.insert(nextIndex + 1, String_Splitor. getNStr(INSERT, nowCount - nextCount));
				return nextIndex;
			}

			return nextIndex;
			//下次从这个\n开始

		}

		@Override
		public int dothing_Start(ModifyBuffer editor, int nowIndex, int start, int end)
		{
			editor. reSAll("\t", "    ");
			String src= editor.toString();
			nowIndex = src.lastIndexOf(SPILT, nowIndex - 1);
			if (nowIndex == -1)
				nowIndex = src.indexOf(SPILT);
			return nowIndex;
			//返回now之前的\n
		}

		@Override
		public int dothing_End(ModifyBuffer editor, int beforeIndex, int start, int end)
		{
			return -1;
		}

	}
	
//	___________________________________________________________________________________________________________________________
	
	//InsertorListener
	public class DefaultInsertorListener extends EditInsertorListener
	{

		@Override
		public void putWords(HashMap<String, String> words)
		{
			// TODO: Implement this method
		}

		public char insertarr[];
		public DefaultInsertorListener()
		{
			insertarr = new char[]{'{','(','[','\'','"','/','\n'};
			Arrays.sort(insertarr);
		}
		public int dothing_insert(Editable editor, int nowIndex)
		{
			String src=editor.toString();
			int charIndex=Array_Splitor.indexOf(src.charAt(nowIndex), insertarr);
			if (charIndex != -1)
			{
				switch (src.charAt(nowIndex))
				{
					case '{':
						editor.insert(nowIndex + 1, "}");
						break;
					case '(':
						editor.insert(nowIndex + 1, ")");
						break;
					case '[':
						editor.insert(nowIndex + 1, "]");
						break;
					case '\'':
						editor.insert(nowIndex + 1, "'");
						break;
					case '"':
						editor.insert(nowIndex + 1, "\"");
						break;
					case '/':
						if (src.charAt(nowIndex - 1) == '<')
						{
							int index= String_Splitor.getBeforeBindow(src, nowIndex - 1, "<", "</");
							wordIndex j= tryWordAfter(src, index);
							editor.insert(nowIndex + 1, src.substring(j.start, j.end) + ">");
							return j.end + 1;
						}
				}
			}
			return nowIndex + 1;
		}
	}
	
	
//	___________________________________________________________________________________________________________________________
	
	//EditCompletorBoxes
	final public class EditCompletorBoxes
	{ 

	    public EditListener getKeyBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<String> onBeforeSearchWord()
				{
					return getKeyword();
				}

				@Override
				public void onFinishSearchWord(List<String> word, List<Icon> adpter)
				{
					addSomeWord(word, adpter, Share.icon_key);
				}
			};
		}
		public EditListener getDefaultBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<String> onBeforeSearchWord()
				{
					List<String> words=new ArrayList<>();
					if (Share.getbit(Search_Bit, Colors.color_attr))
						words.addAll(getAttribute());
					if(Share.getbit(Search_Bit, Colors.color_const))
					    words.addAll(getConstword());

					return words;
				}

				@Override
				public void onFinishSearchWord(List<String> word, List<Icon> adpter)
				{
					addSomeWord(word, adpter, Share.icon_default);
				}
			};
		}

		public EditListener getVillBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<String> onBeforeSearchWord()
				{
					if (Share.getbit(Search_Bit, Colors.color_villber))
					    return getHistoryVillber();
					return null;
				}

				@Override
				public void onFinishSearchWord(List<String> word, List<Icon> adpter)
				{
					addSomeWord(word, adpter, Share.icon_villber);
				}
			};
		}


		public EditListener getFuncBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<String> onBeforeSearchWord()
				{
					if (Share.getbit(Search_Bit, Colors.color_func))
					    return getLastfunc();
					return null;
				}

				@Override
				public void onFinishSearchWord(List<String> word, List<Icon> adpter)
				{
					addSomeWord(word, adpter, Share.icon_func);
				}
			};
		}


		public EditListener getObjectBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<String> onBeforeSearchWord()
				{
					if (Share.getbit(Search_Bit, Colors.color_obj))
						return getThoseObject();
					return null;
				}

				@Override
				public void onFinishSearchWord(List<String> word, List<Icon> adpter)
				{
					addSomeWord(word, adpter, Share.icon_obj);
				}
			};
		}

		public EditListener getTypeBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<String> onBeforeSearchWord()
				{
					if (Share.getbit(Search_Bit, Colors.color_type))
					    return getBeforetype();
					return null;
				}

				@Override
				public void onFinishSearchWord(List<String> word, List<Icon> adpter)
				{
					addSomeWord(word, adpter, Share.icon_type);
				}
			};
		}

		public EditListener getTagBox()
		{
			return new EditCompletorListener(){

				@Override
				public Collection<String> onBeforeSearchWord()
				{
					if (Share.getbit(Search_Bit, Colors.color_tag))
					{
						return getTag();
					}
					return null;
				}

				@Override
				public void onFinishSearchWord(List<String> word, List<Icon> adpter)
				{
					addSomeWord(word, adpter, Share.icon_tag);
				}
			};
		}
	}
	
//	___________________________________________________________________________________________________________________________
	
	//FinderFactory
	public class FinderFactory{
		public EditListener getTextFinder(){
			return new FinderText();
		}
		public EditListener getXMLFinder(){
			return new FinderXML();
		}
		public EditListener getJavaFinder(){
			return new FinderJava();
		}
		public EditListener getCSSFinder(){
			return new FinderCSS();
		}
		public EditListener getHTMLFinder(){
			return new FinderHTML();
		}
		
		public class FinderText extends EditFinderListener
		{

			@Override
			public void OnFindWord(List<BaseEdit.DoAnyThing> totalList,Words WordLib)
			{

			}

			@Override
			public void OnFindNodes(List<BaseEdit.DoAnyThing> totalList, Words WordLib)
			{
				// TODO: Implement this method
				AnyThingFactory. AnyThingForText AllThings = mThings.getAnyThingText();
				totalList.add(AllThings.getGoTo_zhuShi());
				totalList.add(AllThings.getGoTo_Str());
				totalList.add(AllThings.getNoSans_Char());
			}

			@Override
			public void OnClearFindWord(Words WordLib)
			{

			}

			@Override
			public void OnClearFindNodes(int start,int end,String text, List<wordIndex> nodes)
			{
				clearRepeatNode(nodes,end);
			}
		}

		public class FinderXML extends EditFinderListener
		{

			@Override
			public void OnClearFindNodes(int start, int end, String text, List<wordIndex> nodes)
			{
				clearRepeatNode(nodes,end);
			}


			@Override
			public void OnFindWord(List<BaseEdit.DoAnyThing> totalList,Words WordLib)
			{

			}

			@Override
			public void OnFindNodes(List<BaseEdit.DoAnyThing> totalList,Words WordLib)
			{
				AnyThingFactory. AnyThingForXML AllThings = mThings.getAnyThingXML();

				totalList.clear();
				totalList.add(AllThings.getGoTo_zhuShi());	
				totalList.add(AllThings.getGoTo_Str());
				totalList.add(AllThings.getDraw_Tag());

				totalList.add(AllThings.getDraw_Attribute());	

				totalList.add(AllThings.getNoSans_Char());
			}

			@Override
			public void OnClearFindWord(Words WordLib)
			{

			}
		}


		final public class FinderJava extends EditFinderListener
		{

			@Override
			public void OnFindWord(List<BaseEdit.DoAnyThing> totalList,Words WordLib)
			{
				AnyThingFactory. AnyThingForJava AllThings = mThings.getAnyThingJava();

				totalList.add(AllThings.getSans_TryFunc());	
				totalList.add(AllThings.getSans_TryVillber());
				totalList.add(AllThings.getSans_TryType());
				totalList.add(AllThings.getSans_TryObject());
				totalList.add(AllThings.getNoSans_Char());
				//请您在任何时候都加入getChar，因为它可以适时切割单词

			}

			@Override
			public void OnFindNodes(List<BaseEdit.DoAnyThing> totalList,Words WordLib)
			{
				AnyThingFactory. AnyThingForJava AllThings = mThings.getAnyThingJava();

				totalList.add(AllThings.getGoTo_zhuShi());
				totalList.add(AllThings.getGoTo_Str());
				totalList.add(AllThings.getNoSans_Keyword());
				totalList.add(AllThings.getNoSans_Func());
				totalList.add(AllThings.getNoSans_Villber());
				totalList.add(AllThings.getNoSans_Object());
				totalList.add(AllThings.getNoSans_Type());

				totalList.add(AllThings.getNoSans_Char());
				//请您在任何时候都加入getChar，因为它可以适时切割单词

			}

			@Override
			public void OnClearFindWord(Words WordLib)
			{
				Array_Splitor. delSame(getLastfunc(),getKeyword());
				//函数名不可是关键字，但可以和变量或类型重名	
				Array_Splitor.delSame(getLastfunc(),getKeyword());
				//类型不可是关键字
				Array_Splitor.delSame(getBeforetype(),getHistoryVillber());
				//类型不可是变量，类型可以和函数重名
				Array_Splitor.delSame(getBeforetype(),getConstword());
				//类型不可是保留字
				Array_Splitor. delSame(getHistoryVillber(),getKeyword());
				Array_Splitor. delSame(getThoseObject(),getKeyword());
				//变量不可是关键字
				Array_Splitor. delSame(getThoseObject(),getConstword());
				Array_Splitor.delSame(getHistoryVillber(),getConstword());
				//变量不可是保留字
				Array_Splitor.delNumber(getBeforetype());
				Array_Splitor.delNumber(getHistoryVillber());
				Array_Splitor.delNumber(getLastfunc());
				Array_Splitor.delNumber(getThoseObject());
				//去掉数字
			}

			@Override
			public void OnClearFindNodes(int start,int end,String text, List<wordIndex> nodes)
			{
				clearRepeatNode(nodes,end);	
			}
		}



		final public class FinderCSS extends EditFinderListener
		{

			@Override
			public void OnFindWord(List<BaseEdit.DoAnyThing> totalList,Words WordLib)
			{

			}

			@Override
			public void OnFindNodes(List<BaseEdit.DoAnyThing> totalList,Words WordLib)
			{
				AnyThingFactory. AnyThingForCSS CSSThings = mThings.getAnyThingCSS();

				totalList.add(CSSThings.getGoTo_zhuShi());	
				totalList.add(CSSThings.getGoTo_Str());
				totalList.add(CSSThings.getNoSans_Func());
				totalList.add(CSSThings.getCSSDrawer());
				totalList.add(CSSThings.getCSSChecker());


				totalList.add(CSSThings.getDraw_Attribute());	

				totalList.add(CSSThings.getNoSans_Tag());

				totalList.add(CSSThings.getNoSans_Char());
				//请您在任何时候都加入getChar，因为它可以适时切割单词

			}

			@Override
			public void OnClearFindWord(Words WordLib)
			{

			}

			@Override
			public void OnClearFindNodes(int start,int end,String text, List<wordIndex> nodes)
			{
				clearRepeatNode(nodes,end);
				clearRepeatNodeForCSS(text,nodes);
			}

			final public void clearRepeatNodeForCSS(String src,List<wordIndex> nodes){
				//清除优先级低且位置重复的node
				int i;
				for(i=0;i<nodes.size();i++){
					wordIndex now = nodes.get(i);
					if(src.substring(now.start,now.end).equals("-")){
						nodes.remove(i);
						i--;
					}
				}
			}
		}

		final public class FinderHTML extends EditFinderListener
		{

			@Override
			public void OnFindNodes(List<BaseEdit.DoAnyThing> totalList, Words WordLib)
			{
				// TODO: Implement this method
			}


			@Override
			public void OnFindWord(List<BaseEdit.DoAnyThing> totalList,Words WordLib)
			{

			}

			@Override
			public void OnClearFindWord(Words WordLib)
			{

			}

			@Override
			public void OnClearFindNodes(int start,int end,String text, List<wordIndex> nodes)
			{
				reDrawHTML(start,end,text,nodes);
			}


			final protected List<wordIndex> getNodes(String text, String Lua, int now)
			{
				String L = laugua;
				setLuagua(Lua);
				List<wordIndex> tmp = new ArrayList<>();
				FindFor(0,0,text,tmp,new SpannableStringBuilder());
				offsetNode(tmp, now);
				setLuagua(L);
				return tmp;
			}

			final protected List<wordIndex> reDrawHTML(int start,int end,String text,List<wordIndex>nodes)
			{
				List<wordIndex> tmp=new ArrayList<>();
				int now=0,css=-1,js=-1,css_end=-1,js_end=-1;
				try
				{
					while (now != -1)
					{
						css = text.indexOf("<style", now);
						js = text.indexOf("<script", now);
						css_end = text.indexOf("</style", now);
						js_end = text.indexOf("</script", now);
						int min = Array_Splitor.getmin(0, text.length(), css, js, css_end, js_end);
						//找到符合条件的最近tag位置

						if (min == -1)
						{
							break;
							//范围内没有tag了
						}	
						else if (css == min)
						{
							css += 7;
							tmp = getNodes(text.substring(now, css), "xml", now);
							nodes.addAll(tmp);
							now = css;
							//如果是css起始tag，将之前的html染色
						}
						else if (js == min)
						{
							js += 8;
							tmp =  getNodes(text.substring(now, js), "xml", now);
							nodes.addAll(tmp);
							now = js;
							//如果是js起始tag，将之前的html染色
						}
						else if (css_end == min)
						{
							css_end += 8;
							tmp =	getNodes(text.substring(now, css_end), "css", now);
							nodes.addAll(tmp);
							now = css_end;
							//如果是css结束tag，将之间的CSS染色
						}
						else if (js_end == min)
						{
							js_end += 9;
							tmp =	getNodes(text.substring(now, js_end), "java", now);
							nodes.addAll(tmp);
							now = js_end;
							//如果是js结束tag，将之间的js染色
						}
					}

				}
				catch (Exception e)
				{}
				//那最后一段在哪个tag内呢？
				//只要看下个tag
				String s=getText().toString();
				css = s.indexOf("<style", now+start);
				js = s.indexOf("<script", now+start);
				css_end = s.indexOf("</style", now+start);
				js_end = s.indexOf("</script", now+start);

				int min = Array_Splitor.getmin(0,s.length(), css, js, css_end, js_end);
				try
				{
					if (min == -1)
					{
						tmp = getNodes(text.substring(now, text.length()), "xml", now);
						nodes.addAll(tmp);
						//范围内没有tag了
					}	
					else if (css == min)
					{
						tmp = getNodes(text.substring(now, text.length()), "xml", now);
						nodes.addAll(tmp);
						//如果是css起始tag，将之前的xml染色
					}
					else if (js == min)
					{
						tmp = getNodes(text.substring(now, text.length()), "xml", now);
						nodes.addAll(tmp);
						//如果是js起始tag，将之前的xml染色
					}
					else if (css_end == min)
					{
						tmp = getNodes(text.substring(now, text.length()), "css", now);
						nodes.addAll(tmp);
						//如果是css结束tag，将之前的css染色
					}
					else if (js_end == min)
					{
						tmp = getNodes(text.substring(now, text.length()), "java", now);
						nodes.addAll(tmp);
						//如果是js结束tag，将之前的js染色
					}
				}
				catch (Exception e)
				{}
				setLuagua("html");
				return nodes;
			}

		}
		

	}
	
	
	public FinderFactory getFinderFactory(){
		return new FinderFactory();
	}
	public static EditListener getDefaultDrawer(){
		return new DefaultDrawerListener();
	}
	public static EditListener getDefultFormator()
	{
		return new DefaultFormatorListener();
	}
	public EditListener getDefultInsertor()
	{
		return new DefaultInsertorListener();
	}
	public EditCompletorBoxes getCompletorBox(){
		return new EditCompletorBoxes();
	}
	public static EditListener getDefultCanvaser(){
		return new DefaultCanvaser();
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
	

	public void zoomBy(float size){
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
	    pos.start=bounds.centerX();
		pos.end=bounds.centerY();

		int index= tryLine_Start(getText().toString(),offset);
		pos.start=(int)((offset- index)*getTextSize());

		return pos;
	}
	final public wordIndex getRawCursorPos(int offset, int width, int height)
	{
		//获取绝对光标坐标
		wordIndex pos = getCursorPos(offset);
		pos.start=pos.start % width;
		pos.end=pos.end % height;
		return pos;
	}
	final public wordIndex getScrollCursorPos(int offset, int scrollx, int scrolly)
	{
		//获取存在滚动条时的绝对光标坐标
		//当前屏幕起始0相当于scroll滚动量,然后用cursorpos-scroll，就是当前屏幕光标绝对坐标	
		wordIndex pos = getCursorPos(offset);
		pos.start=pos.start - scrollx;
		pos.end=pos.end - scrolly;		
		return pos;
	}

	final public int fromy_getLineOffset(int y)
	{
		float xLine;
		int nowN = 0;
		xLine=y / getLineHeight();

		while (xLine-- > 0)
		{
			nowN=tryLine_End(getText().toString(), nowN + 1);
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
