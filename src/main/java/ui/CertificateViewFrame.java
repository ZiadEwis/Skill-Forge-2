package ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Certificate;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CertificateViewFrame extends BaseFrame {
    private final Certificate certificate;
    private JPanel contentPanel;

    public CertificateViewFrame(Certificate certificate) {
        super("Certificate of Completion");
        this.certificate = certificate;
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();
    }

    @Override
    protected void initComponents() {
        setLayout(new BorderLayout());

        JPanel certPanel = new JPanel();
        certPanel.setLayout(new GridBagLayout());
        certPanel.setBackground(Color.WHITE);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        contentPanel.setBackground(Color.WHITE);

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        innerPanel.setBackground(Color.WHITE);

        JLabel titleLbl = new JLabel("Certificate of Completion");
        titleLbl.setFont(new Font("Serif", Font.BOLD, 22));
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerPanel.add(titleLbl);
        innerPanel.add(Box.createVerticalStrut(20));

        JLabel certifyLbl = new JLabel("This certifies that");
        certifyLbl.setFont(new Font("Serif", Font.PLAIN, 13));
        certifyLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerPanel.add(certifyLbl);
        innerPanel.add(Box.createVerticalStrut(8));

        JLabel nameLbl = new JLabel(certificate.getStudentName());
        nameLbl.setFont(new Font("Serif", Font.BOLD, 18));
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerPanel.add(nameLbl);
        innerPanel.add(Box.createVerticalStrut(15));

        JLabel completedLbl = new JLabel("has successfully completed");
        completedLbl.setFont(new Font("Serif", Font.PLAIN, 13));
        completedLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerPanel.add(completedLbl);
        innerPanel.add(Box.createVerticalStrut(8));

        JLabel courseLbl = new JLabel(certificate.getCourseTitle());
        courseLbl.setFont(new Font("Serif", Font.BOLD, 16));
        courseLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerPanel.add(courseLbl);
        innerPanel.add(Box.createVerticalStrut(20));

        JLabel dateLbl = new JLabel("Date: " + certificate.getFormattedIssueDate());
        dateLbl.setFont(new Font("Dialog", Font.PLAIN, 11));
        dateLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerPanel.add(dateLbl);
        innerPanel.add(Box.createVerticalStrut(3));

        JLabel idLbl = new JLabel("Certificate ID: " + certificate.getCertificateId());
        idLbl.setFont(new Font("Dialog", Font.PLAIN, 10));
        idLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerPanel.add(idLbl);

        contentPanel.add(innerPanel);
        certPanel.add(contentPanel);
        add(certPanel, BorderLayout.CENTER);

        // Button Panel with Export Options
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton exportJsonBtn = createStyledButton("Export as JSON", null);
        exportJsonBtn.addActionListener(e -> exportAsJSON());
        btnPanel.add(exportJsonBtn);
        
        JButton exportPdfBtn = createStyledButton("Export as PDF", null);
        exportPdfBtn.addActionListener(e -> exportAsPDF());
        btnPanel.add(exportPdfBtn);
        
        JButton closeBtn = createStyledButton("Close", null);
        closeBtn.addActionListener(e -> dispose());
        btnPanel.add(closeBtn);
        
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void exportAsJSON() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Certificate as JSON");
        
        String defaultName = "Certificate_" + certificate.getCertificateId() + ".json";
        fileChooser.setSelectedFile(new File(defaultName));
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            // Ensure .json extension
            if (!fileToSave.getName().toLowerCase().endsWith(".json")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".json");
            }
            
            try (FileWriter writer = new FileWriter(fileToSave)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(certificate);
                writer.write(json);
                
                showSuccess("Certificate exported as JSON successfully!\nLocation: " + fileToSave.getAbsolutePath());
            } catch (IOException ex) {
                showError("Error exporting certificate: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void exportAsPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Certificate as PDF");
        
        String defaultName = "Certificate_" + certificate.getCertificateId() + ".pdf";
        fileChooser.setSelectedFile(new File(defaultName));
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            // Ensure .pdf extension
            if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
            }
            
            try {
                createPDF(fileToSave);
                showSuccess("Certificate exported as PDF successfully!\nLocation: " + fileToSave.getAbsolutePath());
            } catch (Exception ex) {
                showError("Error exporting certificate: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void createPDF(File file) throws IOException {
        PDDocument document = new PDDocument();
        try {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // Page dimensions
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float margin = 50;
            
            // Draw outer border
            contentStream.setLineWidth(3);
            contentStream.addRect(margin, margin, pageWidth - 2 * margin, pageHeight - 2 * margin);
            contentStream.stroke();
            
            // Draw inner border
            contentStream.setLineWidth(1);
            contentStream.addRect(margin + 10, margin + 10, 
                                 pageWidth - 2 * margin - 20, 
                                 pageHeight - 2 * margin - 20);
            contentStream.stroke();
            
            // Starting Y position (from top)
            float yPosition = pageHeight - 150;
            
            // Title - "CERTIFICATE OF COMPLETION"
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_BOLD, 28);
            String title = "CERTIFICATE OF COMPLETION";
            float titleWidth = PDType1Font.TIMES_BOLD.getStringWidth(title) / 1000 * 28;
            contentStream.newLineAtOffset((pageWidth - titleWidth) / 2, yPosition);
            contentStream.showText(title);
            contentStream.endText();
            
            yPosition -= 80;
            
            // "This certifies that"
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, 14);
            String certifies = "This certifies that";
            float certifiesWidth = PDType1Font.TIMES_ROMAN.getStringWidth(certifies) / 1000 * 14;
            contentStream.newLineAtOffset((pageWidth - certifiesWidth) / 2, yPosition);
            contentStream.showText(certifies);
            contentStream.endText();
            
            yPosition -= 40;
            
            // Student Name
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_BOLD, 22);
            String studentName = certificate.getStudentName();
            float nameWidth = PDType1Font.TIMES_BOLD.getStringWidth(studentName) / 1000 * 22;
            contentStream.newLineAtOffset((pageWidth - nameWidth) / 2, yPosition);
            contentStream.showText(studentName);
            contentStream.endText();
            
            // Draw line under name
            float lineY = yPosition - 5;
            float lineStartX = (pageWidth - nameWidth) / 2 - 20;
            float lineEndX = (pageWidth + nameWidth) / 2 + 20;
            contentStream.moveTo(lineStartX, lineY);
            contentStream.lineTo(lineEndX, lineY);
            contentStream.stroke();
            
            yPosition -= 50;
            
            // "has successfully completed the course"
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, 14);
            String completed = "has successfully completed the course";
            float completedWidth = PDType1Font.TIMES_ROMAN.getStringWidth(completed) / 1000 * 14;
            contentStream.newLineAtOffset((pageWidth - completedWidth) / 2, yPosition);
            contentStream.showText(completed);
            contentStream.endText();
            
            yPosition -= 40;
            
            // Course Title
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_BOLD, 18);
            String courseTitle = certificate.getCourseTitle();
            
            // Handle long course titles by wrapping
            if (courseTitle.length() > 50) {
                courseTitle = courseTitle.substring(0, 47) + "...";
            }
            
            float courseTitleWidth = PDType1Font.TIMES_BOLD.getStringWidth(courseTitle) / 1000 * 18;
            contentStream.newLineAtOffset((pageWidth - courseTitleWidth) / 2, yPosition);
            contentStream.showText(courseTitle);
            contentStream.endText();
            
            yPosition -= 80;
            
            // Issue Date
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, 12);
            String issueDate = "Issued on: " + certificate.getFormattedIssueDate();
            float dateWidth = PDType1Font.TIMES_ROMAN.getStringWidth(issueDate) / 1000 * 12;
            contentStream.newLineAtOffset((pageWidth - dateWidth) / 2, yPosition);
            contentStream.showText(issueDate);
            contentStream.endText();
            
            yPosition -= 20;
            
            // Certificate ID
            contentStream.beginText();
            contentStream.setFont(PDType1Font.COURIER, 10);
            String certId = "Certificate ID: " + certificate.getCertificateId();
            float certIdWidth = PDType1Font.COURIER.getStringWidth(certId) / 1000 * 10;
            contentStream.newLineAtOffset((pageWidth - certIdWidth) / 2, yPosition);
            contentStream.showText(certId);
            contentStream.endText();
            
            // Footer - Platform Name
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ITALIC, 14);
            String footer = "SkillForge Learning Platform";
            float footerWidth = PDType1Font.TIMES_ITALIC.getStringWidth(footer) / 1000 * 14;
            contentStream.newLineAtOffset((pageWidth - footerWidth) / 2, 80);
            contentStream.showText(footer);
            contentStream.endText();
            
            // Decorative line above footer
            contentStream.moveTo(pageWidth / 2 - 100, 95);
            contentStream.lineTo(pageWidth / 2 + 100, 95);
            contentStream.stroke();
            
            contentStream.close();
            document.save(file);
        } finally {
            document.close();
        }
    }
}