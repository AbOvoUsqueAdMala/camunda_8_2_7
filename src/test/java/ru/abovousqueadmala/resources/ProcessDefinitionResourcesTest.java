package ru.abovousqueadmala.resources;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessDefinitionResourcesTest {

    private static final String BPMN_NS = "http://www.omg.org/spec/BPMN/20100524/MODEL";
    private static final String ZEEBE_NS = "http://camunda.org/schema/zeebe/1.0";
    private static final String DMN_NS = "https://www.omg.org/spec/DMN/20191111/MODEL/";

    @Test
    void demoProcessCallsNestedApprovalProcess() throws Exception {
        Document document = parseXml("bpmn/demo-process.bpmn");

        assertThat(document.getElementsByTagNameNS(BPMN_NS, "process").getLength()).isEqualTo(1);
        assertThat(document.getElementsByTagNameNS(BPMN_NS, "callActivity").getLength()).isEqualTo(1);
        assertThat(document.getElementsByTagNameNS(BPMN_NS, "subProcess").getLength()).isEqualTo(0);

        Element calledElement = (Element) document.getElementsByTagNameNS(ZEEBE_NS, "calledElement").item(0);
        assertThat(calledElement).isNotNull();
        assertThat(calledElement.getAttribute("processId")).isEqualTo("nested-approval-process");
        assertThat(calledElement.getAttribute("bindingType")).isEqualTo("deployment");
    }

    @Test
    void nestedApprovalProcessContainsDecisionAndWorkerTask() throws Exception {
        Document document = parseXml("bpmn/nested-approval-process.bpmn");

        assertThat(document.getElementsByTagNameNS(BPMN_NS, "process").getLength()).isEqualTo(1);
        assertThat(document.getElementsByTagNameNS(BPMN_NS, "businessRuleTask").getLength()).isEqualTo(1);
        assertThat(document.getElementsByTagNameNS(BPMN_NS, "serviceTask").getLength()).isEqualTo(1);

        Element calledDecision = (Element) document.getElementsByTagNameNS(ZEEBE_NS, "calledDecision").item(0);
        assertThat(calledDecision).isNotNull();
        assertThat(calledDecision.getAttribute("decisionId")).isEqualTo("nested-approval-decision");
        assertThat(calledDecision.getAttribute("resultVariable")).isEqualTo("approvalDecision");
        assertThat(calledDecision.getAttribute("bindingType")).isEqualTo("deployment");
    }

    @Test
    void nestedApprovalDecisionExistsAndContainsRules() throws Exception {
        Document document = parseXml("dmn/nested-approval-decision.dmn");

        Element decision = (Element) document.getElementsByTagNameNS(DMN_NS, "decision").item(0);
        assertThat(decision).isNotNull();
        assertThat(decision.getAttribute("id")).isEqualTo("nested-approval-decision");

        Element output = (Element) document.getElementsByTagNameNS(DMN_NS, "output").item(0);
        assertThat(output).isNotNull();
        assertThat(output.getAttribute("typeRef")).isEqualTo("boolean");

        assertThat(document.getElementsByTagNameNS(DMN_NS, "rule").getLength()).isEqualTo(2);
    }

    private Document parseXml(String classpathLocation) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(classpathLocation)) {
            assertThat(inputStream)
                    .withFailMessage("Resource %s was not found on the classpath", classpathLocation)
                    .isNotNull();
            return factory.newDocumentBuilder().parse(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read resource " + classpathLocation, exception);
        }
    }
}
