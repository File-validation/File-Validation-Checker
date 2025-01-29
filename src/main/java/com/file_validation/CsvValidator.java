package com.file_validation;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import java.io.*;
import java.util.*;

public class CsvValidator {

    public void validateCSV(String inputCsvFilePath, String outputCsvFilePath) throws IOException, CsvException {
        File inputFile = new File(inputCsvFilePath);

        // Check if the input file exists
        if (!inputFile.exists()) {
            System.out.println("CSV file not found: " + inputCsvFilePath);
            return;
        }

        List<String[]> rows = new ArrayList<>();
        Map<String, List<Integer>> duplicateSkuMap = new HashMap<>();

        try (CSVReader csvReader = new CSVReader(new FileReader(inputCsvFilePath))) {
            // Read the header
            String[] header = csvReader.readNext();
            if (header == null) {
                System.out.println("CSV file is empty!");
                return;
            }

            // Add a new column for error logs
            String[] extendedHeader = Arrays.copyOf(header, header.length + 1);
            extendedHeader[header.length] = "Error Log";
            rows.add(extendedHeader);

            // Read and validate rows
            String[] row;
            int rowNumber = 1; 
            while ((row = csvReader.readNext()) != null) {
                rowNumber++;
                String errorLog = validateRow(row, rowNumber, duplicateSkuMap);
                String[] extendedRow = Arrays.copyOf(row, row.length + 1);
                extendedRow[row.length] = errorLog;
                rows.add(extendedRow);
            }
        }

        // Check duplicate SKU 
        for (Map.Entry<String, List<Integer>> entry : duplicateSkuMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                for (int rowIndex : entry.getValue()) {
                    String[] row = rows.get(rowIndex);
                    row[row.length - 1] += "Duplicate Variant SKU found: " + entry.getKey() + "; ";
                }
            }
        }

        // Write the updated rows with error logs to the specified output CSV file
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(outputCsvFilePath))) {
            csvWriter.writeAll(rows);
        }

        System.out.println("Validation complete. Updated CSV with error logs written to: " + outputCsvFilePath);
    }

    private String validateRow(String[] row, int rowNumber, Map<String, List<Integer>> duplicateSkuMap) {
        StringBuilder errorLog = new StringBuilder();
        String handle = row[0];
        String option1Name = row[2];
        String option1Value = row[3];
        String option2Name = row[4];
        String option2Value = row[5];
        String variantSku = row[6];

        // Validate header
        if (handle == null || handle.trim().isEmpty()) {
            errorLog.append("Handle cannot be null or empty; ");
        }

        // Validate Option Names
        boolean validOption1 = "Color".equals(option1Name) || "Size".equals(option1Name);
        boolean validOption2 = "Color".equals(option2Name) || "Size".equals(option2Name) || option2Name == null;

        if (!validOption1) {
            errorLog.append("Option1 Name must be 'Color' or 'Size'; ");
        }
        if (!validOption2) {
            errorLog.append("Option2 Name must be 'Color' or 'Size'; ");
        }

        // Validate that at least one option value is present
        boolean hasOptionValue = (option1Value != null && !option1Value.trim().isEmpty()) ||
                                 (option2Value != null && !option2Value.trim().isEmpty());
        if (!hasOptionValue) {
            errorLog.append("At least one option value (Color or Size) must be provided; ");
        }

        // Validate SKU
        if (variantSku == null || variantSku.trim().isEmpty()) {
            if (handle != null && !handle.trim().isEmpty() && hasOptionValue) {
                errorLog.append("Variant SKU cannot be null if handle and at least one option are provided; ");
            }
        } else {
            duplicateSkuMap.computeIfAbsent(variantSku, k -> new ArrayList<>()).add(rowNumber);
        }

        return errorLog.toString().trim();
    }
}
