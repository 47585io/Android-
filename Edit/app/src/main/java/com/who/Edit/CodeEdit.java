package com.who.Edit;

import android.view.*;
import com.who.Edit.Base.*;
import android.content.*;
import android.graphics.*;
import java.util.*;
import com.who.Edit.Base.Share.Share1.*;
import android.text.*;


/* CodeEdit扩展了Edit的功能 */
public class CodeEdit extends Edit
{
	
	private Stack<token> mLast, mNext;
	
	private int IsModify;
	private int mPrivateFlags;
	public static int mPublicFlags;
	
	
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
	protected void config()
	{
		super.config();
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
			DoAndCastToken(token);
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
			DoAndCastToken(token);
			mLast.push(token);
			IsUR(false);
			--IsModify;
		}
	}
	
	/* 应用Token到文本，并将其反向转化 */
    final protected void DoAndCastToken(token token)
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

	public static final int SelectionMask = 32;

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
	public void IsSelection(boolean is){
		mPrivateFlags = is ? mPrivateFlags|SelectionMask : mPrivateFlags&~SelectionMask;
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
	
	public void setFlags(int flags){
		mPrivateFlags = flags;
	}
	public int getFlags(){
		return mPrivateFlags;
	}
	
}
