package com.mycompany.who.Edit.Base.Share.Share1;

/*
 jvm将对象所在的class文件加载到方法区中
 jvm读取main方法入口，将main方法入栈，执行创建对象代码
 在main方法的栈内存中分配对象的引用，在堆中分配内存放入创建的对象，并将栈中的引用指向堆中的对象
 
 所以当对象在实例化完成之后，是被存放在堆内存中的，这里的对象由3部分组成，
   对象头：对象头存储的是对象在运行时状态的相关信息、指向该对象所属类的元数据的指针，如果对象是数组对象那么还会额外存储对象的数组长度
   实例数据：实例数据存储的是对象的真正有效数据，也就是各个属性字段的值，如果在拥有父类的情况下，还会包含父类的字段，字段的存储顺序会受到数据类型长度、以及虚拟机的分配策略的影响对齐
   填充字节：在java对象中，需要对齐填充字节的原因是，64位的jvm中对象的大小被要求向8字节对齐，因此当对象的长度不足8字节的整数倍时，需要在对象中进行填充操作
 (因此如果实例数据没有对齐，那么需要进行对齐补全空缺，补全的bit位仅起占位符作用，不具有特殊含义)
 
 内存结构从上到下分别为：
   8字节  对象头
   4字节  自己的指针
   (4字节  数组长度)
   实例数据
 (开启指针压缩后每个引用类型占4字节，否则8字节，默认开启)
 
 实例数据保存的是对象真正存储的有效信息，保存了代码中定义的各种数据类型的字段内容，并且如果有继承关系存在，子类还会包含从父类继承过来的字段
 在内存中，属性的排列顺序与在类中定义的顺序不同，这是因为jvm会采用字段重排序技术，对原始类型进行重新排序，以达到内存对齐的目的，具体规则遵循如下：
   ①按照数据类型的长度大小，从大到小排列
   ②具有相同长度的字段，会被分配在相邻位置
   ③如果一个字段的长度是L个字节，那么这个字段的偏移量需要对齐至nL的地址处
   ④char和byte类型的变量会被提到前面进行前置补位，避免浪费
   ⑤当一个类拥有父类时，在父类中定义的变量整体出现在子类中定义的变量之前的位置
 
 例如wordIndex，它的内存为:
   8字节  对象头    偏移量为0  (8的倍数)
   4字节  自己指针大小  偏移量为8 (4的倍数)
   4字节  start    偏移量为12 (4的倍数)
   4字节  end      偏移量为16 (4的倍数)
   4字节  span指针  偏移量为20 (4的倍数)
 (这样所有成员的地址间没有缝隙，而且刚好占满了24字节，3*8=24，因此也不需要再将整个对象的空间填充到8的倍数了)
*/
public class wordIndex extends size
{
	//单个词的范围和Span
	public Object span;
	
	public wordIndex(){}
	public wordIndex(int start,int end,Object span){
		this.start=start;
		this.end=end;
		this.span=span;
	}
	public wordIndex(wordIndex o){
		start = o.start;
		end = o.end;
		span = o.span;
	}

	public void set(int s,int e,Object span){
		this.start=s;
		this.end=e;
		this.span=span;
	}
	public void set(wordIndex node){
		this.start=node.start;
		this.end=node.end;
		this.span=node.span;
	}
	
}
