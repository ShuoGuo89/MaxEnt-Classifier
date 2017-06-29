import java.util.*;
import java.io.*;
public class MaxEntClassifier {
	private Map<String, Map<String, Double>> topicLambda;
	private double accuracy;
	
	public MaxEntClassifier(){
		this.topicLambda = new HashMap<String, Map<String, Double>>();
		this.accuracy = 0.0;
	}
	
	public double getAccuracy(){
		return this.accuracy;
	}
	
	public void loadLambda(String modelFile){
		String line = null;
		String topic = "";
		try{
			FileReader modelF = new FileReader(modelFile);
			BufferedReader buffModelF = new BufferedReader(modelF);
			while((line = buffModelF.readLine()) != null){
				line = line.trim();
				if(line.length() > 0){
					String[] content = line.split(" +");
					
					if(content.length == 4){
						topic = content[3];
						this.topicLambda.put(topic, new HashMap<String, Double>());
					} else {
						this.topicLambda.get(topic).put(content[0], Double.valueOf(content[1]));
					}
				}
			}
			buffModelF.close();
		}
		catch(FileNotFoundException ex){
			System.out.println("Model file not found: " + modelFile + "!");
		}
		catch(IOException ex){
			System.out.println("Error reading model file : " + modelFile + "!");
			ex.printStackTrace();
		}
	}
	
	private Map<String, Double> calcProb(Set<String> vocSet){
		Map<String, Double> res = new HashMap<String, Double>();
		double denom = 0.0;
		for(String topic : this.topicLambda.keySet()){
			double sumLambda = 0.0;
			for(String wd : vocSet){
				if(this.topicLambda.get(topic).containsKey(wd)){
					sumLambda += topicLambda.get(topic).get(wd);
				}
			}
			sumLambda += topicLambda.get(topic).get("<default>");
			double numer = Math.exp(sumLambda);
			res.put(topic, numer);
			denom += numer;
		}
		for(String topic : res.keySet()){
			res.put(topic, res.get(topic) / denom);
		}
		return res;
	}
	
	private String findMaxKey(Map<String, Double> map){
		String str = "";
		double tmpMax = Double.MIN_VALUE;
		for(String key : map.keySet()){
			if(map.get(key) > tmpMax) {
				str = key;
				tmpMax = map.get(key);
			}
		}
		return str;
	}
	
	public void maxentClassify(String dataFile, String systemFile){
		String line = null;
		try{
			FileReader dataF = new FileReader(dataFile);
			BufferedReader buffModelF = new BufferedReader(dataF);
			FileWriter sysF = new FileWriter(systemFile);
			BufferedWriter buffSysF = new BufferedWriter(sysF);
			int instanceCount = 0, correctInstance = 0;
			while((line = buffModelF.readLine()) != null){
				line = line.trim();
				if(line.length() > 0){
					instanceCount ++;
					String[] content = line.split(" +");
					String realTopic = content[0];
					Set<String> vocSet = new HashSet<String>();
					int i = 0;
					while(i < content.length){
						vocSet.add(content[i].split(":")[0]);
						i++;
					}
					Map<String, Double> classifyRes = calcProb(vocSet);
					String classifiedTopic = findMaxKey(classifyRes);
					if(classifiedTopic.equals(realTopic)) correctInstance ++;
					//buffSysF.write("Real: " + realTopic + "\tMaxEnt: " + classifiedTopic);
					//buffSysF.newLine();
					for(String topic: classifyRes.keySet()){
						buffSysF.write(topic + " " + classifyRes.get(topic) + " ");
					}
					buffSysF.newLine();
					//buffSysF.newLine();
				}
			}
			buffModelF.close();
			buffSysF.close();
			this.accuracy = (double) (correctInstance) / instanceCount;
		}
		catch(FileNotFoundException ex){
			System.out.println("File not found !");
		}
		catch(IOException ex){
			System.out.println("Error reading file!");
			ex.printStackTrace();
		}
	}
}
