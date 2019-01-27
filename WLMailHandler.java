package javaapplication2;


import java.io.*;
import java.util.Properties;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Date;
import javax.swing.*;

/**
 *
 * @author Siddhartha Bhuyan
 */
public class WLMailHandler {

    public static final String DATA_TITLE_1130 = "1130";
    public static final String DATA_TITLE_1430 = "1430";
    public static final String DATA_TITLE_1730 = "1730";
    public static final String DATA_BULLETIN_TITLE = "waterlevel_nesac";
    public static final String DELIMITER_SPACE = " ";
    public static final String DELIMITER_UNDERSCORE = "_";
    public static final String EMAIL_PASSWORD = "water_level";
    public static final String EMAIL_USERNAME = "waterlevel.nesac@gmail.com";
    public static final String FILE_EXTENSION_PNG = ".png";
    public static final String FILE_EXTENSION_JPG = ".jpg";
    public static final String FOLDER_TO_READ = "inbox";
    public static final String MAILBOX_PROTOCOL = "imaps";
    public static final String SENDER_EMAIL = "waterlevel.nesac@gmail.com";
    public static final String SUBJECT_HOOK_1 = "Waterlevel reports for Dam";
    public static final String SUBJECT_HOOK_2 = "Waterlevel Bulletin";
    public static final String HOST = "smtp.gmail.com";
    public static String PATH_REPORT_FOLDER = "";
    public static String PATH_BULLETIN_FOLDER = "";
    public static final String INFO_LAST_CHECK = "Last reports checked at: ";
    public static final long SLEEP_TIME = 60000;
    public static final String BULLETIN_FORMAT = ".pdf";

    static ArrayList<Message> inboxMails = new ArrayList<Message>();
    static Session emailSession;

    public static void main(String[] args) {
        PATH_REPORT_FOLDER = "C:\\Users\\Kushal Beria\\Desktop\\NESAC\\";
            PATH_BULLETIN_FOLDER = "C:\\Users\\Kushal Beria\\Desktop\\NESAC\\";
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame();
                JPanel panel = new JPanel();
                JLabel label = new JLabel();

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Date dateToday = new Date();
                        label.setText(INFO_LAST_CHECK + dateToday.toString());
                        panel.removeAll();
                        panel.add(label);
                        panel.setVisible(true);
                        frame.add(panel);
                        frame.setVisible(true);
                        ProcessMailsToday(dateToday);
                        System.out.println(INFO_LAST_CHECK + dateToday.toString());
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException ex) {
                        System.out.println(ex.getMessage());
                        Thread.currentThread().interrupt();
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }
        });
        thread.start();
    }

    static void ProcessMailsToday(Date date) {
        try {
            boolean isLatestBulletin = true;
            String fullPath = "";

            Store store;
            inboxMails.clear();
            Properties properties = new Properties();

            properties.put("mail.store.protocol", "imaps");
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.socketFactory.port", "465");
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.port", "465");
            emailSession = Session.getDefaultInstance(properties, null);
            store = emailSession.getStore(MAILBOX_PROTOCOL);
            store.connect(HOST, EMAIL_USERNAME, EMAIL_PASSWORD);

            Folder inboxFolder = store.getFolder(FOLDER_TO_READ);
            inboxFolder.open(Folder.READ_ONLY);
            inboxMails.addAll(Arrays.asList(inboxFolder.getMessages()));

            for (int ind = inboxMails.size() - 1; ind >= 0; ind--) {
                Message currMsg = inboxMails.get(ind);
                String subject = currMsg.getSubject();
                Address[] froms = currMsg.getFrom();
                Date mailDate = currMsg.getSentDate();

                Calendar calendar1 = Calendar.getInstance();
                Calendar calendar2 = Calendar.getInstance();
                calendar1.setTime(date);
                calendar2.setTime(mailDate);
                int currDay = calendar1.get(Calendar.DAY_OF_MONTH);
                int currMonth = calendar1.get(Calendar.MONTH);
                int currYear = calendar1.get(Calendar.YEAR);

                int mailDay = calendar2.get(Calendar.DAY_OF_MONTH);
                int mailMonth = calendar2.get(Calendar.MONTH);
                int mailYear = calendar2.get(Calendar.YEAR);

                String senderId = ((InternetAddress) froms[0]).getAddress();

                boolean subjectCheck = ((subject.toLowerCase().contains(SUBJECT_HOOK_1.toLowerCase())) || (subject.toLowerCase().contains(SUBJECT_HOOK_2.toLowerCase())));
                boolean senderCheck = (senderId.compareTo(SENDER_EMAIL) == 0);
                boolean sameDayCheck = ((currDay == mailDay) && (currMonth == mailMonth) && (currYear == mailYear));

                //int diffInDays = currDay - mailDay;
                if (sameDayCheck == false) {
                    break;
                }

                if (subjectCheck && senderCheck && sameDayCheck) {
                    Multipart multipart = (Multipart) currMsg.getContent();
                    for (int ind1 = 0; ind1 < multipart.getCount(); ind1++) {
                        BodyPart bodyPart = multipart.getBodyPart(ind1);
                        if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                            continue;
                        }
                        InputStream inputStream = bodyPart.getInputStream();
                        //File file = new File("F:\\" + bodyPart.getFileName());
                        if (bodyPart.getFileName().toLowerCase().contains(BULLETIN_FORMAT)) {
                            if (isLatestBulletin == true) {
                                fullPath = PATH_BULLETIN_FOLDER + bodyPart.getFileName();
                                isLatestBulletin = false;
                                System.out.println(fullPath);
                                File file = new File(fullPath);
                                FileOutputStream fileOutputStream = new FileOutputStream(file);
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    fileOutputStream.write(buffer, 0, bytesRead);
                                }
                                fileOutputStream.close();
                                inputStream.close();
                            }
                        } else {
                            fullPath = PATH_REPORT_FOLDER + bodyPart.getFileName();
                            System.out.println(fullPath);
                            File file = new File(fullPath);
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                fileOutputStream.write(buffer, 0, bytesRead);
                            }
                            fileOutputStream.close();
                            inputStream.close();
                        }
                    }
                }
            }
            inboxFolder.close(false);
            store.close();
            //System.out.println("Done");
        } catch (NoSuchProviderException ex) {
            System.out.println(ex.getMessage());
        } catch (MessagingException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}