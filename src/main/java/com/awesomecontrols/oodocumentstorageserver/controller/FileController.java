package com.awesomecontrols.oodocumentstorageserver.controller;

import com.awesomecontrols.oodocumentstorageserver.service.IFileSytemStorage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class FileController {

    private static final Logger LOGGER = Logger.getLogger(FileController.class.getName());

    static {
        if (LOGGER.getLevel() == null) {
            LOGGER.setLevel(Level.FINEST);
        }
    }

    @Autowired
    IFileSytemStorage fileSytemStorage;

//    @PostMapping("/uploadfile")
//    public ResponseEntity<FileResponse> uploadSingleFile(@RequestParam("file") MultipartFile file) {
//
//        String upfile = fileSytemStorage.saveFile(file);
//
//        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
//                .path("/api/download/")
//                .path(upfile)
//                .toUriString();
//
//        return ResponseEntity.status(HttpStatus.OK).body(new FileResponse(upfile, fileDownloadUri, "File uploaded with success!"));
//    }
//
//    @PostMapping("/uploadfiles")
//    public ResponseEntity<List<FileResponse>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
//
//        List<FileResponse> responses = Arrays.asList(files)
//                .stream()
//                .map(
//                        file -> {
//                            String upfile = fileSytemStorage.saveFile(file);
//                            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
//                                    .path("/api/download/")
//                                    .path(upfile)
//                                    .toUriString();
//                            return new FileResponse(upfile, fileDownloadUri, "File uploaded with success!");
//                        }
//                )
//                .collect(Collectors.toList());
//
//        return ResponseEntity.status(HttpStatus.OK).body(responses);
//    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        LOGGER.log(Level.FINEST, "filename: "+filename);
        
        Resource resource = fileSytemStorage.loadFile(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/getfilekey")
    public ResponseEntity<String> getFileKey(@RequestBody String filename) {
        // RequestBody mapea el contenido del body a la variable que se referencia.
        LOGGER.log(Level.FINEST, "entrando...");
        LOGGER.log(Level.FINEST,"filename: "+filename );
        
        // verificar que comience con la / inicial
        filename = filename.startsWith("/")?filename:"/"+filename;
        return ResponseEntity.ok().body(fileSytemStorage.getFileKey(filename));
    }
    
    
    
    @PostMapping("/callback")
    public void callback(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.log(Level.INFO, "Callback!!!");
        
        PrintWriter writer = response.getWriter();

        Scanner scanner = new Scanner(request.getInputStream()).useDelimiter("\\A");
        String body = scanner.hasNext() ? scanner.next() : "";
        
        LOGGER.log(Level.INFO, body);
        
        JSONObject jsonObj = (JSONObject) new JSONObject(body);

        if ( jsonObj.getInt("status") == 2 || jsonObj.getInt("status") == 6) {
            String downloadUri = jsonObj.getString("url");
            
            URL url = new URL(downloadUri);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            InputStream stream = connection.getInputStream();
            
            String fkey = jsonObj.getString("key");
            LOGGER.log(Level.FINEST, "fkey: "+fkey);
            String fileToSave = fileSytemStorage.getFilenameFromKey(fkey);
            LOGGER.log(Level.FINEST, "fileToSave: "+fileToSave);
            File savedFile = new File(fileToSave);
            try (FileOutputStream out = new FileOutputStream(savedFile)) {
                int read;
                final byte[] bytes = new byte[1024];
                while ((read = stream.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }

                out.flush();
            }

            connection.disconnect();
        }
        writer.write("{\"error\":0}");

    }
}
