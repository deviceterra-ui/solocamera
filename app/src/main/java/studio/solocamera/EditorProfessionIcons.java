package studio.solocamera;
import android.graphics.*;
/** Fifty original, scalable profession pictograms. No font or downloaded logo dependency. */
final class EditorProfessionIcons {
 static void line(Canvas c,Paint p,float... xy){Path path=new Path();path.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)path.lineTo(xy[i],xy[i+1]);c.drawPath(path,p);}
 static void box(Canvas c,Paint p,float l,float t,float r,float b){c.drawRoundRect(l,t,r,b,1.3f,1.3f,p);}
 static void draw(Canvas c,int icon,RectF rect,int color,float glow){c.save();c.translate(rect.left,rect.top);c.scale(rect.width()/24,rect.height()/24);Paint p=new Paint(3);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setStrokeWidth(1.55f);if(glow>0)p.setShadowLayer(2.8f*glow,0,0,color);
 switch(icon){
 case 0:box(c,p,6,6,18,18);box(c,p,9,9,15,15);for(int i=8;i<=16;i+=4){line(c,p,i,2,i,6);line(c,p,i,18,i,22);line(c,p,2,i,6,i);line(c,p,18,i,22,i);}break;
 case 1:box(c,p,4,4,20,16);line(c,p,2,20,22,20,20,16,4,16,2,20);line(c,p,9,8,7,10,9,12);line(c,p,15,8,17,10,15,12);break;
 case 2:c.drawArc(2,3,22,23,220,100,false,p);c.drawArc(6,8,18,20,220,100,false,p);c.drawCircle(12,18,1,p);break;
 case 3:line(c,p,8,6,2,12,8,18);line(c,p,16,6,22,12,16,18);line(c,p,14,4,10,20);break;
 case 4:line(c,p,9,15,9,8,12,2,15,8,15,15,12,18,9,15);line(c,p,9,11,5,15,5,19,9,17);line(c,p,15,11,19,15,19,19,15,17);line(c,p,10,20,10,22);line(c,p,14,20,14,22);c.drawCircle(12,9,1.5f,p);break;
 case 5:Path leaf=new Path();leaf.moveTo(4,20);leaf.cubicTo(2,8,10,2,21,3);leaf.cubicTo(22,14,16,20,4,20);c.drawPath(leaf,p);line(c,p,4,20,17,7);line(c,p,10,14,10,8);line(c,p,10,14,16,14);break;
 case 6:line(c,p,12,3,12,22);for(int i=5;i<18;i+=4){line(c,p,12,i+3,6,i,6,i+3,12,i+6);line(c,p,12,i+3,18,i,18,i+3,12,i+6);}break;
 case 7:c.drawCircle(7,18,4,p);c.drawCircle(19,19,2.5f,p);line(c,p,3,14,3,10,11,10,14,16,21,16,21,12,16,12,13,4,8,4,8,10);break;
 case 8:c.drawCircle(12,12,4,p);for(int i=0;i<8;i++){double angle=i*Math.PI/4;line(c,p,12+(float)Math.cos(angle)*7,12+(float)Math.sin(angle)*7,12+(float)Math.cos(angle)*10,12+(float)Math.sin(angle)*10);}break;
 case 9:Path drop=new Path();drop.moveTo(12,2);drop.cubicTo(8,8,4,12,5,17);drop.cubicTo(7,24,19,23,19,16);drop.cubicTo(19,11,15,6,12,2);c.drawPath(drop,p);line(c,p,9,16,10,18,13,19);break;
 case 10:line(c,p,8,3,16,3,16,8,21,8,21,16,16,16,16,21,8,21,8,16,3,16,3,8,8,8,8,3);break;
 case 11:line(c,p,2,13,7,13,10,5,14,20,17,10,19,13,22,13);c.drawCircle(12,12,10,p);break;
 case 12:c.drawArc(4,1,14,15,0,180,false,p);line(c,p,4,8,4,3);line(c,p,14,8,14,3);line(c,p,9,14,9,17);c.drawArc(9,12,21,24,0,180,false,p);line(c,p,21,18,21,12);c.drawCircle(21,9,2.5f,p);break;
 case 13:box(c,p,4,3,20,21);line(c,p,6,18,18,6);line(c,p,6,8,8,6);line(c,p,16,18,18,16);break;
 case 14:c.drawArc(5,0,19,14,90,180,false,p);c.drawArc(5,10,19,24,270,180,false,p);c.drawArc(5,0,19,14,270,180,false,p);c.drawArc(5,10,19,24,90,180,false,p);for(int y=3;y<24;y+=5)line(c,p,8,y,16,y);break;
 case 15:line(c,p,12,5,7,3,2,4,2,19,7,18,12,20,17,18,22,19,22,4,17,3,12,5,12,20);break;
 case 16:line(c,p,2,9,12,3,22,9,12,15,2,9);line(c,p,6,12,6,18,12,21,18,18,18,12);line(c,p,22,9,22,18);break;
 case 17:line(c,p,4,20,5,14,17,2,22,7,10,19,4,20,5,14,10,19);line(c,p,15,4,20,9);break;
 case 18:c.drawCircle(12,9,6,p);line(c,p,9,15,9,19,15,19,15,15);line(c,p,10,22,14,22);line(c,p,12,3,12,1);break;
 case 19:c.drawCircle(12,12,10,p);c.drawOval(7,2,17,22,p);line(c,p,2,12,22,12);c.drawArc(3,5,21,15,0,180,false,p);break;
 case 20:box(c,p,2,7,22,21);line(c,p,8,7,8,3,16,3,16,7);line(c,p,2,12,12,16,22,12);line(c,p,12,12,12,17);break;
 case 21:c.drawCircle(12,12,10,p);c.drawCircle(12,12,6,p);c.drawCircle(12,12,2,p);line(c,p,12,12,22,2,22,7);line(c,p,22,2,17,2);break;
 case 22:line(c,p,3,3,3,21,22,21);line(c,p,6,16,10,11,14,14,21,5);line(c,p,16,5,21,5,21,10);break;
 case 23:c.drawCircle(12,5,3,p);c.drawCircle(4,17,3,p);c.drawCircle(20,17,3,p);line(c,p,10,8,6,14);line(c,p,14,8,18,14);line(c,p,7,17,17,17);break;
 case 24:box(c,p,5,2,19,22);for(int x=8;x<=16;x+=4)for(int y=5;y<=15;y+=5)line(c,p,x,y,x,y+1);line(c,p,10,22,10,18,14,18,14,22);break;
 case 25:line(c,p,2,8,12,2,22,8,2,8);for(int x=5;x<=19;x+=7)line(c,p,x,11,x,19);line(c,p,2,22,22,22);break;
 case 26:box(c,p,2,5,22,20);box(c,p,15,10,22,16);c.drawCircle(18,13,.6f,p);line(c,p,3,5,18,2,18,5);break;
 case 27:c.drawCircle(12,12,10,p);line(c,p,15,7,9,7,8,10,15,14,15,17,8,17);line(c,p,12,4,12,20);break;
 case 28:line(c,p,12,2,21,6,20,16,12,22,4,16,3,6,12,2);line(c,p,7,12,11,16,17,8);break;
 case 29:line(c,p,3,21,3,14,7,14,7,21);line(c,p,10,21,10,9,14,9,14,21);line(c,p,17,21,17,3,21,3,21,21);break;
 case 30:c.drawArc(2,2,12,12,60,280,false,p);c.drawArc(12,2,22,12,200,280,false,p);c.drawArc(7,0,17,10,170,200,false,p);line(c,p,6,10,6,21,18,21,18,10);line(c,p,6,17,18,17);break;
 case 31:line(c,p,5,2,5,9,10,9,10,2);line(c,p,7.5f,2,7.5f,22);line(c,p,19,2,15,10,19,12,19,22,19,2);break;
 case 32:box(c,p,3,8,17,19);c.drawArc(14,8,24,17,270,180,false,p);line(c,p,2,22,21,22);line(c,p,7,2,7,5);line(c,p,12,2,12,5);break;
 case 33:c.drawArc(3,6,21,24,180,180,false,p);line(c,p,1,16,23,16);line(c,p,12,7,12,4);line(c,p,10,4,14,4);break;
 case 34:box(c,p,2,7,22,20);c.drawArc(2,2,22,15,180,180,false,p);line(c,p,7,7,5,12);line(c,p,13,7,11,12);line(c,p,19,7,17,12);break;
 case 35:c.drawCircle(5,5,3,p);c.drawCircle(5,19,3,p);line(c,p,8,6,22,20);line(c,p,8,18,22,4);break;
 case 36:line(c,p,3,8,3,16);line(c,p,7,5,7,19);line(c,p,17,5,17,19);line(c,p,21,8,21,16);line(c,p,7,12,17,12);break;
 case 37:Path petal=new Path();petal.moveTo(12,20);petal.cubicTo(2,18,0,10,3,6);petal.cubicTo(9,6,12,10,12,20);petal.cubicTo(12,10,15,6,21,6);petal.cubicTo(24,10,22,18,12,20);c.drawPath(petal,p);line(c,p,12,20,12,3);break;
 case 38:box(c,p,3,3,21,9);for(int x=5;x<=20;x+=3)line(c,p,x,9,x,21);break;
 case 39:Path heart=new Path();heart.moveTo(12,22);heart.cubicTo(4,16,0,10,3,5);heart.cubicTo(6,1,10,3,12,6);heart.cubicTo(14,3,18,1,21,5);heart.cubicTo(24,10,20,16,12,22);c.drawPath(heart,p);line(c,p,4,12,8,12,10,8,13,16,15,12,20,12);break;
 case 40:c.drawArc(3,3,21,23,180,180,false,p);line(c,p,2,14,22,14,22,19,2,19,2,14);line(c,p,9,4,9,12);line(c,p,15,4,15,12);break;
 case 41:line(c,p,2,11,12,2,22,11);line(c,p,5,9,5,22,19,22,19,9);line(c,p,10,22,10,15,14,15,14,22);break;
 case 42:box(c,p,2,7,22,17);for(int x=5;x<22;x+=4)line(c,p,x,7,x,11);break;
 case 43:c.drawCircle(12,12,5,p);c.drawCircle(12,12,2,p);for(int i=0;i<8;i++){double ang=i*Math.PI/4;line(c,p,12+(float)Math.cos(ang)*7,12+(float)Math.sin(ang)*7,12+(float)Math.cos(ang)*10,12+(float)Math.sin(ang)*10);}break;
 case 44:line(c,p,7,22,7,2,20,2,2,9,22,9,7,2);line(c,p,18,9,18,17,15,17);line(c,p,4,22,10,22);break;
 case 45:box(c,p,2,6,22,21);line(c,p,7,6,9,2,15,2,17,6);c.drawCircle(12,13.5f,5,p);break;
 case 46:box(c,p,9,2,15,15);c.drawArc(5,6,19,20,0,180,false,p);line(c,p,12,20,12,23);line(c,p,8,23,16,23);break;
 case 47:c.drawCircle(12,12,10,p);line(c,p,9,6,18,12,9,18,9,6);break;
 case 48:c.drawArc(3,2,21,20,180,180,false,p);box(c,p,2,11,7,21);box(c,p,17,11,22,21);break;
 case 49:c.drawCircle(12,12,10,p);for(int i=0;i<6;i++){double a=i*Math.PI/3;line(c,p,12+(float)Math.cos(a)*10,12+(float)Math.sin(a)*10,12+(float)Math.cos(a+1)*4.5f,12+(float)Math.sin(a+1)*4.5f);}break;
 }c.restore();}
}
