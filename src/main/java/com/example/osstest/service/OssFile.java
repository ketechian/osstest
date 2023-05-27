package com.example.osstest.service;

import java.io.File;

/**
 * @Description
 * @Author: HZY
 * @CreateTime: 2022/6/1 14:04
 */
public interface OssFile {

    String uploadFile(String filePath);

    String downloadFile(String filePath);

    String isExist(String filePath);

    String enumerateFiles1(String filePath);

    String enumerateFiles2(String filePath);

    String filesSize(String filePath);

    String copySmallFile(String filePath);

}
