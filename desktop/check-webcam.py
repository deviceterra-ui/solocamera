import cv2,json,time
c=cv2.VideoCapture(5,cv2.CAP_DSHOW)
c.set(cv2.CAP_PROP_FRAME_WIDTH,1280);c.set(cv2.CAP_PROP_FRAME_HEIGHT,720)
frames=0;means=[];start=time.time()
while time.time()-start<8:
 ok,f=c.read()
 if ok:
  frames+=1;means.append(float(f.mean()))
  if frames==30:cv2.imwrite('desktop/virtual-webcam-test.jpg',f)
c.release()
r={'device':'OBS Virtual Camera','frames':frames,'seconds':round(time.time()-start,2),'mean_min':min(means) if means else 0,'mean_max':max(means) if means else 0}
open('desktop/virtual-webcam-test.json','w').write(json.dumps(r,indent=2));print(r)

