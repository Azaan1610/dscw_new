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
