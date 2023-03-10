package com.mycompany.who.Edit.DrawerEdit.Base;

import java.util.*;
import com.mycompany.who.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;

public class OtherWords extends Words
{

	public static final int words_func = 0;
	public static final int words_vill = 1;
	public static final int words_obj = 2;
	public static final int words_type = 3;
	public static final int words_tag = 4;
	public static final int words_attr = 5;
	
	public static final int words_key=6;
	public static final int words_const=7;
	
	public List<Collection<String>> mdates;
	
	public OtherWords(int size){
		super();
		mdates=new ArrayList<>();
		add(size);
		init(size);
	}
	
	public void init(int size){
		add( EditCompletorListener.toColletion( keyword));
		add( EditCompletorListener.toColletion(constword));
		mdates.get(words_tag).addAll(EditCompletorListener.toColletion(IknowTag));
		IknowTag=null;
		keyword=null;
		constword=null;
		//单词转移
	}
	
	public void add(int size){
		while(size-->0){
			Collection<String> col=Collections.synchronizedSet(new TreeSet<String>());
			mdates.add(col);
			//每个集合都是安全的
		}
	}
	public void add(Collection<String> words){
		Collection<String> col=Collections.synchronizedCollection(words);
		mdates.add(col);
	}
	
	public void clear(){
		for(Collection t:mdates)
		    t.clear();
	}
}
