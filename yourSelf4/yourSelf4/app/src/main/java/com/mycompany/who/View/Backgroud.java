package com.mycompany.who.View;
import android.graphics.drawable.*;
import android.graphics.*;
import android.content.res.*;

public class Backgroud extends Drawable
{
	
	Paint mPaint;
	Bitmap bit;

	public Backgroud(String picture){
		mPaint = new Paint();
		bit= BitmapFactory.decodeFile(picture);
	}
	public Backgroud(Resources rouse,int id){
		
		mPaint = new Paint();
		bit = BitmapFactory.decodeResource(rouse,id);
	}
	
	@Override
	public void draw(Canvas p1)
	{
		Rect rect = getBounds();
		int width = bit.getWidth();
		int height= bit.getHeight();
		if(rect.width()>rect.height()&&width<height) //横屏且宽高不匹配，旋转270度
			bit=getRotateBitmap(bit,270);
		else if(rect.width()<rect.height()&&width>height) //竖屏且宽高不匹配，旋转90度
			bit=getRotateBitmap(bit,90);
			//每一轮次旋转360度
		p1.drawBitmap(bit,0,0,mPaint);
	}

	@Override
	public void setAlpha(int p1)
	{
		mPaint.setAlpha(p1);
	}
	
	public static Bitmap clearColor(int color,Bitmap bit){
		Bitmap newbit= bit.copy(Bitmap.Config.ARGB_8888,true);
		int i,j; 
		try{
			for(i=0;i<newbit.getHeight();i++){
				for(j=0;j<newbit.getWidth();j++){
					int c= newbit.getPixel(j,i);
					if(c==color)
						newbit.setPixel(j,i,0x00000000);
				}
			}
		}catch(Exception e){}
		return newbit;
	}

	@Override
	public void setColorFilter(ColorFilter p1)
	{
		// TODO: Implement this method
	}

	public Bitmap getBitmap(){
		return bit;
	}
	public void setBitmap(Bitmap bit){
		this. bit = bit;
	}
	
	@Override
	public int getOpacity()
	{
		// TODO: Implement this method
		return 0;
	}
	
	
	// 水平翻转图像，也就是把镜中像左右翻过来
    public static Bitmap getFlipBitmap(Bitmap bitmap) {
        Matrix matrix = new Matrix(); // 创建操作图片用的矩阵对象
        matrix.postScale(-1, 1); // 执行图片的旋转动作
        // 创建并返回旋转后的位图对象
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
								   bitmap.getHeight(), matrix, false);
    }
// 获得比例缩放之后的位图对象
    public static Bitmap getScaleBitmap(Bitmap bitmap, double scaleRatio) {
        Matrix matrix = new Matrix(); // 创建操作图片用的矩阵对象
        matrix.postScale((float) scaleRatio, (float) scaleRatio);
        // 创建并返回缩放后的位图对象
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
								   bitmap.getHeight(), matrix, false);
    }


// 获得旋转角度之后的位图对象
    public static Bitmap getRotateBitmap(Bitmap bitmap, float rotateDegree) {
        Matrix matrix = new Matrix(); // 创建操作图片用的矩阵对象
        matrix.postRotate(rotateDegree); // 执行图片的旋转动作
        // 创建并返回旋转后的位图对象
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
								   bitmap.getHeight(), matrix, false);
    }

}
