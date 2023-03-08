package com.mycompany.who.Edit.DrawerEdit.EditListener;

import com.mycompany.who.Edit.Share.*;
import java.util.*;
import java.util.concurrent.*;

public abstract class EditCompletorListener extends EditListener
{
	public abstract Collection<String> onBeforeSearchWord();
	
	public abstract void onFinishSearchWord(List<String> word,List<Icon> adpter);
	
	public static<T> void toArray(Collection<T> coll,T[] arr){
		coll.toArray(arr);
	}
	public static<T> Collection<T> toColletion(T[] arr){
		List<T> coll = new ArrayList<>();
		for(T s:arr)
		   coll.add(s);
		return coll;
	} 
}
