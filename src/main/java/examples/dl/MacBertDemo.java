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
        Pair<BertTokenizer, Map<String, OnnxTensor>> pair = null;
        try {
            pair = MacBert.parseInputText(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        var predString = MacBert.predCSC(pair);
        List<Pair<String, String>> resultList = MacBert.getErrors(predString, text);
        for(Pair<String, String> result : resultList) {
            System.out.println(text + " => " + result.getLeft() + " " + result.getRight());
        }
    }

}
