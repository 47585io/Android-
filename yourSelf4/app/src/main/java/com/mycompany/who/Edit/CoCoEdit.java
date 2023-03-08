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
	protected boolean isUR=false;
	public Edit lines;
	private List<EditListener> mlistenerVS;
	
	public CoCoEdit(Context cont)
	{
		super(cont);
		mlistenerVS=new ArrayList<>();
		mlistenerVS.add(new DefaultCanvaser());
		this.lines=new Edit(cont);	
		lines.setFocusable(false);
	}
	public CoCoEdit(Context cont,CoCoEdit Edit){
		super(cont,Edit);
		mlistenerVS=Edit.getCanvaserList();
		this.lines=Edit.lines;
	}
	
	public List<EditListener> getCanvaserList(){
		return mlistenerVS;
	}
	@Override
	public void clearListener()
	{
		mlistenerVS.clear();
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
		if(Runner!=null){
		    for(EditListener li:getCanvaserList())
		        Runner.CanvaserForLi(this,canvas,paint,bounds,(EditCanvaserListener)li);
		}
		super.onDraw(canvas);
    }
	
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
	{
		//为什么不直接在最后的函数中设置一个IsModify2，这是因为并非所有子类onTextChanged中都判断是否IsModify，例如记录Uedo或Redo
		//为什么还要IsModify，这是因为有些函数修改时根本一次都不想调onTextChanged
		
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
	final public void addLines(int count){
		while (count-->0)
		{
			addALine();
		} 	 	 
	}
	final public void delLines(int count){
		while (count-->0)
		{
			delALine();
		} 	 	 
	}
	final public void addALine()
	{
		lines.append(lines.getLineCount()+"\n");
		lines.setWidth((lines.getLineCount()+ "").length() * (int)lines.getTextSize());
	}
	final public void delALine()
	{
		String src = lines.getText().toString();
		int end = src.lastIndexOf('\n', src.length() - 2);
		if (end != -1)
			lines.getText().delete(end+1, src.length());
		lines.setWidth((lines.getLineCount() + "").length() * (int)lines.getTextSize());
	}
	
	public void zoomBy(float size){
		Text_Size=size;
		setTextSize(size);
	    lines.setWidth((lines.getLineCount()+"").length()*(int)lines.getTextSize());
		lines.setTextSize(size);
	}
	
	final public wordIndex getCursorPos(int offset)
	{
		//获取光标坐标
		int lines= getLayout().getLineForOffset(offset);
		Rect bounds = new Rect();
		//任何传参取值都必须new
		wordIndex pos = new wordIndex(0, 0, (byte)0);
		getLineBounds(lines, bounds);
	    pos.start=bounds.centerX();
		pos.end=bounds.centerY();

		int index= tryLine_Start(getText().toString(),offset);
		pos.start=(int)( (offset- index)*Text_Size);

		return pos;
	}
	final public wordIndex getRawCursorPos(int offset, int width, int height)
	{
		//获取绝对光标坐标
		wordIndex pos = getCursorPos(offset);
		pos.start=pos.start % width;
		pos.end=pos.end % height;
		return pos;
	}
	final public wordIndex getScrollCursorPos(int offset, int scrollx, int scrolly)
	{
		//获取存在滚动条时的绝对光标坐标
		//当前屏幕起始0相当于scroll滚动量,然后用cursorpos-scroll，就是当前屏幕光标绝对坐标	
		wordIndex pos = getCursorPos(offset);
		pos.start=pos.start - scrollx;
		pos.end=pos.end - scrolly;		
		return pos;
	}

	final public int fromy_getLineOffset(int y)
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
	
	
	
	public static class DefaultCanvaser extends EditCanvaserListener
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
	public static EditListener getDefultCanvaser(){
		return new DefaultCanvaser();
	}

}
