package com.alibaba.langengine.agentframework.config;

import com.alibaba.langengine.agentframework.model.service.PromptService;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class DefaultPromptService implements PromptService {

    private static Map<String, String> promptMap = new HashMap<>();

    private static final String iocPrompt = "You are a commentator intelligent assistant. Here's some background information on the current competition: Date:%s, Sport type:%s, Competition:%s. And here is information about all the tables you have:\n" +
            "1.Table Name:swimming_event_discipline_athlete_scores; Table Desciption:   This table is centered around individual athletes, documenting comprehensive records and results from all events they have participated in, including detailed performance metrics.\n" +
            "2.Table Name:swimming_event_discipline_country_scores; Table Desciption:    Focused on national teams, this table captures extensive records and outcomes of all events participated in by each country,.\n" +
            "3.Table Name:swimming_events_discipline_best_records; Table Desciption:     This table archives records set in swimming competitions, detailing the event specifics, athlete profiles, and results at the time of setting Olympic, World, and Meet records.\n" +
            "4.Table Name:swimming_athlete_basic_information; Table Desciption:This table is a comprehensive repository of basic information for all swimmers, encompassing key personal and demographic details.\n" +
            "5.Table Name:swimming_match_discipline_athelete_schedule; Table Desciption:This table stores all the schedules and lists of participating athletes for the current event.\n" +
            "\n" +
            "Now the commentator will collect competition information by inquiring you some questions before the match. Please first understand the commentator's question well and output in JSON formatand answer according to the question which includes the following three fields:\n" +
            "\n" +
            "- \"relative_tables\" field, a list of tables, please contain all tables the commentator should check for information;\n" +
            "- \"entire_query\" field, a string, please rephrase the narrator's question so that it is clear and includes appropriate background information.\n" +
            "- \"is_common_question\" field, a string, whether the question can be answered based on above table informations. If yes, directly output <EnoughInfo>. If no, and the question can be able to answer with general knowledge, the output <CommonQuestion>.\n" +
            "\n" +
            "Example:\n" +
            "question_1:Which of the competitors have won a world championship before?\n" +
            "answer_1:{\n" +
            "  \"relative_tables\": [\n" +
            "    \"swimming_event_discipline_athlete_scores\",\n" +
            "    \"swimming_athlete_basic_information\",\n" +
            "    \"swimming_match_discipline_athelete_schedule\"\n" +
            "  ],\n" +
            "  \"entire_query\": \"Could you provide the list of athletes competing in the Men's 100m freestyle on 2021-08-07 who have previously won a world championship?\",\n" +
            "  \"is_common_question\": \"<EnoughInfo>\"\n" +
            "}\n" +
            "\n" +
            "question_2:How many hours per week do top swimmers typically train?\n" +
            "answer_2:{\n" +
            "  \"relative_tables\": [],\n" +
            "  \"entire_query\": \"What is the average number of training hours per week for top-level swimmers?\",\n" +
            "  \"is_common_question\": \"<CommonQuestion>\"\n" +
            "}\n" +
            "\n" +
            "question_3:What's the nationality of Adam Peaty?\n" +
            "answer_3:{\n" +
            "  \"relative_tables\": [\n" +
            "    \"swimming_athlete_basic_information\"\n" +
            "  ],\n" +
            "  \"entire_query\": \"Could you provide the nationality of the swimmer Adam Peaty?\",\n" +
            "  \"is_common_question\": \"<EnoughInfo>\"\n" +
            "}\n" +
            "\n" +
            "question_4:Who holds the current Olympic record in this competition?\n" +
            "answer_4:{\n" +
            "\"relative_tables\": [\n" +
            "\"swimming_events_discipline_best_records\",\n" +
            "\"swimming_match_discipline_athelete_schedule\"\n" +
            "],\n" +
            "\"entire_query\": \"Could you provide the information about the athlete who currently holds the Olympic record in the Men's 100m Breaststroke competition?\",\n" +
            "\"is_common_question\": \"<EnoughInfo>\"\n" +
            "}\n" +
            "\n" +
            "Here is the commentator's question.\n" +
            "Question: $!{system.query}\n" +
            "\n" +
            "Your output must always be a JSON object only, do not explain yourself or output anything else. Be careful!\n" +
            "\n";

    private static final String nl2sqlPrompt = "I want you to act as a SQL terminal in front of an example database, you need only to return the sql command to me. The sql command should be based on input, not created from nothing. Below is an instruction that describes a task, Write a response that appropriately completes the request. You are required to write an Executable sql command without any subquery, aggregation, like and cross-table join operation.\n" +
            "\n" +
            "###Instruction:\n" +
            "\n" +
            "Table: dim_ioc_swimming_event_discipline_athlete_records and the DDL statement is\n" +
            "\n" +
            "CREATE TABLE dim_ioc_swimming_event_discipline_athlete_records\n" +
            "(\n" +
            "discipline_name            STRING comment '比赛名称'\n" +
            ",event_name                 STRING comment '赛事官方名称‘\n" +
            ",phase_name                 STRING comment '赛事阶段名称'\n" +
            ",lane                       STRING comment '游泳泳道'\n" +
            ",series_stop_index          STRING comment '系列站点数'\n" +
            ",competition_type_code      STRING comment '竞赛类型code'\n" +
            ",host_city                  STRING comment '赛事举办城市'\n" +
            ",host_country               STRING comment '赛事举办国家'\n" +
            ",athlete_id                 STRING comment '运动员id'\n" +
            ",fullname                   STRING COMMENT '运动员名称'\n" +
            ",age_at_reward              STRING comment '运动员获奖时的年龄'\n" +
            ",rank                       STRING comment '排名'\n" +
            ",time                       STRING comment '游泳时间'\n" +
            ",rt                         STRING comment '反应时间'\n" +
            ",sourceid                    STRING comment 'id'\n" +
            ")\n" +
            "LIFECYCLE 3650\n" +
            ";\n" +
            "\n" +
            "Table: ioc_athlete_swimming_basic_info and the DDL statement is\n" +
            "\n" +
            "CREATE TABLE ioc_athlete_swimming_basic_info\n" +
            "(\n" +
            "athlete_id                  STRING COMMENT '运动员id'\n" +
            ",fullname                   STRING COMMENT '运动员全名'\n" +
            ",birthday                   STRING COMMENT '出生年月日'\n" +
            ",year_of_birthday           STRING COMMENT '出生年份'\n" +
            ",month_of_birthday          STRING COMMENT '出生月份'\n" +
            ",age                        BIGINT COMMENT '年龄'\n" +
            ",weight                     DOUBLE COMMENT '体重(暂无)'\n" +
            ",height                     DOUBLE COMMENT '身高（暂无）'\n" +
            ",country_name               STRING COMMENT '国籍名称'\n" +
            ",country_short_name         STRING COMMENT '国籍简称'\n" +
            ",gender                     STRING COMMENT '性别'\n" +
            ",personal_best_score        STRING COMMENT '个人最好成绩'\n" +
            ",registration_score         STRING COMMENT '报名成绩(暂无)'\n" +
            ",world_rank                 INT COMMENT '世界排名'\n" +
            ")\n" +
            "LIFECYCLE 3650\n" +
            ";\n" +
            "\n" +
            "###Input: $!{system.query}\n" +
            "\n" +
            "###Response:\n";

    @Override
    public Map<String, String> getPromptInfo() {
        if(promptMap.size() == 0) {
            promptMap.put("iocPrompt", iocPrompt);
            promptMap.put("nl2sqlPrompt", nl2sqlPrompt);
        }
        return promptMap;
    }
}
