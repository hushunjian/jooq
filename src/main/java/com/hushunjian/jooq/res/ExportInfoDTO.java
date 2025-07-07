package com.hushunjian.jooq.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportInfoDTO {

    private Integer batchNo;

    private Integer total;

    private String fileName;

    private String fileType;
}
