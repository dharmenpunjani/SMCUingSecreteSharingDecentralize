package secretesharingdecentralize;



import java.util.Random;

import Jama.Matrix;
import java.io.BufferedReader;
import java.io.FileReader;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

public class Shamir
{
	int nParties;
	int id;
	int coEf[];
	//double shares[];
	int pubVals[];
	static double pArray [][];
        static double sumOfParties[];
	public Shamir(int n,int num)
	{
		nParties = n;
		coEf = new int[n-1];
		
		pubVals = new int[n];// holds Public values for all the parties
                sumOfParties = new double[n];
                sumOfParties[0] = 0.0; //  holds the sum of shares of all parties
		pArray = new double[n][n]; //  holds the coeef of the final equation to solve
		id = num;
	}
	
	public void genParray()
	{
		for(int i=0;i<nParties;i++)
		{
			for(int j=0;j<nParties-1;j++)
			{
				pArray[i][j] = Math.pow(pubVals[i], nParties-j-1);
			}
			pArray [i][nParties-1] = 1;
		}
	}
	
	public void setPubVal(int n[])
	{
		for(int i=0;i<n.length;i++)
		{
			pubVals[i] = n[i];
		}
	}
	
	public void genCoEf()
	{
		System.out.println("Coefficient for Party: "+id);
		if(nParties<2)
			return;
		for(int i=0;i<nParties-1;i++)
		{
			Random r = new Random(100+i+1);
			coEf [i] = r.nextInt()*(int)System.nanoTime() % 15;
			if(coEf[i]<0)
				coEf[i] *=-1;
			if(coEf[i]==0)
			{
				i--;
				continue;
			}
			System.out.println(coEf[i]+"::");
		}
	}
	
//	public void genShareFor(double share[][],int n)
//	{
//            for (double[] share1 : share) {
//                for (int j = 0; j < share1.length; j++) {
//                    for (int k = 0; k<coEf.length; k++) {
//                        share1[j] += coEf[k]*(Math.pow(n, nParties-k-1));
//                    }
//                }
//            }
//	}
	
	public void genShare(double sec,double shares[])
	{
		for(int i=0;i<shares.length;i++)
		{
			shares[i] = 0;
			for(int j=0;j<coEf.length;j++)
			{
				shares[i] += coEf[j]*(Math.pow(pubVals[i], nParties-j-1));
			}
			shares[i] += sec;
			shares[i] = (double)Math.round(shares[i] * 1000) / 1000;
                        sumOfParties[i] += shares[i];
			System.out.println((i+1)+" 'th share of the secret from "+id+":"+shares[i] );
		}
	}
	
	public static void main(String s[])
	{
            BufferedReader br=null;
            try {
                
                String data="";
                br = new BufferedReader(new FileReader("/inputSmc.txt"));// give the values in this file
                String line = "";
                while((line=br.readLine())!=null)
                {
                    data +=line+";";
                }
                br.close();
                String []allData = data.split(";");
                
                if(allData.length!=2)
                {
                    System.out.println(" Please specify proper input in file");
                    System.exit(0);
                }
                
                String pubVals[] = allData[0].split(",");
                String secrets[] = allData[1].split(",");
                
                if(pubVals.length!=secrets.length)
                {
                    System.out.println(" Please specify proper input values in file");
                    System.exit(0);
                }
                
                int numParties = pubVals.length;
                Shamir sh[] = new Shamir[numParties];
                
                int n[] = new int[pubVals.length];
                double sec[] = new double[secrets.length];
                
                for(int i=0;i<pubVals.length;i++)
                {
                    n[i] = Integer.parseInt(pubVals[i]);
                   sec[i] = Integer.parseInt(secrets[i]);
                }
                double share[] = new double[numParties];
                for(int i=0;i<numParties;i++)
                {
                    sh[i] = new Shamir(numParties,i);
                    sh[i].setPubVal(n);
                    sh[i].genCoEf();
                }
                for(int i=0;i<numParties;i++)
                {
                    sh[i].genShare(sec[i],share);// generates shares of secerets 
                }
                for(int i=0;i<numParties;i++)
                {
                    System.out.println("The secerete reconstructed by party "+(i+1)+" is :" );
                    sh[i].reConstruct();// reconstructs the secerets from shares
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } 
	}
	
	public void reConstruct()
	{
                genParray();
                
                // [A] [X] = [B]
                // if public values for the parties are 3, 4 , 2 then the equation to be solved will be as followed
                //[ 9 3 1, 16 4 1, 4 2 1] [x, x, x] = [ sum of the shares of party1, sum of the shares of party2, sum of the shares of party3] 
		Matrix A = new Matrix(pArray);
		double[][] B = new double[sumOfParties.length][1]; 
              
                for(int i=0;i<sumOfParties.length;i++)
                {
                    B[i][0] = sumOfParties[i];
                }
		Matrix b = new Matrix(B);
		Matrix x = A.solve(b);
		
		System.out.println((int)x.get(x.getRowDimension()-1, x.getColumnDimension()-1) + " ");
	}
}


