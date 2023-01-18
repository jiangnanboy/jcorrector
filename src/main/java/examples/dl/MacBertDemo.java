package examples.dl;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtException;
import org.apache.commons.lang3.tuple.Pair;
import sy.dl.MacBert;
import sy.dl.bert.LoadModel;
import sy.dl.bert.tokenizerimpl.BertTokenizer;
import util.PropertiesReader;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author YanShi
 * @date 2022/7/4 20:45
 */
public class MacBertDemo {

    public static void main(String[] args) throws OrtException {
        // macbert onnx path
        String onnxPath = MacBertDemo.class.getClassLoader().getResource(PropertiesReader.get("onnx_model_path")).getPath().replaceFirst("/", "");
        LoadModel.loadOnnxModel(onnxPath);
        String text = "今天新情很好。";
        text = "你找到你最喜欢的工作，我也很高心。";
        text = "是的，当线程不再使用时，该缓冲区将被清理（我昨天实际上对此进行了测试，我可以每5ms发送一个新线程，而不会产生净内存累积，并确认它的RNG内存已在GC上清理）。编号7788";
        text = text.toLowerCase();
        Pair<BertTokenizer, Map<String, OnnxTensor>> pair = null;
        try {
            pair = MacBert.parseInputText(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> predTokenList = MacBert.predCSC(pair);
        predTokenList = predTokenList.stream().map(token -> token.replace("##", "")).collect(Collectors.toList());
        String predString = String.join("", predTokenList);
        System.out.println("predString: " + predString);
        List<Pair<String, String>> resultList = MacBert.getErrors(predString, text);
        for(Pair<String, String> result : resultList) {
            System.out.println(text + " => " + result.getLeft() + " " + result.getRight());
        }
    }

}
