/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The contents of this file are subject to the terms of either the Universal Permissive License
 * v 1.0 as shown at http://oss.oracle.com/licenses/upl
 *
 * or the following license:
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openjdk.jmc.agent.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.objectweb.asm.Type;
import org.openjdk.jmc.agent.TransformDescriptor;
import org.openjdk.jmc.agent.TransformRegistry;
import org.openjdk.jmc.agent.impl.DefaultTransformRegistry;
import org.openjdk.jmc.agent.test.util.TestToolkit;

public class TestDefaultTransformRegistry {
	
	private static final String XML_DESCRIPTION_UPDATE = "<jfragent>"
			+ "<events>"
			+ "<event id=\"testing.jfr.testI1\">"
			+ "<name>Test For Retransform and Update</name>"
			+ "<description>This is a test event.</description>"
			+ "<path>demo/retransformEvent1</path>"
			+ "<stacktrace>false</stacktrace>"
			+ "<class>org.openjdk.jmc.agent.test.TestRetransform</class>"
			+ "<method>"
			+ "<name>test</name>"
			+ "<descriptor>()V</descriptor>"
			+ "</method>"
			+ "<location>WRAP</location>"
			+ "</event>"
			+ "</events>"
			+ "</jfragent>";

	private static final String XML_DESCRIPTION_REPLACE = "<jfragent>"
			+ "<events>"
			+ "<event id=\"demo.jfr.test1\">"
			+ "<name>JFR Hello World Event 1 Replace </name>"
			+ "<description>Defined in the xml file and added by the agent.</description>"
			+ "<path>demo/jfrhelloworldevent1</path>"
			+ "<stacktrace>true</stacktrace>"
			+ "<class>org.openjdk.jmc.agent.test.InstrumentMe</class>"
			+ "<method>"
			+ "<name>printHelloWorldJFR1</name>"
			+ "<descriptor>()V</descriptor>"
			+ "</method>"
			+ "<location>WRAP</location>"
			+ "</event>"
			+ "</events>"
			+ "</jfragent>";
	
	public static String getTemplate() throws IOException {
		return TestToolkit.readTemplate(TestDefaultTransformRegistry.class, TestToolkit.DEFAULT_TEMPLATE_NAME);
	}
	
	@Test
	public void testHasPendingTransforms() throws XMLStreamException, IOException {
		TransformRegistry registry = DefaultTransformRegistry
				.from(TestToolkit.getProbesXMLFromTemplate(getTemplate(), "HasPendingTransforms")); //$NON-NLS-1$
		assertNotNull(registry);
		assertTrue(registry.hasPendingTransforms(Type.getInternalName(InstrumentMe.class)));
	}

	@Test
	public void testFrom() throws XMLStreamException, IOException {
		TransformRegistry registry = DefaultTransformRegistry
				.from(TestToolkit.getProbesXMLFromTemplate(getTemplate(), "From")); //$NON-NLS-1$
		assertNotNull(registry);
	}

	@Test
	public void testGetTransformData() throws XMLStreamException, IOException {
		TransformRegistry registry = DefaultTransformRegistry
				.from(TestToolkit.getProbesXMLFromTemplate(getTemplate(), "GetTransformData")); //$NON-NLS-1$
		assertNotNull(registry);
		List<TransformDescriptor> transformData = registry.getTransformData(Type.getInternalName(InstrumentMe.class));
		assertNotNull(transformData);
		assertTrue(transformData.size() > 0);
	}
	
	@Test
	public void testUpdate() throws XMLStreamException, IOException {
		TransformRegistry registry = DefaultTransformRegistry
				.from(TestToolkit.getProbesXMLFromTemplate(getTemplate(), "From")); //$NON-NLS-1$
		assertNotNull(registry);
		List<TransformDescriptor> descriptors = registry.update(XML_DESCRIPTION_UPDATE);
		assertNotNull(descriptors);
		assertEquals(descriptors.get(0).getClassName(), "org/openjdk/jmc/agent/test/TestRetransform");
		assertEquals(descriptors.get(0).getMethod().toString(), "test()V");
		assertTrue(registry.hasPendingTransforms("org/openjdk/jmc/agent/test/TestRetransform"));
	}

	@Test
	public void testUpdateNameCollision() throws XMLStreamException, IOException {
		TransformRegistry registry = DefaultTransformRegistry
				.from(TestToolkit.getProbesXMLFromTemplate(getTemplate(), "Replace")); //$NON-NLS-1$
		assertNotNull(registry);
		List<TransformDescriptor> descriptors = registry.update(XML_DESCRIPTION_REPLACE);
		assertNotNull(descriptors);
		assertTrue(descriptors.isEmpty());
	}

	@Test
	public void testReplace() throws XMLStreamException, IOException {
		TransformRegistry registry = DefaultTransformRegistry
				.from(TestToolkit.getProbesXMLFromTemplate(getTemplate(), "Replace")); //$NON-NLS-1$
		assertNotNull(registry);
		List<TransformDescriptor> descriptors = registry.replace(XML_DESCRIPTION_REPLACE);
		assertNotNull(descriptors);
		assertTrue(descriptors.size() == 1);
		assertEquals(descriptors.get(0).getClassName(), "org/openjdk/jmc/agent/test/InstrumentMe");
		assertEquals(descriptors.get(0).getMethod().toString(), "printHelloWorldJFR1()V");
		assertTrue(registry.hasPendingTransforms("org/openjdk/jmc/agent/test/InstrumentMe"));
	}

	@Test
	public void testClearAllTransformData() throws XMLStreamException, IOException {
		TransformRegistry registry = DefaultTransformRegistry
				.from(TestToolkit.getProbesXMLFromTemplate(getTemplate(), "clearAllTransformData")); //$NON-NLS-1$
		assertNotNull(registry);
		List<String> classesCleared = registry.clearAllTransformData();
		assertEquals(classesCleared.get(0),Type.getInternalName(InstrumentMe.class));
		assertNull(registry.getTransformData(Type.getInternalName(InstrumentMe.class)));
	}

}
