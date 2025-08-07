package com.alibaba.agentic.core.tools;

import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.executor.SystemContext;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.google.adk.tools.Annotations;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import io.reactivex.rxjava3.core.Flowable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/8 16:01
 */
public interface FunctionTool extends BaseTool {

    static FunctionTool creat(Object instance, Method func) {
        String name = func.isAnnotationPresent(Annotations.Schema.class) && !func.getAnnotation(Annotations.Schema.class).name().isEmpty()
                ? func.getAnnotation(Annotations.Schema.class).name() : func.getName();
        String description = func.isAnnotationPresent(Annotations.Schema.class)
                ? func.getAnnotation(Annotations.Schema.class).description() : "";
        FunctionDeclaration.Builder builder = FunctionDeclaration.builder().name(name).description(description);
        FunctionDeclaration functionDeclaration = build(func, builder);
        FunctionTool functionTool = new FunctionTool() {
            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                Parameter[] parameters = func.getParameters();
                Object[] arguments = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    String paramName =
                            parameters[i].isAnnotationPresent(Annotations.Schema.class)
                                    && !parameters[i].getAnnotation(Annotations.Schema.class).name().isEmpty()
                                    ? parameters[i].getAnnotation(Annotations.Schema.class).name()
                                    : parameters[i].getName();
                    if (parameters[i].getType().isAssignableFrom(SystemContext.class)) {
                        arguments[i] = systemContext;
                        continue;
                    }
                    if (!args.containsKey(paramName)) {
                        throw new BaseException(
                                String.format(
                                        "The parameter '%s' was not found in the arguments provided by the model.",
                                        paramName), ErrorEnum.SYSTEM_ERROR);
                    }
                    Class<?> paramType = parameters[i].getType();
                    Object argValue = args.get(paramName);
                    if (paramType.equals(List.class)) {
                        if (argValue instanceof List) {
                            Type type =
                                    ((ParameterizedType) parameters[i].getParameterizedType())
                                            .getActualTypeArguments()[0];
                            arguments[i] = createList((List<Object>) argValue, (Class) type);
                            continue;
                        }
                    } else if (argValue instanceof Map) {
                        arguments[i] = new ObjectMapper().convertValue(argValue, paramType);
                        continue;
                    }
                    arguments[i] = castValue(argValue, paramType);
                }
                try {
                    Object result = func.invoke(instance, arguments);
                    if (result == null) {
                        return Flowable.empty();
                    } else if (result instanceof Flowable) {
                        return (Flowable<Map<String, Object>>) result;
                    } else {
                        return Flowable.just((Map<String, Object>) result);
                    }
                } catch (Exception e) {
                    return Flowable.error(e);
                }
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public FunctionDeclaration declaration() {
                return functionDeclaration;
            }

        };
        return functionTool;
    }

    private static List<Object> createList(List<Object> values, Class<?> type) {
        List<Object> list = new ArrayList<>();
        // List of parameterized type is not supported.
        if (type == null) {
            return list;
        }
        Class<?> cls = type;
        for (Object value : values) {
            if (cls == Integer.class
                    || cls == Long.class
                    || cls == Double.class
                    || cls == Float.class
                    || cls == Boolean.class
                    || cls == String.class) {
                list.add(castValue(value, cls));
            } else {
                list.add(new ObjectMapper().convertValue(value, type));
            }
        }
        return list;
    }

    private static Object castValue(Object value, Class<?> type) {
        if (type.equals(Integer.class) || type.equals(int.class)) {
            if (value instanceof Integer) {
                return value;
            }
        }
        if (type.equals(Long.class) || type.equals(long.class)) {
            if (value instanceof Long || value instanceof Integer) {
                return value;
            }
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            if (value instanceof Double d) {
                return d.doubleValue();
            }
            if (value instanceof Float f) {
                return f.doubleValue();
            }
            if (value instanceof Integer i) {
                return i.doubleValue();
            }
            if (value instanceof Long l) {
                return l.doubleValue();
            }
        } else if (type.equals(Float.class) || type.equals(float.class)) {
            if (value instanceof Double d) {
                return d.floatValue();
            }
            if (value instanceof Float f) {
                return f.floatValue();
            }
            if (value instanceof Integer i) {
                return i.floatValue();
            }
            if (value instanceof Long l) {
                return l.floatValue();
            }
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            if (value instanceof Boolean) {
                return value;
            }
        } else if (type.equals(String.class)) {
            if (value instanceof String) {
                return value;
            }
        }
        return new ObjectMapper().convertValue(value, type);
    }

    private static FunctionDeclaration build(Method func, FunctionDeclaration.Builder builder) {
        List<String> required = new ArrayList<>();
        Map<String, Schema> properties = new LinkedHashMap<>();
        for (Parameter param : func.getParameters()) {
            String paramName =
                    param.isAnnotationPresent(Annotations.Schema.class)
                            && !param.getAnnotation(Annotations.Schema.class).name().isEmpty()
                            ? param.getAnnotation(Annotations.Schema.class).name()
                            : param.getName();
            if (param.getType().isAssignableFrom(SystemContext.class)) {
                continue;
            }
            required.add(paramName);
            properties.put(paramName, buildSchemaFromParameter(param));
        }
        builder.parameters(
                Schema.builder().required(required).properties(properties).type("OBJECT").build());

        Type returnType = func.getGenericReturnType();
        if (returnType != Void.TYPE) {
            Type realReturnType = returnType;
            if (returnType instanceof ParameterizedType parameterizedReturnType) {
                String returnTypeName = ((Class<?>) parameterizedReturnType.getRawType()).getName();
                if (returnTypeName.equals("io.reactivex.rxjava3.core.Flowable")) {
                    returnType = parameterizedReturnType.getActualTypeArguments()[0];
                    if (returnType instanceof ParameterizedType parameterizedType) {
                        returnTypeName = ((Class<?>) parameterizedType.getRawType()).getName();
                    }
                }
                if (returnTypeName.equals("java.util.Map")
                        || returnTypeName.equals("com.google.common.collect.ImmutableMap")) {
                    return builder.response(buildSchemaFromType(returnType)).build();
                }
            }
            throw new BaseException(
                    "Return type should be Map or Flowable<Map>, but it was "
                            + realReturnType.getTypeName(), ErrorEnum.SYSTEM_ERROR);
        }
        return builder.build();
    }

    private static Schema buildSchemaFromParameter(Parameter param) {
        Schema.Builder builder = Schema.builder();
        if (param.isAnnotationPresent(Annotations.Schema.class)
                && !param.getAnnotation(Annotations.Schema.class).description().isEmpty()) {
            builder.description(param.getAnnotation(Annotations.Schema.class).description());
        }
        switch (param.getType().getName()) {
            case "java.lang.String" -> builder.type("STRING");
            case "boolean", "java.lang.Boolean" -> builder.type("BOOLEAN");
            case "int", "java.lang.Integer" -> builder.type("INTEGER");
            case "double", "java.lang.Double", "float", "java.lang.Float", "long", "java.lang.Long" ->
                    builder.type("NUMBER");
            case "java.util.List" -> builder
                    .type("ARRAY")
                    .items(
                            buildSchemaFromType(
                                    ((ParameterizedType) param.getParameterizedType())
                                            .getActualTypeArguments()[0]));
            case "java.util.Map" -> builder.type("OBJECT");
            default -> {
                ObjectMapper objectMapper = new ObjectMapper();
                BeanDescription beanDescription = objectMapper.getSerializationConfig()
                        .introspect(objectMapper.constructType(param.getType()));
                Map<String, Schema> properties = new LinkedHashMap<>();
                for (BeanPropertyDefinition property : beanDescription.findProperties()) {
                    properties.put(property.getName(), buildSchemaFromType(property.getRawPrimaryType()));
                }
                builder.type("OBJECT").properties(properties);
            }
        }
        return builder.build();
    }

    static Schema buildSchemaFromType(Type type) {
        Schema.Builder builder = Schema.builder();
        if (type instanceof ParameterizedType parameterizedType) {
            switch (((Class<?>) parameterizedType.getRawType()).getName()) {
                case "java.util.List" -> builder
                        .type("ARRAY")
                        .items(buildSchemaFromType(parameterizedType.getActualTypeArguments()[0]));
                case "java.util.Map", "com.google.common.collect.ImmutableMap" -> builder.type("OBJECT");
                default -> throw new IllegalArgumentException("Unsupported generic type: " + type);
            }
        } else if (type instanceof Class<?> clazz) {
            switch (clazz.getName()) {
                case "java.lang.String" -> builder.type("STRING");
                case "boolean", "java.lang.Boolean" -> builder.type("BOOLEAN");
                case "int", "java.lang.Integer" -> builder.type("INTEGER");
                case "double", "java.lang.Double", "float", "java.lang.Float", "long", "java.lang.Long" ->
                        builder.type("NUMBER");
                case "java.util.Map", "com.google.common.collect.ImmutableMap" -> builder.type("OBJECT");
                default -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    BeanDescription beanDescription = objectMapper.getSerializationConfig()
                            .introspect(objectMapper.constructType(type));
                    Map<String, Schema> properties = new LinkedHashMap<>();
                    for (BeanPropertyDefinition property : beanDescription.findProperties()) {
                        properties.put(property.getName(), buildSchemaFromType(property.getRawPrimaryType()));
                    }
                    builder.type("OBJECT").properties(properties);
                }
            }
        }
        return builder.build();
    }

    String description();

    FunctionDeclaration declaration();
}
