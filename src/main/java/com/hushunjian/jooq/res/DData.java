package com.hushunjian.jooq.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DData {

    private List<String> column_list;

    private List<List<String>> rows;
}
