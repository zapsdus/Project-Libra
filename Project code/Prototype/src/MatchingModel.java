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
		int[][] MalePref=new int[MaleNumber][FemaleNumber];//Preference value, not rank
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
		        	for (int i=0; i<MaleNumber; i++) {	        		
		        		for (int j=0; j<FemaleNumber; j++) {
		        			objective.addTerm(Distance[i][j],x[i][j]);
		        		}
		        	}
		        	cplex.addMinimize(objective);
		        //constraints
					cplex.setParam(IloCplex.Param.Simplex.Display, 0);
		        	for (int i=0; i<MaleNumber; i++) {
		        		IloLinearNumExpr expr1 = cplex.linearNumExpr();
		        		for (int j=0; j<FemaleNumber; j++) {
		        			expr1.addTerm(1.0,x[i][j]);
		        		}
		        		cplex.addEq(expr1, 1);
		        	}
		        	for (int j=0; j<FemaleNumber; j++) {
		        		IloLinearNumExpr expr2 = cplex.linearNumExpr();
			        	for (int i=0; i<MaleNumber; i++) {
		        			expr2.addTerm(1.0,x[i][j]);
		        		}
		        		cplex.addEq(expr2, 1);
		        	}
		        	
		        	for (int i=0; i<MaleNumber; i++) {
			        	for (int j=0; j<FemaleNumber; j++) {
			        		IloLinearNumExpr expr3 = cplex.linearNumExpr();
				        	for (int k=0; k<MaleNumber; k++) {
				        		if (FemalePref[j][k]<FemalePref[j][i]) {
				        			expr3.addTerm(1.0,x[k][j]);
				        		}
			        		}
				        	IloLinearNumExpr expr4 = cplex.linearNumExpr();
				        	for (int k=0; k<FemaleNumber; k++) {
				        		if (MalePref[i][k]<MalePref[i][j]) {
				        			expr4.addTerm(1.0,x[i][k]);
				        		}
			        		}
				        	cplex.addLe(cplex.sum(x[i][j], expr3,expr4),1);   		
			        	}			        	
		        	}
		        //solve model
		        	if (cplex.solve()) {
		        		//double obj = cplex.getObjValue();
			        	for (int i=0; i<MaleNumber; i++) {
				        		double[] Tempx=cplex.getValues(x[i]);	
				        		for (int j=0; j<FemaleNumber; j++) {
				        			if (Tempx[j]==1.0) {
				        				int i_result=i+1;
				        				int j_result=j+1;
				        				System.out.println("A match of male and female: ("+i_result+","+j_result+")");
				        				//System.out.println(Tempx[j]);
				        			}
				        			
				        		}
				        }
		        		//Calculate the percentage of connected cars!
		        	}
		        	else {
		        		System.out.println("problem not solved");
		        	}
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
