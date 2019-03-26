package org.openjdk.jmc.agent.test;

import static org.junit.Assert.assertNotNull;

import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.openjdk.jmc.agent.Agent;
import org.openjdk.jmc.agent.test.util.TestToolkit;

public class TestRetransform {
	
	/**
	 * Goals/Questions for TestRetransform
	 * 
	 * - How can we get the agent instance to test with?
	 * -- Where can we get instrumentation from for testing
	 * - Can we delay this test until the jar is built?
	 * - We need to open up jdk.attach for this on jdk11
	 */

	private static final String AGENT_OBJECT_NAME = "org.openjdk.jmc.jfr.agent:type=AgentController"; //$NON-NLS-1$
	private static final String TEST_CLASS = "org.openjdk.jmc.agent.test.TestRetransform";
	
	/**
	 * XML Description to check that we're parsing XML snippets into
	 * transform descriptors correctly
	 */
	private static final String XML_DESCRIPTION = "<jfragent>"
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

	@Test
	public void testRetransform() throws Exception {
		// Invoke retransform
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName name = new ObjectName(AGENT_OBJECT_NAME);
		Object[] parameters = {XML_DESCRIPTION};
		String[] signature = {String.class.getName()};
		Class<?>[] clazzes = (Class<?>[]) mbs.invoke(name, "retransformClasses", parameters, signature);
		assertNotNull(clazzes);
		if (Agent.getLogger().isLoggable(Level.FINE)) {
			for (Class<?> clazz : clazzes) {
				// If we've asked for verbose information, we write the generated class
				TraceClassVisitor visitor = new TraceClassVisitor(new PrintWriter(System.out));
				CheckClassAdapter checkAdapter = new CheckClassAdapter(visitor);
				ClassReader reader = new ClassReader(TestToolkit.getByteCode(clazz));
			}
		}
	}
	
	public void test() {
		//Dummy method for instrumentation
	}
}
