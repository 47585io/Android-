package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

import java.util.*;

/*
  EditListenerList，EditListener的子接口，可向上转型
  
  内部管理一组的EditListener
*/
public interface EditListenerList extends EditListener
{
	public void setList(EditListener li)
	
	public void setList(List<EditListener> lis)
		
	public List<EditListener> getList()
}
