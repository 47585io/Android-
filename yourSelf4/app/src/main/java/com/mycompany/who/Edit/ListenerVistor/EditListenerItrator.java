package com.mycompany.who.Edit.ListenerVistor;

import com.mycompany.who.Edit.Base.Share.Share4.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import java.util.*;
import java.util.concurrent.*;


public class EditListenerItrator
{
	public static void foreach(RunLi Callback,Collection<EditListener> lis){
		if(lis==null)
			return;
		for(EditListener li:lis){
			try{
			Callback.runSelf(li);
			}catch(Exception e){}
		}
	}
	public static void foreach(final RunLi Callback,Collection<EditListener> lis,ThreadPoolExecutor pool){
		if(lis==null)
			return;
		if(pool==null){
			foreach(Callback,lis);
			return;
		}
		
		List<Future> results = new ArrayList<>();
		for(final EditListener li:lis){
			Runnable run = new Runnable(){

				@Override
				public void run()
				{
					Callback.runSelf(li);	
				}
			};
			results.add(pool.submit(run));
		}
		FuturePool.FuturePop(results);
	}
	
	public static<T> List<T> foreach(Collection<EditListener> lis,final RunLi<List<T>> Callback){
		if(lis==null)
			return null;
		List<T> r=new ArrayList<>();
		for(EditListener li:lis){
			try{
				r.addAll( Callback.runSelf(li));
			}catch(Exception e){}
		}
		return r;
	}
	public static<T> List<T> foreach(Collection<EditListener> lis,final RunLi<List<T>> Callback,ThreadPoolExecutor pool){
		if(lis==null)
			return null;
		if(pool==null){
			return foreach(lis,Callback);
		}

		List<Future<List<T>>> results = new ArrayList<>();
		for(final EditListener li:lis){
			Callable<List<T>> ca = new Callable<List<T>>(){

				@Override
				public List<T> call()
				{
					try{
						return Callback.runSelf(li);
					}catch(Exception e){}
					return null;
				}
			};
			results.add( pool.submit(ca));
		}
		return FuturePool.FutureGetAll(results);
	}
	
	public static<T> List<T> foreach(final RunLi<T> Callback,Collection<EditListener> lis,boolean is){
		if(lis==null)
			return null;
		List<T> r=new ArrayList<>();
		for(EditListener li:lis){
			try{
				r.add( Callback.runSelf(li));
			}catch(Exception e){}
		}
		return r;
	}
	public static<T> List<T> foreach(final RunLi<T> Callback,ThreadPoolExecutor pool,Collection<EditListener> lis){
		if(lis==null)
			return null;
		if(pool==null){
			return foreach(Callback,lis,false);
		}

		List<Future<T>> results = new ArrayList<>();
		for(final EditListener li:lis){
			Callable<T> ca = new Callable<T>(){

				@Override
				public T call()
				{
					try{
						return Callback.runSelf(li);
					}catch(Exception e){}
					return null;
				}
			};
			results.add( pool.submit(ca));
		}
		return FuturePool.FutureGet(results);
	}
	
	
	public static abstract class RunLi<T>{
		abstract public T run(EditListener li);
		public T runSelf(EditListener li){
			if(li.Enabled())
				return run(li);
			return null;
		}
	}
	
}
