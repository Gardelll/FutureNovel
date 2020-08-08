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
