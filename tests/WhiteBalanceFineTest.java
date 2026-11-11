package studio.solocamera;

public final class WhiteBalanceFineTest {
    public static void main(String[] args){
        float[] neutral=WhiteBalanceFine.gains(2,1,1.1f,2,0);
        if(neutral[0]!=2||neutral[1]!=1||neutral[2]!=1.1f||neutral[3]!=2)throw new AssertionError("Neutral must preserve camera gains exactly");
        float previousRed=0,previousBlue=Float.MAX_VALUE;
        for(int step=-1000;step<=1000;step++){
            float[] gains=WhiteBalanceFine.gains(2,1,1.1f,2,step/10.0);
            if(gains[0]<previousRed||gains[3]>previousBlue||gains[0]<1||gains[0]>3||gains[3]<1||gains[3]>3||gains[1]!=1||gains[2]!=1.1f)throw new AssertionError("Nonmonotonic or invalid gains");
            previousRed=gains[0];previousBlue=gains[3];
        }
        float[] cool=WhiteBalanceFine.gains(2,1,1,2,-50),warm=WhiteBalanceFine.gains(2,1,1,2,50);
        if(!(cool[0]<2&&cool[3]>2&&warm[0]>2&&warm[3]<2))throw new AssertionError("Warm/cool direction");
        System.out.println("PASS fine WB neutral baseline, 2001 continuous steps, warm/cool direction and gain bounds");
        for(double value:new double[]{Double.NaN,Double.POSITIVE_INFINITY,-101,101}){
            try{WhiteBalanceFine.gains(2,1,1,2,value);throw new AssertionError("Invalid adjustment accepted");}catch(IllegalArgumentException expected){}
        }
        try{WhiteBalanceFine.gains(0,1,1,2,0);throw new AssertionError("Invalid baseline accepted");}catch(IllegalArgumentException expected){}
        System.out.println("PASS invalid white-balance values rejected before capture request");
    }
}
