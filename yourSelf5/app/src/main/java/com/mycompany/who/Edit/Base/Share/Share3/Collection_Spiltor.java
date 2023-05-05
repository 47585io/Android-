package com.mycompany.who.Edit.Base.Share.Share3;
import java.util.*;

public class Collection_Spiltor
{
	public static void addAll(Collection t,Collection t2){
		if(t!=null && t2!=null)
			t.addAll(t2);
	}
	
	public static void addAll(Map p,Map p2){
		if(p!=null && p2!=null)
			p.putAll(p2);
	}
}
