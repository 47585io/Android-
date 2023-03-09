package com.mycompany.who.Edit.DrawerEdit.Base;
import android.content.*;
import android.view.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;

public class EditLine extends Edit
{
	
	public int LineCount=1;
	
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
		append( LineCount+"\n");
		setWidth((int)(String.valueOf(LineCount).length() * (int)getTextSize()));
		++LineCount;
	}
	final public void delALine()
	{
		String src = getText().toString();
		int end = src.lastIndexOf('\n', src.length() - 2);
		if (end != -1)
			getText().delete(end+1, src.length());
	    setWidth((int)(String.valueOf(LineCount).length() * (int)getTextSize()));
		--LineCount;
	}

	@Override
	public int maxWidth()
	{
		return (int)(String.valueOf (LineCount).length()*getTextSize());
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
