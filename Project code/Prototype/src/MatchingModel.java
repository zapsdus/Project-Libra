import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

public class MatchingModel {

	public static void main(String[] args) {
		List<String> Male_ID = getlist("data\\Male.csv",0);
		List<String> Male_Age = getlist("data\\Male.csv",1);
		//...
		List<String> Female_ID = getlist("data\\Female.csv",0);
		List<String> Female_Age = getlist("data\\Female.csv",1);
		//...
		int MaleNumber=Male_ID.size()-1;
		int FemaleNumber=Male_ID.size()-1;
		if (MaleNumber==FemaleNumber) {
			System.out.println("Equal number of males and females, please proceed.");
		}
		
		//Some ways to generate preferences here. 
		int[][] MalePref=new int[MaleNumber][FemaleNumber];
		int[][] FemalePref=new int[FemaleNumber][MaleNumber];
		double[][] Distance=new double[MaleNumber][FemaleNumber];
		//Currently, I use random generation.
		for (int i=0;i<MaleNumber;i++) {
			int[] result1 = generateRandomPermutation(FemaleNumber);
			for (int j=0;j<FemaleNumber;j++) {
				MalePref[i][j]=result1[j]+0;
				Distance[i][j]=Math.random()*10.0;
				//System.out.println(Distance[i][j]);
			}
		}
		for (int i=0;i<FemaleNumber;i++) {
			int[] result2 = generateRandomPermutation(MaleNumber);
			for (int j=0;j<MaleNumber;j++) {
				FemalePref[i][j]=result2[j]+0;
			}
		}
		
		//I will add stable matching model constraints later
	       try {
	        	IloCplex cplex=new IloCplex();
	        	//variables
		        	IloNumVar[][] x=new IloNumVar[MaleNumber][]; 
		        	for (int i=0;i<MaleNumber;i++) {
		        		x[i]=cplex.boolVarArray(FemaleNumber);
		        	}
		        //objective
		        	IloLinearNumExpr objective = cplex.linearNumExpr();
		        	//cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, 1.0e-5);
		        	
		        //constraints

					cplex.setParam(IloCplex.Param.Simplex.Display, 0);
		        	
		        //solve model
		        	cplex.end();     
	        }
	        catch (IloException exc) {
	        	exc.printStackTrace();
	        }
		
	
	}
	
	public static List<String> getlist(String csvFile, int ID) {
		List<String> names = new ArrayList<String>();
       	//csvFile = "data\\InboundTrain.csv";
        String line = "";
        String cvsSplitBy = ",";
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] country = line.split(cvsSplitBy);
                names.add(country[ID]);
            }
            //names.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
		return names;
	} 
	
	static Random rand = new Random();
	static int[] generateRandomPermutation(int n) {
	    int[] res = new int[n];
	    for (int i = 0; i < n; i++) {
	        int d = rand.nextInt(i+1);
	        res[i] = res[d];
	        res[d] = i;
	    }
	    return res;
	}
	
	
}
