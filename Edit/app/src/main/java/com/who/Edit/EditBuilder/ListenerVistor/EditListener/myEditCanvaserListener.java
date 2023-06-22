package com.who.Edit.EditBuilder.ListenerVistor.EditListener;

import android.graphics.*;
import android.text.*;
import android.util.*;
import android.widget.*;
import com.who.Edit.Base.Share.Share1.*;
import com.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import android.view.*;


/*
  在画布上绘画
  
  此监听器非常灵活，因为提供了画布，画笔和编辑器。而且一般这个监听器用于onDraw，可以实时刷新

  self表示编辑器本身，canvas和paint分别表示编辑器的画布和画笔，pos是一个坐标，它可能是编辑器上次被触摸的坐标，也可能是光标坐标，这由具体的编辑器决定
  
  在onDraw和afterDraw中都可以进行绘制，不同的是，onDraw在super.onDraw之前，而afterDraw在super.onDraw之后
  
*/
public abstract class myEditCanvaserListener extends myEditListener implements EditCanvaserListener
{
	
	public static final int BeforeDraw = 256;
	//在编辑器绘制前，进行绘制
	
	abstract protected void beforeDraw(View self,Canvas canvas,TextPaint paint,pos pos);
	
	abstract protected void afterDraw(View self,Canvas canvas,TextPaint paint,pos pos)
	
	
	@Override
	public void onDraw(View self, Canvas canvas, TextPaint paint, pos pos)
	{
		int flag = getFlag();
		
		if((flag & BeforeDraw) == BeforeDraw){
		    beforeDraw(self,canvas,paint,pos);
		}
		else{
			afterDraw(self,canvas,paint,pos);
		}
	}
	
}
