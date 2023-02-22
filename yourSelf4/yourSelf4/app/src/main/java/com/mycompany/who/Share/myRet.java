package com.mycompany.who.Share;

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
		byte[] buf=new byte[(int)f.length()];
		String src = null;
		try
		{
			reader.read(buf);
		}
		catch (IOException e)
		{}
		try
		{
			src= new String(buf, decode);
		}
		catch (UnsupportedEncodingException e)
		{}
		return src;
	}

	public void close(){
		try
		{
			reader.close();
		}
		catch (IOException e)
		{}
	}
}
