package com.who.Edit.EditBuilder.ListenerVistor;

/* 
  如果你是EditListenerInfo的拥有者，
  
  需要管理内部的EditListener，并共享EditListenerInfo 
  
*/
public abstract interface EditListenerInfoUser
{
	
	public abstract EditListenerInfo getInfo()

	public abstract void setInfo(EditListenerInfo Info)
	
	public abstract void trimListener()
	
	public abstract void clearListener()
	
}
