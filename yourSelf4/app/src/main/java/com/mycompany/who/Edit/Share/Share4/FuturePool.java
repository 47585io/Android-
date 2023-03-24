package com.mycompany.who.Edit.Share.Share4;
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
	
	public static<T> void FuturePop(Collection<Future<T>> results){
		try
		{
			for (Future<T> result:results)
				result.get();
		}
		catch (ExecutionException e)
		{}
		catch (InterruptedException e)
		{}
	}
	
	public static<T> List<Future<T>> addTotals(Collection<Callable<T>> totals,ThreadPoolExecutor pool){
		List<Future<T>> results= new ArrayList<>();
		for(Callable<T> total:totals)
		    results.add(pool.submit(total));
		return results;
	}
}
