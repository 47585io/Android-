package com.editor.text2.builder;

import android.text.*;
import com.editor.*;
import com.editor.text2.base.share1.*;
import com.editor.text2.base.share2.*;
import com.editor.text2.base.share3.*;
import com.editor.text2.base.share4.*;
import com.editor.text2.builder.listener.*;
import com.editor.text2.builder.listener.baselistener.*;
import com.editor.text2.builder.words.*;
import java.util.*;

import static com.editor.text2.builder.words.Words.*;
import static com.editor.text2.builder.listener.myEditDrawerListener.*;
import com.editor.text.base.*;


public class CodeEditBuilder implements EditBuilder
{

	public static class DrawerFactory implements ListenerFactory
	{

		public static EditDrawerListener getDefaultDrawer(){
			return new DefaultDrawer();
		}
		public EditDrawerListener ToLisrener(String Lua){
			return getDefaultDrawer();
		}
		public void SwitchListener(EditListenerInfo Info, String Lua){
			Info.mDrawerListener = ToLisrener(Lua);
		}

		public static class DefaultDrawer extends myEditDrawerListener
		{

			@Override
			public wordIndex[] howToFindNodes(int start, int end, CharSequence text, Words lib)
			{
				FinderFactory.JavaFinderFactory fa = new FinderFactory.JavaFinderFactory(lib,getPool());
				return startFind(start,end,text,
				                 fa.getStrFinder(),
								 fa.getzhuFinder(),
								 fa.getKeyWordFinder(),
								 fa.getVariableFinder(),
				                 fa.getFuncFinder(),											
								 fa.getCharFinder());
			}
			
		}
		
		
		public static class FinderFactory
		{
			
			public static class TextFinderFactory
			{
				Words WordLib;
				EPool<wordIndex> mNodes;
				//设置一个WordLib，之后获取的DoAnyThing任务都是使用它的单词

				public TextFinderFactory(Words lib,EPool<wordIndex> pool){
					WordLib = lib;
					mNodes = pool;
				}
				
				public Finder getzhuFinder()
				{
					return new Finder(){

						@Override
						public int find(String text, StringBuilder nowWord, int nowIndex, List<wordIndex> nodes)
						{
							CharSequence key = null;
							for(CharSequence c: get_zhu().keySet()){
								if(text.indexOf(c.toString(),nowIndex)==nowIndex){
									key = c;
								}							
							}
							if (key != null)
							{
								//如果它是一个任意的注释，找到对应的另一个，并把它们之间染色
								CharSequence value= get_zhu().get(key);
								int nextindex = text.indexOf(value.toString(), nowIndex + key.length());
								if (nextindex != -1){
									saveChar(text, nowIndex, nextindex + value.length(), Colors.zhuShi, nodes);
									nowIndex = nextindex + value.length() - 1;
								}else{
							    	//如果找不到默认认为到达了末尾
									nowIndex +=1;
								}
								nowWord.delete(0, nowWord.length());
								return nowIndex;
							}
							return -1;
						}
					};
				}
				
				public Finder getStrFinder()
				{
					return new Finder(){

						@Override
						public int find(String text, StringBuilder nowWord, int nowIndex, List<wordIndex> nodes)
						{
							if (text.charAt(nowIndex) == '"')
							{
								//如果它是一个"，一直找到对应的"
								int endIndex = text.indexOf('"', nowIndex + 1);
								if (endIndex != -1){
									saveChar(text, nowIndex, endIndex + 1, Colors.Str, nodes);
									nowIndex = endIndex;
								}else{
									nowIndex+=1;
								}
								nowWord.delete(0, nowWord.length());
								return nowIndex;
							}		
							else if (text.charAt(nowIndex) == '\'')
							{
								//如果它是'字符，将之后的字符加进来
								if (text.charAt(nowIndex + 1) == '\\'){
									wordIndex node= obtainNode(nowIndex, nowIndex + 4, new ForegroundColorSpanX(Colors.Str));
									nodes.add(node);
									nowIndex += 3;	
								}
								else{		
									int endIndex = text.indexOf('\'', nowIndex + 1);
									saveChar(text, nowIndex, endIndex + 1,Colors.Str, nodes);
									nowIndex = endIndex;
								}
								nowWord.delete(0, nowWord.length());
								return nowIndex;
							}	
							return -1;
						}
					};
					
				}
				
				public Finder getCharFinder()
				{
					return new Finder(){

						@Override
						public int find(String text, StringBuilder nowWord, int nowIndex, List<wordIndex> nodes)
						{
							if (StringChecker.IsNumber(text.charAt(nowIndex))&&
							    !StringChecker.IsAtoz(text.charAt(nowIndex-1))&&
								!StringChecker.IsAtoz(text.charAt(nowIndex+1)))
							{
								//否则如果当前的字符是一个数字，就把它加进nodes
								//由于关键字和保留字一定没有数字，所以可以清空之前的字符串
								wordIndex node= obtainNode(nowIndex, nowIndex+1,new ForegroundColorSpanX(Colors.Number));
								nodes.add(node);
								nowWord.delete(0, nowWord.length());
								return nowIndex;
							}	
							else if (getFuhao().contains(text.charAt(nowIndex)))
							{	
								//否则如果它是一个特殊字符，就更不可能了，清空之前累计的字符串
								if (!getSpilt().contains(text.charAt(nowIndex)))
								{
									//如果它不是会被html文本压缩的字符，将它自己加进nodes
									//这是为了保留换行空格等
									wordIndex node=obtainNode(nowIndex, nowIndex + 1, new ForegroundColorSpanX(Colors.FuHao));
									nodes.add(node);
								}
								nowWord.delete(0, nowWord.length());
								//清空之前累计的字符串
								return nowIndex;
							}
							return -1;
						}
					};
				}
				
				public void saveChar(CharSequence src, int nowIndex, int nextIndex, int wantColor, List<wordIndex> nodes)
				{
					Collection spilt = getSpilt();
					int startIndex=nowIndex;
					for (;nowIndex < nextIndex;nowIndex++)
					{
						//保留特殊字符
						if (spilt.contains(src.charAt(nowIndex)))
						{
							wordIndex node= obtainNode(startIndex,nowIndex,new ForegroundColorSpanX(wantColor));
							nodes.add(node);
							startIndex = nowIndex + 1;
						}
					}
					wordIndex node = obtainNode(startIndex, nextIndex, new ForegroundColorSpanX(wantColor));
					nodes.add(node);

				}
				
				public Words getWordLib(){
					return WordLib;
				}
				public EPool getPool(){
					return mNodes;
				}
				public Collection<CharSequence> getKey(){
					return WordLib.getACollectionWords(words_key);
				}
				public Collection<CharSequence> getConst(){
					return WordLib.getACollectionWords(words_const);
				}
				public Collection<Character> getFuhao(){
					return WordLib.getACollectionChars(chars_fuhao);
				}
				public Collection<Character> getSpilt(){
					return WordLib.getACollectionChars(chars_spilt);
				}
				public Map<CharSequence,CharSequence> get_zhu(){
			    	return WordLib.getAMapWords(maps_zhu);
				}
				public Collection<CharSequence> getFunc(){
					return  WordLib.getACollectionWords(words_func);
				}
				public Collection<CharSequence> getVariable(){
					return  WordLib.getACollectionWords(words_variable);
				}
				public Collection<CharSequence> getType(){
					return  WordLib.getACollectionWords(words_type);
				}
				public Collection<CharSequence> getTag(){
					return  WordLib.getACollectionWords(words_tag);
				}
				public Collection<CharSequence> getAttribute(){
					return WordLib.getACollectionWords(words_attr) ;
				}
				
				public wordIndex obtainNode(){
					return mNodes.get();
				}
				public wordIndex obtainNode(Object span)
				{
					wordIndex node = mNodes.get();
					node.span = span;
					return node;
				}
				public wordIndex obtainNode(int start, int end, Object span)
				{
					wordIndex node = mNodes.get();
					node.set(start,end,span,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					return node;
				}
				public wordIndex obtainNode(int start, int end, Object span, int flags)
				{
					wordIndex node = mNodes.get();
					node.set(start,end,span,flags);
					return node;
				}
				
				final public void tryWord(CharSequence src,int index,range tmp)
				{
					Collection fuhao = getFuhao();
					while(index>-1 && fuhao.contains(src.charAt(index)))
						--index;
					tmp.end = index==-1 ? 0: index+1;
					while(index>-1 && !fuhao.contains(src.charAt(index)))
						--index;
					tmp.start= index==-1 ? 0 : index+1;
				}

				final public void tryWordAfter(CharSequence src,int index,range tmp)
				{
					Collection fuhao = getFuhao();
					int len = src.length();
					while(index<len && fuhao.contains(src.charAt(index)))
						++index;
					tmp.start=index;
					while(index<len && !fuhao.contains(src.charAt(index)))
						++index;
					tmp.end=index;
				}
				
			}
			
			public static class JavaFinderFactory extends TextFinderFactory
			{
				
				public JavaFinderFactory(Words lib,EPool<wordIndex> pool){
					super(lib,pool);
				}
				
				public Finder getFuncFinder()
				{
					return new Finder(){

						@Override
						public int find(String text, StringBuilder nowWord, int nowIndex, List<wordIndex> nodes)
						{
							if (text.charAt(nowIndex) == '(')
							{
								//如果它是(字符，将之前的函数名存起来
								wordIndex node = obtainNode();
								tryWord(text, nowIndex-1, node);
								CharSequence func = text.subSequence(node.start, node.end);
								if(!getKey().contains(func))
								{
									getFunc().add(func);
									node.span = new ForegroundColorSpanX(Colors.Function);
									nodes.add(node);
									nodes.add(obtainNode(nowIndex,nowIndex+1,new ForegroundColorSpanX(Colors.FuHao)));
									nowWord.delete(0, nowWord.length());
									return nowIndex;
								}
							}
							return -1;
						}
					};
				}

				public Finder getVariableFinder()
				{
					return new Finder(){

						@Override
						public int find(String text, StringBuilder nowWord, int nowIndex, List<wordIndex> nodes)
						{
							if ((text.charAt(nowIndex) == '.' || text.charAt(nowIndex)=='='))
							{
								wordIndex node = obtainNode(new ForegroundColorSpanX(Colors.Villber));
								tryWord(text, nowIndex-1,node);
								nodes.add(node);
								nodes.add(obtainNode(nowIndex,nowIndex+1,new ForegroundColorSpanX(Colors.FuHao)));
								getVariable().add(text.subSequence(node.start, node.end));
								nowWord.delete(0, nowWord.length());
								return nowIndex;
							}
							return -1;
						}
					};
				}

				public Finder getKeyWordFinder()
				{
					return new Finder(){

						@Override
						public int find(String text, StringBuilder nowWord, int nowIndex, List<wordIndex> nodes)
						{
							//找到一个单词 或者 未找到单词就遇到特殊字符，就把之前累计字符串清空
							//为了节省时间，将更简单的条件放前面，触发&&的断言机制
							if (!StringChecker.IsAtoz(text.charAt(nowIndex + 1)) 
								&& text.charAt(nowIndex + 1) != '_' 
								&& getKey().contains(nowWord.toString()))
							{
								//如果当前累计的字符串是一个关键字并且后面没有a～z这些字符，就把它加进nodes
								wordIndex node= obtainNode(nowIndex - nowWord.length() + 1, nowIndex + 1, new ForegroundColorSpanX(Colors.KeyWord));
								nodes.add(node);
								String Word=nowWord.toString();
								if (Word.equals("class")
									|| Word.equals("new")
									|| Word.equals("extends")
									|| Word.equals("implements")
									|| Word.equals("interface")
									|| Word.equals("instanceof")){
									wordIndex tmp=obtainNode(new ForegroundColorSpanX(Colors.Type));
									tryWordAfter(text, nowIndex + 1,tmp);
									nodes.add(tmp);
									getType().add(text.subSequence(tmp.start, tmp.end));
									nowWord.delete(0, nowWord.length());
									return tmp.end - 1;
								}
								nowWord.delete(0, nowWord.length());
								return nowIndex;
							}
							else if (!StringChecker. IsAtoz(text.charAt(nowIndex + 1)) 
								&& getConst().contains(nowWord.toString()))
							{
								//否则如果当前累计的字符串是一个保留字并且后面没有a～z这些字符，就把它加进nodes
								wordIndex node= obtainNode(nowIndex - nowWord.length() + 1, nowIndex + 1, new ForegroundColorSpanX(Colors.Const));
								nodes.add(node);
								nowWord.delete(0, nowWord.length());
								return nowIndex;
							}	
							//关键字和保留字和变量不重复，所以只要中了其中一个，则就是那个
							//如果能进关键字和保留字和变量的if块，则说明当前字符一定不是特殊字符
							return -1;
						}
					};
				}
			}
		}
	}

	
	public static class CompletorBoxes implements ListenerFactory
	{
		
		@Override
		public void SwitchListener(EditListenerInfo Info, String Lua)
		{
			Info.mCompletorListeners = ToListeners(Lua);
		}
		
		public EditCompletorListener[] ToListeners(String Lua)
		{
			switch(Lua){
				case "xml":
					return getXMLCompletorList();
				case "java":
					return getJavaCompletorList();
				case "css":
					return getCSSCompletorList();
				case "html":
					return getHTMLCompletorList();
				default:
				    return null;
			}
		}

		public static EditCompletorListener[] getJavaCompletorList()
		{
			return new EditCompletorListener[]{getKeyBox(),getConstBox(),getFuncBox(),getVillBox()};
		}
		public static EditCompletorListener[] getXMLCompletorList()
		{
			return new EditCompletorListener[]{};
		}
		public static EditCompletorListener[] getCSSCompletorList()
		{
			return new EditCompletorListener[]{};
		}
		public static EditCompletorListener[] getHTMLCompletorList()
		{
			return new EditCompletorListener[]{};
		}


	    public static EditCompletorListener getKeyBox()
		{
			return new myEditCompletorListener(){

				@Override
				public wordIcon[] finishSearchWord(Collection<CharSequence> words, Words WordLib)
				{
					return makeIcons(words,R.drawable.icon_key);
				}

				@Override
				public Collection<CharSequence> beforeSearchWord(Words Wordlib)
				{
					return Wordlib.getACollectionWords(words_key);
				}

				
			};
		}
		public static EditCompletorListener getConstBox()
		{
			return new myEditCompletorListener(){

				@Override
				public Collection<CharSequence> beforeSearchWord(Words WordLib)
				{
					return WordLib.getACollectionWords(words_const);
				}

				@Override
				public wordIcon[] finishSearchWord(Collection<CharSequence> words, Words WordLib)
				{
					return makeIcons(words,R.drawable.icon_default);
				}
				
			};
		}

		public static EditCompletorListener getVillBox()
		{
			return new myEditCompletorListener(){

				@Override
				public Collection<CharSequence> beforeSearchWord(Words WordLib)
				{
					return WordLib.getACollectionWords(words_variable);
				}

				@Override
				public wordIcon[] finishSearchWord(Collection<CharSequence> words, Words WordLib)
				{
					return makeIcons(words,R.drawable.icon_var);
				}
		
			};
		}


		public static EditCompletorListener getFuncBox()
		{
			return new myEditCompletorListener(){

				@Override
				public Collection<CharSequence> beforeSearchWord(Words WordLib)
				{
					return WordLib.getACollectionWords(words_func);
				}

				@Override
				public wordIcon[] finishSearchWord(Collection<CharSequence> words, Words WordLib)
				{
					return makeIcons(words,R.drawable.icon_func);
				}

				@Override
				public int onInsertWord(Editable editor,int index,CharSequence word)
				{
					int selection = super.onInsertWord(editor,index,word);
					if(editor.charAt(selection)!='(')
					    editor.insert(selection++,"(");
					return selection;
				}	
			};
		}


		public static EditCompletorListener getObjectBox()
		{
			return new myEditCompletorListener(){

				@Override
				public Collection<CharSequence> beforeSearchWord(Words WordLib)
				{
					// TODO: Implement this method
					return null;
				}

				@Override
				public wordIcon[] finishSearchWord(Collection<CharSequence> words, Words WordLib)
				{
					// TODO: Implement this method
					return null;
				}

				
			};
		}

		public static EditCompletorListener getTypeBox()
		{
			return new myEditCompletorListener(){

				@Override
				public Collection<CharSequence> beforeSearchWord(Words WordLib)
				{
					return WordLib.getACollectionWords(words_type);
				}

				@Override
				public wordIcon[] finishSearchWord(Collection<CharSequence> words, Words WordLib)
				{
					return makeIcons(words,R.drawable.icon_type);
				}
				
			};
		}

		public static EditCompletorListener getTagBox()
		{
			return new myEditCompletorListener(){

				@Override
				public Collection<CharSequence> beforeSearchWord(Words WordLib)
				{
					// TODO: Implement this method
					return null;
				}

				@Override
				public wordIcon[] finishSearchWord(Collection<CharSequence> words, Words WordLib)
				{
					// TODO: Implement this method
					return null;
				}
				

		
			};
		}

		public static EditCompletorListener getAttributeBox()
		{
			return new myEditCompletorListener(){

				@Override
				public Collection<CharSequence> beforeSearchWord(Words WordLib)
				{
					// TODO: Implement this method
					return null;
				}

				@Override
				public wordIcon[] finishSearchWord(Collection<CharSequence> words, Words WordLib)
				{
					// TODO: Implement this method
					return null;
				}
				

				
			};
		}
	}
	
	
	public static class WordsPackets implements WordsPacket
	{

		@Override
		public void SwitchWords(Words Lib, String Lua)
		{
			UnPackWords(Lua).loadWords(Lib);
		}

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
			public static final char[] fuhaox= new char[]{
				'(',')','{','}','[',']',
				'=',';',',','.',':',
				'+','-','*','/','%',
				'^','|','&','<','>','?','@',
				'!','~','\'','\n',' ','\t','#','"','\''
			};
			public static final char[] spiltx= new char[]{
				'\n',' ','\t','<','>',
			};
			public static final Collection<Character> fuhao = new HashSet<>();
			public static final Collection<Character> spilt = new HashSet<>();

			static{
				Arrays.sort(fuhaox);
				Arrays.sort(spiltx);
				CodeWords.copySet(fuhao,fuhaox);
				CodeWords.copySet(spilt,spiltx);
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
				Lib.setACollectionWords(words_key,keyword);
				Lib.setACollectionWords(words_const,constword);
				for(int i=words_func;i<=words_attr;++i){
					Lib.setACollectionWords(i,EmptyArray.emptyArray(CharSequence.class));
				}
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
				Lib.setACollectionWords(words_tag,IknowTag);
				Lib.setAMapWords(maps_zhu,zhu_key_value);
			}

		}

	}

	public static WordsPacket getWordsPacket(){
		return new WordsPackets();
	}
	public static ListenerFactory getDrawerFactory(){
		return new DrawerFactory();
	}
	public static ListenerFactory getCompletorFactory(){
		return new CompletorBoxes();
	}
	
	
	@Override
	public void trimListener(EditListenerInfo Info, String Lua)
	{
		if(Info!=null){
			getDrawerFactory().SwitchListener(Info,Lua);
			getCompletorFactory().SwitchListener(Info,Lua);
		}
	}

	@Override
	public void loadWords(Words WordLib, String Lua)
	{
		if(WordLib!=null){
			getWordsPacket().SwitchWords(WordLib,Lua);
		}
	}
	
}
