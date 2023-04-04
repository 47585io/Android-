package com.mycompany.who.Edit.Base;

import java.util.*;
import com.mycompany.who.*;

public class Words
{
	//所有单词
	protected CharSequence[] keyword = new String[]{
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

	protected CharSequence[] IknowTag= new String[]{	"*",
		"html",
		"body",
		"head",
		"title",
		"a",
		"img",
		"audio",
		"input",
		"b",
		"sup",
		"i",
		"small",
		"font",
		"em",
		"strong",
		"sub",
		"ins",
		"del",
		"code",
		"kbd",
		"samp",
		"var",
		"pre",
		"abbr",
		"address",
		"bdo",
		"blockquote",
		"cite",
		"q",
		"dfn",
		"p",
		"div",
		"ul",
		"li",
		"ol",
		"dl",
		"dt",
		"dd",
		"hr",
		"br",
		"h1",
		"h2",
		"h3",
		"h4",
		"h5",
		"h6",
		"main",
		"xmp",
		"style",
		"script",
		"link",
		"textarea",
		"button",
		"select",
		"option",
		"optgroup",
		"picture",
		"source",
		"map",
		"area",
		"table",
		"tr",
		"th",
		"td",
		"caption",
		"from",
		"label",
		"fieldset",
		"legend",
		"datalist",
		"keygen",
		"output",
		"span",
		"frame",
		"iframe",
		"object",
		"embed",
		"marguee",
		"canvas",
		"video",
		"base",
		"meta",
		"details",
		"summary",
		"mark",
		"noscript",
		"figure",
		"svg",
		"circle",
		"line",
		"polyline",
		"rect",
		"ellipse",
		"polygon",
		"path",
		"text",
		"use",
		"g",
		"defs",
		"pattern",
		"animate",
		"image",
		"animateTransform",};

	protected CharSequence[] constword = new String[]{"null","true","false","NaN","NULL"};
	public static char[] fuhao= new char[]{
		'(',')','{','}','[',']',
		'=',';',',','.',':',
		'+','-','*','/','%',
		'^','|','&','<','>','?','@',
		'!','~','\'','\n',' ','\t','#'
	};
	public static char[] spilt= new char[]{
        '\n',' ','\t','<','>',
	};

	public static Map<CharSequence,CharSequence> zhu_key_value;

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
	}

}
