package com.mycompany.who.Edit.ListenerVistor.EditListener;


import android.text.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Share.*;
import java.util.*;

public abstract class EditFinderListener extends EditListener
{
	abstract public void OnFindWord(List<BaseEdit.DoAnyThing> totalList,Words WordLib);
	
	abstract public void OnFindNodes(List<BaseEdit.DoAnyThing> totalList,Words WordLib);
	
	abstract public void OnClearFindWord(Words WordLib);
	
	abstract public void OnClearFindNodes(int start,int end,String text,List<wordIndex> nodes);
	
	public void setSapns(String text,List<wordIndex> nodes,SpannableStringBuilder builder){
		builder.append(text);
		Colors.ForeColorText(builder,nodes);
	}
}
