package com.mycompany.who.Edit.Base;

import android.content.*;
import android.graphics.*;
import android.text.*;
import android.text.method.*;
import android.util.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import com.mycompany.who.Edit.Base.Share.Share3.*;
import com.mycompany.who.Edit.Base.EditMoudle.*;
import java.util.*;


/*
 不要求任何编辑器必须实现模块全部功能，这里先实现Creat

 后续编辑器继承后，改变这个类可以方便地改变所有继承的类

 一个bug:计算字符大小很麻烦，这里只管了纯英文字符

 */
public class Edit extends EditText implements Creat<Edit>,Configer<Edit>,Sizer
{

	private static KeyListener listener;
	public static int Selected_Color=0x75515a6b;
	public static int Background_Color=0;
	public static int Text_Color=0xffabb2bf;
	public static int CursorRect_Color=0x25616263;
	public static Typeface type=Typeface.MONOSPACE;
	private float TextSize;
	private static float FillPadding;

	public Edit(Context cont)
	{
		super(cont);
		Creat();
	}
	public Edit(Context cont, AttributeSet attrs)
	{
		super(cont, attrs);
		Creat();
	}
	public Edit(Context cont, Edit Edit)
	{
		super(cont);
		CopyFrom(Edit);
	}

	@Override
	public void ConfigSelf(Edit target)
	{
		setTextColor(Text_Color);
		setBackgroundColor(Background_Color);
		setTypeface(type);
		setHighlightColor(Selected_Color);
		setTextSize(TextSize);
		/*
		 当设置LineSpacing后，除最后一行以外的其它行都添加这个LineSpacing，所以最后一行会缺失LineSpacing，这里使用padding来补充最后一行高度
		 但实际上，layout还会包含第一行或最后一行的填充，这个填充值无法预测
		 但如下的方案可以完美对齐:
		 */
		float add = getLineHeight()*0.2f;
		setLineSpacing(0.2f, 1.2f);
		setPadding(0, 0, 0, (int)(add-FillPadding));
	}

	@Override
	public void Creat()
	{
		TextSize = 13.5f;
		if (listener == null){
		    listener = getKeyListener();
		}
		if(FillPadding==0){
			FillPadding = getLineHeight()*0.1f;
		}
		ConfigSelf(this);
	}

	@Override
	public Edit CreatOne()
	{
		return new Edit(getContext());
	}

	@Override
	public void CopyFrom(Edit target)
	{
		TextSize = target.TextSize;
		ConfigSelf(this);
	}

	@Override
	public void CopyTo(Edit target)
	{
		target.TextSize = TextSize;
		target.ConfigSelf(target);
	}

	@Override
	public void setTextSize(float size)
	{
		//记录文本原始大小
		TextSize = size;
		super.setTextSize(size);
	}

	@Override
	public float getTextSize()
	{
		//getTextSize获取的是指定机型显示器加权后的值，有可能不对，这里除20f/12f后就是等宽英文字符大小
		return super.getTextSize() / (20f / 12f);
	}
	public float getUnicodeTextSize()
	{
		//获取中文等宽字符大小，我的天，原来super.getTextSize()是等宽中文字符大小，因为我用的是中国手机
		return super.getTextSize();
	}

	@Override
	public int getLineCount()
	{
		return getLineCount(getText().toString());
	}
	public static int getLineCount(String src)
	{
		return String_Splitor.Count('\n', src,0,src.length()) + 1;
	}

	@Override
	public int maxHeight()
	{
		return (int)measureTextHeight(getText().toString());
	}
	@Override
	public int maxWidth()
	{
		return (int)measureTextWidth(getText().toString());
	}
	@Override
    public size WAndH()
	{
		return new size(maxWidth(),maxHeight());
	}
	
	/* 测量文本宽度 */
	final public float measureTextWidth(String text)
	{
		CharSequence str;
		float width = 0, w = 0;
		int lastIndex =0, index = 0;
		while(true)
		{
		    index = text.indexOf('\n',lastIndex);
			if(index==-1)
			{
				//最后一行宽度
				str = text.subSequence(lastIndex,text.length());
				w = measureTextLen(str);
				if(w>width){
					width = w;
				}
				break;
			}
			str = text.subSequence(lastIndex,index);
			w = measureTextLen(str);
			if(w>width){
				width = w;
			}
			++index;
			lastIndex = index;
		}
		return width;
	}
	/* 测量文本高度 */
	final public float measureTextHeight(String text)
	{
		return getLineCount(text)*getLineHeight();
	}
	/* 测量文本长度，包含英文和中文，使用TextSize加权后的值 */
	final public float measureTextLen(CharSequence text)
	{
		int Unicode = String_Splitor.Unicode.checkUnicodeCount(text);
		int Ascii = text.length() - Unicode;
		return getTextSize() * Ascii + getUnicodeTextSize() * Unicode;
	}

	/* 截取startLine行之后的内容，返回内容的范围 */
	final public size subLines(int startLine)
	{
		return subLines(startLine, getText().toString());
	}
	/* 截取startLine行和endLine行之间的内容 */
	final public size subLines(int startLine, int endLine)
	{
		return subLines(startLine, endLine, getText().toString());
	}

	final public static size subLines(int startLine, int endLine, String src)
	{
		size j = new size();
		j.start = subLines(startLine,src).start;
		j.end = String_Splitor.NIndex('\n', src, j.start, endLine-startLine);
		
		//让我们用同样的方式来对待j.end
		if(j.end==0){
			;
		}
		else if (j.end < 0){
			j.end = src.length();
		}
		else if(j.end < src.length()){
			++j.end;
		}
		
		return j;
	}
	final public static size subLines(int startLine, String src)
	{
		size j = new size();
		int index = String_Splitor.NIndex('\n', src, 0, startLine - 1);
		//先走到startLine之前的'\n'位置
		
		if(index==0){
			//找到的index为0，则startLine<=1
			j.start = index;
		}
		else if (index < 0){
			//没有找到startLine，一定超出范围了
			j.start = src.length();
		}
		else if(index < src.length()){
			//找到了，如果index不会超出src.length()，从当前位置加1，走到startLine的起始
			j.start = index + 1;
		}
		
		j.end = src.length();
		//末尾永远是src.length()
		return j;
	}

	final public void lockSelf(boolean is)
	{
		if (is)
			setKeyListener(null);
		else
			setKeyListener(listener);
	}

	final public static void closeInputor(Context context, View editText)
	{
		editText.requestFocus();//请求焦点
		InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}
	final public static void openInputor(Context context, View editText)
	{
		editText.requestFocus();
		InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.showSoftInput(editText, 0);
	}

	public void zoomBy(float size)
	{
		TextSize *= size;
		setTextSize(TextSize);
	}

}
