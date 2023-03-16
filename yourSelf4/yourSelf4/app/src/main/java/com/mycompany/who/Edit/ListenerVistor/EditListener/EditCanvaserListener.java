package com.mycompany.who.Edit.ListenerVistor.EditListener;

import android.graphics.*;
import android.text.*;
import android.widget.*;
import android.util.*;

public abstract class EditCanvaserListener extends EditListener
{
	public abstract void onDraw(EditText self,Canvas canvas,TextPaint paint,Rect Cursor_bounds);

	final public void LetMeCanvaser(EditText self, Canvas canvas, TextPaint paint, Rect Cursor_bounds)
	{
		try{
			if(Enabled())
				Canvaser(self,canvas,paint,Cursor_bounds);
		}catch(Exception e){
			Log.e("Canvaser Error", toString()+" "+e.toString());
		}
	}
	
	protected void Canvaser(EditText self, Canvas canvas, TextPaint paint, Rect Cursor_bounds){
		onDraw(self,canvas,paint,Cursor_bounds);
	}
}
	
