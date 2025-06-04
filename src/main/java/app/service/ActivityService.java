package app.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.repository.ActivityRepository;
import dbTableMapper.Activity;

@Service
public class ActivityService {
	
	@Autowired
	private ActivityRepository repository;
	
	public List<Activity> getAllActivities() {
		return repository.findAll();
	}
	
	public void deleteActivity(Long id) {
		repository.deleteActivity(id);
	}
	
	public void closeAsctivity(Long id) {
		repository.closeActivity(id);
	}

	public void exportToExcel(List<Activity> data, File file) throws IOException {
	    try (Workbook workbook = new XSSFWorkbook()) {
	        Sheet sheet = workbook.createSheet("Attività");

	        // Intestazione
	        Row header = sheet.createRow(0);
	        String[] headers = { "ID", "Nome", "Descrizione", "Inizio", "Fine", "Fine Prevista", "Ore", "Completata", "Priorità" };
	        for (int i = 0; i < headers.length; i++) {
	            header.createCell(i).setCellValue(headers[i]);
	        }

	        // Dati
	        int rowIdx = 1;
	        for (Activity a : data) {
	            Row row = sheet.createRow(rowIdx++);
	            row.createCell(0).setCellValue(a.getId());
	            row.createCell(1).setCellValue(a.getNome());
	            row.createCell(2).setCellValue(a.getDescrizione());
	            row.createCell(3).setCellValue(a.getInizio() != null ? a.getInizio().toString() : "");
	            row.createCell(4).setCellValue(a.getFine() != null ? a.getFine().toString() : "");
	            row.createCell(5).setCellValue(a.getFine_prev() != null ? a.getFine_prev().toString() : "");
	            row.createCell(6).setCellValue(a.getOre());
	            row.createCell(7).setCellValue(a.isDone());
	            row.createCell(8).setCellValue(a.getPriority());
	        }

	        // Autosize colonne
	        for (int i = 0; i < headers.length; i++) {
	            sheet.autoSizeColumn(i);
	        }

	        // Scrittura file
	        try (FileOutputStream fos = new FileOutputStream(file)) {
	            workbook.write(fos);
	        }
	    }
	}

	public void exportToExcel(List<Activity> data, File file, boolean isUpdate) throws IOException {
	    Workbook workbook;
	    Sheet sheet;
	    int rowIdx;

	    if (isUpdate && file.exists()) {
	        // Apri file esistente (mantiene grafici)
	        try (FileInputStream fis = new FileInputStream(file)) {
	            workbook = new XSSFWorkbook(fis);
	        }
	        sheet = workbook.getSheetAt(0);
	        rowIdx = sheet.getLastRowNum() + 1; // Continua da ultima riga
	    } else {
	        // Nuovo file
	        workbook = new XSSFWorkbook();
	        sheet = workbook.createSheet("Attività");

	        // Intestazioni
	        Row header = sheet.createRow(0);
	        String[] headers = { "ID", "Nome", "Descrizione", "Inizio", "Fine", "Fine Prevista", "Ore", "Completata", "Priorità" };
	        for (int i = 0; i < headers.length; i++) {
	            header.createCell(i).setCellValue(headers[i]);
	        }
	        rowIdx = 1;
	    }

	    // Dati
	    for (Activity a : data) {
	        Row row = sheet.createRow(rowIdx++);
	        row.createCell(0).setCellValue(a.getId());
	        row.createCell(1).setCellValue(a.getNome());
	        row.createCell(2).setCellValue(a.getDescrizione());
	        row.createCell(3).setCellValue(a.getInizio() != null ? a.getInizio().toString() : "");
	        row.createCell(4).setCellValue(a.getFine() != null ? a.getFine().toString() : "");
	        row.createCell(5).setCellValue(a.getFine_prev() != null ? a.getFine_prev().toString() : "");
	        row.createCell(6).setCellValue(a.getOre());
	        row.createCell(7).setCellValue(a.isDone());
	        row.createCell(8).setCellValue(a.getPriority());
	    }

	    // Scrittura finale (fuori da blocco condizionale!)
	    try (FileOutputStream fos = new FileOutputStream(file)) {
	        workbook.write(fos);
	    }

	    workbook.close();
	}



}
