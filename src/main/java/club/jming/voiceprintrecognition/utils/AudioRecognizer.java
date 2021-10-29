package club.jming.voiceprintrecognition.utils;

import org.springframework.stereotype.Component;
import org.tritonus.sampled.convert.PCM2PCMConversionProvider;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AudioRecognizer {

    //频率最高震级，[哪个块][哪个频率范围]
    double[][] highMagnitude;
    //记录哪个频率出现，出现为1否则为0，[哪个块][哪个频率]
    double[][] recordPoints;
    //相应频率，[哪个块][哪个频率范围]
    long[][] frequency;
    //<片段index,特征hash>
    public static Map<Integer, Long> songDP;

    private static final int BLOCKSIZE = 4096;

    private static void initSongDP() {
        songDP = new HashMap<>();
    }

    /**
     * 根据音乐生成数据点(音频指纹)
     *
     * @param filePath
     * @return <片段index,hash>
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    public Map<Integer, Long> createDP(String filePath) throws IOException, UnsupportedAudioFileException {

        AudioInputStream in;
        File file = null;
        AudioInputStream outDin = null;

        //格式转换类，将MP3格式转换成类似模拟信号，fft分析的声纹才有意义
        PCM2PCMConversionProvider conversionProvider = new PCM2PCMConversionProvider();


        if (filePath.contains("http")) {
            URL url = new URL(filePath);
            in = AudioSystem.getAudioInputStream(url);
        } else {
            file = new File(filePath);
            in = AudioSystem.getAudioInputStream(file);
        }

        AudioFormat format = in.getFormat();

        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(), 16,
                    format.getChannels(),format.getChannels() *2 , format.getSampleRate(), false);
            in = AudioSystem.getAudioInputStream(format, in);
        }

        System.out.println(format.toString());

        outDin = conversionProvider.getAudioInputStream(getFormat(), in);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int n = 0;        //定义n为刷新缓冲区次数
        byte[] buffer = new byte[1024]; //设置字节数组缓冲区
        assert file != null;
        int length = (int) file.length() / 1024;

        // 获取音乐文件的字节流
        try {
            while (true) {
                n++;
                if (n > length) {
                    break;
                }
                int count;
                //非录音，从音频流读取数据放到缓冲区buffer，偏移量为0，读取最大长度为1024，返回读入缓冲区的总字节数
                count = outDin.read(buffer, 0, 1024);
                if (count > 0) {
                    out.write(buffer, 0, count);    //从偏移量0开始将指定字节数组buffer的count字节数写入此字节数组输出流。
                }
            }

//                    byte b[] = out.toByteArray();

            try {
                //做频谱分析
                makeSpectrum(out);

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }

            out.close();


        } catch (IOException e) {
            System.err.println("I/O problems: " + e);
            System.exit(-1);
        }

        in.close();
        outDin.close();

        return songDP;
    }

    private AudioFormat getFormat() {
        float sampleRate = 44100;
        int sampleSizeInBits = 8;
        int channels = 1; // mono
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,
                bigEndian);
    }

    //做频谱分析
    void makeSpectrum(ByteArrayOutputStream out) {
        byte[] audio = out.toByteArray();
        final int totalSize = audio.length;
//        System.out.println("---------------------------------------取文件大小: "+totalSize);
        int blockLength = totalSize / BLOCKSIZE;        //滑动窗口大小为4096
//        System.out.println("块数量: " + blockLength);

        //转变成频域需要复数:
        Complex[][] results = new Complex[blockLength][];

        //对所有的数据块进行傅里叶转换，将时域数据转为频域
        for (int times = 0; times < blockLength; times++) {
            Complex[] complex = new Complex[BLOCKSIZE];
            for (int i = 0; i < BLOCKSIZE; i++) {
                //将时域数据化成虚数为0的复数：
                complex[i] = new Complex(audio[(times * BLOCKSIZE) + i], 0);
            }
            //对数据块执行FFT分析:
            results[times] = FFT.fft(complex);
        }
        //确定特征点
        determineKeyPoints(results);
    }

    public final int UPPER_LIMIT = 300;
    public final int LOWER_LIMIT = 30;

    // 将频谱分成多个子带，选择如下几个子带：低音子带为30Hz-40Hz,40Hz-80Hz和80 Hz-120Hz（贝司吉他等乐器的基频会出现低音子带）
    // 中音和高音子带分别为120Hz-180Hz 和180Hz-300Hz（人声和大部分其他乐器的基频出现在这两个子带）。
    public final int[] RANGE = new int[]{40, 80, 120, 180, UPPER_LIMIT + 1};

    //找出哪个频率范围
    public int getIndex(int freq) {
        int i = 0;
        while (RANGE[i] < freq) {
            i++;
        }
        return i;
    }

    //确定特征点
    void determineKeyPoints(Complex[][] results) {
//        FileWriter fstream = null;
//        try {
//            fstream = new FileWriter("result.txt");
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }
//        BufferedWriter outFile = new BufferedWriter(fstream);

        //初始化
        highMagnitude = new double[results.length][5];
        for (int i = 0; i < results.length; i++) {
            for (int j = 0; j < 5; j++) {
                highMagnitude[i][j] = 0;
            }
        }

        recordPoints = new double[results.length][UPPER_LIMIT];
        for (int i = 0; i < results.length; i++) {
            for (int j = 0; j < UPPER_LIMIT; j++) {
                recordPoints[i][j] = 0;
            }
        }

        frequency = new long[results.length][5];
        for (int i = 0; i < results.length; i++) {
            for (int j = 0; j < 5; j++) {
                frequency[i][j] = 0;
            }
        }

        initSongDP();

        for (int t = 0; t < results.length; t++) {
            for (int freq = LOWER_LIMIT; freq < UPPER_LIMIT - 1; freq++) {
                // 获取震级(分贝)
                double mag = Math.log(results[t][freq].abs() + 1);

                // 找出所处的范围：
                int index = getIndex(freq);

                // 保存最高震级和相应频率：
                if (mag > highMagnitude[t][index]) {
                    highMagnitude[t][index] = mag;
                    recordPoints[t][freq] = 1;
                    frequency[t][index] = freq;
                }
            }

//            try {
//                for (int k = 0; k < 5; k++) {
//                    outFile.write("" + highMagnitude[t][k] + ";" + frequency[t][k] + "\t");
//                }
//                outFile.write("\n");
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            //为歌曲的某个块生成哈希索引
            long h = hash(frequency[t][0], frequency[t][1], frequency[t][2], frequency[t][3],frequency[t][4]);
//            System.out.println("\r\n"+n);
//            System.out.println(h);
//            n++;

            songDP.put(t, h);
        }

//        try {
//            outFile.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private static final int FUZ_FACTOR = 2;

    private long hash(long p1, long p2, long p3, long p4,long p5) {
        return (p5-(p5%FUZ_FACTOR))*100000000000l+(p4 - (p4 % FUZ_FACTOR)) * 100000000 + (p3 - (p3 % FUZ_FACTOR)) * 100000
                + (p2 - (p2 % FUZ_FACTOR)) * 100 + (p1 - (p1 % FUZ_FACTOR));
    }
}
