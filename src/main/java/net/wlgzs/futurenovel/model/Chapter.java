/*
 *  Copyright (C) 2020 Future Studio
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.wlgzs.futurenovel.model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.apache.ibatis.annotations.AutomapConstructor;

/**
 * 小说的章节
 */
@Data
@AllArgsConstructor(onConstructor_={@AutomapConstructor})
public class Chapter implements Serializable, NovelNode {

    private static final long serialVersionUID = 2888176444107291305L;

    /**
     * 章节的唯一 ID
     */
    @NonNull
    private UUID uniqueId;

    /**
     * 所属的小说的 ID
     */
    @NonNull
    private UUID fromNovel;

    /**
     * 章标题
     */
    @NonNull
    @Getter(onMethod_ = {@Override})
    private String title;

    /**
     * 小节索引
     */
    @NonNull
    private ArrayNode sections;

    @Override
    public UUID getParentUUID() {
        return fromNovel;
    }

    @Override
    public UUID getThisUUID() {
        return uniqueId;
    }

}
