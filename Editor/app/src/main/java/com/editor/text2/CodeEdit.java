package com.editor.text2;

import android.content.*;
import android.graphics.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.editor.*;
import com.editor.text.*;
import com.editor.text2.base.share1.*;
import com.editor.text2.base.share2.*;
import com.editor.text2.base.share4.*;
import com.editor.text2.builder.*;
import com.editor.text2.builder.listener.baselistener.*;
import com.editor.text2.builder.words.*;
import java.util.*;
import java.util.concurrent.*;
import static com.editor.text2.builder.listener.myEditCompletorListener.*;
import com.editor.text.base.*;
import com.editor.text2.base.share3.*;
import android.text.style.*;
import com.editor.text.span.*;
import android.os.*;
import com.editor.text2.builder.listener.*;


public class CodeEdit extends Edit implements OnItemClickListener,OnItemLongClickListener
{

	private int ModifyCount;
	private int mPrivateFlags;
	public static int mPublicFlags;
	
	private String mLuagua;
	private Words mWordLib;
	private EditListenerInfo mListenerInfo;
	private EditBuilder mEditBuilder;
	
	private Stack<token> mLast, mNext;
	private ThreadPoolExecutor mPool;
	private HandlerQueue.HandlerLock mLocker;
	
	private static ListView mContent;
	private static ListView mContent2;
	private onOpenWindowLisrener mListener;
	
	
	public CodeEdit(Context cont){
		super(cont);
	}

	@Override
	protected void init()
	{
		super.init();
		mLast = new Stack<>();
		mNext = new Stack<>();
		mWordLib = new CodeWords();
		mListenerInfo = new EditListenerInfo();
		mEditBuilder = new CodeEditBuilder();
	}

	@Override
	protected void config(){
		super.config();
		setLuagua("java");
	}
	
	public void setPool(ThreadPoolExecutor pool){
		mPool = pool;
	}
	public Words getWordLib(){
		return mWordLib;
	}
	public EditListenerInfo getInfo(){
		return mListenerInfo;
	}
	public EditBuilder getEditBuilder(){
		return mEditBuilder;
	}
	public void setLuagua(String Lua){
		mLuagua = Lua;
		mWordLib.clear();
		mListenerInfo.clear();
		mEditBuilder.loadWords(mWordLib,Lua);
		mEditBuilder.trimListener(mListenerInfo,Lua);
	}
	public String getLuagua(){
		return mLuagua;
	}
	
	public EditDrawerListener getDrawer(){
		return mListenerInfo.mDrawerListener;
	}
	public EditFormatorListener getFormator(){
		return mListenerInfo.mFormatorListener;
	}
	public EditCompletorListener[] getCompletors(){
		return mListenerInfo.mCompletorListeners;
	}
	public EditCanvaserListener[] getCanvasers(){
		return mListenerInfo.mCanvaserListeners;
	}
	public EditRunnarListener getRunnar(){
		return mListenerInfo.mRunnarListener;
	}
	
	
/*
 ------------------------------------------------------------------------------------

 染色者
 
 调度监听器对范围内的文本染色，

 ------------------------------------------------------------------------------------
*/

	final public void reDrawText(final int start, final int end)
	{
		final wordIndex[] nodes = onFindNodes(start,end,getText());
		Runnable runDraw = new Runnable()
		{
			@Override
			public void run()
			{
				onDrawNodes(start,end,getText(),nodes);
				invalidate();
			}
		};
		post(runDraw);
	}
	
	final public Runnable ReDrawText(final int start, final int end)
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				reDrawText(start,end);
			}
		};
	}
	
	//染色期间不要让用户输入，除非染色被截止或完成
	final public void reDrawTextContinuous(int start, int end)
	{
		if(mLocker!=null){
			return;
		}
		final int once = 4000;
		final int len = end-start;
		final int count = len%once==0 ? len/once:len/once+1;
		final Editable editor = getText();
		final Runnable[] totals = new Runnable[count];
		for(int i = 0;start<end;start+=once)
		{
			final int st = start;
			final int en = start+once>end ? end:start+once;
			Runnable run = new Runnable()
			{
				@Override
				public void run()
				{
					final wordIndex[] nodes = onFindNodes(st,en,editor);
					onDrawNodes(st,en,editor,nodes);
				}
			};	
			totals[i++] = run;
		}
		mLocker = HandlerQueue.doTotals(totals,getHandler(),500);
	}
	
	protected wordIndex[] onFindNodes(int start, int end, CharSequence text)
	{
		EditDrawerListener li = getDrawer();
		wordIndex[] nodes = EmptyArray.emptyArray(wordIndex.class);
		if(li!=null){
			nodes = li.onFindNodes(start,end,getText(),mWordLib);
		}
		return nodes;
	}
	
	protected void onDrawNodes(int start, int end, Spannable text, wordIndex[] nodes)
	{
		EditDrawerListener li = getDrawer();
		if(li!=null){
			li.onDrawNodes(start,end,getText(),nodes);
		}
	}
	
/*
 ------------------------------------------------------------------------------------
  
 整理者
 
 ------------------------------------------------------------------------------------
*/

    /* 对齐范围内的文本 */
	public final void Format(final int start, final int end)
	{
		//大量次数的修改，我们不想一直刷新
		beginBatchEdit();
		++ModifyCount;
		IsFormat(true);
		onFormat(start, end, getText());
		IsFormat(false);
		-- ModifyCount;
		endBatchEdit();
	}

	protected void onFormat(final int start, final int end, final Editable editor)
	{	
		EditFormatorListener li = getFormator();
		if(li!=null){
			li.onFormat(start, end, editor);
		}
	}

    /* 在指定位置插入后续字符 */
	public final void Insert(final int index,final int count)
	{	
		++ModifyCount;
		IsFormat(true);
		onInsert(index, count, getText());
		IsFormat(false);
		-- ModifyCount;
	}	
	
	public void onInsert(final int index,final int count, Editable editor)
	{
		EditFormatorListener li = getFormator();
		if(li!=null){
		    li.onInsert(index,count,editor);
		}
	}
	
/*
 ----------------------------------------------------------------------------

 提示器
	 
 根据用户输入的单词，搜索相似单词，并打开窗口进行提示
 
 用户可以选择提示单词进行插入或查看doc
	 
 ------------------------------------------------------------------------------------
*/

	public final void openWindow()
	{ 
		if(mListener==null){
			return;
		}
		final WordAdapter<wordIcon> adapter = WordAdapter.getDefultAdapter();
		onSearchWords(getText(),getSelectionEnd(),adapter);
		Runnable run = new Runnable()
		{
			@Override
			public void run(){
				putWindow(adapter);
			}
		};
		post(run);
	}

	public Runnable OpenWindow()
	{
		return new Runnable()
		{
			@Override
			public void run(){
				openWindow();
			}
		};
	}

	/* 在不同集合中找单词 */
	protected void onSearchWords(final CharSequence text,final int index,final WordAdapter<wordIcon> Adapter)
	{
		EditCompletorListener[] lis = getCompletors();
		if(lis!=null)
		{
			for(int i=0;i<lis.length;++i)
			{
				wordIcon[] Icons = lis[i].onSearchWord(text,index,mWordLib);
				if(Icons.length>0){
					Adapter.addAll(lis[i].hashCode(),Icons);
				}
			}
		}
	}
	
	/* 寻找单词的doc */
	protected void onSearchDocs(final CharSequence word, final int id, final WordAdapter<wordIcon> Adapter)
	{
		EditCompletorListener[] lis = getCompletors();
		if(lis!=null)
		{
			for(int i=0;i<lis.length;++i)
			{
				if(lis[i].hashCode()==id){
					wordIcon[] Icons = lis[i].onSearchDoc(word,mWordLib);
					Adapter.addAll(lis[i].hashCode(),Icons);
					return;
				}
			}
		}
	}
	
	public void insertWord(final CharSequence word, final int index, final int id)
	{
		++ModifyCount;
		onInsertWord(getText(),word,index,id);
		--ModifyCount;
	}

	/* 插入单词，支持Span文本 */
	protected void onInsertWord(final Editable editor,final CharSequence word, final int index, final int id)
	{
		//遍历所有listener，找到这个单词的放入者，由它自己处理插入
		EditCompletorListener[] lis = getCompletors();
		if(lis!=null)
		{
			for(int i=0;i<lis.length;++i)
			{
				if(lis[i].hashCode()==id){
					lis[i].onInsertWord(editor,index,word);
					return;
				}
			}
		}
	}

	public void setWindowListener(onOpenWindowLisrener li){
		mListener = li;
	}
	private void putWindow(ListAdapter adapter)
	{
		if(mListener==null){
			return;
		}
		if(mContent==null){
			mContent = creatContent();
		}
		if(adapter.getCount()!=0)
		{
			mContent.setAdapter(adapter);
			mContent.setOnItemClickListener(this);
			mContent.setOnItemLongClickListener(this);
			final pos p = null;
			int x = (int) p.x;
			int y = (int) (p.y+getLineHeight());

			final int width = getWidth();
			final int scrollX = getScrollX();
			int wantWidth = (int)(width*0.8);
			if(p.x+wantWidth > scrollX+width){
				x = scrollX+width-wantWidth;
			}

			final int height = getHeight();
			final int scrollY = getScrollY();
			int wantHeight = measureWindowHeight(mContent,height/2);
			if(p.y+wantHeight > scrollY+height){
				y = (int)p.y-wantHeight;
			}
			mListener.callOnOpenWindow(x-scrollX,y-scrollY,wantWidth,wantHeight);
			mListener.callOnRefreshWindow(mContent,0,0,wantWidth,wantHeight);
		}
		else{
			mListener.callOnCloseWindow();
		}
	}
	private void closeWindow()
	{
		if(mListener!=null){
			mListener.callOnCloseWindow();
		}
	}
	private static int measureWindowHeight(AdapterView Window, int maxHeight)
	{
		Adapter adapter = Window.getAdapter();
		if(adapter==null){
			return 0;
		}
		int height=0;
		int count = adapter.getCount();
		for (int i=0;i<count;++i)
		{
			View view = adapter.getView(i, null, Window);
			view.measure(0, 0);
			height += view.getMeasuredHeight();
			if(height>=maxHeight){
				return maxHeight;
			}
		}
		return height;
	}
	private ListView creatContent()
	{
		ListView v = new ListView(getContext());
		v.setDivider(null);
		v.setFocusable(false);
		return v;
	}
	
/*
 ------------------------------------------------------------------------------------
  绘画者
 -----------------------------------------------------------------------------------
*/

	@Override
	protected void dispatchDraw(final Canvas canvas)
	{
		EditCanvaserListener[] lis = getCanvasers();
		if(lis!=null){
			TextPaint paint = getPaint();
			for(int i=0;i<lis.length;++i){
				lis[i].onDraw(this,canvas,paint);
			}
		}
	}
	
/*
 -----------------------------------------------------------------------------------
 运行器
 -----------------------------------------------------------------------------------
*/

	final public String MakeCommand(final String state)
	{
		EditRunnarListener li = getRunnar();
		return li==null ? "" : li.onMakeCommand(this,state);
	}

	protected int RunCommand(final String command)
	{
		EditRunnarListener li = getRunnar();
		return li==null ? 0 : li.onRunCommand(this,command);
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
			++ModifyCount;
			IsUR(true);
			token token = mLast.pop();
			doAndCastToken(token);
			mNext.push(token);
			IsUR(false);
			--ModifyCount;
		}
	}

	/* 得到Token并应用到文本，并把转化的Token存入stack */
	final public void Redo()
	{
		if(mNext.size()>0 && !IsUR())
		{
			++ModifyCount;
			IsUR(true);
		    token token = mNext.pop();
			doAndCastToken(token);
			mLast.push(token);
			IsUR(false);
			--ModifyCount;
		}
	}

	/* 应用Token到文本，并将其反向转化 */
    final protected void doAndCastToken(token token)
	{
		final Editable editor = getText();
		if (token.src.equals(""))
		{	
			//如果token会将范围内字符串删除，则我要将其保存，待之后插入
			CharSequence text = editor.subSequence(token.start, token.end);
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
			CharSequence text = editor.subSequence(token.start, token.end);
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
					Editable editor = (Editable) token.src;
					editor.insert(0,src);

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
 ------------------------------------------------------------------------------------

 核心功能的调度函数
 
 根据情况进行打开窗口，染色，插入文本
 
 ------------------------------------------------------------------------------------
*/

	@Override
	public void onTextChanged(CharSequence text, int start, int lenghtBefore, int lengthAfter)
	{
		if(mLocker!=null){
			mLocker.lockHandler();
			mLocker = null;
		}
		super.onTextChanged(text, start, lenghtBefore, lengthAfter);
		if(IsModify()){
			return;
		}
		//bug: 子线程的时间不同步，等任务开始时，下标可能超出范围
		Runnable run1 = OpenWindow();
		//mPool.execute(run1);
		BaseLayout layout = getLayout();
		Runnable run2 = ReDrawText(layout.getOffsetToLeftOf(start), layout.getOffsetToRightOf(start+lengthAfter));
		//mPool.execute(run2);
		if(lengthAfter>0){
			Insert(start,lengthAfter);
		}
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
		return (mPrivateFlags&ModifyMask) == ModifyMask || ModifyCount!=0 || getTextWatcherDepth()>1 || (mPublicFlags&ModifyMask) == ModifyMask;
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

 其它的事件
 
 ------------------------------------------------------------------------------------
*/
	
	@Override
	public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
	{/*
		wordIcon icon = (wordIcon) p1.getItemAtPosition(p3);
		CharSequence name = icon.getName();
		WordAdapter<wordIcon> adapter = WordAdapter.getDefultAdapter();
		adapter.addAll(0,new wordIconX(R.drawable.icon_default,"没有任何doc..."));
		onSearchDocs(name,(int)p4,adapter);
		mListener.callOnRefreshWindow(mContent);*/
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
	{
		wordIcon icon = (wordIcon) p1.getItemAtPosition(p3);
		CharSequence name = icon.getName();
		insertWord(name,getSelectionEnd(),(int)p4);
		mListener.callOnCloseWindow();
	}

	@Override
	public boolean performClick()
	{
		closeWindow();
		return super.performClick();
	}

	@Override
	public void onSelectionChanged(final int start, int end, int oldStart, int oldEnd, final CharSequence editor)
	{
		super.onSelectionChanged(start, end, oldStart, oldEnd, editor);
		
	}
	
	private static final int SelectedColor = 0x75515a6b;
	private static final Object bindowStartSpan = new myBackgroundColorSpan(SelectedColor);
	private static final Object bindowEndSpan = new myBackgroundColorSpan(SelectedColor);
	private int lastBindowStart = -1, lastBindowEnd = -1;
	private Runnable mLastSelectionRunnable;
	
}
