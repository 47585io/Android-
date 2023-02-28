package com.mycompany.who.Edit.DrawerEdit.EditListener;

import android.graphics.*;
import android.text.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import android.widget.*;

public abstract class EditCanvaserListener extends EditListener
{
	public abstract void onDraw(EditText self,Canvas canvas,TextPaint paint,Rect Cursor_bounds);
}
	
