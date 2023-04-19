package com.mycompany.who.Edit.Base.Share.Share4;
import java.util.*;
import java.util.concurrent.*;

public class FuturePool
{
	public static<T> List<T> FutureGet(Collection<Future<T>> results){
		List<T> date = new ArrayList<>();
		try
		{
			for (Future<T> result:results)
				date.add(result.get());
		}
		catch (ExecutionException e)
		{}
		catch (InterruptedException e)
		{}
		return date;
	}
	
	public static<T> List<T> FutureGetAll(Collection<Future<List< T>>> results){
		List<T> date = new ArrayList<>();
		try
		{
			for (Future<List< T>> result:results)
				date.addAll(result.get());
		}
		catch (ExecutionException e)
		{}
		catch (InterruptedException e)
		{}
		return date;
	}
	
	public static void FuturePop(Collection<Future> results){
		try
		{
			for (Future result:results)
				result.get();
		}
		catch (ExecutionException e)
		{}
		catch (InterruptedException e)
		{}
	}
	
	
	public static List<Future> addTotals(Collection<Runnable> totals,ThreadPoolExecutor pool){
		List<Future> results= new ArrayList<>();
		for(Runnable total:totals)
		    results.add(pool.submit(total));
		return results;
	}
	public static List<Future> addTotalS(Collection<Callable> totals,ThreadPoolExecutor pool){
		List<Future> results= new ArrayList<>();
		for(Callable total:totals)
		    results.add(pool.submit(total));
		return results;
	}
}
