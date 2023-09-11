package com.editor.text2.builder.listenerInfo.listener.baselistener;
import android.graphics.*;
import android.widget.*;
import com.editor.text.base.*;
import com.editor.text.*;
import android.text.*;

public abstract interface EditCanvaserListener extends EditListener
{
	public abstract void onDraw(Edit self, Canvas canvas, TextPaint paint)
}
