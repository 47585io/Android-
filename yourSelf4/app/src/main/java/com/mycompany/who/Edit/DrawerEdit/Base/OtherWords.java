package com.mycompany.who.Edit.DrawerEdit.Base;

import java.util.*;
import com.mycompany.who.*;

public class OtherWords extends Words
{

	public static int words_func = 0;
	public static int words_vill = 1;
	public static int words_obj = 2;
	public static int words_type = 3;
	public static int words_tag = 4;
	public static int words_attr = 5;
	
	
	public List<Collection<String>> mdates;
	
	public OtherWords(int size){
		mdates=new ArrayList<>();
		while(size-->0){
			Collection<String> col=Collections.synchronizedSet(new TreeSet<String>());
			mdates.add(col);
			//安全地加词
		}
	}
	
	public void clear(){
		for(Collection t:mdates)
		    t.clear();
	}
}
