package com.mycompany.who.Edit.ListenerVistor.EditListener;

import com.mycompany.who.Edit.Share.*;
import java.util.*;
import java.util.concurrent.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.*;
import android.util.*;

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
	
	final public List<Icon> LetMeCompelet(String wantBefore,String wantAfter,int before,int after,Words Wordlib)
	{
		List<Icon> Adapter = null;
		if(!Enabled())
			return Adapter;
		try{
		    Adapter=Compelet(wantBefore,wantAfter,before,after,Wordlib);
		}catch(Exception e){
			Log.e("Completing Error", toString()+" "+e.toString());
		}
		return Adapter;
	}
	
	protected List<Icon> Compelet(String wantBefore,String wantAfter,int before,int after,Words Wordlib){
		
		Collection<String> lib;
		List<String> words = null;
		List<Icon> Adapter=new ArrayList<>();
			
		lib = onBeforeSearchWord(Wordlib);
		if (lib != null && lib.size() != 0)
		{
			words =CodeEdit. SearchOnce(wantBefore, wantAfter, lib, before, after);
		}
		onFinishSearchWord(words, Adapter);
		
		return Adapter;
	}
	
}
