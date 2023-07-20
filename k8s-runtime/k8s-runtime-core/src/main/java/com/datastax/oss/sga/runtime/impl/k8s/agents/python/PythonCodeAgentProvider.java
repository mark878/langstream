package com.datastax.oss.sga.runtime.impl.k8s.agents.python;

import com.datastax.oss.sga.api.model.AgentConfiguration;
import com.datastax.oss.sga.api.model.Application;
import com.datastax.oss.sga.api.model.Module;
import com.datastax.oss.sga.api.model.Resource;
import com.datastax.oss.sga.api.runtime.AgentNode;
import com.datastax.oss.sga.api.runtime.ComponentType;
import com.datastax.oss.sga.api.runtime.ComputeClusterRuntime;
import com.datastax.oss.sga.api.runtime.ExecutionPlan;
import com.datastax.oss.sga.impl.common.AbstractAgentProvider;
import com.datastax.oss.sga.impl.common.DefaultAgentNode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.datastax.oss.sga.runtime.impl.k8s.KubernetesClusterRuntime.CLUSTER_TYPE;

/**
 * Implements support for custom Agents written in Python.
 */
@Slf4j
public class PythonCodeAgentProvider extends AbstractAgentProvider {

    public PythonCodeAgentProvider() {
        super(Set.of("python-source", "python-sink", "python-function"), List.of(CLUSTER_TYPE));
    }

    @Override
    protected final ComponentType getComponentType(AgentConfiguration agentConfiguration) {
        switch (agentConfiguration.getType()) {
            case "python-source":
                return ComponentType.SOURCE;
            case "python-sink":
                return ComponentType.SINK;
            case "python-function":
                return ComponentType.FUNCTION;
            default:
                throw new IllegalArgumentException("Unsupported agent type: " + agentConfiguration.getType());
        }
    }


}