package com.mycompany.who.Edit;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.text.*;
import android.text.method.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import java.lang.reflect.*;
import java.util.*;

import com.mycompany.who.Edit.DrawerEdit.EditListener.*;
import com.mycompany.who.Edit.Share.*;
import com.mycompany.who.Edit.DrawerEdit.*;
import android.util.*;
import com.mycompany.who.Edit.DrawerEdit.Base.*;

public class CoCoEdit extends CompleteEdit 
{
	public static int CursorRect_Color=0x25616263;
	public boolean isUR=false;
	public Edit lines;
	protected ArrayList<EditListener> mlistenerCan;
	
	public CoCoEdit(Context cont)
	{
		super(cont);
		mlistenerCan=new ArrayList<>();
		mlistenerCan.add(new DefaultCanvaser());
		this.lines=new Edit(cont);	
		lines.setFocusable(false);
	}
	public CoCoEdit(Context cont,CoCoEdit Edit){
		super(cont,Edit);
		mlistenerCan=Edit.mlistenerCan;
		this.lines=Edit.lines;
	}
	public CoCoEdit(Context cont,AttributeSet set){
		super(cont,set);
	}
	@Override
	public void reSet()
	{
		super.reSet();
		lines.config();
		mlistenerCan.add(new DefaultCanvaser());
	}
	
	public ArrayList<EditListener> getCanvaserList(){
		return mlistenerCan;
	}
	@Override
	public void clearListener()
	{
		mlistenerCan.clear();
		super.clearListener();
	}
	
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		//获取当前控件的画笔
        TextPaint paint = getPaint();
		int lines= getLayout().getLineForOffset(getSelectionStart());
		Rect bounds = new Rect();
		getLineBounds(lines, bounds);
		for(EditListener li:getCanvaserList())
		    if(li!=null)
			    ((EditCanvaserListener)li).onDraw(this,canvas,paint,bounds);
		super.onDraw(canvas);
    }
	
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
	{
		//为什么不直接在最后的函数中设置一个IsModify2，这是因为并非所有子类onTextChanged中都判断是否IsModify，例如记录Uedo或Redo
		//为什么还要IsModify，这是因为有些函数修改时根本一次都不想调onTextChanged
		if (!isDraw&&!isFormat){
			
		if (lengthAfter != 0)
		{
			ArrayList<Integer> indexs = null;
			indexs = String_Splitor.indexsOf("\n", text.toString().substring(start, start + lengthAfter));	
			addLines(indexs.size());
		}
		}
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
	}

	public void reLines(int line){
		int caline= line- lines.getLineCount();
		if(caline<0){
			while(caline++<0){
				delALine();
			}
		}
		else if(caline>0){
			while(caline-->0){
				addALine();
			}
		}
	}
	public void addLines(int count){
		while (count-->0)
		{
			addALine();
		} 	 	 
	}
	public void delLines(int count){
		while (count-->0)
		{
			delALine();
		} 	 	 
	}
	public void addALine()
	{
		lines.append(lines.getLineCount()+"\n");
		lines.setWidth((lines.getLineCount()+ "").length() * (int)lines.getTextSize());
	}
	public void delALine()
	{
		String src = lines.getText().toString();
		int end = src.lastIndexOf('\n', src.length() - 2);
		if (end != -1)
			lines.getText().delete(end+1, src.length());
		lines.setWidth((lines.getLineCount() + "").length() * (int)lines.getTextSize());
	}
	
	public wordIndex getCursorPos(int offset)
	{
		//获取光标坐标
		int lines= getLayout().getLineForOffset(offset);
		Rect bounds = new Rect();
		//任何传参取值都必须new
		wordIndex pos = new wordIndex(0, 0, (byte)0);
		getLineBounds(lines, bounds);
	    pos.start=bounds.centerX();
		pos.end=bounds.centerY();
	    
		int nowN=fromy_getLineOffset(pos.end);
		pos.start=(int)((offset-nowN)*Text_Size);
		
		return pos;
	}
	public wordIndex getRawCursorPos(int offset, int width, int height)
	{
		//获取绝对光标坐标
		wordIndex pos = getCursorPos(offset);
		pos.start=pos.start % width;
		pos.end=pos.end % height;
		return pos;
	}
	public wordIndex getScrollCursorPos(int offset, int scrollx, int scrolly)
	{
		//获取存在滚动条时的绝对光标坐标
		//当前屏幕起始0相当于scroll滚动量,然后用cursorpos-scroll，就是当前屏幕光标绝对坐标	
		wordIndex pos = getCursorPos(offset);
		pos.start=pos.start - scrollx;
		pos.end=pos.end - scrolly;		
		return pos;
	}

	public int fromy_getLineOffset(int y)
	{
		float xLine;
		int nowN = 0;
		xLine=y / getLineHeight();
	
		while (xLine-- > 0)
		{
			nowN=tryLine_End(getText().toString(), nowN + 1);
			//从起始行开始，向后试探至那行的offset
		}
		return nowN;
	}
	public int fromPos_getCharOffset(int x, int y)
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
	
	public void zoomBy(float size){
		setTextSize(size);
	    lines.setWidth((lines.getLineCount()+"").length()*(int)lines.getTextSize());
		lines.setTextSize(size);
	}
	public int Uedo_(EditDate.Token token)
	{
		IsModify++;
		isUR=true;
		int endSelection=0;
		if (token != null)
		{
			//范围限制
			if(token.start<0)
				token.start=0;
			if(token.end>getText().length())
				token.end=getText().length();
				
			if (token.src == "")
			{
				stack.Reput(token.start, token.start, getText().subSequence(token.start, token.end).toString());
				//如果Uedo会将范围内字符串删除，则我要将其保存，待之后插入
				getText().delete(token.start, token.end);	
				endSelection=token.start;
			}
			else if (token.start == token.end)
			{
				//如果Uedo会将在那里插入一个字符串，则我要将其下标保存，待之后删除
				stack.Reput(token.start, token.start + token.src.length(), "");
				getText().insert(token.start, token.src);
				endSelection=token.start + token.src.length();
			}
			else
			{
				stack.Reput(token.start, token.start + token.src.length(), getText().subSequence(token.start, token.end).toString());
				//另外的，则是反向替换某个字符串
			    getText().replace(token.start, token.end, token.src);
				endSelection=token.start + token.src.length();
			}
		}
		isUR=false;
		IsModify--;
		return endSelection;
	}

	public int Redo_(EditDate.Token token)
	{
		IsModify++;
		isUR=true;
		int endSelection=0;
		if (token != null)
		{
			if(token.start<0)
				token.start=0;
			if(token.end>getText().length())
				token.end=getText().length();
				
			if (token.src == "")
			{
				stack.put(token.start, token.start , getText().subSequence(token.start, token.end).toString());
				//如果Redo会将范围内字符串删除，则我要将其保存，待之后插入
				getText().delete(token.start, token.end);
				endSelection=token.start;
			}
			else if (token.start == token.end)
			{
				//如果Redo会将在那里插入一个字符串，则我要将其下标保存，待之后删除
				stack.put(token.start, token.start + token.src.length(), "");
				getText().insert(token.start, token.src);
				endSelection=token.start + token.src.length();
			}
			else
			{
				stack.put(token.start, token.start + token.src.length(), getText().subSequence(token.start, token.end).toString());
				//另外的，则是反向替换某个字符串
			    getText().replace(token.start, token.end, token.src);
				endSelection=token.start + token.src.length();
		    }
		}
		isUR=false;
		IsModify--;
		return endSelection;
	}

	public void Uedo()
	{
		//批量Uedo
		if(stack==null)
			return;
		
		EditDate.Token token;	
		int endSelection;
		try
		{
			while (true)
			{
				token=stack.getLast();
				endSelection=Uedo_(token);
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
		{}
	}
	public void Redo()
	{
		//批量Redo
		if(stack==null)
			return;
		
		EditDate.Token token;
		int endSelection;
		try
		{
			while (true)
			{
				token=stack.getNext();
				endSelection=Redo_(token);
				setSelection(endSelection);
				EditDate.Token token2=stack.seeNext();
				if (token2 == null)
					return;
				else if ( token2.start == token.end)	
					continue;
				else
					break;
			}
		}
		catch (Exception e)
		{}
	}
	
	class DefaultCanvaser extends EditCanvaserListener
	{

		@Override
		public void onDraw(EditText self,Canvas canvas, TextPaint paint,Rect bounds)
		{
			//设置画笔的描边宽度值
			paint.setStrokeWidth(0.2f);
			paint.setStyle(Paint.Style.FILL_AND_STROKE);

			//任何修改都会触发重绘，这里在光标位置画矩形
			
			paint.setColor(CursorRect_Color);
			canvas.drawRect(bounds,paint);
			
		}
		
	}

}
