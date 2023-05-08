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
import static com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListenerInfo.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.EditListener.RunLi;


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

	public EditListener getCanvaser()
	{
		return Info!=null ? Info.findAListener(CanvaserIndex):null;
	}
	public boolean setCanvaser(EditListener li)
	{
		return Info!=null ? Info.addListenerTo(li,CanvaserIndex):false;
	}
	public EditListener getLineChecker()
	{
		return Info!=null ? Info.findAListener(LineCheckerIndex):null;
	}
	public boolean setLineChecker(EditListener li)
	{
		return Info!=null ? Info.addListenerTo(li,LineCheckerIndex):false;
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
		    Info.clear();
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
			super.onDraw(canvas);
		}
	}

	@Override
	public void DrawAndDraw( final Canvas canvas, final TextPaint paint, final size pos, final int flag)
	{
		EditListener lis = getCanvaser();
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li instanceof EditCanvaserListener){
			        ((EditCanvaserListener)li).LetMeCanvaser(EditLine.this, canvas, paint, pos, flag);
				}
				return false;
			}
		};
		lis.dispatchCallBack(run);
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
	
	protected void onLineChange(final int start,final int before,final int after)
	{
		EditListener lis = getLineChecker();
		if(lis==null)
			return;
		RunLi run = new RunLi()
		{
			public boolean run(EditListener li)
			{
				if(li instanceof EditLineChangeListener){
			        ((EditLineChangeListener)li).Change(start,before,after);
				}
				return false;
			}
		};
		lis.dispatchCallBack(run);
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
				mlistenerVS.add(li);
				return true;
			}
			else if(li instanceof EditLineChangeListener){
				mlistenerLS.add(li);
				return true;
			}
			return false;
		}

		@Override
		public boolean delAListener(EditListener li)
		{
			if(mlistenerVS.remove(li))
				return true;
			if(mlistenerLS.remove(li))
				return true;
			return false;
		}

		@Override
		public EditListener findAListener(String name)
		{
			return null;
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
		
		@Override
		public int size()
		{
			// TODO: Implement this method
			return 0;
		}

		@Override
		public void clear()
		{
			// TODO: Implement this method
		}

		@Override
		public boolean contrans(EditListener li)
		{
			return false;
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
