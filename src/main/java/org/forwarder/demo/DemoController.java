/**
 * 
 */
package org.forwarder.demo;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.forwarder.Config;
import org.forwarder.Forwarder;
import org.forwarder.Session;
import org.forwarder.executor.impls.RayExecutor;
import org.onnx4j.Tensor;
import org.onnx4j.Tensor.AllocationMode;
import org.onnx4j.tensor.DataType;
import org.onnx4j.tensor.Shape;
import org.onnx4j.tensor.TensorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.twelvemonkeys.image.ResampleOp;

import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Decoder;

/**
 * @author HarryLee
 * @createdOn 2019年7月5日
 * @company 广州广之旅国际旅行社股份有限公司
 *
 */
@Slf4j
@RestController
@RequestMapping("/demo/")
@SuppressWarnings("restriction")
public class DemoController {

	private static final int INPUT_IMG_WIDTH = 28;
	private static final int INPUT_IMG_HEIGHT = 28;

	private static Logger logger = LoggerFactory.getLogger(DemoController.class);

	private static BASE64Decoder decoder = new sun.misc.BASE64Decoder();

	private static Forwarder fw;

	static {
		try {
			logger.info("Loading mnist model ...");
			String absoluteModelPath = URLDecoder
					.decode(DemoController.class.getResource("/models/mnist/opset_v8/model.onnx").getFile(), "utf-8");
			fw = Forwarder
					.config(
						Config
							.builder()
							.setDebug(true)
							.setMemoryByteOrder(ByteOrder.LITTLE_ENDIAN)
							.build()
					)
					.load(absoluteModelPath)
					.executor(RayExecutor.class);
			logger.info("MNIST model loaded.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "forward", method = RequestMethod.POST)
	@ResponseBody
	public float[] forward(@RequestParam(value = "img") String base64Img, @RequestParam String backend)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, Exception {
		BufferedImage bufferedImage = base64StringToImage(base64Img);
		bufferedImage = resize(bufferedImage, INPUT_IMG_WIDTH, INPUT_IMG_HEIGHT);

		float[] preditions;
		try (Tensor inputTensor = convert2InputTensor(bufferedImage)) {
			//FileUtils.write(new File("d://out.txt"), inputTensor.toString(), "UTF-8");
			
			try (Session<?> session = fw.backend(backend).newSession()) {
				Tensor outputTensor = session.feed(inputTensor).forward().getOutput("Plus214_Output_0");
				FloatBuffer buffer = outputTensor.getData().asFloatBuffer();
				preditions = new float[buffer.capacity()];
				for (int n = 0; n < buffer.capacity(); n++) {
					preditions[n] = buffer.get(n);
				}
			}
		}
		return preditions;
	}
	
	private static BufferedImage resize(BufferedImage img, int height, int width) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }

	private static BufferedImage base64StringToImage(String base64String) throws IOException {
		byte[] bytes = decoder.decodeBuffer(base64String);
		try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
			BufferedImage bi = ImageIO.read(bais);
			return bi;
		}
	}

	private static BufferedImage resizeImage(BufferedImage inputImage, Integer width, Integer height) {
		ResampleOp resampleOp = new ResampleOp(width, height);
		return resampleOp.filter(inputImage, null);
	}
	
	private static BufferedImage toGrayImage(BufferedImage inputImage) {
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		ColorConvertOp op = new ColorConvertOp(cs, null);
		return op.filter(inputImage, null);
		/*ImageFilter filter = new GrayFilter(true, 50);  
		ImageProducer producer = new FilteredImageSource(inputImage.getSource(), filter);  
		Image image = Toolkit.getDefaultToolkit().createImage(producer);  
		return BufferedImage. ().getGraphics().getColor()*/
	}

	private static Tensor convert2InputTensor(BufferedImage bufferedImage) {
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		TensorBuilder builder = TensorBuilder
				.builder(DataType.FLOAT, Shape.create(1, 1, height, width),
						Tensor.options().setAllocationMode(AllocationMode.DIRECT).setByteOrder(ByteOrder.nativeOrder()))
				.name("Input3");

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int color = bufferedImage.getRGB(j, i);
				int alpha = (color >> 24) & 0xff;
			    int red = (color >> 16) & 0xff;
			    int green = (color >> 8) & 0xff;
			    int blue = (color) & 0xff;
			    int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
			    //System.out.print(String.format("gray(%s,%s) = %s,\t\t\t", i, j, gray));
			    //System.out.print(String.format("(%s,%s) -> [gray=%s] [color=%s,a=%s,r=%s,g=%s,b=%s],\t", i, j, gray, color, alpha, red, green, blue));
				builder.putFloat((float) gray);
			}
			//System.out.println("");
		}

		return builder.build();
	}

}
