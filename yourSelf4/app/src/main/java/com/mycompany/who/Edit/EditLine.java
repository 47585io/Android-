package com.mycompany.who.Edit;

import android.content.*;
import android.graphics.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import com.mycompany.who.Edit.ListenerVistor.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import java.util.*;

public class EditLine extends Edit implements CodeEdit.myCanvaser,EditListenerInfoUser
{

	public int LineCount=0;
	protected EditLineListenerInfo Info;
	protected size pos=new size();
	
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
		Info = new EditLineListenerInfo();
	}

	@Override
	public void CopyFrom(Edit target)
	{
		LineCount = ((EditLine)target).LineCount;
		super.CopyFrom(target);
		this.Info = ((EditLine)target).Info;
	}

	@Override
	public void CopyTo(Edit target)
	{
		((EditLine)target). LineCount = LineCount;
		super.CopyTo(target);
		((EditLine)target).Info = Info;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		TextPaint paint = getPaint();
		try
		{		
		    DrawAndDraw(canvas,paint,pos,EditCanvaserListener.OnDraw);
			super.onDraw(canvas);
			DrawAndDraw(canvas,paint,pos,EditCanvaserListener.AfterDraw);	
		}
		catch (Exception e)
		{
			Log.e("OnDraw Error", e.toString());
		}
	}

	public EditListenerList getCanvaserList()
	{
		if(Info!=null)
			return Info.mlistenerVS;
		return null;
	}
	
	@Override
	public void DrawAndDraw( Canvas canvas, TextPaint paint, size pos, int flag)
	{
		EditListenerList listener = getCanvaserList();
		if(listener!=null){
			List<EditListener> lis = listener.getList();
			for(EditListener li:lis)
			    if(li instanceof EditCanvaserListener)
			        ((EditCanvaserListener)li).LetMeCanvaser(this,canvas,paint,pos,flag);
		}
	}
	
	@Override
	public EditListenerInfo getInfo()
	{
		return Info;
	}

	@Override
	public void setInfo(EditListenerInfo i)
	{
		//必须传递EditLineListenerInfo及其子类
		if(i instanceof EditLineListenerInfo)
			Info = (EditLine.EditLineListenerInfo) i;
	}

	@Override
	public void trimListener()
	{
	}

	@Override
	public void clearListener()
	{
		if(Info!=null)
		    Info.mlistenerVS.getList().clear();
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
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		pos.start=(int) event.getX();
		pos.end=(int) event.getY();
		return super.onTouchEvent(event);
	}
	
	public static class EditLineListenerInfo implements EditListenerInfo
	{

	    public EditListenerList mlistenerVS;
		
		public EditLineListenerInfo(){
			mlistenerVS = new EditListenerList();
		}
		
		@Override
		public boolean addAListener(EditListener li)
		{
			if(li instanceof EditCanvaserListener){
				mlistenerVS.getList().add(li);
				return true;
			}
			return false;
		}

		@Override
		public boolean delAListener(EditListener li)
		{
			if(mlistenerVS.getList().remove(li))
				return true;
			return false;
		}

		@Override
		public EditListener findAListener(String name)
		{
			return Helper.checkName(mlistenerVS,name);
		}
		
		@Override
		public boolean addListenerTo(EditListener li, int toIndex)
		{
			// TODO: Implement this method
			return false;
		}

		@Override
		public boolean delListenerFrom(int fromIndex)
		{
			// TODO: Implement this method
			return false;
		}

		@Override
		public EditListener findAListener(int fromIndex)
		{
			// TODO: Implement this method
			return null;
		}
		
	}

}
