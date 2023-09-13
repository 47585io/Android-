package com.editor.text2.builder;

import android.text.*;
import com.editor.text2.base.*;
import com.editor.text2.builder.listenerInfo.*;
import com.editor.text2.builder.listenerInfo.listener.*;
import com.editor.text2.builder.listenerInfo.listener.baselistener.*;
import com.editor.text2.builder.words.*;
import java.util.*;
import static com.editor.text2.builder.listenerInfo.EditListenerInfo.*;
import static com.editor.text2.builder.words.Words.*;
import android.text.style.*;
import com.editor.text.base.*;

public class CodeEditBuilder implements EditBuilder
{

	public static class DrawerFactory implements ListenerFactory
	{

		public static EditListener getDefaultDrawer(){
			return new DefaultDrawer();
		}
		public EditListener ToLisrener(String Lua){
			return getDefaultDrawer();
		}
		public void SwitchListener(EditListenerInfo Info, String Lua)
		{
			//所有的工厂都替换了监听器
			Info.delListenerFrom(DrawerIndex);
			Info.addListenerTo(ToLisrener(Lua),DrawerIndex);
		}

		public static class DefaultDrawer extends myEditDrawerListener
		{

			@Override
			public wordIndex[] howToFindNodes(int start, int end, CharSequence text, Words lib)
			{
				int len = end-start;
				char[] array = new char[len];
				TextUtils.getChars(text,start,end,array,0);
				List<wordIndex> nodes = new LinkedList<>();
				
				//Step1: 检查注释和字符串及字符，优先级最高
				int last = 0;
				while (true)
				{
					int index = ArrayUtils.indexOf(array,'"',last);
					if(index<0){
						break;
					}
					int nextIndex = ArrayUtils.indexOf(array,'"',index+1);
					if(nextIndex<0){
						nodes.add(obtainNode(index,len,new ForegroundColorSpan(0xff98c379)));
						break;
					}
					nodes.add(obtainNode(index,nextIndex+1,new ForegroundColorSpan(0xff98c379)));
					last = nextIndex+1;
				}
				Collection<Character> chars = lib.getACollectionChars(chars_fuhao);
				
				//Step2: 检查关键字
				StringBuilder b = new StringBuilder();
				Collection<Character> S = lib.getACollectionChars(chars_spilt);
				Collection<CharSequence> words = lib.getACollectionWords(words_key);
				for(int i=0;i<array.length;++i)
				{
					char c = array[i];
					b.append(c);
					if(chars.contains(c)||S.contains(c)){
						b.delete(0,b.length());
						continue;
					}
					if(words.contains(b.toString()))
					{
						c = i<array.length-1 ? array[i+1]:'0';
						if(!(c>='a' && c<='z' && c>='A' && c<='Z')){
							nodes.add(obtainNode(i-b.length()+1,i+1,new ForegroundColorSpan(0xffcc80a9)));
						}
						b.delete(0,b.length());
					}
				}
				//Step3: 检查变量，函数，类，不可处于关键字范围内
				
				//Step4: 检查字符，字符不可处于单词范围内
				for(int i=array.length-1;i>=0;--i)
				{
					char c = array[i];
					if(c>='0'&&c<='9'){
						//nodes.add(obtainNode(i,i+1,new ForegroundColorSpan(0xffff9090)));
					}
					if(chars.contains(c)){
						nodes.add(obtainNode(i,i+1,new ForegroundColorSpan(0xff57adbb)));
					}
				}
				
				wordIndex[] nodeArray = new wordIndex[nodes.size()];
				nodes.toArray(nodeArray);
				return nodeArray;
			}
				
			private void findPairChars(char[] array, char st, char en, List<wordIndex> nodes, boolean[] table)
			{
				int last = 0;
				while (true)
				{
					int index = ArrayUtils.indexOf(array,'"',last);
					if(index<0){
						break;
					}
					int nextIndex = ArrayUtils.indexOf(array,'"',index+1);
					if(nextIndex<0)
					{
						wordIndex node = obtainNode(index,array.length,new ForegroundColorSpan(0xff98c379),table);
						if(node!=null){
							nodes.add(node);
						}
						break;
					}
					wordIndex node = obtainNode(index,nextIndex+1,new ForegroundColorSpan(0xff98c379),table);
					if(node!=null){
						nodes.add(node);
					}
					last = nextIndex+1;
				}
			}
			private void findPairWords(CharSequence st, CharSequence en, List<wordIndex> nodes){
				
			}

			private wordIndex obtainNode(int start, int end, Object span, boolean[] table)
			{
				if(checkRange(start,end,table)){
					return null;
				}
				Arrays.fill(table,start,end,true);
				return obtainNode(start, end, span);
			}
			
			private boolean checkRange(int start, int end, boolean[] table)
			{
				for(;start<end;++start)
				{
					if(table[start]){
						return true;
					}
				}
				return false;
			}
			
		}

	}
	
	public static class WordsPackets implements WordsPacket
	{

		@Override
		public void SwitchWords(Words Lib, String Lua)
		{
			UnPackWords(Lua).loadWords(Lib);
		}

		@Override
		public AWordsPacket UnPackWords(String Lua)
		{
			switch (Lua)
			{
				case "text":
				    return getBaseWordsPacket();
				case "xml":
					return getXMLWordsPacket();
				case "java":
				    return getJavaWordsPacket();
			}
			return null;
		}

		public static AWordsPacket getJavaWordsPacket(){
			return new JavaWordsPacket();
		}
		public static AWordsPacket getXMLWordsPacket(){
			return new XMLWordsPacket();
		}
		public static AWordsPacket getBaseWordsPacket(){
			return new BaseWordsPacket();
		}


		public static class BaseWordsPacket implements AWordsPacket
		{
			public static final Character[] fuhaox= new Character[]{
				'(',')','{','}','[',']',
				'=',';',',','.',':',
				'+','-','*','/','%',
				'^','|','&','<','>','?','@',
				'!','~','\'','\n',' ','\t','#','"','\''
			};
			public static final Character[] spiltx= new Character[]{
				'\n',' ','\t','<','>',
			};
			public static final Collection<Character> fuhao = new HashSet<>();
			public static final Collection<Character> spilt = new HashSet<>();

			static{
				Arrays.sort(fuhaox);
				Arrays.sort(spiltx);
				Collections.addAll(fuhao,fuhaox);
				Collections.addAll(spilt,spiltx);
			}

			@Override
			public void loadWords(Words Lib)
			{
				Lib.setACollectionChars(chars_fuhao,fuhao);
				Lib.setACollectionChars(chars_spilt,spilt);
			}
			
			final public static void tryWord(CharSequence src,int index,range tmp)
			{
				//试探前面的单词
				try{
					while(fuhao.contains(src.charAt(index)))
						--index;
					tmp.end=index+1;
					while(!fuhao.contains(src.charAt(index)))
						--index;
					tmp.start=index+1;
				}catch(Exception e){
					tmp.start=0;
					tmp.end=0;
				}
			}

		}

		public static class JavaWordsPacket extends BaseWordsPacket
		{
			//所有单词
			public static CharSequence[] keyword = new String[]{
				"goto","const",
				"enum","assert",
				"package","import",
				"final","static","this","super",
				"try","catch","finally","throw","throws",
				"public","protected","private","friendly",
				"native","strictfp","synchronized","transient","volatile",
				"class","interface","abstract","implements","extends","new",
				"byte","char","boolean","short","int","float","long","double","void",
				"if","else","while","do","for","switch","case","default","break","continue","return","instanceof",
				"Override","Deprecated","SuppressWarnings","SafeVarargs","FunctionalInterface","param","UnsupportedAppUsage"
			};

			public static CharSequence[] constword = new String[]{"null","true","false"};
			public static CharSequence[] IknowFunc=new String[]{};
			public static CharSequence[] IknowType=new String[]{};
			public static Map<CharSequence,CharSequence> zhu_key_value=new HashMap<>();

			static{
				zhu_key_value.put("//","\n");
				zhu_key_value.put("/*","*/");
			}

			@Override
			public void loadWords(Words Lib)
			{
				super.loadWords(Lib);
				Lib.setACollectionWords(words_key,Arrays.asList(keyword));
				Lib.setACollectionWords(words_const,Arrays.asList(constword));
				Lib.setAMapWords(maps_zhu,zhu_key_value);
			}

		}

		public static class XMLWordsPacket extends BaseWordsPacket
		{
			public static CharSequence[] IknowTag= new String[]{	"*",
				"html","body","head","title","a","img","audio","input","b","sup","i","small","font","em","strong","sub",
				"ins","del","code","kbd","samp","var","pre","abbr","address","bdo","blockquote","cite","q","dfn","p","div","ul","li","ol","dl","dt","dd",
				"hr","br","h1","h2","h3","h4","h5","h6","main","xmp","style","script","link","textarea",
				"button","select","option","optgroup","picture","source","map","area","table","tr","th","td","caption","from","label","fieldset","legend",
				"datalist","keygen","output","span","frame","iframe","object","embed","marguee","canvas","video","base","meta","details","summary",
				"mark","noscript","figure","svg","circle","line","polyline","rect","ellipse","polygon","path","text","use","g","defs","pattern","animate","image","animateTransform",
			};
			public static CharSequence[] Attribute=new String[]{

			};
			public static Map<CharSequence,CharSequence> zhu_key_value=new HashMap<>();

			static{
				zhu_key_value.put("<!--","-->");
			}

			@Override
			public void loadWords(Words Lib)
			{
				super.loadWords(Lib);
				Lib.setACollectionWords(words_tag,Arrays.asList(IknowTag));
				Lib.setAMapWords(maps_zhu,zhu_key_value);
			}

		}

	}

	public static WordsPacket getWordsPacket()
	{
		return new WordsPackets();
	}
	
	
	@Override
	public void SwitchLuagua(Object O, String Lua)
	{
		EditListenerInfo Info = null;
		Words WordLib = null;
		if(O instanceof EditListenerInfo){
			Info = (EditListenerInfo) O;
		}
		if(O instanceof EditListenerInfoUser){
			Info = ((EditListenerInfoUser)O).getInfo();
		}
		if(O instanceof Words){
			WordLib = (Words) O;
		}
		if(O instanceof WordsUser){
			WordLib = ((WordsUser)O).getWordLib();
		}
		if(Info!=null){
			
		}
		if(WordLib!=null){
			getWordsPacket().SwitchWords(WordLib,Lua);
		}
	}

	@Override
	public void trimListener(EditListenerInfo Info)
	{
		// TODO: Implement this method
	}

	@Override
	public void loadWords(Words Lib)
	{
		WordsPackets.getBaseWordsPacket().loadWords(Lib);
	}
	
}
