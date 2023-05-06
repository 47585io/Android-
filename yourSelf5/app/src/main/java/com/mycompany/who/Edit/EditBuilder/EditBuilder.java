package com.mycompany.who.Edit.EditBuilder;

import com.mycompany.who.Edit.EditBuilder.ListenerVistor.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import com.mycompany.who.Edit.EditBuilder.WordsVistor.*;


/*
 知道interface里的函数加abstract有什么用吗？

 这是为了方便以后将interfac改成abstract class

*/
public interface EditBuilder
{

	public static interface ListenerFactory
	{
		abstract public void SwitchListener(EditListenerInfo Info,String Lua)

		abstract public EditListener ToLisrener(String Lua)
	}

	public static interface WordsPacket
	{
		abstract public void SwitchWords(Words Lib,String Lua)

		abstract public AWordsPacket UnPackWords(String Lua)

		public static interface AWordsPacket
		{
			abstract public void loadWords(Words Lib)
		}
	}

	abstract public void SwitchLuagua(Object O,String Lua)

	abstract public void trimListener(EditListenerInfo Info)

	abstract public void loadWords(Words Lib)

}
