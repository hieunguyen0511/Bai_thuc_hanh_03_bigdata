import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ElectricityConsumption {

    // Mapper class
    public static class ElectricityMapper extends Mapper<LongWritable, Text, Text, Text> {
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            String[] fields = line.split("\\s+");

            // Năm là khóa (key)
            String year = fields[0];

            // Tính trung bình lượng điện tiêu thụ
            double totalConsumption = 0;
            int count = 0;

            for (int i = 1; i < fields.length; i++) {
                totalConsumption += Double.parseDouble(fields[i]);
                count++;
            }

            double avgConsumption = totalConsumption / count;

            // Xuất key-value: năm và giá trị trung bình
            context.write(new Text(year), new Text(String.valueOf(avgConsumption)));
        }
    }

    // Reducer class
    public static class ElectricityReducer extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            for (Text value : values) {
                double avgConsumption = Double.parseDouble(value.toString());
                if (avgConsumption > 30) {
                    context.write(key, new Text(value.toString()));
                }
            }
        }
    }

    // Main method to run the job
    public static void main(String[] args) throws Exception {
        // Tạo một công việc mới cho MapReduce
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "electricity consumption");

        // Thiết lập các lớp Mapper và Reducer
        job.setJarByClass(ElectricityConsumption.class);
        job.setMapperClass(ElectricityMapper.class);
        job.setReducerClass(ElectricityReducer.class);

        // Định dạng kiểu dữ liệu đầu vào và đầu ra
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        // Đường dẫn đến tệp dữ liệu đầu vào và đầu ra
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // Chạy công việc MapReduce
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}