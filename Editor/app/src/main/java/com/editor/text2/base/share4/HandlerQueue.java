package com.editor.text2.base.share4;
import android.os.*;
import android.util.*;

public class HandlerQueue
{
	
	private static final Handler mHamdler = new Handler();
	
	public static HandlerLock doTotals(Runnable[] totals, Handler handler, long delayMillis)
	{
		if(totals==null || totals.length==0){
			return null;
		}
		if(handler==null){
			handler = mHamdler;
			Log.e("Handler is null","");
		}
		HandlerLock locker = new HandlerLock();
		doTotal(0,totals,handler,locker,delayMillis);
		return locker;
	}

	/* 递归进行post，如果任务很多且需要进行长时间前台操作必须使用 */
	private static void doTotal(final int index,final Runnable[] totals,final Handler handler,final HandlerLock locker,final long delayMillis)
	{
		if(index>=totals.length || locker.lock){
			//任务抛出前，判断是否可以抛出
			return;
		}
		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
				if(locker.lock){
					//在任务抛出到执行的延迟中，是否设置了lock
					return;
				}
				totals[index].run();
				if(!locker.lock){
					//任务执行中是否设置了lock
					doTotal(index+1,totals,handler,locker,delayMillis);
					//执行完后再调用Recursion去post下个index的任务
					//这样每执行完一个任务，主线程都可以先顺着执行下去，缓口气，接下来继续执行下个任务
				}	
			}
		};
		handler.postDelayed(run,delayMillis);
		//递归地抛并执行任务
	}

	public static class HandlerLock
	{
		private boolean lock;
		
		public void lockHandler(){
			lock = true;
		}
	}
	
}
