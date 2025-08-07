package com.alibaba.agentic.core.engine.node;

import com.alibaba.agentic.core.engine.constants.NodeIdConstant;
import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import com.alibaba.agentic.core.engine.utils.XmlUtils;
import com.alibaba.agentic.core.flows.storage.FlowStorageService;
import com.alibaba.agentic.core.utils.ApplicationContextUtil;
import com.alibaba.smart.framework.engine.SmartEngine;
import com.alibaba.smart.framework.engine.bpmn.constant.BpmnNameSpaceConstant;
import com.alibaba.smart.framework.engine.constant.SmartBase;
import com.alibaba.smart.framework.engine.exception.EngineException;
import com.alibaba.smart.framework.engine.model.assembly.ProcessDefinition;
import com.alibaba.smart.framework.engine.service.command.RepositoryCommandService;
import com.alibaba.smart.framework.engine.service.query.RepositoryQueryService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.StringWriter;
import java.util.*;

@Slf4j
@Data
public class FlowCanvas {

    private static final String SMART_NAMESPACE_PREFIX = "smart";

    // 执行的头节点
    private FlowNode root;
    // 执行的session 信息存储的方式
    // 整个流程中共享的全局变量
    private FlowConfig flowConfig;

    private String processDefinitionId;

    private String version;

    // 部署最终生成的bpmn xml内容
    public FlowDefinition deploy() {

        // 初始化一个xml document
        Document document = DocumentHelper.createDocument();

        // 创建process
        Element definitionsElement = createDefinitionsElement(document);

        if (StringUtils.isEmpty(processDefinitionId)) {
            processDefinitionId = UUID.randomUUID().toString();
        }
        if (StringUtils.isEmpty(version)) {
            version = "1.0.0";
        }

        //校验一下是否被覆盖
        Element processElement = createProcessElement(definitionsElement, processDefinitionId, version);

        //创建start节点以及指向root节点的边的片段
        addStartNode(processElement, root);

        // 依次创建每个节点的片段
        Deque<FlowNode> nodeQueue = new ArrayDeque<>();
        nodeQueue.offer(root);
        // 记录访问过的节点，防止有环情况进入死循环
        Set<String> visitedNodeIdSet = new HashSet<>();
        visitedNodeIdSet.add(root.getId());
        while (!nodeQueue.isEmpty()) {
            FlowNode currentNode = nodeQueue.poll();
            currentNode.generate(processElement);
            if (CollectionUtils.isNotEmpty(currentNode.getConditionalContainerList())) {
                currentNode.getConditionalContainerList().forEach(conditionalFlowNode -> {
                    currentNode.registerCondition(conditionalFlowNode);
                    if (Objects.nonNull(conditionalFlowNode.getFlowNode()) && !visitedNodeIdSet.contains(conditionalFlowNode.getFlowNode().getId())) {
                        nodeQueue.offer(conditionalFlowNode.getFlowNode());
                        visitedNodeIdSet.add(conditionalFlowNode.getFlowNode().getId());
                    }
                });
                // 只有在conditionalContainerList非空时，elseNext才有效
                if (Objects.nonNull(currentNode.getElseNext()) && !visitedNodeIdSet.contains(currentNode.getElseNext().getId())) {
                    nodeQueue.offer(currentNode.getElseNext());
                    visitedNodeIdSet.add(currentNode.getElseNext().getId());
                }
            } else if (Objects.nonNull(currentNode.getNext())) {
                if (!visitedNodeIdSet.contains(currentNode.getNext().getId())){
                    nodeQueue.offer(currentNode.getNext());
                    visitedNodeIdSet.add(currentNode.getNext().getId());
                }
            } else {
                // 持久化一个指向结束节点的边
                this.addEdgeToEnd(processElement, currentNode);
            }
        }
        this.addEndNode(processElement);

        String xmlString = getXmlString(document);
        // deploy到smart engine中
        this.deployProcessDefinition(processDefinitionId, version, xmlString);

        FlowDefinition flowDefinition = new FlowDefinition();
        flowDefinition.setDefinitionId(processDefinitionId);
        flowDefinition.setVersion(version);
        flowDefinition.setBpmnXml(xmlString);

        // 返回最终的definition
        return flowDefinition;
    }


    /**
     * 创建definitions元素并添加命名空间
     */
    private Element createDefinitionsElement(Document document) {
        Namespace bpmnNamespace = new Namespace("", BpmnNameSpaceConstant.NAME_SPACE);
        Namespace smartNamespace = new Namespace(SMART_NAMESPACE_PREFIX, SmartBase.SMART_NS);
        Element definitionsElement = document.addElement("definitions");
        definitionsElement.add(bpmnNamespace);
        definitionsElement.add(smartNamespace);
        return definitionsElement;
    }

    /**
     * 创建process元素并设置基本属性
     */
    private Element createProcessElement(Element definitionsElement, String processDefinitionId, String version) {
        Element processElement = definitionsElement.addElement(QName.get("process", Namespace.NO_NAMESPACE));
        processElement.addAttribute("id", processDefinitionId);
        processElement.addAttribute("isExecutable", "true");
        processElement.addAttribute("version", version);
        return processElement;
    }

    private String getXmlString(Document document) {
        StringWriter stringWriter = new StringWriter();
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            XMLWriter writer = new XMLWriter(stringWriter, format);
            writer.write(document);
            writer.close();
            return stringWriter.toString().replaceAll("xmlns=\"\"", "");
        } catch (Throwable e) {
            log.error("Error generating XML string", e);
            return null;
        }
    }

    private Element addStartNode(Element processElement, FlowNode node) {
        Element startEvent = processElement.addElement(QName.get("startEvent", Namespace.NO_NAMESPACE));
        startEvent.addAttribute("id", NodeIdConstant.START);
        XmlUtils.genEdge(processElement, NodeIdConstant.START, node.getId());
        return startEvent;
    }

    private Element addEndNode(Element processElement) {
        Element endEvent = processElement.addElement(QName.get("endEvent", Namespace.NO_NAMESPACE));
        endEvent.addAttribute("id", NodeIdConstant.END);
        return endEvent;
    }

    private void addEdgeToEnd(Element processElement, FlowNode node) {
        XmlUtils.genEdge(processElement, node.getId(), NodeIdConstant.END);
    }


    public void deployProcessDefinition(String defineId, String version, String xml) {
        SmartEngine smartEngine = (SmartEngine) ApplicationContextUtil.getBean(SmartEngine.class);
        FlowStorageService flowStorageService = (FlowStorageService) ApplicationContextUtil.getBean(FlowStorageService.class);
        RepositoryQueryService repositoryQueryService = smartEngine.getRepositoryQueryService();
        try {
            String bpmnXml = flowStorageService.getBpmnXml(defineId, version);
            if (bpmnXml != null) {
                log.info("bpmnXml already exists for defineId: {}, version: {}. Overwriting existing FlowDefinition!", defineId, version);
            }
            flowStorageService.saveBpmnXml(new FlowDefinition(defineId, version, xml));
            ProcessDefinition processDefinition = repositoryQueryService.getCachedProcessDefinition(defineId, version);
            if (processDefinition != null) {
                return;
            }
            RepositoryCommandService repositoryCommandService = smartEngine.getRepositoryCommandService();
            repositoryCommandService.deployWithUTF8Content(xml);
            log.info("deployProcessDefinition success, defineId: {}, version: {}", defineId, version);
        } catch (Exception e) {
            log.error("deployProcessDefinition fail, defineId: {}, version: {}", defineId, version, e);
            throw new EngineException(e);
        }
    }

}
