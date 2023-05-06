package com.mycompany.who.Edit.Base;

import java.util.*;
import com.mycompany.who.Edit.Base.Share.Share3.*;

public interface Words 
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
	
	
	public void clear()

	public int size()

	public Collection<Character> getACollectionChars(int index)
	
	public Collection<CharSequence> getACollectionWords(int index)
	
	public Map<CharSequence,CharSequence> getAMapWords(int index)
	
	public void setACollectionChars(int index,Collection<Character> words)
	
	public void setACollectionWords(int index,Collection<CharSequence> words)
	
	public void setAMapWords(int index,Map<CharSequence,CharSequence> words)
	
}


