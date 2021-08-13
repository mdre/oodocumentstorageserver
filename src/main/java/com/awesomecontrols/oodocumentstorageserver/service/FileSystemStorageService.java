package com.awesomecontrols.oodocumentstorageserver.service;

import com.awesomecontrols.oodocumentstorageserver.exception.FileNotFoundException;
import com.awesomecontrols.oodocumentstorageserver.exception.FileStorageException;
import com.awesomecontrols.oodocumentstorageserver.properties.FileUploadProperties;
import com.awesomecontrols.oodocumentstorageserver.properties.Security;
import com.awesomecontrols.oodocumentstorageserver.util.AES;
import com.awesomecontrols.oodocumentstorageserver.util.StringUtil;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements IFileSytemStorage {

    private final static Logger LOGGER = Logger.getLogger(FileSystemStorageService.class.getName());
    static {
        if (LOGGER.getLevel() == null) {
            LOGGER.setLevel(Level.FINEST);
        }
    }

    private final Path dirLocation;
    private final String key;

    @Autowired
    public FileSystemStorageService(FileUploadProperties fileUploadProperties, Security sec) {
        this.dirLocation = Paths.get(fileUploadProperties.getLocation())
                .toAbsolutePath()
                .normalize();
        this.key = sec.getKey();
    }

    @Override
    @PostConstruct
    public void init() {
        // TODO Auto-generated method stub
        try {
            Files.createDirectories(this.dirLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create upload dir!");
        }

    }

    @Override
    public String saveFile(MultipartFile file) {
        // TODO Auto-generated method stub

        try {

            String fileName = file.getOriginalFilename();
            Path dfile = this.dirLocation.resolve(fileName);
            Files.copy(file.getInputStream(), dfile, StandardCopyOption.REPLACE_EXISTING);

            return fileName;

        } catch (Exception e) {
            throw new FileStorageException("Could not upload file");
        }

    }

    @Override
    public Resource loadFile(String fileName) {
        // decodificar el nombre 
        
        String localFilename = getFilenameFromKey(fileName);
        try {
            Path file = this.dirLocation.resolve(localFilename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Could not find file");
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Could not download file");
        }

    }

    @Override
    public String getFileKey(String fileName) throws FileNotFoundException {
        // verificar si el archivo existe
        LOGGER.log(Level.FINEST, "fileName: "+fileName);
        LOGGER.log(Level.FINEST, "fullpath: "+dirLocation + fileName);
        String fkey = "";
        if (Files.exists(Paths.get(dirLocation + fileName))) {
            try {
                String fAes = AES.encrypt(StringUtil.getRandomString(5)+dirLocation + fileName, key);
                LOGGER.log(Level.FINEST, "fAes: "+fAes);
                fkey = Base64.getEncoder().encodeToString(fAes.getBytes());
                LOGGER.log(Level.FINEST, "B64 key:" + fkey);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException ex) {
                Logger.getLogger(FileSystemStorageService.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            throw new FileNotFoundException(fileName);
        }
        return fkey;
    }

    @Override
    public String getFilenameFromKey(String fkey) {
        LOGGER.log(Level.FINEST, "fkey: "+fkey);
        String b64 = new String(Base64.getDecoder().decode(fkey.getBytes()));
        LOGGER.log(Level.FINEST, "B64 decode: "+b64);
        String f = "";
        try {
            f = AES.decrypt(b64, key).substring(5);
            LOGGER.log(Level.FINEST, "file decoded from key: "+f);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(FileSystemStorageService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return f;
    }

 
}
