package servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import com.github.difflib.pojo.FileChanges;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;

@WebServlet(urlPatterns = { "/comparePackage" })
@MultipartConfig
public class ComparePackageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public ComparePackageServlet() {
        super();
    }

    String UPLOAD_DIRECTORY = "uploads";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String uploadPath = getServletContext().getRealPath("/") + File.separator + UPLOAD_DIRECTORY;
        // UPLOAD_DIRECTORY;
        System.out.println("My value ---------------------------------------------------------- ");
        String back = request.getParameter("back");
        System.out.println("My value ---------------------------- " + back);
//        String back = "";
        String snapshot1 = "";
        String snapshot2 = "";

        // File1 is greater, checkLength = 1
        if (back == null || back.equals("")) {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            Part file1 = request.getPart("file1"); // Retrieves <input
                                                   // type="file" name="file">
            String fileName1 = Paths.get(file1.getSubmittedFileName()).getFileName().toString(); // MSIE
                                                                                                 // fix.
            snapshot1 = fileName1.substring(0, fileName1.indexOf("."));
            file1.write(uploadPath + File.separator + fileName1);

            // InputStream fileContent = filePart.getInputStream();
            Part file2 = request.getPart("file2"); // Retrieves <input
                                                   // type="file" name="file">
            String fileName2 = Paths.get(file2.getSubmittedFileName()).getFileName().toString(); // MSIE
                                                                                                 // fix.
            snapshot2 = fileName2.substring(0, fileName2.indexOf("."));
            file2.write(uploadPath + File.separator + fileName2);

            unzip(uploadPath + File.separator + fileName1, uploadPath + File.separator + snapshot1);
            unzip(uploadPath + File.separator + fileName2, uploadPath + File.separator + snapshot2);
        } else {
            snapshot1 = request.getParameter("snapshot1");
            snapshot2 = request.getParameter("snapshot2");
        }

        HashMap<String, ArrayList<FileChanges>> fileChangesList = getFileChanges1(uploadPath,
                snapshot1 + File.separator + "META-INF" + File.separator + "package.xml",
                snapshot2 + File.separator + "META-INF" + File.separator + "package.xml");

        request.setAttribute("object", fileChangesList.get("object"));
        request.setAttribute("process", fileChangesList.get("process"));
        request.setAttribute("bpd", fileChangesList.get("bpd"));
        request.setAttribute("environmentVariableSet", fileChangesList.get("environmentVariableSet"));
        request.setAttribute("coachView", fileChangesList.get("coachView"));
        request.setAttribute("snapshot1", snapshot1);
        request.setAttribute("snapshot2", snapshot2);
        RequestDispatcher dispatcher = this.getServletContext()
                .getRequestDispatcher("/WEB-INF/views/comparePackageView.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private static void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileInputStream fis;
        // buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to " + newFile.getAbsolutePath());
                // create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                // close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            // close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static HashMap<String, ArrayList<FileChanges>> getFileChanges(String uploadPath, String fileName1,
            String fileName2, int checkLength) throws IOException {

        File xml1 = new File(uploadPath + File.separator + fileName1);
        File xml2 = new File(uploadPath + File.separator + fileName2);

        List<String> original = Files.readAllLines(xml1.toPath());
        List<String> revised = Files.readAllLines(xml2.toPath());

        DiffRowGenerator generator = DiffRowGenerator.create().showInlineDiffs(true).inlineDiffByWord(true)
                .oldTag(f -> "").newTag(f -> "").build();
        List<DiffRow> rows = null;
        try {
            rows = generator.generateDiffRows(original, revised);
        } catch (DiffException e) {
            e.printStackTrace();
        }

        ArrayList<FileChanges> objectsList = new ArrayList<>();
        ArrayList<FileChanges> processList = new ArrayList<>();
        ArrayList<FileChanges> bpdList = new ArrayList<>();
        ArrayList<FileChanges> envList = new ArrayList<>();
        ArrayList<FileChanges> coachViewList = new ArrayList<>();

        HashMap<String, ArrayList<FileChanges>> objectMap = new HashMap<>();
        int lineNum = 1;
        for (DiffRow row : rows) {
            System.out.println(row.getTag() + "|" + row.getOldLine() + "|" + row.getNewLine() + "|");

            if (row.getOldLine().contains("object id") || row.getNewLine().contains("object id")) {

                String obj;
                if (checkLength == 1) {
                    obj = !row.getOldLine().equals("") ? row.getOldLine() : row.getNewLine();
                } else {
                    obj = !row.getOldLine().equals("") ? row.getNewLine() : row.getOldLine();
                }

                int idIndex = obj.indexOf("id=");
                int versionIndex = obj.indexOf("versionId=");
                int fileNameIndex = obj.indexOf("name=");
                int typeIndex = obj.indexOf("type=");

                String type = obj.substring(typeIndex + 6, obj.length() - 6);

                FileChanges change = new FileChanges();
                change.setFileName(obj.substring(idIndex + 4, versionIndex - 2));
                change.setObjectName(obj.substring(fileNameIndex + 6, typeIndex - 2));
                change.setChangeType(row.getTag().name().equals("EQUAL") ? "NOT CHANGE" : row.getTag().name());
                change.setObjectType(type);
                change.setLineNum(lineNum);

                if (type.equals("twClass")) {
                    objectsList.add(change);
                } else if (type.equals("process")) {
                    processList.add(change);
                } else if (type.equals("bpd")) {
                    bpdList.add(change);
                } else if (type.equals("environmentVariableSet")) {
                    envList.add(change);
                } else if (type.equals("coachView")) {
                    coachViewList.add(change);
                }
            }
            lineNum++;
        }

        objectMap.put("object", objectsList);
        objectMap.put("process", processList);
        objectMap.put("bpd", bpdList);
        objectMap.put("environmentVariableSet", envList);
        objectMap.put("coachView", coachViewList);

        return objectMap;
    }

    private static HashMap<String, ArrayList<FileChanges>> getFileChanges1(String uploadPath, String fileName1,
            String fileName2) throws IOException {

        ArrayList<FileChanges> objectsList = new ArrayList<>();
        ArrayList<FileChanges> processList = new ArrayList<>();
        ArrayList<FileChanges> bpdList = new ArrayList<>();
        ArrayList<FileChanges> envList = new ArrayList<>();
        ArrayList<FileChanges> coachViewList = new ArrayList<>();

        File xml1 = new File(uploadPath + File.separator + fileName1);
        File xml2 = new File(uploadPath + File.separator + fileName2);

        List<String> original = Files.readAllLines(xml1.toPath());
        List<String> revised = Files.readAllLines(xml2.toPath());

        Patch<String> patch;
        List<AbstractDelta<String>> deltas;
        try {
            patch = DiffUtils.diff(original, revised);
            deltas = patch.getDeltas();
            int endPos = 0;
            int lineNum = 1;
            for (AbstractDelta<String> delta : deltas) {
                for (String line : original.subList(endPos, delta.getSource().getPosition())) {
                    System.out.println(line);
                    if (line.contains("object id")) {

                        String obj = line.trim();

                        int idIndex = obj.indexOf("id=");
                        int versionIndex = obj.indexOf("versionId=");
                        int fileNameIndex = obj.indexOf("name=");
                        int typeIndex = obj.indexOf("type=");

                        String type = obj.substring(typeIndex + 6, obj.length() - 3);

                        FileChanges change = new FileChanges();
                        change.setFileName(obj.substring(idIndex + 4, versionIndex - 2));
                        change.setObjectName(obj.substring(fileNameIndex + 6, typeIndex - 2));
                        change.setChangeType("NOT CHANGE");
                        change.setObjectType(type);
                        change.setLineNum(lineNum);

                        if (type.equals("twClass")) {
                            objectsList.add(change);
                        } else if (type.equals("process")) {
                            processList.add(change);
                        } else if (type.equals("bpd")) {
                            bpdList.add(change);
                        } else if (type.equals("environmentVariableSet")) {
                            envList.add(change);
                        } else if (type.equals("coachView")) {
                            coachViewList.add(change);
                        }
                    }
                    lineNum++;
                }

                if (delta.getSource().size() > delta.getTarget().size()) {
                    for (String line : delta.getSource().getLines()) {
                        System.out.println(line);
                        if (line.contains("object id")) {

                            String obj = line;
                            String target = delta.getTarget().getLines().size() > 0
                                    ? delta.getTarget().getLines().get(0) : "";

                            int idIndex = obj.indexOf("id=");
                            int versionIndex = obj.indexOf("versionId=");

                            String type = getObjectType(obj);

                            FileChanges change = new FileChanges();
                            change.setFileName(obj.substring(idIndex + 4, versionIndex - 2));
                            change.setObjectName(getObjectName(obj));

                            if (!target.equals("") && getObjectName(obj).equals(getObjectName(target))
                                    && getObjectType(obj).equals(getObjectType(target))) {
                                if (getVersion(obj).equals(getVersion(target))) {
                                    change.setChangeType("NOT CHANGE");
                                } else {
                                    change.setChangeType("CHANGE");
                                }
                            } else {
                                change.setChangeType("DELETE");
                            }

                            change.setObjectType(type);
                            change.setLineNum(lineNum);

                            if (type.equals("twClass")) {
                                objectsList.add(change);
                            } else if (type.equals("process")) {
                                processList.add(change);
                            } else if (type.equals("bpd")) {
                                bpdList.add(change);
                            } else if (type.equals("environmentVariableSet")) {
                                envList.add(change);
                            } else if (type.equals("coachView")) {
                                coachViewList.add(change);
                            }
                        }
                        lineNum++;
                    }
                } else if (delta.getSource().size() < delta.getTarget().size()) {
                    for (String line : delta.getTarget().getLines()) {
                        System.out.println(line);
                        if (line.contains("object id")) {

                            String obj = line;
                            String ori = delta.getSource().getLines().size() > 0 ? delta.getSource().getLines().get(0)
                                    : "";

                            int idIndex = obj.indexOf("id=");
                            int versionIndex = obj.indexOf("versionId=");

                            String type = getObjectType(obj);

                            FileChanges change = new FileChanges();
                            change.setFileName(obj.substring(idIndex + 4, versionIndex - 2));
                            change.setObjectName(getObjectName(obj));
                            if (!ori.equals("") && getObjectName(obj).equals(getObjectName(ori))
                                    && getObjectType(obj).equals(getObjectType(ori))) {
                                if (getVersion(obj).equals(getVersion(ori))) {
                                    change.setChangeType("NOT CHANGE");
                                } else {
                                    change.setChangeType("CHANGE");
                                }
                            } else {
                                change.setChangeType("INSERT");
                            }
                            change.setObjectType(type);
                            change.setLineNum(lineNum);

                            if (type.equals("twClass")) {
                                objectsList.add(change);
                            } else if (type.equals("process")) {
                                processList.add(change);
                            } else if (type.equals("bpd")) {
                                bpdList.add(change);
                            } else if (type.equals("environmentVariableSet")) {
                                envList.add(change);
                            } else if (type.equals("coachView")) {
                                coachViewList.add(change);
                            }
                        }
                        lineNum++;
                    }

                } else {
                    for (String line : delta.getTarget().getLines()) {
                        System.out.println(line);
                        if (line.contains("object id")) {

                            String obj = line;

                            int idIndex = obj.indexOf("id=");
                            int versionIndex = obj.indexOf("versionId=");

                            String type = getObjectType(obj);

                            FileChanges change = new FileChanges();
                            change.setFileName(obj.substring(idIndex + 4, versionIndex - 2));
                            change.setObjectName(getObjectName(obj));
                            change.setChangeType("CHANGE");

                            change.setObjectType(type);
                            change.setLineNum(lineNum);

                            if (type.equals("twClass")) {
                                objectsList.add(change);
                            } else if (type.equals("process")) {
                                processList.add(change);
                            } else if (type.equals("bpd")) {
                                bpdList.add(change);
                            } else if (type.equals("environmentVariableSet")) {
                                envList.add(change);
                            } else if (type.equals("coachView")) {
                                coachViewList.add(change);
                            }
                        }
                        lineNum++;
                    }
                }

                endPos = delta.getSource().last() + 1;
            }
        } catch (DiffException e1) {
            e1.printStackTrace();
        }

        HashMap<String, ArrayList<FileChanges>> objectMap = new HashMap<>();
        objectMap.put("object", objectsList);
        objectMap.put("process", processList);
        objectMap.put("bpd", bpdList);
        objectMap.put("environmentVariableSet", envList);
        objectMap.put("coachView", coachViewList);

        return objectMap;
    }

    private static String getObjectName(String line) {
        int fileNameIndex = line.indexOf("name=");
        int typeIndex = line.indexOf("type=");

        return line.substring(fileNameIndex + 6, typeIndex - 2);
    }

    private static String getObjectType(String line) {
        int typeIndex = line.indexOf("type=");
        return line.substring(typeIndex + 6, line.length() - 3);
    }

    private static String getVersion(String line) {
        int versionIndex = line.indexOf("versionId=");
        int fileNameIndex = line.indexOf("name=");
        return line.substring(versionIndex + 11, fileNameIndex - 2);
    }
}
