package com.mycompany.who.Edit.DrawerEdit;

import android.content.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import java.util.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;
import android.util.*;

public class DrawerCSS extends DrawerJava
{
	public DrawerCSS(Context cont){
		super(cont);
	}
	public DrawerCSS(Context cont,DrawerCSS Edit)
	{
		super(cont,Edit);
	}
	public DrawerCSS(Context cont,AttributeSet set){
		super(cont,set);
	}
	@Override
	public void setLuagua(String name)
	{
		if(name.equals("css"))
			setDefaultFinder(new FinderCSS());
		super.setLuagua(name);
	}
	
	

	public void clearRepeatNodeForCSS(ArrayList<wordIndex> nodes){
		//清除优先级低且位置重复的node
		int i;
		String src = getText().toString();
		for(i=0;i<nodes.size();i++){
			wordIndex now = nodes.get(i);
			if(src.substring(now.start,now.end).equals("-")){
				nodes.remove(i);
				i--;
			}
		}
	}

	
	
	public wordIndex tryWordForCSS(String src, int index)
	{
		//试探前面的单词
		wordIndex tmp = new wordIndex(0, 0, (byte)0);
		try
		{
			while (Array_Splitor. indexOf(src.charAt(index), getFuhao()) != -1)
				index--;
			tmp.end=index + 1;
			while (src.charAt(index) == '-' || Array_Splitor.indexOf(src.charAt(index), getFuhao()) == -1)
				index--;
			tmp.start=index + 1;
		}
		catch (Exception e)
		{
			return new wordIndex(0, 0, (byte)0);
		}
		return tmp;
	}

	public wordIndex tryWordAfterCSS(String src, int index)
	{
		//试探后面的单词
		wordIndex tmp = new wordIndex(0, 0, (byte)0);
		try
		{
			while (Array_Splitor.indexOf(src.charAt(index), getFuhao()) != -1)
				index++;
			tmp.start=index;
			while (src.charAt(index) == '-' || Array_Splitor.indexOf(src.charAt(index), getFuhao()) == -1)
				index++;
			tmp.end=index;
		}
		catch (Exception e)
		{
			return new wordIndex(0, 0, (byte)0);
		}
		return tmp;
	}
	public wordIndex tryWordSplit(String src,int nowIndex){
		//试探纯单词
		int index=nowIndex-1;
	    wordIndex tmp = new wordIndex(0,0,(byte)0);
		try{
			while(index>-1&&Array_Splitor.indexOf(src.charAt(index),getFuhao())==-1)
				index--;
			tmp.start=index+1;
			tmp.end=nowIndex;
		}catch(Exception e){
			return new wordIndex(0,0,(byte)0);
		}
		return tmp;
	}
	public wordIndex tryWordSplitAfter(String src,int index){
		//试探纯单词
	    wordIndex tmp = new wordIndex(0,0,(byte)0);
		try{
			tmp.start=index;
			while(index<src.length()&&Array_Splitor.indexOf(src.charAt(index),getFuhao())==-1)
				index++;
			tmp.end=index;
		}catch(Exception e){
			return new wordIndex(0,0,(byte)0);
		}
		return tmp;
	}

	class AnyThingForCSS extends AnyThingForText
	{
		public DoAnyThing getCSSDrawer()
		{
			return new DoAnyThing(){

				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					wordIndex node=new wordIndex(0, 0, (byte)0);
					if (src.charAt(nowIndex) == '#')
					{
						nodes.add(new wordIndex(nowIndex,nowIndex+1,Colors.color_fuhao));
						node=tryWordAfterCSS(src, nowIndex + 1);
						node.b=Colors.color_cssid;
						nodes.add(node);
					    getHistoryVillber().add(src.substring(node.start,node.end));
						nowIndex=node.end - 1;
						return nowIndex;
					}
					else if (src.charAt(nowIndex) == '.' && !String_Splitor.indexOfNumber(src.charAt(nowIndex - 1)) && !String_Splitor.indexOfNumber(src.charAt(nowIndex + 1)))
					{
						nodes.add(new wordIndex(nowIndex,nowIndex+1,Colors.color_fuhao));
						node=tryWordAfterCSS(src, nowIndex + 1);
						getBeforetype().add(src.substring(node.start,node.end));
						node.b=Colors.color_csscla;
						nodes.add(node);
						nowIndex=node.end - 1;
						return nowIndex;
					}
					return -1;
				}
			};
		}

		public DoAnyThing getCSSChecker()
		{
			return new DoAnyThing(){

				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					//为了节省时间，将更简单的条件放前面，触发&&的断言机制
					if(String_Splitor.IsAtoz(src.charAt(nowIndex + 1))&& src.charAt(tryLine_End(src, nowIndex) - 1) == '{'){
						 if(getHistoryVillber().contains(nowWord.toString())){
						     nodes.add(new wordIndex(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_cssid));
						     return nowIndex;
						 }
						 else if(getBeforetype().contains(nowWord.toString())){
							 nodes.add(new wordIndex(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_csscla));
							return nowIndex;
						 }
					}
					
					return -1;
				}
			};
		}


		public DoAnyThing getNoSans_Tag()
		{
			//获取Tag
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{

					if (!String_Splitor. IsAtoz(src.charAt(nowIndex + 1))
						&& src.charAt(tryLine_End(src, nowIndex) - 1) == '{'
						&&(getTag().contains(nowWord.toString()) || Array_Splitor.indexOf(nowWord.toString(), getIknowtag()) != -1))
					{
						//如果当前累计的字符串是一个Tag并且后面没有a～z和这些字符，就把它加进nodes
						nodes.add(new wordIndex(nowIndex - nowWord.length() + 1, nowIndex + 1, Colors.color_tag));
						nowWord.replace(0, nowWord.length(), "");
					    return nowIndex;
					}
					return -1;
				}
			};
		}
		public DoAnyThing getNoSans_Attribute()
		{
			//获取属性
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					int afterIndex=tryAfterIndex(src, nowIndex + 1);

					if ( !String_Splitor. IsAtoz(src.charAt(nowIndex + 1)) && src.charAt(afterIndex) != '('&&getAttribute().contains(nowWord) )
					{
						//如果当前累计的字符串是一个属性并且后面没有a～z这些字符，就把它加进nodes
						nodes.add(new wordIndex(nowIndex - nowWord.length() + 1, nowIndex + 1,Colors. color_attr));
						nowWord.replace(0, nowWord.length(), "");
					    return nowIndex;
					}
					return -1;
				}
			};
		}	
		public DoAnyThing getDraw_Attribute()
		{
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					wordIndex node;
					if (src.charAt(nowIndex) == '='
						|| src.charAt(nowIndex) == ':'){
						int i=src.indexOf('{',nowIndex);
						if (i<tryLine_End(src, nowIndex)&&i!=-1)
						{
							node=tryWordAfterCSS(src, nowIndex + 1);
							node.b=Colors.color_cssfl;
							getAttribute().add(src.substring(node.start, node.end));
							nodes.add(node);
							nowIndex=node.end - 1;
							return nowIndex;
						}
						else{
							node=tryWordForCSS(src, nowIndex - 1);
							node.b=Colors.color_attr;
							getAttribute().add(src.substring(node.start, node.end));
							nodes.add(node);
							nowIndex=node.end - 1;
							return nowIndex;
						}
					}
					return -1;
				}
			};
		}
	}
	
	class AnyThingForquick{
		//如果所有东西不需进行二次查找，就用这个吧
		public DoAnyThing getNoSans_Func(){
			//获取函数
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{

					if(src.charAt(nowIndex)=='('){
						//否则如果当前累计的字符串是一个函数并且后面是（ 字符，就把它加进nodes
						wordIndex node = tryWord(src,nowIndex);
						node.b=Colors.color_func;
						nodes.add(node);
						nodes.add(new wordIndex(nowIndex,nowIndex+1,Colors.color_fuhao));
						getLastfunc().add(src.substring(node.start,node.end));
						nowWord.replace(0,nowWord.length(),"");
						return nowIndex;
					}
					return -1;
				}
			};
		}
		public DoAnyThing getNoSans_Object(){
			//获取对象
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{

					if(src.charAt(nowIndex)=='.'){
						//否则如果当前累计的字符串是一个对象并且后面是.字符，就把它加进nodes
						wordIndex node = tryWord(src,nowIndex);
						node.b=Colors.color_obj;
						nodes.add(node);
						nodes.add(new wordIndex(nowIndex,nowIndex+1,Colors.color_fuhao));
						getThoseObject().add(src.substring(node.start,node.end));
						nowWord.replace(0,nowWord.length(),"");
						return nowIndex;
					}
					return -1;
				}
			};
		}
	}
	
	
	class FinderCSS extends EditFinderListener
	{

		@Override
		public void OnFindWord(ArrayList<DrawerBase.DoAnyThing> totalList,TreeSet<String> vector)
		{
			
		}

		@Override
		public void OnDrawWord(ArrayList<DrawerBase.DoAnyThing> totalList,TreeSet<String> vector)
		{
			AnyThingForCSS CSSThings = new AnyThingForCSS();

			totalList.add(CSSThings.getGoTo_zhuShi());	
			totalList.add(CSSThings.getGoTo_Str());
		    totalList.add(new AnyThingForquick().getNoSans_Func());
			totalList.add(CSSThings.getCSSDrawer());
			totalList.add(CSSThings.getCSSChecker());


			totalList.add(CSSThings.getDraw_Attribute());	

			totalList.add(CSSThings.getNoSans_Tag());

			totalList.add(CSSThings.getNoSans_Char());
			//请您在任何时候都加入getChar，因为它可以适时切割单词
			
		}

		@Override
		public void OnClearFindWord(TreeSet<String> vector)
		{
			
		}

		@Override
		public void OnClearDrawWord(int start,int end,String text, ArrayList<wordIndex> nodes)
		{
			clearRepeatNodeForCSS(nodes);
		}
	}
}
