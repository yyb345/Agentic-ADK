package com.alibaba.langengine.lindorm.vectorstore;

import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.SearchExtBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class LVectorExtBuilder extends SearchExtBuilder {

    final Map<String, String> searchParams;
    protected final String name;

    public LVectorExtBuilder(String name, Map<String, String> searchParams) {
        this.name = name;
        this.searchParams = searchParams;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeMap(searchParams, StreamOutput::writeString, StreamOutput::writeString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LVectorExtBuilder that = (LVectorExtBuilder) o;
        return Objects.equals(searchParams, that.searchParams) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchParams, name);
    }

    @Override
    public String getWriteableName() {
        return name;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(name);
        for (Map.Entry<String, String> searchParam : searchParams.entrySet()) {
            builder.field(searchParam.getKey(), searchParam.getValue());
        }
        builder.endObject();
        return builder;
    }
}