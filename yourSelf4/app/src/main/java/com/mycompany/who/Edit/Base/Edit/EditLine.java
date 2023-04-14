package com.mycompany.who.Edit.Base.Edit;
import android.content.*;
import android.view.*;
import com.mycompany.who.Edit.Share.*;
import com.mycompany.who.Edit.Share.Share1.*;
import android.util.*;
import android.text.*;

public class EditLine extends Edit
{
	
	public int LineCount=0;
	
	public EditLine(Context cont){
		super(cont);
	}
	public EditLine(Context cont,EditLine Edit){
		super(cont);
	}
	public EditLine(Context cont,AttributeSet attrs){
		super(cont,attrs);
	}

	@Override
	public void Creat()
	{
		LineCount=0;
		super.Creat();
	}

	@Override
	public void CopyFrom(Edit target)
	{
		LineCount = ((EditLine)target).LineCount;
		super.CopyFrom(target);
	}

	@Override
	public void CopyTo(Edit target)
	{
		((EditLine)target). LineCount = LineCount;
		super.CopyTo(target);
	}
	
	
	public void reLines(int line){
		int caline= line-LineCount;
		if(caline<0){
			delLines(-caline);
		}
		else if(caline>0){
			addLines(caline);
		}
	}
	//如果需处理大量行，都应该调用addLines和delLines，因为它们更快
	final public void addLines(int count){
		StringBuilder b = new StringBuilder();
		while (count-->0)
		{
			++LineCount;
			b.append(LineCount+"\n");
		} 	 	 
		append(b);
	}
	final public void delLines(int count){
		Editable e = getText();
		int end = e.length()-1;
		String src = e.toString();
		while (count-->0)
		{
			--LineCount;
			end = src.lastIndexOf('\n', end-1);
		} 	 	 
		e.delete(end+1,e.length());
	}
	final public void addALine()
	{
		++LineCount;
		append( LineCount+"\n");
	}
	final public void delALine()
	{
		--LineCount;
		String src = getText().toString();
		int end = src.lastIndexOf('\n', src.length() - 2);
		if (end != -1)
			getText().delete(end+1, src.length());
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
	{
		setWidth((String.valueOf(LineCount).length() * (int)getTextSize())+30);
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
	}

	@Override
	public int maxWidth()
	{
		return (int)(String.valueOf (LineCount).length()*getTextSize())+30;
	}

	@Override
	public int maxHeight()
	{
		return LineCount*getLineHeight();
	}
	
	@Override
	public int getLineCount()
	{
		return LineCount;
	}

	@Override
	public size WAndH()
	{
		return new size(maxWidth(),maxHeight());
	}

	@Override
	public void zoomBy(float size)
	{
		super.zoomBy(size);
		setWidth(maxWidth());
	}
	

}
