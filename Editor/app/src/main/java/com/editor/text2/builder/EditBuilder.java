package com.editor.text2.builder;
import com.editor.text2.builder.listenerInfo.*;
import com.editor.text2.builder.listenerInfo.listener.baselistener.*;
import com.editor.text2.builder.words.*;

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

