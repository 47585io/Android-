package com.mycompany.who.Edit.Share.Share4;

import java.lang.reflect.*;

public class Getter
{
	public static Object getObject(String type,Object...args){
		//用指定的type和参数args创建一个对象
		Class clazz=null;
		Class[] types=new Class[args.length];
		int i;
		for(i=0;i<types.length;i++){
			types[i] = args[i].getClass();
		}
		try
		{
			clazz= Class.forName(type);
			for(Constructor consr: clazz.getConstructors()){
				Class[] src = consr.getParameterTypes();
				try{
			    	if(Args(types,src))
					    return consr.newInstance(args);
				}catch(Exception e){}
			}
		}
		catch (ClassNotFoundException e)
		{}
		return null;
	}

	public static<T> Object getField(String name,T vector){
		//获得vector中名为name的成员
		try
		{
			Field f= vector.getClass().getField(name);
			f.setAccessible(true);
			try
			{
				return f.get(vector);
			}
			catch (IllegalAccessException e)
			{}
			catch (IllegalArgumentException e)
			{}
		}
		catch (NoSuchFieldException e)
		{}
		return null;
	}
	public static<T> void setFiled(String name,T vector,T n){
		try
		{
			Field f= vector.getClass().getField(name);
			f.setAccessible(true);
			try
			{
				f.set(vector,n);
			}
			catch (IllegalAccessException e)
			{}
			catch (IllegalArgumentException e)
			{}
		}
		catch (NoSuchFieldException e)
		{}
	}
	public static<T> Method getFunc(String name,T block,Object...args){
		//得到block中名为name且参数列表为args的方法
		Class[] types=new Class[args.length];
		int i;
		for(i=0;i<types.length;i++){
			types[i] = args[i].getClass();
		}
		//得到参数类型

		for(Method func:block.getClass().getMethods()){
			try{
				if(func.getName().equals(name)){
					Class[] src= func.getParameterTypes();
					if(Args(types,src))
						return func;
				}
				//匹配正确的重载函数
			}catch(Exception e){}
		}
		return null;
	}

	public static boolean Args(Class...dst,Class...src) throws NoSuchMethodException, SecurityException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException{
		//类型列表dst中的类型是否src中的类型，或父类，接口的类型

		int i=0;
		if(dst.length!=src.length)
			return false;
		if(src.length==0)
			return true;
		for(i=0;i<src.length;i++){
			Class d = dst[i];
			Class s = src[i];
			if(!(Arg(d,s)||Arg(s,d)))
				return false;
		}
		return true;
	}
	public static boolean Arg(Class d,Class s){
		//类型src的类型是否为dst的类型，或父类，接口的类型

		if(d.getTypeName().equals(s.getTypeName()))
			return true;
		else if(ArgUP(d,s))
			return true;

		return false;
	}
	public static boolean ArgUP(Class d,Class s){
		//向上遍历父类
		if(d.getSimpleName().equals("Object"))
		//已经遍历到了Object
			return false;
		if(d.getSuperclass().getTypeName().equals(s.getTypeName())){
			//如果父类类型与s相同，返回true
			return true;
		}
		else{
			Class[] in= d.getInterfaces();
			for(Class n:in){
				if(n.getTypeName().equals(s.getTypeName()))
					return true;
				//如果接口类型与s相同，返回true
			}
		}

		return ArgUP(d.getSuperclass(),s);
		//不与上面任意类型相同，继续向上遍历，并将之后的函数返回值作为最终返回值
	}
}
