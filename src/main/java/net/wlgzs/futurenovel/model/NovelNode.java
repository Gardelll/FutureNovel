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

import java.util.UUID;

public interface NovelNode {

    /**
     * @return 父节点 ID
     */
    UUID getParentUUID();

    /**
     * @return 此节点的 ID
     */
    UUID getThisUUID();

    /**
     * @return 标题
     */
    String getTitle();

}
