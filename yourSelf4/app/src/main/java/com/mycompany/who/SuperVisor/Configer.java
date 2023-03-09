package com.mycompany.who.SuperVisor;
import java.util.*;

public abstract class Configer<T>
{
	public List<Configer<T>> configerList;
	T target;
	
	public Configer(T target){
		this.target=target;
	}
	
	public void startConfig(List<Configer<T>> cs){
		for(Configer c:cs){
			c. ConfigSelf(target);
		}
	}
	public List<Configer<T>> getList(){
		return configerList;
	}
	
	abstract void ConfigSelf(T target)
}
