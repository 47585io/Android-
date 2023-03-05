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
		
		ArrayList<Future<Boolean>> results = new ArrayList<>();
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
	
	public static<T> ArrayList<T> foreach(Collection<EditListener> lis,final RunLi<ArrayList<T>> Callback){
		if(lis==null)
			return null;
		ArrayList<T> r=new ArrayList<>();
		for(EditListener li:lis){
			try{
				r.addAll( Callback.runSelf(li));
			}catch(Exception e){}
		}
		return r;
	}
	
	public static<T> ArrayList<T> foreach(Collection<EditListener> lis,final RunLi<ArrayList<T>> Callback,ThreadPoolExecutor pool){
		if(lis==null)
			return null;
		if(pool==null){
			return foreach(lis,Callback);
		}

		ArrayList<Future<ArrayList<T>>> results = new ArrayList<>();
		for(final EditListener li:lis){
			Callable<ArrayList<T>> ca = new Callable<ArrayList<T>>(){

				@Override
				public ArrayList<T> call()
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
	
	public static<T> ArrayList<T> foreach(final RunLi<T> Callback,Collection<EditListener> lis,boolean is){
		if(lis==null)
			return null;
		ArrayList<T> r=new ArrayList<>();
		for(EditListener li:lis){
			try{
				r.add( Callback.runSelf(li));
			}catch(Exception e){}
		}
		return r;
	}
	public static<T> ArrayList<T> foreach(final RunLi<T> Callback,ThreadPoolExecutor pool,Collection<EditListener> lis){
		if(lis==null)
			return null;
		if(pool==null){
			return foreach(Callback,lis,false);
		}

		ArrayList<Future<T>> results = new ArrayList<>();
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
	
	public static void DelListener(ArrayList<EditListener> lis,String name){
		for(Object li:lis.toArray())
		    if(((EditListener)li).name.equals(name))
				lis.remove(li);
	}

	public static void DelListener(ArrayList<EditListener> lis,int hashCode){
		for(Object li:lis.toArray())
		    if(li.hashCode()==hashCode)
				lis.remove(li);
	}
}
