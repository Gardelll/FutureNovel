package net.wlgzs.futurenovel.typehandler;

import java.nio.ByteBuffer;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(UUID.class)
public class UUIDTypeHandler extends BaseTypeHandler<UUID> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType) throws SQLException {
        //if (jdbcType != null && jdbcType != JdbcType.BINARY) throw new SQLException("Database type is not compatible");
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(parameter.getMostSignificantBits());
        bb.putLong(parameter.getLeastSignificantBits());
        ps.setBytes(i, bb.array());
    }

    @Override
    public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
        ByteBuffer bb = ByteBuffer.wrap(rs.getBytes(columnName));
        long uuidMost = bb.getLong();
        long uuidLeast = bb.getLong();
        return new UUID(uuidMost, uuidLeast);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        ByteBuffer bb = ByteBuffer.wrap(rs.getBytes(columnIndex));
        long uuidMost = bb.getLong();
        long uuidLeast = bb.getLong();
        return new UUID(uuidMost, uuidLeast);
    }

    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        ByteBuffer bb = ByteBuffer.wrap(cs.getBytes(columnIndex));
        long uuidMost = bb.getLong();
        long uuidLeast = bb.getLong();
        return new UUID(uuidMost, uuidLeast);
    }
}
