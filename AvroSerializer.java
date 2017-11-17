package com.hadoop.avro;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.Date;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;

import com.haddop.util.DateUtil;
public class AvroSerializer {
	
	public static String csvFilesFolder =  "C:\\Users\\pandubabu\\eclipse-workspace\\Avro\\Avro\\Resources\\csv\\";
	public static String avscFilesFolder =  "C:\\Users\\pandubabu\\eclipse-workspace\\Avro\\Avro\\Resources\\avsc\\";
	public static String avroFilesFolder =  "C:\\Users\\pandubabu\\eclipse-workspace\\Avro\\Avro\\Resources\\avro\\";
	public static String avroDeserializerFiles =  "C:\\Users\\pandubabu\\eclipse-workspace\\Avro\\Avro\\Resources\\avrodesr\\";
	
	public static final int FRAMEWORK_FIELDS_COUNT = 4;
		
	public static void main(String[] args) {
		String avscFileName = "user.avsc";
		String avroFileName = "user.avro";
		String csvFileName = "user.csv";
		String cycleDate = "20170925";
		int header = 1;
		try {
			seraializeCsvFile(avscFilesFolder+avscFileName , avroFilesFolder+avroFileName, csvFileName , "," ,cycleDate,header);
		} catch (IOException e) {			
			e.printStackTrace();
		}
		deseraializeAvroFileToCsv(avscFilesFolder+avscFileName , avroFilesFolder+avroFileName);
	}	
	/**
	 * This function mainly serializes the csv file data into Avro format based on the given
	 * input parameters.
	 * @param avscFile
	 * @param avrofilPath
	 * @param csvfileName
	 * @param delimiter
	 * @param cycleDate
	 * @throws IOException
	 */
	public static void seraializeCsvFile(String avscFile , String avrofilPath , String csvfileName ,String delimiter , String cycleDate,int header) 
			throws IOException{		
		//Loading the avsc schema file
		Schema schema = new Schema.Parser().parse(new File(avscFile));
		
		//Reading field names from the avsc schema file names
		//List<String> avscFieldNames = schema.getFields().stream().map(Field :: name).collect(Collectors.toList());
		
		//Initializing avro serializer 
		File file = new File(avrofilPath);
		DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(schema);
		DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<GenericRecord>(datumWriter);
		dataFileWriter.create(schema, file);			
		
		//Reading the input CSV file
		LongSummaryStatistics nonEmptyLines = Files.lines(FileSystems.getDefault().getPath(csvFilesFolder, csvfileName)).
			skip(header).				
			filter(line -> !"".equalsIgnoreCase(line)).
			map(line -> {    //Construction avro object for the each line based on the schema.					
				GenericRecord object = new GenericData.Record(schema);
				String[] rowData = line.split(delimiter);					
				for(int i=0;i<schema.getFields().size()-FRAMEWORK_FIELDS_COUNT;i++) {
					object.put(schema.getFields().get(i).name(), rowData[i]);
				}
				return object;
			}).map(object ->{  //Adding framework tracking related fields
				object.put("user", "System"); //It will be changed to JIRA ticket when ever any manual updates done during support.
				try {
					object.put("cycleDate",DateUtil.getDate(cycleDate, "yyyyMMdd").getTime());
				} catch (ParseException e) {					
					e.printStackTrace();
					return null;
				}
				object.put("timeStamp",System.currentTimeMillis());				
				object.put("batchId", "PolciyBatch-1");
				return object;
			}).map(object -> { //Writing avro object to the output stream(avro file)
				try {
					if(null == object) {
						return 0L;  //0 - means exception occured while creating object to avro file.
					}else {
						dataFileWriter.append(object);
					}					
				} catch (IOException e) {
					e.printStackTrace();
					return 0L;  //0 - means exception occured while writing object to avro file.
				}					
				return 1L;  //1 - means successfully written the line object to the avro file.
			}).collect(Collectors.summarizingLong(Long :: longValue));						
		System.out.println("  nonEmptyLines "+nonEmptyLines.getSum());
		dataFileWriter.close();
	}
	
	
	public static void deseraializeAvroFileToCsv(String avscFile , String avrofilPath) {
		try {
			Schema schema = new Schema.Parser().parse(new File(avscFile)); 
			//Deserialize users from disk
			DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);			
			DataFileReader<GenericRecord> dataFileReader = new DataFileReader<GenericRecord>(new File(avrofilPath), datumReader);
			GenericRecord user = null;
			while (dataFileReader.hasNext()) {
				// Reuse user object by passing it to next(). This saves us from
				// allocating and garbage collecting many objects for files with
				// many items.
				user = dataFileReader.next(user);
				System.out.println(user);
				Date cycleDate = new Date((Long)user.get("cycleDate"));
				Date timeStamp = new Date((Long)user.get("timeStamp"));
				
				//System.out.println(cycleDate+"="+cycleDate  + "=timeStamp"+timeStamp);			        
			} 
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
