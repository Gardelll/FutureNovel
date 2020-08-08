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

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.apache.ibatis.annotations.AutomapConstructor;

/**
 * 小说的小节
 */
@Data
@AllArgsConstructor(onConstructor_={@AutomapConstructor})
public class Section implements Serializable, NovelNode {

    private static final long serialVersionUID = -1682410254970643692L;

    /**
     * 小节的唯一 ID
     */
    @NonNull
    private UUID uniqueId;

    /**
     * 所属章节的 ID
     */
    @NonNull
    private UUID fromChapter;

    /**
     * 小节标题
     */
    @NonNull
    @Getter(onMethod_ = {@Override})
    private String title;

    /**
     * 文本
     */
    @NonNull
    private String text;

    @Override
    public UUID getParentUUID() {
        return fromChapter;
    }

    @Override
    public UUID getThisUUID() {
        return uniqueId;
    }

}
