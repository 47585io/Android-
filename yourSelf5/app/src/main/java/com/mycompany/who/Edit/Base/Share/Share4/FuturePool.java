package com.mycompany.who.Edit.Base.Share.Share4;
import java.util.*;
import java.util.concurrent.*;


/*
  便捷地添加任务，完成任务
*/
public class FuturePool
{
	public static<T> List<T> FutureGet(Collection<Future<T>> results)
	{
		List<T> date = new LinkedList<>();
		try
		{
			for (Future<T> result:results){
			    if(result!=null)
				    date.add(result.get());
				else
					date.add(null);
			}
		}
		catch (ExecutionException e)
		{}
		catch (InterruptedException e)
		{}
		return date;
	}
	
	public static<T> List<T> FutureGetAll(Collection<Future<List< T>>> results)
	{
		List<T> date = new LinkedList<>();
		try
		{
			for (Future<List< T>> result:results)
			    if(date!=null)
				    date.addAll(result.get());
		}
		catch (ExecutionException e)
		{}
		catch (InterruptedException e)
		{}
		return date;
	}
	
	public static void FuturePop(Collection<Future> results)
	{
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
	
	/* 
	   函数重载时，要保证主类型不同，而不是元素类型
	
	   虽然Collection<Runnable>和Collection<Callable> totals可以编译通过，但运行时会产生异常 
	*/
	public static List<Future> addTotals(Collection<Runnable> totals,ThreadPoolExecutor pool)
	{
		List<Future> results= new LinkedList<>();
		for(Runnable total:totals)
		    if(total!=null)
		        results.add(pool.submit(total));
		return results;
	}
	public static List<Future> addTotalS(Collection<Callable> totals,ThreadPoolExecutor pool)
	{
		List<Future> results= new LinkedList<>();
		for(Callable total:totals)
		    if(total!=null)
		        results.add(pool.submit(total));
			else
				results.add(null);
		return results;
	}
	
}
