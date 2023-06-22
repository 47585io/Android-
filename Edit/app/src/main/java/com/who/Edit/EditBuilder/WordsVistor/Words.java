package com.who.Edit.EditBuilder.WordsVistor;

import java.util.*;


/* 
  Words是一个单词库，用于存储和交换单词
*/
public abstract interface Words 
{
	
	public static final int maps_zhu = 0;
	
	public static final int chars_fuhao = 0;
	
	public static final int chars_spilt = 1;
	
	public static final int words_key=0;
	
	public static final int words_const=1;
	
	public static final int words_func = 2;
	
	public static final int words_vill = 3;
	
	public static final int words_obj = 4;
	
	public static final int words_type = 5;
	
	public static final int words_tag = 6;
	
	public static final int words_attr = 7;
	
	
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


