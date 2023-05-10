package com.mycompany.who.Edit.Base.Share.Share4;

/*
 将int类型的数据作为指针传递

 注意，若以指针传递，小心误修改对象，少使用set
*/
final public class Int
{

	public int date;

	public Int(){
		date = 0;
	}
	public Int(int d){
		date = d;
	}

	public int get(){
		return date;
	}
	public void set(int d){
		date = d;
	}
	
	public int add()
	{
		int before = date;
		++date;
		return before;
	}
	public int less()
	{
		int before = date;
		--date;
		return before;
	}
	
}
