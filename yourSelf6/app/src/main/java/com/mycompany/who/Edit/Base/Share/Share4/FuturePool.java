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
		List<T> date = new ArrayList<>();
		try
		{
			for (Future<T> result:results)
			{
			    if (result != null)
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
		List<T> date = new ArrayList<>();
		try
		{
			for (Future<List< T>> result:results)
			    if (date != null)
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
		int size = results.size();
		while (size != 0)
		{
			for (Future result:results)
			{
				if (result.isCancelled() || result.isDone()){
					--size;
				}
			}
			try{
				Thread.sleep(50);
			}
			catch (InterruptedException e){}
		}
	}

	/* 
	 函数重载时，要保证主类型不同，而不是元素类型

	 虽然Collection<Runnable>和Collection<Callable>可以编译通过，但运行时会产生异常 

	*/
	public static List<Future> addTotals(Collection<Runnable> totals, ThreadPoolExecutor pool)
	{
		List<Future> results= new ArrayList<>();
		for (Runnable total:totals)
		    if (total != null)
		        results.add(pool.submit(total));
		return results;
	}
	
	public static List<Future> addTotals(Collection<Runnable> totals, ThreadPoolExecutor pool, int onceCount)
	{
		List<Future> results= new ArrayList<>();
		Runnable[] tmp = new Runnable[onceCount];
		int i = 0;
		Runnable r;
		Future f;
		for (Runnable total:totals){
			tmp[i++]=total;
			if(i >= onceCount){
				i = 0;
				r = AndRunnable(tmp);
				f = pool.submit(r);
				results.add(f);
			}
		}
		r = AndRunnable(tmp);
		f = pool.submit(r);
		results.add(f);
		return results;
	}
	
	public static List<Future> addTotalS(Collection<Callable> totals, ThreadPoolExecutor pool)
	{
		List<Future> results= new ArrayList<>();
		for (Callable total:totals)
		    if (total != null)
		        results.add(pool.submit(total));
			else
				results.add(null);
		return results;
	}

	public static Runnable AndRunnable(final Runnable... run)
	{
		return new Runnable(){

			@Override
			public void run()
			{
				for (Runnable r:run)
				{
					r.run();
				}
			}
		};
	}
	
	
	/* 使用Runnabler，便无需担心参数值无法final */
	public static class Runnabler implements Runnable
	{
		private Object[] args;
		
		public Runnabler(Object... args){
			this.args = args;
		}
		
		public Object[] getArags(){
			return args;
		}
		
		@Override
		public void run(){}
	}

}
