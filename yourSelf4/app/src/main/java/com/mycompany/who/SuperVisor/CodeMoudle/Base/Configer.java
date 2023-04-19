package com.mycompany.who.SuperVisor.CodeMoudle.Base;
import java.util.*;

public interface Configer<T>
{
	public void ConfigSelf(T target)
	
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
