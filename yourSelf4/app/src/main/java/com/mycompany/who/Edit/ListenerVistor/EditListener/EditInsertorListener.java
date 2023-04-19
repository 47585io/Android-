package com.mycompany.who.Edit.ListenerVistor.EditListener;

import android.text.*;
import java.util.*;
import android.util.*;
import android.widget.*;

public abstract class EditInsertorListener extends EditListener
{
	
	abstract public int dothing_insert(EditText self, int nowIndex)
	
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
