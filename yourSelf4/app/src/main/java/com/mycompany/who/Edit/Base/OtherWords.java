package com.mycompany.who.Edit.Base;

import java.util.*;
import com.mycompany.who.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;


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

	public List<Collection<CharSequence>> mdates;
	
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
			Collection<CharSequence> col=Collections.synchronizedSet(new TreeSet<>());
			mdates.add(col);
			//每个集合都是安全的
		}
	}
	public void add(Collection<CharSequence> words){
		Collection<CharSequence> col=Collections.synchronizedCollection(words);
		mdates.add(col);
	}
	
	public void clear(){
		for(Collection t:mdates)
		    t.clear();
	}
	
	
	public Collection<CharSequence> getKeyword(){
		return mdates.get(words_key);
	}
	public Collection<CharSequence> getConstword(){
		return mdates.get(words_const);
	}
	public char[] getFuhao(){
		return fuhao;
	}
	public char[] getSpilt(){
		return spilt;
	}
	public Map<CharSequence,CharSequence> get_zhu(){
		return zhu_key_value;
	}
	public Collection<CharSequence> getLastfunc(){
		return mdates.get(words_func);
	}
	public Collection<CharSequence> getHistoryVillber(){
		return mdates.get(words_vill);
	}
	public Collection<CharSequence> getThoseObject(){
		return mdates.get(words_obj);
	}
	public Collection<CharSequence> getBeforetype(){
		return mdates.get(words_type);
	}
	public Collection<CharSequence> getTag(){
		return mdates.get(words_tag);
	}
	public Collection<CharSequence> getAttribute(){
		return mdates.get(words_attr);
	}
	
	
}
