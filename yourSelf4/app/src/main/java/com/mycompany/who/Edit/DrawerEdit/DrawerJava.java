package com.mycompany.who.Edit.DrawerEdit;

import android.content.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import com.mycompany.who.Edit.DrawerEdit.Share.Colors.*;
import java.util.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;

public class DrawerJava extends DrawerXML
{
	public DrawerJava(Context cont){
		super(cont);
	}

	@Override
	public void setLuagua(String name)
	{
		if(name.equals("java"))
			setDefaultFinder( new FinderJava());
		super.setLuagua(name);
	}
	
	
	public class AnyThingForJava extends AnyThingForText{

		//不回溯的NoSans块，用已有信息完成判断
		public DoAnyThing getNoSans_Keyword(){
			//获取关键字和保留字
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					//找到一个单词 或者 未找到单词就遇到特殊字符，就把之前累计字符串清空
					//为了节省时间，将更简单的条件放前面，触发&&的断言机制
					if(!String_Splitor. IsAtoz(src.charAt(nowIndex+1))&&src.charAt(nowIndex+1)!='_'&&Array_Splitor.indexOf(nowWord.toString(),getKeyword())!=-1){
						//如果当前累计的字符串是一个关键字并且后面没有a～z这些字符，就把它加进nodes
						nodes.add(new wordIndex(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_key));
						nowWord.replace(0,nowWord.length(),"");
					    return nowIndex;
					}
					else if(!String_Splitor. IsAtoz(src.charAt(nowIndex+1))&&Array_Splitor.indexOf(nowWord.toString(),getConstword())!=-1){
						//否则如果当前累计的字符串是一个保留字并且后面没有a～z这些字符，就把它加进nodes
						nodes.add(new wordIndex(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_const));
						nowWord.replace(0,nowWord.length(),"");
						return nowIndex;
					}		

					//关键字和保留字和变量不重复，所以只要中了其中一个，则就是那个
					//如果能进关键字和保留字和变量的if块，则说明当前字符一定不是特殊字符

					return -1;
				}	
			};
		}
		
		
		public DoAnyThing getNoSans_Func(){
			//获取函数
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					int afterIndex=tryAfterIndex(src,nowIndex+1);
					if(src.charAt(afterIndex)=='('){
					    if(getLastfunc().contains(nowWord.toString())){
							//否则如果当前累计的字符串是一个函数并且后面是（ 字符，就把它加进nodes
						    nodes.add(new wordIndex(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_func));
						    nowWord.replace(0,nowWord.length(),"");
						    return afterIndex-1;
						}
					}
					return -1;
				}
			};
		}
		public DoAnyThing getNoSans_Villber(){
			//获得变量
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					if(!String_Splitor. IsAtoz(src.charAt(nowIndex+1))&&getHistoryVillber().contains(nowWord.toString())){
						int afterIndex=tryAfterIndex(src,nowIndex+1);
						if(src.charAt(afterIndex)!='('){
							//否则如果当前累计的字符串是一个变量并且后面没有a～z和（ 这些字符，就把它加进nodes
						    nodes.add(new wordIndex(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_villber));
						    nowWord.replace(0,nowWord.length(),"");
						    return afterIndex-1;
						}
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
					if(!String_Splitor. IsAtoz(src.charAt(nowIndex+1))&&getThoseObject().contains(nowWord.toString())){
						int afterIndex=tryAfterIndex(src,nowIndex+1);
						if(src.charAt(afterIndex)!='('){
							//否则如果当前累计的字符串是一个对象并且后面没有a～z和（ 这些字符，就把它加进nodes
						    nodes.add(new wordIndex(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_obj));
						    nowWord.replace(0,nowWord.length(),"");
						    return afterIndex-1;
						}
					}
					return -1;
				}
			};
		}

		public DoAnyThing getNoSans_Type(){
			//获取类型
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					if(!String_Splitor. IsAtoz(src.charAt(nowIndex+1))&&getBeforetype().contains(nowWord.toString())){
						int afterIndex=tryAfterIndex(src,nowIndex+1);
						if(src.charAt(afterIndex)!='('){
							//否则如果当前累计的字符串是一个类型并且后面没有a～z和（ 这些字符，就把它加进nodes
						    nodes.add(new wordIndex(nowIndex-nowWord.length()+1,nowIndex+1,Colors.color_type));
						    nowWord.replace(0,nowWord.length(),"");
						    return afterIndex-1;

						}
					}
					return -1;
				}
			};
		}


		//会回溯的Sans块，试探并记录单词
		public DoAnyThing getSans_TryFunc(){
			//试探函数
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					wordIndex node;
					if(src.charAt(nowIndex)=='('){
						//如果它是(字符，将之前的函数名存起来
						node=tryWord(src,nowIndex-1);
						getLastfunc().add(src.substring(node.start,node.end));
						return nowIndex;
					}

					return -1;	
				}	
			};
		}
		public DoAnyThing getSans_TryVillber(){
			//试探变量和类型
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{					 
					wordIndex node; 
					if((src.charAt(nowIndex)=='=')){
						//如果它是.或=字符，将之前的对象名或变量存起来	
						//=前后必须是分割符或普通的英文字符，不能是任何与=结合的算术字符
						node=tryWord(src,nowIndex-1);
						if(src.charAt(nowIndex)=='='&&!getHistoryVillber().contains(src.substring(node.start,node.end))
						   &&Array_Splitor.indexOf(src.charAt(nowIndex-1),arr)==-1
						   &&Array_Splitor.indexOf(src.charAt(nowIndex+1),arr)==-1){
							//二次试探，得到类型
							//变量必须首次出现才有类型
							int nowN= tryLine_Start(src, node.start);
							wordIndex tmp = tryWord(src,node.start-1);
							if(tmp.start>nowN)
							//类型与变量必须在同一行
							    getBeforetype().add(src.substring(tmp.start,tmp.end));
						}

						getHistoryVillber().add(src.substring(node.start,node.end));
						return nowIndex;
					}
					return -1;
			    }
			};
		}
		public DoAnyThing getSans_TryObject(){
			//试探对象
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{
					wordIndex node;
					if(src.charAt(nowIndex)=='.'&&!String_Splitor.indexOfNumber(src.charAt(nowIndex+2))){
						node=tryWord(src,nowIndex-1);
						getThoseObject().add(src.substring(node.start,node.end));
						return nowIndex;
					}
					return -1;
				}
			};
		}
		public DoAnyThing getSans_TryType(){
			//试探类型
			return new DoAnyThing(){
				@Override
				public int dothing(String src, StringBuffer nowWord, int nowIndex, ArrayList<wordIndex> nodes)
				{	
				   	if(!String_Splitor.IsAtoz(src.charAt(nowIndex+1))){
						int index = Array_Splitor.indexOf(nowWord.toString(),WordLib.keyword);
					    if(index==-1)
							return -1;
					    if(WordLib.keyword[index]=="class"
						   ||WordLib.keyword[index]=="new"
						   ||WordLib.keyword[index]=="extends"
						   ||WordLib.keyword[index]=="implements"
						   ||WordLib.keyword[index]=="interface"){
							wordIndex tmp=tryWordAfter(src,nowIndex+1);
							getBeforetype().add(src.substring(tmp.start,tmp.end));
							return nowIndex;
						}

					}
					return -1;
				}
			};
		}

		private char arr[]; 
		AnyThingForJava(){
			arr= new char[]{'!','~','+','-','*','/','%','^','|','&','<','>','='};
		    Arrays.sort(arr);
		}
	}
	
	
	class FinderJava extends EditFinderListener
	{

		@Override
		public void OnFindWord(ArrayList<DrawerBase.DoAnyThing> totalList)
		{
			AnyThingForJava AllThings = new AnyThingForJava();

			totalList.add(AllThings.getSans_TryFunc());	
			totalList.add(AllThings.getSans_TryVillber());
			totalList.add(AllThings.getSans_TryType());
			totalList.add(AllThings.getSans_TryObject());
			totalList.add(AllThings.getNoSans_Char());
			//请您在任何时候都加入getChar，因为它可以适时切割单词
			
		}

		@Override
		public void OnDrawWord(ArrayList<DrawerBase.DoAnyThing> totalList)
		{
			AnyThingForJava AllThings = new AnyThingForJava();

		    totalList.add(AllThings.getGoTo_zhuShi());
		    totalList.add(AllThings.getGoTo_Str());
			totalList.add(AllThings.getNoSans_Keyword());
		    totalList.add(AllThings.getNoSans_Func());
		    totalList.add(AllThings.getNoSans_Villber());
			totalList.add(AllThings.getNoSans_Object());
		    totalList.add(AllThings.getNoSans_Type());

			totalList.add(AllThings.getNoSans_Char());
			//请您在任何时候都加入getChar，因为它可以适时切割单词
			
		}

		@Override
		public void OnClearFindWord(Words words, OtherWords twords)
		{
			Array_Splitor. delSame(getLastfunc(),getKeyword());
			//函数名不可是关键字，但可以和变量或类型重名	
			Array_Splitor.delSame(getLastfunc(),getKeyword());
			//类型不可是关键字
			Array_Splitor.delSame(getBeforetype(),getHistoryVillber());
			//类型不可是变量，类型可以和函数重名
			Array_Splitor.delSame(getBeforetype(),getConstword());
			//类型不可是保留字
			Array_Splitor. delSame(getHistoryVillber(),getKeyword());
			Array_Splitor. delSame(getThoseObject(),getKeyword());
			//变量不可是关键字
			Array_Splitor. delSame(getThoseObject(),getConstword());
			Array_Splitor.delSame(getHistoryVillber(),getConstword());
			//变量不可是保留字
			Array_Splitor.delNumber(getBeforetype());
			Array_Splitor.delNumber(getHistoryVillber());
			Array_Splitor.delNumber(getLastfunc());
			Array_Splitor.delNumber(getThoseObject());
			//去掉数字
		}

		@Override
		public void OnClearDrawWord(int start,int end,String text, ArrayList<wordIndex> nodes)
		{
			clearRepeatNode(nodes);	
		}
	}
}
