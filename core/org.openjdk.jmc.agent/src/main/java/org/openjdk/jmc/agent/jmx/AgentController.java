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
package org.openjdk.jmc.agent.jmx;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openjdk.jmc.agent.TransformDescriptor;
import org.openjdk.jmc.agent.TransformRegistry;

public class AgentController implements AgentControllerMBean {
	
	private static final Logger logger = Logger.getLogger(AgentController.class.getName());
	
	private final Instrumentation instrumentation;
	private final TransformRegistry registry;

	public AgentController(Instrumentation instrumentation, TransformRegistry registry) {
		this.instrumentation = instrumentation;
		this.registry = registry;
	}

	@Override
	public Class<?>[] retransformClasses(String xmlDescription) throws Exception {
		// Update the transformation registry to keep things consistent.
		List<TransformDescriptor> descriptors = registry.update(xmlDescription);
		Class<?>[] classesToRetransform = new Class[descriptors.size()];
		int i = 0;
		// Collect the classes so we can retransform them in one go.
		for (TransformDescriptor descriptor : descriptors) {
			try {
				Class<?> classToRetransform = Class.forName(descriptor.getClassName().replace('/', '.'));
				classesToRetransform[i] = classToRetransform;
				i++;
			} catch (ClassNotFoundException cnfe) {
				logger.log(Level.SEVERE, "Unable to find class: " + descriptor.getClassName(), cnfe);
			}
		}
		instrumentation.retransformClasses((Class<?>[]) classesToRetransform);
		return classesToRetransform;
	}
}
