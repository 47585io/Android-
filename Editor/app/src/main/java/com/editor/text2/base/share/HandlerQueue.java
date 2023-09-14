package com.editor.text2.base.share;
import android.os.*;
import android.util.*;

public class HandlerQueue
{
	
	public static void doTotals(Runnable[] totals,Handler handler)
	{
		if(totals==null || totals.length==0){
			return;
		}
		if(handler==null){
			handler = new Handler();
			Log.e("Handler is null","");
		}
		doTotal(0,totals,handler);
	}

	/* 递归进行post，如果单个任务需要进行长时间前台操作必须使用 */
	private static void doTotal(final int index,final Runnable[] totals,final Handler handler)
	{
		if(index>=totals.length){
			return;
		}
		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
				totals[index].run();
				doTotal(index+1,totals,handler);
				//执行完后再调用Recursion去post下个index的任务
				//这样每执行完一个任务，主线程都可以先顺着执行下去，缓口气，接下来继续执行下个任务
			}
		};
		handler.postDelayed(run,500);
		//递归地抛并执行任务
	}

}
