package com.mycompany.who.Edit.Base.Share.Share1;

/*
 例如token，它的内存为:
 8字节  对象头    偏移量为0  (8的倍数)
 4字节  自己指针大小  偏移量为8 (4的倍数)
 4字节  start    偏移量为12 (4的倍数)
 4字节  end      偏移量为16 (4的倍数)
 4字节  src指针   偏移量为20 (4的倍数)
 
 (这样所有成员的地址间没有缝隙，而且刚好占满了24字节，3*8=24，因此也不需要再将整个对象的空间扩大到8的倍数了)
*/
public class token extends size
{
	public CharSequence src;
	
	public token(){}
	public token(int start,int end,CharSequence src){
		this.start=start;
		this.end=end;
		this.src=src;
	}
	public token(token o){
		start = o.start;
		end = o.end;
		src = o.src;
	}
	
	public void set(int s,int e,CharSequence src){
		this.start=s;
		this.end=e;
		this.src = src;
	}
	
}
