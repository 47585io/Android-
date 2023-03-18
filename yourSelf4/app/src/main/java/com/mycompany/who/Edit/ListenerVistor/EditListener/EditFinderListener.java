package com.mycompany.who.Edit.ListenerVistor.EditListener;


import android.text.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Share.*;
import java.util.*;
import android.util.*;

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
	
	final public List<wordIndex> LetMeFind(int start, int end, String text, Words WordLib)
	{
		if (!Enabled())
			return null;

		List<wordIndex> nodes = null;
		try{
		    nodes = Find(start,end,text,WordLib);
		}
		catch (Exception e)
		{
			Log.e("Finding Error", toString()+" "+e.toString());
			return null;
		}
		return nodes;
	}

	protected List<wordIndex> Find(int start, int end, String text, Words WordLib){
		
		List<BaseEdit. DoAnyThing> totalList =new ArrayList<>() ;
		List<wordIndex> nodes=new ArrayList<>();

		OnFindWord(totalList, WordLib);
		BaseEdit. startFind(text, totalList,nodes);
		totalList.clear();
		OnClearFindWord(WordLib);
		OnFindNodes(totalList,WordLib);
		BaseEdit. startFind(text, totalList,nodes);
		OnClearFindNodes(start, end, text, nodes);
		
		return nodes;
	}
	
	
}
