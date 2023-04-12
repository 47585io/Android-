package com.mycompany.who.Edit.ListenerVistor;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import com.mycompany.who.Edit.ListenerVistor.*;

import java.util.*;
import android.widget.*;
import com.mycompany.who.Edit.*;

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
		Lis=new ArrayList<>();
		Infos=new ArrayList<>();
	}
	
	public List<EditListenerInfo> getInfos(){
		return Infos;
	}
	
	public void creatListener(EditText Edit){
		//创建一些Listener，并将它的指针添加至Lis，之后将它的指针添加至Info
		//将Info的指针加入Infos
		onInit(Edit);
		EditListenerInfo self = ((CodeEdit)Edit).getInfo();
		List<EditListener> lis= new ArrayList<>();
		onGetListeners(lis);
		if(lis.size()!=0){
			Infos.add(self);
			for(EditListener li:lis){
				if(self.addAListener(li))
				    Lis.add(li);
			}
		}
	}
	public void creatListener(EditListenerInfo self){
		//创建一些Listener，并将它的指针添加至Lis，之后将它的指针添加至Info
		//将Info的指针加入Infos
		List<EditListener> lis= new ArrayList<>();
		onGetListeners(lis);
		if(lis.size()!=0){
			Infos.add(self);
			for(EditListener li:lis){
				if(self.addAListener(li))
				    Lis.add(li);
			}
		}
	}
	public void delListener(EditListenerInfo self){
		//将指定的Listener的指针从Info中移除
		for(int i =0;i<Lis.size();++i){
			EditListener li = Lis.get(i);
			if(self.delAListener(li))
				onDelListener(li);
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
	
	abstract protected void onDestory()
	
	abstract public void onDelListener(EditListener li)
	
    abstract public void onGetListeners(List<EditListener> lis)
	
	abstract public void onInit(EditText self)
	
	
	public static interface Extension_Spiltor{
			
		public void addAExtension(Extension E)
		
		public void delAExtension(Extension E)
		
		public void findExtension(String name)
		
	}
	
}
