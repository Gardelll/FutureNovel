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

package net.wlgzs.futurenovel;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("future")
public class AppProperties {
    /**
     * 应用目录
     */
    private String appHome = System.getProperty("user.home", System.getenv("HOME")) + "/future-novel";
    /**
     * 文件上传目录
     */
    private String uploadDir = appHome + "/uploads";
    /**
     * token 过期配置
     */
    private Token token = new Token();
    @Data
    public static class Token {
        private long savePeriod = 10; // 存数据库周期（分钟）
        private long expire = 7; // 过期时间（天）
        private long cookieExpire = 30; // Cookie 过期时间（天）
    }
}
