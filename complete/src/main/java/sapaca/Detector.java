package sapaca;

import org.bytedeco.javacpp.opencv_core.IplImage;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.*;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;

public class Detector {
	private ArrayList<IplImage> croppedFaces = new ArrayList<>();
	private ArrayList<Face> faceObjects = new ArrayList<>();
	private int counter;
	private Part part;
	private String xmlPath;
	private IplImage eyeImage;

	public Detector(Part part, IplImage image) {
		this.part = part;
		// setXmlFile();
		detectFace(image);
	}

	private void detectFace(IplImage originalImage) {
		File xmlFile = null;
		try {
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(part.getXmlName());
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			IOUtils.copy(inputStream, outputStream);
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
			byte data[] = outputStream.toByteArray();

			xmlFile = new File("sapaca/" + part.getXmlName());
			xmlFile.getParentFile().mkdirs();

			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(xmlFile));
			bufferedOutputStream.write(data);
			bufferedOutputStream.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad(xmlFile.getAbsolutePath()));
		IplImage grayImage = IplImage.create(originalImage.width(), originalImage.height(), IPL_DEPTH_8U, 1);
		cvCvtColor(originalImage, grayImage, CV_BGR2GRAY);

		CvMemStorage storage = CvMemStorage.create();
		CvSeq foundAreas = cvHaarDetectObjects(grayImage, cascade, storage, 1.2, 2, 0);
		if (part.getPartToDetect().equals(PartToDetect.FACE)) {
			for (int i = 0; i < foundAreas.total(); i++) {
				counter = i;
				CvRect r = new CvRect(cvGetSeqElem(foundAreas, i));
				cvRectangle(originalImage, cvPoint(r.x(), r.y()), cvPoint(r.x() + r.width(), r.y() + r.height()),
						CvScalar.GREEN, 1, CV_AA, 0);

				CvRect r2 = cvRect(r.x() - 100, r.y() - 100, r.width() + 200, r.height() + 200);
				cvSetImageROI(originalImage, r2);
				croppedFaces.add(i, cropImage(originalImage));
			}

		}
		if (part.getPartToDetect().equals(PartToDetect.EYES)) {
			for (int i = 0; i < foundAreas.total(); i++) {
				counter = i;
				CvRect r = new CvRect(cvGetSeqElem(foundAreas, i));
				cvRectangle(originalImage, cvPoint(r.x(), r.y()), cvPoint(r.x() + r.width(), r.y() + r.height()),
						CvScalar.GREEN, 1, CV_AA, 0);
			}
			croppedFaces.add(cropImage(originalImage));
			eyeImage = originalImage;
		}
	}

	private IplImage cropImage(IplImage originalImage) {
		IplImage croppedImage = IplImage.create(cvGetSize(originalImage), originalImage.depth(),
				originalImage.nChannels());
		cvCopy(originalImage, croppedImage);

		if (part.getPartToDetect().equals(PartToDetect.FACE)) {
			cvResetImageROI(originalImage);
			Face faceObject = new Face(croppedImage);
			faceObjects.add(counter, faceObject);
		}

		if (part.getPartToDetect().equals(PartToDetect.EYES)) {
			Face faceObject = new Face(croppedImage);
			faceObjects.add(faceObject);
		}
		return croppedImage;
	}

	protected ArrayList<Face> getFaces() {
		return faceObjects;
	}

	// private void setXmlFile() {
	// try {
	// URL source =
	// Detector.class.getClassLoader().getResource(part.getXmlName());
	// xmlPath = new File(source.toURI()).getAbsolutePath();
	// } catch (URISyntaxException e) {
	// xmlPath = "";
	// } catch (Exception e) {
	// part = null;
	// xmlPath = "";
	// }
	// }

	public IplImage getEyeImage() {
		return eyeImage;
	}
}
