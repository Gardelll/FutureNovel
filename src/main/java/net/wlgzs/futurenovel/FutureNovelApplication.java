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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.yaml.snakeyaml.Yaml;

public class FutureNovelApplication {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        Path propertiesPath = Paths.get(new AppProperties().getAppHome(), "application.yml");
        if (!Files.exists(propertiesPath)) {
            try (InputStream inputStream = FutureNovelApplication.class.getResourceAsStream("/application.yml"); var out = new FileOutputStream(propertiesPath.toFile())) {
                inputStream.transferTo(out);
                System.err.println("配置文件已保存至" + propertiesPath.toAbsolutePath().toString());
                System.err.println("请修改后执行");
                System.exit(2);
                return;
            } catch (IOException e) {
                System.err.println("无法读取文件");
                e.printStackTrace();
            }
        }
        try (var input = Files.newInputStream(propertiesPath)) {
            var yaml = new Yaml();
            Map<String, Object> configurationMap = yaml.loadAs(input, Map.class);
            new SpringApplicationBuilder().properties(configurationMap)
                .sources(AppConfig.class)
                .run(args);
        } catch (IOException e) {
            System.err.println("无法读取文件");
            e.printStackTrace();
        }
    }

}
