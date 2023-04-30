package com.mycompany.who.SuperVisor.CodeMoudle.Base.View3;
import android.widget.*;
import android.content.*;
import android.util.*;
import com.mycompany.who.Edit.*;
import java.util.*;

public class LineGroup extends LinearLayout implements EditLine.LineSpiltor
{
	public static int MaxLine = 5000;
	private int lineCount;
	private List<EditLine> LineList = new ArrayList<>();
	
	public LineGroup(Context cont){
		super(cont);
		init();
	}
	public LineGroup(Context cont,AttributeSet attrs){
		super(cont,attrs);
		init();
	}
	protected void init(){
		setOrientation(VERTICAL);
	}
	
	public List<EditLine> getLineList(){
		return LineList;
	}
	
	public void addEditLine(){
		EditLine Line = LineList.get(LineList.size()-1);
		Line = new EditLine(getContext(),Line);
		LineList.add(Line);
		addView(Line);
	}
	
	@Override
	public void reLines(int line)
	{
		int caline= line-lineCount;
		if(caline<0){
			delLines(-caline);
		}
		else if(caline>0){
			addLines(caline);
		}
	}
	
	@Override
	public void addLines(int count)
	{
		lineCount+=count;
		EditLine Line = LineList.get(LineList.size()-1);
		EditLine Line2 = LineList.get(LineList.size()-2);
		int freeLine = MaxLine - Line.getLineCount()-Line2.getLineCount();
		Line.addLines(freeLine);
		count -= freeLine;
		for(;count>0;){
			addEditLine();
			
		}
	}

	@Override
	public void delLines(int count)
	{
		
	}

	@Override
	public void addALine()
	{
		// TODO: Implement this method
	}

	@Override
	public void delALine()
	{
		// TODO: Implement this method
	}

	@Override
	public int getLineCount()
	{
		return lineCount;
	}

}
