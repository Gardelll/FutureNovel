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

import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.format.datetime.DateFormatter;

@SpringBootApplication
@MapperScan("net.wlgzs.futurenovel.dao")
@EnableConfigurationProperties(AppProperties.class)
@Slf4j
public class AppConfig {

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8:00"));
    }

    @Bean(name = "defaultDateFormatter")
    public DateFormatter dateFormatter() {
        DateFormatter dateFormatter = new DateFormatter("yyyy年MM月dd日 HH:mm:ss");
        dateFormatter.setTimeZone(TimeZone.getDefault());
        return dateFormatter;
    }

}
