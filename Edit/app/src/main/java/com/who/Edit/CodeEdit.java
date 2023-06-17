package com.who.Edit;
import android.view.*;
import com.who.Edit.Base.*;
import android.content.*;
import android.graphics.*;
import java.util.*;
import com.who.Edit.Base.Share.Share1.*;
import android.text.*;

public class CodeEdit extends Edit
{
	
	private Stack<token> mLast, mNext;
	private int mLastIndex;
	
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

	@Override
	public void beforeTextChanged(CharSequence text, int start, int count, int after)
	{
		super.beforeTextChanged(text, start, count, after);
		//文本修改前，判断一下本次start是否与上次相连
		if(start==mLastIndex){
			
		}
		
		//计算文本修改后，光标最后位置
		if (count != 0){
			mLastIndex = start;
		}
		else if (after != 0){
			mLastIndex = start+after;
		}	
		
	}

	@Override
	public void onTextChanged(CharSequence text, int start, int lenghtBefore, int lengthAfter)
	{
		super.onTextChanged(text, start, lenghtBefore, lengthAfter);
	}
	
	
	/* 得到Token并应用到文本，并把转化的Token存入stack */
    final public void Uedo()
	{
		if(mLast.size()>0)
		{
			token token = mLast.pop();
			if (token != null)
			{
				DoAndCastToken(token);
				mNext.push(token);
			}
		}
	}

	/* 得到Token并应用到文本，并把转化的Token存入stack */
	final public void Redo()
	{
		if(mNext.size()>0)
		{
		    token token = mNext.pop();
			if (token != null)
			{
				DoAndCastToken(token);
				mLast.push(token);
			}
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
	
}
