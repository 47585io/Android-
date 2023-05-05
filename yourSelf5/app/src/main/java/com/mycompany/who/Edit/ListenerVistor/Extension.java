package com.mycompany.who.Edit.ListenerVistor;
import com.mycompany.who.Edit.ListenerVistor.EditListener.BaseEditListener.*;
import java.util.*;

/*
  更灵活的方案
*/
public abstract class Extension
{
	
	List<EditListener> Lis;
	List<EditListenerInfo> Infos;
	boolean Enabled;
	String name;
	
	public Extension(){
		Lis=new LinkedList<>();
		Infos=new LinkedList<>();
	}
	
	public List<EditListenerInfo> getInfos(){
		return Infos;
	}
	public List<EditListener> getListeners(){
		return Lis;
	}
	
	public void creatListener(EditListenerInfoUser user){
		//创建一些Listener，并将它的指针添加至Lis，之后将它的指针添加至Info
		//将Info的指针加入Infos
		
		if(user==null)
			return;
		
		onInit(user);
		EditListenerInfo self = user.getInfo();
		List<EditListener> lis= new LinkedList<>();
		onGetListeners(lis);
		if(lis.size()!=0){
			if(onAddInfo(self))
			    Infos.add(self);
			for(EditListener li:lis){
				if(self.addAListener(li)){
					if(onAddListener(li))
				        Lis.add(li);
				}
			}
		}
	}
	public void creatListener(EditListenerInfo self){
		//创建一些Listener，并将它的指针添加至Lis，之后将它的指针添加至Info
		//将Info的指针加入Infos
		
		if(self==null)
			return;
		
		List<EditListener> lis= new LinkedList<>();
		onGetListeners(lis);
		if(lis.size()!=0){
			if(onAddInfo(self))
			    Infos.add(self);	
			for(EditListener li:lis){
				if(self.addAListener(li)){
					if(onAddListener(li))
				        Lis.add(li);
				}
			}
		}
	}
	public void delListener(EditListenerInfo self){
		//将指定的Listener的指针从Info中移除
		for(EditListener li:Lis){
			if(self.delAListener(li))
				if(onDelListener(li))
				    Lis.remove(li);	
		}
	}
	public void Delete(){
		//踢出所有的Info，踢出前删除Listener
		onDestory();
		for(EditListenerInfo self:Infos){
			delListener(self);
		}
		Infos=null;
		Lis=null;
	}
	
	protected void setEnabled(boolean Enabled){
		//为所有分配的Listener设置Enabled
		for(EditListener li:Lis){
			li.setEnabled(Enabled);
		}
		this. Enabled = Enabled;
	}
	
	public boolean getEnabled(){
		return Enabled;
	}
	
	public String getName(){
		return name;
	}
	
	
	abstract protected void onDestory()
	
	abstract public boolean onAddInfo(EditListenerInfo Info)
	
	abstract public boolean onAddListener(EditListener li)
	
	abstract public boolean onDelListener(EditListener li)
	
    abstract public void onGetListeners(List<EditListener> lis)
	
	abstract public void onInit(EditListenerInfoUser user)
	
	
	public static interface Extension_Spiltor{
			
		public void addAExtension(Extension E)
		
		public void delAExtension(Extension E)
		
		public Extension findAExtension(String name)
		
	}
	
}
