package com.hushunjian.jooq.mapping;

import com.hushunjian.jooq.generator.tables.records.UserRecord;
import com.hushunjian.jooq.res.JsonInfo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapping {

    UserMapping USER_MAPPING = Mappers.getMapper(UserMapping.class);

    JsonInfo toUser(UserRecord user);
}
