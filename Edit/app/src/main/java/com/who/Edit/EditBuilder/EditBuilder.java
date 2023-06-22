package com.who.Edit.EditBuilder;

import com.who.Edit.EditBuilder.ListenerVistor.*;
import com.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import com.who.Edit.EditBuilder.WordsVistor.*;


/*
 知道interface里的函数加abstract有什么用吗？

 这是为了方便以后将interfac改成abstract class

 通过EditBuilder，使用指定的EditListener和Words快速构建一个Edit
 
 构建包含两大东西: 
 
   EditListener工厂，各种监听器，根据需要去配置EditListenerInfo
		
   Words包，装满了一些单词，根据需要去配置Words
   
*/
public abstract interface EditBuilder
{

	public static abstract interface ListenerFactory
	{
		public abstract void SwitchListener(EditListenerInfo Info,String Lua)

		public abstract EditListener ToLisrener(String Lua)
	}

	public static abstract interface WordsPacket
	{
		public abstract void SwitchWords(Words Lib,String Lua)

		public abstract AWordsPacket UnPackWords(String Lua)

		public static abstract interface AWordsPacket
		{
			public abstract void loadWords(Words Lib)
		}
	}

	public abstract void SwitchLuagua(Object O,String Lua)

	public abstract void trimListener(EditListenerInfo Info)

	public abstract void loadWords(Words Lib)

}
