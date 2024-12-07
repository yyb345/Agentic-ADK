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
package com.alibaba.langengine.demo.chain.support;

import com.alibaba.langengine.core.chain.tot.ThoughtValidity;
import com.alibaba.langengine.core.chain.tot.ToTChecker;
import lombok.Data;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class MyChecker extends ToTChecker {

    private String sudokuSolution;

    public MyChecker(String sudokuSolution) {
        setSudokuSolution(sudokuSolution);
    }
    @Override
    public ThoughtValidity evaluate(String problemDescription, List<String> thoughts) {
        String lastThought = thoughts.get(thoughts.size() - 1);
        String cleanSolution = lastThought.replace(" ", "").replace("\"", "");
        String regexSolution = cleanSolution.replace("*", ".").replace("|", "\\|");

        if (sudokuSolution.contains(cleanSolution)) {
            return ThoughtValidity.VALID_FINAL;
        } else {
            Pattern pattern = Pattern.compile(regexSolution);
            Matcher matcher = pattern.matcher(sudokuSolution);
            if (matcher.find()) {
                return ThoughtValidity.VALID_INTERMEDIATE;
            } else {
                return ThoughtValidity.INVALID;
            }
        }
    }

    public static void main(String[] args) {
        String regexSolution = "Step1：3,4,.,2\\|1,.,3,.\\|.,1,.,3\\|4,.,.,1";
//        String regexSolution = "Step1:\n" +
//                "\n" +
//                "3,.,.,2\\|1,.,3,.\\|.,1,.,3\\|4,.,.,1\n" +
//                "\n" +
//                "Thispuzzlehasnoknowndigits,solet'sstartbyfillinginoneofthe.cells.\n" +
//                "\n" +
//                "3,.,.,2\\|1,.,3,.\\|.,1,.,3\\|4,.,.,1\n" +
//                "\n" +
//                "Let'sfillinthefirst.cellwith4.\n" +
//                "\n" +
//                "3,4,.,2\\|1,.,3,.\\|.,1,.,3\\|4,.,.,1\n" +
//                "\n" +
//                "Now,weneedtomakesuretherearenoduplicatedigitsinthesamerow,column,or2x2subgrid.Let'scheckthefirstrow:\n" +
//                "\n" +
//                "3,4,.,2\n" +
//                "\n" +
//                "Therearenoduplicateshere.Nowlet'scheckthefirstcolumn:\n" +
//                "\n" +
//                "3\n" +
//                "4\n" +
//                ".\n" +
//                "2\n" +
//                "\n" +
//                "Again,noduplicates.Finally,let'scheckthe2x2subgrid:\n" +
//                "\n" +
//                "3,4\n" +
//                ".\n" +
//                "2\n" +
//                "\n" +
//                "Noduplicateshereeither.\n" +
//                "\n" +
//                "Thisthoughtisavalidpartialsolutiontothepuzzle.Let'smoveontothenextstep.";
        String sudokuSolution = "3,4,1,2|1,2,3,4|2,1,4,3|4,3,2,1";
        Pattern pattern = Pattern.compile(regexSolution, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sudokuSolution);
        if (matcher.find()) {
            System.out.println(ThoughtValidity.VALID_INTERMEDIATE);
        } else {
            System.out.println(ThoughtValidity.INVALID);
        }
    }
}
