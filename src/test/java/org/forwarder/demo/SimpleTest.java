/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.forwarder.executor.impls.RecursionExecutor;
import org.onnx4j.Tensor;
import org.onnx4j.tensor.DataType;
import org.onnx4j.tensor.Shape;
import org.onnx4j.tensor.TensorBuilder;
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

	private static final int LOOP_TIMES = 1;

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
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public SimpleTest(String testName) throws FileNotFoundException, IOException, OperationNotSupportedException,
			NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		super(testName);

		String modelPath = URLDecoder.decode(SimpleTest.class.getResource("/simple/model.onnx").getFile(), "utf-8");
		assertNotNull(modelPath);

		this.forwarder = Forwarder
				.config(Config.builder().setDebug(true).setMemoryByteOrder(ByteOrder.LITTLE_ENDIAN).build())
				.load(modelPath).executor(RecursionExecutor.class);
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
	 * @throws Exception 
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void testForwardUsingTFBackend()
			throws Exception {
		try (Forwarder f = this.forwarder) {
			this.testForward("Tensorflow");
		}
	}

	/**
	 * Using Deeplearning4j as backend for model forward
	 * @throws Exception 
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void testForwardUsingDL4JBackend()
			throws Exception {
		try (Forwarder f = this.forwarder) {
			this.testForward("DL4J");
		}
	}

	private void testForward(String backendName)
			throws Exception {
		Backend<?> backend = this.forwarder.backend(backendName);
		Stopwatch watch = Stopwatch.createStarted();
		Tensor x2_0;
		Tensor y0;
		for (int m = 0; m < LOOP_TIMES; m++) {
			try (Session<?> session = backend.newSession()) {
				x2_0 = TensorBuilder
						.builder(
							DataType.FLOAT, 
							Shape.create(2L, 1L), 
							Tensor.options()
						)
						.name("x2:0")
						.putFloat(3f)
						.putFloat(2f)
						.build();
				y0 = session.feed(x2_0).forward().getOutput("y:0");

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