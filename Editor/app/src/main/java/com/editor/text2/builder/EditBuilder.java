package com.editor.text2.builder;
import com.editor.text2.builder.listener.baselistener.*;
import com.editor.text2.builder.words.*;

public abstract interface EditBuilder
{

	public static abstract interface ListenerFactory
	{
		public abstract void SwitchListener(EditListenerInfo Info,String Lua)
	}

	public static abstract interface WordsPacket
	{
		public abstract void SwitchWords(Words Lib,String Lua)

		public static abstract interface AWordsPacket
		{
			public abstract void loadWords(Words Lib)
		}
	}

	public abstract void trimListener(EditListenerInfo Info, String Lua)

	public abstract void loadWords(Words Lib, String Lua)
	
}
