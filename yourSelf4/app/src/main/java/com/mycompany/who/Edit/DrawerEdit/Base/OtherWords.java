package com.mycompany.who.Edit.DrawerEdit.Base;

import java.util.*;
import com.mycompany.who.*;

public class OtherWords
{
	
	
	public ArrayList<TreeSet<String>> mdates;
	
	public OtherWords(int size){
		mdates=new ArrayList<>();
		while(size-->0){
			mdates.add(new TreeSet<String>());
		}
	}
	
	public void clear(){
		for(TreeSet t:mdates)
		    t.clear();
	}
}
