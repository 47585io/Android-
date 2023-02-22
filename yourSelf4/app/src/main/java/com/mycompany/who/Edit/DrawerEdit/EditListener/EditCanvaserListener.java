package com.mycompany.who.Edit.DrawerEdit.EditListener;

import android.graphics.*;
import android.text.*;

public abstract class EditCanvaserListener extends EditListener
{
	public abstract void onDraw(Canvas canvas,TextPaint paint,Rect bounds);
}
	
