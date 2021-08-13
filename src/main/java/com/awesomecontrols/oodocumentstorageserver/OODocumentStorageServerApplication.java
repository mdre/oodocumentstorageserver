package com.awesomecontrols.oodocumentstorageserver;

import com.awesomecontrols.oodocumentstorageserver.properties.FileUploadProperties;
import com.awesomecontrols.oodocumentstorageserver.properties.Security;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    FileUploadProperties.class,
    Security.class
})
public class OODocumentStorageServerApplication {

    //=====================================================
    // https://devwithus.com/download-upload-files-with-spring-boot/
    //=====================================================        
    public static void main(String[] args) {
        SpringApplication.run(OODocumentStorageServerApplication.class, args);
    }

}
