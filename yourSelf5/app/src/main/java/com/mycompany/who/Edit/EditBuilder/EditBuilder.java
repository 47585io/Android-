package com.mycompany.who.Edit.EditBuilder;

import com.mycompany.who.Edit.EditBuilder.ListenerVistor.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import com.mycompany.who.Edit.EditBuilder.WordsVistor.*;


/*
 知道interface里的函数加abstract有什么用吗？

 这是为了方便以后将interfac改成abstract class

 通过EditBuilder，使用指定的EditListener和Words快速构建一个Edit
 
 构建包含两大东西: 
 
   EditListener工厂，各种监听器，根据需要去配置EditListenerInfo
		
   Words包，装满了一些单词，根据需要去配置Words
   
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
