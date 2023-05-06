package com.mycompany.who.Edit.EditBuilder.ListenerVistor;

/* 
  如果你是EditListenerInfo的拥有者，
  
  需要管理内部的EditListener，并共享EditListenerInfo 
*/
public interface EditListenerInfoUser
{
	public EditListenerInfo getInfo()

	public void setInfo(EditListenerInfo Info)
	
	public void trimListener()
	
	public void clearListener()
}
