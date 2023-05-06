package com.mycompany.who.Edit.EditBuilder.ListenerVistor;

import com.mycompany.who.Edit.Base.Share.Share4.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.*;
import java.util.*;
import java.util.concurrent.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;


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
	public static void foreach(RunLi Callback,EditListener lis)
	{
		if(lis==null)
			return;
		if(lis instanceof EditListener){
			Callback.runSelf(lis);
		}
		else if(lis instanceof EditListenerList){
			foreach(Callback,((EditListenerList)lis).getList());
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
	public static<T> List<T> foreach(EditListener lis, RunLi<List<T>> Callback)
	{
		if(lis==null)
			return null;
		List<T> r = null;
		if(!(lis instanceof EditListenerList))
			r = Callback.runSelf(lis);
		else if(lis instanceof EditListenerList)
			r = foreach(((EditListenerList)lis).getList(),Callback);
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
	
	public static<T> List<T> foreach(final RunLi<T> Callback,EditListener lis,List<T> r)
	{
		if(lis==null)
			return null;
		if(!(lis instanceof EditListenerList))
			r.add(Callback.runSelf(lis));
		else if(lis instanceof EditListenerList){
			foreach(Callback,((EditListenerList)lis).getList(),r);
		}
		return r;
	}

	public static abstract class RunLi<T>
	{
		abstract protected T run(EditListener li);
		
		final public T runSelf(EditListener li)
		{
			if(li.Enabled())
				return run(li);
			return null;
		}
	}
	
}
