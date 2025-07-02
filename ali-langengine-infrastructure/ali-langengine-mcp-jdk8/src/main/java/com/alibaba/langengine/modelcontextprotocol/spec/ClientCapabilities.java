/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * Clients can implement additional features to enrich connected MCP servers with
 * additional capabilities. These capabilities can be used to extend the functionality
 * of the server, or to provide additional information to the server about the
 * client's capabilities.
 *
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ClientCapabilities {
    private final Map<String, Object> experimental;
    private final RootCapabilities roots;
    private final Sampling sampling;

    public ClientCapabilities(
            @JsonProperty("experimental") Map<String, Object> experimental,
            @JsonProperty("roots") RootCapabilities roots,
            @JsonProperty("sampling") Sampling sampling) {
        this.experimental = experimental;
        this.roots = roots;
        this.sampling = sampling;
    }

    public Map<String, Object> experimental() {
        return experimental;
    }

    public RootCapabilities roots() {
        return roots;
    }

    public Sampling sampling() {
        return sampling;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientCapabilities that = (ClientCapabilities) o;
        return Objects.equals(experimental, that.experimental) &&
               Objects.equals(roots, that.roots) &&
               Objects.equals(sampling, that.sampling);
    }

    @Override
    public int hashCode() {
        return Objects.hash(experimental, roots, sampling);
    }

    @Override
    public String toString() {
        return "ClientCapabilities{" +
               "experimental=" + experimental +
               ", roots=" + roots +
               ", sampling=" + sampling +
               '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Roots define the boundaries of where servers can operate within the filesystem,
     * allowing them to understand which directories and files they have access to.
     * Servers can request the list of roots from supporting clients and
     * receive notifications when that list changes.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
@Data
    public static class RootCapabilities {
        private final Boolean listChanged;

        public RootCapabilities(@JsonProperty("listChanged") Boolean listChanged) {
            this.listChanged = listChanged;
        }

        public Boolean listChanged() {
            return listChanged;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RootCapabilities that = (RootCapabilities) o;
            return Objects.equals(listChanged, that.listChanged);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listChanged);
        }

        @Override
        public String toString() {
            return "RootCapabilities{" +
                   "listChanged=" + listChanged +
                   '}';
        }
    }

    /**
     * Provides a standardized way for servers to request LLM
     * sampling ("completions" or "generations") from language
     * models via clients. This flow allows clients to maintain
     * control over model access, selection, and permissions
     * while enabling servers to leverage AI capabilities—with
     * no server API keys necessary. Servers can request text or
     * image-based interactions and optionally include context
     * from MCP servers in their prompts.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public static class Sampling {
        public Sampling() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o != null && getClass() == o.getClass();
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "Sampling{}";
        }
    }

    public static class Builder {
        private Map<String, Object> experimental;
        private RootCapabilities roots;
        private Sampling sampling;

        public Builder experimental(Map<String, Object> experimental) {
            this.experimental = experimental;
            return this;
        }

        public Builder roots(Boolean listChanged) {
            this.roots = new RootCapabilities(listChanged);
            return this;
        }

        public Builder sampling() {
            this.sampling = new Sampling();
            return this;
        }

        public ClientCapabilities build() {
            return new ClientCapabilities(experimental, roots, sampling);
        }
    }
}
