package com.file_validation;

import com.opencsv.exceptions.CsvException;

import java.io.IOException;

public class App {
    public static void main(String[] args) {
        String inputCsvFilePath = "/home/eleos/Downloads/products_export_test_1.csv";
        String outputCsvFilePath = "/home/eleos/Documents/error_validated_products_export.csv";

        try {
            CsvValidator validator = new CsvValidator();
            validator.validateCSV(inputCsvFilePath, outputCsvFilePath);
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }
}
