package com.mycompany.who.Edit.ListenerVistor.EditListener;

import android.graphics.*;
import android.text.*;
import android.util.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;

public abstract class EditCanvaserListener extends EditListener
{
	
	public static final int OnDraw = 0;
	public static final int AfterDraw = 1;
	
	public abstract void onDraw(EditText self,Canvas canvas,TextPaint paint,size pos);

	public abstract void afterDraw(EditText self,Canvas canvas,TextPaint paint,size pos)
	
	final public void LetMeCanvaser(EditText self, Canvas canvas, TextPaint paint, size pos,int flag)
	{
		try{
			if(Enabled())
				Canvaser(self,canvas,paint,pos,flag);
		}catch(Exception e){
			Log.e("Canvaser Error", toString()+" "+e.toString());
		}
	}
	
	protected void Canvaser(EditText self, Canvas canvas, TextPaint paint,size pos,int flag){
		if(flag == OnDraw)
		    onDraw(self,canvas,paint,pos);
		else if(flag==AfterDraw)
			afterDraw(self,canvas,paint,pos);
	}
}
	
