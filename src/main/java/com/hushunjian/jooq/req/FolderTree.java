package com.hushunjian.jooq.req;

import lombok.Data;

import java.util.List;

@Data
public class FolderTree {

    private String id;

    private String folderName;

    private String parentId;

    private List<FolderTree> childs;

    private String outLine;

}
