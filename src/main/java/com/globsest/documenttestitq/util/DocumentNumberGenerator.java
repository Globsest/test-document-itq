package com.globsest.documenttestitq.util;

import com.globsest.documenttestitq.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class DocumentNumberGenerator {

    private final DocumentRepository documentRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generateUniqueNumber() {
        String prefix = "DOC-" + LocalDate.now().format(DATE_FORMATTER) + "-";
        String number;
        int attempts = 0;
        int maxAttempts = 100;

        do {
            int random = ThreadLocalRandom.current().nextInt(10000, 99999);
            number = prefix + String.format("%05d", random);
            attempts++;
        } while (documentRepository.findByNumber(number).isPresent() && attempts < maxAttempts);

        if (attempts >= maxAttempts) {
            throw new RuntimeException("Не удалось сгенерировать уникальный номер документа после " + maxAttempts + " попыток");
        }

        return number;
    }
}
