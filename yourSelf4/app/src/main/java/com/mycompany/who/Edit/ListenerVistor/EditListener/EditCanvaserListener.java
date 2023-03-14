package com.mycompany.who.Edit.ListenerVistor.EditListener;

import android.graphics.*;
import android.text.*;
import android.widget.*;

public abstract class EditCanvaserListener extends EditListener
{
	public abstract void onDraw(EditText self,Canvas canvas,TextPaint paint,Rect Cursor_bounds);
}
	
