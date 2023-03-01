package com.mycompany.who.Edit.DrawerEdit.Base;
import java.util.*;
import java.util.concurrent.*;

public class FuturePool
{
	public static<T> ArrayList<T> FutureGet(Collection<Future<T>> results){
		ArrayList<T> date = new ArrayList<>();
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
	
	public static<T> ArrayList<T> FutureGetAll(Collection<Future<ArrayList< T>>> results){
		ArrayList<T> date = new ArrayList<>();
		try
		{
			for (Future<ArrayList< T>> result:results)
				date.addAll(result.get());
		}
		catch (ExecutionException e)
		{}
		catch (InterruptedException e)
		{}
		return date;
	}
	
	public static<T> ArrayList<Future<T>> addTotals(Collection<Callable<T>> totals,ThreadPoolExecutor pool){
		ArrayList<Future<T>> results= new ArrayList<>();
		for(Callable<T> total:totals)
		    results.add(pool.submit(total));
		return results;
	}
}
