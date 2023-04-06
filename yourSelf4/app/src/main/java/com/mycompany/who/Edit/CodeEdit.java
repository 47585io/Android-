package com.mycompany.who.Edit;

import android.content.*;
import android.graphics.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Base.Edit.*;
import com.mycompany.who.Edit.Base.Moudle.*;
import com.mycompany.who.Edit.ListenerVistor.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import com.mycompany.who.Edit.Share.*;
import com.mycompany.who.Edit.Share.Share1.*;
import com.mycompany.who.Edit.Share.Share2.*;
import com.mycompany.who.Edit.Share.Share3.*;
import com.mycompany.who.Edit.Share.Share4.*;
import java.security.acl.*;
import java.util.*;
import java.util.concurrent.*;
import android.content.res.*;
import java.math.*;
import android.text.style.*;
import static com.mycompany.who.Edit.Base.Colors.*;

/*
   整理是一切的开始
   
     若一个东西可以有很多功能，例如编辑器可以有Format，Draw，Complete，不妨分别写几个接口，然后让其它类根据自己的需要来实现，这叫接口

     工厂是创建一组不同配置的产品的类，通过工厂可以方便地创建某个类的实例，例如上面的编辑器，我们知道，接口只声明方法，不定义实现。也就是说，所有实现它们的编辑器都要重写一遍方法，不管实现逻辑是否相同，这时就可以用工厂，定义一组实现了接口的类，然后根据需要创建对象并返回

     若一些东西属于同一类，例如onTouchListener，onKeyListener，它们都是Listener，不妨写一个ListenerInfo，将它们装到一起，这叫封装
  
     若多个类有相同功能并且也具有父子或兄弟关系，例如Cat和Dog，不妨将相同功能（函数）写在一个共同的基类Animal，这样让它们都继承基类即可，这叫继承
     注意，继承最大的用处是重写方法和实现多态！若可以用组合就可以不用继承
     就算是继承，也尽量使主要功能写在父类中（就算是abstract都可以），子类对功能扩展即可，这样可以完美实现多态（即调用方法相同），并且修改父类时子类不易受影响

     若多个类有相同功能但不具有父子或兄弟关系，不妨将相同功能（函数）写在一个类中，然后各自为它们分配一个此类的成员，然后通过这个成员来进行操作，这叫组合

     若当前的类中的情况比较复杂，不好访问一些东西，那么可以添加一个类，通过这个类专门用于访问一些东西，这叫代理

     写代码时，例如函数参数，能写成基类就尽量写成基类，毕竟我们有多态（向上转型），这样之后改动时也方便
     如果某个函数或类可以使用模板就尽量用模板
     若方法或类可以设为static，一定设置，这样外部可以直接通过类访问

     在类上开一些接口是指故意某些东西不写，而是托付给另一个类的成员实现，并且这个成员可以替换，因此更改内部的成员就可以直接更改效果

     将一堆类放在同一个包中，所有类只做一件事，但这件事需要它们共同完成
     本包中的类只能使用本包及子包中的类，使每个包功能独立，便于拿走使用
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

   从现在开始，所有被调函数，例如Drawing，必须自己管理好线程和IsModify安全，然后将真正操作交给另一个函数

   在写代码时，必须保证当前的代码已经优化成最简的了，才能去继续扩展，扩展前先备份

 */
 
 /*
   Enabled_Drawer  禁用所有的自动染色
   Enabled_Format  禁用所有的自动格式化
   Enabled_Complete  禁用所有的自动补全
   
   IsModify / IsModify2    当前编辑器被修改，不做任何事
   isDraw                  当前编辑器已在染色，不再染色
   isFormat                当前编辑器已在格式化，不再格式化
   isUR                    当前编辑器已在Uedo或Redo，不再Uedo Redo
   
 */
public class CodeEdit extends Edit implements Drawer,Formator,Completor,UedoWithRedo
{
	
	//一千行代码实现代码染色，格式化，自动补全，Uedo，Redo
	protected Words WordLib;
	protected EditDate stack;
	public static EPool2 Ep;
	public static EPool3 Epp;
	protected EditListenerInfo Info;
	protected ThreadPoolExecutor pool;
	protected EditListenerFactory mfactory;
	/*
	  不要随便修改Listener，现在使用pool，并且一组Edit使用一个Info，如果有多个线程同时修改，非常不安全
	  但是我又不能直接用clone分别复制给每一个Edit新的Info，因为我要管Enable，clone的新的Listener没办法管
	  呜呜呜，对不起真的没办法了
	*/
	
	protected boolean isDraw=false;
	protected boolean isFormat=false;
	protected boolean isUR=false;
	protected int IsModify;
	protected boolean IsModify2;
	/*
	  你应该在所有会修改文本的函数添加设置IsModify，并在ontextChange中适当判断，避免死循环
	  IsModify管小的函数中的修改，防止从函数中跳到另一个onTextChanged事件
	  IsModify2管大的onTextChanged事件中的修改，一个onTextChanged事件未执行完，不允许跳到另一个onTextChanged事件
	  这里IsModify是int类型，这是因为如果用boolean，一个函数中最后设置的IsModify=false会抵消上个函数开头的IsModify=true
    */
	
    protected Edit lines;
	protected ListView mWindow;
	protected String laugua;
	protected int Search_Bit;
	protected String HTML;
	protected SpannableStringBuilder buider;
	
	public static int tryLines=1;
	public static boolean Enabled_Drawer=false;
	public static boolean Enabled_Format=false;
	public static boolean Enabled_Complete=false;
	public static int Delayed_Draw = 0;
	
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

	@Override
	public void CopyFrom(Edit target)
	{
		super.CopyFrom(target);
		CodeEdit Edit = (CodeEdit) target;
		this.WordLib=Edit.WordLib;	
		this.stack = new EditDate();
		this.Info = Edit.Info;	
		this.pool = Edit.pool;
		this.mWindow = Edit.mWindow;
		this.lines = Edit.lines;
		this.laugua = Edit.laugua;
		this.Search_Bit = Edit.Search_Bit;
		this.mfactory = Edit.mfactory;
		addTextChangedListener(new DefaultText());
	}

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
		Edit.lines = this.lines;
		Edit.laugua = this.laugua;
		Edit.Search_Bit = this.Search_Bit;
		Edit.mfactory = this.mfactory;
		Edit.addTextChangedListener(new DefaultText());
		Edit.setText(this.getText()); 
	}

	@Override
	public void Creat()
	{
		super.Creat();
		WordLib=new OtherWords(6);
		stack = new EditDate();
		Info = new EditListenerInfo();
		addTextChangedListener(new DefaultText());
		mfactory = new EditListenerFactory2();
		trimListener();
	}
	
	public void trimListener()
	{
		if(mfactory!=null)
			mfactory.trimListener(this);
	}
	public void clearListener()
	{
		if(mfactory!=null)
			mfactory.clearListener(this);
	}
	public List<EditListener> getFinderList(){
		if(Info!=null)
			return Info.mlistenerFS;
		return null;
	}
	public void setDrawer(EditListener li)
	{
		if(Info!=null)
		    Info.mlistenerD = li;
	}
	public EditListener getDrawer()
	{
		if(Info!=null)
		    return Info.mlistenerD;
		return null;
	}
	public void setFormator(EditListener li)
	{
		if(Info!=null)
	        Info.mlistenerM = (EditFormatorListener)li;
	}
	public EditListener getFormator()
	{
		if(Info!=null)
		    return Info.mlistenerM;
		return null;
	}
	public List<EditListener> getInsertorList()
	{
		if(Info!=null)
		    return Info.mlistenerIS;
		return null;
	}
	public List<EditListener> getCompletorList()
	{
		if(Info!=null)
		    return Info.mlistenerCS;
		return null;
	}
	public List<EditListener> getCanvaserList()
	{
		if(Info!=null)
		    return Info.mlistenerVS;
		return null;
	}
	public EditListenerInfo getInfo(){
		return Info;
	}
	public void setInfo(EditListenerInfo i){
		Info=i;
	}

	public void setLuagua(String Lua)
	{
		if(mfactory!=null){
		    laugua = Lua;
	        mfactory.SwitchLuagua(this,Lua);
	    }
	}
	public String getLuagua(){
		return laugua;
	}
	public void setSearchBit(int bit){
		Search_Bit = bit;
	}
	public int getSearchBit(){
		return Search_Bit;
	}
	
	/*
	  我真服了，在构造自己前会调用onTextChanged，这时候自己的成员全是null，
	  
	  而是最坑的是: 居然也没有与外部类绑定，那么就不能直接在任何函数中返回外部类的成员，因为外部类对象为null
	  
	*/
	
	public void setPool(ThreadPoolExecutor pool){
		this.pool = pool;
	}	
	public void setWindow(ListView Window){
		mWindow = Window;
	}
	public void setEditLine(Edit Line){
		lines = Line;
	}
	public void setWordLib(Words WordLib){
		this.WordLib = WordLib;
	}
	public void setFactory(EditListenerFactory f){
		mfactory = f;
	}
	
    public ThreadPoolExecutor getPool(){
		return pool;
	}
	public ListView getWindow(){
		return mWindow;
	}
	public Edit getEditLine(){
		return lines;
	}
	public Words getWordLib(){
		return WordLib;
	}
	public EditListenerFactory getFactory(){
		return mfactory;
	}
	
/*
_________________________________________
	 
 染色
	 
 startFind：调度total对一段文本进行查找，找到的node由total自已装入
 
 Draw：用nodes为文本直接染色
 
 reDraw：更复杂的染色，其调用FindFor与Drawing，加入了线程
 
 FindFor：由其它的函数调用，可以实现复杂的查找
 
 Drawing：由其它的函数调用，可以实现复杂的染色
 
 getHTML：通过nodes制作对应的HTML文本
 
 prepare：准备文本，其调用onFindNodes和getHTML
 
 onFindNodes: 真正的找操作写在这里，可以给很多Listener查找
 
 onDrawNodes: 真正的染色操作写在这里，同时只能有一个Listener修改
 
 注意，不要直接调用FindFor，而是调用obFindNodes
	 
 _________________________________________

Dreawr
	 
	reDraw ->1
	

	  1-> FindFor ->2

	  1-> Drawing ->2


	    2-> onFindNodes

	    2-> onDrawNodes

 _________________________________________

 */
	 
	final public static void startFind(String src,List<DoAnyThing> totalList,List<wordIndex> nodes){
		//开始查找，为了保留换行空格等，只replace单词本身，而不是src文本
		//Spanned本质是用html样式替换原字符串
		//html中，多个连续空格会压缩成一个，换行会替换成空格
		//防止重复（覆盖），只遍历一次
		StringBuffer nowWord = new StringBuffer();
		int nowIndex;
		for(nowIndex=0;nowIndex<src.length();nowIndex++){
			nowWord.append(src.charAt(nowIndex));
			//每次追加一个字符，交给totalList中的任务过滤
			//注意是先追加，index后++

			//如果是其它的，可以使用用户过滤方案
			for(DoAnyThing total:totalList){
				try{
				    int index= total.dothing(src,nowWord,nowIndex,nodes);
				    if(index>=nowIndex){
				        //单词已经找到了，不用找了
						nowIndex=index;
						break;
					}
				}catch(Exception e){
					Log.e("StartFind Don't know！","The total name is"+total.toString()+"  Has Error "+e.toString());
				}
			}
		}
	}

	final public void Draw(int start,int end,List<wordIndex> nodes){
		//反向染色，前面不受后面已有Spanned影响
		IsModify++;
		isDraw=true;

		if(nodes!=null&&nodes.size()!=0){
		    try{
				wordIndex[] tmp = new wordIndex[nodes.size()];
				nodes.toArray(tmp);
			    //在Edit中的真实下标开始，将范围内的单词染色
				Colors.ForeColorText(getText(),tmp,start,null);
		    }catch(Exception e){
			    Log.e("Draw Don't know！","  Has Error "+e.toString());
		    }
		}
		isDraw=false;
		IsModify--;
	}
	
	final public static String getHTML(List<wordIndex> nodes,String text,Colors.ByteToColor2 Color){
		//中间函数，用于生成HTML文本
		
		if(nodes==null||text==null)
			return "";
		
		if(Color==null)
			Color = new Colors.myColor2();
			
		StringBuilder arr = new StringBuilder();
		int index=0;
		arr.append("<!DOCTYPE HTML><html><meta charset='UTF-8'/>   <style> * {  padding: 0%; margin: 0%; outline: 0; border: 0; color: "+Color.getDefultS()+";background-color: "+Color.getDefultBgS()+";font-size: 10px;font-weight: 700px;tab-size: 4;overflow: scroll;font-family:monospace;line-height:16px;} *::selection {background-color: rgba(62, 69, 87, 0.4);}</style><body>");
		for(wordIndex node:nodes){
			//如果在上个node下个node之间有空缺的未染色部分，在html文本中也要用默认的颜色染色
			if(node.start>index)
				arr.append(Colors.textForeColor(text.substring(index,node.start),Color.getDefultS()));
			arr.append(Colors.  textForeColor(text.substring(node.start,node.end),Color.fromByteToColorS(node.b)));
			index=node.end;
		}
		if(index<text.length())
			arr.append(Colors.  textForeColor(text.substring(index,text.length()),Color.getDefultS()));
		arr.append("<br><br><br><hr><br><br></body></html>");
		return arr.toString();
	}
	final public static String getHTML(SpannableStringBuilder b){
		size[] nodes = Colors. subSpanPos(0,b.length(),b,Colors.SpanType);
		Object[] spans = b.getSpans(0,b.length(),Colors.SpanType);
		int index = 0;
		String text = b.toString();
		StringBuilder arr = new StringBuilder();
		arr.append("<!DOCTYPE HTML><html><meta charset='UTF-8'/>   <style> * {  padding: 0%; margin: 0%; outline: 0; border: 0; color: "+Colors.Default_+";background-color: "+Colors.Bg_+";font-size: 10px;font-weight: 700px;tab-size: 4;overflow: scroll;font-family:monospace;line-height:16px;} *::selection {background-color: rgba(62, 69, 87, 0.4);}</style><body>");
		
		for(int i=0;i<spans.length;++i){
			size node = nodes[i];
			if(!( spans[i] instanceof ForegroundColorSpan))
				continue;
			ForegroundColorSpan span = (ForegroundColorSpan) spans[i];
			//如果在上个node下个node之间有空缺的未染色部分，在html文本中也要用默认的颜色染色
			if(node.start>index)
				arr.append(Colors.textForeColor(text.substring(index,node.start),Colors.Default_));
			arr.append(Colors. textForeColor(text.substring(node.start,node.end),Colors.toString(span.getForegroundColor())));
			index=node.end;
		}
		arr.append("<br><br><br><hr><br><br></body></html>");
		return arr.toString();
	}
	
	final public void reDraw(final int start,final int end){
		//立即进行一次默认的完整的染色	
		Runnable run = new Runnable(){
			@Override
			public void run()
			{
				String text = getText().toString();
				List<wordIndex> nodes;
				if(text==null)
				    return;
				try{
				    nodes = FindFor(start,end,text);//寻找nodes	
					Drawing(start,end,text,nodes);//染色			
				}catch(Exception e){
					Log.e("reDraw Don't know！","  Has Error "+e.toString());
				}
			}
		};
		//只在大的函数中使用pool，小的函数中就只是处理过程，这样就比较可控
		if(getPool()!=null){
		    getPool().submit(run);
		}
		else
			run.run();
			//如果有pool，在子线程中执行
			//否则直接执行
	}
	
	final public Future prepare(final int start,final int end,final String text){
		//准备指定文本的颜料
		Runnable run = new Runnable(){

			@Override
			public void run()
			{
				List<wordIndex> nodes = null;
				SpannableStringBuilder b = new SpannableStringBuilder(text.substring(start,end));
				try
				{
					Ep.start();
					nodes = onFindNodes(start, end, text,getFinderList());
					((EditDrawerListener)getDrawer()).setSpan(b,nodes);
					CodeEdit. this.buider = b;
					CodeEdit. this.HTML = getHTML(b);
					//HTML= getHTML(nodes,text.substring(start,end),((EditDrawerListener)getDrawer()).getByteToColor());
					Ep.stop();
				}
				catch (Exception e){}
			}
		};
		if(getPool()!=null)
		    return getPool().submit(run);
		else
			run.run();
		return null;
	}
	public void GetString(StringBuilder HTML,SpannableStringBuilder builder){
		//获取准备好了的文本
		if(this.HTML!=null&&HTML!=null){
		    HTML.append(this.HTML);
			this.HTML=null;
		}
		if(this.buider!=null&&builder!=null){
		    builder.append(this.buider);
			this.buider=null;
		}
	}
	
	//当调用FindFor后，务必调用Drawing停止Ep
	protected final List<wordIndex> FindFor(int start, int end, String text)
	{
		//为了安全，禁止重写
		long last = 0,now = 0;
		last = System.currentTimeMillis();
		List<wordIndex> nodes = null;
		Ep.start();//开始记录
		try
		{
		    nodes= onFindNodes(start, end, text,getFinderList());
		}
		catch (Exception e)
		{
			Log.e("FindNodes Error", e.toString());
		}	

		now = System.currentTimeMillis();
		Log.w("After FindNodes", "I take " + (now - last) + " ms, " + Ep.toString());
		return nodes;
	}
	//FindNodes不会修改文本和使用Ep，所以可以直接调用
	//且其支持任意EditFinderListener查找
	public List<wordIndex> onFindNodes(int start, int end, String text, List<EditListener> lis)throws Exception
	{
		List<wordIndex> nodes = new ArrayList<>();
		if(lis!=null){
			for(EditListener li:lis){
		        List<wordIndex> tmp = ((EditFinderListener)li).LetMeFind(start, end,text,getWordLib(), this);
				nodes.addAll(tmp);
			}
		    return nodes;
		}
		return null;
	}

	protected final void Drawing(final int start, final int end,final String src, final List<wordIndex> nodes)
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
					if (nodes != null)
					    onDrawNodes(start, end,src, nodes,getDrawer());
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
		if (Delayed_Draw == 0){
			//这里为什么要这样判断，直接post不行吗？
			//post是将任务抛给主线程的消息队列了，但什么时候执行就不一定了，很可能会让接下来的修改优先于染色
			//因此我这里判断我在不在子线程中，如果不在就直接run，卡住线程，免得有一些问题
			if(getPool()!=null)
			    post(run);
			else
				run.run();
		}
		else{
			if(getPool()!=null)
			    postDelayed(run, Delayed_Draw);
			else{
				try
				{
					Thread.sleep(Delayed_Draw);
				}
				catch (InterruptedException e)
				{}
				run.run();
			}
		}
		//为了线程安全，涉及UI操作必须抛到主线程	
	}
	//会修改文本，不允许直接调用
	protected void onDrawNodes(int start, int end,String src, List<wordIndex> nodes,EditListener l)throws Exception
	{
		//应该重写这个
		EditDrawerListener li = ((EditDrawerListener)l);
		if(li!=null)
		    li.LetMeDraw(start, end,src, nodes,this);
	}
	
	
/*_________________________________________
	
格式化

Format：负责大量文本的格式化，为了避免多个Formator把文本改的乱七八糟，只能有一个Formator修改文本

onFormat: 修改文本，只能有一个Listener修改文本


Insert：即时插词，只能有一个Listener修改文本

onInsert：被Insert时调度

________________________________________

Formator

	 Format ->1

	 Insert ->2 
	 

	    1-> onFormat
  
	    2-> onInsert

_________________________________________

*/

	public final void Format(final int start, final int end)
	{
		//为了安全，禁止重写

		IsModify++;
		isFormat = true; //在此时才会修改文本

		long last = 0,now = 0;
		last = System.currentTimeMillis();
		try
		{
			onFormat(start, end, getFormator());
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

	protected void onFormat(int start,int end,EditListener l){
		
		EditFormatorListener li = ((EditFormatorListener)l);
		if(li!=null)
			li.LetMeFormat(start, end, this);
	}
	

	public final int Insert(final int index)
	{
		//插入字符
		int count = 0;
		IsModify++;
		isFormat = true;

		try
		{
		    onInsert(index,getInsertorList());
		}
		catch (Exception e)
		{
			Log.e("Insert Error", e.toString());
		}

		isFormat = false;
		IsModify--;
		
		return count;
	}	

	protected void onInsert(int index,List<EditListener> lis)throws Exception
	{
		if(lis!=null){
			for(EditListener li:lis){
		       int p = ((EditInsertorListener)li).LetMeInsert(this, index);
			   if(p>=0)
			       setSelection(p);
			}  
		}
	}
	

/*
_________________________________________

 自动补全
 
 getWindow: 获取窗口
 
 openWindow：打开单词窗口
 
 SearchInGroup: 为所有Listener搜索单词
 
 SearchOnce：在一个库中搜索单词
 
 addSomeWord：将单词添加到窗口
 
 callOnopenWindow: 将要打开窗口了
 
 calc: 如何摆放窗口
 
 
 insertWord：用户选择单词后插入

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
 
	final public void openWindow(int index)
	{
		if(getWindow()==null)
			return;
		
		final CharSequence wantBefore= getWord(index);
		final CharSequence wantAfter = getAfterWord(index);
		//获得光标前后的单词，并开始查找

		Runnable run = new Runnable(){
			@Override
			public void run()
			{
				Epp.start();//开始存储
				long last = 0,now = 0;
				last = System.currentTimeMillis();

				List<Icon> Icons = SearchInGroup(wantBefore, wantAfter, 0, wantBefore.length(),getCompletorList());
				//经过一次查找，Icons里装满了单词
				now = System.currentTimeMillis();
				Log.w("After SearchWords", "I take " + (now - last) + " ms, " + Epp.toString());

				if (Icons != null)
				{
					final WordAdpter<Icon> adapter = new WordAdpter<Icon>(getContext(), Icons, R.layout.WordIcon);
					Runnable run2=new Runnable(){

						@Override
						public void run()
						{
							getWindow().setAdapter(adapter);
							try
							{
							    callOnopenWindow(getWindow());
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
		if (getPool() != null)
		    getPool().submit(run);//因为含有阻塞，所以将任务交给池子
		else
			run.run();
	}

	public List<Icon> SearchInGroup(final CharSequence wantBefore, final CharSequence wantAfter, final int before, final int after,List<EditListener> ls)
	{
		//用多线程在不同集合中找单词
		List<EditListener> Group = ls;
		ThreadPoolExecutor pool = getPool();
		EditListenerItrator.RunLi<List<Icon>> run = new EditListenerItrator.RunLi<List<Icon>>(){

			@Override
			public List<Icon> run(EditListener li)
			{
				return ((EditCompletorListener)li).LetMeCompelet(wantBefore, wantAfter, before, after, WordLib,CodeEdit. this);
			}
		};
		
		if (getPool() != null)
			return EditListenerItrator.foreach(Group, run);
		return EditListenerItrator.foreach(Group, run);
		//阻塞以获得所有单词
	}
	
	//在库中搜索单词，支持Span文本
	final public static List<CharSequence> SearchOnce(CharSequence wantBefore, CharSequence wantAfter, CharSequence[] target, int before, int after)
	{
		List<CharSequence> words=null;
		Array_Splitor. Idea ino = Array_Splitor.getNo();
		Array_Splitor. Idea iyes = Array_Splitor.getyes();
		if (!wantBefore.equals(""))
		//如果前字符串不为空，则搜索
		    words = Array_Splitor.indexsOf(wantBefore, target, before, ino);
		if (!wantAfter.equals("") && words != null)
		//如果前字符串搜索结果不为空并且后字符串不为空，就从之前的搜索结果中再次搜索
		    words = Array_Splitor.indexsOf(wantAfter, words, after, iyes);
		else if (!wantAfter.equals("") && wantBefore.equals(""))
		{
			//如果前字符串为空，但后字符串不为空，则只从后字符串开始搜索
			words = Array_Splitor.indexsOf(wantAfter, target, after, iyes);
		}
		return words;
	}

	final public static List<CharSequence> SearchOnce(CharSequence wantBefore, CharSequence wantAfter, Collection<CharSequence> target, int before, int after)
	{
		//同上
		List<CharSequence> words=null;
		Array_Splitor. Idea ino = Array_Splitor.getNo();
		Array_Splitor. Idea iyes = Array_Splitor.getyes();
		if (!wantBefore.equals(""))
		    words = Array_Splitor.indexsOf(wantBefore, target, before, ino);
		if (!wantAfter.equals("") && words != null)
		    words = Array_Splitor.indexsOf(wantAfter, words, after, iyes);
		else if (!wantAfter.equals("") && wantBefore.equals(""))
		{
			words = Array_Splitor.indexsOf(wantAfter, target, after, iyes);
		}
		return words;
	}

	final public static void addSomeWord(List<CharSequence> words, List<Icon> adapter, byte flag, int icon)
	{
		//排序并添加一组的单词块，支持Span文本
		if (words == null || words.size() == 0)
			return;
		Array_Splitor.sort(words);
		Array_Splitor.sort2(words);
	
		for (CharSequence word: words)
		{
			Icon token = Epp.get();
			token.setIcon(icon);
			token.setName(word);
			token.setflag(flag);
		    adapter.add(token);
		}

	}
	protected void callOnopenWindow(ListView Window)
	{
		if ( getWindow().getAdapter() != null && getWindow().getAdapter().getCount() > 0)
		{
			size pos = calc(this);
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
	
	public size calc(EditText Edit){
		return getCursorPos(Edit.getSelectionStart());
	}
	
	//插入单词，支持Span文本
	final public void insertWord(CharSequence word, int index, int flag)
	{
		IsModify++;
		try
		{
			onInsertword(word,index,flag);
		}
		catch (Exception e)
		{
			Log.e("InsertWord With Complete Error ",e.toString());
		}
		IsModify--;
	}
	protected void onInsertword(CharSequence word, int index, int flag)
	{
		Editable editor = getText();	

		wordIndex tmp = tryWordSplit(editor.toString(), index);
		wordIndex tmp2 = tryWordSplitAfter(editor.toString(), index);

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
	

/*
_________________________________________

   Canvaser
   
   你可以进行任意的绘制操作
   
   DrawAndDraw: 为所有Listener绘制

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
			++IsModify;
		    DrawAndDraw(this,canvas,paint,bounds,EditCanvaserListener.OnDraw,getCanvaserList());
			super.onDraw(canvas);
			DrawAndDraw(this,canvas,paint,bounds,EditCanvaserListener.AfterDraw,getCanvaserList());	
			--IsModify;
		}
		catch (Exception e)
		{
			Log.e("OnDraw Error", e.toString());
		}
		
    }
	
	public void DrawAndDraw(EditText self, Canvas canvas, TextPaint paint, Rect Cursor_bounds,int flag,List<EditListener> ls)
	{
		for (EditListener li:ls)
			((EditCanvaserListener)li).LetMeCanvaser(this, canvas, paint, Cursor_bounds,flag);
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

    final protected int Uedo_(EditDate.Token token)
	{
		IsModify++;
		isUR = true;
		int endSelection=0;
		if (token != null)
		{
			//范围限制
			if (token.start < 0)
				token.start = 0;
			if (token.end > getText().length())
				token.end = getText().length();
			ongetUR(token);

			if (token.src == "")
			{
				stack.Reput(token.start, token.start, getText().subSequence(token.start, token.end));
				//如果Uedo会将范围内字符串删除，则我要将其保存，待之后插入
				getText().delete(token.start, token.end);	
				endSelection = token.start;
			}
			else if (token.start == token.end)
			{
				//如果Uedo会将在那里插入一个字符串，则我要将其下标保存，待之后删除
				stack.Reput(token.start, token.start + token.src.length(), "");
				getText().insert(token.start, token.src);
				endSelection = token.start + token.src.length();
			}
			else
			{
				stack.Reput(token.start, token.start + token.src.length(), getText().subSequence(token.start, token.end));
				//另外的，则是反向替换某个字符串
			    getText().replace(token.start, token.end, token.src);
				endSelection = token.start + token.src.length();
			}
		}
		isUR = false;
		IsModify--;
		return endSelection;
	}

	final protected int Redo_(EditDate.Token token)
	{
		IsModify++;
		isUR = true;
		int endSelection=0;
		if (token != null)
		{
			if (token.start < 0)
				token.start = 0;
			if (token.end > getText().length())
				token.end = getText().length();
			ongetUR(token);

			if (token.src == "")
			{
				stack.put(token.start, token.start , getText().subSequence(token.start, token.end));
				//如果Redo会将范围内字符串删除，则我要将其保存，待之后插入
				getText().delete(token.start, token.end);
				endSelection = token.start;
			}
			else if (token.start == token.end)
			{
				//如果Redo会将在那里插入一个字符串，则我要将其下标保存，待之后删除
				stack.put(token.start, token.start + token.src.length(), "");
				getText().insert(token.start, token.src);
				endSelection = token.start + token.src.length();
			}
			else
			{
				stack.put(token.start, token.start + token.src.length(), getText().subSequence(token.start, token.end));
				//另外的，则是反向替换某个字符串
			    getText().replace(token.start, token.end, token.src);
				endSelection = token.start + token.src.length();
		    }
		}
		isUR = false;
		IsModify--;
		return endSelection;
	}

	public void Uedo()
	{
		//批量Uedo
		if (stack == null||stack.Usize()==0)
			return;

		EditDate.Token token = null;	
		int endSelection;
		try
		{
			token = stack.getLast();
			endSelection = Uedo_(token);
			setSelection(endSelection);
			//设置光标位置
		}
		catch (Exception e)
		{
			Log.e("Uedo Error",token.toString()+" "+e.toString());
		}
	}
	public void Redo()
	{
		//批量Redo
		if (stack == null||stack.Rsize()==0)
			return;

		EditDate.Token token = null;
		int endSelection;
		try
		{
			token = stack.getNext();
			endSelection = Redo_(token);
			setSelection(endSelection);	
		}
		catch (Exception e)
		{
			Log.e("Redo Error",token.toString()+" "+e.toString());
		}
	}
	protected void ongetUR(EditDate.Token token)
	{
	}
	protected void onPutUR(EditDate.Token token)
	{
	}

	
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
		/*
		 输入4个字符，删除一个字符，它们的值变化：
		 0, 0, 1  从0开始插入1个字符
		 1, 0, 1  从1开始插入1个字符
		 2, 0, 1  从2开始插入1个字符
		 3, 0, 1  从3开始插入1个字符
		 3, 1, 0  从4开始删除1个字符，达到3 
		 */

		/*
		 这里需要注意的是，任何replace,insert,append,delete函数中都会调ontextChange
		 另外的，replace并不分两次调用ontextChange，而是直接把删除count与插入after一并传过来，所以都得判断
		 因此，start光标总是以在最前面的位置为准
		 text就是EditText内部SpannableStringBuilder，它们是同步的，SpannableStringBuilder实现了CharSequence， Editable接口，则getText得到的也是这个
		 另外的，getText().subSequence得到的也是SpannableStringBuilder
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
		 *  editable 输入结束呈现在输入框中的信息
		 */
		@Override
		public void afterTextChanged(Editable p1)
		{
			
		}

	}
	
	
	protected void onBeforeTextChanged(CharSequence str, int start, int count, int after){

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
			if (count != 0)
			{
				//如果删除了字符，本次删除了count个字符后达到start，那么上次的字符串就是：
				//从现在start开始，插入start～start+count之间的字符串
				stack.put(start, start, str.subSequence(start , start + count));
				onPutUR(stack.seeLast());
			}
			if (after != 0)
			{
				//如果还插入了字符，本次即将从start开始插入after个字符，那么上次的字符串就是：
				//删除现在start～start+after之间的字符串
				stack.put(start, start + after, "");
				onPutUR(stack.seeLast());
			}					

		}
		catch (Exception e)
		{}
	}

	protected void NowTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter){
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
 
	Insert和reDraw并不冲突，即使是延迟染色也没事，因为只有输入时才染色，Insert默认向后插入，所以没事
	
 _________________________________________

*/
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
	{
		if(IsModify!=0||IsModify2)
			return;
		//如果正被修改，不允许再次修改	
		
		if(Enabled_Complete){
			//是否启用自动补全
			openWindow(getSelectionStart());
		}
		
		if (lengthAfter != 0)
		{
			//如果没有输入，则不用做什么
		    IsModify2=true;	
			
			if (Enabled_Format&&!isFormat)
			{		
				//是否启用自动format
				Insert(start);
				//Format(start,start+lengthAfter);
				//为了安全，不调用Format
			}
			
			if(Enabled_Drawer&&!isDraw){
				//是否启用自动染色
			    
			    //试探起始行和之前之后的tryLines行，并染色
				String src = text.toString();
			    wordIndex tmp=new wordIndex(0,0,(byte)0);
			    tmp.start=tryLine_Start(src,start);
			    tmp.end=tryLine_End(src,start+lengthAfter);
			    for(int i=1;i<tryLines;i++){
				    tmp.start=tryLine_Start(src,tmp.start-1);
				    tmp.end=tryLine_End(src,tmp.end+1);
				}
			    reDraw(tmp.start,tmp.end);
			    
			}
			IsModify2=false;
			
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
	final public static wordIndex tryWord(CharSequence src,int index){
		//试探前面的单词
		wordIndex tmp = Ep.get();
		try{
			while(Array_Splitor. indexOf(src.charAt(index),Words.fuhao)!=-1)
				index--;
			tmp.end=index+1;
			while(Array_Splitor.indexOf(src.charAt(index),Words.fuhao)==-1)
				index--;
			tmp.start=index+1;
		}catch(Exception e){
			return new wordIndex(0,0,(byte)0);
		}
		return tmp;
	}
	
	final public static wordIndex tryWordAfter(CharSequence src,int index){
		//试探后面的单词
		wordIndex tmp = Ep.get();
		try{
			while(Array_Splitor.indexOf(src.charAt(index),Words.fuhao)!=-1)
				index++;
			tmp.start=index;
			while(Array_Splitor.indexOf(src.charAt(index),Words.fuhao)==-1)
				index++;
			tmp.end=index;
		}catch(Exception e){
			return new wordIndex(0,0,(byte)0);
		}
		return tmp;
	}
	final public static int tryAfterIndex(CharSequence src,int index){
		//试探后面的下一个非分隔符
		while(index<src.length()
			  &&src.charAt(index)!='<'
			  &&src.charAt(index)!='>'
			  &&Array_Splitor.indexOf(src.charAt(index),Words.spilt)!=-1){
			index++;
		}
		return index;
	}
	final public static int tryLine_Start(String src,int index){
		//试探当前下标所在行的起始
		int start= src.lastIndexOf('\n',index-1);	
		if(start==-1)
			start=0;
	    else
			start+=1;
		return start;
	}
	final public static int tryLine_End(String src,int index){
		//试探当前下标所在行的末尾
		int end=src.indexOf('\n',index);
		if(end==-1)
			end=src.length();
		return end;
	}
	
	final public static wordIndex tryWordSplit(CharSequence src,int nowIndex){
		//试探纯单词
		int index=nowIndex-1;
	    wordIndex tmp = Ep.get();
		try{
			while(index>-1&&Array_Splitor.indexOf(src.charAt(index),Words.fuhao)==-1)
				index--;
			tmp.start=index+1;
			tmp.end=nowIndex;
		}catch(Exception e){
			return new wordIndex(0,0,(byte)0);
		}
		return tmp;
	}
	final public static wordIndex tryWordSplitAfter(CharSequence src,int index){
		//试探纯单词
	    wordIndex tmp = Ep.get();
		try{
			tmp.start=index;
			while(index<src.length()&&Array_Splitor.indexOf(src.charAt(index),Words.fuhao)==-1)
				index++;
			tmp.end=index;
		}catch(Exception e){
			return new wordIndex(0,0,(byte)0);
		}
		return tmp;
	}
	final public String getWord(int offset)
	{
		//获得光标前的纯单词
	    wordIndex node = tryWordSplit(getText().toString(), offset);
		if (node.end == 0)
			node.end = offset;
		String want= getText().toString().substring(node.start, node.end);
		return want;
	}
	final public String getAfterWord(int offset)
	{
		//获得光标后面的纯单词
		wordIndex node = tryWordSplitAfter(getText().toString(), offset);
		if (node.end == 0)
			node.end = getText().toString().length();
		String want= getText().toString().substring(node.start, node.end);
		return want;
	}
	
	
	//DoAnyThing，用于找nodes
	public static interface DoAnyThing{
		public abstract int dothing(String src,StringBuffer nowWord,int nowIndex,List<wordIndex> nodes);
		//修饰符非常重要，之前没写public，总是会函数执行异常
	}
	
	//权限类
	public static class EditChroot{	
	
	    public EditChroot(){}
		public EditChroot(boolean m,boolean d,boolean f,boolean u){
			IsModify = m;
			isDraw = d;
			isFormat = f;
			isUR = u;
		}
		public EditChroot(EditChroot o){
			compare(o);
		}
		public void compare(EditChroot f){
			IsModify = f.IsModify;
			isDraw = f.isDraw;
			isFormat = f.isFormat;
			isUR = f.isUR;
		}
		public EditChroot getChroot(){
			return new EditChroot(IsModify,isDraw,isFormat,isUR);
		}
		
		public boolean IsModify;
		public boolean isDraw;
		public boolean isFormat;
		public boolean isUR;
	}
	
	
	/* 其它函数 */
	
	public static void clearRepeatNode(List<wordIndex> nodes){
		//清除优先级低且位置重复的node

		if(nodes==null)
			return;

		int i,j;
		for(i=0;i<nodes.size();i++){
			wordIndex now = nodes.get(i);
			if(now.start==now.end){
				nodes.remove(i--);
				continue;
			}
			for(j=i+1;j<nodes.size();j++){
				if( nodes.get(j).equals(now)){
					nodes.remove(j--);
				}
			}
		}
	}
	final public static void offsetNode(List<wordIndex> nodes,int start){

		if(nodes==null)
			return;

		for(wordIndex node:nodes){
			node.start+=start;
			node.end+=start;
		}
	}
	
	final public void reSAll(int start, int end, String want, CharSequence to)
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
		super.zoomBy(size);
		if(lines!=null){
		    lines.zoomBy(size);
		}
	}
	public void compareChroot(EditChroot f){
		IsModify2 = f.IsModify;
		isDraw = f.isDraw;
		isFormat = f.isFormat;
		isUR = f.isUR;
	}
	public EditChroot getChroot(){
		return new EditChroot(IsModify2,isDraw,isFormat,isUR);
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
	public void IsUR(boolean is){
		isUR = is;
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
    为你省下更多开辟和释放空间的时间，但可能占很多内存
*/
	public static class EPool2 extends EPool<wordIndex>
	{

		@Override
		protected wordIndex creat()
		{
			return new wordIndex();
		}

		@Override
		protected void resetE(wordIndex E)
		{
		}

	}

	public static class EPool3 extends EPool<Icon>
	{

		@Override
		protected Icon creat()
		{
			return new Icon();
		}

		@Override
		protected void resetE(Icon E)
		{
		}

	}
	
 /*
 _________________________________________

 更详细的执行过程，也许你会使用它们，但这样可能很麻烦
 
 _________________________________________
 
 */
	public static interface myDrawer extends Drawer{
			
		public List<wordIndex> FindFor(int start, int end, String text)
		
		public void onFindNodes(int start, int end, String text, List<wordIndex>nodes,List<EditListener> lis)throws Exception
		
		public void Drawing(final int start, final int end, final List<wordIndex> nodes)
		
		public void onDrawNodes(int start, int end, List<wordIndex> nodes, EditListener li)throws Exception
		
	}
	
	
	public static interface myFormator extends Formator{

		public void onFormat(int start,int end,EditListener li)
		
		public int onInsert(int index,List<EditListener> lis)throws Exception
	
	}
	
	
	public static interface myCompletor extends Completor{

		public List<Icon> SearchInGroup(final CharSequence wantBefore, final CharSequence wantAfter, final int before, final int after,List<EditListener> lis)
		
		public void callOnopenWindow(ListView Window)
		
		public void insertWord(CharSequence word, int index, int flag)
		
		public void onInsertword(CharSequence word, int index, int flag)
		
		public ListView getWindow()
	
	}
	
	public static interface myCanvaser {
		
		public void DrawAndDraw(EditText self, Canvas canvas, TextPaint paint, Rect Cursor_bounds,int flag,List<EditListener> lis)
		
	}
	
	public static interface myUedoWithRedo extends UedoWithRedo{
		
		public int Uedo_(EditDate.Token token)

		public int Redo_(EditDate.Token token)
		
		public void ongetUR(EditDate.Token token)	
		
		public void onPutUR(EditDate.Token token)
	
		public void onBeforeTextChanged(CharSequence str, int start, int count, int after)
		
	}
	
	public static interface UseListener{
		
		public EditListenerInfo getInfo()
		
		public void setInfo(EditListenerInfo Info)
		
		public void trimListener()
		
		public void clearListener()
		
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

		public ListView getWindow()
		
		public void setWindow(ListView Window)

	}
	
	public static interface IwantLine{
		
		public Edit getEditLine()
		
		public void setEditLine(EditLine line)
		
	}

}
