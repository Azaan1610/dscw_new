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

		
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub = MatrixServiceGrpc.newBlockingStub(channel);
		
		//these are the 8 different servers with their respective stubs
		ManagedChannel channel1 = ManagedChannelBuilder.forAddress("", 9090).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub1 = MatrixServiceGrpc.newBlockingStub(channel1);
		
		ManagedChannel channel2 = ManagedChannelBuilder.forAddress("", 9090).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub2 = MatrixServiceGrpc.newBlockingStub(channel2);
		
		ManagedChannel channel3 = ManagedChannelBuilder.forAddress("", 9090).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub3 = MatrixServiceGrpc.newBlockingStub(channel3);
		
		ManagedChannel channel4 = ManagedChannelBuilder.forAddress("", 9090).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub4 = MatrixServiceGrpc.newBlockingStub(channel4);
		
		ManagedChannel channel5 = ManagedChannelBuilder.forAddress("", 9090).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub5 = MatrixServiceGrpc.newBlockingStub(channel5);
		
		ManagedChannel channel6 = ManagedChannelBuilder.forAddress("", 9090).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub6 = MatrixServiceGrpc.newBlockingStub(channel6);
		
		ManagedChannel channel7 = ManagedChannelBuilder.forAddress("", 9090).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub7 = MatrixServiceGrpc.newBlockingStub(channel7);
		
		ManagedChannel channel8 = ManagedChannelBuilder.forAddress("", 9090).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub8 = MatrixServiceGrpc.newBlockingStub(channel8);

		int maxVal = matrixA.length; //just get matrixA row length. do not need to get matrixB length as we already know that both sizes are same.
		int matrixC[][] = new int[maxVal][maxVal];

		//here we add all the stubs for each channel in an array so we can keep track of them 
		ArrayList<MatrixServiceGrpc.MatrixServiceBlockingStub> stubArray = new ArrayList<MatrixServiceGrpc.MatrixServiceBlockingStub>();
                stubArray.add(stub1);
                stubArray.add(stub2);
                stubArray.add(stub3);
                stubArray.add(stub4);
                stubArray.add(stub5);
                stubArray.add(stub6);
                stubArray.add(stub7);
                stubArray.add(stub8);


		int requiredServers = 8; 
		int stubs = 0;
		for (int i = 0; i < maxVal; i++) { // row
			for (int j = 0; j < maxVal; j++) { // col
				for (int k = 0; k < maxVal; k++) {
					
					MatrixReply temp=stubArray.get(stubs).multiplyBlock(MatrixRequest.newBuilder().setA(matrixA[i][k]).setB(matrixB[k][j]).build());
					if(stubs == requiredServers-1) stubs = 0;
					else stubs++;
					MatrixReply temp2=stubArray.get(stubs).addBlock(MatrixRequest.newBuilder().setA(matrixC[i][j]).setB(temp.getC()).build());
					matrixC[i][j] = temp2.getC();
					if(stubs == requiredServers-1) stubs = 0;
					else stubs++;
				}
			}
		}

		 // Print result matrix
		 for (int i = 0; i < matrixA.length; i++) {
			for (int j = 0; j < matrixA[0].length; j++) {
				System.out.print(matrixC[i][j] + " ");
			}
			System.out.println("");
		}
		// Close channels
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
