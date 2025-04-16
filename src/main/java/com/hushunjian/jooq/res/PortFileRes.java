package com.hushunjian.jooq.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortFileRes {

    private String portId;

    private String fileName;

    private String dateTime;
}
