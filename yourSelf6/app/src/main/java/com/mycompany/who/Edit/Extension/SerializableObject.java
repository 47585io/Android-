package com.mycompany.who.Edit.Extension;
import java.io.*;


/*
 transient关键字的主要作用就是让某些被transient关键字修饰的成员属性变量不被序列化

 Java提供了一种对象序列化的机制。用一个字节序列可以表示一个对象，该字节序列包含该对象的数据、对象的类型和对象中存储的属性等信息。
 
 字节序列写出到文件之后，相当于文件中持久保存了一个对象的信息。
 
 反之，该字节序列还可以从文件中读取回来，重构对象，对它进行反序列化。对象的数据、对象的类型和对象中存储的数据信息，都可以用来在内存中创建对象

 整个过程都是 Java 虚拟机（JVM）独立的，也就是说，在一个平台上序列化的对象可以在另一个完全不同的平台上反序列化该对象。

 类 ObjectInputStream 和 ObjectOutputStream 是高层次的数据流，它们包含反序列化和序列化对象的方法。

 ObjectOutputStream 类包含很多写方法来写各种数据类型，但是一个特别的方法例外：
   public final void writeObject(Object x) throws IOException

 上面的方法序列化一个对象，并将它发送到输出流。相似的 ObjectInputStream 类包含如下反序列化一个对象的方法：
   public final Object readObject() throws IOException, ClassNotFoundException

 该方法从流中取出下一个对象，并将对象反序列化。它的返回值为Object，因此，你需要将它转换成合适的数据类型。
 
*/
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
