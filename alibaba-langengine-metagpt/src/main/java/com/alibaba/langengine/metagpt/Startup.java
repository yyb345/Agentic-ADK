/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.metagpt;

import com.alibaba.langengine.metagpt.roles.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 启动程序
 *
 * @author xiaoxuan.lp
 */
@Data
public class Startup {

    private double investment = 3.0d;

    private int nRound = 5;

    private boolean codeReview = false;

    private boolean runTests = false;

    private boolean implement = true;

    /**
     * 运行
     *
     * @param idea
     */
    public void run(String idea) {
        //软件公司
        SoftwareCompany company = new SoftwareCompany();

        //角色设定
        List<Role> roles = new ArrayList<>();
        roles.add(new ProductManager()); //产品经理角色
        roles.add(new Architect()); //架构师角色
        roles.add(new ProjectManager()); //项目经理角色
        roles.add(new Engineer()); //开发工程师角色

        //雇佣员工
        company.hire(roles);

        //老板需求设定
        company.startProject(idea);

        //开始运行
        company.run(nRound);
    }

    public static void main(String[] args) {
        Startup startup = new Startup();
        startup.run("帮我写一个打印\"hello world\"的python应用程序");
    }
}
