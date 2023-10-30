package com.demo_rentas.Controllers;

import com.demo_rentas.Models.DocService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class DocumentosController {

    @Value("${uri.service2}")
    private String uriService2;

    @Value("${uploadDirTxtD}")
    private String uploadDirTxtD;

    @Value("${uploadDirZipD}")
    private String uploadDirZipD;

    @Value("${downloadLogs}")
    private String downloadLogs;

    @Value("${downloadLogsSystem}")
    private String downloadSystemLogs;

    @GetMapping("documentos")
    public String documentos() {
        return "SolicitudDocumentos/Documentos";
    }

    @GetMapping("probarServicioDocumentos")
    public String obtenerRespuestaDoc(Model model) throws IOException {
        DocService docService = new DocService();

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(uriService2);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequest = objectMapper.writeValueAsString(docService);

        StringEntity entity = new StringEntity(jsonRequest);
        entity.setContentType("application/json");
        httpPost.setEntity(entity);

        HttpResponse responseHttp = httpClient.execute(httpPost);
        HttpEntity responseEntity2 = responseHttp.getEntity();
        String jsonResponse = EntityUtils.toString(responseEntity2);

        ObjectMapper objectMapper2 = new ObjectMapper();
        DocService[] responseArray = objectMapper2.readValue(jsonResponse, DocService[].class);
        List<DocService> response = Arrays.asList(responseArray);
        model.addAttribute("docsService", response);
        return "Respuesta/RespuestaServicioDocumentos";
    }

    @RequestMapping("/uploadTXTDocumentos")
    public String uploadFileTxtDoc(@RequestParam("fileTxt") MultipartFile file) {
        if (!file.isEmpty()) {
            if (file.getOriginalFilename().endsWith(".txt")) {
                try {
                    File uploadDir = new File(uploadDirTxtD);
                    if (!uploadDir.exists()) {
                        uploadDir.mkdirs();
                    }
                    File uploadedFile = new File(uploadDirTxtD + File.separator + file.getOriginalFilename());
                    file.transferTo(uploadedFile);

                    return "/SuccessResponse";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "/ErrorResponse";
                }
            } else {
                return "redirect:/ErrorResponse?message=el archivo debe ser .txt";
            }
        }
        return "/ErrorResponse";
    }

    @RequestMapping("/uploadZIPDocumentos")
    public String uploadFileZipDoc(@RequestParam("fileZip") MultipartFile file) {
        if (!file.isEmpty()) {
            if (file.getOriginalFilename().endsWith(".zip")) {
                try {

                    File uploadDir = new File(uploadDirZipD);
                    if (!uploadDir.exists()) {
                        uploadDir.mkdirs();
                    }
                    File uploadedFile = new File(uploadDirZipD + File.separator + file.getOriginalFilename());
                    file.transferTo(uploadedFile);

                    return "/SuccessResponse";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "/ErrorResponse";
                }
            } else {
                return "redirect:/ErrorResponse?message=el archivo debe ser .zip";
            }
        }
        return "/ErrorResponse";
    }

    @GetMapping("/downloadLogsDoc")
    public void downloadFilesDoc(HttpServletResponse response) {

        try {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=archivosLogsRegDoc.zip");
            OutputStream outputStream = response.getOutputStream();
            ZipOutputStream zipOut = new ZipOutputStream(outputStream);

            comprimirDirectorio(new File(downloadLogs), zipOut);
            comprimirDirectorio(new File(downloadSystemLogs), zipOut);

            zipOut.close();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void comprimirDirectorio(File directorio, ZipOutputStream zipOut) throws IOException {
        File[] files = directorio.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    addToZipFileDoc(file, zipOut);
                } else if (file.isDirectory()) {
                    comprimirDirectorio(file, zipOut);
                }
            }
        }
    }

    private static void addToZipFileDoc(File file, ZipOutputStream zipOut) throws IOException {
        FileInputStream fileInput = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fileInput.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fileInput.close();
    }

    @GetMapping("/ErrorResponseDoc")
    public String showErrorPageZipDoc(@RequestParam("message") String errorMessage, Model model) {
        model.addAttribute("errorMessage", errorMessage);
        return "/ErrorResponse";
    }


}
