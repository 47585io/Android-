package com.mycompany.who.Edit.DrawerEdit;

import android.content.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import java.util.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;

public class DrawerXML extends DrawerText
{
	public DrawerXML(Context cont){
		super(cont);
	}

	@Override
	public void setLuagua(String name)
	{
		if(name=="xml")
			setDefaultFinder( new FinderXML());
		super.setLuagua(name);
	}
	
	class AnyThingForXML extends AnyThingForText{
		public DoAnyThing getDraw_Tag(){
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					//简单的一个xml方案
					wordIndex node;
					if(src.charAt(nowIndex)=='<'){
						node=tryWordAfter(src,nowIndex+1);
						node.b=Colors.color_tag;
						getTag().add(src.substring(node.start,node.end));
						nodes.add(node);
						nowIndex=node.end-1;
						return nowIndex;
					}

					return -1;
				}
			};
		}
		public DoAnyThing getDraw_Attribute(){
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					wordIndex node;
					if(src.charAt(nowIndex)=='='
					   ||src.charAt(nowIndex)==':'
					   ){
						node=tryWord(src,nowIndex-1);
						node.b=Colors.color_attr;
						getAttribute().add(src.substring(node.start,node.end));
						nodes.add(node);
						nowIndex=node.end-1;
						return nowIndex;
					}
					return -1;
				}
			};
		}

	}
	
	class FinderXML extends EditFinderListener
	{

		@Override
		public void OnFindWord(ArrayList<DrawerBase.DoAnyThing> totalList,TreeSet<String> vector)
		{
			
		}

		@Override
		public void OnDrawWord(ArrayList<DrawerBase.DoAnyThing> totalList,TreeSet<String> vector)
		{
			AnyThingForXML AllThings = new AnyThingForXML();

			totalList.clear();
			totalList.add(AllThings.getGoTo_zhuShi());	
			totalList.add(AllThings.getGoTo_Str());
			totalList.add(AllThings.getDraw_Tag());

			totalList.add(AllThings.getDraw_Attribute());	

			totalList.add(AllThings.getNoSans_Char());
		}

		@Override
		public void OnClearFindWord(TreeSet<String> vector)
		{
			
		}

		@Override
		public void OnClearDrawWord(int start,int end,String text, ArrayList<wordIndex> nodes)
		{
			
		}
	}
	
}
