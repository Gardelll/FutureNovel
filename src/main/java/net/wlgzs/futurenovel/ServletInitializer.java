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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.yaml.snakeyaml.Yaml;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	@SuppressWarnings("unchecked")
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		try (var stream = Files.newInputStream(Paths.get(new AppProperties().getAppHome(), "application.yml"))) {
			var yaml = new Yaml();
			Map<String, Object> configurationMap = yaml.loadAs(stream, Map.class);
			return application.properties(configurationMap).sources(AppConfig.class);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
