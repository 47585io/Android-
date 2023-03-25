package com.mycompany.who.Edit.Base.Edit;
import android.content.*;
import android.view.*;
import com.mycompany.who.Edit.Share.*;
import com.mycompany.who.Edit.Share.Share1.*;
import android.util.*;

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
		++LineCount;
		append( LineCount+"\n");
		setWidth((int)(String.valueOf(LineCount).length() * (int)getTextSize())+30);
	}
	final public void delALine()
	{
		--LineCount;
		String src = getText().toString();
		int end = src.lastIndexOf('\n', src.length() - 2);
		if (end != -1)
			getText().delete(end+1, src.length());
	    setWidth((int)(String.valueOf(LineCount).length() * (int)getTextSize())+30);
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
	
	

}
