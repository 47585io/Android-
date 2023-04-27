package com.mycompany.who.SuperVisor.CodeMoudle.Base.View.Share;

import java.io.*;

public class myRet
{
	private FileInputStream reader;
	private File f;
	
	public myRet(String path){
		try
		{
			reader=new FileInputStream(path);
			f=new File(path);
		}
		catch (FileNotFoundException e)
		{}
	}
	public myRet(File f){
		try
		{
			reader=new FileInputStream(f);
			this.f=f;
		}
		catch (FileNotFoundException e)
		{}
	}

	public String r(String decode){
		String src = "";
		try
		{
			if(reader!=null&&f!=null){
				byte[] buf=new byte[(int)f.length()];
			    reader.read(buf);
			    src= new String(buf, decode);
			}
		}
		catch (Exception e)
		{}
		return src;
	}

	public void close(){
		try
		{
			if(reader!=null)
			    reader.close();
		}
		catch (IOException e)
		{}
	}
}
