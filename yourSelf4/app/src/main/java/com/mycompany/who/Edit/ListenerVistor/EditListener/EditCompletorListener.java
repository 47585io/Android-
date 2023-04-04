package com.mycompany.who.Edit.ListenerVistor.EditListener;

import android.util.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Share.Share2.*;
import java.util.*;
import android.widget.*;

public abstract class EditCompletorListener extends EditListener
{
	public abstract Collection<CharSequence> onBeforeSearchWord(Words Wordlib,EditText self);
	
	public abstract void onFinishSearchWord(List<CharSequence> word,List<Icon> adpter,EditText self);
	
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
	
	final public List<Icon> LetMeCompelet(CharSequence wantBefore,CharSequence wantAfter,int before,int after,Words Wordlib,EditText self)
	{
		List<Icon> Adapter = null;
		if(!Enabled())
			return Adapter;
		try{
		    Adapter=Compelet(wantBefore,wantAfter,before,after,Wordlib,self);
		}catch(Exception e){
			Log.e("Completing Error", toString()+" "+e.toString());
		}
		return Adapter;
	}
	
	protected List<Icon> Compelet(CharSequence wantBefore,CharSequence wantAfter,int before,int after,Words Wordlib,EditText self){
		
		Collection<CharSequence> lib;
		List<CharSequence> words = null;
		List<Icon> Adapter=new ArrayList<>();
			
		lib = onBeforeSearchWord(Wordlib,self);
		if (lib != null && lib.size() != 0)
		{
			words =CodeEdit. SearchOnce(wantBefore, wantAfter, lib, before, after);
		}
		onFinishSearchWord(words, Adapter,self);
		
		return Adapter;
	}
	
}
