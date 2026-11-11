package studio.solocamera;

import android.content.ContentResolver;
import android.content.Context;
import android.app.ActivityManager;
import android.graphics.*;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import com.google.android.gms.tflite.java.TfLite;
import com.google.android.gms.tasks.Tasks;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.InterpreterApi;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/** Optional, on-device creator AI models. Model bytes remain in the app-private models folder. */
final class EditorAi {
    static final String MODEL_NAME="selfie_segmentation.tflite";
    static final long MODEL_ESTIMATE=249L*1024;
    private static final long MAX_MODEL_BYTES=512L*1024;
    private static final String MODEL_URL="https://storage.googleapis.com/mediapipe-assets/selfie_segmentation.tflite";
    interface DownloadProgress {boolean update(long read,long total);}
    static final class SubjectPoint {final long at;final float x,y,height;SubjectPoint(long at,float x,float y,float height){this.at=at;this.x=x;this.y=y;this.height=height;}}
    private EditorAi(){}

    static File modelFile(Context context){return new File(new File(context.getFilesDir(),"models"),MODEL_NAME);}
    static boolean installed(Context context){File f=modelFile(context);if(!f.isFile()||f.length()<100_000||f.length()>MAX_MODEL_BYTES)return false;try(RandomAccessFile input=new RandomAccessFile(f,"r")){input.seek(4);return input.readByte()=='T'&&input.readByte()=='F'&&input.readByte()=='L'&&input.readByte()=='3';}catch(IOException e){return false;}}
    static void download(Context context,DownloadProgress progress)throws Exception{
        File dir=modelFile(context).getParentFile();if(!dir.exists()&&!dir.mkdirs())throw new IOException("Could not create model storage");File temp=new File(dir,MODEL_NAME+".download");HttpsURLConnection connection=null;
        try{connection=(HttpsURLConnection)new URL(MODEL_URL).openConnection();connection.setConnectTimeout(20000);connection.setReadTimeout(60000);connection.setInstanceFollowRedirects(false);connection.setRequestProperty("Accept","application/octet-stream");int status=connection.getResponseCode();if(status!=200)throw new IOException("Model host returned HTTP "+status);long expected=connection.getContentLengthLong();if(expected>MAX_MODEL_BYTES)throw new IOException("Model file exceeded the safe download limit");long read=0;try(InputStream in=new BufferedInputStream(connection.getInputStream());OutputStream out=new BufferedOutputStream(new FileOutputStream(temp))){byte[] buffer=new byte[8192];int count;while((count=in.read(buffer))!=-1){read+=count;if(read>MAX_MODEL_BYTES)throw new IOException("Model file exceeded the safe download limit");out.write(buffer,0,count);if(progress!=null&&!progress.update(read,expected))throw new InterruptedIOException("Download cancelled");}}if(expected>0&&read!=expected)throw new IOException("Download ended before the model was complete");if(read<100_000||!hasTfliteIdentifier(temp))throw new IOException("Downloaded file is not a valid LiteRT model");File target=modelFile(context);if(target.exists()&&!target.delete())throw new IOException("Could not replace the previous model");if(!temp.renameTo(target))throw new IOException("Could not save the downloaded model");
        }finally{if(connection!=null)connection.disconnect();if(temp.exists())temp.delete();}
    }
    private static boolean hasTfliteIdentifier(File file)throws IOException{try(RandomAccessFile input=new RandomAccessFile(file,"r")){input.seek(4);return input.readByte()=='T'&&input.readByte()=='F'&&input.readByte()=='L'&&input.readByte()=='3';}}

    static File removePhotoBackground(Context context,Uri uri)throws Exception{
        if(!installed(context))throw new FileNotFoundException("Download the photo cutout model first");
        ActivityManager activityManager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);ActivityManager.MemoryInfo memory=new ActivityManager.MemoryInfo();if(activityManager!=null){activityManager.getMemoryInfo(memory);if(memory.availMem<memory.threshold||memory.availMem<128L*1024*1024)throw new IOException("Not enough free memory for this photo effect. Close other apps and try again.");}
        if(Build.VERSION.SDK_INT>=29){PowerManager power=(PowerManager)context.getSystemService(Context.POWER_SERVICE);if(power!=null&&power.getCurrentThermalStatus()>=PowerManager.THERMAL_STATUS_SEVERE)throw new IOException("This phone is too hot to safely run the photo effect. Let it cool, then try again.");}
        Tasks.await(TfLite.initialize(context));
        InterpreterApi.Options options=new InterpreterApi.Options().setRuntime(InterpreterApi.Options.TfLiteRuntime.FROM_SYSTEM_ONLY).setNumThreads(1);
        try(InterpreterApi model=InterpreterApi.create(modelFile(context),options)){
            int[] inputShape=model.getInputTensor(0).shape(),outputShape=model.getOutputTensor(0).shape();
            if(inputShape.length!=4||inputShape[0]!=1||inputShape[1]!=256||inputShape[2]!=256||inputShape[3]!=3||model.getInputTensor(0).dataType()!=DataType.FLOAT32||outputShape.length!=4||outputShape[0]!=1||outputShape[1]!=256||outputShape[2]!=256||outputShape[3]!=1||model.getOutputTensor(0).dataType()!=DataType.FLOAT32)throw new IOException("This model version has an unsupported input or output format");
            Bitmap source=decodePhoto(context.getContentResolver(),uri);Bitmap square=Bitmap.createBitmap(256,256,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(square);canvas.drawColor(Color.BLACK);float scale=Math.min(256f/source.getWidth(),256f/source.getHeight());float width=source.getWidth()*scale,height=source.getHeight()*scale,left=(256-width)/2f,top=(256-height)/2f;Paint draw=new Paint(Paint.FILTER_BITMAP_FLAG|Paint.ANTI_ALIAS_FLAG);canvas.drawBitmap(source,null,new RectF(left,top,left+width,top+height),draw);
            int[] sample=new int[256*256];square.getPixels(sample,0,256,0,0,256,256);ByteBuffer input=ByteBuffer.allocateDirect(256*256*3*4).order(ByteOrder.nativeOrder());for(int pixel:sample){input.putFloat(Color.red(pixel)/255f);input.putFloat(Color.green(pixel)/255f);input.putFloat(Color.blue(pixel)/255f);}input.rewind();float[][][][] mask=new float[1][256][256][1];model.run(input,mask);
            int w=source.getWidth(),h=source.getHeight();int[] pixels=new int[w*h];source.getPixels(pixels,0,w,0,0,w,h);for(int y=0;y<h;y++){int my=Math.max(0,Math.min(255,(int)(top+(y+.5f)*scale)));for(int x=0;x<w;x++){int mx=Math.max(0,Math.min(255,(int)(left+(x+.5f)*scale)));float confidence=Math.max(0,Math.min(1,mask[0][my][mx][0]));int foreground=Math.max(0,Math.min(255,Math.round((confidence-.12f)/.78f*255)));int index=y*w+x,color=pixels[index];pixels[index]=Color.argb(Color.alpha(color)*foreground/255,Color.red(color),Color.green(color),Color.blue(color));}}
            source.recycle();square.recycle();Bitmap result=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);result.setPixels(pixels,0,w,0,0,w,h);File outputDir=new File(context.getFilesDir(),"editor-images");if(!outputDir.exists()&&!outputDir.mkdirs()){result.recycle();throw new IOException("Image storage unavailable");}File output=new File(outputDir,"cutout-"+java.util.UUID.randomUUID()+".png");try(FileOutputStream stream=new FileOutputStream(output)){if(!result.compress(Bitmap.CompressFormat.PNG,100,stream))throw new IOException("Could not encode transparent cutout");}finally{result.recycle();}return output;
        }
    }
    static ArrayList<SubjectPoint> trackPerson(Context context,Uri uri,long sourceStartMs,long durationMs)throws Exception{
        if(!installed(context))throw new FileNotFoundException("Download the optional person model first");
        if(durationMs<400)throw new IOException("This clip is too short to analyze");
        checkDevice(context);
        Tasks.await(TfLite.initialize(context));
        InterpreterApi.Options options=new InterpreterApi.Options().setRuntime(InterpreterApi.Options.TfLiteRuntime.FROM_SYSTEM_ONLY).setNumThreads(1);
        try(InterpreterApi model=InterpreterApi.create(modelFile(context),options)){
            validateModel(model);MediaMetadataRetriever retriever=new MediaMetadataRetriever();ArrayList<SubjectPoint> points=new ArrayList<>();
            try{retriever.setDataSource(context,uri);int samples=Math.max(2,Math.min(12,(int)Math.ceil(durationMs/1800.0)+1));for(int i=0;i<samples;i++){checkDevice(context);long at=durationMs*i/(samples-1);Bitmap frame=retriever.getFrameAtTime((sourceStartMs+at)*1000,MediaMetadataRetriever.OPTION_CLOSEST);if(frame==null)continue;try{SubjectPoint point=locateSubject(frame,model,at);if(point!=null)points.add(point);}finally{frame.recycle();}}}
            finally{try{retriever.release();}catch(Exception ignored){}}
            return points;
        }
    }
    private static void checkDevice(Context context)throws IOException{ActivityManager manager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);ActivityManager.MemoryInfo memory=new ActivityManager.MemoryInfo();if(manager!=null){manager.getMemoryInfo(memory);if(memory.availMem<memory.threshold||memory.availMem<160L*1024*1024)throw new IOException("Not enough free memory to analyze video. Close other apps and try again.");}if(Build.VERSION.SDK_INT>=29){PowerManager power=(PowerManager)context.getSystemService(Context.POWER_SERVICE);if(power!=null&&power.getCurrentThermalStatus()>=PowerManager.THERMAL_STATUS_SEVERE)throw new IOException("This phone is too hot to analyze video. Let it cool, then try again.");}}
    private static void validateModel(InterpreterApi model)throws IOException{int[] in=model.getInputTensor(0).shape(),out=model.getOutputTensor(0).shape();if(in.length!=4||in[0]!=1||in[1]!=256||in[2]!=256||in[3]!=3||model.getInputTensor(0).dataType()!=DataType.FLOAT32||out.length!=4||out[0]!=1||out[1]!=256||out[2]!=256||out[3]!=1||model.getOutputTensor(0).dataType()!=DataType.FLOAT32)throw new IOException("This model version has an unsupported input or output format");}
    private static SubjectPoint locateSubject(Bitmap source,InterpreterApi model,long at)throws IOException{Bitmap square=Bitmap.createBitmap(256,256,Bitmap.Config.ARGB_8888);try{Canvas canvas=new Canvas(square);canvas.drawColor(Color.BLACK);float scale=Math.min(256f/source.getWidth(),256f/source.getHeight()),w=source.getWidth()*scale,h=source.getHeight()*scale,left=(256-w)/2f,top=(256-h)/2f;canvas.drawBitmap(source,null,new RectF(left,top,left+w,top+h),new Paint(Paint.FILTER_BITMAP_FLAG|Paint.ANTI_ALIAS_FLAG));int[] pixels=new int[256*256];square.getPixels(pixels,0,256,0,0,256,256);ByteBuffer input=ByteBuffer.allocateDirect(pixels.length*3*4).order(ByteOrder.nativeOrder());for(int pixel:pixels){input.putFloat(Color.red(pixel)/255f);input.putFloat(Color.green(pixel)/255f);input.putFloat(Color.blue(pixel)/255f);}input.rewind();float[][][][] mask=new float[1][256][256][1];model.run(input,mask);double sx=0,sy=0,weight=0,minX=256,minY=256,maxX=0,maxY=0;for(int y=0;y<256;y+=2)for(int x=0;x<256;x+=2){if(x<left||x>=left+w||y<top||y>=top+h)continue;float a=mask[0][y][x][0];if(a<.55f)continue;sx+=x*a;sy+=y*a;weight+=a;minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);}if(weight<1000||maxY<=minY)return null;float cx=(float)((sx/weight-left)/w),cy=(float)((sy/weight-top)/h),height=(float)((maxY-minY)/h);if(cx<0||cx>1||cy<0||cy>1)return null;return new SubjectPoint(at,cx,cy,height);}finally{square.recycle();}}
    private static Bitmap decodePhoto(ContentResolver resolver,Uri uri)throws IOException{return ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver,uri),(decoder,info,source)->{int w=info.getSize().getWidth(),h=info.getSize().getHeight();double scale=Math.min(1,1600.0/Math.max(w,h));decoder.setTargetSize(Math.max(1,(int)Math.round(w*scale)),Math.max(1,(int)Math.round(h*scale)));decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);});}
}
