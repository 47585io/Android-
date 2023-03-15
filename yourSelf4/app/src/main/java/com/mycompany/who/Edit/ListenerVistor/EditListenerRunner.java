package com.mycompany.who.Edit.ListenerVistor;


import android.graphics.*;
import android.text.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import com.mycompany.who.Edit.Share.*;
import java.util.*;

/*
 一些Listener不懂，它们的函数怎么组合调用，Runner来解决！
 */
public interface EditListenerRunner
{
	
	public void FindForLi(int start,int end,String text,Words WorLib,List<wordIndex> nodes,SpannableStringBuilder builder,EditFinderListener li)
	
	public void DrawingForLi(int start, int end, List<wordIndex> nodes,SpannableStringBuilder builder,Editable editor,EditDrawerListener li)
	
	public String FormatForLi(int start,int end,String editor,EditFormatorListener li)
	
	public void InsertForLi(Editable editor, int nowIndex,EditInsertorListener li)
	
	public List<Icon> CompeletForLi(String wantBefore,String wantAfter,int before,int after,Words lib,EditCompletorListener li)
	
	public void CanvaserForLi(EditText self,Canvas canvas,TextPaint paint,Rect Cursor_bounds,EditCanvaserListener li)
}
