package com.twq.databind;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClassCreateHelper {
    static final int MODEL = 0;
    static final int PRESENTER = 1;
    static final int VIEW = 2;
    static final int CONTRACT = 0;


    public ClassCreateHelper() {
    }

    private static String getPackageName(String path) {
        String[] strings = path.split("/");
        StringBuilder packageName = new StringBuilder();
        int index = 0;
        int length = strings.length;

        int j;
        for (j = 0; j < strings.length; ++j) {
            if (strings[j].equals("com") || strings[j].equals("org") || strings.equals("cn")) {
                index = j;
                break;
            }
        }

        for (j = index; j < length - 2; ++j) {
            packageName.append(strings[j] + ".");
        }

        return packageName.toString();
    }

    public static void createImplClass(AnActionEvent event, final String filePath, String packageName, int type, ClassModel classModel) {
        WriteCommandAction.runWriteCommandAction(event.getProject(), new Runnable() {
            @Override
            public void run() {
                System.out.println("filePath:" + filePath + " packageName:" + packageName + " functionName:" + classModel.getFunctionName() + " fullClassName:" + classModel.getClassName());
                String functionName = classModel.getFunctionName();
                String contractName = classModel.getClassName();
                String typeName = "";
                String implType = "implements";
                switch (type) {
                    case 0:
                        typeName = "Model";
                        break;
                    case 1:
                        typeName = "Presenter";
                        implType = "extends";
                }

                System.out.println("功能名称" + functionName);
                String implClassName = functionName + typeName;
                String filePathTwo = null;
                filePathTwo = filePath + implClassName + ".java";
                System.out.println("生成文件路径:" + filePathTwo);
                File file = new File(filePathTwo);

                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    writer.write("package " + packageName + ";");
                    writer.newLine();
                    writer.newLine();
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                    writer.write("/**\n* Created by MVPDataBind on " + sdf.format(date) + "\n*/");
                    writer.newLine();
                    writer.write("public class " + implClassName + " " + implType + " " + contractName + "." + typeName + "{");
                    writer.newLine();
                    writer.newLine();
                    writer.write("}");
                    writer.flush();
                    writer.close();
                } catch (Exception var13) {
                    var13.printStackTrace();
                }

            }
        });

    }
}
