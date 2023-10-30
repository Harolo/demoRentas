package com.demo_rentas.Controllers;

import com.demo_rentas.Models.ResService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.HttpEntity;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Controller
public class RentasController {

    @Value("${uri.service1}")
    private String uriService1;

    @Value("${uploadDirTxtR}")
    private String uploadDirTxtR;

    @Value("${uploadDirZipR}")
    private String uploadDirZipR;

    @Value("${downloadLogs}")
    private String downloadLogs;

    @Value("${downloadLogsSystem}")
    private String downloadSystemLogs;


    @GetMapping("rentas")
    public String rentas() {
        return "Rentas/RentasTxt";
    }

    @GetMapping("probarServicioRentas")
    public String obtenerRespuestaRen(Model model) throws IOException {
        ResService resService = new ResService();

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(uriService1);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequest = objectMapper.writeValueAsString(resService);

        StringEntity entity = new StringEntity(jsonRequest);
        entity.setContentType("application/json");
        httpPost.setEntity(entity);

        HttpResponse responseHttp = httpClient.execute(httpPost);
        HttpEntity responseEntity2 = responseHttp.getEntity();
        String jsonResponse = EntityUtils.toString(responseEntity2);

        ObjectMapper objectMapper2 = new ObjectMapper();
        ResService[] responseArray = objectMapper2.readValue(jsonResponse, ResService[].class);
        List<ResService> responseList = Arrays.asList(responseArray);
        model.addAttribute("resService", responseList);
        return "Respuesta/RespuestaServicioRentas";
    }

    @RequestMapping("uploadTXTRentas")
    public String uploadFileTxtRen(@RequestParam("fileTxt") MultipartFile file) {
        if (!file.isEmpty()) {
            if (file.getOriginalFilename().endsWith(".txt")) {
                try {
                    File uploadDir = new File(uploadDirTxtR);
                    if (!uploadDir.exists()) {
                        uploadDir.mkdirs();
                    }
                    Path filePath = Paths.get(uploadDirTxtR + File.separator + file.getOriginalFilename());
                    Files.write(filePath,file.getBytes());

                    return "SuccessResponse";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "ErrorResponse";
                }
            } else {
                return "redirect:ErrorResponse?message=el archivo debe ser .txt";
            }
        }
        return "ErrorResponse";
    }

    @RequestMapping("uploadZIPRentas")
    public String uploadFileZipRen(@RequestParam("fileZip") MultipartFile file) {
        if (!file.isEmpty()) {
            if (file.getOriginalFilename().endsWith(".zip")) {
                try {

                    File uploadDir = new File(uploadDirZipR);
                    if (!uploadDir.exists()) {
                        uploadDir.mkdirs();
                    }
                    File uploadedFile = new File(uploadDirZipR + File.separator + file.getOriginalFilename());
                    file.transferTo(uploadedFile);

                    return "SuccessResponse";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "ErrorResponse";
                }
            } else {
                return "redirect:ErrorResponse?message=el archivo debe ser .zip";
            }
        }
        return "ErrorResponse";
    }

    @GetMapping("downloadLogsRen")
    public void downloadFilesRen(HttpServletResponse response) {

        try {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=archivosLogsSoliRentas.zip");
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
                    addToZipFileRen(file, zipOut);
                } else if (file.isDirectory()) {
                    comprimirDirectorio(file, zipOut);
                }
            }
        }
    }

    private static void addToZipFileRen(File file, ZipOutputStream zipOut) throws IOException {
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

    @GetMapping("ErrorResponse")
    public String showErrorPageTxtRen(@RequestParam("message") String errorMessage, Model model) {
        model.addAttribute("errorMessage", errorMessage);
        return "ErrorResponse";
    }

}
