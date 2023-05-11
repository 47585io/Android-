package com.mycompany.who.Edit.Extension;

import com.mycompany.who.Edit.EditBuilder.ListenerVistor.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import java.io.*;
import java.util.*;


/*  除EditBuilder之外的，更灵活的方案  */
public abstract class Extension extends SerializableObject
{

	private transient List<EditListener> Lis;
	private transient List<EditListenerInfo> Infos;
	private int flag;
	private String name;
	private Settings settings;


	public Extension()
	{
		super();
		Lis=new LinkedList<>();
		Infos=new LinkedList<>();
		settings = loadSettings();
	}

	public List<EditListenerInfo> getInfos(){
		return Infos;
	}
	public List<EditListener> getListeners(){
		return Lis;
	}

	/* 为所有分配的Listener设置flag */
	public void setFlag(int flag)
	{
		for(EditListener li:Lis){
			li.setFlag(flag);
		}
		this.flag = flag;
	}

	public void setName(String name){
		this.name=name;
	}
	public String getName(){
		return name;
	}

	public void setSettings(Settings s){
		settings = s;
	}
	public Settings getSettings(){
		return settings;
	}


	/* 创建一些Listener，并将它的指针添加至Lis，之后将它的指针添加至Info，将Info的指针加入Infos */
	public void creatListener(EditListenerInfoUser user)
	{
		if(user==null)
			return;

		onConfig(user);
		EditListenerInfo Info = user.getInfo();
		List<EditListener> lis= new LinkedList<>();
		onGetListeners(lis);

		if(lis.size()!=0)
		{
			if(onAddInfo(Info)){
			    Infos.add(Info);
			}
			for(EditListener li:lis)
		    {
				if(Info.addAListener(li))
				{
					if(onAddListener(li)){
				        Lis.add(li);
					}
				}
			}
		}
	}

	/* 同上，兼容EditListenerInfo的重载 */
	public void creatListener(EditListenerInfo Info)
	{
		if(Info==null)
			return;
		List<EditListener> lis= new LinkedList<>();
		onGetListeners(lis);

		if(lis.size()!=0)
		{
			if(onAddInfo(Info)){
			    Infos.add(Info);	
			}
			for(EditListener li:lis)
			{
				if(Info.addAListener(li))
				{
					if(onAddListener(li)){
				        Lis.add(li);
					}
				}
			}
		}
	}

	/* 将指定的Listener的指针从Info中移除 */
	public void delListener(EditListenerInfo self)
	{
		for(EditListener li:Lis)
		{
			if(self.delAListener(li))
			{
				if(onDelListener(li)){
				    Lis.remove(li);	
				}
			}
		}
	}

	/* 踢出所有的Info，踢出前删除Listener */
	public void clear()
	{
		for (EditListenerInfo self:Infos){
			delListener(self);
		}
		Infos=null;
		Lis=null;
	}


	abstract public boolean onAddInfo(EditListenerInfo Info)

	abstract public boolean onAddListener(EditListener li)

	abstract public boolean onDelListener(EditListener li)

    abstract public void onGetListeners(List<EditListener> lis)

	abstract public void onConfig(EditListenerInfoUser user)
	
	abstract public Settings loadSettings()


	public static interface Extension_Spiltor
	{

		public void addAExtension(Extension E)

		public void delAExtension(Extension E)

		public Extension findAExtension(String name)

	}

	public static interface Settings
	{

		public void readSettings(String settings)

		public String writeSettings()

		public void setSettingItem(int id,SettingItem item)
		
		public SettingItem getSettingItem()

		
		public static interface SettingItem
		{
			public String name()

			public String dateStr()

			public int flag()
		}

	}

}
