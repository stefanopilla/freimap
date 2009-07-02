public class ThreadExists implements Runnable{
    public ThreadExists(){
        Thread ct=Thread.currentThread();
        ct.setName("Thread Principale");
        Thread t=new Thread(this, "Thread Figlio");
        System.out.println("Thread Attuale: " +ct);
        System.out.println("Thread figlio: " + t);
        t.start();
        try{
            Thread.sleep(3000);
        }catch(InterruptedException e){
            System.out.println("Thread Principale interrotto");
        }
            System.out.println("Uscita Thread Principale!");
        }

    public void run(){
        try {
            for( int i=5; i>0; i--){
                System.out.println(""+ i);
                Thread.sleep(1000);
            }
        }catch(InterruptedException e){
            System.out.println("Thread Figlio interrotto!");
        }
        System.out.println("uscita Thread figlio");
    }
    public static void main(String[] args){
        new ThreadExists();
    }
    }
 

