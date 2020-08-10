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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.apache.ibatis.annotations.AutomapConstructor;

@Data
@AllArgsConstructor(onConstructor_={@AutomapConstructor})
public class BookSelf implements Serializable {

    private static final long serialVersionUID = 0x459b1be46995d9caL;

    @NonNull
    private final UUID uniqueId;

    @NonNull
    private final UUID accountId;

    @NonNull
    private String title;

    @NonNull
    private ArrayNode novels;

    public void addNovel(@NonNull ObjectNode novelIndex) {
        novels.add(novelIndex);
    }

    public void clear() {
        novels.removeAll();
    }

    public void remove(@NonNull UUID novelIndexId) {
        for (int i = 0; i < novels.size(); i++) {
            int finalI = i;
            Optional.ofNullable(novels.get(i).get("uniqueId"))
                .map(JsonNode::asText)
                .ifPresent(s -> {
                    if (s.equals(novelIndexId.toString()))
                        novels.remove(finalI);
                });
        }
    }

}
