package org.forwarder.demo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

import javax.naming.OperationNotSupportedException;

import org.forwarder.Backend;
import org.forwarder.Config;
import org.forwarder.Forwarder;
import org.forwarder.Session;
import org.onnx4j.Tensor;
import org.onnx4j.tensor.DataType;
import org.onnx4j.tensor.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for Forward class.
 */
public class SimpleTest extends TestCase {

	private static final int LOOP_TIMES = 1000;

	private static Logger logger = LoggerFactory.getLogger(SimpleTest.class);

	public Forwarder forwarder;

	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws OperationNotSupportedException
	 */
	public SimpleTest(String testName) throws FileNotFoundException, IOException, OperationNotSupportedException {
		super(testName);

		String modelPath = URLDecoder.decode(SimpleTest.class.getResource("/simple/model.onnx").getFile(), "utf-8");
		assertNotNull(modelPath);

		this.forwarder = Forwarder
				.config(Config.builder().setDebug(true).setMemoryByteOrder(ByteOrder.LITTLE_ENDIAN).build())
				.load(modelPath).executor("recursion");
		assert forwarder != null;
	}

	/**
	 * @return the suite of tests being tested
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static Test suite() throws FileNotFoundException, IOException {
		return new TestSuite(SimpleTest.class);
	}

	/**
	 * Using Tensorflow as backend for model forward
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws OperationNotSupportedException
	 */
	public void testForwardUsingTFBackend()
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, OperationNotSupportedException {
		this.testForward("Tensorflow");
	}

	/**
	 * Using Deeplearning4j as backend for model forward
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws OperationNotSupportedException
	 */
	public void testForwardUsingDL4JBackend()
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, OperationNotSupportedException {
		this.testForward("DL4J");
	}

	private void testForward(String backendName)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, OperationNotSupportedException {
		Backend<?> backend = this.forwarder.backend(backendName);

		Stopwatch watch = Stopwatch.createStarted();
		for (int m = 0; m < LOOP_TIMES; m++) {
			try (Session<?> session = backend.newSession()) {
				Tensor x2_0 = Tensor.builder(DataType.FLOAT, Shape.create(2L, 1L), Tensor.options()).putFloat(3f)
						.putFloat(2f).build();
				Tensor y0 = session.feed("x2:0", x2_0).forward().getOutput("y:0");

				if (m < (LOOP_TIMES - 1))
					continue;

				//
				// Dump outputs data
				//
				logger.debug("Output: {}", y0.toString());
			}
		}
		long elapsedTimeMS = watch.elapsed(TimeUnit.MILLISECONDS);

		logger.info("Total time: {}s\tAvg time: {}ms", elapsedTimeMS, ((float) elapsedTimeMS) / LOOP_TIMES);
	}

}
