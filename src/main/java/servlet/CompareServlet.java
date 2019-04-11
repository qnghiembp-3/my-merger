package servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.pojo.Changes;
import com.github.difflib.pojo.ChangesSelection;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;

@WebServlet(urlPatterns = { "/compare" })
public class CompareServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public CompareServlet() {
        super();
    }

    String UPLOAD_DIRECTORY = "uploads";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String uploadPath = getServletContext().getRealPath("/") + File.separator + UPLOAD_DIRECTORY;

        String fileName = request.getParameter("fileName");
        String name = request.getParameter("name");
        String snapshot1 = request.getParameter("snapshot1");
        String snapshot2 = request.getParameter("snapshot2");
        String lineNumPar = request.getParameter("lineNum");
        String type = request.getParameter("type");
        String objType = request.getParameter("objType");
        String packLine = request.getParameter("packLine");
        String changeType = request.getParameter("changeType");

        boolean selection = true;

        if (lineNumPar != null) {
            merge(uploadPath, snapshot1, snapshot2, fileName, Integer.parseInt(lineNumPar), type);
            updatePackageFile(uploadPath, snapshot1, snapshot2, "package", Integer.parseInt(packLine), fileName, name,
                    objType, changeType);
        }

        File xml1 = new File(uploadPath + File.separator + snapshot1 + File.separator + "objects" + File.separator
                + fileName + ".xml");
        File xml2 = new File(uploadPath + File.separator + snapshot2 + File.separator + "objects" + File.separator
                + fileName + ".xml");

        if (!xml1.exists()) {
            xml1.createNewFile();
        }
        if (!xml2.exists()) {
            xml2.createNewFile();
        }

        if (xml1.length() == 0 || xml2.length() == 0) {
            selection = false;
        }

        List<String> original = Files.readAllLines(xml1.toPath());
        List<String> revised = Files.readAllLines(xml2.toPath());

        DiffRowGenerator generator = DiffRowGenerator.create().showInlineDiffs(true).inlineDiffByWord(true)
                .oldTag(f -> f ? "<span class=\"editOldInline\"><del>" : "</del></span>")
                .newTag(f -> f ? "<span class=\"editNewInline\"><b>" : "</b></span>").build();
        List<DiffRow> rows = null;
        try {
            rows = generator.generateDiffRows(original, revised);
        } catch (DiffException e) {
            e.printStackTrace();
        }

        ArrayList<Changes> changeList1 = new ArrayList<>();
        ArrayList<ChangesSelection> changeSelection = new ArrayList<>();

        int lineNum = 0;
        for (DiffRow row : rows) {
            lineNum++;
            Changes change = new Changes();
            change.setPosition(lineNum);
            change.setSource1(row.getOldLine());
            change.setTarget1(row.getNewLine());

            changeList1.add(change);

            if (row.getTag().name() != DiffRow.Tag.EQUAL.name() && selection == true) {
                ChangesSelection sel = new ChangesSelection();
                sel.setPosition(lineNum);
                sel.setContent(row.getTag().name() + " at line " + lineNum);
                changeSelection.add(sel);
            }

        }

        request.setAttribute("changes", changeList1);
        request.setAttribute("name", name);
        request.setAttribute("fileName", fileName);
        request.setAttribute("changeSelections", changeSelection);
        request.setAttribute("snapshot1", snapshot1);
        request.setAttribute("snapshot2", snapshot2);
        request.setAttribute("objType", objType);
        request.setAttribute("packLine", packLine);
        request.setAttribute("changeType", changeType);

        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/WEB-INF/views/compareView.jsp");

        dispatcher.forward(request, response);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    public void merge(String uploadPath, String snapshot1, String snapshot2, String fileName, int position, String type)
            throws IOException {

        if (position > 0) {
            BufferedReader reader1 = new BufferedReader(new FileReader(uploadPath + File.separator + snapshot1
                    + File.separator + "objects" + File.separator + fileName + ".xml"));

            BufferedReader reader2 = new BufferedReader(new FileReader(uploadPath + File.separator + snapshot2
                    + File.separator + "objects" + File.separator + fileName + ".xml"));

            PrintWriter pw = new PrintWriter(uploadPath + File.separator + snapshot2 + File.separator + "objects"
                    + File.separator + "tmp" + ".xml");

            String line1 = reader1.readLine();

            String line2 = reader2.readLine();

            String tmp = "";
            int lineNum = 1;

            while (line1 != null) {
                if (lineNum == position) {
                    tmp = line1;
                }
                line1 = reader1.readLine();

                lineNum++;
            }

            lineNum = 1;
            while (line2 != null) {
                if (lineNum == position) {
                    if (type.equals("DELETE")) {
                        pw.println(tmp);
                        pw.println(line2);
                    } else if (type.equals("CHANGE")) {
                        pw.println(tmp);
                    } else if (type.equals("INSERT")) {

                    }
                } else {
                    pw.println(line2);
                }

                line2 = reader2.readLine();

                lineNum++;
            }

            pw.flush();
            pw.close();
            reader1.close();
            reader2.close();

            File new_file = new File(uploadPath + File.separator + snapshot2 + File.separator + "objects"
                    + File.separator + fileName + ".xml");
            File old_file = new File(uploadPath + File.separator + snapshot2 + File.separator + "objects"
                    + File.separator + "tmp" + ".xml");
            new_file.delete();
            old_file.renameTo(new_file);
        } else {
            File source = new File(uploadPath + File.separator + snapshot1 + File.separator + "objects" + File.separator
                    + fileName + ".xml");
            File dest = new File(uploadPath + File.separator + snapshot2 + File.separator + "objects" + File.separator
                    + fileName + ".xml");

            if (dest.exists()) {
                dest.delete();
            }
            Files.copy(source.toPath(), dest.toPath());
        }
    }

    public void updatePackageFile(String uploadPath, String snapshot1, String snapshot2, String fileName, int position,
            String objectName, String name, String objectType, String changeType) throws IOException {
        if (changeType.equals("CHANGE")) {

            String snapshotId = getUUID();

            String object = "<object id=\"" + objectName + "\" versionId=\"" + snapshotId + "\" name=\"" + name
                    + "\" type=\"" + objectType + "\"/>";

            BufferedReader reader2 = new BufferedReader(new FileReader(uploadPath + File.separator + snapshot2
                    + File.separator + "META-INF" + File.separator + fileName + ".xml"));

            PrintWriter pw = new PrintWriter(uploadPath + File.separator + snapshot2 + File.separator + "META-INF"
                    + File.separator + "tmp" + ".xml");

            String line2 = reader2.readLine();

            while (line2 != null) {
                if (line2.contains("object id")) {
                    String objectName2 = getObjectName(line2);
                    String objectType2 = getObjectType(line2);
                    if (name.equals(objectName2) && objectType2.equals(objectType)) {
                        pw.println(object);
                    } else {
                        pw.println(line2);
                    }
                } else {
                    pw.println(line2);
                }

                line2 = reader2.readLine();

            }

            pw.flush();
            pw.close();
            reader2.close();

        } else if (changeType.equals("INSERT")) {
            // delete object
            BufferedReader reader2 = new BufferedReader(new FileReader(uploadPath + File.separator + snapshot2
                    + File.separator + "META-INF" + File.separator + fileName + ".xml"));

            PrintWriter pw = new PrintWriter(uploadPath + File.separator + snapshot2 + File.separator + "META-INF"
                    + File.separator + "tmp" + ".xml");

            String line2 = reader2.readLine();

            while (line2 != null) {
                if (line2.contains("object id")) {

                    String objectName2 = getObjectName(line2);
                    String objectType2 = getObjectType(line2);

                    if (name.equals(objectName2) && objectType2.equals(objectType)) {
                        // do nothing
                    } else {
                        pw.println(line2);
                    }
                } else {
                    pw.println(line2);
                }

                line2 = reader2.readLine();
            }

            pw.flush();
            pw.close();
            reader2.close();
        } else if (changeType.equals("DELETE")) {
            // insert object

            BufferedReader reader1 = new BufferedReader(new FileReader(uploadPath + File.separator + snapshot1
                    + File.separator + "META-INF" + File.separator + fileName + ".xml"));
            BufferedReader reader2 = new BufferedReader(new FileReader(uploadPath + File.separator + snapshot2
                    + File.separator + "META-INF" + File.separator + fileName + ".xml"));

            PrintWriter pw = new PrintWriter(uploadPath + File.separator + snapshot2 + File.separator + "META-INF"
                    + File.separator + "tmp" + ".xml");

            String line1 = reader1.readLine();
            String tmp = "";
            while (line1 != null) {
                if (line1.contains("object id")) {
                    String objectName1 = getObjectName(line1);
                    String objectType1 = getObjectType(line1);
                    if (objectName1.equals(name) && objectType1.equals(objectType)) {
                        tmp = line1;
                        break;
                    }
                }

                line1 = reader1.readLine();
            }

            reader1.close();

            String line2 = reader2.readLine();

            String prevType = "";
            String curType = "";
            while (line2 != null) {
                if (line2.contains("object id")) {
                    curType = getObjectType(line2);
                    if (!prevType.equals("") && prevType != curType) {
                        if (prevType.equals("process") && objectType.equals("process")) {
                            pw.println(tmp);
                            pw.println(line2);
                        } else if (objectType.equals("twClass") && prevType.equals("process")
                                && (!curType.equals("twClass"))) {
                            pw.println(tmp);
                            pw.println(line2);
                        } else if (objectType.equals("coachView") && prevType.equals("projectDefaults")
                                && (!curType.equals("coachView"))) {
                            pw.println(tmp);
                            pw.println(line2);
                        } else {
                            pw.println(line2);
                        }
                    } else {
                        pw.println(line2);
                    }

                    prevType = getObjectType(line2);
                } else {
                    pw.println(line2);
                }

                line2 = reader2.readLine();
            }

            pw.flush();
            pw.close();
            reader2.close();
        }

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

    private static String getObjectName(String line) {
        int fileNameIndex = line.indexOf("name=");
        int typeIndex = line.indexOf("type=");

        return line.substring(fileNameIndex + 6, typeIndex - 2);
    }

    private static String getObjectType(String line) {
        int typeIndex = line.indexOf("type=");
        return line.substring(typeIndex + 6, line.length() - 3);
    }
}
