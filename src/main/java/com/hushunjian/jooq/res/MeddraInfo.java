package com.hushunjian.jooq.res;

import lombok.Data;

import java.util.List;

@Data
public class MeddraInfo {

    private String name;

    private List<MeddraChildren> childs;
}
