package com.mycompany.who.Edit.ListenerVistor.EditListener;

import android.graphics.*;
import android.text.*;
import android.widget.*;
import android.util.*;

public abstract class EditCanvaserListener extends EditListener
{
	
	public static final int OnDraw = 0;
	public static final int AfterDraw = 1;
	
	public abstract void onDraw(EditText self,Canvas canvas,TextPaint paint,Rect Cursor_bounds);

	public abstract void afterDraw(EditText self,Canvas canvas,TextPaint paint,Rect Cursor_bounds)
	
	final public void LetMeCanvaser(EditText self, Canvas canvas, TextPaint paint, Rect Cursor_bounds,int flag)
	{
		try{
			if(Enabled())
				Canvaser(self,canvas,paint,Cursor_bounds,flag);
		}catch(Exception e){
			Log.e("Canvaser Error", toString()+" "+e.toString());
		}
	}
	
	protected void Canvaser(EditText self, Canvas canvas, TextPaint paint, Rect Cursor_bounds,int flag){
		if(flag == OnDraw)
		    onDraw(self,canvas,paint,Cursor_bounds);
		else if(flag==AfterDraw)
			afterDraw(self,canvas,paint,Cursor_bounds);
	}
}
	
