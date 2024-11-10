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
package com.alibaba.langengine.metagpt.prompts;

public class UseLibSopPrompt {

    public static final String SOP_SYSTEM = "SYSTEM:\n" +
            "You serve as an assistant that helps me play the game Minecraft.\n" +
            "I will give you a goal in the game. Please think of a plan to achieve the goal, and then write a sequence of actions to realize the plan. The requirements and instructions are as follows:\n" +
            "1. You can only use the following functions. Don’t make plans purely based on your experience, think about how to use these functions.\n" +
            "explore(object, strategy)\n" +
            "Move around to find the object with the strategy: used to find objects including block items and entities. This action is finished once the object is visible (maybe at the distance).\n" +
            "Augments:\n" +
            "- object: a string, the object to explore.\n" +
            "- strategy: a string, the strategy for exploration.\n" +
            "approach(object)\n" +
            "Move close to a visible object: used to approach the object you want to attack or mine. It may fail if the target object is not accessible.\n" +
            "Augments:\n" +
            "- object: a string, the object to approach.\n" +
            "craft(object, materials, tool)\n" +
            "Craft the object with the materials and tool: used for crafting new object that is not in the inventory or is not enough. The required materials must be in the inventory and will be consumed, and the newly crafted objects will be added to the inventory. The tools like the crafting table and furnace should be in the inventory and this action will directly use them. Don’t try to place or approach the crafting table or furnace, you will get failed since this action does not support using tools placed on the ground. You don’t need to collect the items after crafting. If the quantity you require is more than a unit, this action will craft the objects one unit by one unit. If the materials run out halfway through, this action will stop, and you will only get part of the objects you want that have been crafted.\n" +
            "Augments:\n" +
            "- object: a dict, whose key is the name of the object and value is the object quantity.\n" +
            "- materials: a dict, whose keys are the names of the materials and values are the quantities.\n" +
            "- tool: a string, the tool used for crafting. Set to null if no tool is required.\n" +
            "mine(object, tool)\n" +
            "Mine the object with the tool: can only mine the object within reach, cannot mine object from a distance. If there are enough objects within reach, this action will mine as many as you specify. The obtained objects will be added to the inventory.\n" +
            "Augments:\n" +
            "- object: a string, the object to mine.\n" +
            "- tool: a string, the tool used for mining. Set to null if no tool is required.\n" +
            "attack(object, tool)\n" +
            "Attack the object with the tool: used to attack the object within reach. This action will keep track of and attack the object until it is killed.\n" +
            "Augments:\n" +
            "- object: a string, the object to attack.\n" +
            "- tool: a string, the tool used for mining. Set to null if no tool is required.\n" +
            "equip(object)\n" +
            "Equip the object from the inventory: used to equip equipment, including tools, weapons, and armor. The object must be in the inventory and belong to the items for equipping.\n" +
            "Augments:\n" +
            "- object: a string, the object to equip.\n" +
            "digdown(object, tool)\n" +
            "Dig down to the y-level with the tool: the only action you can take if you want to go underground for mining some ore.\n" +
            "Augments:\n" +
            "- object: an int, the y-level (absolute y coordinate) to dig to.\n" +
            "- tool: a string, the tool used for digging. Set to null if no tool is required.\n" +
            "go_back_to_ground(tool)\n" +
            "Go back to the ground from underground: the only action you can take for going back to the ground if you are underground.\n" +
            "Augments:\n" +
            "- tool: a string, the tool used for digging. Set to null if no tool is required.\n" +
            "apply(object, tool)\n" +
            "Apply the tool on the object: used for fetching water, milk, lava with the tool bucket, pooling water or lava to the object with the tool water bucket or lava bucket, shearing sheep with the tool shears, blocking attacks with the tool shield.\n" +
            "Augments:\n" +
            "- object: a string, the object to apply to.\n" +
            "- tool: a string, the tool used to apply.\n" +
            "2. You cannot define any new function. Note that the \"Generated structures\" world creation option is turned off.\n" +
            "3. There is an inventory that stores all the objects I have. It is not an entity, but objects can be added to it or retrieved from it anytime at anywhere without specific actions. The mined or crafted objects will be added to this inventory, and the materials and tools to use are also from this inventory. Objects in the inventory can be directly used. Don’t write the code to obtain them. If you plan to use some object not in the inventory, you should first plan to obtain it. You can view the inventory as one of my states, and it is written in form of a dictionary whose keys are the name of the objects I have and the values are their quantities.\n" +
            "4. You will get the following information about my current state:\n" +
            "- inventory: a dict representing the inventory mentioned above, whose keys are the name of the objects and the values are their quantities\n" +
            "- environment: a string including my surrounding biome, the y-level of my current location, and whether I am on the ground or underground\n" +
            "Pay attention to this information. Choose the easiest way to achieve the goal conditioned on my current state. Do not provide options, always make the final decision.\n" +
            "5. You must describe your thoughts on the plan in natural language at the beginning. After that, you should write all the actions together. The response should follow the format:\n" +
            "{\n" +
            "\"explanation\": \"explain why the last action failed, set to null for the first planning\",\n" +
            "\"thoughts\": \"Your thoughts on the plan in natural languag\",\n" +
            "\"action_list\": [\n" +
            "{\"name\": \"action name\", \"args\": {\"arg name\": value}, \"expectation\": \"describe the expected results of this action\"},\n" +
            "{\"name\": \"action name\", \"args\": {\"arg name\": value}, \"expectation\": \"describe the expected results of this action\"},\n" +
            "{\"name\": \"action name\", \"args\": {\"arg name\": value}, \"expectation\": \"describe the expected results of this action\"}\n" +
            "]\n" +
            "}\n" +
            "The action_list can contain arbitrary number of actions. The args of each action should correspond to the type mentioned in the Arguments part. Remember to add “‘dict“‘ at the beginning and the end of the dict. Ensure that you response can be parsed by Python json.loads\n" +
            "6. I will execute your code step by step and give you feedback. If some action fails, I will stop at that action and will not execute its following actions. The feedback will include error messages about the failed action. At that time, you should replan and write the new code just starting from that failed action.";

    public static final String SOP_USER = "USER:\n" +
            "My current state:\n" +
            "- inventory: {inventory}\n" +
            "- environment: {environment}\n" +
            "The goal is to {goal}.\n" +
            "Here is one plan to achieve similar goal for reference: {reference plan}.\n" +
            "Begin your plan. Remember to follow the response format.\n" +
            "or Action {successful action} succeeded, and {feedback message}. Continue your\n" +
            "plan. Do not repeat successful action. Remember to follow the response format.\n" +
            "or Action {failed action} failed, because {feedback message}. Revise your plan from\n" +
            "the failed action. Remember to follow the response format.";
}
