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

import com.alibaba.langengine.metagpt.roles.RoleContext;
import com.alibaba.langengine.metagpt.utils.FileUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class Cache {

    private RoleContext rc;
    private boolean useCache = true;

    public Cache(RoleContext rc, boolean useCache) {
        this.rc = rc;
        this.useCache = useCache;
    }

    public Boolean writeCache(String key, String filed, String content) {
        if (!useCache) {
            return true;
        }
        String path = this.rc.getEnv().getWorkspace() + "_cache_/" + this.rc.getEnv().getProjectCode() + "/" + key;
        String cachePath = FileUtils.writeFile(path, filed, content);
        if (StringUtils.isEmpty(cachePath)) {
            return false;
        } else {
            return true;
        }
    }

    public String readeCache(String key, String filed) {
        if (!useCache) {
            return "";
        }
        String path = this.rc.getEnv().getWorkspace() + "_cache_/" + this.rc.getEnv().getProjectCode() + "/" + key;
        return FileUtils.readFile(path, filed);
    }

}
