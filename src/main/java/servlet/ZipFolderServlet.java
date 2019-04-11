package servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = { "/zip" })
public class ZipFolderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    String UPLOAD_DIRECTORY = "uploads";
    String DOWNLOAD_DIRECTORY = "downloads";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String uploadPath = getServletContext().getRealPath("/") + File.separator + UPLOAD_DIRECTORY;

        String snapshot2 = request.getParameter("snapshot2");
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=New " + snapshot2 + ".zip");

        ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
        File fileToZip = new File(uploadPath + File.separator + snapshot2);

        updatePackageFile(uploadPath, snapshot2, "package", 6);

        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                if (!childFile.isDirectory() && childFile.length() == 0) {
                    childFile.delete();
                } else {
                    zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
                }
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName.split("/", 2)[1]);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    public void updatePackageFile(String uploadPath, String snapshot2, String fileName, int position)
            throws IOException {
        String snapshotId = getUUID();

        Date date = new Date();
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(date);

        String snapshotTarget = "<snapshot id=\"2064." + snapshotId + "\" name=\"" + "Snapshot "
                + snapshotId.split("-")[4] + "\" acronym=\"S1\"  originalCreationDate=\"" + formattedDate
                + "\" description=\"\"/>";

        BufferedReader reader2 = new BufferedReader(new FileReader(uploadPath + File.separator + snapshot2
                + File.separator + "META-INF" + File.separator + fileName + ".xml"));

        PrintWriter pw = new PrintWriter(uploadPath + File.separator + snapshot2 + File.separator + "META-INF"
                + File.separator + "tmp" + ".xml");

        String line2 = reader2.readLine();

        int lineNum = 1;

        while (line2 != null) {
            if (lineNum == position) {
                pw.println(snapshotTarget);
            } else {
                pw.println(line2);
            }

            line2 = reader2.readLine();

            lineNum++;
        }

        pw.flush();
        pw.close();
        reader2.close();

        File new_file = new File(uploadPath + File.separator + snapshot2 + File.separator + "META-INF" + File.separator
                + fileName + ".xml");
        File old_file = new File(uploadPath + File.separator + snapshot2 + File.separator + "META-INF" + File.separator
                + "tmp" + ".xml");
        new_file.delete();
        old_file.renameTo(new_file);

    }

    private static String getUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}