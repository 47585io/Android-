package com.who.Edit;

import android.content.*;
import android.text.*;
import android.util.*;
import com.who.Edit.Base.*;
import com.who.Edit.Base.Share.Share1.*;
import com.who.Edit.Base.Share.Share3.*;
import com.who.Edit.EditBuilder.*;
import com.who.Edit.EditBuilder.ListenerVistor.*;
import com.who.Edit.EditBuilder.ListenerVistor.EditListener.*;
import com.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import com.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.EditListener.*;
import static com.who.Edit.EditBuilder.ListenerVistor.EditListenerInfo.*;
import com.who.Edit.EditBuilder.WordsVistor.*;
import java.util.*;
import java.util.concurrent.*;
import com.who.Edit.Base.Share.Share2.*;


/* CodeEdit扩展了Edit的功能 */
public class CodeEdit extends Edit implements EditBuilderUser
{

	private Words WordLib;
	private EditListenerInfo Info;
	private EditBuilder EditBuilder;
	private Stack<token> mLast, mNext;
	
	private int IsModify;
	private int mPrivateFlags;
	public static int mPublicFlags;
	
	private String luagua;
	private ThreadPoolExecutor pool;
	private List<Icon> Icons;
	private myEditCompletorListener.CompleteListener mLi;
	public static int Delayed_Millis = 50;
	
	
	public CodeEdit(Context cont){
		super(cont);
	}
	
	@Override
	protected void init()
	{
		super.init();
		mLast = new Stack<>();
		mNext = new Stack<>();
	}

	@Override
	protected void config(){
		super.config();
	}

	@Override
	public void setEditBuilder(EditBuilder builder){
		EditBuilder = builder;
	}
	@Override
	public EditBuilder getEditBuilder(){
		return EditBuilder;
	}
    @Override
	public void setLuagua(String Lua){
		EditBuilder.SwitchLuagua(this,Lua);
	}
	@Override
	public String getLuagua(){
		return luagua;
	}
	@Override
	public EditListenerInfo getInfo(){
		return Info;
	}
	@Override
	public void setInfo(EditListenerInfo Info){
		this.Info = Info;
	}
	@Override
	public void trimListener(){
		EditBuilder.trimListener(Info);
	}
	@Override
	public void clearListener(){
		Info.clear();
	}
	@Override
	public Words getWordLib(){
		return WordLib;
	}
	@Override
	public void setWordLib(Words Lib){
		WordLib = Lib;
	}
	@Override
	public void loadWords(){
		EditBuilder.loadWords(WordLib);
	}
	@Override
	public void clearWords(){
		WordLib.clear();
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
	final public void reDraw(final int start,final int end)
	{	
	    if(IsDraw()){
			//flag值不可被抵消
			return;
		}

	    final Spannable editor = getText();
		long last = System.currentTimeMillis();

		try{
			onFindNodes(start, end, editor); 
			//找nodes，即使getFinderList为null，因为我认为onFindNodes也可以不用listener
		}catch (Exception e){
			Log.e("FindNodes Error", e.toString());
		}	

		long now = System.currentTimeMillis();
		Log.w("After FindNodes","I'm "+hashCode()+", "+ "I take " + (now - last) + " ms ");
		//经过一次寻找后，让我们开始染色

		Runnable run =new Runnable()
		{
			public void run()
			{		
				long last = System.currentTimeMillis();
				++IsModify; //为保证isxxx安全，不要在子线程中使用它们
				IsDraw(true);

				try{
					onDrawNodes(start, end, editor); 
				}
				catch (Exception e){
					Log.e("DrawNodes Error", e.toString());
				}

				IsDraw(false);
				--IsModify; //为保证isxxx能成功配对，它们必须写在try和catch外，并紧贴try和catch
				long now = System.currentTimeMillis();	
				Log.w("After DrawNodes","I'm "+CodeEdit.this.hashCode()+", "+ "I take " + (now - last) + " ms, ");		
			}
		};
		postDelayed(run,Delayed_Millis);//将UI任务交给主线程
	}

	/* 寻找单词并存储到Words中，同时记录单词的范围和颜色 */
	public void onFindNodes(final int start, final int end, final CharSequence text)
	{
		//当final修饰一个基本数据类型时，表示该基本数据类型的值一旦在初始化后便不能发生变化；
		//如果final修饰一个引用类型时，则在对其初始化之后就不可以指向其它对象了，但该引用所指向的对象的内容是可以发生变化的
		final Words WordLib = getWordLib();
		EditListener li = getDrawer();
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li instanceof EditDrawerListener){
				    ((EditDrawerListener)li).onFindNodes(start, end, text, WordLib);
				    //nodes.addAll(tmp);
				}
				return false;
			}
		};
		li.dispatchCallBack(run);
		//这就是我要找的，能接纳任意参数，还能兼容任意EditListener的办法:
		//创建匿名内部类对象，让它包含参数，然后让EditListener把接口分发下去，每个Listener回调接口并传递自己，在接口实现中可以使用包含的参数
	}

	/* 用寻找到的范围染色 */
	public void onDrawNodes(final int start, final int end, final Spannable editor)
	{
		EditListener li = getDrawer();
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li instanceof EditDrawerListener){
				    ((EditDrawerListener)li).onDrawNodes(start, end, editor);
				}
				return false;
			}
		};
		li.dispatchCallBack(run);
	}

	/* 准备指定文本的颜料 */
	final public void prepare(final int start,final int end,final Spannable text)
	{
		long last = System.currentTimeMillis();
		
		try
		{	
			onFindNodes(start, end, text);
			onDrawNodes(start, end, text);
		}
		catch (Exception e){
			Log.e("prepare Error",e.toString());
		}

		long now = System.currentTimeMillis();
		Log.w("After PrePare","I'm "+hashCode()+", "+ "I take " + (now - last) + " ms, ");

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
	public final int Format(final int start, final int end)
	{
		if(IsFormat()){
			return 0;
		}

		//为了安全，禁止重写
		Editable editor = getText();
		int before = editor.length();
		long last = System.currentTimeMillis();

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

		long now = System.currentTimeMillis();
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
				    ((EditFormatorListener)li).onFormat(start, end, editor);
				}
				return false;
			}
		};
		li.dispatchCallBack(run);
	}

    /* 在指定位置插入后续字符 */
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
		EditListener lis = getFormator();
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li instanceof EditFormatorListener){
				    int selection = ((EditFormatorListener)li).onInsert(index,count,editor);
					setSelection(selection,selection);
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
	final public void openWindow()
	{
		if(mLi==null || IsComplete()){
			return;
		}
		long last = System.currentTimeMillis();
		
		try{
		    SearchInGroup(getText(),getSelectionEnd());
		}
		catch(Exception e){
			Log.e("SearchWord Error", e.toString());
		}

		//经过一次查找，Icons里装满了单词
		long now = System.currentTimeMillis();
		Log.w("After SearchWords","I'm "+hashCode()+", "+ "I take " + (now - last) + " ms, ");

		Runnable run = new Runnable()
		{
			public void run()
			{
				mLi.onFinishSearchWord(CodeEdit.this,Icons);
			}
		};
		postDelayed(run,Delayed_Millis);//将UI任务交给主线程
	}

	/* 在不同集合中找单词 */
	public void SearchInGroup(final CharSequence text,final int index)
	{
		EditListener lis = getCompletor();
		final Words WordLib = getWordLib();
		
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li instanceof EditCompletorListener){
			        ((EditCompletorListener)li).onSearchWord(text,index,WordLib);
			    }
				return false;
			}
		};
	    lis.dispatchCallBack(run);
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
		//遍历所有listener，找到这个单词的放入者，由它自己处理插入
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li instanceof EditCompletorListener && li.hashCode() == id)
				{
				    int selection = ((EditCompletorListener)li).onInsertWord(editor,index,word);
				    setSelection(selection,selection);
				    return true;
				}
				return false;
			}
		};

		if(!lis.dispatchCallBack(run))
		{
		    //没有找到listener，就执行默认操作
		   // editor.replace(tmp.start, tmp2.end, word);
		  //  setSelection(tmp.start + word.length());
		    //把光标移动到最后
		}
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
	
	@Override
	public void beforeTextChanged(CharSequence text, int start, int count, int after)
	{
		super.beforeTextChanged(text, start, count, after);
		
		if(IsUR()){
			//如果它是由于Uedo本身或无需处理的（例如染色）造成的修改，则不能装入	
			//另一个情况是，Uedo需要保存修改时，额外插入的文本
			return;
		}
		
		token token = null;
		if(mLast.size()>0){
			token = mLast.peek();
		}
		
		//文本修改前，判断一下本次修改位置是否与上次token相连
		if(token==null || !replaceToken(text,start,count,after,token))
		{
		    //如果不相连，制作一个token，并保存到mLast
		    token = makeToken(text,start,count,after);
		    if(token!=null){
			    mLast.push(token);
		    }
		}
	}
	
	/* 得到Token并应用到文本，并把转化的Token存入stack */
    final public void Uedo()
	{
		if(mLast.size()>0 && !IsUR())
		{
			++IsModify;
			IsUR(true);
			token token = mLast.pop();
			doAndCastToken(token);
			mNext.push(token);
			IsUR(false);
			--IsModify;
		}
	}

	/* 得到Token并应用到文本，并把转化的Token存入stack */
	final public void Redo()
	{
		if(mNext.size()>0 && !IsUR())
		{
			++IsModify;
			IsUR(true);
		    token token = mNext.pop();
			doAndCastToken(token);
			mLast.push(token);
			IsUR(false);
			--IsModify;
		}
	}
	
	/* 应用Token到文本，并将其反向转化 */
    final protected void doAndCastToken(token token)
	{
		CharSequence text;
		Editable editor = getText();
		
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
	
	/* 根据文本变化来制作一个Token */
	final protected token makeToken(CharSequence text, int start, int count, int after)
	{
		token token = null;
		if(count!=0 && after!=0)
		{
			//如果删除了字符并且插入字符，本次删除了count个字符后达到start，并且即将从start开始插入after个字符
			//那么上次的字符串就是：替换start~start+after之间的字符串为start~start+count之间的字符串
			token = new token(start, start+after, text.subSequence(start, start+count));	
		}
		else if (count != 0)
		{
			//如果删除了字符，本次删除了count个字符后达到start，那么上次的字符串就是：
			//从现在start开始，插入start～start+count之间的字符串
			token = new token(start, start, text.subSequence(start, start+count));
		}
		else if (after != 0)
		{
			//如果插入了字符，本次即将从start开始插入after个字符，那么上次的字符串就是：
			//删除现在start～start+after之间的字符串
			token = new token(start, start+after, "");		
		}	
		return token;
	}
	
	/* 检查token是否可以扩展，如果可以就扩展token的范围 */
	final protected boolean replaceToken(CharSequence text, int start, int count, int after, token token)
	{
		if(count != 0)
		{
			//如果token的类型是删除型token
			if(token.start==token.end)
			{
				//继续删除了字符，我们应该检查token的前面
				if(token.start==start+count)
				{
					//我们继续向前截取删除的文本，并添加到已有的文本前
					CharSequence src = text.subSequence(start,start+count);
					SpannableStringBuilder b = (SpannableStringBuilder) token.src;
					b.insert(0,src);
					
					//并且移动到新的位置
					token.start=start;
					token.end=start;
					return true;
				}
			}
		}
		else if(after != 0)
		{
			//如果token的类型是插入型token
			if(token.end!=token.start)
			{
				//继续插入了字符，我们应该检查token的后面
				if(token.end==start)
				{
					//我们继续向后扩展token的范围
					token.end+=after;
					return true;
				}
			}
		}
		return false;
	}
	
/*
---------------------------------------------------------------

 核心功能的调度函数 onTextChange

 -> openWindow

 -> Insert

 -> reDraw

 Insert和reDraw并不冲突，即使是延迟染色也没事，因为只有输入时才染色，Insert默认向后插入，所以没事

 就算是染色下标超出范围也没事，我们有try，只要不超太多影响结果就可以(怕上次文本没染完，下次就又修改，所以不使用Format)

---------------------------------------------------------------

*/

    @Override
	public void onTextChanged(CharSequence text, int start, int lenghtBefore, int lengthAfter)
	{
		
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
	 
	public static final int DrawMask = 1;

	public static final int FormatMask = 2;

	public static final int CompleteMask = 4;

	public static final int CanvasMask = 8;

	public static final int RunMask = 16;

	public static final int URMask = 64;

	public static final int ModifyMask = 128;

	
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
	
	public void setFlags(int flags){
		mPrivateFlags = flags;
	}
	public int getFlags(){
		return mPrivateFlags;
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
    final public static class CodeEditListenerInfo implements EditListenerInfo
	{

		private Map<Integer,EditListener> mlistenerS;

		public CodeEditListenerInfo()
		{
			mlistenerS = CollectionSpiltor.EmptyMap();
			//我们希望它们一开始就是EditListenerList
			mlistenerS.put(CompletorIndex,new myEditListenerList());
			mlistenerS.put(CanvaserIndex,new myEditListenerList());
		}

		@Override
	    public boolean addAListener(EditListener li) 
		{
			if(li==null)
				return false;

			if(li instanceof EditDrawerListener){
				return addListenerTo(li,DrawerIndex);
			}
			else if(li instanceof EditFormatorListener){
				return addListenerTo(li,FormatorIndex);
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
						int key = CollectionSpiltor.vualeToKey(l,mlistenerS);
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
		public boolean contians(EditListener li){
			return mlistenerS.containsValue(li);
		}

	}


/*
 ------------------------------------------------------------------------------------

 每一个Edit都有自己的Words，内部可以自由存储单词，但必须实现接口，通过接口，Words可以设置和获取单词

 每一个Edit都应该尽量保存所有的单词，以便在切换语言时仍可使用

 ------------------------------------------------------------------------------------
*/
	final public static class CodeWords implements Words
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
			mchars = CollectionSpiltor.EmptyMap();
			mdates = CollectionSpiltor.EmptyMap();
			mmaps = CollectionSpiltor.EmptyMap();

			mchars.put(chars_fuhao,CollectionSpiltor.EmptySet());
			mchars.put(chars_spilt,CollectionSpiltor.EmptySet());
			for(int i=words_key;i<=words_attr;++i){
				mdates.put(i,new prefixCharSequenceMap());
			}
			mmaps.put(maps_zhu,CollectionSpiltor.EmptyMap());
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
			mchars.put(index,CollectionSpiltor.copySet(words));
		}
	    @Override
		public void setACollectionWords(int index, Collection<CharSequence> words){
			mdates.put(index,CollectionSpiltor.copySet(words));
		}
		@Override
		public void setAMapWords(int index, Map<CharSequence, CharSequence> words){
			mmaps.put(index,CollectionSpiltor.copyMap(words));
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
		public boolean contians(int index){
			return mchars.containsKey(index) || mdates.containsKey(index) || mmaps.containsKey(index);
		}

	}
	
	
}
