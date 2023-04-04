package com.mycompany.who.Edit.ListenerVistor.EditListener;

import android.text.*;
import android.util.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Share.Share1.*;
import java.util.*;
import android.widget.*;

public abstract class EditFinderListener extends EditListener
{
	abstract public void OnFindWord(List<CodeEdit.DoAnyThing> totalList,Words WordLib,EditText self);
	
	abstract public void OnFindNodes(List<CodeEdit.DoAnyThing> totalList,Words WordLib,EditText self);
	
	abstract public void OnClearFindWord(Words WordLib,EditText self);
	
	abstract public void OnClearFindNodes(int start,int end,String text,EditText self,List<wordIndex> nodes);
	
	
	final public List<wordIndex> LetMeFind(int start, int end,String text,Words WordLib, EditText self)
	{
		if (!Enabled())
			return null;

		List<wordIndex> nodes = null;
		try{
		    nodes = Find(start,end,text,WordLib,self);
		}
		catch (Exception e)
		{
			Log.e("Finding Error", toString()+" "+e.toString());
			return null;
		}
		return nodes;
	}

	protected List<wordIndex> Find(int start, int end, String text,Words WordLib, EditText self){
		
		List<CodeEdit. DoAnyThing> totalList =new ArrayList<>() ;
		List<wordIndex> nodes=new ArrayList<>();

		OnFindWord(totalList, WordLib,self);
		CodeEdit. startFind(text.substring(start,end), totalList,nodes);
		totalList.clear();
		OnClearFindWord(WordLib,self);
		
		OnFindNodes(totalList,WordLib,self);
		CodeEdit. startFind(text.substring(start,end), totalList,nodes);
		OnClearFindNodes(start, end, text, self, nodes);
		
		return nodes;
	}
	
	
}
