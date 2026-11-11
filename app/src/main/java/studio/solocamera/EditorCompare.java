package studio.solocamera;
final class EditorCompare {
 static double split(EditorProject.Clip c,long local){if(c.comparisonMode!=3)return c.comparisonSplit;double t=EditorProject.bound(local/(double)Math.max(200,c.comparisonDuration),0,1);return t*t*(3-2*t);}
 static double alpha(double x,double y,EditorProject.Clip c,long local){double axis=c.comparisonMode==2?y:x,edge=split(c,local)*2-1;double f=.006,t=EditorProject.bound((axis-edge+f)/(2*f),0,1);return 1-t*t*(3-2*t);}
}
