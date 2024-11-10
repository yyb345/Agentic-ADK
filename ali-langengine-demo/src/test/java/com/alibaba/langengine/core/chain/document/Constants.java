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
package com.alibaba.langengine.core.chain.document;

public class Constants {

    public static final String OPEN_INFO_CONTENT = "淘宝开放平台（Taobao Open Platform，简称TOP）是阿里与外部生态互联互通的重要开放途径，通过开放的产品技术把阿里经济体一系列基础服务，像水、电、煤一样输送给我们的商家、开发者、社区媒体以及其他合作伙伴，推动行业的定制、创新、进化, 并最终促成新商业文明生态圈。\n" +
            "\n" +
            "开放平台当前支持集团天猫、淘宝、阿里妈妈、飞猪、ICBU、AE、零售通、盒马、Lazada、钉钉、云OS、智慧园区、虾米、阿里通信等集团绝大多数业务的能力和数据开放(当前集团仅1688，菜鸟CP、阿里云自建开放平台，其他业务全部使用开放平台进行阿里能力开放)，当前支持的业务标签(业务线)共175个，开放API数量10000+，注册开发者240万+，日均API调用量约190亿次，双11当天调用量340亿+，峰值70万QPS。\n" +
            "\n" +
            "开放平台支持的开放模式包括五类：\n" +
            "1. 官方开放（开放API最多，最常见的开放）：服务的提供方是内部各业务方，调用方为外部和内部。\n" +
            "2. 官方集成（ISV按照官方标准与阿里进行系统对接，如淘宝游戏充值，盒马会员通，全渠道会员通）：服务提供方是ISV，标准由平台定义，ISV只需要负责实现。\n" +
            "3. 三方互通（三方ISV与三方ISV通过平台进行信息互通，代表作是奇门仓储业务）：调用和实现方都属于ISV，平台制定标准，ISV需要管理路由，主要降低ISV多对多的对接效率问题。\n" +
            "4. 三方开放（云网关，让ISV具备向其他ISV开放接口的能力）： 平台将鉴权，流控等能力开放给外部isv。\n" +
            "5. 消息服务：通过消息的方式，将信息通知给ISV，只允许同步状态变更数据，比如：订单状态变更消息等。";

    public static final String OPEN_WORD_CONTENT = "在业务开放的过程中，我们需要对Top有个快速的理解。\n" +
            "1.基本概念\n" +
            "在通过Top开发应用或者开放接口之前，我们需要对TOP相关的一些概念有了解。首先看下下图：\n" +
            "\n" +
            "\n" +
            "如上图，我们需要明确以下：\n" +
            "1. 对于一个开发者，如果要在Top上开发应用，首先要成为一个开发者ISV，并且获取对应api接口的权限。\n" +
            "2. ISV开发的应用是给阿里的卖家或者买家用的。因此卖家或买家在使用应用前需要授权该应用。\n" +
            "3. 阿里用户在使用三方软件操作中，所有对他商品、交易的操作都是从三方应用发起的 ，而不是直接在淘宝的页面上进行操作。\n" +
            "4. 因此，用户在使用应用前，必须要到淘宝的授权中心，告诉淘宝，说**应用可以操作我的数据。这个过程也就是授权，应用获取sessionkey（其他关键词：token）的过程。因此可以看到sessionkey 是 用户授权三方应用可以访问他数据的一个凭证。\n" +
            "5. 当应用从淘宝取到授权信息之后，才能调接口操作用户的数据。\n" +
            "6. 当然 开发者也可能是卖家自身，就是说我有能力在TOP平台上开发工具给我自己使用。\n" +
            "接下来对上面提到的概念进行详细说明。\n" +
            "2.ISV\n" +
            "对于一个用户如果要成为ISV，则需要：\n" +
            "\n" +
            "\n" +
            "ISV开发者中心： http://console.open.taobao.com/ ， 在该后台可以管理与TOP相关的所有信息。因此建议开发者对页面的各个链接都操作下，看看具体的功能，说不定哪一天你可能就需要里面的某些操作。";

    public static final String OPEN_DEMO_CONTENT = "如何上传文件？";
}
