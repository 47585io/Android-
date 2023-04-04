package com.mycompany.who.Edit.Base.Edit;
import android.content.*;
import android.graphics.*;
import android.text.method.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import com.mycompany.who.Edit.Share.Share1.*;
import com.mycompany.who.Edit.Share.Share3.*;
import java.util.*;
import org.xml.sax.ext.*;
import android.util.*;
import android.text.*;


/*
  一个bug:计算字符大小很麻烦，这里只管了纯英文字符
*/
public class Edit extends EditText implements Creat<Edit>
{

	@Override
	public void Creat()
	{
		config();
		listener = new EditText(getContext()).getKeyListener();
	}

	@Override
	public Edit CreatOne()
	{
		return new Edit(getContext());
	}

	@Override
	public void CopyFrom(Edit target)
	{
		config();
	}

	@Override
	public void CopyTo(Edit target)
	{
		target.config();
	}
	
	protected static KeyListener listener;
	public static int Selected_Color=0x75515a6b;
	public static int Background_Color=0;
	public static int Text_Color=0xffabb2bf;
	public static int CursorRect_Color=0x25616263;
	public float TextSize=14;
	
	public Edit(Context cont){
		super(cont);
		Creat();
	}
	public Edit(Context cont,AttributeSet attrs){
		super(cont,attrs);
		Creat();
	}
	public Edit(Context cont,Edit Edit)
	{
		super(cont);
		CopyFrom(Edit);
	}
	
	public void config(){	
		setTextColor(Text_Color);
		setBackgroundResource(0);
		setTypeface(Typeface.MONOSPACE);
		setHighlightColor(Selected_Color);
		setTextSize(TextSize);
		setLetterSpacing(0.01f);
		setLineSpacing(0.2f,1.2f);
		setPadding(0,0,0,0);
	}
	
	public float getTextSize(){
		return super.getTextSize()/1.65f;
	}
	
	public int getLineCount(){
		String src = getText().toString();	
		int Count = String_Splitor.Count('\n',src)+1;
		return Count;
	}
	
	public int maxHeight(){
		return getLineHeight()*(String_Splitor.Count('\n',getText().toString())+1);
	}
	public int maxWidth(){
		Editable editor = getText();
		List<Integer> indexs = String_Splitor.indexsOf('\n',editor.toString());
		if(indexs==null||indexs.size()==0){
			return (int)(editor.length()*getTextSize());
		}
		int width=0;
		int last=0;
		for(int i: indexs){
			int w=(i-last);
			if(w>width)
				width=w;
			last=i;
		}
		width=(width>editor.length()-last)?width:editor.length()-last;
		return (int)(width*getTextSize());
	}
	public size WAndH(){
		size size=new size();
		Editable editor = getText();
		List<Integer> indexs = String_Splitor.indexsOf('\n',editor.toString());
		if(indexs==null||indexs.size()==0){
			size.start= (int)(editor.length()*getTextSize());
			size.end=getLineHeight();
			return size;
		}
		int width=0;
		int last=0;
		for(int i: indexs){
			int w=(i-last);
			if(w>width)
				width=w;
			last=i;
		}
		width=(width>editor.length()-last)?width:editor.length()-last;
		//最后一行的长度是？
		size.start=(int)(width*getTextSize());
		size.end=(indexs.size()+1)*getLineHeight();
		return size;
	}
	
	public static final size LAndC(String text){
		//为外部文本测量行数与最宽的那行字符数
		size size=new size();
		List<Integer> indexs = String_Splitor.indexsOf('\n',text);
		if(indexs==null||indexs.size()==0){
			size.start= (text.length());
			size.end= 1;
			return size;
		}
		int width=0;
		int last=0;
		for(int i: indexs){
			int w=(i-last);
			if(w>width)
				width=w;
			last=i;
		}
		width=(width>text.length()-last)?width:text.length()-last;
		size.start=width;
		size.end=(indexs.size()+1);
		return size;
	}
	

	final public size subLines(int startLine){
		size j = new size();
		List<Integer> indexs = String_Splitor.indexsOf('\n',getText().toString());
		if(indexs==null)
			return j;
		j.start= indexs.get(startLine-2)+1;
		j.end=getText().toString().length();
		return j;
	}
	final public size subLines(int startLine,int endLine){
		size j = new size();
		List<Integer> indexs = String_Splitor.indexsOf('\n',getText().toString());
		if(indexs==null)
			return j;
		j.start= indexs.get(startLine-2)+1;
		j.end=indexs.get(endLine-2)+1;
		return j;
	}
	
	final public void lockSelf(boolean is){
		if(is)
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
	
	public void zoomBy(float size){
		setTextSize(size);
		TextSize=size;
	}
	
}
