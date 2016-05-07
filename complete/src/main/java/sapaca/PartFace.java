package sapaca;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.IplImage;

import java.io.File;
import java.net.URL;
import static org.bytedeco.javacpp.opencv_core.cvLoad;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import org.bytedeco.javacpp.opencv_stitching.Stitcher;


/**
 * Created by caro on 05.05.2016.
 */
public class PartFace implements Part {
    private static String xmlPath;
    private CvHaarClassifierCascade cascade;
    private String xmlName;

    @Override
    public CvHaarClassifierCascade loadClassifier() {
        cascade = new CvHaarClassifierCascade(cvLoad(xmlPath));
        MatVector imgs = new MatVector();
        Mat pano = new Mat();
        Stitcher stitcher = Stitcher.createDefault(false);
        int status = stitcher.stitch(imgs, pano);
        System.out.println(status);
        return cascade;
    }

    @Override
    public String getXmlPath() {
        return xmlPath;
    }

    @Override
    public String getXmlName() {
        xmlName = "haarcascade_frontalface_alt.xml";
        return xmlName;
    }
}