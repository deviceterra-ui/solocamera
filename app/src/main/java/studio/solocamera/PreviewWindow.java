package studio.solocamera;
/** Four bounded in-flight frames preserve cadence without an unbounded socket backlog. */
final class PreviewWindow {
 private final long[] sequences={-1,-1,-1,-1},started=new long[4];
 synchronized boolean acquire(long id,long now){for(int i=0;i<4;i++)if(sequences[i]==-1){sequences[i]=id;started[i]=now;return true;}return false;}
 synchronized boolean acknowledge(long id){for(int i=0;i<4;i++)if(sequences[i]==id){sequences[i]=-1;return true;}return false;}
 synchronized boolean waiting(){for(long sequence:sequences)if(sequence==-1)return false;return true;}
 synchronized boolean expired(long now){for(int i=0;i<4;i++)if(sequences[i]!=-1&&now-started[i]>3_000_000_000L)return true;return false;}
 synchronized void reset(){java.util.Arrays.fill(sequences,-1);}
}

