package com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share;
import android.widget.*;
import android.net.*;
import android.webkit.*;
import com.mycompany.who.*;
import com.mycompany.who.Edit.*;
import java.io.*;

public class Share
{
	public static void setEdit(CodeEdit Edit,String name){
		//如果是一个文本文件，编辑器该采用怎样的染色和提示呢？
		int icon= getFileIcon(name);
		if(icon== R.drawable.file_type_java
		   ||icon== R.drawable.file_type_js
		   ||icon== R.drawable.file_type_c
		   ||icon== R.drawable.file_type_cpp
		   ||icon== R.drawable.file_type_h){
			Edit.setLuagua("java");
		}
		else if(icon== R.drawable.file_type_xml){
			Edit.setLuagua("xml");
		}
		else if(icon== R.drawable.file_type_css){
			Edit.setLuagua("css");
		}
		else if(icon== R.drawable.file_type_html){
			Edit.setLuagua("html");
		}
		else{
			Edit.setLuagua("text");
		}

	}

	public static int getFileIcon(String src){
		if(src.equals("夹"))
			return R.drawable.folder;
		else if(src.equals("打开夹"))
			return R.drawable.folder_open;
		int index= src.lastIndexOf('.');
		if(src.substring(index+1,src.length()).toLowerCase().equals("bak")){
			return getFileIcon(src.substring(0,index));
		}	
	    src = src.substring(index+1,src.length()).toLowerCase();

		if(src.equals("html"))
			return R.drawable.file_type_html;
		else if(src.equals("xml"))
			return R.drawable.file_type_xml;
		else if(src.equals("java"))
			return R.drawable.file_type_java;
		else if(src.equals("txt"))
			return R.drawable.file_type_txt;
		else if(src.equals("jpg")||src.equals("jpeg")||src.equals("png")||src.equals("gif"))
			return R.drawable.file_type_pic;
		else if(src.equals("h"))
			return R.drawable.file_type_h;
		else if(src.equals("c"))
			return R.drawable.file_type_c;
		else if(src.equals("cpp"))
			return R.drawable.file_type_cpp;
		else if(src.equals("css"))
			return R.drawable.file_type_css;
		else if(src.equals("js"))
			return R.drawable.file_type_js;


		return R.drawable.file_type_unknown;
	}

	public static int getFileIcon(File f){
		if(f.isDirectory())
			return R.drawable.folder;

		return getFileIcon(f.getName());
	}

	public static boolean isImage(String name){	
		name=name.toLowerCase();
		if(name.endsWith(".jpg")||name.endsWith("jpeg")||name.endsWith(".png")||name.endsWith(".gif"))
			return true;
		return false;
	}
	public static boolean isGif(String name){
		name=name.toLowerCase();
		if(name.endsWith(".gif"))
			return true;
		return false;
	}
	public static boolean unknowFile(String name){
		if(getFileIcon(name)==R.drawable.file_type_unknown)
		    return true;
		return false;
	}
	public static boolean isTextFile(String name){
		int resid= getFileIcon(name);
		if(resid==R.drawable.folder||resid==R.drawable.folder_open||resid==R.drawable.file_type_unknown||resid==R.drawable.file_type_pic)
			return false;
		return true;
	}

	
	
	public static void setImage(ImageView v,String name){
		Uri.Builder h=new Uri.Builder();
		h.path(name);
		Uri b=h.build();
		v.setImageURI(b);	
	}

	public static void setToWeb(WebView web,String path){
		WebSettings setting = web.getSettings();
		setting.setDomStorageEnabled(true);
		setting.setAllowFileAccess(true);
		setting.setAllowFileAccessFromFileURLs(true);
		web.loadUrl(path);
	}
}
