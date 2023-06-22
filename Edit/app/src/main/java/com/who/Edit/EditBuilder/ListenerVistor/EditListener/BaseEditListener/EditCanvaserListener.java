package com.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

import android.graphics.*;
import android.text.*;
import android.widget.*;
import com.who.Edit.Base.Share.Share1.*;
import android.view.*;

public abstract interface EditCanvaserListener extends EditListener
{
	public abstract void onDraw(View self, Canvas canvas, TextPaint paint, pos pos)
}
