package com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share;

import java.io.*;
import java.nio.*;

public class myLog
{
	private FileOutputStream writer;
	public myLog(File f){
		try
		{
			writer=new FileOutputStream(f);
		}
		catch (IOException e)
		{}
	}
	public myLog(String path){
		try
		{
			writer=new FileOutputStream(path);
		}
		catch (IOException e)
		{}
	}
	public <T> void e(T str,boolean to){
		try
		{
			writer.write((str.toString() + "\n").getBytes());
			if(to)
			    writer.flush();
		}
		catch (IOException e)
		{}
	}
	public void close(){
		try
		{
			writer.close();
		}
		catch (IOException e)
		{}
	}

	@Override
	protected void finalize() throws Throwable
	{
		// TODO: Implement this method
		super.finalize();
		writer.close();
	}

}



