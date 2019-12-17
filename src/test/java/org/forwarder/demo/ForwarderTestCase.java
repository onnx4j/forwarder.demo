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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.io.FileUtils;
import org.forwarder.Backend;
import org.forwarder.Config;
import org.forwarder.Forwarder;
import org.forwarder.Session;
import org.forwarder.executor.impls.RayExecutor;
import org.onnx4j.Tensor;
import org.onnx4j.onnx.prototypes.OnnxProto3.TensorProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import junit.framework.TestCase;

public abstract class ForwarderTestCase extends TestCase {

	private static final int TENSOR_MAX_OUTPUT_LEN = 1000;

	private static Logger logger = LoggerFactory.getLogger(ForwarderTestCase.class);

	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public ForwarderTestCase(String testName) {
		super(testName);
	}

	protected void testModel(Map<String, String> tensorPairPaths, String modelPath, String inputName, String outputName,
			String[] backendNames, float tolerance) throws FileNotFoundException, IOException, NoSuchMethodException,
			SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, OperationNotSupportedException {
		String absoluteModelPath = URLDecoder.decode(TinyYoloV2Test.class.getResource(modelPath).getFile(), "utf-8");
		assertNotNull(absoluteModelPath);

		try (Forwarder forwarder = Forwarder
				.config(Config.builder().setDebug(true).setMemoryByteOrder(ByteOrder.LITTLE_ENDIAN).build())
				.load(absoluteModelPath).executor(RayExecutor.class)) {
			assert forwarder != null;
			for (Entry<String, String> tensorPairPath : tensorPairPaths.entrySet()) {
				Tensor inputTensor = this.loadTensor(forwarder, tensorPairPath.getKey());
				logger.debug("Loaded input tensor proto named \"{}\"", tensorPairPath.getKey());

				Tensor exceptedOutputTensor = this.loadTensor(forwarder, tensorPairPath.getValue());
				logger.debug("Loaded input tensor proto named \"{}\"", tensorPairPath.getValue());

				logger.debug("Input Tensor: {}", this.dumpTensor(inputTensor));
				logger.debug("Excepted Tensor: {}", this.dumpTensor(exceptedOutputTensor));

				for (String backendName : backendNames) {
					Backend<?> backend = forwarder.backend(backendName);
					try (Session<?> session = backend.newSession()) {
						Tensor y0 = session.feed(inputName, inputTensor).forward().getOutput(outputName);

						logger.debug("Actual: {}", this.dumpTensor(y0));

						this.assertSimilarity(y0, exceptedOutputTensor, 0.001f);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Failed to close forwarder instance", e);
		}
	}

	/**
	 * 判别两个比对的Tensor，数据类型和形状是否相等，数值上是否相似（在指定的容差范围内）
	 * 
	 * @param actual
	 * @param excepted
	 * @param tolerance
	 */
	protected void assertSimilarity(Tensor actual, Tensor excepted, float tolerance) {
		assertEquals(excepted.getValueInfo(), actual.getValueInfo());
		assertEquals(excepted.getData().capacity(), actual.getData().capacity());

		FloatBuffer actualFloat = actual.getData().asFloatBuffer();
		FloatBuffer exceptedFloat = excepted.getData().asFloatBuffer();

		for (int n = 0; n < actualFloat.capacity(); n++) {
			Float nActual = actualFloat.get();
			Float nExcepted = exceptedFloat.get();
			assertTrue(Math.abs((nActual) - (nExcepted)) <= tolerance);
		}
	}

	protected Tensor loadTensor(Forwarder forwarder, String tensorProtoName)
			throws InvalidProtocolBufferException, IOException {
		String tensorProtoPath = URLDecoder.decode(this.getClass().getResource(tensorProtoName).getFile(), "utf-8");
		assertNotNull(tensorProtoPath);
		TensorProto tensorProto = TensorProto.parseFrom(FileUtils.readFileToByteArray(new File(tensorProtoPath)));
		Tensor tensor = Tensor.toTensor(tensorProto, forwarder.getConfig().getTensorOptions());
		return tensor;
	}

	protected String dumpTensor(Tensor tensor) {
		String tensorString = tensor.toString().replaceAll("[\n\t]", "");
		return tensorString.length() > TENSOR_MAX_OUTPUT_LEN
				? tensorString.subSequence(0, TENSOR_MAX_OUTPUT_LEN) + " ..." : tensorString;
	}

}