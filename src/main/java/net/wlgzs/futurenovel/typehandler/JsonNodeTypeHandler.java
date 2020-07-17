package net.wlgzs.futurenovel.typehandler;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.wlgzs.futurenovel.AppConfig;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(JsonNode.class)
public class JsonNodeTypeHandler extends BaseTypeHandler<JsonNode> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JsonNode parameter, JdbcType jdbcType) throws SQLException {
        try {
            StringWriter stringWriter = new StringWriter();
            var generator = AppConfig.objectMapper.createGenerator(stringWriter);
            AppConfig.objectMapper.writeTree(generator, parameter);
            ps.setString(i, stringWriter.toString());
        } catch (IOException e) {
            throw new SQLException("Failed to serialize json", e);
        }
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        try {
            StringReader stringReader = new StringReader(rs.getString(columnName));
            return AppConfig.objectMapper.readTree(stringReader);
        } catch (IOException | ClassCastException e) {
            throw new SQLException("Failed to deserialize json", e);
        }
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            StringReader stringReader = new StringReader(rs.getString(columnIndex));
            return AppConfig.objectMapper.readTree(stringReader);
        } catch (IOException | ClassCastException e) {
            throw new SQLException("Failed to deserialize json", e);
        }
    }

    @Override
    public JsonNode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        try {
            StringReader stringReader = new StringReader(cs.getString(columnIndex));
            return AppConfig.objectMapper.readTree(stringReader);
        } catch (IOException | ClassCastException e) {
            throw new SQLException("Failed to deserialize json", e);
        }
    }

}
