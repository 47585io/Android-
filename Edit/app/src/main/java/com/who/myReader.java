package com.who;

import java.io.*;

public class myReader
{
	private FileInputStream reader;
	private File f;

	public myReader(String path)
	{
		try{
			reader=new FileInputStream(path);
			f=new File(path);
		}
		catch (FileNotFoundException e){}
	}
	public myReader(File f)
	{
		try{
			reader=new FileInputStream(f);
			this.f=f;
		}
		catch (FileNotFoundException e){}
	}

	public String r(String decode)
	{
		String src = "";
		try
		{
			if(reader!=null&&f!=null){
				byte[] buf=new byte[(int)f.length()];
			    reader.read(buf);
			    src = new String(buf, decode);
			}
		}
		catch (Exception e){}
		return src;
	}

	public void close()
	{
		try{
			if(reader!=null)
			    reader.close();
		}
		catch (IOException e){}
	}
}
