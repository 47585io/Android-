package com.mycompany.who.Edit.ListenerVistor.EditListener;

import com.mycompany.who.Edit.Share.*;
import java.util.*;
import java.util.concurrent.*;
import com.mycompany.who.Edit.Base.*;

public abstract class EditCompletorListener extends EditListener
{
	public abstract Collection<String> onBeforeSearchWord(Words Wordlib);
	
	public abstract void onFinishSearchWord(List<String> word,List<Icon> adpter);
	
	public static<T> void toArray(Collection<T> coll,T[] arr){
		if(coll!=null)
		    coll.toArray(arr);
	}
	public static<T> Collection<T> toColletion(T[] arr){
		List<T> coll = new ArrayList<>();
		if(arr!=null)
		    for(T s:arr)
		        coll.add(s);
		return coll;
	} 
}
