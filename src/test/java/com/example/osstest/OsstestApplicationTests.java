package com.example.osstest;

import com.example.osstest.service.OssFile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OsstestApplicationTests {
    @Autowired
    private OssFile ossFile;

    @Test
    void contextLoads() {
        String filePath = "D:\\BaiduNetdiskDownload\\JOJO的奇妙冒险 石之海\\JoJos.Bizarre.Adventure.S05E01.Stone.Ocean.1080p.NF.WEB-DL.AAC2.0.H.264-aKraa.mkv";
        ossFile.uploadFile(filePath);
    }

    @Test
    void downloadFile() {
        //本地文件保存路径
        String localPath = "C:\\Users\\zheyuan he\\Desktop\\gxs.jpg";
        ossFile.downloadFile(localPath);
    }

    @Test
    void isExist() {
        String filePath = "object/test/2.jpg";
        ossFile.isExist(filePath);
    }

    @Test
    void enumerateFiles1() {
        String filePath = "object/test";
        ossFile.enumerateFiles1(filePath);
    }

    @Test
    void enumerateFiles2() {
        //最后一定要加"/"
        String filePath = "object/test/";
        ossFile.enumerateFiles2(filePath);
    }

    @Test
    void filesSize() {
        String filePath = "object/test/";
        ossFile.filesSize(filePath);
    }

    @Test
    void copySmallFile() {
        String filePath = "object/test/3.jpg";
        ossFile.copySmallFile(filePath);
    }
}
