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

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.AutomapConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor(onConstructor_={@AutomapConstructor})
public class ReadHistory implements Serializable {

    private static final long serialVersionUID = 0xa3252f0c7328ddbeL;

    @NonNull
    private final UUID uniqueId;

    @NonNull
    private final UUID accountId;

    @NonNull
    private final UUID sectionId;

    @NonNull
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy年MM月dd日 HH:mm:ss")
    private final Date createTime;

    private String title;

    private UUID novelIndexId;

    private String novelTitle;

    private String chapterTitle;

    private String coverImgUrl;

    private String authors;

}
