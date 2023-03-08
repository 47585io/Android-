package com.mycompany.who.Edit.DrawerEdit.EditListener;

import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import java.util.*;
import com.mycompany.who.Edit.DrawerEdit.*;
import com.mycompany.who.Edit.DrawerEdit.Base.*;
import android.text.*;

public abstract class EditFinderListener extends EditListener
{
	abstract public void OnFindWord(List<DrawerBase.DoAnyThing> totalList,Words WordLib);
	
	abstract public void OnFindNodes(List<DrawerBase.DoAnyThing> totalList,Words WordLib);
	
	abstract public void OnClearFindWord(Words WordLib);
	
	abstract public void OnClearFindNodes(int start,int end,String text,List<wordIndex> nodes);
	
	public void setSapns(String text,List<wordIndex> nodes,SpannableStringBuilder builder){
		builder.append(text);
		Colors.ForeColorText(builder,nodes);
	}
}
