package com.mycompany.who.Edit.DrawerEdit.Base;
import java.util.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;
import java.util.concurrent.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;

public abstract class EditListenerItrator
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
		
		List<Future<Boolean>> results = new ArrayList<>();
		for(final EditListener li:lis){
			Callable<Boolean> ca = new Callable<Boolean>(){

				@Override
				public Boolean call()
				{
					try{
					Callback.runSelf(li);
					}catch(Exception e){}
					return null;
				}
			};
			results.add( pool.submit(ca));
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
			if(EditListener.Enabled(li))
				return run(li);
			return null;
		}
	}
	
	public static void DelListener(List<EditListener> lis,String name){
		for(Object li:lis.toArray())
		    if(((EditListener)li).name.equals(name))
				lis.remove(li);
	}
	public static void DelListener(List<EditListener> lis,EditListener l){
		for(Object li:lis.toArray())
		    if(li.equals(l))
				lis.remove(li);
	}
	public static void DelListener(List<EditListener> lis,int hashCode){
		for(Object li:lis.toArray())
		    if(li.hashCode()==hashCode)
				lis.remove(li);
	}
	
	
}
