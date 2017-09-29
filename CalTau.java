package duthieuthucte;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
public class CalTau {
	protected File f1 ;
	protected File f2 ;
	protected Scanner input1;
	protected Scanner input2;
	protected FileWriter fw;
	protected double timenext;
	public CalTau(){
		f1 = new File("lambda _20_8_2017.csv");
		f2 = new File("muy.csv");
		
		try{
			input1 = new Scanner(f1);
			input2 = new Scanner(f2);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void  writeOnFile(int queueCap,int hostNum,double alpha,double theta,String fileName){
		int number = 0;
		try {
			fw = new FileWriter(fileName);
			while(input1.hasNext()){
				double lamda = Double.parseDouble(input1.nextLine())/600;
				double muy = 1/Double.parseDouble(input1.nextLine());
				calculateTau(queueCap, hostNum, lamda, muy, alpha, theta);
//				System.out.println("muy ="+muy);
//				System.out.println("lambda="+lambda);
				
				//System.out.println(timenext+"\n");
				number++;
				
				fw.write(timenext+"\n");
			}
			System.out.println("number"+number);
			System.out.println("done");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public  void calculateTau(int jobsqueuecapacity,int hostNum,double lamda,double muy,double alpha,double theta){
	//	System.out.println("dang calculate");
//		NewHelper.reset();
//        NewHelper.setAlpha(alpha);
//        
//        NewHelper.setMuy(muy);
//       NewHelper.setLamda(lamda);;
//       NewHle
	//	System.out.println("muy+"+NewHelper.muy);
		//changeLamd
	    int K = jobsqueuecapacity;
		int c = hostNum;
		 double pi[][] = new double[hostNum+1][jobsqueuecapacity+1];
		 double a[][] = new double[hostNum+1][jobsqueuecapacity+1];  
		 double b[][] = new double[hostNum+11][jobsqueuecapacity+1];  
		 double s1[][] = new double[hostNum+1][jobsqueuecapacity+1];
		for(int i = 0; i<= c; i++){
			for(int j = 0;j <= K; j++){
				pi[i][j] = 0;
				a[i][j] = 0;
				b[i][j] =0;
				s1[i][j] =0;
			}
		}
		pi[0][0] = 1;
		int s = c;
		// i = 0
		b[0][K] = (lamda)/(theta*K+s*alpha);
		for(int j  = K-1; j>=1; j--){
				b[0][j] = lamda/(lamda+ min(j, s, c)*alpha
						+j*theta- (j+1)*theta*b[0][j+1]);
		}
		for(int j  = 1; j <= K; j++){
			pi[0][j] =	b[0][j]*pi[0][j-1];
				
		}
		//i =1 	
		for(int j = 1 ; j <= K ; j++){
		// tinh pi[1][1]
			pi[1][1] =pi[1][1]+min(j,c,s)*alpha*pi[0][j]/muy;
		}
		a[1][K] = Math.min(c,s)*alpha*pi[0][K]/(muy+Math.min(c-1, s)*alpha
				+(K-1)*theta);
		b[1][K] =  lamda/(muy+Math.min(c-1, s)*alpha+
				(K-1)*theta);
		for(int j = K-1; j>=2 ; j--){
			a[1][j] =((muy+j*theta)*a[1][j+1]+min(j,c,s)*alpha*
					pi[0][j])/(muy+lamda+min(j-1,c-1,s)*alpha+
							(j-1)*theta-(muy+j*theta)*b[1][j+1]);
			
			b[1][j] = lamda/(muy+lamda+min(j-1,c-1,s)*alpha+
					(j-1)*theta-(muy+j*theta)*b[1][j+1]);
		}
		for(int j = 2 ; j <= K ; j++){
			pi[1][j] = a[1][j]+ b[1][j]*pi[1][j-1];
		}
		// i = 2 den c-1
		for(int i = 2 ; i < c; i++ ){
			for(int j = i; j<= K;j++){
				pi[i][i] = pi[i][i] +( min(j-i+1,c-i+1,s)*alpha*pi[i-1][j])/(muy*i);
			}
			a[i][K] = Math.min(c-i+1, s)*alpha*pi[i-1][K]/(Math.min(c-i, s)*alpha+
					i*muy+(K-i)*theta);
			b[i][K] = lamda/(Math.min(c-i, s)*alpha+
					i*muy+(K-i)*theta);
			
			for(int j = K-1; j >= i+1; j--){
				s1[i][j] = lamda + min(j-i, c-i, s)*alpha+muy*i+
						(j-i)*theta;
				
				a[i][j] =((i*muy+(j-i+1)*theta)*a[i][j+1]+min(j-i+1,c-i+1,s)*alpha*
						pi[i-1][j])/(s1[i][j]-(i*muy+(j+1-i)*theta)*b[i][j+1]);
				b[i][j] = lamda/(s1[i][j]-(i*muy+(j+1-i)*theta)*b[i][j+1]);
				
			}
			for(int j = i+1 ; j<=K; j++){
				pi[i][j] = a[i][j]+b[i][j]*pi[i][j-1];
			}
		}	
	   // i = c
		for(int j = c; j<= K;j++){
			pi[c][c] = (pi[c][c] + min(j-c+1,1,s)*alpha*pi[c-1][j])/(muy*c);
		}
		a[c][K] =	 alpha*pi[c-1][K]/(c*muy+(K-c)*theta);
		b[c][K] = lamda/(c*muy+(K-c)*theta);
		for(int j = K-1; j >= c+1 ; j--){
			a[c][j]=((c*muy+(j+1-c)*theta)*a[c][j+1]+alpha*pi[c-1][j])/
					(lamda+ c*muy+(j-c)*theta-(c*muy
							+(j-c+1)*theta)*b[c][j+1]	);
			b[c][j] = lamda/(lamda+ c*muy+(j-c)*theta-(c*muy
					+(j-c+1)*theta)*b[c][j+1]);
			
			
					
		}
		for(int j= c+1;j <= K ; j++){
			pi[c][j] = a[c][j]+pi[c][j-1]*b[c][j];
		}
		
		// tinh To
		double  temp = 0;
		for(int i  = 0; i <= c; i++ ){
			for(int j = i; j <= K; j++){
				temp = temp + pi[i][j];
				
			}
			//System.out.println("temp  ="+temp);
		}
		
		
		// tinh pi[0][0]
		pi[0][0] = 1/temp;
		double temp2 = pi[0][0];
		//System.out.println("temp = "+temp);
		
		for(int i = 0; i<= c; i++){
			for(int j = i;j <= K; j++){
				if(j!=0) pi[i][j] =  pi[i][j]*temp2;
			}
		}
		temp2 = 0;
		for(int i = 0; i <= c; i++)
			for(int j = i+1; j<= K ; j++)
				temp2 = temp2 + pi[i][j]*min(c-i,j-i,s);
		timenext =  1/(alpha*temp2);
		
    	
    }
    public static int min(int a, int b ,int c){
		int min =a;
		if(b < min) min = b;
		if(c < min) min = c;
		return min;
	}
    public static int min(int a,int b){
    	int min = a;
    	if(b<a) min =b;
    	return min;
    }
    
	
		
	
	public static void main(String args[]){
		int queueCap = Integer.parseInt(args[0]);
		int hostNum = Integer.parseInt(args[1]);
		double lamda = Double.parseDouble(args[2]);
		double muy = Double.parseDouble(args[3]);
		double alpha = Double.parseDouble(args[4]);
		double theta = Double.parseDouble(args[5]);
		//String fileName = args[5];
		CalTau t = new CalTau();
		//t.writeOnFile(queueCap,hostNum,alpha,theta,fileName);
		t.calculateTau(queueCap, hostNum, lamda, muy, alpha, theta);
		//t.calculatetimenextversion2();
		System.out.println("timenext:"+t.timenext);
	}
}
