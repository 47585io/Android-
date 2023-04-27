package com.mycompany.who.Edit.ListenerVistor.EditListener;

import android.text.*;
import java.util.*;
import android.util.*;
import android.widget.*;


/* 
  插入字符
  
  editor表示编辑器的文本容器，nowIndex表示光标位置
  
  值得注意，返回的index可以调整编辑器的光标位置
  
*/
public abstract class EditInsertorListener extends EditListener
{
	
	abstract protected int dothing_insert(Editable editor, int nowIndex)
	//在这里检查nowIndex下标处的字符，并决定如何插入后续字符
	
	final public int LetMeInsert(Editable editor, int nowIndex)
	{
		int newIndex = nowIndex;
		try{
			if (Enabled())
				newIndex= Insert(editor,nowIndex);
		}
		catch (IndexOutOfBoundsException e){
			Log.e("Inserting Error", toString()+" "+e.toString());
		}
		return newIndex;
	}
	
	protected int Insert(Editable editor, int nowIndex){
		return dothing_insert(editor, nowIndex);
	}
	
}
