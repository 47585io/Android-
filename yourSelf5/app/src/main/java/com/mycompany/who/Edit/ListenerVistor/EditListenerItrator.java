package com.mycompany.who.Edit.ListenerVistor;

import com.mycompany.who.Edit.Base.Share.Share4.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import java.util.*;
import java.util.concurrent.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.BaseEditListener.*;


public class EditListenerItrator
{
	
	public static void foreach(RunLi Callback,Collection<EditListener> lis)
	{
		if(lis==null)
			return;
		for(EditListener li:lis){
			try{
			    Callback.runSelf(li);
			}catch(Exception e){}
		}
	}
	public static void foreach(RunLi Callback,EditListener... lis)
	{
		if(lis==null)
			return;
		for(EditListener li:lis){
			try{
			    Callback.runSelf(li);
			}catch(Exception e){}
		}
	}

	public static<T> List<T> foreach(Collection<EditListener> lis, RunLi<List<T>> Callback)
	{
		if(lis==null)
			return null;
		List<T> r=new LinkedList<>();
		for(EditListener li:lis){
			try{
				r.addAll( Callback.runSelf(li));
			}catch(Exception e){}
		}
		return r;
	}
	
	public static<T> List<T> foreach(EditListener... lis,final RunLi<List<T>> Callback)
	{
		if(lis==null)
			return null;
		List<T> r=new LinkedList<>();
		for(EditListener li:lis){
			try{
				r.addAll( Callback.runSelf(li));
			}catch(Exception e){}
		}
		return r;
	}
	

	public static<T> List<T> foreach(final RunLi<T> Callback,Collection<EditListener> lis,List<T> r)
	{
		if(lis==null)
			return null;
		for(EditListener li:lis){
			try{
				r.add(Callback.runSelf(li));
			}catch(Exception e){}
		}
		return r;
	}
	
	public static<T> List<T> foreach(final RunLi<T> Callback,EditListener... lis,List<T> r)
	{
		if(lis==null)
			return null;
		for(EditListener li:lis){
			try{
				r.add(Callback.runSelf(li));
			}catch(Exception e){}
		}
		return r;
	}

	public static abstract class RunLi<T>
	{
		abstract public T run(EditListener li);
		public T runSelf(EditListener li){
			if(li.Enabled())
				return run(li);
			return null;
		}
	}
	
}
