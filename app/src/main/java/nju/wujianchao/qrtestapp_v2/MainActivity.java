package nju.wujianchao.qrtestapp_v2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Button beginButton;

    private int imgCounts=500;
    private ArrayList<Integer> imgList=new ArrayList<Integer>();
    private int imgWidth;
    private  int imgHeight;

    private SyncBitmapStack bitmapStack=new SyncBitmapStack();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }


    //界面初始化工作
    private void initView() {
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder=surfaceView.getHolder();
        surfaceHolder.addCallback(new MyCallBack());

        beginButton = (Button) findViewById(R.id.beginButton);

        //定义按钮点击事件
        beginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开启加载图片线程
                new LoadImage().start();
                //开启绘图线程
                new DrawImage(0,0).start();
            }
        });
    }

    //surfaceholder 回调方法
    class MyCallBack implements SurfaceHolder.Callback{

        //创建surface界面
        @Override
        public void surfaceChanged(SurfaceHolder holder,int format,int width,int height){
            Log.i("Surface:","change");
        }

        //屏幕改变
        @Override
        public void surfaceCreated(SurfaceHolder holder){
            Log.i("Surface:","created");

            //加载图片资源编号
            for(int i=0;i<imgCounts;i++){
                int id = getResources().getIdentifier("a"+i, "drawable", "nju.wujianchao.qrtestapp_v2");
                imgList.add(id);
            }

            Bitmap bitmap= BitmapFactory.decodeResource(getResources(),imgList.get(0));
            imgWidth=bitmap.getWidth();
            imgHeight=bitmap.getHeight();

        }

        //surface退出
        @Override
        public void surfaceDestroyed(SurfaceHolder holder){
            Log.i("Surface:","destroy");
        }
    }

    //Bitmap栈 用于同步生产和消费线程
    class SyncBitmapStack{
        int index=0;
        Bitmap[] bitmapArray=new Bitmap[5];

        public synchronized void push(Bitmap bitmap){
            while(index==bitmapArray.length){
                try{
                    //让当前线程等待
                    this.wait();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            //唤醒在此对象上等待的线程
            this.notify();
            bitmapArray[index]=bitmap;
            this.index++;
        }

        public synchronized Bitmap pop(){
            while(index==0){
                try{
                    //让当前线程等待
                    this.wait();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            //唤醒在此对象上等待的线程
            this.notify();
            this.index--;
            return bitmapArray[index];
        }

    }

    //绘图线程
    class DrawImage extends Thread{
        int px,py;

        public DrawImage(int x,int y){
            px=x;
            py=y;
        }

        int index=0;
        long time;

        public void run(){
            while (true){

//                if(index==0){
//                time = System.currentTimeMillis();}

                Bitmap bitmap=bitmapStack.pop();

                //time = System.currentTimeMillis();

                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);

                Canvas canvas=surfaceHolder.lockCanvas(new Rect(this.px,this.py,this.px+imgWidth,this.py+imgHeight));

                Rect mSrcRect, mDestRect;
                mSrcRect = new Rect(this.px,this.py,this.px+imgWidth,this.py+imgHeight);
                mDestRect = new Rect(this.px,this.py,this.px+imgWidth,this.py+imgHeight);
                canvas.drawBitmap(bitmap, mSrcRect, mDestRect, paint);

                surfaceHolder.unlockCanvasAndPost(canvas);

//                if(index==48){
//                Log.i(TAG, "zzzzzzzzzzzzzzzzzzzzzzzzztime:" + (System.currentTimeMillis() - time));}
                //Log.i(TAG, "zzzzzzzzzzzzzzzzzzzzzzzzzdraw:"+ (System.currentTimeMillis() - time));

                index++;

            }
        }
    }

    //加载图片线程
    class LoadImage extends  Thread{
        int imgIndex=0;


        public void run(){
            while(true){

                //long time=System.currentTimeMillis();

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgList.get(imgIndex));

                //Log.i(TAG,"zzzzzzzzzzzzzzzzzzzzzzzzzload:"+(System.currentTimeMillis()-time));

                bitmapStack.push(bitmap);

                imgIndex++;
                if (imgIndex == imgList.size()) {
                    while(true){}
                }

            }

        }

    }


}
