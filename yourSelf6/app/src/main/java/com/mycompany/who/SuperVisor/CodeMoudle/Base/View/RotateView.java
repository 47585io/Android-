package com.mycompany.who.SuperVisor.CodeMoudle.Base.View;

import android.content.*;
import android.graphics.*;
import android.view.*;

public class RotateView extends View
{

	public String path = "";
	public double angle=0;
	
	public RotateView(Context cont){
		super(cont);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		//从文件中读取图片数据
		Bitmap bitmap= BitmapFactory.decodeFile(path);
		Paint pen = new Paint();
		bitmap = Rotate(bitmap,angle);
		//将位图旋转指定的角度
		canvas.drawBitmap(bitmap,0,0,pen);
		//然后将位图画到View的画布上
		super.onDraw(canvas);
	}


	/*
	 旋转图片  

	 将每个像素点对应到此点旋转后的位置

	 */
	public static Bitmap Rotate(Bitmap src,double angle){
		Bitmap newBit = Expand(src,angle); //旋转前根据旋转角度创建一个足够大的位图
		int i,j;
		//遍历原位图所有点，按指定角度旋转后的坐标，将其设置到新的位图上
		for(i=0;i<src.getWidth();++i)
		{
			//获取当前列的点
			for(j=0;j<src.getHeight();++j)
			{
				//获取当前行的点
				pos p=new pos(i,j); //确定点在位图上的位置
				int pixel = src.getPixel(i,j); //根据位置获取位图上的点的颜色
				p = toDiderPos(p,src); //原图上的图像坐标转换为原图上的笛卡尔坐标
				p = rotatePixel(p,angle); //计算此点旋转后的新的坐标
				InsertPixel(p,angle,src,newBit); //插值
				p = toImagePos(p,newBit); //新的图上的笛卡尔坐标转换为新的图上的图像坐标
				newBit.setPixel((int)p.x,(int)p.y,pixel); //将颜色设置到指定的位置
			}
		}
		return newBit;
	}


	/*
	 创建一个更大的位图

	 计算图像旋转后的四个顶点坐标
	 然后让对角坐标相减，得到宽高
	 */
	public static Bitmap Expand(Bitmap map,double angle){

		pos leftTop = toDiderPos(new pos(0,0),map); 
		pos leftBottom = toDiderPos( new pos(0,map.getHeight()),map);
		pos rightTop = toDiderPos( new pos(map.getWidth(),0),map);
		pos rightBottom = toDiderPos( new pos(map.getWidth(),map.getHeight()),map);
		//图像四个顶点旋转前的坐标（笛卡尔坐标系）
		//先以图像坐标表示，然后转换为笛卡尔坐标

		leftTop = rotatePixel(leftTop,angle);
		leftBottom = rotatePixel(leftBottom,angle);
		rightTop = rotatePixel(rightTop,angle);
		rightBottom = rotatePixel(rightBottom,angle);
		//图像四个顶点旋转后的坐标（笛卡尔坐标系）

	    double width1 = Math.abs(leftTop.x-rightBottom.x);
	    double width2 = Math.abs(leftBottom.x-rightTop.x);
		double height1 = Math.abs(leftTop.y-rightBottom.y);
	    double height2 = Math.abs(leftBottom.y-rightTop.y);
		//然后让对角坐标相减，得到宽高

		int width = (int) Math.max(width1,width2)+1;
		int height = (int) Math.max(height1,height2)+1;
		//选出最大的

		return Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_4444);
	}


	/*
	 单个点P(x,y)的旋转后的点P1(x1,y1)的坐标只与点P的坐标和旋转角度β有关

	 两角和的余弦公式：
	 cos(α)cos(β)-sin(α)sin(β) = cos(α+β)
	 两角和的正弦公式:
	 cos(α)sin(β)+sin(α)cos(β) = sin(α+β)

	 设点与原点的距离为R
	 因为 x = Rcos(α)， y = Rsin(α)
	 x1 = Rcos(α+β)， y1 = Rsin(α+β)	

	 又因为 cos(α)cos(β)-sin(α)sin(β) = cos(α+β)
	 所以 x1 = R( cos(α)cos(β)-sin(α)sin(β) )
	 = Rcos(α)cos(β) -Rsin(α)sin(β)
	 = xcos(β) -ysin(β)
	 同理，y1 = xsin(β) +ycos(β)

	 */
	public static pos rotatePixel(pos before,double angle){
		pos after = new pos();
		double randAngle = Math.PI/180*angle; // 1度 = π/180弧度
		after.x = before.x*Math.cos(randAngle)-before.y*Math.sin(randAngle); //旋转后的x坐标
		after.y = before.x*Math.sin(randAngle)+before.y*Math.cos(randAngle); //旋转后的y坐标
		return after;
	}
	public static pos rotatePixel(double x,double y,double angle){
		pos after = new pos();
		double randAngle = Math.PI/180*angle; // 1度 = π/180弧度
		after.x = x*Math.cos(randAngle)-y*Math.sin(randAngle); //旋转后的x坐标
		after.y = x*Math.sin(randAngle)+y*Math.cos(randAngle); //旋转后的y坐标
		return after;
	}

	/*
	 获取点在旋转前的坐标

	 因为 x1 = xcos(β) -ysin(β)
	 所以 ysin(β) = -x1+xcos(β)
	 y = (-x1+xcos(β))/sin(β)

	 因为 y1 = xsin(β) +ycos(β)
	 所以 ycos(β) = y1-xsin(β)
	 y = (y1-xsin(β))/cos(β)

	 (-x1+xcos(β))/sin(β) = (y1-xsin(β))/cos(β)

	 两边各乘sin(β)cos(β)，得：
	 -x1cos(β) +xcos(β)^2 = y1sin(β) - xsin(β)^2
	 -x1cos(β) -y1sin(β) = -xcos(β)^2 -xsin(β)^2
	 x1cos(β) +y1sin(β) = xcos(β)^2 +xsin(β)^2
	 x1cos(β) +y1sin(β) = x (cos(β)^2 + sin(β)^2)
	 x = x1cos(β) +y1sin(β)

	 同理，y = y1cos(β)-x1sin(β)

	 */
	public static pos beforeRotatePixel(pos after,double angle){
		pos before = new pos();
		double randAngle = Math.PI/180*angle; // 1度 = π/180弧度
		before.x = after.x*Math.cos(randAngle)+after.y*Math.sin(randAngle);
		before.y = after.y*Math.cos(randAngle)-after.x*Math.sin(randAngle);
		return before;
	}
	public static pos beforeRotatePixel(double x,double y,double angle){
		pos before = new pos();
		double randAngle = Math.PI/180*angle; // 1度 = π/180弧度
		before.x = x*Math.cos(randAngle)+y*Math.sin(randAngle);
		before.y = y*Math.cos(randAngle)-x*Math.sin(randAngle);
		return before;
	}


	/*
	 插值

	 若坐标为小数，则必须获取其周围的整数点，然后逐个重新赋值，保证没有点遗漏
	 */
	public static void InsertPixel(pos after,double angle,Bitmap src,Bitmap newB){
		int x = (int) after.x;
		int y = (int) after.y;
		//当前点的近似位置
		for(int i=x-1;i<x+2;++i)
		{
			//获取当前周围一圈的点
			for(int j = y-1;j<y+2;++j)
			{
			    pos p = beforeRotatePixel(i,j,angle);
				//每拿到一个点，就求得此点在原图上的坐标
				p =toImagePos(p.x,p.y,src); 
				//然后再转化为原图上的图像坐标
				
				if(p.x>-1&&p.y>-1&&p.x<src.getWidth()&&p.y<src.getHeight())
				{
				    int pixel = src.getPixel((int)p.x,(int)p.y);
					//获取点的颜色
				    p = toImagePos(i,j,newB);
					//然后，将点设置在新的位图上的i,j位置
				    newB.setPixel((int)p.x,(int)p.y,pixel);
				}
				
			}
		}
	}


	/* 
	 以图像中点为原点，将图像坐标转换为笛卡尔坐标

	 x1 = x-0.5W 
	 y1 = -y+0.5H
	 (W和H分别为图像宽和高)

	 可以理解为，以图像中点为原点，将图像分为四份，
	 然后每个点的x坐标相当于左移0.5W
	 每个点的y坐标相当于将y轴翻转后上移0.5H（往上增加）

	 （这是因为图像坐标系以图片左上角为原点，向右x++，向下y++)
	 （可以看到，若把图像坐标系当成笛卡尔坐标系上的第四象限，x轴刚好符合，y轴却是反的)
	 （即每个点的x坐标可以直接移，但y坐标先取反才能上移，例如(100,100)，转换成笛卡尔坐标为(50,-50)

	 */
	public static pos toDiderPos(pos p,Bitmap map){
		pos np =  new pos();
		np.x = p.x-0.5*map.getWidth();  
		np.y = 0.5*map.getHeight()-p.y;
		return np;
	}
	public static pos toDiderPos(double x,double y,Bitmap map){
		pos np =  new pos();
		np.x = x-0.5*map.getWidth();  
		np.y = 0.5*map.getHeight()-y;
		return np;
	}
	
	/* 
	 以图像左上角为原点，将笛卡尔坐标转换为图像坐标

	 x1 = x+0.5W 
	 y1 = -y+0.5H

	 这个是上面的函数的逆运算，可以理解为，以图像左上角为原点
	 然后每个点的x坐标相当于右移0.5W
	 每个点的y坐标相当于将y轴翻转回来后下移0.5H （往下增加）

	 */
	public static pos toImagePos(pos p,Bitmap map){
		pos np =  new pos();
		np.x = p.x+(0.5*map.getWidth());
		np.y = (0.5*map.getHeight())-p.y;
		return np;
	}
	public static pos toImagePos(double x,double y,Bitmap map){
		pos np =  new pos();
		np.x = x+0.5*map.getWidth();
		np.y = 0.5*map.getHeight()-y;
		return np;
	}


	public static class pos{

		public pos(){}
		public pos(double x,double y){
			this.x=x;
			this.y=y;
		}

		public double x;
		public double y;
	}


}
