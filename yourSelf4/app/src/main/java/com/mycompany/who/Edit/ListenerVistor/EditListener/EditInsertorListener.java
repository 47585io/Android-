package com.mycompany.who.Edit.ListenerVistor.EditListener;

import android.text.*;
import java.util.*;
import android.util.*;
import android.widget.*;

public abstract class EditInsertorListener extends EditListener
{
	private HashMap<CharSequence,CharSequence> words;
	public EditInsertorListener(){
		words= new HashMap<>();
	}
	
	abstract public void putWords(HashMap<CharSequence,CharSequence> words)
	
	public int dothing_insert(EditText self, int nowIndex){
		Editable editor = self.getText();
		String src = editor.toString();
		for(CharSequence start:words.keySet()){
			if(src.indexOf(start.toString())==0){
				CharSequence end = words.get(start);
				editor.insert(nowIndex+1,end);
				return nowIndex+end.length();
			}
		}
		return nowIndex+1;
	}
	
	final public int LetMeInsert(EditText self, int nowIndex)
	{
		int newIndex = nowIndex;
		try
		{
			if (Enabled())
				newIndex= Insert(self,nowIndex);
		}
		catch (IndexOutOfBoundsException e)
		{
			Log.e("Inserting Error", toString()+" "+e.toString());
			return -1;
		}
		return newIndex;
	}
	
	protected int Insert( EditText self, int nowIndex){
		return dothing_insert(self, nowIndex);
	}
}
