package com.editor.text2.builder;
import com.editor.text2.builder.listenerInfo.*;
import com.editor.text2.builder.words.*;

public interface EditBuilderUser extends EditListenerInfoUser,WordsUser
{
	public abstract EditBuilder getEditBuilder()

	public abstract void setLuagua(String Lua)

	public abstract String getLuagua()
}
