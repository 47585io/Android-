package com.mycompany.who.Edit.Base.Share.Share4;
import java.util.*;

public abstract class EPool<T>
{
	//简单的元素常量池
	//申明一下，这样做的原因是：
	//元素可以重复用，而不是创建之后马上销毁，太浪费了
	private List<T> Es;
	private int p;
	private int size;
	private int isStart;
	private int isDisbled;
	protected int MaxSize=50000;
	protected int onceCount=1000;
	
	public EPool(){
		Es=new ArrayList<>();
		init();
	}
	
	synchronized public T get(){
		//从池子中获取一个元素，如果池子元素不足，创建一些
		//如果超出最大的数量，直接创建一个返回
		if(p>MaxSize-1)
			return creat();
		if(p>Es.size()-1)
			put(onceCount);
		T E= Es.get(p++);
		resetE(E); //重置元素
		
		++size;
		//记录本次使用的size
		//若未start就使用get，只要调stop，size会累计至下次一起回收
		return E;
	}
	
	synchronized public void start(){		
	    //开始记录
		++isStart;
	}
	synchronized public void stop(){
		--isStart;
		if(isStart==0){
			//必须保证所有的任务都完成了，才收回nodes
		    recyle(size);    
		}
	}
	
	synchronized public void put(int size){
		//放入多少个
		int i;
		for(i=0;i<size;++i){
			Es.add(creat());
		}
	}
	
	synchronized public void recyle(int size){
		//指针向前偏移size
		p-=size;
		this.size-=size;
	}
	
	
	protected abstract T creat()
	
	protected abstract void resetE(T E)
	
	protected abstract void init()
	
	
	public int isStart(){
		return isStart;
	}
	public int size(){
		return p;
	}
	public int MaxSize(){
		return 50000;
	}
	public int onceCount(){
		return 1000;
	}
	synchronized public void isDisbled(boolean is){
		if(is){
			MaxSize = 0;
			++isDisbled;
			//MaxSize调整为0，以禁用get，使池子中元素不变，其它的值暂时保存以下次启用或回收
		}
		else{
			--isDisbled;
			if(isDisbled==0)
			    MaxSize = MaxSize();
			//所有人都不用了，则将MaxSize恢复
		}
	}
	
	@Override
	public String toString()
	{
		String src = "";
		src=getClass().getSimpleName();
		if(isStart==0)
		    src+=" isStop With "+isStart;
		else
			src+=" isStart With "+isStart;
			
		src+=", I hava "+Es.size();
		src+=" Element, Used "+p;
		src+=" Element, Will be released "+size;
		src+=" Element, Leaked "+(p-size);
		return src;
	}
	 
}
