package org.jewzaam.mapper;

import java.io.FileInputStream;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.Test;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Modified from: https://github.com/fabric8io/data-mapper/blob/master/examples/map-2/src/test/java/org/example/TransformationTest.java
 * 
 * @author jewzaam nmalik
 */
public class TransformationTest extends CamelSpringTestSupport {

    @EndpointInject(uri = "mock:src2dest-test-output")
    private MockEndpoint resultEndpoint;

    @Produce(uri = "direct:src2dest-test-input")
    private ProducerTemplate startEndpoint;

    @Test
    public void transform() throws Exception {
        // setup expectations
        resultEndpoint.expectedMessageCount(1);
        resultEndpoint.expectedBodiesReceived(readFile("src/data/destination.xml"));

        // run test
        startEndpoint.sendBody(readFile("src/data/source.xml"));

        // verify results
        resultEndpoint.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("direct:src2dest-test-input")
                    .log("Before transformation:\n ${body}")
                    .to("ref:src2dest")
                    .log("After transformation:\n ${body}")
                    .to("mock:src2dest-test-output");
            }
        };
    }

    @Override
    protected AbstractXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("META-INF/spring/camel-context.xml");
    }

    private String readFile(String filePath) throws Exception {
        String content;
        FileInputStream fis = new FileInputStream(filePath);
        try {
            content = createCamelContext().getTypeConverter().convertTo(String.class, fis);
        } finally {
            fis.close();
        }
        return content;
    }
}
