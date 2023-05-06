package com.mycompany.who.Edit;

import java.util.*;
import android.content.*;
import android.graphics.*;
import android.text.*;
import android.util.*;
import android.view.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;


public class EditLine extends Edit implements CodeEdit.myCanvaser,EditListenerInfoUser
{

	//如果不是无关紧要的，别直接赋值，最后其实会在构造对象时赋值，等同于在构造函数最后赋值
	private int LineCount;
	private EditLineListenerInfo Info;
	private size pos=new size();
	
	public EditLine(Context cont){
		super(cont);
	}
	public EditLine(Context cont,EditLine Edit){
		super(cont,Edit);
	}
	public EditLine(Context cont,AttributeSet attrs){
		super(cont,attrs);
	}

	@Override
	public void Creat()
	{
		super.Creat();
		LineCount=0;
		Info = new EditLineListenerInfo();
	}

	@Override
	public void CopyFrom(Edit target)
	{
		super.CopyFrom(target);
		this.LineCount = ((EditLine)target).LineCount;
		this.Info = ((EditLine)target).Info;
	}

	@Override
	public void CopyTo(Edit target)
	{
		super.CopyTo(target);
		((EditLine)target). LineCount = LineCount;
		((EditLine)target).Info = Info;
	}

	public EditListenerList getCanvaserList()
	{
		if(Info!=null)
			return Info.mlistenerVS;
		return null;
	}
	public void setCanvaserList(EditListenerList l){
		if(Info!=null){
			Info.mlistenerVS = l;
		}
	}
	public EditListenerList getLineCheckerList()
	{
		if(Info!=null)
			return Info.mlistenerLS;
		return null;
	}
	public void setLineCheckerList(EditListenerList l)
	{
		if(Info!=null){
			Info.mlistenerLS = l;
		}
	}
	
	
	@Override
	public EditListenerInfo getInfo(){
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
	public void trimListener(){}

	@Override
	public void clearListener(){
		if(Info!=null)
		    Info.mlistenerVS.getList().clear();
	}

	
	@Override
	protected void onDraw(Canvas canvas)
	{
		TextPaint paint = getPaint();
		try
		{		
		    DrawAndDraw(canvas,paint,pos,myEditCanvaserListener.OnDraw);
			super.onDraw(canvas);
			DrawAndDraw(canvas,paint,pos,myEditCanvaserListener.AfterDraw);	
		}
		catch (Exception e)
		{
			Log.e("OnDraw Error", e.toString());
		}
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
	
	
	public void reLines(int line)
	{
		int caline= line-LineCount;
		if(caline<0){
			delLines(-caline);
		}
		else if(caline>0){
			addLines(caline);
		}
	}
	//如果需处理大量行，都应该调用addLines和delLines，因为它们更快
	final public void addLines(int count)
	{
		onLineChange(LineCount,0,count);
		StringBuilder b = new StringBuilder();
		while (count-->0)
		{
			++LineCount;
			b.append(LineCount);
			b.append('\n');
		} 	 	 
		append(b);
	}
	
	final public void delLines(int count)
	{
		onLineChange(LineCount,count,0);
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
		onLineChange(LineCount,0,1);
		++LineCount;
		append( LineCount+"\n");
	}
	
	final public void delALine()
	{
		onLineChange(LineCount,1,0);
		--LineCount;
		String src = getText().toString();
		int end = src.lastIndexOf('\n', src.length() - 2);
		if (end != -1)
			getText().delete(end+1, src.length());
	}
	
	@Override
	public int getLineCount(){
		return LineCount;
	}
	
	protected void onLineChange(int start,int before,int after)
	{
		EditListenerList l = getLineCheckerList();
		if(l!=null){
			l.setEdit(this);
			List<EditListener> lis = l.getList();
			for(EditListener li:lis){
				if(li instanceof EditLineChangeListener){
					((EditLineChangeListener)li).Change(LineCount,1,0);
				}
			}
		}
	}
	
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
	{
		setWidth(maxWidth());
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		pos.start=(int) event.getX();
		pos.end=(int) event.getY();
		return super.onTouchEvent(event);
	}
	
	
	@Override
	public int maxWidth(){
		return (int)((String.valueOf(LineCount).length()+1)*getTextSize());
	}
	
	@Override
	public int maxHeight(){
		return LineCount*(getLineHeight()+1);
	}
	
	@Override
	public size WAndH(){
		return new size(maxWidth(),maxHeight());
	}

	@Override
	public void zoomBy(float size){
		super.zoomBy(size);
		setWidth(maxWidth());
	}
	
	
	public static class EditLineListenerInfo implements EditListenerInfo
	{

		@Override
		public void clear()
		{
			// TODO: Implement this method
		}

		@Override
		public boolean contrans(EditListener li)
		{
			// TODO: Implement this method
			return false;
		}
		

	    protected EditListenerList mlistenerVS;
		protected EditListenerList mlistenerLS;
		
		public EditLineListenerInfo(){
			mlistenerVS = new myEditListenerList();
			mlistenerLS = new myEditListenerList();
		}
		
		@Override
		public boolean addAListener(EditListener li)
		{
			if(li instanceof EditCanvaserListener){
				mlistenerVS.getList().add(li);
				return true;
			}
			else if(li instanceof EditLineChangeListener){
				mlistenerLS.getList().add(li);
				return true;
			}
			return false;
		}

		@Override
		public boolean delAListener(EditListener li)
		{
			if(mlistenerVS.getList().remove(li))
				return true;
			if(mlistenerLS.getList().remove(li))
				return true;
			return false;
		}

		@Override
		public EditListener findAListener(String name)
		{
			EditListener li = null;
			li = Helper.checkName(mlistenerVS,name);
			if(li!=null)
				Helper.checkName(mlistenerLS,name);
			return li;
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
	
	
	public static interface LineSpiltor{
		
		public void reLines(int line)
		
		public void addLines(int count)
		
		public void delLines(int count)
	  
		public int getLineCount()
		
		public void onLineChange(int start,int before,int after)
		
	}

}
