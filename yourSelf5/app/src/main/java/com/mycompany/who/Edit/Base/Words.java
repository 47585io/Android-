package com.mycompany.who.Edit.Base;

import java.util.*;
import com.mycompany.who.Edit.Base.Share.Share3.*;

public class Words
{
	//所有单词
	private CharSequence[] keyword = new String[]{
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
		"Override","Deprecated","SuppressWarnings","SafeVarargs","FunctionalInterface","param",

		"delete","typeof","var","in","instanceof","debugger","export","with","let","yield","volatile","function",

		"auto","bool","wchar_t","signed","unsigned","union","struct","sizeof","typeid","typedef","virtual","operator",
		"constexpr","using","namespace","inline","friend","template","noexcept","static_cast","const_cast","dynamic_cast","reinterpret_cast",
		"register","explicit","extern",
	};

	private CharSequence[] IknowTag= new String[]{	"*",
		"html","body","head","title","a","img","audio","input","b","sup","i","small","font","em","strong","sub",
		"ins","del","code","kbd","samp","var","pre","abbr","address","bdo","blockquote","cite","q","dfn","p","div","ul","li","ol","dl","dt","dd",
		"hr","br","h1","h2","h3","h4","h5","h6","main","xmp","style","script","link","textarea",
		"button","select","option","optgroup","picture","source","map","area","table","tr","th","td","caption","from","label","fieldset","legend",
		"datalist","keygen","output","span","frame","iframe","object","embed","marguee","canvas","video","base","meta","details","summary",
		"mark","noscript","figure","svg","circle","line","polyline","rect","ellipse","polygon","path","text","use","g","defs","pattern","animate","image","animateTransform",};

	private CharSequence[] constword = new String[]{"null","true","false","NaN","NULL"};
	public static char[] fuhao= new char[]{
		'(',')','{','}','[',']',
		'=',';',',','.',':',
		'+','-','*','/','%',
		'^','|','&','<','>','?','@',
		'!','~','\'','\n',' ','\t','#','"','\''
	};
	public static char[] spilt= new char[]{
        '\n',' ','\t','<','>',
	};

	public static Map<CharSequence,CharSequence> zhu_key_value;
	
	public static final int words_func = 0;
	public static final int words_vill = 1;
	public static final int words_obj = 2;
	public static final int words_type = 3;
	public static final int words_tag = 4;
	public static final int words_attr = 5;
	public static final int words_key=6;
	public static final int words_const=7;

	public List<Collection<CharSequence>> mdates;
	//支持保存Span单词，但可能有一些异常
	
	static{
		zhu_key_value= Collections.synchronizedMap(new HashMap<>());
		sort();
		zhu_key_value.put("//","\n");
		zhu_key_value.put("/*","*/");
		zhu_key_value.put("<!--","-->");
	}

	public static void sort(){
		Arrays.sort(fuhao);
		Arrays.sort(spilt);
	}

	public Words(){
		Arrays.sort(keyword);
		Arrays.sort(constword);
		Arrays.sort(IknowTag);
		mdates=Collections.synchronizedList(new ArrayList<>());
		add(6);
		init();
	}

	public void init(){
		add(Array_Splitor.toList(keyword));
		add(Array_Splitor.toList(constword));
		mdates.get(words_tag).addAll(Array_Splitor.toList(IknowTag));
		IknowTag=null;
		keyword=null;
		constword=null;
		//单词转移
	}

	public void add(int size){
		while(size-->0){
			Collection<CharSequence> col=Collections.synchronizedSet(new HashSet<>());
			mdates.add(col);
			//每个集合都是安全的
		}
	}
	public void add(Collection<CharSequence> words){
		Collection<CharSequence> col=Collections.synchronizedCollection(words);
		mdates.add(col);
	}

	public Collection<CharSequence> get(int index){
		return mdates.get(index);
	}
	public void set(int index,Collection<CharSequence> words){
		mdates.set(index,words);
	}
	public void clear(){
		for(Collection t:mdates)
		    t.clear();
	}
	public int size(){
		return mdates.size();
	}
	
	public Collection<CharSequence> getKeyword(){
		return mdates.get(words_key);
	}
	public Collection<CharSequence> getConstword(){
		return mdates.get(words_const);
	}
	public static char[] getFuhao(){
		return fuhao;
	}
	public static char[] getSpilt(){
		return spilt;
	}
	public static Map<CharSequence,CharSequence> get_zhu(){
		return zhu_key_value;
	}
	public Collection<CharSequence> getLastfunc(){
		return mdates.get(words_func);
	}
	public Collection<CharSequence> getHistoryVillber(){
		return mdates.get(words_vill);
	}
	public Collection<CharSequence> getThoseObject(){
		return mdates.get(words_obj);
	}
	public Collection<CharSequence> getBeforetype(){
		return mdates.get(words_type);
	}
	public Collection<CharSequence> getTag(){
		return mdates.get(words_tag);
	}
	public Collection<CharSequence> getAttribute(){
		return mdates.get(words_attr);
	}

	
}
