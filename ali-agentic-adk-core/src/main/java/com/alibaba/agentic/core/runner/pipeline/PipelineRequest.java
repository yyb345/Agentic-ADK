package com.alibaba.agentic.core.runner.pipeline;

import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/10 17:25
 */
@Builder
@Data
@ToString
public class PipelineRequest {

    //执行的pipeline的code list
    private List<String> pipeCodeList;

    private FlowDefinition flowDefinition;

    // originRequest -> request
    private Map<String, Object> request;

}
