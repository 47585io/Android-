package com.mycompany.who.SuperVisor.CodeMoudle.Base.View3;
import java.util.*;

public abstract interface Configer<T>
{
	public abstract void ConfigSelf(T target)
	
	public static class ConfigAll
	{
		public static <T> void startConfig(List<Configer<T>> cs,T target){
			for(Configer c:cs){
				c. ConfigSelf(target);
			}
		}

		public static<T> void startConfig(Configer<T> c,List<T> target){
			for(T t:target)
				c.ConfigSelf(t);
		}
	}
}
