package com.mycompany.who.Edit.Base.Edit;
import android.content.*;
import android.view.*;
import com.mycompany.who.Edit.Share.*;
import com.mycompany.who.Edit.Share.Share1.*;

public class EditLine extends Edit
{
	
	public int LineCount=0;
	
	public EditLine(Context cont){
		super(cont);
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
	public wordIndex WAndH()
	{
		return new wordIndex(maxWidth(),maxHeight(),(byte)0);
	}
	
	

}
