package sy.dl.bert;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

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
     * @param onnxPath
     * @throws OrtException
     */
    public static void loadOnnxModel(String onnxPath) throws OrtException {
        System.out.println("load onnx model...");
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
