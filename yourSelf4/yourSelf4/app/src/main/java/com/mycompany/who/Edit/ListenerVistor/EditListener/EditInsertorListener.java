package com.mycompany.who.Edit.ListenerVistor.EditListener;

import android.text.*;
import java.util.*;
import android.util.*;

public abstract class EditInsertorListener extends EditListener
{
	private HashMap<String,String> words;
	public EditInsertorListener(){
		words= new HashMap<String,String>();
	}
	abstract public void putWords(HashMap<String,String> words)
	
	public int dothing_insert(Editable editor, int nowIndex){
		String src = editor.toString();
		for(String start:words.keySet()){
			if(src.indexOf(start)==0){
				String end = words.get(start);
				editor.insert(nowIndex+1,end);
				return nowIndex+end.length();
			}
		}
		return nowIndex+1;
	}
	
	final public int LetMeInsert(Editable editor, int nowIndex)
	{
		int newIndex = nowIndex;
		try
		{
			if (Enabled())
				newIndex= Insert(editor,nowIndex);
		}
		catch (IndexOutOfBoundsException e)
		{
			Log.e("Inserting Error", toString()+" "+e.toString());
			return nowIndex;
		}
		return newIndex;
	}
	
	protected int Insert(Editable editor, int nowIndex){
		return dothing_insert(editor, nowIndex);
	}
}
