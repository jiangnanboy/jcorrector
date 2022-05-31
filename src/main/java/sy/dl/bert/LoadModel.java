package sy.dl.bert;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import util.PropertiesReader;

import java.util.Optional;

/**
 * @author sy
 * @date 2022/5/31 19:03
 */
public class LoadModel {

    public static OrtSession session;
    public static OrtEnvironment env;
    /**
     * load onnx model
     * @throws OrtException
     */
    public static void loadOnnxModel() throws OrtException {
        System.out.println("load onnx model...");
        String onnxPath = LoadModel.class.getClassLoader().getResource(PropertiesReader.get("onnx_model_path")).getPath().replaceFirst("/", "");
//        String onnxPath = PropertiesReader.get("onnx_model_path");

        env = OrtEnvironment.getEnvironment();
        session = env.createSession(onnxPath, new OrtSession.SessionOptions());
    }

    /**
     * close onnx model
     */
    public static void closeOnnxModel() {
        System.out.println("close onnx model...");
        if (Optional.of(session).isPresent()) {
            try {
                session.close();
            } catch (OrtException e) {
                e.printStackTrace();
            }
        }
        if(Optional.of(env).isPresent()) {
            env.close();
        }
    }

}
