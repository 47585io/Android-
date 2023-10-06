package com.editor;

import java.io.*;

public class myReader
{
	
	private static final int GC_Meomry = 5*1024*1024;

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
			if(reader!=null&&f!=null)
			{
				byte[] buf=new byte[(int)f.length()];
			    reader.read(buf);
			    src = new String(buf, decode);
			}
		}
		catch (Exception e){}
		return src;
	}
	
	public void rr(Reader r) throws IOException
	{
		int len = (int) f.length();
		int now = 0;
		byte[] buf = new byte[GC_Meomry];
		
		while(now<len)
		{
			int count = now+GC_Meomry>len ? len-now:GC_Meomry;
			reader.read(buf,0,count);
			r.r(buf,0,count);
			now += count;
			reader.skip(count);
		}
		reader.mark(0);
	}

	public void close()
	{
		try{
			if(reader!=null)
			    reader.close();
		}
		catch (IOException e){}
	}
	
	public static interface Reader
	{
		public void r(byte[] buf,int index,int count)
	}
	
}
