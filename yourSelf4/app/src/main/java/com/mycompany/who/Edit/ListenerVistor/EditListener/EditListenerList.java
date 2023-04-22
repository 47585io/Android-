package com.mycompany.who.Edit.ListenerVistor.EditListener;
import java.util.*;

public class EditListenerList extends EditListener
{
	
	protected List<EditListener> lis;

	public EditListenerList(){
		lis = Collections.synchronizedList(new ArrayList<>());
	}

	public void setList(List<EditListener> lis){
		this.lis.clear();
		if(lis!=null)
		    this.lis.addAll(lis);
	}
	public List<EditListener> getList(){
		return lis;
	}

	@Override
	public boolean equals(Object obj)
	{
		for(EditListener li:lis){
			if(li.equals(obj))
				return true;
		}
		return super.equals(obj);
	}

	@Override
	public void setEnabled(boolean Enabled)
	{
		for(EditListener li:lis){
			li.setEnabled(Enabled);
		}
		super.setEnabled(Enabled);
	}

	@Override
	public void setName(String name)
	{
		for(EditListener li:lis){
			li.setName(name);
		}
		super.setName(name);
	}
	
}
