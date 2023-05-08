package com.mycompany.who.Edit;

import android.content.*;
import android.graphics.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.R;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Base.EditMoudle.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import com.mycompany.who.Edit.Base.Share.Share2.*;
import com.mycompany.who.Edit.Base.Share.Share3.*;
import com.mycompany.who.Edit.Base.Share.Share4.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.EditListener.RunLi;
import java.util.*;
import java.util.concurrent.*;
import com.mycompany.who.Edit.EditBuilder.*;
import com.mycompany.who.Edit.EditBuilder.WordsVistor.*;
import static com.mycompany.who.Edit.CodeEditBuilder.WordsPackets.BaseWordsPacket.*;
import static com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListenerInfo.*;


/*
   整理是一切的开始
   
     若一个东西可以有很多功能，例如编辑器可以有Format，Draw，Complete，不妨分别写几个接口，然后让其它类根据自己的需要来实现，这叫接口

     工厂是创建一组不同配置的产品的类，通过工厂可以方便地创建某个类的实例，
	 
	 例如上面的编辑器，我们知道，接口只声明方法，不定义实现。也就是说，所有实现它们的编辑器都要重写一遍方法，不管实现逻辑是否相同，这时可以使用继承，定义一个实现了接口的类，之后直接继承

     若一些东西属于同一类，例如onTouchListener，onKeyListener，它们都是Listener，不妨写一个ListenerInfo，将它们装到一起，这叫封装
  
     若多个类有相同功能并且也具有父子或兄弟关系，例如Cat和Dog，不妨将相同功能（函数）写在一个共同的基类Animal，这样让它们都继承基类即可，这叫继承
     注意，继承最大的用处是重写方法和实现多态！若可以用组合就可以不用继承
     就算是继承，也尽量使主要功能写在父类中（就算是abstract都可以），子类对功能扩展即可，这样可以完美实现多态（即调用方法相同），并且修改父类时子类不易受影响

     若多个类有相同功能但不具有父子或兄弟关系，不妨将相同功能（函数）写在一个类中，然后各自为它们分配一个此类的成员，然后通过这个成员来进行操作，这叫组合

     若当前的类中的情况比较复杂，不好访问一些东西，那么可以添加一个类，通过这个类专门用于访问一些东西，这叫代理

     写代码时，例如函数参数，能写成基类就尽量写成基类，毕竟我们有多态（向上转型），这样之后改动时也方便
     如果某个函数或类可以使用模板就尽量用模板
     若方法或类可以设为static，一定设置，这样外部可以直接通过类访问
	 不要直接使用某个成员，能写get和set最好，这样便于以后修改

     在类上开一些接口是指故意某些东西不写，而是托付给另一个类的成员实现，并且这个成员可以替换，因此更改内部的成员就可以直接更改效果

     将一堆类放在同一个包中，所有类只做一件事，但这件事需要它们共同完成，多个包之间相对独立，不要各自访问内部的结构
     本包中的类只能使用本包及子包中的类，便于拿走使用
     如果本包中需要做一些繁琐的事，不妨创建一个Share包，在其中写

 */
 
 /*
 
 主要功能：
 
    reDraw
	
	Format
	
	Insert
	
	openWindow
	
	Uedo
	
	Redo
	
	Lines
	
 */

 /*
   在基类上开一些接口，另外的，复杂的函数我都设置成了final

   从现在开始，所有被调函数，例如reDraw，必须自己管理好线程和IsModify和Ep安全，然后将真正操作交给另一个函数
   
   为了兼容外部的东西，除了EditText不作为参数传递，其它的都应尽量以参数传递，例如之后的onDrawNodes，它可以给任意的Editable进行染色

   在写代码时，必须保证当前的代码已经优化成最简的了，才能去继续扩展，扩展前先备份

 */
 
 /*
   Enabled_Drawer  禁用所有的自动染色
   Enabled_Format  禁用所有的自动格式化
   Enabled_Complete  禁用所有的自动补全
   
   IsModify / IsModify2    当前编辑器被修改，不做任何事
   isDraw                  当前编辑器已在染色，不再染色
   isFormat                当前编辑器已在格式化，不再格式化
   isComplete              当前编辑器已在自动补全，不再自动补全
   isUR                    当前编辑器已在Uedo或Redo，不再Uedo Redo
   
 */
public class CodeEdit extends Edit implements Drawer,Formator,Completor,UedoWithRedo,Canvaser,Runnar,EditBuilderUser
{
	//一千行代码实现代码染色，格式化，自动补全，Uedo，Redo
	
	private TwoStack<token> stack;
	private ThreadPoolExecutor pool;
	private Words WordLib;
	private EditListenerInfo Info;
	private EditBuilder builder;
	  //不要随便修改Listener，现在使用pool，并且一组Edit使用一个Info，如果有多个线程同时修改，非常不安全
	  //但是我又不能直接用clone分别复制给每一个Edit新的Info，因为我要管Enable，clone的新的Listener没办法管
	  //呜呜呜，对不起真的没办法了
	
	private boolean isDraw;
	private boolean isFormat;
	private boolean isComplete;
	private boolean isUR;
	private int IsModify;
	private boolean IsModify2;
	  //你应该在所有会修改文本的函数添加设置IsModify，并在ontextChange中适当判断，避免死循环
	  //IsModify管小的函数中的修改，防止从函数中跳到另一个onTextChanged事件
	  //IsModify2管大的onTextChanged事件中的修改，一个onTextChanged事件未执行完，不允许跳到另一个onTextChanged事件
	  //这里IsModify是int类型，这是因为如果用boolean，一个函数中最后设置的IsModify=false会抵消上个函数开头的IsModify=true
	
	private LineSpiltor Line;
	private AdapterView mWindow;
	private StringBuffer laugua;
	private String HTML;
	private Spanned spanStr;

	static EPool2 Ep;
	static EPool3 Epp;
	
	public static int tryLines=1;
	public static boolean Enabled_Drawer=false;
	public static boolean Enabled_Format=false;
	public static boolean Enabled_Complete=false;
	public static int Delayed_Millis = 50;
	
	static{
		Ep=new EPool2();
		Epp=new EPool3();
	}
	
	public CodeEdit(Context cont){
	 	super(cont);
	}
	public CodeEdit(Context cont,AttributeSet attrs){
		super(cont,attrs);
	}
	public CodeEdit(Context cont,CodeEdit Edit){
		super(cont,Edit);
	}

    /* 将target的数据拷贝到自己身上  */
	@Override
	public void CopyFrom(Edit target)
	{
		super.CopyFrom(target);
		CodeEdit Edit = (CodeEdit) target;
		this.WordLib=Edit.WordLib;	
		if(stack==null)
		    this.stack = new TwoStack<>();
		this.Info = Edit.Info;	
		this.pool = Edit.pool;
		this.Line = Edit.Line;
		this.mWindow = Edit.mWindow;
		this.laugua = Edit.laugua;
		this.builder = Edit.builder;
		addTextChangedListener(new DefaultText());
	}

	/* 将自己的数据拷贝到target身上  */
	@Override
	public void CopyTo(Edit target)
	{
		super.CopyTo(target);
		CodeEdit Edit = (CodeEdit) target;
		Edit.WordLib = this.WordLib;	
		Edit.stack = this.stack;
		Edit.Info = this.Info;	
		Edit.pool = this.pool;
		Edit.Line = this.Line;
		Edit.mWindow = this.mWindow;
		Edit.laugua = this.laugua;
		Edit.builder = this.builder;
		Edit.addTextChangedListener(new DefaultText());
		Edit.setText(this.getText()); 
	}

	/* 初始化数据 */
	@Override
	public void Creat()
	{
		super.Creat();
		laugua = new StringBuffer();
		WordLib=new CodeWords();
		stack = new TwoStack<>();
		Info = new CodeEditListenerInfo();
		addTextChangedListener(new DefaultText());
		builder = new CodeEditBuilder();
		trimListener();
		loadWords();
	}
	
	
/*
__________________________________________________________________________________

对Listener和Words进行操作

  trimListener，loadWords及setLuagua都依赖EditBuilder，所以可以设置EditBuilder
  
__________________________________________________________________________________

*/
	public void trimListener()
	{
		if(builder!=null)
			builder.trimListener(Info);
	}
	public void clearListener()
	{
		if(Info!=null)
			Info.clear();
	}
	@Override
	public void loadWords()
	{
		if(builder!=null)
			builder.loadWords(WordLib);
	}
	@Override
	public void clearWords()
	{
		if(WordLib!=null)
		    WordLib.clear();
	}
	public void setLuagua(String Lua)
	{
		if(builder!=null){
		    laugua.replace(0,laugua.length(),Lua);
			builder.SwitchLuagua(this,Lua);
	    }
	}
	public String getLuagua()
	{
		return laugua.toString();
	}
	
	public boolean setFinder(EditListenerList lis)
	{
		return Info!=null ? Info.addListenerTo(lis,FinderIndex):false;
	}
	public EditListener getFinder()
	{
		return Info!=null ? Info.findAListener(FinderIndex):null;
	}
    public boolean setDrawer(EditListener li)
	{
		return Info!=null ? Info.addListenerTo(li,DrawerIndex):false;
	}
	public EditListener getDrawer()
	{
		return Info!=null ? Info.findAListener(DrawerIndex):null;
	}
	public boolean setFormator(EditListener li)
	{
		return Info!=null ? Info.addListenerTo(li,FormatorIndex):false;
	}
	public EditListener getFormator()
	{
		return Info!=null ? Info.findAListener(FormatorIndex):null;
	}
	public boolean setInsertor(EditListener li)
	{
		return Info!=null ? Info.addListenerTo(li,InsertorIndex):false;
	}
	public EditListener getInsertor()
	{
		return Info!=null ? Info.findAListener(InsertorIndex):null;
	}
	public boolean setCompletor(EditListener li)
	{
		return Info!=null ? Info.addListenerTo(li,CompletorIndex):false;
	}
	public EditListener getCompletor()
	{
		return Info!=null ? Info.findAListener(CompletorIndex):null;
	}
	public boolean setCanvaser(EditListener li)
	{
		return Info!=null ? Info.addListenerTo(li,CanvaserIndex):false;
	}
	public EditListener getCanvaser()
	{
		return Info!=null ? Info.findAListener(CanvaserIndex):null;
	}
	public boolean setRunnar(EditListener li)
	{
		return Info!=null ? Info.addListenerTo(li,RunnarIndex):false;
	}
	public EditListener getRunnar()
	{
		return Info!=null ? Info.findAListener(RunnarIndex):null;
	}
	
	
/*
 __________________________________________________________________________________
	 
  我真服了，在super()中会调用setText("")，然后会调用onTextChanged，这时候自己的成员全是null，
	  
  而是最坑的是: 居然也没有与外部类绑定，那么就不能直接在任何函数中返回外部类的成员，因为外部类对象为null
  
 __________________________________________________________________________________
 
*/

	public void setPool(ThreadPoolExecutor pool){
		this.pool = pool;
	}	
	public void setWindow(AdapterView Window){
		mWindow = Window;
	}
	public void setEditLine(LineSpiltor l){
		Line = l;
	}
	public void setInfo(EditListenerInfo i){
		Info = i;
	}
	public void setWordLib(Words WordLib){
		this.WordLib = WordLib;
	}
	public void setEditBuilder(EditBuilder b){
		builder = b;
	}
	
    public ThreadPoolExecutor getPool(){
		return pool;
	}
	public AdapterView getWindow(){
		return mWindow;
	}
	public LineSpiltor getEditLine(){
		return Line;
	}
	public EditListenerInfo getInfo(){
		return Info;
	}
	public Words getWordLib(){
		return WordLib;
	}
	public EditBuilder getEditBuilder(){
		return builder;
	}
	
	
/*
_________________________________________
	 
染色者
 
 reDraw：复杂的染色，其调用onFindNodes与onDrawNodes
 
 prepare：准备文本，然后等待之后get，其调用onFindNodes，onDrawNodes，onPrePare
 
 onFindNodes: 真正的找操作写在这里，可以给很多Listener查找
 
 onDrawNodes: 真正的染色操作写在这里，同时只能有一个Listener修改
 
 onPrePare: 真正的存储操作写在这里，一定要存储
 
 GetString: 如何获取存储的文本
	 
 _________________________________________

Dreawr
	 
	reDraw ->1
	
	prepare ->1 & 2
	  
	    1-> onFindNodes
		
	    1-> onDrawNodes
		
		2-> onPrePare

 _________________________________________

 */
	
	/* 立即进行一次默认的完整的染色 */
	final public void reDraw(final int start,final int end)
	{	
	    final Editable editor = getText();
		final List<wordIndex> nodes = new ArrayList<>();
			
		long last = 0,now = 0;
		last = System.currentTimeMillis();
		Ep.start(); //开始记录
		
		try{
			onFindNodes(start, end, editor.toString(), nodes); 
			//找nodes，即使getFinderList为null，因为我认为onFindNodes也可以不用listener
		}catch (Exception e){
			Log.e("FindNodes Error", e.toString());
		}	
		
		now = System.currentTimeMillis();
		Log.w("After FindNodes","I'm "+hashCode()+", "+ "I take " + (now - last) + " ms, " + Ep.toString());
		//经过一次寻找，nodes里装满了单词，让我们开始染色
		
		Runnable run =new Runnable()
		{
			public void run()
			{		
				long last=0,now=0;
				last = System.currentTimeMillis();
				++IsModify; //为保证isxxx安全，不要在子线程中使用它们
				isDraw = true; 	
				
				try{
					onDrawNodes(start, end, nodes, editor); 
				}
				catch (Exception e){
					Log.e("DrawNodes Error", e.toString());
				}

				isDraw = false;
				--IsModify; //为保证isxxx能成功配对，它们必须写在try和catch外，并紧贴try和catch
				Ep.stop(); //Draw完后申请回收nodes，若Ep和isxxx同时出现，它应紧贴isxxx之前或之后后，避免异常		
				now = System.currentTimeMillis();	
				Log.w("After DrawNodes","I'm "+hashCode()+", "+ "I take " + (now - last) + " ms, " + Ep.toString());		
			}
		};
		post(run);//将UI任务交给主线程
	}
	
	/* FindNodes不会修改文本和启动Ep，所以可以直接调用 */
	public void onFindNodes(final int start, final int end, final String text, final List<wordIndex> nodes)
	{
		final Words WordLib = getWordLib();
		EditListener lis = getFinder();
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if( li instanceof EditFinderListener){
				    List<wordIndex> tmp = ((EditFinderListener)li).LetMeFind(start, end, text, WordLib);
				    nodes.addAll(tmp);
				}
				return false;
			}
		};
		lis.dispatchCallBack(run);
	}

	/* 会修改文本，不允许直接调用 */
	protected void onDrawNodes(final int start, final int end, final List<wordIndex> nodes, final Spannable editor)
	{
		EditListener li = getDrawer();
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li instanceof EditDrawerListener){
				    ((EditDrawerListener)li).LetMeDraw(start, end, nodes, editor);
				}
				return false;
			}
		};
		li.dispatchCallBack(run);
	}
	
	/* 准备指定文本的颜料 */
	final public void prepare(final int start,final int end,final String text)
	{
		List<wordIndex> nodes = new ArrayList<>();
		SpannableStringBuilder b = new SpannableStringBuilder(text);
		
		Ep.start();
		try
		{	
			onFindNodes(start, end, text, nodes);
			onDrawNodes(start, end, nodes, b);
			onPrePare(start, end, text, nodes, b);
		}
		catch (Exception e){
			Log.e("prepare Error",e.toString());
		}
		Ep.stop(); //为保证Ep能成功配对，它们必须写在try和catch外，并贴进try和catch
	}
	
	/* 存储文本 */
	protected void onPrePare(int start, int end, String text, List<wordIndex> nodes,Spanned b)
	{
		this.spanStr = b;
		EditDrawerListener li = (EditDrawerListener) getDrawer();
		if(li != null)
		    this.HTML = li.getHTML(b);	
		else
			this.HTML = myEditDrawerListener.getHTML(b,null);
	}
	
	/* 获取准备好了的文本 */
	public void GetString(StringBuilder HTML, SpannableStringBuilder str)
	{	
		if(this.HTML!=null&&HTML!=null){
		    HTML.append(this.HTML);
			this.HTML=null;
		}
		if(this.spanStr!=null&&str!=null){
		    str.append(this.spanStr);
			this.spanStr=null;
		}
	}
	
	public void setHTML(String HTML){
		this. HTML=HTML;
	}
	public void setSpanStr(Spanned str){
		this. spanStr=str;
	}
	public String getHTML(){
		return HTML;
	}
	public Spanned getsSpanStr(){
		return spanStr;
	}
	
	
/*
_________________________________________
	
整理者

Format：负责大量文本的格式化，最后返回格式化期间增加的字符数，为了避免多个Formator把文本改的乱七八糟，只能有一个Formator修改文本

onFormat: 修改文本，最后返回插入期间增加的字符数，只能有一个Listener修改文本


Insert：即时插词，可以有多个Insertor插词

onInsert：被Insert时调度

________________________________________

Formator

	 Format ->1

	 Insert ->2 
	 

	    1-> onFormat
  
	    2-> onInsert

_________________________________________

*/

    /* 对齐范围内的文本 */
	public final int Format(final int start, final int end)
	{
		//为了安全，禁止重写
		Editable editor = getText();
		int before = editor.length();
		long last = 0,now = 0;
		last = System.currentTimeMillis();
		++IsModify;
		isFormat = true; 	
		
		try{
			onFormat(start, end, editor);
		}
		catch (Exception e){
			Log.e("Format Error", e.toString());
		}
		
		isFormat = false;
		--IsModify;
		now = System.currentTimeMillis();
		Log.w("After Format Replacer","I'm "+hashCode()+", "+ "I take " + (now - last) + " ms," +"The time maybe too Loog！");
	    return editor.length()-before;
	}

	protected void onFormat(final int start, final int end, final Editable editor)
	{	
		EditListener li = getFormator();
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li instanceof EditFormatorListener){
				    ((EditFormatorListener)li).LetMeFormat(start, end, editor);
				}
				return false;
			}
		};
		li.dispatchCallBack(run);
	}
	
    /* 在指定位置插入后续字符 */
	public final int Insert(final int index,final int count)
	{
		Editable editor = getText();
		int before = editor.length();
		++IsModify;
		isFormat = true;
		
		try{
		    onInsert(index,count,editor);
		}
		catch (Exception e){
			Log.e("Insert Error", e.toString());
		}
		
		isFormat = false;
		--IsModify;
		return editor.length()-before;
	}	

	protected void onInsert(final int index,final int count, final Editable editor)
	{
		EditListener lis = getInsertor();
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li instanceof EditInsertorListener){
				    int selection = ((EditInsertorListener)li).LetMeInsert(editor,index,count);
					setSelection(selection);
				}
				return false;
			}
		};
		lis.dispatchCallBack(run);
	}
	

/*
_________________________________________

提示器
 
 getWindow: 获取窗口
 
 openWindow：打开单词窗口
 
 SearchInGroup: 为所有Listener搜索单词
 
 addSomeWord：将单词添加到窗口
 
 callOnopenWindow: 将要打开窗口了
 
 calc: 如何摆放窗口
 
 
 insertWord：用户选择单词后插入，最后返回插入期间增加的字符数

 onInsertword：用户选择单词后插入时调度
 
_________________________________________
 	
 Completor


	 openWindow ->1

	 insertWord ->2
	   
	  
	   1-> SearchInGroup

	   1-> callOnopenWindow ->1②

	   1②-> calc
	   
	   2-> onInsertword

 _________________________________________

 */
 
    /* 打开窗口，并排列可选单词 */
	final public void openWindow()
	{
		final AdapterView Window = getWindow(); 
		if(Window==null)
			return;
		
		long last = 0,now = 0;
		last = System.currentTimeMillis();
		final WordAdpter adapter = new WordAdpter(R.layout.WordIcon);
		Epp.start();//开始存储
		
		try{
		    SearchInGroup(getText().toString(),getSelectionEnd(),adapter);
		}
		catch(Exception e){
			Log.e("SearchWord Error", e.toString());
		}
		//经过一次查找，Icons里装满了单词
		now = System.currentTimeMillis();
		Log.w("After SearchWords","I'm "+hashCode()+", "+ "I take " + (now - last) + " ms, " + Epp.toString());
		
		Runnable run2=new Runnable()
		{
			public void run()
			{
				Window.setAdapter(adapter);
			    Epp.stop(); //将单词放入Window后回收Icons，在isxxx之前，避免异常
			    isComplete=true;
				++IsModify; //我害怕会在callOnopenWindow中修改文本，既然不在子线程，就也加上吧
				
				try{
				    callOnopenWindow(Window);
				}
				catch (Exception e){
				    Log.e("OpenWindow Error", e.toString());
				}
				
				--IsModify;
				isComplete=false;
				Log.w("After OpenWindow","I'm "+hashCode()+", "+ Epp.toString());
		    }
		};
		post(run2);//将UI任务交给主线程
	}
	
	public void closeWindow(){
		getWindow().setX(-9999);
	}
	
	/* 在不同集合中找单词 */
	public void SearchInGroup(final String src,final int index,final WordAdpter Adapter)
	{
		EditListener lis = getCompletor();
		final Words WordLib = getWordLib();
		final CharSequence wantBefore= getWord(src,index);
		final CharSequence wantAfter = getAfterWord(src,index);
		final int before = 0;
		final int after = wantBefore.length();
		//获得光标前后的单词，并开始查找
		
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li instanceof EditCompletorListener){
			        List<Icon> Icons = ((EditCompletorListener)li).LetMeSearch(src,index,wantBefore,wantAfter,before,after,WordLib);
			        Adapter.addAll(Icons,li.hashCode());
			    }
				return false;
			}
		};
	    lis.dispatchCallBack(run);
	}
	
    /* 排序并添加一组相同icon的单词块到adapter，支持Span文本 */
	final public static void addSomeWord(List<CharSequence> words, List<Icon> adapter, int icon)
	{
		if (words == null || words.size() == 0)
			return;
		Array_Splitor.sort(words);
		Array_Splitor.sort2(words);
	
		for (CharSequence word: words)
		{
			IconX token = (IconX) Epp.get();
			token.setIcon(icon);
			token.setName(word);
		    adapter.add(token);
		}
	}
	/* 排序并添加一组的单词块，支持Span文本 */
	final public static void addSomeWord(List<CharSequence> words, List<Icon> adapter, String path)
	{
		if (words == null || words.size() == 0)
			return;
		Array_Splitor.sort(words);
		Array_Splitor.sort2(words);

		for (CharSequence word: words)
		{
			IconX token = (IconX) Epp.get();
			token.setPath(path);
			token.setName(word);
		    adapter.add(token);
		}
	}
	
	protected void callOnopenWindow(AdapterView Window)
	{
		if (Window.getAdapter() != null && Window.getAdapter().getCount() > 0)
		{
			size pos = calc(this);
			Window.setX(pos.start);
			Window.setY(pos.end);
		}
		else{
			//如果删除字符后没有了单词，则移走
			closeWindow();
		}
	}	
	public size calc(EditText Edit){
		return getCursorPos(Edit.getSelectionStart());
	}
	
	/* 插入单词，支持Span文本 */
	final public int insertWord(CharSequence word, int index, int id)
	{
		Editable editor = getText();
		int before = editor.length();
		++IsModify;
		try{
			onInsertword(editor,word,index,id);
		}
		catch (Exception e){
			Log.e("InsertWord With Complete Error ",e.toString());
		}
		--IsModify;
		return editor.length()-before;
	}
	protected void onInsertword(final Editable editor,final CharSequence word, final int index, final int id)
	{
		EditListener lis = getCompletor();
		wordIndex tmp = tryWordSplit(editor.toString(), index);
		wordIndex tmp2 = tryWordSplitAfter(editor.toString(), index);
		final size range = new size(tmp.start,tmp2.end);
		
		//遍历所有listener，找到这个单词的放入者，由它自己处理插入
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li instanceof EditCompletorListener && li.hashCode() == id){
				    int selection = ((EditCompletorListener)li).LetMeInsertWord(editor,index,range,word);
				    setSelection(selection);
				    return true;
				}
				return false;
			}
		};
		
		if(!lis.dispatchCallBack(run)){
		    //没有找到listener，就执行默认操作
		    editor.replace(tmp.start, tmp2.end, word);
		    setSelection(tmp.start + word.length());
		    //把光标移动到最后
		}
	}
	

/*
_________________________________________

绘画者
   
   你可以进行任意的绘制操作
   
   DrawAndDraw: 为所有Listener绘制
   
   
   onDraw -> DrawAndDraw

_________________________________________

*/

    @Override
	protected void onDraw(Canvas canvas)
	{
		//获取当前控件的画笔
        TextPaint paint = getPaint();
		size pos = getCursorPos(getSelectionEnd());
		++IsModify;
		try
		{
		    DrawAndDraw(canvas,paint,pos,myEditCanvaserListener.OnDraw);
			super.onDraw(canvas);
			DrawAndDraw(canvas,paint,pos,myEditCanvaserListener.AfterDraw);	
		}
		catch (Exception e){
			Log.e("OnDraw Error", e.toString());
			super.onDraw(canvas); //即使Listener出现问题，也请继续绘制
		}
		--IsModify;
    }
	
	protected void DrawAndDraw(final Canvas canvas, final TextPaint paint, final size pos, final int flag)
	{
		EditListener lis = getCanvaser();
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li instanceof EditCanvaserListener){
			        ((EditCanvaserListener)li).LetMeCanvaser(CodeEdit.this, canvas, paint, pos, flag);
				}
				return false;
			}
		};
		lis.dispatchCallBack(run);
	}
	
	
/*
_________________________________________

运行器

 MakeCommand: 在不同状态下制作不同命令
 
 RunCommand: 执行命令
 
 onMakeCommand: 如何制作命令
 
 onRunCommand: 如何运行命令

_________________________________________

Runnar

	 MakeCommand ->1

	 RunCommand ->2 


	 1-> onMakeCommand

	 2-> onRunCommand
	 
_________________________________________

*/

	@Override
	final public String MakeCommand(final String state)
	{
		String com = "";
		++IsModify;
		try{
			com = onMakeCommand(state);
		}
		catch(Exception e){
			Log.e("onMakeCommand Error",e.toString());
		}
		--IsModify;
		return com;//为保证isxxx能成功配对，请不要提前返回
	}

	protected String onMakeCommand(final String state)
	{
		StringBuffer com = new StringBuffer();
		EditListener li = getRunnar();
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				((EditRunnarListener)li).LetMeMake(CodeEdit.this,state);
				return false;
			}
		};
		li.dispatchCallBack(run);
		return com.toString();
	}
	
	@Override
	final public int RunCommand(String command)
	{
		int flag = 0;
		++IsModify;
		try{
			flag = onRunCommand(command);
		}
		catch(Exception e){
			Log.e("onRunCommand Error",e.toString());
		}
		--IsModify;
		return flag;
	}

	protected int onRunCommand(final String command)
	{
		int flag = 0;
		EditListener li = getRunnar();
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				((EditRunnarListener)li).LetMeRun(CodeEdit.this,command);
				return false;
			}
		};
		li.dispatchCallBack(run);
		return flag;
	}
	
	
/*
_________________________________________
   
Uedo和Redo

 以下内容无需重写

 对于stack中的token，在文本修改时自己存储

 Uedo和Redo只负责拿出token并replace

 当修改时，Uedo存储token
 
 当Uedo时，Redo存储token
 
 不包含Uedo和Redo造成的修改，这由isUR的状态决定
	
_________________________________________

*/

    /* 应用Token到文本，并将其反向转化 */
    final protected token DoAndCastToken(token token)
	{
		CharSequence text;
		Editable editor = getText();
		if (token.start < 0)
			token.start = 0;
		if (token.end > editor.length())
			token.end = editor.length();
		onGetUR(token);

		if (token.src.equals(""))
		{	
			//如果token会将范围内字符串删除，则我要将其保存，待之后插入
			text = editor.subSequence(token.start, token.end);
			editor.delete(token.start, token.end);	
			token.set(token.start, token.start, text);
		}
		else if (token.start == token.end)
		{
			//如果token会将在那里插入一个字符串，则我要将其下标保存，待之后删除
			editor.insert(token.start, token.src);
			token.set(token.start, token.start + token.src.length(), "");
		}
		else
		{
			//另外的，则是反向替换某个字符串
			text = editor.subSequence(token.start, token.end);
			editor.replace(token.start, token.end, token.src);
			token.set(token.start, token.start + token.src.length(), text);
		}
		return token;
	}

	/* 得到Token并应用到文本，并把转化的Token存入stack */
    final protected int Uedo_()
	{
		token token = stack.getLast();
		int endSelection=getSelectionEnd();
		if (token != null)
		{
			token = DoAndCastToken(token);
			stack.Reput(token);
			endSelection = token.end;
		}
		return endSelection;
	}

	/* 得到Token并应用到文本，并把转化的Token存入stack */
	final protected int Redo_()
	{
		token token = stack.getNext();
		int endSelection=getSelectionEnd();
		if (token != null)
		{
			DoAndCastToken(token);
			stack.put(token);
			endSelection = token.end;
		}
		return endSelection;
	}

	final public void Uedo()
	{
		if (stack.Usize()==0)
			return;

		++IsModify;
		isUR = true;
		try{
			int endSelection = Uedo_();
			setSelection(endSelection);
			//设置光标位置
		}
		catch (Exception e){
			Log.e("Uedo Error",e.toString());
		}
		isUR = false;
		--IsModify;
	}
	
	final public void Redo()
	{
		if (stack.Rsize()==0)
			return;

		++IsModify;
		isUR = true;
		try{
			int endSelection = Redo_();
			setSelection(endSelection);	
		}
		catch (Exception e){
			Log.e("Redo Error",e.toString());
		}
		isUR = false;
		--IsModify;
	}
	
	public void clearStackDate(){
		stack.clear();
	}
	
	protected void onGetUR(token token){}
	
	protected void onPutUR(token token){}

	
/*
_________________________________________

 核心功能的调度监听器TextWatcher
 
	 onBeforeTextChanged
	 
	 put Token
	 
	 Add or Del Lines
	 
_________________________________________

*/
	public class DefaultText implements TextWatcher
	{
		
		/**
		 * 输入框改变前的内容
		 *  charSequence 输入前字符串
		 *  start 起始光标，在最前面的位置
		 *  count 删除字符串的数量（这里的count是用str.length()计算的，因为删除一个emoji表情，count打印结果是 2）
		 *  after 输入框中改变后的字符串与起始位置的偏移量（也就是输入字符串的length）
		 */
		/**
		 * 输入4个字符，删除一个字符，它们的值变化：
		 * 0, 0, 1  从0开始插入1个字符
		 * 1, 0, 1  从1开始插入1个字符
		 * 2, 0, 1  从2开始插入1个字符
		 * 3, 0, 1  从3开始插入1个字符
		 * 3, 1, 0  从4开始删除1个字符，达到3 
		 */
		/**
		 * 这里需要注意的是，任何replace,insert,append,delete函数中都会调ontextChange
		 * 另外的，replace并不分两次调用ontextChange，而是直接把删除count与插入after一并传过来，所以都得判断
		 * 因此，start光标总是以在最前面的位置为准
		 * text就是EditText内部SpannableStringBuilder，它们是同步的，SpannableStringBuilder实现了CharSequence， Editable接口，则getText得到的也是这个
		 * 另外的，getText().subSequence得到的也是SpannableStringBuilder
		 */
		@Override
		public void beforeTextChanged(CharSequence str, int start, int count, int after)
		{
			try{
			    onBeforeTextChanged(str,start,count,after);
			}catch(Exception e){
				Log.e("BeforeTextChange",e.toString());
			}
		}

		/**
		 * 输入框改变后的内容
		 *  charSequence 字符串信息
		 *  start 起始光标
		 *  before 输入框中改变前的字符串与起始位置的偏移量（也就是删除字符串的length）
		 *  count 输入字符串的数量（输入一个emoji表情，count打印结果是2）
		 */
		@Override
		public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
		{	
		    try{
		        NowTextChanged(text,start,lengthBefore,lengthAfter);
		    }catch(Exception e){
		    	Log.e("NowTextChange",e.toString());
		    }
		}

		/**
		 * editable 输入结束呈现在输入框中的信息
		 */
		@Override
		public void afterTextChanged(Editable p1)
		{
			
		}

	}
	
	protected void onBeforeTextChanged(CharSequence str, int start, int count, int after)
	{
		/*
		 if(count!=0 && !isDraw) 
		 { 
		 //在删除\n前，删除行 
		 int size=String_Splitor.Count('\n', str.toString().substring(start,start + count)); 
		 lines. delLines(size); 
		 }
		 */

		if (isUR)
		{
			return;
			//如果它是由于Uedo本身或无需处理的（例如染色）造成的修改，则不能装入
			//另一个情况是，Uedo需要保存格式化时，额外插入的文本
		}

		try
		{
			token token = null;
			if(count!= 0 && after!=0)
			{
				//如果删除了字符并且插入字符，本次删除了count个字符后达到start，并且即将从start开始插入after个字符
				//那么上次的字符串就是：替换start~start+after之间的字符串为start~start+count之间的字符串
				token = new token(start,start+after,str.subSequence(start,start+count));	
			}
			else if (count != 0)
			{
				//如果删除了字符，本次删除了count个字符后达到start，那么上次的字符串就是：
				//从现在start开始，插入start～start+count之间的字符串
				token = new token(start, start, str.subSequence(start , start + count));
			}
			else if (after != 0)
			{
				//如果插入了字符，本次即将从start开始插入after个字符，那么上次的字符串就是：
				//删除现在start～start+after之间的字符串
				token = new token(start, start + after, "");		
			}	
			if(token!=null)
			{
			    stack.put(token);
				onPutUR(token);
			}
		}
		catch (Exception e){}
	}

	protected void NowTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
	{
		/*
		 if (!isDraw&&lengthAfter != 0){
		 int size=String_Splitor.Count('\n', text.toString().substring(start, start + lengthAfter));	
		 lines. addLines(size);
		 //增加行
		 }*/
	}
	

/*
_________________________________________
 
 核心功能的调度函数 onTextChange
 
	-> openWindow
	 
	-> Insert
	 
	-> reDraw
 
	Insert和reDraw并不冲突，即使是延迟染色也没事，因为只有输入时才染色，Insert默认向后插入，所以没事，就算是染色下标超出范围也没事，我们有try，只要不超太多影响结果就可以(怕上次文本没染完，下次就又修改，所以不使用Format)
	
 _________________________________________

*/
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
	{
		
		if(IsModify!=0||IsModify2)
			return;
		//如果正被修改，不允许再次修改	
		
		if(Enabled_Complete&&!isComplete)
		{
			//是否启用自动补全
			if(getPool()!=null)
				getPool().execute(OpenWindow());
			else
			    openWindow();
		}
		
		if (lengthAfter != 0)
		{
			//如果没有输入，则不用做什么
		    IsModify2=true;	
			
			if (Enabled_Format&&!isFormat)
			{		
				//是否启用自动format
				Insert(start,lengthAfter);
				//Format(start,start+lengthAfter);
				//为了安全，不调用Format
			}
			
			if(Enabled_Drawer&&!isDraw)
			{
				//是否启用自动染色		
				String src = text.toString();
			    wordIndex tmp=new wordIndex();
			    tmp.start=tryLine_Start(src,start);
			    tmp.end=tryLine_End(src,start+lengthAfter);
			  	
				//试探起始行和之前之后的tryLines行，并染色
				for(int i=1;i<tryLines;i++){
				    tmp.start=tryLine_Start(src,tmp.start-1);
				    tmp.end=tryLine_End(src,tmp.end+1);
				}
				if(getPool()!=null)
					getPool().execute(ReDraw(tmp.start,tmp.end));
				else
			        reDraw(tmp.start,tmp.end);	
			}
			
			IsModify2=false; //双重拦截
		}
		
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		
	}

	
/*
_________________________________________

 文本测量函数
 
	 tryWord	//试探前面的单词
	 
	 tryWordAfter //试探后面的单词
	 
	 tryAfterIndex	//试探后面的下一个非分隔符
	 
	 tryLine_Start //试探当前下标所在行的起始
	 
	 tryLine_End //试探当前下标所在行的末尾
	 
	 tryWordSplit //试探纯单词
	 
	 tryWordSplitAfter //试探后面的纯单词
	 
	 getWord 	//获得光标前的纯单词 
	 
	 getAfterWord	//获得光标后的纯单词
	 
_________________________________________

*/

	final public static wordIndex tryWord(CharSequence src,int index)
	{
		//试探前面的单词
		wordIndex tmp = Ep.get();
		try{
			while(fuhao.contains(src.charAt(index)))
				index--;
			tmp.end=index+1;
			while(!fuhao.contains(src.charAt(index)))
				index--;
			tmp.start=index+1;
		}catch(Exception e){
			tmp.start=0;
			tmp.end=0;
			return tmp;
		}
		return tmp;
	}
	
	final public static wordIndex tryWordAfter(CharSequence src,int index)
	{
		//试探后面的单词
		wordIndex tmp = Ep.get();
		try{
			while(fuhao.contains(src.charAt(index)))
				index++;
			tmp.start=index;
			while(!fuhao.contains(src.charAt(index)))
				index++;
			tmp.end=index;
		}catch(Exception e){
			tmp.start=0;
			tmp.end=0;
			return tmp;
		}
		return tmp;
	}
	
	final public static int tryAfterIndex(CharSequence src,int index)
	{
		//试探后面的下一个非分隔符
		while(index<src.length()
			  &&src.charAt(index)!='<'
			  &&src.charAt(index)!='>'
			  &&spilt.contains(src.charAt(index))){
			index++;
		}
		return index;
	}
	
	final public static int tryLine_Start(String src,int index)
	{
		//试探当前下标所在行的起始
		int start= src.lastIndexOf('\n',index-1);	
		if(start==-1)
			start=0;
	    else
			start+=1;
		return start;
	}
	
	final public static int tryLine_End(String src,int index)
	{
		//试探当前下标所在行的末尾
		int end=src.indexOf('\n',index);
		if(end==-1)
			end=src.length();
		return end;
	}
	
	final public static wordIndex tryWordSplit(CharSequence src,int nowIndex)
	{
		//试探纯单词
		int index=nowIndex-1;
	    wordIndex tmp = Ep.get();
		try{
			while(index>-1&&!fuhao.contains(src.charAt(index)))
				index--;
			tmp.start=index+1;
			tmp.end=nowIndex;
		}catch(Exception e){
			tmp.start=0;
			tmp.end=0;
			return tmp;
		}
		return tmp;
	}
	
	final public static wordIndex tryWordSplitAfter(CharSequence src,int index)
	{
		//试探纯单词
	    wordIndex tmp = Ep.get();
		try{
			tmp.start=index;
			while(index<src.length()&&!fuhao.contains(src.charAt(index)))
				index++;
			tmp.end=index;
		}catch(Exception e){
			tmp.start=0;
			tmp.end=0;
			return tmp;
		}
		return tmp;
	}
	
	final public static CharSequence getWord(CharSequence src,int offset)
	{
		//获得光标前的纯单词
	    wordIndex node = tryWordSplit(src, offset);
		if (node.end == 0)
			node.end = offset;
		CharSequence want= src.subSequence(node.start, node.end);
		return want;
	}
	
	final public static CharSequence getAfterWord(CharSequence src,int offset)
	{
		//获得光标后面的纯单词
		wordIndex node = tryWordSplitAfter(src, offset);
		if (node.end == 0)
			node.end = src.length();
		CharSequence want= src.subSequence(node.start, node.end);
		return want;
	}


/*
__________________________________________________________________________________

权限类

  封装了CodeEdit的所有控制权限，直接方便地设置和获取权限
  
  遗憾的是，CodeEdit内部不直接拥有EditChroot，但也提供了兼容函数EditChroot和getChroot
  
  除此之外，也可以使用IsModify，IsDraw等函数直接设置和修改某个权限
 
__________________________________________________________________________________

*/
	public static class EditChroot
	{	
	
	    public EditChroot(){}
		public EditChroot(boolean m,boolean d,boolean f,boolean c,boolean u){
			set(m,d,f,c,u);
		}
		public EditChroot(EditChroot o){
			compare(o);
		}
		public void set(boolean m,boolean d,boolean f,boolean c,boolean u)
		{
			IsModify = m;
			isDraw = d;
			isFormat = f;
			isComplete = c;
			isUR = u;
		}
		public void compare(EditChroot f)
		{
			IsModify = f.IsModify;
			isDraw = f.isDraw;
			isFormat = f.isFormat;
			isComplete = f.isComplete;
			isUR = f.isUR;
		}
		public EditChroot getChroot(){
			return new EditChroot(IsModify,isDraw,isFormat,isComplete,isUR);
		}
		
		public boolean IsModify;
		public boolean isDraw;
		public boolean isFormat;
		public boolean isComplete;
		public boolean isUR;
		
	}
	
	public void compareChroot(EditChroot f)
	{
		IsModify2 = f.IsModify;
		isDraw = f.isDraw;
		isFormat = f.isFormat;
		isComplete = f.isComplete;
		isUR = f.isUR;
	}
	public EditChroot getChroot(){
		return new EditChroot(IsModify2||IsModify!=0,isDraw,isFormat,isComplete,isUR);
	}
	public void IsModify(boolean is){
		IsModify2 = is;
	}
	public void IsDraw(boolean is){
		isDraw = is;
	}
	public void IsFormat(boolean is){
		isFormat = is;
	}
	public void IsComplete(boolean is){
		isComplete = is;
	}
	public void IsUR(boolean is){
		isUR = is;
	}
	public boolean IsModify(){
		return IsModify2||IsModify!=0;
	}
	public boolean IsDraw(){
		return isDraw;
	}
	public boolean IsFormat(){
		return isFormat;
	}
	public boolean IsComplete(){
		return isComplete;
	}
	public boolean IsUR(){
		return isUR;
	}
	

/*
_________________________________________

  重写默认的函数，以使其更好使用

_________________________________________

*/

	/* 防止下标越界 */
	@Override
	public void setSelection(int index)
	{
		if(index>=0&&index<=getText().length())
		    super.setSelection(index);
	}
	@Override
	public void setSelection(int start, int stop)
	{
		if(start>=0&&start<=getText().length()&&stop>=0&&stop<=getText().length())
		    super.setSelection(start, stop);
	}

	/* 防止post失败，导致Ep无法停止 */
	@Override
	public boolean post(Runnable action)
	{
		while(!super.post(action)){
			try
			{
				Thread.sleep(Delayed_Millis);
			}
			catch (InterruptedException e){}
		}
		return true;
	}
	
	@Override
	public boolean postDelayed(Runnable action, long delayMillis)
	{
		while(!super.postDelayed(action,delayMillis)){
			try
			{
				Thread.sleep(Delayed_Millis);
			}
			catch (InterruptedException e){}
		}
		return true;
	}

	
/* 
__________________________________________________________________________________

  受支持的，可在线程中运行的任务
  
  所有函数都默认不直接使用线程，即使可以在线程中运行，也只返回一个线程安全的Runnable，
  
  这样做的好处是: 由您控制如何处理和启动这些任务，而不是每个任务都默认消耗一个线程
	  
__________________________________________________________________________________
	 
*/

	public final Runnable ReDraw(final int start,final int end)
	{
		return new Runnable(){

			@Override
			public void run()
			{
				reDraw(start,end);
			}
		};
	}
	public final Runnable Prepare(final int start,final int end,final String text)
	{
		return new Runnable(){

			@Override
			public void run()
			{
				prepare(start,end,text);
			}
		};
	}
	public final Runnable OpenWindow()
	{
		return new Runnable(){

			@Override
			public void run()
			{
				openWindow();
			}
		};
	}

	
/* 
_________________________________________

其它函数 

  reSAll: 从起始位置开始，反向把字符串中的want替换为to
  
  getCursorPos: 获取光标坐标
  
  getRawCursorPos: 获取绝对光标坐标
  
  getScrollCursorPos: 获取存在滚动条时的绝对光标坐标
  
  fromy_getLineOffset: 从纵坐标获取行
  
  fromPos_getCharOffset: 从坐标获取光标位置
  
  setSpans: 设置一些span
  
  clearSpan: 清除范围内的span
  
  subSpanPos: 获取span的范围
  
_________________________________________

*/

	final public void reSAll(int start, int end, String want, CharSequence to)
	{
		++IsModify;
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
		--IsModify;
	}
	
	final public size getCursorPos(int offset)
	{
		//获取光标坐标
		int lines= getLayout().getLineForOffset(offset);
		Rect bounds = new Rect();
		//任何传参取值都必须new
		size pos = new size();
		getLineBounds(lines, bounds);
	    pos.start = bounds.centerX();
		pos.end = bounds.centerY();

		int index= tryLine_Start(getText().toString(), offset);
		pos.start = (int)((offset - index) * getTextSize());

		return pos;
	}
	final public size getRawCursorPos(int offset, int width, int height)
	{
		//获取绝对光标坐标
		size pos = getCursorPos(offset);
		pos.start = pos.start % width;
		pos.end = pos.end % height;
		return pos;
	}
	final public size getScrollCursorPos(int offset, int scrollx, int scrolly)
	{
		//获取存在滚动条时的绝对光标坐标
		//当前屏幕起始0相当于scroll滚动量,然后用cursorpos-scroll，就是当前屏幕光标绝对坐标	
		size pos = getCursorPos(offset);
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
	
	public void setSpans(size[] nodes,Object[] spans,int start){
		Colors.setSpans(nodes,spans,getText());
	}
	public<T> void clearSpan(int start,int end,Class<T> type){
		Colors.clearSpan(start,end,getText(),type);
	}
	public<T> size[] subSpanPos(int start,int end,Class<T> type){
		return Colors.subSpanPos(start,end,getText(),type);
	}

	
/*  
__________________________________________________________________________________

  为你省下更多开辟和释放空间的时间，但可能占很多内存  
	
    Ep应该比较安全，任意时间内，只有一个线程中的一个Edit能获取一个
	
	获取到的这个元素只属于这个Edit，之后获取的元素是之后的事

	若池子正启用，一时半会这个元素不会重置，也不用担心了
	
	如果马上stop了，虽然会清除本次拿走的元素，但也只是把下标偏移，但不重置元素

    如果池子正关闭，您硬要从中拿，那么池子会宽容地将下标向后移
	
	之后再启动，之后获取的元素是之后的事
	
__________________________________________________________________________________
	
*/
	public static class EPool2 extends EPool<wordIndex>
	{

		@Override
		protected void init()
		{
			put(onceCount);
		}
		
		@Override
		protected wordIndex creat()
		{
			return new wordIndex();
		}

		@Override
		protected void resetE(wordIndex E){}
		
	}
	
	public static class EPool3 extends EPool<Icon>
	{

		@Override
		protected void init()
		{
			put(250);
		}
		
		@Override
		protected Icon creat()
		{
			return new IconX();
		}
		
		@Override
		protected void resetE(Icon E){}
		
	}
	
	public static wordIndex getANode()
	{
		return Ep.get();	
	}
	
	public static Icon getAIcon()
	{
		return Epp.get();
	}
	
	public static void requestDisbledEpool(boolean is,boolean is2)
	{
		Ep.isDisbled(is);
		Epp.isDisbled(is2);
	}
	

/*  
__________________________________________________________________________________
 
 每一个Edit都有自己的Info，但基本操作是不变，下面将具体的操作交给这些listener
 
 CodeEditListenerInfo实现了EditListenerInfo，并实现了基本功能

 CodeEdit实现了EditListenerInfoUser，可以直接操作Info
__________________________________________________________________________________
	 
 可以有多个Finder，但只有一个Drawer，这就是我把Finder与Drawer分开的原因，不用每个Finder都各自Draw

 反之，为了统一Draw，node存储的就是范围和颜色标志，Drawer只要把它们统一设置

 Format和Insert分开也是这个原因，即不用每个Insertor都绑定一个Formator
 
 Completor包含搜索单词和插入单词两大功能，谁搜索到的单词谁插入
 
 Canvaser用于在编辑器的画布上进行绘制，当然可以有多个
 
 Runnar用于制作和运行命令，命令与具体的Runnar有关
 
__________________________________________________________________________________

*/
    public static class CodeEditListenerInfo implements EditListenerInfo
	{
		
		private Map<Integer,EditListener> mlistenerS = Collection_Spiltor.EmptyMap();

		public CodeEditListenerInfo()
		{
			//我们希望它们一开始就是EditListenerList
			mlistenerS.put(FinderIndex,new myEditListenerList());
			mlistenerS.put(InsertorIndex,new myEditListenerList());
			mlistenerS.put(CompletorIndex,new myEditListenerList());
			mlistenerS.put(CanvaserIndex,new myEditListenerList());
		}
		
		@Override
	    public boolean addAListener(EditListener li) 
		{
			if(li==null)
				return false;
				
			if(li instanceof EditFinderListener){
				return addListenerTo(li,FinderIndex);
			}
			else if(li instanceof EditDrawerListener){
				return addListenerTo(li,DrawerIndex);
			}
			else if(li instanceof EditFormatorListener){
				return addListenerTo(li,FormatorIndex);
			}
			else if(li instanceof EditInsertorListener){
				return addListenerTo(li,InsertorIndex);
			}
			else if(li instanceof EditCompletorListener){
				return addListenerTo(li,CompletorIndex);
			}
			else if(li instanceof EditCanvaserListener){
				return addListenerTo(li,CanvaserIndex);
			}
			else if(li instanceof EditRunnarListener){
				return addListenerTo(li,RunnarIndex);
			}
			return false;
		}

		@Override
		public boolean delAListener(EditListener li) 
		{	
			if(li==null)
				return false;
				
			for(EditListener l:mlistenerS.values())
			{
				if(l!=null && l instanceof EditListenerList){
					if(((EditListenerList)l).remove(li)){
						return true;
					}
				}
				else if(l!=null && l.equals(li)){
					int key = Collection_Spiltor.vualeToKey(l,mlistenerS);
					mlistenerS.remove(key);
					return true;
				}
			}
			return false;
		}

		@Override
		public EditListener findAListener(String name)
		{	
			for(EditListener li:mlistenerS.values())
			{
				EditListener l = li==null ? null: li.findListenerByName(name);
				if(l!=null){
					return l;
				}
			}
			return null;
		}
		
		@Override
		public boolean addListenerTo(EditListener li, int toIndex)
		{
			EditListener l = findAListener(toIndex);
			if(l!=null)
			{
				if(l instanceof EditListenerList){
					((EditListenerList)l).add(li);
				}
				else if(l instanceof EditListener){
					EditListenerList list = new myEditListenerList();
					list.add(l);
					list.add(li);
					mlistenerS.remove(toIndex);
					mlistenerS.put(toIndex,list);
				}
			}
			else{
				mlistenerS.remove(toIndex);
			    mlistenerS.put(toIndex,li);
			}
			return true;
		}
		
		@Override
		public boolean delListenerFrom(int fromIndex){		
		    return mlistenerS.remove(fromIndex)!=null;
		}

		@Override
		public EditListener findAListener(int fromIndex){
			return mlistenerS.get(fromIndex);
		}

		@Override
		public int size(){
			return mlistenerS.size();
		}
		@Override
	    public void clear(){
			mlistenerS.clear();
		}
		@Override
		public boolean contrans(EditListener li){
			return mlistenerS.containsValue(li);
		}
		
	}
	

 /*
 __________________________________________________________________________________

 每一个Edit都有自己的Words，内部可以自由存储单词，但必须实现接口，通过接口，Words可以设置和获取单词
 
 每一个Edit都应该尽量保存所有的单词，以便在切换语言时仍可使用

 __________________________________________________________________________________
 
 */
	public static class CodeWords implements Words
	{
		//所有单词使用Map存储，以使index可以为任意的值
		private Map<Integer,Collection<Character>> mchars;
		private Map<Integer,Collection<CharSequence>> mdates;
		private Map<Integer,Map<CharSequence,CharSequence>> mmaps;
		//支持保存Span单词，但可能有一些异常

		public CodeWords(){
			init();
		}
		public void init()
		{
			//即使未使用，也先装入空的集合，以使get不为null
			mchars = EmptyMap();
			mdates = EmptyMap();
			mmaps = EmptyMap();
			
			mchars.put(chars_fuhao,EmptySet());
			mchars.put(chars_spilt,EmptySet());
			for(int i=words_key;i<=words_attr;++i){
				mdates.put(i,EmptySet());
			}
			mmaps.put(maps_zhu,EmptyMap());
		}

		@Override
		public Collection<Character> getACollectionChars(int index){
			return mchars.get(index);
		}
		@Override
		public Collection<CharSequence> getACollectionWords(int index){
			return mdates.get(index);
		}
		@Override
		public Map<CharSequence, CharSequence> getAMapWords(int index){
			return mmaps.get(index);
		}

		@Override
		public void setACollectionChars(int index, Collection<Character> words)
		{
			Set set = EmptySet();
			Collection_Spiltor.addAll(set,words);
			mchars.put(index,set);
		}
	    @Override
		public void setACollectionWords(int index, Collection<CharSequence> words)
		{
			Set set = EmptySet();
			Collection_Spiltor.addAll(set,words);
			mdates.put(index,set);
		}
		@Override
		public void setAMapWords(int index, Map<CharSequence, CharSequence> words)
		{
			Map map = EmptyMap();
			Collection_Spiltor.addAll(map,words);
			mmaps.put(index,map);
		}
		
		public Map EmptyMap(){
			return Collections.synchronizedMap(new HashMap());
		}
		public Set EmptySet(){
			return Collections.synchronizedSet(new HashSet());
		}
		
		@Override
		public void clear(){
			init();
		}
		@Override
		public int size(){
			return mdates.size()+mmaps.size()+mchars.size();
		}
		@Override
		public boolean contrans(int index){
			return mchars.containsKey(index) || mdates.containsKey(index) || mmaps.containsKey(index);
		}
		
	}
	
	
 /*
 _________________________________________

 更详细的执行过程，也许你会使用它们，但这样可能很麻烦
 
 _________________________________________
 
 */
 
	public static interface myDrawer extends Drawer{
			
		public void onFindNodes(int start, int end, String text, List<wordIndex> nodes)
		
		public void onDrawNodes(int start, int end, List<wordIndex> nodes, Editable editor)
		
		public void prepare(int start, int end, String text)
		
		public void onPrepare(int start, int end, String text, List<wordIndex> nodes, Spanned spanStr)
		
	}
	
	public static interface myFormator extends Formator{

		public int onFormat(int start, int end, Editable editor)
		
		public int onInsert(int index, int count, Editable editor)
	
	}
	
	public static interface myCompletor extends Completor{

		public void SearchInGroup(String src, int index, WordAdpter Adapter)
		
		public void callOnopenWindow(View Window)
		
		public int insertWord(CharSequence word, int index, int flag)
		
		public void onInsertword(Editable editor, CharSequence word, int index, int flag)
	
	}
	
	public static interface myCanvaser extends Canvaser {
		
		public void DrawAndDraw(Canvas canvas, TextPaint paint, size pos, int flag)
		
	}
	
	public static interface myRunnar extends Runnar{
		
		public String onMakeCommand(String state)
		
		public int onRunCommand(String command)
		
	}
	
	public static interface myUedoWithRedo extends UedoWithRedo{
		
		public token DoAndCastToken(token token)
		
		public int Uedo_()

		public int Redo_()
		
		public void onGetUR(token token)	
		
		public void onPutUR(token token)
	
	}
	
	public static interface myChroot{
		
		public void compareChroot(EditChroot f)
			
		public EditChroot getChroot()
		
	}
	
	public static interface IlovePool{
		
		public void setPool(ThreadPoolExecutor pool)
		
		public ThreadPoolExecutor getPool()
		
	}
	
	public static interface IneedWindow{

		public AdapterView getWindow()
		
	}
	
	public static interface IwantLine{
		
		public EditLine getEditLine()
		
	}
	
	public static interface requestWithCodeEdit extends IlovePool,IneedWindow,IwantLine{}

}
