package com.datastax.oss.sga.impl.agents.ai;

import com.datastax.oss.sga.api.model.Module;
import com.datastax.oss.sga.api.model.Pipeline;
import com.datastax.oss.sga.api.runtime.AgentNode;
import com.datastax.oss.sga.api.runtime.ExecutionPlan;
import com.datastax.oss.sga.api.runtime.ExecutionPlanOptimiser;
import com.datastax.oss.sga.impl.common.DefaultAgentNode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public final class GenAIToolKitExecutionPlanOptimizer implements ExecutionPlanOptimiser {

    @Override
    public boolean canMerge(AgentNode previousAgent, AgentNode agentImplementation) {
        if (Objects.equals(previousAgent.getAgentType(), GenAIToolKitFunctionAgentProvider.AGENT_TYPE)
                && Objects.equals(previousAgent.getAgentType(), agentImplementation.getAgentType())
                && previousAgent instanceof DefaultAgentNode agent1
                && agentImplementation instanceof DefaultAgentNode agent2) {
            Map<String, Object> purgedConfiguration1 = new HashMap<>(agent1.getConfiguration());
            purgedConfiguration1.remove("steps");
            Map<String, Object> purgedConfiguration2 = new HashMap<>(agent2.getConfiguration());
            purgedConfiguration2.remove("steps");

            // test query steps with different datasources
            Object datasource1 = purgedConfiguration1.remove("datasource");
            Object datasource2 = purgedConfiguration2.remove("datasource");

            if (datasource1 != null && datasource2 != null) {
                log.info("Comparing datasources {} and {}", datasource1, datasource2);
                if (!Objects.equals(datasource1, datasource2)) {
                    log.info("Agents {} and {} cannot be merged (different datasources)",
                            previousAgent, agentImplementation);
                    return false;
                }
            }

            log.info("Comparing {} and {}", purgedConfiguration1, purgedConfiguration2);
            return purgedConfiguration1.equals(purgedConfiguration2);
        }
        log.info("Agents {} and {} cannot be merged", previousAgent, agentImplementation);
        return false;
    }

    @Override
    public AgentNode mergeAgents(Module module, Pipeline pipeline, AgentNode previousAgent, AgentNode agentImplementation,
                                 ExecutionPlan applicationInstance) {
        if (Objects.equals(previousAgent.getAgentType(), agentImplementation.getAgentType())
                && previousAgent instanceof DefaultAgentNode agent1
                && agentImplementation instanceof DefaultAgentNode agent2) {

            log.info("Merging agents");
            log.info("Agent 1: {}", agent1);
            log.info("Agent 2: {}", agent2);

            Map<String, Object> configurationWithoutSteps1 = new HashMap<>(agent1.getConfiguration());
            List<Map<String, Object>> steps1 = (List<Map<String, Object>>) configurationWithoutSteps1.remove("steps");
            Map<String, Object> configurationWithoutSteps2 = new HashMap<>(agent2.getConfiguration());
            List<Map<String, Object>> steps2 = (List<Map<String, Object>>) configurationWithoutSteps2.remove("steps");

            List<Map<String, Object>> mergedSteps = new ArrayList<>();
            mergedSteps.addAll(steps1);
            mergedSteps.addAll(steps2);

            Map<String, Object> result = new HashMap<>();
            result.putAll(configurationWithoutSteps1);
            result.putAll(configurationWithoutSteps2);
            result.put("steps", mergedSteps);

            log.info("Discarding topic {}", agent1.getInputConnection());
            applicationInstance.discardTopic(agent1.getOutputConnection());

            agent1.overrideConfigurationAfterMerge(agent1.getAgentType(), result, agent2.getOutputConnection());

            log.info("Discarding topic {}", agent2.getInputConnection());
            applicationInstance.discardTopic(agent2.getInputConnection());
            return previousAgent;
        }
        throw new IllegalStateException();
    }
}