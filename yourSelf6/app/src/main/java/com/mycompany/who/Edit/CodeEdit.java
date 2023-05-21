package com.mycompany.who.Edit;

import android.content.*;
import android.graphics.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Base.EditMoudle.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import com.mycompany.who.Edit.Base.Share.Share2.*;
import com.mycompany.who.Edit.Base.Share.Share3.*;
import com.mycompany.who.Edit.Base.Share.Share4.*;
import com.mycompany.who.Edit.EditBuilder.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.EditListener.*;
import com.mycompany.who.Edit.EditBuilder.WordsVistor.*;
import java.util.*;
import java.util.concurrent.*;

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
	
	Format & Insert
	
	openWindow
	
	Canvaser
	
	Runnar
	
	Uedo & Redo
	
	Lines & Selection
	
*/

/*

   在基类上开一些接口，另外的，复杂的函数我都设置成了final，不安全的函数设为protected

   从现在开始，所有被调函数，例如reDraw，必须自己管理好线程和IsModify和Ep安全，然后将真正操作交给另一个函数，这样各司其职，降低复杂度，便于修改(修改其中一个不影响另一个)
   
   为了兼容外部的东西，除了EditText，Listener和Words不作为参数传递，其它的都应尽量以参数传递，以使用自己，自己的行为和数据来操作，例如之后的onDrawNodes，它可以给任意的Editable进行染色，但使用的是我的方案

   在写代码时，必须保证当前的代码已经优化成最简的了，才能去继续扩展，扩展前先备份，避免之后出bug

*/
 
/*

   mPrivateFlags和mPublicFlags包含以下内容:

   mPublicFlags
   ↓
   isDraw  禁用所有的自动染色
   isFormat  禁用所有的自动格式化
   isComplete  禁用所有的自动补全
   ...
   
   mPrivateFlags
   ↓
   IsModify / IsModify2    当前编辑器被修改，不再自动做事
   isDraw                  当前编辑器已在染色，不再染色
   isFormat                当前编辑器已在格式化，不再格式化
   isComplete              当前编辑器已在自动补全，不再自动补全
   isUR                    当前编辑器已在Uedo或Redo，不再Uedo Redo
   ...
   
   我们在任何可能修改文本的函数中设置IsModify，这样在onTextChanged中判断，避免死循环
   我们仅在一些需要供我们使用的flag时，才会在函数中设置对应的flag，例如IsDraw，IsFormat，IsUR等
   注意: 我们不在任何主动调用的方法中判断IsModify，而是在自动调用时才需要判断，避免函数失效
   
   因为flag在主线程中设置，而onTextChanged也在主线程中调用，因此在默认情况下，不会死循环，并且每一时刻只有一个函数被执行，所有函数都会自动调用，即使手动调用也不会有问题
   另外的，本人最开始的意图是只有IsModify，只有IsModify起着关键性作用，其它flag意义不大
   如果因人为手动禁用某个flag，这个flag对应的功能将关闭，为了保证flag一直禁用，主动调用函数也是不行的，因为可能在函数中重新设置
   
 
   tryCount          染色或其它工作检查的行数，次数或个数，tryCount的值越小，编辑器速度更快  
   Delayed_Millis    (为所有的post任务设置一个延迟时间，以及缓冲时间
                     您不应该在EditListener中使用Handler，因为它并不安全，所造成的后果本人概不承担
                     您应该使用Delayed_Millis设置一个统一的任务post延时，以由编辑器内部管理何时进行调度)
					 
*/

public class CodeEdit extends Edit implements Drawer,Formator,Completor,UedoWithRedo,Canvaser,Runnar,SelectionSeer,Liner,EditBuilderUser,EditState
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
	
	private int mPrivateFlags;
	private int IsModify;
	  //你应该在所有会修改文本的函数添加设置IsModify，并在ontextChange中适当判断，避免死循环
	  //IsModify管小的函数中的修改，防止从函数中跳到另一个onTextChanged事件
	  //IsModify2管大的onTextChanged事件中的修改，一个onTextChanged事件未执行完，不允许跳到另一个onTextChanged事件
	  //这里IsModify是int类型，这是因为如果用boolean，一个函数中最后设置的IsModify=false会抵消上个函数开头的IsModify=true，这是为了避免套娃调用的问题

	private int lineCount;
	private AdapterView mWindow;
	private StringBuffer laugua;
	private String HTML;
	private Spanned spanStr;

	static EPool2 Ep;
	static EPool3 Epp;
	
	public static int tryCount=1;
	public static int Delayed_Millis = 50;
	public static int mPublicFlags;
	
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

	@Override
	public Edit CreatOne(){
		return new CodeEdit(getContext());
	}
	
	
/*
------------------------------------------------------------------------------------

对Listener和Words进行操作

  trimListener，loadWords及setLuagua都依赖EditBuilder，所以可以设置EditBuilder
  
------------------------------------------------------------------------------------
*/

	@Override
	public void trimListener()
	{
		if(builder!=null)
			builder.trimListener(Info);
	}
	@Override
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
	public boolean setLinerChecker(EditListener li)
	{
		return Info!=null ? Info.addListenerTo(li,LineCheckerIndex):false;
	}
	public EditListener getLinerChecker()
	{
		return Info!=null ? Info.findAListener(LineCheckerIndex):null;
	}
	public boolean setSelectionSeer(EditListener li)
	{
		return Info!=null ? Info.addListenerTo(li,SelectionSeerIndex):false;
	}
	public EditListener getSelectionSeer()
	{
		return Info!=null ? Info.findAListener(SelectionSeerIndex):null;
	}
	
	
/*
 ------------------------------------------------------------------------------------
	 
  我真服了，在super()中会调用setText("")，然后会调用onTextChanged，这时候自己的成员全是null，
	  
  而是最坑的是: 居然也没有与外部类绑定，那么就不能直接在任何函数中返回外部类的成员，因为外部类对象为null
  
 ------------------------------------------------------------------------------------
*/

	public void setPool(ThreadPoolExecutor pool){
		this.pool = pool;
	}	
	public void setWindow(AdapterView Window){
		mWindow = Window;
	}
	@Override
	public void setInfo(EditListenerInfo i){
		Info = i;
	}
	@Override
	public void setWordLib(Words WordLib){
		this.WordLib = WordLib;
	}
	@Override
	public void setEditBuilder(EditBuilder b){
		builder = b;
	}
	
    public ThreadPoolExecutor getPool(){
		return pool;
	}
	public AdapterView getWindow(){
		return mWindow;
	}
	@Override
	public EditListenerInfo getInfo(){
		return Info;
	}
	@Override
	public Words getWordLib(){
		return WordLib;
	}
	@Override
	public EditBuilder getEditBuilder(){
		return builder;
	}
	
	
/*
------------------------------------------------------------------------------------
	 
染色者
 
 reDraw：复杂的染色，其调用onFindNodes与onDrawNodes
 
 prepare：准备文本，然后等待之后get，其调用onFindNodes，onDrawNodes，onPrePare
 
 onFindNodes: 真正的找操作写在这里，可以给很多Listener查找
 
 onDrawNodes: 真正的染色操作写在这里，同时只能有一个Listener修改
 
 onPrePare: 真正的存储操作写在这里，一定要存储
 
 GetString: 如何获取存储的文本
	 
 ------------------------------------------------------------------------------------

Dreawr
	 
	reDraw ->1
	
	prepare ->1 & 2
	  
	    1-> onFindNodes
		
	    1-> onDrawNodes
		
		2-> onPrePare

 ------------------------------------------------------------------------------------
 
 */
	
	/* 立即进行一次默认的完整的染色 */
	@Override
	final public void reDraw(final int start,final int end)
	{	
	    if(IsDraw()){
			//flag值不可被抵消
			return;
		}
	
	    final Editable editor = getText();
		final List<wordIndex> nodes = new ArrayList<>();
			
		long last, now;
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
				long last, now;
				last = System.currentTimeMillis();
				++IsModify; //为保证isxxx安全，不要在子线程中使用它们
				IsDraw(true);
				
				try{
					onDrawNodes(start, end, nodes, editor); 
				}
				catch (Exception e){
					Log.e("DrawNodes Error", e.toString());
				}

				IsDraw(false);
				--IsModify; //为保证isxxx能成功配对，它们必须写在try和catch外，并紧贴try和catch
				Ep.stop(); //Draw完后申请回收nodes，若Ep和isxxx同时出现，它应紧贴isxxx之前或之后后，避免异常		
				now = System.currentTimeMillis();	
				Log.w("After DrawNodes","I'm "+hashCode()+", "+ "I take " + (now - last) + " ms, " + Ep.toString());		
			}
		};
		postDelayed(run,Delayed_Millis);//将UI任务交给主线程
	}
	
	/* FindNodes不会修改文本和启动Ep，所以可以直接调用 */
	public void onFindNodes(final int start, final int end, final String text, final List<wordIndex> nodes)
	{
		//当final修饰一个基本数据类型时，表示该基本数据类型的值一旦在初始化后便不能发生变化；
		//如果final修饰一个引用类型时，则在对其初始化之后就不可以指向其它对象了，但该引用所指向的对象的内容是可以发生变化的
		final Words WordLib = getWordLib();
		EditListener lis = getFinder();
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li!=null && li instanceof EditFinderListener){
				    List<wordIndex> tmp = ((EditFinderListener)li).LetMeFind(start, end, text, WordLib);
				    nodes.addAll(tmp);
				}
				return false;
			}
		};
		lis.dispatchCallBack(run);
		//这就是我要找的，能接纳任意参数，还能兼容任意EditListener的办法:
		//创建匿名内部类对象，让它包含参数，然后让EditListener把接口分发下去，每个Listener回调接口并传递自己，在接口实现中可以使用包含的参数
	}

	/* 会修改文本，不允许直接调用 */
	protected void onDrawNodes(final int start, final int end, final List<wordIndex> nodes, final Spannable editor)
	{
		EditListener li = getDrawer();
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li!=null && li instanceof EditDrawerListener){
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
		long last, now;
		last = System.currentTimeMillis();
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
		now = System.currentTimeMillis();
		Log.w("After PrePare","I'm "+hashCode()+", "+ "I take " + (now - last) + " ms, " + Ep.toString());
		
	}
	
	/* 存储文本 */
	public void onPrePare(int start, int end, String text, List<wordIndex> nodes,Spanned b)
	{
		this.spanStr = b;
		EditDrawerListener li = (EditDrawerListener) getDrawer();
		if(li != null)
		    this.HTML = li.getHTML(nodes,text);	
		else
			this.HTML = myEditDrawerListener.getHTML(nodes,text,null);
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
------------------------------------------------------------------------------------
	
整理者

Format：负责大量文本的格式化，最后返回格式化期间增加的字符数，为了避免多个Formator把文本改的乱七八糟，只能有一个Formator修改文本

onFormat: 修改文本，最后返回插入期间增加的字符数，只能有一个Listener修改文本


Insert：即时插词，可以有多个Insertor插词

onInsert：被Insert时调度

------------------------------------------------------------------------------------

Formator

	 Format ->1

	 Insert ->2 
	 

	    1-> onFormat
  
	    2-> onInsert

------------------------------------------------------------------------------------

*/

    /* 对齐范围内的文本 */
	@Override
	public final int Format(final int start, final int end)
	{
		if(IsFormat()){
			return 0;
		}
		
		//为了安全，禁止重写
		Editable editor = getText();
		int before = editor.length();
		long last, now;
		last = System.currentTimeMillis();
		
		++IsModify;
		IsFormat(true); 	
		
		try{
			onFormat(start, end, editor);
		}
		catch (Exception e){
			Log.e("Format Error", e.toString());
		}
		
		IsFormat(false);
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
				if(li!=null && li instanceof EditFormatorListener){
				    ((EditFormatorListener)li).LetMeFormat(start, end, editor);
				}
				return false;
			}
		};
		li.dispatchCallBack(run);
	}
	
    /* 在指定位置插入后续字符 */
	@Override
	public final int Insert(final int index,final int count)
	{	
		if(IsFormat()){
			return 0;
		}
	
		Editable editor = getText();
		int before = editor.length();
		++IsModify;
		IsFormat(true);
		
		try{
		    onInsert(index,count,editor);
		}
		catch (Exception e){
			Log.e("Insert Error", e.toString());
		}
		
		IsFormat(false);
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
				if(li!=null && li instanceof EditInsertorListener){
				    int selection = ((EditInsertorListener)li).LetMeInsert(editor,index,count);
					setSelection(selection);
				}
				return false;
			}
		}; 
		lis.dispatchCallBack(run);
	}
	

/*
----------------------------------------------------------------------------

提示器
 
 openWindow：打开单词窗口
 
 closeWindow: 关闭窗口
 
 SearchInGroup: 为所有Listener搜索单词
 
 callOnopenWindow: 将要打开窗口了
 
 calc: 如何摆放窗口
 
 
 insertWord：用户选择单词后插入，最后返回插入期间增加的字符数

 onInsertword：用户选择单词后插入时调度
 
----------------------------------------------------------------------------
 	
 Completor


	 openWindow ->1

	 insertWord ->2
	   
	  
	   1-> SearchInGroup

	   1-> callOnopenWindow ->1②

	   1②-> calc
	   
	   2-> onInsertword

 ----------------------------------------------------------------------------

 */
 
    /* 打开窗口，并排列可选单词 */
	@Override
	final public void openWindow()
	{
		final AdapterView Window = getWindow(); 
		if(Window==null || IsComplete()){
			return;
		}
		
		long last, now;
		last = System.currentTimeMillis();
		final WordAdapter<Icon> adapter = WordAdapter.getDefultAdapter();
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
		
		Runnable run = new Runnable()
		{
			public void run()
			{
				Window.setAdapter(adapter);
			    Epp.stop(); //将单词放入Window后回收Icons，在isxxx之前，避免异常
				++IsModify; //我害怕会在callOnopenWindow中修改文本，既然不在子线程，就也加上吧
				
				try{
				    callOnopenWindow(Window);
				}
				catch (Exception e){
				    Log.e("OpenWindow Error", e.toString());
				}
				
				--IsModify;
				Log.w("After OpenWindow","I'm "+hashCode()+", "+ Epp.toString());
		    }
		};
		postDelayed(run,Delayed_Millis);//将UI任务交给主线程
	}
	
	@Override
	public void closeWindow(){
		getWindow().setX(-9999);
	}
	
	/* 在不同集合中找单词 */
	public void SearchInGroup(final String src,final int index,final WordAdapter<Icon> Adapter)
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
				if(li!=null && li instanceof EditCompletorListener){
			        List<Icon> Icons = ((EditCompletorListener)li).LetMeSearch(src,index,wantBefore,wantAfter,before,after,WordLib);
			        Adapter.addAll(Icons,li.hashCode());
			    }
				return false;
			}
		};
	    lis.dispatchCallBack(run);
	}
	
	public void callOnopenWindow(AdapterView Window)
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
		size tmp =new size(), tmp2 = new size();
		tryWordOnce(editor.toString(), index,tmp);
		tryWordAfterOnce(editor.toString(), index,tmp2);
		final size range = new size(tmp.start,tmp2.end);
		
		//遍历所有listener，找到这个单词的放入者，由它自己处理插入
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li!=null && li instanceof EditCompletorListener && li.hashCode() == id){
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
---------------------------------------------------------------

绘画者
   
   你可以进行任意的绘制操作
   
   DrawAndDraw: 为所有Listener绘制
   
   
   onDraw -> DrawAndDraw

---------------------------------------------------------------

*/

    @Override
	protected void onDraw(Canvas canvas)
	{
		if(IsCanvas()){
			//即使禁用了Canvas，也要进行默认绘制
			super.onDraw(canvas);
			return;
		}
		
		//获取当前控件的画笔
        TextPaint paint = getPaint();
		size pos = getCursorPos(getSelectionEnd());
		++IsModify;
		try
		{
		    DrawAndDraw(canvas,paint,pos,myEditCanvaserListener.BeforeDraw);
			super.onDraw(canvas);
			DrawAndDraw(canvas,paint,pos,myEditCanvaserListener.AfterDraw);	
		}
		catch (Exception e){
			Log.e("OnDraw Error", e.toString());
			super.onDraw(canvas); //即使Listener出现问题，也请继续绘制
		}
		--IsModify;
		//我们并不主动进行Canvas的禁用，这没有意义
    }
	
	protected void DrawAndDraw(final Canvas canvas, final TextPaint paint, final size pos, final int flag)
	{
		EditListener lis = getCanvaser();
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li!=null && li instanceof EditCanvaserListener){
			        ((EditCanvaserListener)li).LetMeCanvaser(CodeEdit.this, canvas, paint, pos, flag);
				}
				return false;
			}
		};
		lis.dispatchCallBack(run);
	}
	
	
/*
---------------------------------------------------------------

运行器

 MakeCommand: 在不同状态下制作不同命令
 
 RunCommand: 执行命令
 
 onMakeCommand: 如何制作命令
 
 onRunCommand: 如何运行命令

---------------------------------------------------------------

Runnar

	 MakeCommand ->1

	 RunCommand ->2 


	 1-> onMakeCommand

	 2-> onRunCommand
	 
---------------------------------------------------------------

*/

	@Override
	final public String MakeCommand(final String state)
	{
		if(IsRun()){
			return "";
		}
		
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
				if(li!=null && li instanceof EditRunnarListener){
				    ((EditRunnarListener)li).LetMeMake(CodeEdit.this,state);
				}
				return false;
			}
		};
		li.dispatchCallBack(run);
		return com.toString();
	}
	
	@Override
	final public int RunCommand(String command)
	{
		if(IsRun()){
			return 0;
		}
		
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
				if(li!=null && li instanceof EditRunnarListener){
				    ((EditRunnarListener)li).LetMeRun(CodeEdit.this,command);
			    }
				return false;
			}
		};
		li.dispatchCallBack(run);
		return flag;
	}
	
	
/*
---------------------------------------------------------------

 Liner和SelectionSeer

 一个用于计算行数，以便以后使用

 一个用于检查光标变化，更重要的是调用监听器
 
 我太累了，所以就乱写了

---------------------------------------------------------------

*/

    @Override
	public void onLineChange(final int start,final int before,final int after)
	{
		if(IsLine()){
			return;
		}
		
		try{
			EditListener li = getLinerChecker();
			RunLi run = new RunLi()
			{
				public boolean run(EditListener li)
				{
					if(li!=null && li instanceof EditLineChangeListener){
						((EditLineChangeListener)li).LineChange(start,before,after);
					}
					return false;
				}
			};
			li.dispatchCallBack(run);
		}
		catch(Exception e){
			Log.e("LineChanged Error",e.toString());
		}
	}
	
	@Override
	protected void onSelectionChanged(final int selStart, final int selEnd)
	{
		if(IsSelection()){
			super.onSelectionChanged(selStart,selEnd);
			return;
		}
		
		++IsModify;
		try{
			EditListener li = getSelectionSeer();
			RunLi run = new RunLi()
			{
				public boolean run(EditListener li)
				{
					if(li!=null && li instanceof EditSelectionChangeListener){
						((EditSelectionChangeListener)li).SelectionChange(selStart,selEnd,getText());
					}
					return false;
				}
			};
			li.dispatchCallBack(run);
		}
		catch(Exception e){
			Log.e("onSelectionChanged Error",e.toString());
		}
		--IsModify;
		super.onSelectionChanged(selStart, selEnd);
	}
	
	
/*
---------------------------------------------------------------
   
Uedo和Redo

 以下内容无需重写

 对于stack中的token，在文本修改时自己存储

 Uedo和Redo只负责拿出token并replace

 当修改时，Uedo存储token
 
 当Uedo时，Redo存储token
 
 当Redo时，Uedo存储token
 
 不包含Uedo和Redo造成的修改，这由isUR的状态决定
	
---------------------------------------------------------------

*/

    /* 应用Token到文本，并将其反向转化 */
    final protected void DoAndCastToken(token token)
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
	}

	/* 得到Token并应用到文本，并把转化的Token存入stack */
    final protected int Uedo_()
	{
		token token = stack.getLast();
		int endSelection=getSelectionEnd();
		if (token != null)
		{
			DoAndCastToken(token);
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

	@Override
	final public void Uedo()
	{
		if (stack.Usize()==0 || IsUR())
			return;

		++IsModify;
		IsUR(true);
		try{
			int endSelection = Uedo_();
			setSelection(endSelection);
			//设置光标位置
		}
		catch (Exception e){
			Log.e("Uedo Error",e.toString());
		}
		IsUR(false);
		--IsModify;
	}
	
	@Override
	final public void Redo()
	{
		if (stack.Rsize()==0 || IsUR())
			return;

		++IsModify;
		IsUR(true);
		try{
			int endSelection = Redo_();
			setSelection(endSelection);	
		}
		catch (Exception e){
			Log.e("Redo Error",e.toString());
		}
		IsUR(false);
		--IsModify;
	}
	
	public void clearStackDate(){
		stack.clear();
	}
	
	protected void onGetUR(token token){}
	
	protected void onPutUR(token token){}

	
/*
---------------------------------------------------------------

 核心功能的调度监听器TextWatcher
 
 以下内容无需重写:
 
	onBeforeTextChanged
	 
	put Token
	 
	Add or Del Lines
	 
---------------------------------------------------------------

*/
	public class DefaultText implements TextWatcher
	{	
		/**
		 * 输入框改变前的内容
		 *  charSequence 输入前EditText的文本
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
			}
			catch(Exception e){
				Log.e("BeforeTextChange",e.toString());
			}
		}

		/**
		 * 输入框改变后的内容
		 *  charSequence  输入后EditText的文本
		 *  start 起始光标
		 *  before 输入框中改变前的字符串与起始位置的偏移量（也就是删除字符串的length）
		 *  count 输入字符串的数量（输入一个emoji表情，count打印结果是2）
		 */
		@Override
		public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
		{	
		    try{
		        NowTextChanged(text,start,lengthBefore,lengthAfter);
		    }
			catch(Exception e){
		    	Log.e("NowTextChange",e.toString());
		    }
		}

		/**
		 * editable 输入结束呈现在输入框中的信息
		 */
		@Override
		public void afterTextChanged(Editable p1){}

	}
	
	protected void onBeforeTextChanged(CharSequence str, int start, int count, int after)
	{
		countLineBefore(str,start,count,after);
		saveTokenToStack(str,start,count,after);
	}

	protected void NowTextChanged(CharSequence str, int start, int count, int after)
	{
		countLineAfter(str,start,count,after);
	}
	
	final protected void saveTokenToStack(CharSequence str, int start, int count, int after)
	{
		if (IsUR())
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
				token = new token(start, start+after, str.subSequence(start, start+count));	
			}
			else if (count != 0)
			{
				//如果删除了字符，本次删除了count个字符后达到start，那么上次的字符串就是：
				//从现在start开始，插入start～start+count之间的字符串
				token = new token(start, start, str.subSequence(start, start+count));
			}
			else if (after != 0)
			{
				//如果插入了字符，本次即将从start开始插入after个字符，那么上次的字符串就是：
				//删除现在start～start+after之间的字符串
				token = new token(start, start+after, "");		
			}	
			if(token!=null)
			{
			    stack.put(token);
				onPutUR(token);
			}
		}
		catch (Exception e){}
	}
	
	protected void countLineBefore(CharSequence str, int start, int count, int after)
	{
		 if(count!=0) 
		 { 
		     //在删除\n前，删除行 
		     int line=String_Splitor.Count('\n', str.toString().substring(start,start + count)); 
		     if(line>0){
			     onLineChange(lineCount,line,0);
			     lineCount-=line;
			 }
		 }
	}
	
	protected void countLineAfter(CharSequence str, int start, int count, int after)
	{
		if (after != 0)
	    {
			//增加行
		    int line = String_Splitor.Count('\n', str.toString().substring(start, start + after));	
		    if(line>0){
			    onLineChange(lineCount,0,line);
			    lineCount+=line; 
			}
	    }
	}
	

/*
---------------------------------------------------------------
 
 核心功能的调度函数 onTextChange
 
	-> openWindow
	 
	-> Insert
	 
	-> reDraw
 
	Insert和reDraw并不冲突，即使是延迟染色也没事，因为只有输入时才染色，Insert默认向后插入，所以没事，就算是染色下标超出范围也没事，我们有try，只要不超太多影响结果就可以(怕上次文本没染完，下次就又修改，所以不使用Format)
	
 ---------------------------------------------------------------

*/
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
	{
		
		if(IsModify()){
			return;
			//如果正被修改，不允许再次修改	
		}
		
		if(!IsComplete()&&lengthAfter<=tryCount&&lengthBefore<=tryCount)
		{
			//是否启用自动补全
			if(getPool()!=null){
				//如果设置了pool，将提升效率
				getPool().execute(OpenWindow());
			}
			else{
			    openWindow();
			}
		}
		
		if (lengthAfter != 0)
		{
			//如果没有输入，则不用做什么
		    IsModify(true);	
			
			if (!IsFormat()&&lengthAfter<=tryCount)
			{		
				//是否启用自动format
				Insert(start,lengthAfter);
			}
			
			if(!IsDraw())
			{
				//是否启用自动染色		
				String src = text.toString();
			    size tmp=new size(start,start+lengthAfter);
				tmp.start=tryLine_Start(src,tmp.start);
				tmp.end=tryLine_End(src,tmp.end);	
				
				//试探起始行和之前之后的tryCount行，并染色
				for(int i=1;i<tryCount;++i){
				    tmp.start=tryLine_Start(src,tmp.start-1);
				    tmp.end=tryLine_End(src,tmp.end+1);	
				}
				
				if(getPool()!=null){
					getPool().execute(ReDraw(tmp.start,tmp.end));
				}
				else{
			        reDraw(tmp.start,tmp.end);	
				}
			}
			
			IsModify(false); //双重拦截
		}
		
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		
	}

	
/*
---------------------------------------------------------------

 文本测量函数
 
	 tryWord	//试探前面的单词
	 
	 tryWordAfter //试探后面的单词
	 
	 tryAfterIndex	 //试探后面的下一个非分隔符
	 
	 tryLine_Start   //试探当前下标所在行的起始
	 
	 tryLine_End   //试探当前下标所在行的末尾
	 
	 tryWordOnce   //试探前面的单词(仅一次)
	 
	 tryWordAfterOnce //试探后面的单词(仅一次)
	 
	 getWord 	//获得光标前的单词 
	 
	 getAfterWord	//获得光标后的单词
	 
---------------------------------------------------------------

*/

	final public static void tryWord(CharSequence src,int index,size tmp)
	{
		//试探前面的单词
		try{
			while(fuhao.contains(src.charAt(index)))
				--index;
			tmp.end=index+1;
			while(!fuhao.contains(src.charAt(index)))
				--index;
			tmp.start=index+1;
		}catch(Exception e){
			tmp.start=0;
			tmp.end=0;
		}
	}
	
	final public static void tryWordAfter(CharSequence src,int index,size tmp)
	{
		//试探后面的单词
		try{
			while(fuhao.contains(src.charAt(index)))
				++index;
			tmp.start=index;
			while(!fuhao.contains(src.charAt(index)))
				++index;
			tmp.end=index;
		}catch(Exception e){
			tmp.start=0;
			tmp.end=0;
		}
	}
	
	final public static int tryAfterIndex(CharSequence src,int index)
	{
		//试探后面的下一个非分隔符
		while(index<src.length()
			  &&src.charAt(index)!='<'
			  &&src.charAt(index)!='>'
			  &&spilt.contains(src.charAt(index))){
			++index;
		}
		return index;
	}
	
	final public static int tryLine_Start(String src,int index)
	{
		//试探当前下标所在行的起始
		int start= src.lastIndexOf('\n',index-1);	
		start = start==-1 ? 0:start+1;
		return start;
	}
	
	final public static int tryLine_End(String src,int index)
	{
		//试探当前下标所在行的末尾
		int end=src.indexOf('\n',index);
		end = end==-1 ? src.length():end;
		return end;
	}
	
	final public static void tryWordOnce(CharSequence src,int nowIndex,size tmp)
	{
		//试探纯单词
		int index=nowIndex-1;
	    try{
			while(index>-1&&!fuhao.contains(src.charAt(index)))
				--index;
			tmp.start=index+1;
			tmp.end=nowIndex;
		}catch(Exception e){
			tmp.start=0;
			tmp.end=0;
		}
	}
	
	final public static void tryWordAfterOnce(CharSequence src,int index,size tmp)
	{
		//试探纯单词
	    try{
			tmp.start=index;
			while(index<src.length()&&!fuhao.contains(src.charAt(index)))
				++index;
			tmp.end=index;
		}catch(Exception e){
			tmp.start=0;
			tmp.end=0;
		}
	}
	
	final public static CharSequence getWord(CharSequence src,int offset)
	{
		//获得光标前的纯单词
	    size node = new size();
		tryWordOnce(src, offset,node);
		if (node.end == 0)
			node.end = offset;
		CharSequence want= src.subSequence(node.start, node.end);
		return want;
	}
	
	final public static CharSequence getAfterWord(CharSequence src,int offset)
	{
		//获得光标后面的纯单词
		size node = new size();
		tryWordAfterOnce(src, offset,node);
		if (node.end == 0)
			node.end = src.length();
		CharSequence want= src.subSequence(node.start, node.end);
		return want;
	}


/*
------------------------------------------------------------------------------------

权限

  CodeEdit的所有控制权限，直接方便地设置和获取权限
  
  我们使用mPrivateFlags和mPublicFlags的相同的一位的值共同得出当前编辑器的某个状态
  
  我们设置状态时，仅设置mPrivateFlags，这样当前编辑器的某个功能被禁用
  
  mPublicFlags是共享的，对其设置将对所有编辑器生效
  
------------------------------------------------------------------------------------
*/

	@Override
	public void setEditFlags(int flags)
	{
		mPrivateFlags = flags;
	}
	@Override
	public int getEditFlags()
	{
		return mPrivateFlags;
	}
	
	public void IsModify(boolean is){
		mPrivateFlags = is ? mPrivateFlags|ModifyMask : mPrivateFlags&~ModifyMask;
	}
	public void IsUR(boolean is){
		mPrivateFlags = is ? mPrivateFlags|URMask : mPrivateFlags&~URMask;
	}
	public void IsDraw(boolean is){
		mPrivateFlags = is ? mPrivateFlags|DrawMask : mPrivateFlags&~DrawMask;
	}
	public void IsFormat(boolean is){
		mPrivateFlags = is ? mPrivateFlags|FormatMask : mPrivateFlags&~FormatMask;
	}
	public void IsComplete(boolean is){
		mPrivateFlags = is ? mPrivateFlags|CompleteMask : mPrivateFlags&~CompleteMask;
	}
	public void IsCanvas(boolean is){
		mPrivateFlags = is ? mPrivateFlags|CanvasMask : mPrivateFlags&~CanvasMask;
	}
	public void IsRun(boolean is){
		mPrivateFlags = is ? mPrivateFlags|RunMask : mPrivateFlags&~RunMask;
	}
	public void IsSelection(boolean is){
		mPrivateFlags = is ? mPrivateFlags|SelectionMask : mPrivateFlags&~SelectionMask;
	}
	public void IsLine(boolean is){
		mPrivateFlags = is ? mPrivateFlags|LineMask : mPrivateFlags&~LineMask;
	}

	public boolean IsModify(){
		return (mPrivateFlags&ModifyMask) == ModifyMask || IsModify!=0 || (mPublicFlags&ModifyMask) == ModifyMask;
	}
	public boolean IsUR(){
		return (mPrivateFlags&URMask) == URMask || (mPublicFlags&URMask) == URMask ;
	}
	public boolean IsDraw(){
		return (mPrivateFlags&DrawMask) == DrawMask || (mPublicFlags&DrawMask) == DrawMask;
	}
	public boolean IsFormat(){
		return (mPrivateFlags&FormatMask) == FormatMask || (mPublicFlags&FormatMask) == FormatMask;
	}
	public boolean IsComplete(){
		return (mPrivateFlags&CompleteMask) == CompleteMask || (mPublicFlags&CompleteMask) == CompleteMask;
	}
	public boolean IsCanvas(){
		return (mPrivateFlags&CanvasMask) == CanvasMask || (mPublicFlags&CanvasMask) == CanvasMask;
	}
	public boolean IsRun(){
		return (mPrivateFlags&RunMask) == RunMask || (mPublicFlags&RunMask) == RunMask;
	}
	public boolean IsSelection(){
		return (mPrivateFlags&SelectionMask) == SelectionMask || (mPublicFlags&SelectionMask) == SelectionMask;
	}
	public boolean IsLine(){
		return (mPrivateFlags&LineMask) == LineMask || (mPublicFlags&LineMask) == LineMask;
	}
	

/*
------------------------------------------

  重写默认的函数，以使其更好使用

------------------------------------------
*/

	/* 防止下标越界 */
	@Override
	public void setSelection(int index)
	{
		if(index>=0&&index<=getText().length()){
		    super.setSelection(index);
		}
	}
	
	@Override
	public void setSelection(int start, int stop)
	{
		if(start>=0&&start<=getText().length()&&stop>=0&&stop<=getText().length()){
		    super.setSelection(start, stop);
		}
	}

	/* 防止post失败，导致Ep无法停止 */
	@Override
	public boolean post(Runnable action)
	{
		while(!super.post(action)){
			try{
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
			try{
				Thread.sleep(Delayed_Millis);
			}
			catch (InterruptedException e){}
		}
		return true;
	}

	/* 不用再全部测量了 */
	@Override
	public int getLineCount()
	{
		return lineCount+1;
	}
	
/* 
------------------------------------------------------------------------------------

  受支持的，可在线程中运行的任务
  
  所有函数都默认不直接使用线程，即使可以在线程中运行，也只返回一个线程安全的Runnable，
  
  这样做的好处是: 由您控制如何处理和启动这些任务，而不是每个任务都默认消耗一个线程
	  
------------------------------------------------------------------------------------
*/

	public final Runnable ReDraw(final int start,final int end)
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				reDraw(start,end);
			}
		};
	}
	public final Runnable Prepare(final int start,final int end,final String text)
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				prepare(start,end,text);
			}
		};
	}
	public final Runnable OpenWindow()
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				openWindow();
			}
		};
	}

/* 
---------------------------------------------------------------

其它函数 

  reSAll: 从起始位置开始，反向把字符串中的want替换为to
  
  getCursorPos: 获取光标坐标
  
  getRawCursorPos: 获取绝对光标坐标
  
  getScrollCursorPos: 获取存在滚动条时的绝对光标坐标
  
  getOffsetForPosition: 从坐标获取光标位置
  
  setSpans: 设置一些span
  
  clearSpan: 清除范围内的span
  
  subSpans: 获取span的范围和span
  
---------------------------------------------------------------

*/

	final public void reSAll(int start, int end, String want, CharSequence to)
	{
		++IsModify;
		int len = want.length();
		Editable editor = getText();
		String src=getText().toString().substring(start, end);
		int nowIndex = src.lastIndexOf(want);
		
		while (nowIndex != -1)
		{
			//从起始位置开始，反向把字符串中的want替换为to
			editor.replace(nowIndex + start, nowIndex + start + len, to);	
			nowIndex = src.lastIndexOf(want, nowIndex - 1);
		}
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

	public void setSpans(int start,wordIndex... spans){
		Colors.setSpans(start,getText(),spans);
	}
	public<T> void clearSpan(int start,int end,Class<T> type){
		Colors.clearSpan(start,end,getText(),type);
	}
	public<T> wordIndex[] subSpans(int start,int end,Class<T> type){
		return Colors.subSpans(start,end,getText(),type);
	}

/*  
------------------------------------------------------------------------------------

  为你省下更多开辟和释放空间的时间，但可能占很多内存  
	
    我根本无法将Epool共享，因为无法保证安全，因此外部尽量不要使用
	
	如果需要使用，建议只在Finder和Completor的监听器中使用，并且使用前start，使用后stop
	
	但我仍将保留以供默认的监听器使用，如果您使用默认的监听器，这会提升内存利用率
	
------------------------------------------------------------------------------------

*/
	public static class EPool2 extends EPool<wordIndex>
	{

		@Override
		protected void init(){
			put(onceCount);
		}
		
		@Override
		protected wordIndex creat(){
			return new wordIndex();
		}

		@Override
		protected void resetE(wordIndex E){}
		
	}
	
	public static class EPool3 extends EPool<IconX>
	{

		@Override
		protected void init(){
			put(250);
		}
		
		@Override
		protected IconX creat(){
			return new IconX();
		}
		
		@Override
		protected void resetE(IconX E){}
		
	}

/*  
------------------------------------------------------------------------------------
 
 每一个Edit都有自己的Info，但基本操作是不变，下面将具体的操作交给这些listener
 
 CodeEditListenerInfo实现了EditListenerInfo，并实现了基本功能

 CodeEdit实现了EditListenerInfoUser，可以直接操作Info
 
------------------------------------------------------------------------------------
	 
 可以有多个Finder，但只有一个Drawer，这就是我把Finder与Drawer分开的原因，不用每个Finder都各自Draw

 反之，为了统一Draw，node存储的就是范围和颜色标志，Drawer只要把它们统一设置

 Format和Insert分开也是这个原因，即不用每个Insertor都绑定一个Formator
 
 Completor包含搜索单词和插入单词两大功能，谁搜索到的单词谁插入
 
 Canvaser用于在编辑器的画布上进行绘制，当然可以有多个
 
 Runnar用于制作和运行命令，命令与具体的Runnar有关
 
------------------------------------------------------------------------------------

*/
    public static class CodeEditListenerInfo implements EditListenerInfo
	{
		
		private Map<Integer,EditListener> mlistenerS;

		public CodeEditListenerInfo()
		{
			mlistenerS = Collection_Spiltor.EmptyMap();
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
			else if(li instanceof EditLineChangeListener){
				return addListenerTo(li,LineCheckerIndex);
			}
			else if(li instanceof EditSelectionChangeListener){
				return addListenerTo(li,SelectionSeerIndex);
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
				if(l!=null)
				{
					if(l instanceof EditListenerList)
					{
						if(((EditListenerList)l).remove(li)){
							//它处于一个EditListenerList中
							return true;
						}
					}
					else if(l.equals(li))
					{
						//它是某个EditListener
						int key = Collection_Spiltor.vualeToKey(l,mlistenerS);
						if(mlistenerS.remove(key)!=null){
						    return true;
						}
					}
				}	
			}
			return false;
		}

		@Override
		public EditListener findAListener(Object name)
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
					//目标位置已有一个EditListenerList，就直接加入
					((EditListenerList)l).add(li);
				}
				else if(l instanceof EditListener)
				{
					//目标位置已有一个EditListener，就将它们合并
					EditListenerList list = new myEditListenerList();
					list.add(l);
					list.add(li);
					mlistenerS.remove(toIndex);
					mlistenerS.put(toIndex,list);
				}
			}
			else{
				//否则直接设置
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
 ------------------------------------------------------------------------------------

 每一个Edit都有自己的Words，内部可以自由存储单词，但必须实现接口，通过接口，Words可以设置和获取单词
 
 每一个Edit都应该尽量保存所有的单词，以便在切换语言时仍可使用

 ------------------------------------------------------------------------------------
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
			mchars = Collection_Spiltor.EmptyMap();
			mdates = Collection_Spiltor.EmptyMap();
			mmaps = Collection_Spiltor.EmptyMap();
			
			mchars.put(chars_fuhao,Collection_Spiltor.EmptySet());
			mchars.put(chars_spilt,Collection_Spiltor.EmptySet());
			for(int i=words_key;i<=words_attr;++i){
				mdates.put(i,Collection_Spiltor.EmptySet());
			}
			mmaps.put(maps_zhu,Collection_Spiltor.EmptyMap());
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
		public void setACollectionChars(int index, Collection<Character> words){
			mchars.put(index,Collection_Spiltor.copySet(words));
		}
	    @Override
		public void setACollectionWords(int index, Collection<CharSequence> words){
			mdates.put(index,Collection_Spiltor.copySet(words));
		}
		@Override
		public void setAMapWords(int index, Map<CharSequence, CharSequence> words){
			mmaps.put(index,Collection_Spiltor.copyMap(words));
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
 ---------------------------------------------------------------

 更详细的执行过程，也许你会使用它们，但这样可能很麻烦
 
 ---------------------------------------------------------------
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

		public void SearchInGroup(String src, int index, WordAdapter Adapter)
		
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
		
		public void saveTokenToStack(CharSequence str, int start, int count, int after)
		
		public token DoAndCastToken(token token)
		
		public int Uedo_()

		public int Redo_()
		
		public void onGetUR(token token)	
		
		public void onPutUR(token token)
	
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
	
	public static interface requestByCodeEdit extends IlovePool,IneedWindow,IwantLine{}

}
