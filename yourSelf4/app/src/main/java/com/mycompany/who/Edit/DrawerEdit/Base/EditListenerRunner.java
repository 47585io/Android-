package com.mycompany.who.Edit.DrawerEdit.Base;
import java.util.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import android.widget.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;
import android.text.*;
import com.mycompany.who.Edit.Share.*;
import android.graphics.*;


public interface EditListenerRunner
{
	public ArrayList<wordIndex> FindForLi(int start,int end,String text,Words WorLib,OtherWords WorLib2,EditFinderListener li)
	public void DrawingForLi(int start, int end, ArrayList<wordIndex> nodes,EditText self,EditDrawerListener li)
	public String FormatForLi(int start,int end,String src,EditFormatorListener li)
	public void InsertForLi(Editable editor, int nowIndex,EditInsertorListener li)
	public ArrayList<Icon>  CompeletForLi(String wantBefore,String wantAfter,int before,int after,EditCompletorListener li)
	public void CanvaserForLi(EditText self,Canvas canvas,TextPaint paint,Rect Cursor_bounds,EditCanvaserListener li)
}
