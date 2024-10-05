import java.io.IOException;
import java.util.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;

public class BT1 {
	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
		public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
			String[] splittedValues = value.toString().split("\\s+");
			String currentYear = splittedValues[0];
			Text year = new Text(currentYear);
			output.collect(year, new IntWritable(Integer.parseInt(splittedValues[splittedValues.length - 1])));
		}
	}

	public static class Reduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
		public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
			while (values.hasNext()) {
				int current = values.next().get(); 
				if (current >= 30) {				
					output.collect(key, new IntWritable(current));
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		JobConf conf = new JobConf(BT1.class);
		conf.setJobName("BT1");
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntWritable.class);
		conf.setMapperClass(Map.class);
		conf.setCombinerClass(Reduce.class);
		conf.setReducerClass(Reduce.class);
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
		JobClient.runJob(conf);
	}
}