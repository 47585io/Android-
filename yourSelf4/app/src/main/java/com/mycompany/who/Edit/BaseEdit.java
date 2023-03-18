package com.mycompany.who.Edit;

import android.content.*;
import android.text.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.*;

import com.mycompany.who.Edit.Share.*;
import java.util.*;
import java.util.concurrent.*;
import com.mycompany.who.Edit.Base.Moudle.*;
import com.mycompany.who.Edit.Base.Edit.*;
import android.util.*;

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
public abstract class BaseEdit extends Edit implements Drawer,Formator,Completor,UedoWithRedo
{
	
	//一百行代码实现代码染色，格式化，自动补全，Uedo，Redo
	protected OtherWords WordLib;
	protected EditDate stack;
	public static EPool2 Ep;
	public static EPool3 Epp;
	protected ThreadPoolExecutor pool;
	
	protected boolean isDraw=false;
	protected boolean isFormat=false;
	protected boolean isUR=false;
	protected int IsModify;
	protected boolean IsModify2;
	//你应该在所有会修改文本的函数添加设置IsModify，并在ontextChange中适当判断，避免死循环
	//IsModify管小的函数中的修改，防止从函数中跳到另一个onTextChanged事件
	//IsModify2管大的onTextChanged事件中的修改，一个onTextChanged事件未执行完，不允许跳到另一个onTextChanged事件
	//这里IsModify是int类型，这是因为如果用boolean，一个函数中最后设置的IsModify=false会抵消上个函数开头的IsModify=true
	
	public EditLine lines;
	public String laugua;
	String HTML;
	SpannableStringBuilder buider;
	
	public static int tryLines=1;
	public static boolean Enabled_Drawer=false;
	public static boolean Enabled_Format=false;
	public static boolean Enabled_Complete;
	
	static{
		Ep=new EPool2();
		Epp=new EPool3();
	}
	
	BaseEdit(Context cont){
	 	super(cont);
		WordLib=new OtherWords(6);
		this.lines=new EditLine(cont);	
		this.stack = new EditDate();
		addTextChangedListener(new DefaultText());
	}
	BaseEdit(Context cont,BaseEdit Edit){
		super(cont,Edit);
		pool = Edit. pool;
		this.WordLib=Edit.WordLib;	
		this.lines=Edit.lines;
		this.stack = new EditDate();
		addTextChangedListener(new DefaultText());
	}
	
	public void setPool(ThreadPoolExecutor pool)
	{
		this.pool = pool;
	}
	public ThreadPoolExecutor getPool()
	{
		return pool;
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
 
 prepare：准备文本，其调用FindFor和getHTML
	 
 _________________________________________
*/

	abstract protected void FindFor(int start,int end,String text,List<wordIndex> nodes,SpannableStringBuilder builder)
	abstract protected void Drawing(int start,int end,List<wordIndex> nodes,SpannableStringBuilder builder)
	

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
		    Editable editor=getText();
		    String text = editor.toString().substring(start,end);
		
		    try{
		        SpannableStringBuilder builder= Colors.ForeColorText(text,nodes);
			    //在Edit中的真实下标开始，将范围内的单词染色
			    editor.replace(start,end,builder);
	
		    }catch(Exception e){
			    Log.e("Draw Don't know！","  Has Error "+e.toString());
		    }
		}
		isDraw=false;
		IsModify--;
	}
	
	final public static String getHTML(List<wordIndex> nodes,String text){
		//中间函数，用于生成HTML文本
		
		if(nodes==null||text==null)
			return "";
		
		StringBuffer arr = new StringBuffer();
		int index=0;
		arr.append("<!DOCTYPE HTML><html><meta charset='UTF-8'/>   <style> * {  padding: 0%; margin: 0%; outline: 0; border: 0; color: #abb2bf;background-color: rgb(28, 32, 37);font-size: 10px;font-weight: 700px;tab-size: 4;overflow: scroll;font-family:monospace;line-height:16px;} *::selection {background-color: rgba(62, 69, 87, 0.4);}</style><body>");
		for(wordIndex node:nodes){
			//如果在上个node下个node之间有空缺的未染色部分，在html文本中也要用默认的颜色染色
			if(node.start>index)
				arr.append(Colors.textForeColor(text.substring(index,node.start),Colors. Default_));
			arr.append(Colors.  textForeColor(text.substring(node.start,node.end),Colors.fromByteToColorS(node.b)));
			index=node.end;
		}
		if(index<text.length())
			arr.append(Colors.  textForeColor(text.substring(index,text.length()),Colors.Default_));
		arr.append("<br><br><br><hr><br><br></body></html>");
		return arr.toString();
	}

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
	
	final public Future reDraw(final int start,final int end){
		//立即进行一次默认的完整的染色	
		Runnable run = new Runnable(){
			@Override
			public void run()
			{
				final String text = getText().subSequence(start,end).toString();
				final List<wordIndex> nodes=new ArrayList<>();
				final SpannableStringBuilder builder = new SpannableStringBuilder();
				if(text==null)
				    return;
				try{
				    FindFor(start,end,text,nodes,builder);//寻找nodes	
					Drawing(start,end,nodes,builder);//染色			
				}catch(Exception e){
					Log.e("reDraw Don't know！","  Has Error "+e.toString());
				}
			}
		};
		if(pool!=null){
		    return pool.submit(run);
		}
		else
			run.run();
			//如果有pool，在子线程中执行
			//否则直接执行
		return null;
	}
	
	final public Future prepare(final int start,final int end,final String text){
		//准备指定文本的颜料
		Runnable run = new Runnable(){

			@Override
			public void run()
			{
				final List<wordIndex> nodes=new ArrayList<>();
				final SpannableStringBuilder builder = new SpannableStringBuilder();
				FindFor(start,end,text.substring(start,end),nodes,builder);
				BaseEdit. this.buider=builder;
				HTML= getHTML(nodes,text.substring(start,end));
			}
		};
		if(pool!=null)
		    return pool.submit(run);
		else
			run.run();
		return null;
	}
	final public void GetString(StringBuilder HTML,SpannableStringBuilder builder){
		//获取准备好了的文本
		if(this.HTML!=null){
		    HTML.append(this.HTML);
			this.HTML=null;
		}
		if(this.buider!=null){
		    builder.append(this.buider);
			this.buider=null;
		}
	}
	
	
	
/*_________________________________________
	
格式化

Format：负责大量文本的格式化

Insert：即时插词->

 ->onInsert：被Insert时调度

________________________________________
*/
    abstract public Future Format(final int start, final int end)
	//涉及Runner，交给子类实现
	abstract public void Insert(int index)
	
/*_________________________________________

 自动补全
 
 openWindow：打开单词窗口 ->
 
 ->SearchOnce：在一个库中搜索单词
 
 ->addSomeWord：将单词添加到窗口
 
 子类应该组织这些函数
 
 insertWord：用户选择单词后插入 ->

 ->onInsertword：用户选择单词后插入时调度
 
	 _________________________________________
 */	
	
	abstract public Future openWindow(ListView Window,int index,ThreadPoolExecutor pool)
	//涉及Runner，交给子类实现
	final public static List<String> SearchOnce(String wantBefore, String wantAfter, String[] target, int before, int after)
	{
		List<String> words=null;
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

	final public static List<String> SearchOnce(String wantBefore, String wantAfter, Collection<String> target, int before, int after)
	{
		//同上
		List<String> words=null;
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

	final public static void addSomeWord(List<String> words, List<Icon> adapter, byte flag)
	{
		//排序并添加一组的单词块
		if (words == null || words.size() == 0)
			return;
		Array_Splitor.sort(words);
		Array_Splitor.sort2(words);
		int icon = Share.getWordIcon(flag);
		for (String word: words)
		{
			Icon token = Epp.get();
			token.setIcon(icon);
			token.setName(word);
			token.setflag(flag);
		    adapter.add(token);
		}

	}
	
	final public void insertWord(String word, int index, int flag)
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
	abstract protected void onInsertword(String word,int index,int flag)		
	//交给子类实现
	
	
/*_________________________________________
   
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
				stack.Reput(token.start, token.start, getText().subSequence(token.start, token.end).toString());
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
				stack.Reput(token.start, token.start + token.src.length(), getText().subSequence(token.start, token.end).toString());
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
				stack.put(token.start, token.start , getText().subSequence(token.start, token.end).toString());
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
				stack.put(token.start, token.start + token.src.length(), getText().subSequence(token.start, token.end).toString());
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
		if (stack == null)
			return;

		EditDate.Token token = null;	
		int endSelection;
		try
		{
			while (true)
			{
				token = stack.getLast();
				endSelection = Uedo_(token);
				setSelection(endSelection);
				//设置光标位置
				EditDate.Token token2=stack.seeLast();
				if (token2 == null)
					return;
				else if (token2.start == token.end)	
					continue;
				//如果token位置紧挨着，持续Uedo	
				else
					break;
			}
		}
		catch (Exception e)
		{
			Log.e("Uedo Error",token.toString()+" "+e.toString());
		}
	}
	public void Redo()
	{
		//批量Redo
		if (stack == null)
			return;

		EditDate.Token token = null;
		int endSelection;
		try
		{
			while (true)
			{
				token = stack.getNext();
				endSelection = Redo_(token);
				setSelection(endSelection);
				EditDate.Token token2=stack.seeNext();
				if (token2 == null)
					return;
				else if (token2.start == token.end)	
					continue;
				else
					break;
			}
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
	 
	 Uedo
	 
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
		 */
		@Override
		public void beforeTextChanged(CharSequence str, int start, int count, int after)
		{
			onBeforeTextChanged(str,start,count,after);
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
		    NowTextChanged(text,start,lengthBefore,lengthAfter);
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

		if (isDraw || isUR)
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
				stack.put(start, start, str.toString().substring(start , start + count));
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
			openWindow(getWindow(), getSelectionStart(), getPool());
		}
		
		if (lengthAfter != 0)
		{
			//如果没有输入，则不用做什么
		    IsModify2=true;	
		
			if (Enabled_Format)
			{		
				//是否启用自动format
				Insert(start);
				//为了安全，不调用Format
			}
			
			if(Enabled_Drawer){
				//是否启用自动染色
			    
			    //试探起始行和之前之后的tryLines行，并染色
			    wordIndex tmp=new wordIndex(0,0,(byte)0);
			    tmp.start=tryLine_Start(text.toString(),start);
			    tmp.end=tryLine_End(text.toString(),start+lengthAfter);
			    for(int i=1;i<tryLines;i++){
				    tmp.start=tryLine_Start(text.toString(),tmp.start-1);
				    tmp.end=tryLine_End(text.toString(),tmp.end+1);
				}
			    reDraw(tmp.start,tmp.end);
			    
			}
			IsModify2=false;
				
		}
		
		super.onTextChanged(text, start, lengthBefore, lengthAfter);

	}
	
	abstract public ListView getWindow()
	
	
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
	final public static wordIndex tryWord(String src,int index){
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
	
	final public static wordIndex tryWordAfter(String src,int index){
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
	final public static int tryAfterIndex(String src,int index){
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
	
	final public static wordIndex tryWordSplit(String src,int nowIndex){
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
	final public static wordIndex tryWordSplitAfter(String src,int index){
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
	
	
	abstract public void setLuagua(String l)
	
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
	
}