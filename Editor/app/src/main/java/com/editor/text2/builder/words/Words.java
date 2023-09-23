package com.editor.text2.builder.words;
import java.util.*;

public abstract interface Words 
{

	public static final int maps_zhu = 0;

	public static final int chars_fuhao = 0;

	public static final int chars_spilt = 1;

	public static final int words_key=0;
	
	public static final int words_const = 1;
	
	public static final int words_func = 2;

	public static final int words_variable = 3;

	public static final int words_type = 4;

	public static final int words_tag = 5;

	public static final int words_attr = 6;


	public abstract void clear()

	public abstract int size()

	public abstract boolean contrans(int index)

	public abstract Collection<Character> getACollectionChars(int index)

	public abstract Collection<CharSequence> getACollectionWords(int index)

	public abstract Map<CharSequence,CharSequence> getAMapWords(int index)

	public abstract void setACollectionChars(int index,Collection<Character> words)

	public abstract void setACollectionWords(int index,Collection<CharSequence> words)

	public abstract void setAMapWords(int index,Map<CharSequence,CharSequence> words)

}
