package com.mycompany.who.Edit.DrawerEdit.EditListener;

import com.mycompany.who.Edit.Share.*;
import java.util.*;
import java.util.concurrent.*;

public abstract class EditCompletorListener extends EditListener
{
	public abstract Collection<String> onBeforeSearchWord();
	public abstract void onFinishSearchWord(ArrayList<String> word,ArrayList<Icon> adpter);
	
	public static String[] toArray(Collection<String> coll){
		String[] arr = new String[coll.size()];
		coll.toArray(arr);
		return arr;
	}
	public static Collection<String> toColletion(String[] arr){
		ArrayList<String> coll = new ArrayList<>();
		for(String s:arr)
		   coll.add(s);
		return coll;
	} 
}
