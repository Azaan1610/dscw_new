package com.example.grpc.client.grpcclient;
import com.example.grpc.client.grpcclient.uploadController;

import com.example.grpc.server.grpcserver.PingRequest;
import com.example.grpc.server.grpcserver.PongResponse;
import com.example.grpc.server.grpcserver.PingPongServiceGrpc;
import com.example.grpc.server.grpcserver.MatrixRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.*;
import com.example.grpc.server.grpcserver.MatrixReply;
import com.example.grpc.server.grpcserver.MatrixServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
@Service
public class GRPCClientService {
	
	//the matrix file saved on my local host (refers to my git repo)
	String MatrixFilePath = "/home/azaanzafar1610/dscw_new";

	//lab 2 stuff
    public String ping() {
        	ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();        
		PingPongServiceGrpc.PingPongServiceBlockingStub stub
                = PingPongServiceGrpc.newBlockingStub(channel);        
		PongResponse helloResponse = stub.ping(PingRequest.newBuilder()
                .setPing("")
                .build());        
		channel.shutdown();        
		return helloResponse.getPong();
    }

	public uploadController upload(@RequestParam("file") MultipartFile file, @RequestParam("deadline")int deadline) throws IOException{
		
		String nameOfFile = file.getOriginalFilename(); // get file name of matrix
		String fileContentType = file.getContentType();

		//here we create a new file in the root destination 
		File fileDestination = new File(MatrixFilePath + '/' + nameOfFile);

		if (!fileDestination.getParentFile().exists())  fileDestination.getParentFile().mkdirs(); 
                    
        
                try { file.transferTo(fileDestination); }
                catch (Exception e) { return new uploadController(nameOfFile, fileContentType, "add a file please " + e.getMessage()); }
			
		
				// this is where the functionality goes for reading in the file and then converting it into a 2d int[][] matrix
		BufferedReader reader;
		reader = new BufferedReader(new FileReader(fileDestination));
		//read in the file from the destination above

		String firstDimension = reader.readLine();
		String[] split = firstDimension.split(" ");
		int firstX = Integer.parseInt(split[0]); // get the row
		int firstY = Integer.parseInt(split[0]);// get the column
		int[][] first = new int[firstX][firstY]; // make a new 2d int array called first 

		for (int i = 0; i < firstX; i++) { //here we iterate through the FIRST matrix in the file and convert it to a 2d int[][] matrix array
			String[] line = reader.readLine().split(" ");
			for (int j = 0; j < firstY; j++) {
				first[i][j] = Integer.parseInt(line[j]);
				}
		}

		// Read "@" from the matrix file
		reader.readLine(); //this is in place to differentiate the first matrix from the second and vice versa
		String secondDimension = reader.readLine();
		String[] split2 = secondDimension.split(" ");
		int secX = Integer.parseInt(split2[0]);
		int secY = Integer.parseInt(split2[0]);
		int[][] second = new int[secX][secY];

		for (int i = 0; i < secX; i++) {
			String[] line = reader.readLine().split(" ");
			for (int j = 0; j < secX; j++) {
				second[i][j] = Integer.parseInt(line[j]);
				}
			}

		int[][] matrixA = first; //create a new 2d int array caled matrixA and store the 'first' matrix in it from the code above
		int[][] matrixB = second;
		
		//check size of both matrices to see if they are square
		String checkSize = sizeCheck(matrixA, matrixB);
		if (checkSize != "Equal Size :)"){
			return new uploadController(nameOfFile, fileContentType, checkSize);
		}

		//given we know that the matrix is a square matrix from the checks above, lets verify if they are power of 2
		String checkPower = powerCheck(matrixA, matrixB);
		if (checkPower != "power of 2 :)"){
			return new uploadController(nameOfFile, fileContentType, checkPower);
		}

		//if all checks are successful then upload the file 
		return new uploadController(nameOfFile, fileContentType, "All checks passed, File Uploaded");
		



	}

	//class for validating the size of the matrices (both should be sqaure)
	public static String sizeCheck(int[][]matrixA, int[][]matrixB){
		int matrixA_row1 = matrixA.length; // checks the length of the row for matrix a (eg how many elements in the row)
		int matrixA_col1 = matrixA[0].length; // checks the first column [0] of matrixA (eg how many elements in that col)
		String reply;
		int matrixB_row1 = matrixB.length;
		int matrixB_col1 = matrixB[0].length;

		//here we compare to see if both matrices have same length of row and column by splitting eg matrixB[0] gets us the column in the first row
		if(matrixA_row1!=matrixB_row1 || matrixA_col1!=matrixB_col1){
				reply = "Error: Both matrices have to be SQUARE and equal in dimension!";
		}
		else{
				reply = "Equal Size :)";
		}
		return reply;
	}

	public static String powerCheck(int[][]matrixA, int[][]matrixB){
		int matrixA_row1 = matrixA.length;
		String reply;
		int matrixB_row1 = matrixB.length;


		//here we are checking if the rows of each matrix return an EVEN number for log base of 2
		double matrixA_powerCheck = (Math.log(matrixA_row1)/Math.log(2));
        double matrixB_powerCheck = (Math.log(matrixB_row1)/Math.log(2));

		//if it returns an even number then we know that the matrix is a power of 2
		if (matrixA_powerCheck%2 ==0 && matrixB_powerCheck%2==0){ //checking if returned value is even or not
			reply = "power of 2 :)";
		}
		else{
			reply = "Not power of 2";
		}
		return reply;

	}

	public void matrixCalc(int[][]matrixA, int[][]matrixB, int deadline){//use deadline when doing that part

		
		ManagedChannel channel1 = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub1 = MatrixServiceGrpc.newBlockingStub(channel1);
		
		//these are the 8 different servers with their respective stubs
		ManagedChannel channel2 = ManagedChannelBuilder.forAddress("34.134.1.234", 9090).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub2 = MatrixServiceGrpc.newBlockingStub(channel2);
		
		ManagedChannel channel3 = ManagedChannelBuilder.forAddress("34.72.254.205", 9090).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub3 = MatrixServiceGrpc.newBlockingStub(channel3);
		
		ManagedChannel channel4 = ManagedChannelBuilder.forAddress("34.70.178.178", 9090).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub4 = MatrixServiceGrpc.newBlockingStub(channel4);
		
		ManagedChannel channel5 = ManagedChannelBuilder.forAddress("35.202.223.3", 9090).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub5 = MatrixServiceGrpc.newBlockingStub(channel5);
		
		ManagedChannel channel6 = ManagedChannelBuilder.forAddress("34.139.13.15", 9090).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub6 = MatrixServiceGrpc.newBlockingStub(channel6);
		
		ManagedChannel channel7 = ManagedChannelBuilder.forAddress("34.148.185.11", 9090).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub7 = MatrixServiceGrpc.newBlockingStub(channel7);
		
		ManagedChannel channel8 = ManagedChannelBuilder.forAddress("34.75.187.63", 9090).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub8 = MatrixServiceGrpc.newBlockingStub(channel8);
	
	
		int A[][] = matrixA;
		int B[][] = matrixB;
		int MAX = matrixA.length; //just get matrixA row length. do not need to get matrixB length as we already know that both sizes are same.
		//here we create a new matrix called matrixC
		int C[][] = new int[MAX][MAX];
		//deadline footprinting 
		long startTime = System.nanoTime();
		MatrixReply deadline_footprinting_matrix = stub1.multiplyBlock(MatrixRequest.newBuilder().setA(A[0][0]).setB(B[MAX-1][MAX-1]).build());
		long endTime = System.nanoTime();
		long footprint= endTime-startTime;

		
		
		
		
/////////////////////////////////////////////////////////

		int totalCalls = 0;
		//calling multiplication block function
		for(int i=0;i<MAX;i++){
			for(int j=0;j<MAX;j++){
				for(int k=0;k<MAX;k++){
					totalCalls++;
					}//end of k loop
			}//end of j loop
		}// end of i loop


		int RequiredNUMServer = 1+(int)(Math.round((footprint*totalCalls)/(deadline*1_000_000_000.0))); //minimum server would always be 1
		int callsPerServer = totalCalls/RequiredNUMServer; //real-time call is the total number of iterations performed via for-loop;

		int currentCallNum = 0;
			
		//scaling based on required number of servers:
		
		if(RequiredNUMServer==1){//if required number of server equals to 1
			for(int i=0;i<MAX;i++){
				for(int j=0;j<MAX;j++){
					C[i][j]=0;
					for(int k=0;k<MAX;k++){
						MatrixReply multiplyAB = stub1.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
						MatrixReply addAB = stub1.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
						C[i][j]= addAB.getC();
					}//end of k loop
				}//end of j loop
			}// end of i loop
		}



		else if(RequiredNUMServer==2){//if required number of server equals to 2
			for(int i=0;i<MAX;i++){
				for(int j=0;j<MAX;j++){
					C[i][j]=0;
					for(int k=0;k<MAX;k++){
						//calling block function 
						MatrixReply multiplyAB;
						MatrixReply addAB;
						if(currentCallNum<=(callsPerServer*1)){//server 1
							multiplyAB = stub1.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub1.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();

						}
						else if((currentCallNum>(callsPerServer*1)) && (currentCallNum<=(callsPerServer*2))){//server 2
							multiplyAB = stub2.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub2.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						currentCallNum++;
					}//end of k loop
				}//end of j loop
			}// end of i loop
		}

		else if(RequiredNUMServer==3){
			for(int i=0;i<MAX;i++){
				for(int j=0;j<MAX;j++){
					C[i][j]=0;
					for(int k=0;k<MAX;k++){
						//calling block function 
						MatrixReply multiplyAB;
						MatrixReply addAB;
						if(currentCallNum<=(callsPerServer*1)){//server 1
							multiplyAB = stub1.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub1.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();

						}
						else if((currentCallNum>(callsPerServer*1)) && (currentCallNum<=(callsPerServer*2))){//server 2
							multiplyAB = stub2.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub2.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*2)) && (currentCallNum<=(callsPerServer*3))){//server 3
							multiplyAB = stub3.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub3.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						currentCallNum++;
					}//end of k loop
				}//end of j loop
			}// end of i loop
		}
		else if(RequiredNUMServer==4){
			for(int i=0;i<MAX;i++){
				for(int j=0;j<MAX;j++){
					C[i][j]=0;
					for(int k=0;k<MAX;k++){
						//calling block function 
						MatrixReply multiplyAB;
						MatrixReply addAB;
						if(currentCallNum<=(callsPerServer*1)){//server 1
							multiplyAB = stub1.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub1.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();

						}
						else if((currentCallNum>(callsPerServer*1)) && (currentCallNum<=(callsPerServer*2))){//server 2
							multiplyAB = stub2.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub2.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*2)) && (currentCallNum<=(callsPerServer*3))){//server 3
							multiplyAB = stub3.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub3.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*3)) && (currentCallNum<=(callsPerServer*4))){//server 4
							multiplyAB = stub4.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub4.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						currentCallNum++;
					}//end of k loop
				}//end of j loop
			}// end of i loop
		}
		else if(RequiredNUMServer==5){
			for(int i=0;i<MAX;i++){
				for(int j=0;j<MAX;j++){
					C[i][j]=0;
					for(int k=0;k<MAX;k++){
						//calling block function 
						MatrixReply multiplyAB;
						MatrixReply addAB;
						if(currentCallNum<=(callsPerServer*1)){//server 1
							multiplyAB = stub1.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub1.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();

						}
						else if((currentCallNum>(callsPerServer*1)) && (currentCallNum<=(callsPerServer*2))){//server 2
							multiplyAB = stub2.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub2.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*2)) && (currentCallNum<=(callsPerServer*3))){//server 3
							multiplyAB = stub3.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub3.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*3)) && (currentCallNum<=(callsPerServer*4))){//server 4
							multiplyAB = stub4.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub4.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*4)) && (currentCallNum<=(callsPerServer*5))){//server 5
							multiplyAB = stub5.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub5.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						currentCallNum++;
					}//end of k loop
				}//end of j loop
			}// end of i loop
			
		}
		else if(RequiredNUMServer==6){
			for(int i=0;i<MAX;i++){
				for(int j=0;j<MAX;j++){
					C[i][j]=0;
					for(int k=0;k<MAX;k++){
						//calling block function 
						MatrixReply multiplyAB;
						MatrixReply addAB;
						if(currentCallNum<=(callsPerServer*1)){//server 1
							multiplyAB = stub1.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub1.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();

						}
						else if((currentCallNum>(callsPerServer*1)) && (currentCallNum<=(callsPerServer*2))){//server 2
							multiplyAB = stub2.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub2.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*2)) && (currentCallNum<=(callsPerServer*3))){//server 3
							multiplyAB = stub3.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub3.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*3)) && (currentCallNum<=(callsPerServer*4))){//server 4
							multiplyAB = stub4.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub4.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*4)) && (currentCallNum<=(callsPerServer*5))){//server 5
							multiplyAB = stub5.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub5.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*5)) && (currentCallNum<=(callsPerServer*6))){//server 6
							multiplyAB = stub6.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub6.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						currentCallNum++;
					}//end of k loop
				}//end of j loop
			}// end of i loop
		}
		else if(RequiredNUMServer==7){
			for(int i=0;i<MAX;i++){
				for(int j=0;j<MAX;j++){
					C[i][j]=0;
					for(int k=0;k<MAX;k++){
						//calling block function 
						MatrixReply multiplyAB;
						MatrixReply addAB;
						if(currentCallNum<=(callsPerServer*1)){//server 1
							multiplyAB = stub1.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub1.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();

						}
						else if((currentCallNum>(callsPerServer*1)) && (currentCallNum<=(callsPerServer*2))){//server 2
							multiplyAB = stub2.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub2.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*2)) && (currentCallNum<=(callsPerServer*3))){//server 3
							multiplyAB = stub3.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub3.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*3)) && (currentCallNum<=(callsPerServer*4))){//server 4
							multiplyAB = stub4.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub4.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*4)) && (currentCallNum<=(callsPerServer*5))){//server 5
							multiplyAB = stub5.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub5.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*5)) && (currentCallNum<=(callsPerServer*6))){//server 6
							multiplyAB = stub6.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub6.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*6)) && (currentCallNum<=(callsPerServer*7))){//server 7
							multiplyAB = stub7.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub7.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						currentCallNum++;
					}//end of k loop
				}//end of j loop
			}// end of i loop
		}
		else if(RequiredNUMServer>=8){//multiplication on server with scaling if the required number of servers is greater than 8 for better load balancing
			for(int i=0;i<MAX;i++){
				for(int j=0;j<MAX;j++){
					C[i][j]=0;
					for(int k=0;k<MAX;k++){
						//calling block function 
						MatrixReply multiplyAB;
						MatrixReply addAB;
						if(currentCallNum<=(callsPerServer*1)){//server 1
							multiplyAB = stub1.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub1.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();

						}
						else if((currentCallNum>(callsPerServer*1)) && (currentCallNum<=(callsPerServer*2))){//server 2
							multiplyAB = stub2.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub2.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*2)) && (currentCallNum<=(callsPerServer*3))){//server 3
							multiplyAB = stub3.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub3.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*3)) && (currentCallNum<=(callsPerServer*4))){//server 4
							multiplyAB = stub4.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub4.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*4)) && (currentCallNum<=(callsPerServer*5))){//server 5
							multiplyAB = stub5.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub5.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*5)) && (currentCallNum<=(callsPerServer*6))){//server 6
							multiplyAB = stub6.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub6.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else if((currentCallNum>(callsPerServer*6)) && (currentCallNum<=(callsPerServer*7))){//server 7
							multiplyAB = stub7.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub7.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						else{//server 8
							multiplyAB = stub8.multiplyBlock(MatrixRequest.newBuilder().setA(A[i][k]).setB(B[k][j]).build());
							addAB = stub8.addBlock(MatrixRequest.newBuilder().setA(C[i][j]).setB(multiplyAB.getC()).build());
							C[i][j]= addAB.getC();
						}
						currentCallNum++;
						
						}//end of k loop
				}//end of j loop
			}// end of i loop
		}

		

		for (int i = 0; i < A.length; i++) {
			for (int j = 0; j < A[0].length; j++) {
				System.out.print(C[i][j] + " ");
			}
			System.out.println("");
		}

		
		
		channel1.shutdown();
		channel2.shutdown();
		channel3.shutdown();
		channel4.shutdown();
		channel5.shutdown();
		channel6.shutdown();
		channel7.shutdown();
		channel8.shutdown();
			



	} 

















    // public String add(){
	// 	ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",9090)
	// 	.usePlaintext()
	// 	.build();
	// 	MatrixServiceGrpc.MatrixServiceBlockingStub stub
	// 	 = MatrixServiceGrpc.newBlockingStub(channel);
	// 	MatrixReply A=stub.addBlock(MatrixRequest.newBuilder()
	// 		.setA00(1)
	// 		.setA01(2)
	// 		.setA10(5)
	// 		.setA11(6)
	// 		.setB00(1)
	// 		.setB01(2)
	// 		.setB10(5)
	// 		.setB11(6)
	// 		.build());
	// 	String resp= A.getC00()+" "+A.getC01()+"<br>"+A.getC10()+" "+A.getC11()+"\n";
	// 	return resp;
    // }
}
