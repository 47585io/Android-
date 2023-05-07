package com.mycompany.who.Edit.EditBuilder.ListenerVistor;

import com.mycompany.who.Edit.Base.Share.Share4.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.*;
import java.util.*;
import java.util.concurrent.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;


public class EditListenerItrator
{
	
	public static void foreachOne(RunLi Callback,EditListener li)
	{
		if(li==null || Callback==null)
			return;
		try{
			Callback.run(li);
		}catch(Exception e){}
	}
	
	public static void foreachSome(RunLi Callback,EditListenerList lis)
	{
		if(lis==null)
			return;
		for(EditListener li:lis.getList()){
			foreachOne(Callback,li);
		}
		foreachOne(Callback,lis);
	}
	
	public static void foreachCheck(EditListener lis,RunLi Callback)
	{
		if(lis==null)
			return;
			//先判断它是不是孑类，再判断是不是父类
		else if(lis instanceof EditListenerList){
			foreachSome(Callback,((EditListenerList)lis));
		}
		else if(lis instanceof EditListener){
			foreachOne(Callback,lis);
		}
	}
	
	/* 带回EditListener的返回值 */
	public static boolean foreachOne(RunLiCut Callback,EditListener li)
	{
		boolean is = false;
		if(li==null || Callback==null)
			return is;
		try{
			is = Callback.run(li);
		}
		catch(Exception e){}
		return is;
	}

	/* 当有EditListener返回true，直接返回true */
	public static boolean foreachSome(RunLiCut Callback,EditListenerList lis)
	{
		if(lis==null)
			return false;
		for(EditListener li:lis.getList()){
			if(foreachOne(Callback,li))
				return true;
		}
		return foreachOne(Callback,lis);
	}

	public static boolean foreachCheck(EditListener lis,RunLiCut Callback)
	{
		boolean is = false;
		if(lis==null)
			return is;
		//先判断它是不是孑类，再判断是不是父类
		else if(lis instanceof EditListenerList){
			is = foreachSome(Callback,((EditListenerList)lis));
		}
		else if(lis instanceof EditListener){
			is = foreachOne(Callback,lis);
		}
		return is;
	}
	
	public static interface RunLi
	{
		public void run(EditListener li);
	}
	
	public static interface RunLiCut
	{
		public boolean run(EditListener li)
	}
	
}
