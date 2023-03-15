package com.mycompany.who.Edit.Share;
import java.util.*;

public abstract class EPool<T>
{
	//简单的元素常量池
	//申明一下，这样做的原因是：
	//元素可以重复用，而不是创建之后马上销毁，太浪费了
	protected List<T> Es;
	protected int p;
	private int size;
	private int isStart;
	public int MaxSize=50000;
	public int onceCount=100;
	
	public EPool(){
		Es=new ArrayList<>();
		p=0;
		put(onceCount);
	}
	synchronized public T get(){
		//从池子中获取一个元素，如果池子元素不足，创建一些
		//如果超出最大的数量，直接创建
		if(p>MaxSize-1)
			return creat();
		if(p>Es.size()-1)
			put(onceCount);
		T E= Es.get(p++);
		if(isStart!=0)
			++size;
			//记录本次使用的size
		return E;
	}
	
	synchronized public void start(){		
	    //开始记录
		isStart++;
	}
	synchronized public void stop(){
		isStart--;
		if(isStart==0){
			//必须保证所有的任务都完成了，才收回nodes
		    recyle(size);
		    size=0;
		}
		//stop后，指针向前移size，size清0
	}
	
	synchronized public void put(int size){
		//放入多少个
		int i;
		for(i=0;i<size;++i){
			Es.add(creat());
		}
	}
	public abstract T creat()
	public abstract void resetE(T E)
	
	synchronized public void recyle(int size){
		//指针向前偏size，并将之间的元素重置
		p-=size;
		int i;
		for(i=p;i<p+size;++i)
		    resetE(Es.get(i));
	}
}
