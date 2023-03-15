package com.mycompany.who.Edit.ListenerVistor;

import android.graphics.*;
import android.text.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import com.mycompany.who.Edit.Share.*;
import java.util.*;

public class EditListenerRunnerInfo
{
	public EditListenerRunner DR;
	
	public EditListenerRunner MR;
	
	public EditListenerRunner CR;
	
	public EditListenerRunner VR;
	
	public EditListenerRunnerInfo(){
		DR=EditRunnerFactory.getDrawerRunner();
		MR=EditRunnerFactory.getFormatRunner();
		CR=EditRunnerFactory.getCompleteRunner();
		VR=EditRunnerFactory.getCanvasRunner();
	}
	
	public void FindForLi(int start,int end,String text,Words WorLib,List<wordIndex> nodes,SpannableStringBuilder builder,EditFinderListener li){
		DR.FindForLi(start,end,text,WorLib,nodes,builder,li);
	}
	public void DrawingForLi(int start, int end, List<wordIndex> nodes,SpannableStringBuilder builder,Editable editor,EditDrawerListener li){
		DR.DrawingForLi(start,end,nodes,builder,editor,li);
	}
	public String FormatForLi(int start,int end,String editor,EditFormatorListener li){
		return MR.FormatForLi(start,end,editor,li);
	}
	public void InsertForLi(Editable editor, int nowIndex,EditInsertorListener li){
		MR.InsertForLi(editor,nowIndex,li);
	}
	public List<Icon> CompeletForLi(String wantBefore,String wantAfter,int before,int after,Words lib,EditCompletorListener li){
		return CR.CompeletForLi(wantBefore,wantAfter,before,after,lib,li);
	}
	public void CanvaserForLi(EditText self,Canvas canvas,TextPaint paint,Rect Cursor_bounds,EditCanvaserListener li){
		VR.CanvaserForLi(self,canvas,paint,Cursor_bounds,li);
	}
	
}
