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
import java.util.HashMap;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for TinyYoloV2 model.
 */
public class TinyYoloV2Test extends ForwarderTestCase {

	//private static Logger logger = LoggerFactory.getLogger(TinyYoloV2Test.class);

	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public TinyYoloV2Test(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() throws FileNotFoundException, IOException {
		return new TestSuite(TinyYoloV2Test.class);
	}

	public void testModelWithOpsetV1() throws FileNotFoundException, NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			OperationNotSupportedException, IOException {
		Map<String, String> tensorPairPaths = new HashMap<>();
		for (int n = 0; n < 3; n++) {
			tensorPairPaths.put(
					"/tiny_yolov2/opset_v1/test_data_set_" + n + "/input_0.pb",
					"/tiny_yolov2/opset_v1/test_data_set_" + n + "/output_0.pb"
					);
		}

		super.testModel(
				tensorPairPaths, 
				"/tiny_yolov2/opset_v1/model.onnx", 
				"image", 
				"grid",
				new String[] { "Tensorflow", "DL4J" }, 
				0.001f);
	}

	public void testModelWithOpsetV7() throws FileNotFoundException, NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			OperationNotSupportedException, IOException {
		Map<String, String> tensorPairPaths = new HashMap<>();
		for (int n = 0; n < 3; n++) {
			tensorPairPaths.put(
					"/tiny_yolov2/opset_v7/test_data_set_" + n + "/input_0.pb",
					"/tiny_yolov2/opset_v7/test_data_set_" + n + "/output_0.pb"
					);
		}

		super.testModel(
				tensorPairPaths, 
				"/tiny_yolov2/opset_v7/model.onnx", 
				"image", 
				"grid",
				new String[] { "Tensorflow", "DL4J" }, 
				0.001f);
	}

	public void testModelWithOpsetV8() throws FileNotFoundException, NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			OperationNotSupportedException, IOException {
		Map<String, String> tensorPairPaths = new HashMap<>();
		for (int n = 0; n < 3; n++) {
			tensorPairPaths.put(
					"/tiny_yolov2/opset_v8/test_data_set_" + n + "/input_0.pb",
					"/tiny_yolov2/opset_v8/test_data_set_" + n + "/output_0.pb");
		}

		this.testModel(
				tensorPairPaths, 
				"/tiny_yolov2/opset_v8/model.onnx", 
				"image", 
				"grid",
				new String[] { "Tensorflow", "DL4J" }, 
				0.001f);
	}

}