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
	
	public Extension(){
		Lis=new ArrayList<>();
	}
	
	public void creatAExtension(EditListenerInfo self){
		//创建一些Listener，并将它的指针添加至Lis，之后将它的指针添加至Info
		//将Info的指针加入Infos
		Init(self);
		List<EditListener> lis= new ArrayList<>();
		if(lis.size()!=0){
			Lis.addAll(lis);
			Infos.add(self);
			for(EditListener li:lis){
				self.addAListener(li);
			}
		}
	}
	public void delAExtension(EditListenerInfo self){
		//将指定的Listener的指针从Info中移除
		for(EditListener li:Lis){
			self.delAListener(li);
		}
	}
	public void Delete(){
		//踢出所有的Info，踢出前删除Listener
		onDestory();
		for(EditListenerInfo self:Infos){
			delAExtension(self);
		}
		Infos=null;
		Lis=null;
	}
	
	protected void setEnabled(boolean Enabled){
		//为所有分配的Listener设置Enabled
		for(EditListener li:Lis){
			li.setEnabled(Enabled);
		}
	}
	
	protected void onDestory(){}
	
    abstract void getListeners(List<EditListener> lis)
	
	abstract void Init(EditListenerInfo self)
	
	
	public static interface Extension_Spiltor{
			
	}
	
}
