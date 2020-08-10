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

package net.wlgzs.futurenovel.typehandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@MappedTypes(JsonNode.class)
@Component
public class JsonNodeTypeHandler extends BaseTypeHandler<JsonNode> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JsonNode parameter, JdbcType jdbcType) throws SQLException {
        try {
            StringWriter stringWriter = new StringWriter();
            var generator = objectMapper.createGenerator(stringWriter);
            objectMapper.writeTree(generator, parameter);
            ps.setString(i, stringWriter.toString());
        } catch (IOException e) {
            throw new SQLException("Failed to serialize json", e);
        }
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        try {
            StringReader stringReader = new StringReader(rs.getString(columnName));
            return objectMapper.readTree(stringReader);
        } catch (IOException | ClassCastException e) {
            throw new SQLException("Failed to deserialize json", e);
        }
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            StringReader stringReader = new StringReader(rs.getString(columnIndex));
            return objectMapper.readTree(stringReader);
        } catch (IOException | ClassCastException e) {
            throw new SQLException("Failed to deserialize json", e);
        }
    }

    @Override
    public JsonNode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        try {
            StringReader stringReader = new StringReader(cs.getString(columnIndex));
            return objectMapper.readTree(stringReader);
        } catch (IOException | ClassCastException e) {
            throw new SQLException("Failed to deserialize json", e);
        }
    }

}
