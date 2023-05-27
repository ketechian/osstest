package com.example.osstest.service.Impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.event.ProgressEvent;
import com.aliyun.oss.event.ProgressEventType;
import com.aliyun.oss.event.ProgressListener;
import com.aliyun.oss.model.*;
import com.example.osstest.service.OssFile;
import com.example.osstest.utils.ConstantPropertiesUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * @Description
 * @Author: HZY
 * @CreateTime: 2022/6/1 14:07
 */

@Service
public class OssFileImpl implements OssFile, ProgressListener {

    //读取配置文件的内容
    @Value("${aliyun.oss.file.endpoint}")
    private String endpoint;
    @Value("${aliyun.oss.file.keyid}")
    private String accessKeyId;
    @Value("${aliyun.oss.file.keysecret}")
    private String accessKeySecret;
    @Value("${aliyun.oss.file.bucketname}")
    private String bucketName;


    private long bytesWritten = 0;
    private long totalBytes = -1;
    private boolean succeed = false;

    /**
     * 上传文件
     *
     * @param filePath
     * @return
     */
    @Override
    public String uploadFile(String filePath) {
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String objectName = "object/test/files/5.mkv";

        // 限速1 MB/s。
        int limitSpeed = 1024 * 1024 * 8;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建PutObjectRequest对象。
            // 限速上传。
            //InputStream inputStream = new FileInputStream(filePath);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, new File(filePath));
            putObjectRequest.setTrafficLimit(limitSpeed);
            // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
            // ObjectMetadata metadata = new ObjectMetadata();
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            // metadata.setObjectAcl(CannedAccessControlList.Private);
            // putObjectRequest.setMetadata(metadata);

            // 指定上传文件操作时是否覆盖同名Object。
            // 不指定x-oss-forbid-overwrite时，默认覆盖同名Object。
            // 指定x-oss-forbid-overwrite为false时，表示允许覆盖同名Object。
            // 指定x-oss-forbid-overwrite为true时，表示禁止覆盖同名Object，如果同名Object已存在，程序将报错。
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setHeader("x-oss-forbid-overwrite", "false");
            putObjectRequest.setMetadata(metadata);

            // 上传文件的同时指定了进度条参数。
            ossClient.putObject(putObjectRequest.<PutObjectRequest>withProgressListener(new OssFileImpl()));

            return objectName;
        } catch (OSSException oe) {
            oe.printStackTrace();
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
            return "error";
        } catch (ClientException ce) {
            ce.printStackTrace();
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
            return "error";
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    public boolean isSucceed() {
        return succeed;
    }

    /**
     * 下载文件
     * @param filePath
     * @return
     */
    @Override
    public String downloadFile(String filePath) {

        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String objectName = "object/test/1.jpg";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 下载Object到本地文件，并保存到指定的本地路径中。如果指定的本地文件存在会覆盖，不存在则新建。
            // 如果未指定本地路径，则下载后的文件默认保存到示例程序所属项目对应本地路径中。
            ossClient.getObject(new GetObjectRequest(bucketName, objectName), new File(filePath));

        } catch (OSSException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        } finally {
            // 关闭OSSClient
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }

    /**
     * 判断文件是否存在
     * @param filePath
     * @return
     */
    @Override
    public String isExist(String filePath) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 判断文件是否存在。如果返回值为true，则文件存在，否则存储空间或者文件不存在。
            // 设置是否进行重定向或者镜像回源。默认值为true，表示忽略302重定向和镜像回源；如果设置isINoss为false，则进行302重定向或者镜像回源。
            //boolean isINoss = true;
            boolean found = ossClient.doesObjectExist(bucketName, filePath);
            //boolean found = ossClient.doesObjectExist(bucketName, objectName, isINoss);
            System.out.println(found);
        } catch (OSSException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        } finally {
            // 关闭OSSClient
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }

    /**
     * 列举文件1
     * @param filePath
     * @return
     */
    @Override
    public String enumerateFiles1(String filePath) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 构造ListObjectsRequest请求。
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName);
            // 设置prefix参数来获取目录下的所有文件。
            listObjectsRequest.setPrefix(filePath);

            // 递归列举fun目录下的所有文件。
            ObjectListing listing = ossClient.listObjects(listObjectsRequest);

            // 遍历所有文件。
            System.out.println("Objects:");
            for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
                System.out.println(objectSummary.getKey());
            }

            // 遍历所有commonPrefix。
            System.out.println("\nCommonPrefixes:");
            for (String commonPrefix : listing.getCommonPrefixes()) {
                System.out.println(commonPrefix);
            }
        } catch (OSSException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }

    /**
     * 列举文件2
     * @param filePath
     * @return
     */
    @Override
    public String enumerateFiles2(String filePath) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 构造ListObjectsRequest请求。
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName);

            // 设置正斜线（/）为文件夹的分隔符。
            listObjectsRequest.setDelimiter("/");

            // 列出fun目录下的所有文件和文件夹。
            listObjectsRequest.setPrefix(filePath);

            ObjectListing listing = ossClient.listObjects(listObjectsRequest);

            // 遍历所有文件。
            System.out.println("Objects:");
            // objectSummaries的列表中给出的是fun目录下的文件。
            for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
                System.out.println(objectSummary.getKey());
            }

            // 遍历所有commonPrefix。
            System.out.println("\nCommonPrefixes:");
            // commonPrefixs列表中显示的是指定   目录下的所有子文件夹。
            for (String commonPrefix : listing.getCommonPrefixes()) {
                System.out.println(commonPrefix);
            }
        } catch (OSSException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }

    /**
     * 判断文件大小
     * @param filePath
     * @return
     */
    @Override
    public String filesSize(String filePath) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            ListObjectsV2Result result = null;
            do {
                // 默认情况下，每次列举100个文件或目录。
                ListObjectsV2Request request = new ListObjectsV2Request(bucketName).withDelimiter("/").withPrefix(filePath);
                if (result != null) {
                    request.setContinuationToken(result.getNextContinuationToken());
                }
                result = ossClient.listObjectsV2(request);
                List<String> folders = result.getCommonPrefixes();
                for (String folder : folders) {
                    System.out.println(folder + " : " + (calculateFolderLength(ossClient, bucketName, folder) / 1024) + "KB");
                }
                List<OSSObjectSummary> sums = result.getObjectSummaries();
                for (OSSObjectSummary s : sums) {
                    System.out.println(s.getKey() + " : " + (s.getSize() / 1024) + "KB");
                }
            } while (result.isTruncated());
        } catch (OSSException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }

    @Override
    public String copySmallFile(String filePath) {
        String endpoint = ConstantPropertiesUtils.END_POINT;
        String accessKeyId = ConstantPropertiesUtils.ACCESS_KEY_ID;
        String accessKeySecret = ConstantPropertiesUtils.ACCESS_KEY_SECRET;
        // 填写源Bucket名称。
        String sourceBucketName = ConstantPropertiesUtils.BUCKET_NAME;
        // 填写源Object的完整路径，完整路径中不能包含Bucket名称。
        String sourceKey = "object/test/files/3.jpg";
        // 填写与源Bucket处于同一地域的目标Bucket名称。
        String destinationBucketName = ConstantPropertiesUtils.BUCKET_NAME;
        // 填写目标Object的完整路径。Object完整路径中不能包含Bucket名称。
        String destinationKey = filePath;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        // 拷贝文件。
        try {
            CopyObjectResult result = ossClient.copyObject(sourceBucketName, sourceKey, destinationBucketName, destinationKey);
            System.out.println("ETag: " + result.getETag() + " LastModified: " + result.getLastModified());
        } catch (OSSException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }

    // 获取某个存储空间下指定目录（文件夹）下的文件大小。
    private static long calculateFolderLength(OSS ossClient, String bucketName, String folder) {
        long size = 0L;
        ListObjectsV2Result result = null;
        do {
            // MaxKey默认值为100，最大值为1000。
            ListObjectsV2Request request = new ListObjectsV2Request(bucketName).withPrefix(folder).withMaxKeys(1000);
            if (result != null) {
                request.setContinuationToken(result.getNextContinuationToken());
            }
            result = ossClient.listObjectsV2(request);
            List<OSSObjectSummary> sums = result.getObjectSummaries();
            for (OSSObjectSummary s : sums) {
                size += s.getSize();
            }
        } while (result.isTruncated());
        return size;
    }

    //进度条
    @Override
    public void progressChanged(ProgressEvent progressEvent) {
        long bytes = progressEvent.getBytes();
        ProgressEventType eventType = progressEvent.getEventType();
        switch (eventType) {
            case TRANSFER_STARTED_EVENT:
                System.out.println("Start to upload......");
                break;
            case REQUEST_CONTENT_LENGTH_EVENT:
                this.totalBytes = bytes;
                System.out.println(this.totalBytes + " bytes in total will be uploaded to OSS");
                break;
            case REQUEST_BYTE_TRANSFER_EVENT:
                this.bytesWritten += bytes;
                if (this.totalBytes != -1) {
                    int percent = (int)(this.bytesWritten * 100.0 / this.totalBytes);
                    System.out.println(bytes + " bytes have been written at this time, upload progress: " + percent +
                            "%(" + this.bytesWritten + "/" + this.totalBytes + ")");
                } else {
                    System.out.println(bytes + " bytes have been written at this time, upload ratio: unknown" +
                            "(" + this.bytesWritten + "/...)");
                }
                break;
            case TRANSFER_COMPLETED_EVENT:
                this.succeed = true;
                System.out.println("Succeed to upload, " + this.bytesWritten + " bytes have been transferred in total");
                break;
            case TRANSFER_FAILED_EVENT:
                System.out.println("Failed to upload, " + this.bytesWritten + " bytes have been transferred");
                break;
            default:
                break;
        }
    }
}
