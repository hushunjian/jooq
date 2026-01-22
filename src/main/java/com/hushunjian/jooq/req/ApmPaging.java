package com.hushunjian.jooq.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApmPaging {

    private Boolean needTotal;

    private int pageNum;

    private int pageSize;
}
