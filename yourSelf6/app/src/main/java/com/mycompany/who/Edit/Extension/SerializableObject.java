package com.mycompany.who.Edit.Extension;
import java.io.*;

public abstract class SerializableObject implements Serializable
{
	
	public SerializableObject(){
		onInit();
	}
	
	public String Delete() throws FileNotFoundException, IOException
	{	
		save();
		FileOutputStream out = new FileOutputStream(getPath());
		ObjectOutputStream objOut = new ObjectOutputStream(out);
		objOut.writeObject(this);
		clear();
		return getPath();
	}

	public static Object decodeFile(String path) throws FileNotFoundException, IOException, ClassNotFoundException
	{
		FileInputStream in = new FileInputStream(path);
		ObjectInputStream objIn = new ObjectInputStream(in);
		Object obj = objIn.readObject();
		return obj;
	}
	
	abstract public void onInit()
	
	abstract public String getPath()
	
	abstract protected void clear()
	
	abstract protected void save()
	
}
