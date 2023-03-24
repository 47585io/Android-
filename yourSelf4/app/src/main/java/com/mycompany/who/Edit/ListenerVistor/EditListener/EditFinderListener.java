package com.mycompany.who.Edit.ListenerVistor.EditListener;

import android.text.*;
import android.util.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Share.Share1.*;
import java.util.*;

public abstract class EditFinderListener extends EditListener
{
	abstract public void OnFindWord(List<CodeEdit.DoAnyThing> totalList,Words WordLib);
	
	abstract public void OnFindNodes(List<CodeEdit.DoAnyThing> totalList,Words WordLib);
	
	abstract public void OnClearFindWord(Words WordLib);
	
	abstract public void OnClearFindNodes(int start,int end,String text,List<wordIndex> nodes);
	
	public Colors.ByteToColor2 BToC = null;
	
	public void setSapns(String text,List<wordIndex> nodes,SpannableStringBuilder builder){
		builder.append(text);
		Colors.ForeColorText(builder,nodes,getByteToColor());
	}
	public Colors.ByteToColor2 getByteToColor(){
		return BToC;
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
		
		List<CodeEdit. DoAnyThing> totalList =new ArrayList<>() ;
		List<wordIndex> nodes=new ArrayList<>();

		OnFindWord(totalList, WordLib);
		CodeEdit. startFind(text, totalList,nodes);
		totalList.clear();
		OnClearFindWord(WordLib);
		
		OnFindNodes(totalList,WordLib);
		CodeEdit. startFind(text, totalList,nodes);
		OnClearFindNodes(start, end, text, nodes);
		
		return nodes;
	}
	
	
}
