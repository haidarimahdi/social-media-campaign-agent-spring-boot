package com.example.socialmediacampaignagentsprintboot.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrandVoiceService {
    private static final Path EXTERNAL_FILE = Paths.get("brand-voice.txt");
//    private final ResourceLoader resourceLoader;

//    @PostConstruct
//    public void init() {
//        if (!Files.exists(EXTERNAL_FILE)) {
//            try {
//                // Copy the default read-only resource to a writable location
//                byte[] defaultContent = resourceLoader.getResource("classpath:/brand-voice.txt").getInputStream().readAllBytes();
//                Files.write(EXTERNAL_FILE, defaultContent);
//                log.info("Initialized writable brand-voice.txt on the hard drive.");
//            } catch (IOException e) {
//                log.error("Failed to initialize brand voice file.", e);
//            }
//        }
//    }

    public String getBrandVoice() {
        if (!Files.exists(EXTERNAL_FILE)) {
            return "";
        }
        try {
            return Files.readString(EXTERNAL_FILE);
        } catch (IOException e) {
            log.error("Error reading brand voice", e);
            return "";
        }
    }

    public void saveBrandVoice(String content) {
        try {
            Files.writeString(EXTERNAL_FILE, content != null ? content : "");
            log.info("Human operator updated the global Brand Voice constraints.");
        } catch (IOException e) {
            log.error("Failed to save brand voice", e);
        }
    }
}
