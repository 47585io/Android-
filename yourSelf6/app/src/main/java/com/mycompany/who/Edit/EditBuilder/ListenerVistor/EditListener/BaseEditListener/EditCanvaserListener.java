package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;
import android.graphics.*;
import android.text.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;

public abstract interface EditCanvaserListener extends EditListener
{
	public abstract void onDraw(EditText self, Canvas canvas, TextPaint paint, size pos,int flag)
}
