package com.editor.text2;

import android.content.*;
import android.text.*;
import com.editor.text.*;
import com.editor.text2.base.share1.*;
import com.editor.text2.base.share4.*;
import com.editor.text2.builder.*;
import com.editor.text2.builder.listenerInfo.*;
import com.editor.text2.builder.listenerInfo.listener.baselistener.*;
import com.editor.text2.builder.words.*;
import java.util.*;
import java.util.concurrent.*;
import android.widget.*;
import android.view.*;
import android.graphics.*;
import static com.editor.text2.builder.listenerInfo.EditListenerInfo.*;
import static com.editor.text2.builder.listenerInfo.listener.baselistener.EditListener.RunLi;
import static com.editor.text2.builder.listenerInfo.listener.myEditCompletorListener.onOpenWindowLisrener;
import com.editor.text.base.*;
import com.editor.text2.base.share2.*;
import android.widget.AdapterView.*;

public class CodeEdit extends Edit implements EditBuilderUser,OnItemClickListener
{

	@Override
	public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
	{
		wordIcon icon = (wordIcon) p1.getItemAtPosition(p3);
		CharSequence name = icon.getName();
		insertWord(name,getSelectionEnd(),(int)p4);
	}
	

	private Words mWordLib;
	private EditListenerInfo mListenerInfo;
	private EditBuilder mEditBuilder;
	
	private ThreadPoolExecutor mPool;
	private AdapterView mWindow;
	private onOpenWindowLisrener mListener;
	
	private Stack<token> mLast, mNext;
	private int mPrivateFlags;
	public static int mPublicFlags;
	
	private HandlerQueue.HandlerLock mLocker;
	
	
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
		mListenerInfo = new CodeEditListenerInfo();
		mEditBuilder = new CodeEditBuilder();
	}

	@Override
	protected void config()
	{
		super.config();
		mEditBuilder.SwitchLuagua(this,"java");
	}
	
	public void setPool(ThreadPoolExecutor pool){
		mPool = pool;
	}
	
	@Override
	public Words getWordLib()
	{
		return mWordLib;
	}

	@Override
	public void loadWords()
	{
		// TODO: Implement this method
	}

	@Override
	public void clearWords()
	{
		// TODO: Implement this method
	}

	@Override
	public EditListenerInfo getInfo()
	{
		return mListenerInfo;
	}

	@Override
	public void trimListener()
	{
		// TODO: Implement this method
	}

	@Override
	public void clearListener()
	{
		// TODO: Implement this method
	}

	@Override
	public EditBuilder getEditBuilder()
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public void setLuagua(String Lua)
	{
		// TODO: Implement this method
	}

	@Override
	public String getLuagua()
	{
		// TODO: Implement this method
		return null;
	}
	
	
	public EditListener getDrawer(){
		return mListenerInfo.findAListener(DrawerIndex);
	}
	public EditListener getFormator(){
		return mListenerInfo.findAListener(FormatorIndex);
	}
	public EditListener getCompletor(){
		return mListenerInfo.findAListener(CompletorIndex);
	}
	public EditListener getCanvaser(){
		return mListenerInfo.findAListener(CanvaserIndex);
	}
	public EditListener getRunnar(){
		return mListenerInfo.findAListener(RunnarIndex);
	}
	
	
	public void reDrawText(final int start, final int end)
	{
		final wordIndex[] nodes = onFindNodes(start,end,getText(),mWordLib);
		Runnable runDraw = new Runnable()
		{
			@Override
			public void run(){
				onDrawNodes(start,end,getText(),nodes);
				invalidate();
			}
		};
		post(runDraw);
	}
	
	public Runnable ReDrawText(final int start, final int end)
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
	public void reDrawTextS(int start, int end)
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
					final wordIndex[] nodes = onFindNodes(st,en,editor,mWordLib);
					onDrawNodes(st,en,editor,nodes);
				}
			};	
			totals[i++] = run;
		}
		mLocker = HandlerQueue.doTotals(totals,getHandler());
	}
	
	private wordIndex[] onFindNodes(int start, int end, CharSequence text, Words lib)
	{
		EditDrawerListener li = (EditDrawerListener) getDrawer();
		wordIndex[] nodes = li.onFindNodes(start,end,getText(),mWordLib);
		return nodes;
	}
	
	private void onDrawNodes(int start, int end, Spannable text, wordIndex[] nodes)
	{
		EditDrawerListener li = (EditDrawerListener) getDrawer();
		li.onDrawNodes(start,end,getText(),nodes);
	}

	@Override
	public void onTextChanged(CharSequence text, int start, int lenghtBefore, int lengthAfter)
	{
		if(mLocker!=null){
			mLocker.lockHandler();
			mLocker = null;
		}
		Runnable run1 = OpenWindow();
		mPool.execute(run1);
		Runnable run2 = ReDrawText(BlockLayout.tryLine_Start(text,start), BlockLayout.tryLine_End(text,start+lengthAfter));
		mPool.execute(run2);
		super.onTextChanged(text, start, lenghtBefore, lengthAfter);
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
			IsUR(true);
			token token = mLast.pop();
			doAndCastToken(token);
			mNext.push(token);
			IsUR(false);
		}
	}

	/* 得到Token并应用到文本，并把转化的Token存入stack */
	final public void Redo()
	{
		if(mNext.size()>0 && !IsUR())
		{
			IsUR(true);
		    token token = mNext.pop();
			doAndCastToken(token);
			mLast.push(token);
			IsUR(false);
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
		return (mPrivateFlags&ModifyMask) == ModifyMask || ((EditableBlockList)getText()).getTextWatcherDepth()!=0 || (mPublicFlags&ModifyMask) == ModifyMask;
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
	
	public void openWindow()
	{ 
		if(mWindow==null){
			return;
		}
		final WordAdapter<wordIcon> adapter = WordAdapter.getDefultAdapter();
		SearchInGroup(getText(),getSelectionEnd(),adapter);
		Runnable run = new Runnable()
		{
			public void run(){
				mWindow.setAdapter(adapter);
				putWindow();
			}
		};
		post(run);
	}
	
	public Runnable OpenWindow()
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

	/* 在不同集合中找单词 */
	public void SearchInGroup(final CharSequence text,final int index,final WordAdapter<wordIcon> Adapter)
	{
		EditListener lis = getCompletor();
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li instanceof EditCompletorListener){
			        wordIcon[] Icons = ((EditCompletorListener)li).onSearchWord(text,index,mWordLib);
			        Adapter.addAll(li.hashCode(),Icons);
			    }
				return false;
			}
		};
	    lis.dispatchCallBack(run);
	}

	/* 插入单词，支持Span文本 */
	final public int insertWord(CharSequence word, int index, int id)
	{
		onInsertword(getText(),word,index,id);
		return 0;
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
				    setSelection(selection);
				    return true;
				}
				return false;
			}
		};
		if(!lis.dispatchCallBack(run)){
		    //没有找到listener，就直接插入
		    editor.replace(index,index,word,0,word.length());
		}
	}
	
	public void setWindow(AdapterView Window, onOpenWindowLisrener li){
		mWindow = Window;
		mListener = li;
	}
	
	private void putWindow()
	{
		if(mWindow==null){
			return;
		}
		mWindow.setVisibility(VISIBLE);
		final pos p = getSelectionStartPos();
		int x = (int) p.x;
		int y = (int) (p.y+getLineHeight());

		final int width = getWidth();
		int wantWidth = (int)(width*0.8);
		if(p.x+wantWidth>getScrollX()+width){
			x = getScrollX()+width-wantWidth;
		}

		final int height = getHeight();
		int wantHeight = measureWindowHeight(mWindow,height/2);
		if(p.y+wantHeight>getScrollY()+height){
			y = (int)p.y-wantHeight;
		}
		mWindow.layout(x,y,x+wantWidth,y+wantHeight);
		mListener.callOnOpenWindow(mWindow,x,y);
	}
	
	public void cloaeWindow(){
		mWindow.setVisibility(GONE);
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

	@Override
	public boolean performClick()
	{
		cloaeWindow();
		return super.performClick();
	}

}
